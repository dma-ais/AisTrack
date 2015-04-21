package dk.dma.ais.track.rest.resource.serializers;

import com.fasterxml.jackson.databind.JsonSerializer;
import dk.dma.enav.model.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CountrySerializer extends JsonSerializer<Country> {

    private static final Logger LOG = LoggerFactory.getLogger(CountrySerializer.class);

    @Override
    public void serialize(Country country, com.fasterxml.jackson.core.JsonGenerator jg, com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws IOException, com.fasterxml.jackson.core.JsonProcessingException {
        jg.writeString(country.getTwoLetter());
    }

}
