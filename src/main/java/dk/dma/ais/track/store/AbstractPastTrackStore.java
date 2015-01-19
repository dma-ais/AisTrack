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
import java.util.Map;

import dk.dma.ais.track.AisTrackConfiguration;
import dk.dma.ais.track.model.PastTrackPosition;

public abstract class AbstractPastTrackStore implements PastTrackStore {

    protected final int defaultMinPastTrackDist;
    protected final long pastTrackTtl;
    protected final long expiryTime;

    public AbstractPastTrackStore(AisTrackConfiguration cfg) {
        defaultMinPastTrackDist = cfg.defaultMinPastTrackDist();
        pastTrackTtl = cfg.pastTrackTtl().toMillis();
        expiryTime = cfg.targetExpire().toMillis();
    }

    protected List<PastTrackPosition> get(int mmsi, Integer minDist, Duration age, Map<Integer, PastTrack> map) {
        if (minDist == null) {
            minDist = defaultMinPastTrackDist;
        }

        PastTrack track = map.get(mmsi);
        if (track == null) {
            return null;
        }
        long ageTtl = age != null ? age.toMillis() : Long.MAX_VALUE;
        track.trim(pastTrackTtl);
        return PastTrack.downSample(track.asList(), minDist, ageTtl);
    }

}
