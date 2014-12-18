package dk.dma.ais.track.store;

import dk.dma.ais.track.model.MaxSpeed;
import dk.dma.ais.track.model.VesselTarget;

public interface MaxSpeedStore {
    
    void register(VesselTarget target);
    
    MaxSpeed getMaxSpeed(int mmsi);

}
