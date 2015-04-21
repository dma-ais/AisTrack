package dk.dma.ais.track.rest.resource.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CannotParseFilterExpressionException extends RuntimeException {
    public CannotParseFilterExpressionException(Exception e, String filterExpression) {
        super("Can't parse filter expression: \"" + filterExpression + "\": " + e.getMessage());
    }
}
