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

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

public class MaxSpeedRing implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ArrayList<Float> ring;
    private long lastUpdate;

    public MaxSpeedRing(int size) {
        ring = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ring.add(0f);
        }
    }

    public synchronized void register(float speed) {
        register(LocalDate.now(), speed);
    }

    public synchronized void register(LocalDate now, float speed) {
        lastUpdate = System.currentTimeMillis();
        int nowDay = (int) (now.toEpochDay() % size());                
        if (ring.get(nowDay) < speed) {
            ring.set(nowDay, speed);
        }
        expire(now);
    }
    
    public synchronized void expire() {
        expire(LocalDate.now());
    }
    
    public synchronized void expire(LocalDate now) {
        LocalDate old = now.minusDays(size() - 1);
        int tomDay = (int) (old.toEpochDay() % size());
        if (ring.get(tomDay) != 0) {
            ring.set(tomDay, 0f);
        }        
    }
    
    public synchronized float getMaxSpeed() {
        float maxSpeed = 0f;
        for (Float speed : ring) {
            maxSpeed = Math.max(maxSpeed, speed);
        }
        return maxSpeed;
    }

    public synchronized int size() {
        return ring.size();
    }
    
    public synchronized long getLastUpdate() {
        return lastUpdate;
    }

}
