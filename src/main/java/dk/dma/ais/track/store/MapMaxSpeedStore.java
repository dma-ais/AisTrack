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

import java.util.concurrent.ConcurrentHashMap;

import dk.dma.ais.track.model.MaxSpeed;
import dk.dma.ais.track.model.VesselTarget;

public class MapMaxSpeedStore implements MaxSpeedStore {

    final ConcurrentHashMap<Integer, Double> maxSpeedMap = new ConcurrentHashMap<>();

    public MapMaxSpeedStore() {

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

    @Override
    public void close() {
    }
    
    @Override
    public int size() {
        return maxSpeedMap.size();
    }

}
