package dk.dma.ais.track;

import dk.dma.ais.track.model.Target;

public class TargetFilter {

    protected int ttlLive;
    protected int ttlSat;

    public TargetFilter() {

    }

    public boolean test(Target target) {
        int ttl = target.getSourceType() != null && target.getSourceType().equals("SAT") ? ttlSat : ttlLive;
        if (ttl == 0) {
            return true;
        }
        long age = (System.currentTimeMillis() - target.getLastReport().getTime()) / 1000;
        return age < ttl;
    }

    public int getTtlLive() {
        return ttlLive;
    }

    public void setTtlLive(int ttlLive) {
        this.ttlLive = ttlLive;
    }

    public int getTtlSat() {
        return ttlSat;
    }

    public void setTtlSat(int ttlSat) {
        this.ttlSat = ttlSat;
    }

}
