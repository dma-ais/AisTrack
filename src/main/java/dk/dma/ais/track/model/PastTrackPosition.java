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
package dk.dma.ais.track.model;

import java.io.Serializable;

public class PastTrackPosition implements Serializable, Comparable<PastTrackPosition> {

    private static final long serialVersionUID = 1L;

    private double lat;
    private double lon;
    private double cog;
    private double sog;
    private long time;

    public PastTrackPosition() {
    }

    public PastTrackPosition(double lat, double lon, double cog, double sog, long time) {
        super();
        this.lat = lat;
        this.lon = lon;
        this.cog = cog;
        this.sog = sog;
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getCog() {
        return cog;
    }

    public double getSog() {
        return sog;
    }

    public long getTime() {
        return time;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setCog(double cog) {
        this.cog = cog;
    }

    public void setSog(double sog) {
        this.sog = sog;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isDead(int ttl) {
        int elapsed = (int) ((System.currentTimeMillis() - time) / 1000);
        return elapsed > ttl;
    }

    @Override
    public int compareTo(PastTrackPosition p2) {
        if (this.time < p2.time) {
            return -1;
        } else if (this.time > p2.time) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object p2) {
        return time == ((PastTrackPosition) p2).getTime();
    }

    @Override
    public int hashCode() {
        return (int) time;
    }

}
