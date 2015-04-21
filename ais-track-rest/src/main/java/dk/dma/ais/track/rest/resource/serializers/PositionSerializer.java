package dk.dma.ais.track.rest.resource.serializers;

import com.fasterxml.jackson.databind.JsonSerializer;
import dk.dma.enav.model.geometry.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PositionSerializer extends JsonSerializer<Position> {

    private static final Logger LOG = LoggerFactory.getLogger(PositionSerializer.class);

    @Override
    public void serialize(Position pos, com.fasterxml.jackson.core.JsonGenerator jg, com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws IOException, com.fasterxml.jackson.core.JsonProcessingException {
        jg.writeStartObject();
        jg.writeNumberField("lat", (float) pos.getLatitude());
        jg.writeNumberField("lon", (float) pos.getLongitude());
        jg.writeEndObject();
    }

}
