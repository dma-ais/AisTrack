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
import java.util.ArrayList;
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
import dk.dma.ais.track.model.MaxSpeed;
import dk.dma.ais.track.model.VesselTarget;

public class MapDbMaxSpeedStore implements MaxSpeedStore {

    static final Logger LOG = LoggerFactory.getLogger(MapDbMaxSpeedStore.class);

    private final MapDb<Integer, MaxSpeedRing> db;
    private final BTreeMap<Integer, MaxSpeedRing> maxSpeedMap;
    private final ScheduledExecutorService expireExecutor;
    private final int ringSize;

    @Inject
    public MapDbMaxSpeedStore(AisTrackConfiguration cfg) throws IOException {
        LOG.info("Loading max speed database using backup dir: " + cfg.backup());
        Files.createDirectories(Paths.get(cfg.backup()));
        db = MapDb.create(cfg.backup(), "maxspeeddb");
        if (db == null) {
            System.exit(-1);
        }
        maxSpeedMap = db.getMap();
        LOG.info(maxSpeedMap.size() + " max speeds loaded");
        ringSize = cfg.maxSpeedRingSize();
        final long cleanupInterval = cfg.cleanupInterval().toMillis();
        final long expiryTime = (ringSize + 2) * 24 * 60 * 60 * 1000;
        expireExecutor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(cleanupInterval);
                    } catch (InterruptedException e) {
                        return;
                    }
                    long now = System.currentTimeMillis();
                    long removed = 0;
                    for (Map.Entry<Integer, MaxSpeedRing> entry : maxSpeedMap.entrySet()) {
                        entry.getValue().expire();
                        long age = now - entry.getValue().getLastUpdate();
                        if (age > expiryTime) {
                            maxSpeedMap.remove(entry.getKey());
                            removed++;
                        }
                    }
                    if (removed > 0) {
                        LOG.info("Max speed targets removed " + removed);
                    }                    
                    db.getDb().compact();
                }
            }
        };
        expireExecutor.execute(task);
    }
    
    @Override
    public List<MaxSpeed> getMaxSpeedList() {
        ArrayList<MaxSpeed> list = new ArrayList<>();
        for (Map.Entry<Integer, MaxSpeedRing> entry : maxSpeedMap.entrySet()) {
            list.add(new MaxSpeed(entry.getKey(), entry.getValue().getMaxSpeed()));
        }
        return list;
    }

    @Override
    public void register(VesselTarget target) {
        Double sog = target.getSog();
        if (!target.isValidPos() || sog == null) {
            return;
        }
        maxSpeedMap.putIfAbsent(target.getMmsi(), new MaxSpeedRing(ringSize));
        MaxSpeedRing ring = maxSpeedMap.get(target.getMmsi());
        ring.register(sog.floatValue());
    }

    @Override
    public MaxSpeed getMaxSpeed(int mmsi) {
        MaxSpeedRing ring = maxSpeedMap.get(mmsi);
        if (ring == null) {
            return null;
        }
        return new MaxSpeed(mmsi, ring.getMaxSpeed());
    }
    
    @Override
    public void close() {
        LOG.info("Stopping max speed store expiry thread");
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
        return maxSpeedMap.size();
    }


}
