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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import dk.dma.ais.track.AisTrackConfiguration;
import dk.dma.ais.track.model.PastTrackPosition;
import dk.dma.ais.track.model.VesselTarget;

public class MapPastTrackStore implements PastTrackStore {
    
    private static final long CLEANUP_INTERVAL = Duration.parse("PT10M").toMillis();
    
    protected final ConcurrentHashMap<Integer, PastTrack> trackMap = new ConcurrentHashMap<>();
    protected final int defaultMinPastTrackDist; 
    protected final long pastTrackTtl;
    protected final long expiryTime;
    
    protected long lastCleanup;
    
    @Inject
    public MapPastTrackStore(AisTrackConfiguration cfg) {
        defaultMinPastTrackDist = cfg.defaultMinPastTrackDist();
        pastTrackTtl = cfg.pastTrackTtl().toMillis();
        expiryTime = cfg.targetExpire().toMillis();
    }
        
    @Override
    public List<PastTrackPosition> get(int mmsi, Integer minDist) {
        if (minDist == null) {
            minDist = defaultMinPastTrackDist;
        }
        PastTrack track = trackMap.get(mmsi);
        if (track == null) {
            return null;
        }
        track.trim(pastTrackTtl);
        return PastTrack.downSample(track.asList(), minDist);
    }

    @Override
    public void add(VesselTarget target) {
        cleanup();
        if (!target.isValidPos()) {
            return;
        }
        trackMap.putIfAbsent(target.getMmsi(), new PastTrack());        
        PastTrack track = trackMap.get(target.getMmsi());
        track.add(new PastTrackPosition(target.getLat(), target.getLon(), target.getCog(), target.getSog(), target.getLastPosReport().getTime()));
        track.trim(pastTrackTtl);
    }
    
    private void cleanup() {
        long now = System.currentTimeMillis();
        if (lastCleanup + CLEANUP_INTERVAL > now) {
            return;
        }
        lastCleanup = now;
        for (Integer mmsi : trackMap.keySet()) {
            PastTrack track = trackMap.get(mmsi);
            PastTrackPosition newest = track.newestPos();
            if (newest == null) {
                trackMap.remove(mmsi);
                continue;
            }
            long age = now - newest.getTime();
            if (age > expiryTime) {
                trackMap.remove(mmsi);
            } else {
                track.trim(pastTrackTtl);
            }
        }
    }

    @Override
    public void remove(int mmsi) {
        trackMap.remove(mmsi);        
    }
    
    @Override
    public void close() {
    }

}