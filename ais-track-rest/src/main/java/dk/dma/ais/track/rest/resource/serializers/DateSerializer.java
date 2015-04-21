package dk.dma.ais.track.rest.resource.serializers;

import com.fasterxml.jackson.databind.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

public class DateSerializer extends JsonSerializer<Date> {

    private static final Logger LOG = LoggerFactory.getLogger(DateSerializer.class);

    @Override
    public void serialize(Date date, com.fasterxml.jackson.core.JsonGenerator jg, com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws IOException, com.fasterxml.jackson.core.JsonProcessingException {
        jg.writeString(String.valueOf(Instant.ofEpochMilli(date.getTime())));
    }

}
