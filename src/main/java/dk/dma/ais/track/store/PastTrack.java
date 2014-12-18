package dk.dma.ais.track.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import dk.dma.ais.track.model.PastTrackPosition;
import dk.dma.enav.model.geometry.Position;

public class PastTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    private TreeSet<PastTrackPosition> track = new TreeSet<>();

    public synchronized void add(PastTrackPosition pos) {
        track.add(pos);
    }

    public synchronized List<PastTrackPosition> asList() {
        return new ArrayList<PastTrackPosition>(track);
    }

    public synchronized void trim(long ttl) {
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
    }
    
    public synchronized PastTrackPosition newestPos() {
        if (track.size() > 0) {
            return track.last();
        }
        return null;
    }

    public static List<PastTrackPosition> downSample(List<PastTrackPosition> list, int minPastTrackDist) {
        List<PastTrackPosition> downSampled = new ArrayList<>();
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
        return downSampled;
    }
    
    public static double distance(PastTrackPosition pos1, PastTrackPosition pos2) {
        return Position.create(pos1.getLat(), pos1.getLon()).rhumbLineDistanceTo(Position.create(pos2.getLat(), pos2.getLon()));        
    }

}