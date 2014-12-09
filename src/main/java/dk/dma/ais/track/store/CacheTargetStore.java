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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import dk.dma.ais.track.AisTrackConfiguration;
import dk.dma.ais.track.model.Target;

public class CacheTargetStore<T extends Target> implements TargetStore<T> {

    private Cache<Integer, T> cache;

    @Inject
    public CacheTargetStore(AisTrackConfiguration cfg) {
        cache = CacheBuilder.newBuilder().expireAfterWrite(cfg.targetExpire().toMillis(), TimeUnit.MILLISECONDS).maximumSize(5000000).build();
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
    
}
