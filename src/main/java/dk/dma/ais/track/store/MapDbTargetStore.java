package dk.dma.ais.track.store;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import dk.dma.ais.track.model.Target;

public class MapDbTargetStore<T extends Target> implements TargetStore<T> {

    private ConcurrentNavigableMap<Integer, T> map;
    private DB db;

    public MapDbTargetStore() {
        // TODO settings from conf
        db = DBMaker.newFileDB(new File("targetdb")).closeOnJvmShutdown().transactionDisable().make();
        map = db.getTreeMap("vesselTargets");
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
    public void init() {
    }

    @Override
    public void close() {
    }

}
