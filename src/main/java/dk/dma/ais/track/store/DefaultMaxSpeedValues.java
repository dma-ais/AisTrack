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

import java.util.HashMap;
import java.util.Map;

/**
 * Returns the default max-speed values for a vessel of the given AIS type.
 */
public class DefaultMaxSpeedValues {

    private static final Map<Integer, Float> MAX_SPEEDS = new HashMap<>();

    static {
        // Based on statistics received from ESR@dma.dk
        setMaxSpeedForTypes(20, 29, 100f);
        setMaxSpeedForType(30, 11.5f);
        setMaxSpeedForType(31, 12.1f);
        setMaxSpeedForType(32, 12.1f);
        setMaxSpeedForType(33, 11.3f);
        setMaxSpeedForType(34, 18f);
        setMaxSpeedForType(35, 30f);
        setMaxSpeedForType(36, 6f);
        setMaxSpeedForType(37, 15f);
        setMaxSpeedForTypes(40, 49, 40f);
        setMaxSpeedForType(50, 18.7f);
        setMaxSpeedForType(51, 30f);
        setMaxSpeedForType(52, 12.1f);
        setMaxSpeedForType(53, 11.9f);
        setMaxSpeedForType(54, 18f);
        setMaxSpeedForType(55, 20f);
        setMaxSpeedForType(58, 15.7f);
        setMaxSpeedForTypes(60, 69, 19.5f);
        setMaxSpeedForTypes(70, 79, 15.1f);
        setMaxSpeedForTypes(80, 89, 13.6f);
    }

    /**
     * Defines the max speed for a vessel of the given AIS type
     *
     * @param type the AIS type
     * @param maxSpeed the max speed
     */
    private static void setMaxSpeedForType(int type, float maxSpeed) {
        MAX_SPEEDS.put(type, maxSpeed);
    }

    /**
     * Defines the max speed for a vessel of the given consecutive AIS type interval.
     *
     * @param typeFrom the AIS type minimum value
     * @param typeTo the AIS type maximum value
     * @param maxSpeed the max speed
     */
    private static void setMaxSpeedForTypes(int typeFrom, int typeTo, float maxSpeed) {
        for (int type = typeFrom; type <= typeTo; type++) {
            MAX_SPEEDS.put(type, maxSpeed);
        }
    }

    /**
     * Returns the max speed for a vessel of the given AIS type
     *
     * @param type the AIS type
     */
    public static float getMaxSpeedForType(int type) {
        Float maxSpeed = MAX_SPEEDS.get(type);
        return (maxSpeed == null) ? 0f : maxSpeed;
    }
}

