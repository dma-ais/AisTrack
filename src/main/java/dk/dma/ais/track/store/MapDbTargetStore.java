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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.track.AisTrackConfiguration;
import dk.dma.ais.track.model.Target;

public class MapDbTargetStore<T extends Target> implements TargetStore<T> {

    static final Logger LOG = LoggerFactory.getLogger(MapDbTargetStore.class);

    private final Map<Integer, T> map;
    private final DB db;

    @Inject
    public MapDbTargetStore(AisTrackConfiguration cfg) throws IOException {
        LOG.info("Loading target database using backup dir: " + cfg.backup());
        Files.createDirectories(Paths.get(cfg.backup()));
        final long expiryTime = cfg.targetExpire().toMillis();
        final long cleanupInterval = cfg.cleanupInterval().toMillis();
        db = DBMaker.newFileDB(new File(cfg.backup() + "/targetdb")).closeOnJvmShutdown().transactionDisable().make();
        map = db.getTreeMap("vesselTargets");
        LOG.info(map.size() + " targets loaded");
        ScheduledExecutorService expireExecutor = Executors.newSingleThreadScheduledExecutor();
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
                    for (T target : map.values()) {
                        Date lastReport = target.getLastReport();
                        long age = now - lastReport.getTime();
                        if (age > expiryTime) {
                            map.remove(target.getMmsi());
                        }
                    }
                    db.compact();
                }
            }
        };
        expireExecutor.execute(task);
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

}
