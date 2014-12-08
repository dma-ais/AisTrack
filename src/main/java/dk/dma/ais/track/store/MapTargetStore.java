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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dk.dma.ais.track.model.Target;

public class MapTargetStore<T extends Target> implements TargetStore<T> {

    private final Map<Integer, T> map = new ConcurrentHashMap<>();

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
