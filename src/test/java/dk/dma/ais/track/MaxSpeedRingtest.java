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
package dk.dma.ais.track;

import java.time.LocalDate;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.ais.track.store.MaxSpeedRing;

public class MaxSpeedRingtest {
    
    @Test
    public void simpleRingTest() {
        MaxSpeedRing ring = new MaxSpeedRing(30);
        Random rand = new Random(System.currentTimeMillis());        
        LocalDate now = LocalDate.now();
        
        for (int d=0; d < 1000; d++) {            
            for (int i=0; i < 100; i++) {
                float speed = rand.nextFloat() * 20.0f;
                ring.register(now, speed);
            }
            if (d == 100) {
                ring.register(now, 1000f);
            }
            if (d == 970) {
                ring.register(now, 2000f);
            }            
            if (d == 971) {
                ring.register(now, 21f);
            }
            now = now.plusDays(1);
        }        
        Assert.assertEquals(21f, ring.getMaxSpeed(), 0.01f);
    }

}
