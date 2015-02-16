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
package dk.dma.ais.track.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.mapdb.BTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.track.AisTrackConfiguration;
import dk.dma.ais.track.model.PastTrackPosition;
import dk.dma.ais.track.model.VesselTarget;

public class MapDbPastTrackStore extends AbstractPastTrackStore implements PastTrackStore {

    static final Logger LOG = LoggerFactory.getLogger(MapDbPastTrackStore.class);

    private final MapDb<Integer, PastTrack> db;
    private final BTreeMap<Integer, PastTrack> trackMap;
    private final ScheduledExecutorService expireExecutor;

    @Inject
    public MapDbPastTrackStore(AisTrackConfiguration cfg) throws IOException {
        super(cfg);
        LOG.info("Loading past track database using backup dir: " + cfg.backup());
        Files.createDirectories(Paths.get(cfg.backup()));
        db = MapDb.create(cfg.backup(), "pasttrackdb");
        if (db == null) {
            System.exit(-1);
        }
        trackMap = db.getMap();
        LOG.info(trackMap.size() + " tracks loaded");
        final long cleanupInterval = cfg.cleanupInterval().toMillis();
        expireExecutor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            while (true) {
                try {
                    Thread.sleep(cleanupInterval);
                } catch (InterruptedException e) {
                    return;
                }
                periodicCleanUp();
            }
        };
        expireExecutor.execute(task);
    }

    /**
     * Called periodically to remove stale data
     */
    private void periodicCleanUp() {
        try {
            long t0 = System.currentTimeMillis();
            long removedPoints = 0;
            List<Integer> removedTracks = new ArrayList<>();
            Map<Integer, PastTrack> updatedTracks = new HashMap<>();
            for (Map.Entry<Integer, PastTrack> entry : trackMap.entrySet()) {
                PastTrack track = entry.getValue();
                if (track.needsTrimming(pastTrackTtl)) {
                    track = new PastTrack(track);
                    removedPoints += track.trim(pastTrackTtl);
                    if (track.size() == 0) {
                        removedTracks.add(entry.getKey());
                    } else {
                        updatedTracks.put(entry.getKey(), track);
                    }
                }
            }

            removedTracks.forEach(trackMap::remove);
            for (Map.Entry<Integer, PastTrack> entry : updatedTracks.entrySet()) {
                trackMap.put(entry.getKey(), entry.getValue());
            }

            if (removedPoints > 0 || removedTracks.size() > 0) {
                LOG.info("Removed " + removedPoints + " past track points and " + removedTracks.size() + " past tracks in " +
                        (System.currentTimeMillis() - t0) + " ms");
            }
            db.getDb().compact();

        } catch (Exception e) {
            LOG.error("Failed clean-up", e);
        }
    }

    @Override
    public List<PastTrackPosition> get(int mmsi, Integer minDist, Duration age) {
        return get(mmsi, minDist, age, trackMap);
    }

    @Override
    public void add(VesselTarget target) {
        if (!target.isValidPos()) {
            return;
        }

        PastTrack track = trackMap.get(target.getMmsi());
        if (track != null) {
            // NB: Never modify a track that has been added to MspDB:
            // See: http://www.mapdb.org/doc/caches.html
            track = new PastTrack(track);
        } else {
            track = new PastTrack();
        }

        track.add(new PastTrackPosition(target.getLat(), target.getLon(), target.getCog(), target.getSog(), target
                .getLastPosReport().getTime()));
        track.trim(pastTrackTtl);

        trackMap.put(target.getMmsi(), track);
    }

    @Override
    public void close() {
        LOG.info("Stopping past track store expiry thread");
        expireExecutor.shutdownNow();
        try {
            expireExecutor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("Closing database");
        db.close();
        LOG.info("Database closed");
    }
    
    @Override
    public int size() {
        return trackMap.size();
    }

}
