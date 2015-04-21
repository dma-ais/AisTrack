package dk.dma.ais.track.rest.resource.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class TargetNotFoundException extends RuntimeException {
    public TargetNotFoundException(int mmsi) {
        super("No target with MMSI " + mmsi);
    }
}
