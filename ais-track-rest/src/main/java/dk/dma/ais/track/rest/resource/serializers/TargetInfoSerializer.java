package dk.dma.ais.track.rest.resource.serializers;

import com.fasterxml.jackson.databind.JsonSerializer;
import dk.dma.ais.tracker.targetTracker.TargetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TargetInfoSerializer extends JsonSerializer<TargetInfo> {

    private static final Logger LOG = LoggerFactory.getLogger(TargetInfoSerializer.class);

    @Override
    public void serialize(TargetInfo targetInfo, com.fasterxml.jackson.core.JsonGenerator jg, com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws IOException, com.fasterxml.jackson.core.JsonProcessingException {
        jg.useDefaultPrettyPrinter();

        jg.writeStartObject();
        jg.writeObjectField("source", targetInfo.getPacketSource());
        jg.writeObjectField("target", targetInfo.getAisTarget());
        jg.writeEndObject();
    }

}
