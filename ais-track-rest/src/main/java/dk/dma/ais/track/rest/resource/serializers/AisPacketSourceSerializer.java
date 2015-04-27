package dk.dma.ais.track.rest.resource.serializers;

import com.fasterxml.jackson.databind.JsonSerializer;
import dk.dma.ais.packet.AisPacketSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isBlank;

public class AisPacketSourceSerializer extends JsonSerializer<AisPacketSource> {

    private static final Logger LOG = LoggerFactory.getLogger(AisPacketSourceSerializer.class);

    @Override
    public void serialize(AisPacketSource packetSource, com.fasterxml.jackson.core.JsonGenerator jg, com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws IOException, com.fasterxml.jackson.core.JsonProcessingException {
        jg.writeStartObject();
        if (packetSource != null) {
            if (packetSource.getSourceCountry() != null)
                jg.writeStringField("country", packetSource.getSourceCountry().getTwoLetter());

            if (packetSource.getSourceType() != null)
                jg.writeStringField("type", packetSource.getSourceType().toString());

            if (packetSource.getSourceBaseStation() != null)
                jg.writeStringField("bs", packetSource.getSourceBaseStation().toString());

            if (packetSource.getSourceId() != null)
                jg.writeStringField("id", packetSource.getSourceId());

            if (!isBlank(packetSource.getSourceRegion()))
                jg.writeStringField("region", packetSource.getSourceRegion());
        }
        jg.writeEndObject();
    }

}
