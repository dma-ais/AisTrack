package dk.dma.ais.track.store;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.track.model.Target;

public class MapDbTargetStore<T extends Target> implements TargetStore<T> {

    static final Logger LOG = LoggerFactory.getLogger(MapDbTargetStore.class);

    private final Map<Integer, T> map;
    private final DB db;
    private final long expiryTime = TimeUnit.HOURS.toMillis(24);
    private final long cleanupInterval = TimeUnit.MINUTES.toMillis(1);

    public MapDbTargetStore() {
        // TODO settings from conf
        LOG.info("Loading target database");
        db = DBMaker.newFileDB(new File("targetdb")).closeOnJvmShutdown().transactionDisable().make();
        map = db.getTreeMap("vesselTargets");
        LOG.info(map.size() + " targets loaded");
    }

    @Override
    public void init() {        
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
                    LOG.info("Target cleanup");
                    long removed = 0;
                    long now = System.currentTimeMillis();
                    for (T target : map.values()) {
                        Date lastReport = target.getLastReport();
                        long age = now - lastReport.getTime();
                        if (age > expiryTime) {
                            map.remove(target.getMmsi());
                            removed++;
                        }
                    }
                    db.compact();
                    LOG.info("Removed " + removed + " old targets");                    
                }
            }
        };
        expireExecutor.execute(task);
    }

    @Override
    public void close() {
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
