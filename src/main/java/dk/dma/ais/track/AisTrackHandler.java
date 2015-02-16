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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisTargetType;
import dk.dma.ais.message.IVesselPositionMessage;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.track.model.MaxSpeed;
import dk.dma.ais.track.model.PastTrackPosition;
import dk.dma.ais.track.model.VesselTarget;
import dk.dma.ais.track.store.MaxSpeedStore;
import dk.dma.ais.track.store.PastTrackStore;
import dk.dma.ais.track.store.TargetStore;

public class AisTrackHandler implements Consumer<AisPacket> {

    static final Logger LOG = LoggerFactory.getLogger(AisTrackHandler.class);

    private final TargetStore<VesselTarget> vesselStore;
    private final PastTrackStore pastTrackStore;
    private final MaxSpeedStore maxSpeedStore;
    private final boolean pastTrack;
    private final boolean registerMaxSpeed;
    private final Meter messages;
    private boolean stopped;

    @Inject
    public AisTrackHandler(TargetStore<VesselTarget> vesselStore, PastTrackStore pastTrackStore, MaxSpeedStore maxSpeedStore,
            AisTrackConfiguration cfg, MetricRegistry metrics) {
        this.vesselStore = vesselStore;
        this.pastTrackStore = pastTrackStore;
        this.pastTrack = cfg.pastTrack();
        this.maxSpeedStore = maxSpeedStore;
        this.registerMaxSpeed = cfg.registerMaxSpeed();
        this.messages = metrics.meter("messages");
        metrics.register(MetricRegistry.name("vesselStore", "size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return vesselStore.size();
            }
        });
        metrics.register(MetricRegistry.name("pastTrackStore", "size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return pastTrackStore.size();
            }
        });
        
        metrics.register(MetricRegistry.name("maxSpeedStore", "size"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return maxSpeedStore.size();
            }
        });       
    }

    @Override
    public void accept(AisPacket packet) {
        messages.mark();
        // Must have valid AIS message
        AisMessage message = packet.tryGetAisMessage();
        if (message == null) {
            return;
        }
        AisTargetType type = message.getTargetType();
        if (type == null) {
            return;
        }

        // Handle different types
        switch (type) {
        case A:
        case B:
            handleVessel(packet);
            break;
        default:
            break;
        }
    }

    private void handleVessel(AisPacket packet) {        
        AisMessage message = packet.tryGetAisMessage();

        // Reject invalid MMSI numbers
        if (message.getUserId() < 100000000 || message.getUserId() > 999999999) {
            return;
        }

        // Create vessel target
        VesselTarget target = new VesselTarget(packet);

        // Get existing entry
        VesselTarget oldTarget = vesselStore.get(target.getMmsi());

        // Avoid updating with old pos message
        boolean oldMessage = false;
        if (oldTarget != null && message instanceof IVesselPositionMessage) {
            Date oldPosReport = oldTarget.getLastPosReport();
            Date thisPosReport = target.getLastPosReport();
            if (oldPosReport != null && thisPosReport != null && oldPosReport.after(thisPosReport)) {
                oldMessage = true;
            }
        }

        // Handle target type change
        if (oldTarget != null && (target.getTargetType() != oldTarget.getTargetType())) {
            // Discard old target
            oldTarget = null;
        }

        // Register max speed
        if (registerMaxSpeed && !stopped) {
            maxSpeedStore.register(target);
        }

        // Save past track position
        if (pastTrack && !stopped) {
            pastTrackStore.add(target);
        }

        if (!oldMessage && !stopped) {
            // Merge and save
            if (oldTarget != null) {
                target = oldTarget.merge(target);
            }
            vesselStore.put(target);
        }

    }

    public VesselTarget getVessel(int mmsi) {
        return vesselStore.get(mmsi);
    }

    public TargetStore<VesselTarget> getVesselStore() {
        return vesselStore;
    }

    public List<VesselTarget> getVesselList(VesselTargetFilter vesselFilter) {
        List<VesselTarget> targets = new ArrayList<>();
        for (VesselTarget t : vesselStore.list()) {
            if (vesselFilter.test(t)) {
                targets.add(t);
            }
        }
        return targets;
    }

    public PastTrackStore getPastTrackStore() {
        return pastTrackStore;
    }

    public List<PastTrackPosition> getPastTrack(int mmsi, Integer minDist, Duration age) {
        List<PastTrackPosition> list = pastTrackStore.get(mmsi, minDist, age);
        if (list == null) {
            return null;
        }
        ArrayList<PastTrackPosition> track = new ArrayList<>(list);
        Collections.sort(track);

        return track;
    }

    public MaxSpeed getMaxSpeed(int mmsi) {
        return maxSpeedStore.getMaxSpeed(mmsi);
    }
    
    public List<MaxSpeed> getMaxSpeedList() {
        return maxSpeedStore.getMaxSpeedList();
    }


    public void stop() {
        stopped = true;

        if (pastTrackStore != null) {
            pastTrackStore.close();
        }
        if (vesselStore != null) {
            vesselStore.close();
        }
        if (maxSpeedStore != null) {
            maxSpeedStore.close();
        }
    }

}
