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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import dk.dma.ais.track.model.PastTrackPosition;
import dk.dma.ais.track.model.VesselTarget;

public class MapPastTrackStore implements PastTrackStore {
    
    ConcurrentHashMap<Integer, List<PastTrackPosition>> trackMap = new ConcurrentHashMap<>();
        
    @Override
    public List<PastTrackPosition> get(int mmsi) {
        return trackMap.get(mmsi);
    }

    @Override
    public void add(VesselTarget target) {
        if (!target.isValidPos()) {
            return;
        }
        trackMap.putIfAbsent(target.getMmsi(), new ArrayList<>());
        List<PastTrackPosition> track = trackMap.get(target.getMmsi());
        PastTrackPosition pos = new PastTrackPosition(target.getLat(), target.getLon(), target.getCog(), target.getSog(), target.getLastPosReport().getTime());
        track.add(pos);
    }

    @Override
    public void remove(int mmsi) {
        trackMap.remove(mmsi);        
    }

}
