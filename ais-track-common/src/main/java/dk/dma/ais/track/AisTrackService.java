/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.ais.track;

import dk.dma.ais.bus.AisBus;
import dk.dma.ais.bus.consumer.DistributerConsumer;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketFilters;
import dk.dma.ais.packet.AisPacketSource;
import dk.dma.ais.tracker.targetTracker.TargetInfo;
import dk.dma.ais.tracker.targetTracker.TargetTracker;
import dk.dma.ais.tracker.targetTracker.TargetTrackerFileBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.System.exit;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *  AisTrackService is a service which can receive a never-ending
 *  stream of AisPackets from an AisBus, and track individual vessels
 *  in the stream.
 */
@Service
@ThreadSafe
public class AisTrackService {

    private static final Logger LOG = LoggerFactory.getLogger(AisTrackService.class);
    { LOG.info("AisTrackService created.");  }

    /** Number of secs between status outputs */
    private final int secsBetweenStatus = 60;

    /** Number of minutes between cleanup */
    private final int minutesBetweenCleanup = 5;

    @Inject
    private AisBus aisBus;

    @Inject
    private TargetTracker tracker;

    @Inject
    private TargetTrackerFileBackupService backupService;

    @Value("${dk.dma.ais.track.AisTrackService.targetExpire}")
    private String targetExpire;

    private final Predicate<AisPacket> trackerInputPacketFilter;
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService statusExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService serviceExecutor = Executors.newSingleThreadExecutor();

    /** Create a TrackService with no input filter */
    public AisTrackService() {
        this(p -> true);
    }

    /**
     * Create a TrackService with an input filter
     * @param trackerInputPacketFilter
     */
    public AisTrackService(Predicate<AisPacket> trackerInputPacketFilter) {
        this.trackerInputPacketFilter = trackerInputPacketFilter;
    }

    public int numberOfTargets() {
        return tracker.size();
    }

    public int numberOfTargets(Predicate<? super AisPacketSource> packetSourceFilter) {
        return targets(packetSourceFilter, ti->true).size();
    }

    public Set<TargetInfo> targets() {
        return tracker
            .streamSequential()
            .collect(Collectors.toSet());
    }

    public Set<TargetInfo> targets(Predicate<? super AisPacketSource> packetSourceFilter, Predicate<? super TargetInfo> targetInfoFilter) {
        return tracker
            .streamSequential(packetSourceFilter, targetInfoFilter)
            .collect(Collectors.toSet());
    }

    public TargetInfo target(int mmsi, Predicate<? super AisPacketSource> packetSourceFilter) {
        return tracker.get(mmsi, packetSourceFilter);
    }

    public void start() {
        LOG.info("Starting AisTrackService");
        Objects.requireNonNull(aisBus);

        if(backupService != null){
            LOG.info("Starting {}", TargetTrackerFileBackupService.class.getSimpleName());
            backupService.startAsync();
            backupService.awaitRunning();
        } else {
            LOG.info("{} not available", TargetTrackerFileBackupService.class.getSimpleName());
        }

        if(cleanup()){
            Duration duration = Duration.parse(targetExpire);
            BiPredicate<AisPacketSource, TargetInfo> predicate = new LastReportFilter(duration);
            cleanupExecutor.scheduleAtFixedRate(() -> tracker.removeAll(predicate), minutesBetweenCleanup, minutesBetweenCleanup, MINUTES);
            LOG.info("Configured cleanup of targets older than {}", targetExpire);
        }else{
            LOG.info("AisTrackService.targetExpire not configured. Cleanup not configured.");
        }

        statusExecutor.scheduleAtFixedRate(() -> LOG.debug("Now tracking " + tracker.size() + " targets."), secsBetweenStatus, secsBetweenStatus, SECONDS);
        serviceExecutor.submit(() -> {
            startAisBus(packet -> {
                if (trackerInputPacketFilter.test(packet))
                    tracker.update(packet);
            });
        });
    }

    private void startAisBus(Consumer<AisPacket> packetConsumer) {
        LOG.debug("Starting AisBus");
        try {
            DistributerConsumer distributor = new DistributerConsumer();
            distributor.getConsumers().add(packetConsumer);
            distributor.init();
            aisBus.registerConsumer(distributor);
            aisBus.start();
            aisBus.startConsumers();
            aisBus.startProviders();
        } catch (Exception e) {
            LOG.error("Failed to start AisBus", e);
            exit(-1);
        }
        LOG.debug("AisBus started");
    }

    public void stop() {
        LOG.info("Stopping AisTrackService");
        if (aisBus != null) {
            aisBus.cancel();
        }

        serviceExecutor.shutdownNow();
        statusExecutor.shutdown();

        if(cleanup()){
            cleanupExecutor.shutdownNow();
        }

        if(backupService != null && backupService.isRunning()){
            backupService.stopAsync();
            backupService.awaitTerminated();
        }

        LOG.info("AisTrackService stopped.");
    }

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            LOG.error("Uncaught exception in thread " + t.getClass().getCanonicalName() + ": " + e.getMessage(), e);
            exit(-1);
        });

        AisTrackService trackService = new AisTrackService(AisPacketFilters.parseExpressionFilter("t.sog>2 & m.mmsi in (265522540, 219001000, 230985150, 230985150, 265588910, 357860000, 357860000)"));
        trackService.start();
    }

    private boolean cleanup(){
        return targetExpire != null && targetExpire.trim().length() != 0;
    }

    void setTargetTracker(TargetTracker targetTracker) {
        this.tracker = targetTracker;
    }

    void setTargetTrackerFileBackupService(TargetTrackerFileBackupService backupService) {
        this.backupService = backupService;
    }

    void setAisBus(AisBus aisBus) {
        this.aisBus = aisBus;
    }

    AisBus getAisBus() {
        return aisBus;
    }
}
