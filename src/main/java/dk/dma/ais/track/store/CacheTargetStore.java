package dk.dma.ais.track.store;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import dk.dma.ais.track.model.Target;

public class CacheTargetStore<T extends Target> implements TargetStore<T> {

    private Cache<Integer, T> cache;

    public CacheTargetStore() {

    }

    @Override
    public void init() {
        // TODO paramters from config
        cache = CacheBuilder.newBuilder().expireAfterWrite(48, TimeUnit.HOURS).maximumSize(5000000).build();
    }

    @Override
    public T get(int mmsi) {
        return cache.getIfPresent(mmsi);
    }

    @Override
    public void put(T target) {
        cache.put(target.getMmsi(), target);
    }

    @Override
    public int size() {
        return (int) cache.size();
    }

    @Override
    public Collection<T> list() {
        return cache.asMap().values();
    }
    
    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}
