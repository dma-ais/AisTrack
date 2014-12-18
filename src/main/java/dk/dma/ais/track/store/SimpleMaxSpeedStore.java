package dk.dma.ais.track.store;

import java.util.concurrent.ConcurrentHashMap;

import dk.dma.ais.track.model.MaxSpeed;
import dk.dma.ais.track.model.VesselTarget;

public class SimpleMaxSpeedStore implements MaxSpeedStore {
    
    final ConcurrentHashMap<Integer, Double> maxSpeedMap = new ConcurrentHashMap<>();
    
    public SimpleMaxSpeedStore() {
        
    }

    @Override
    public void register(VesselTarget target) {
        if (!target.isValidPos()) {
            return;
        }
        Double cur = maxSpeedMap.get(target.getMmsi());
        if (cur == null || cur.doubleValue() < target.getSog()) {
            maxSpeedMap.put(target.getMmsi(), target.getSog());
        }
    }

    @Override
    public MaxSpeed getMaxSpeed(int mmsi) {
        Double ms = maxSpeedMap.get(mmsi);
        if (ms == null) {
            return null;
        }
        return new MaxSpeed(ms);
    }

}
