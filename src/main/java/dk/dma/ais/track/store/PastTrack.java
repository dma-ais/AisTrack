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

import dk.dma.ais.track.model.PastTrackPosition;
import dk.dma.enav.model.geometry.Position;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class PastTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    private TreeSet<PastTrackPosition> track = new TreeSet<>();

    /**
     * Constructor
     */
    public PastTrack() {
    }

    /**
     * Clone constructor
     * @param pastTrack the past track to clone
     */
    public PastTrack(PastTrack pastTrack) {
        // TODO: Figure out if we need to deep clone the individual tracks
        track.addAll(pastTrack.track);
    }

    public synchronized void add(PastTrackPosition pos) {
        track.add(pos);
    }

    public synchronized List<PastTrackPosition> asList() {
        return new ArrayList<>(track);
    }

    public synchronized boolean needsTrimming(long ttl) {
        long maxAge = System.currentTimeMillis() - ttl;
        for (PastTrackPosition pos : track) {
            if (pos.getTime() < maxAge) {
                return true;
            }
        }
        return false;
    }

    public synchronized int trim(long ttl) {
        long maxAge = System.currentTimeMillis() - ttl;
        List<PastTrackPosition> removeSet = new ArrayList<>();
        for (PastTrackPosition pos : track) {
            if (pos.getTime() < maxAge) {
                removeSet.add(pos);
            } else {
                break;
            }
        }
        for (PastTrackPosition old : removeSet) {
            track.remove(old);
        }
        return removeSet.size();
    }

    public synchronized PastTrackPosition newestPos() {
        if (track.size() > 0) {
            return track.last();
        }
        return null;
    }

    public synchronized int size() {
        return track.size();
    }

    public static List<PastTrackPosition> downSample(List<PastTrackPosition> list, int minPastTrackDist, long age) {
        long maxAge = System.currentTimeMillis() - age;
        ArrayList<PastTrackPosition> downSampled = new ArrayList<>();
        if (list.size() == 0) {
            return downSampled;
        }
        downSampled.add(list.get(0));
        int i = 0;
        int n;
        while (i < list.size()) {
            PastTrackPosition pos = list.get(i);
            n = i + 1;
            while (n < list.size()) {
                PastTrackPosition next = list.get(n);
                if (distance(pos, next) > minPastTrackDist) {
                    downSampled.add(next);
                    break;
                }
                n++;
            }
            i = n;
        }
        int start = 0;
        while (start < downSampled.size() && downSampled.get(start).getTime() < maxAge) {
            start++;
        }
        return downSampled.subList(start, downSampled.size());
    }

    public static double distance(PastTrackPosition pos1, PastTrackPosition pos2) {
        return Position.create(pos1.getLat(), pos1.getLon()).rhumbLineDistanceTo(Position.create(pos2.getLat(), pos2.getLon()));
    }

}
