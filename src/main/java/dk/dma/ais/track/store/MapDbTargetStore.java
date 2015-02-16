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
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.track.AisTrackConfiguration;
import dk.dma.ais.track.model.Target;

public class MapDbTargetStore<T extends Target> implements TargetStore<T> {

    static final Logger LOG = LoggerFactory.getLogger(MapDbTargetStore.class);

    private final Map<Integer, T> map;
    private final MapDb<Integer, T> db;
    private final ScheduledExecutorService expireExecutor;
    private final long expiryTime;

    @Inject
    public MapDbTargetStore(AisTrackConfiguration cfg) throws IOException {
        LOG.info("Loading target database using backup dir: " + cfg.backup());
        Files.createDirectories(Paths.get(cfg.backup()));
        expiryTime = cfg.targetExpire().toMillis();
        final long cleanupInterval = cfg.cleanupInterval().toMillis();
        db = MapDb.create(cfg.backup(), "targetdb");
        if (db == null) {
            System.exit(-1);
        }
        map = db.getMap();
        LOG.info(map.size() + " targets loaded");
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
            long now = System.currentTimeMillis();
            long removed = 0;
            for (T target : map.values()) {
                Date lastReport = target.getLastReport();
                long age = now - lastReport.getTime();
                if (age > expiryTime) {
                    map.remove(target.getMmsi());
                    removed++;
                }
            }
            if (removed > 0) {
                LOG.info("Cleaned up " + removed + " targets in " + (System.currentTimeMillis() - now) + " ms");
            }
            db.getDb().compact();
        } catch (Exception e) {
            LOG.error("Failed clean-up", e);
        }
    }

    @Override
    public T get(int mmsi) {
        return map.get(mmsi);
    }

    @Override
    public void put(T target) {
        map.put(target.getMmsi(), target);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<T> list() {
        return map.values();
    }
    
    @Override
    public void close() {
        LOG.info("Stopping target store expiry thread");
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

}
