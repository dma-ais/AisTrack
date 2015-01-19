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
package dk.dma.ais.track.resource;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.dma.ais.track.AisTrackConfiguration;

@Singleton
@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    static final Logger LOG = LoggerFactory.getLogger(MetricsResource.class);

    final ObjectMapper mapper = new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, false,
            MetricFilter.ALL));
    final MetricRegistry metrics;
    final AisTrackConfiguration cfg;

    @Inject
    public MetricsResource(MetricRegistry metrics, AisTrackConfiguration cfg) {
        this.metrics = metrics;
        this.cfg = cfg;
    }

    @GET
    @Path("/all")
    public String all() {
        try {
            return mapper.writeValueAsString(metrics);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to generate metrics", e);
            throw new InternalServerErrorException();
        }
    }

    @GET
    @Path("/flow/{meter}")
    public String flow(@PathParam("meter") String meter, @DefaultValue("0.0") @QueryParam("expected") Double expexted) {
        Meter m = metrics.getMeters().get(meter);
        double rate = m.getFiveMinuteRate();
        HashMap<String, String> map = new HashMap<>();
        map.put("status", rate >= expexted ? "ok" : "error");
        map.put("rate", Double.toString(rate));
        map.put("expexted", Double.toString(expexted));
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to generate metrics", e);
            throw new InternalServerErrorException();
        }
    }
    
    @GET
    @Path("/properties")
    public String properties() {
        HashMap<String, String> map = new HashMap<>();
        for (String key : cfg.propertyNames()) {
            map.put(key, cfg.getProperty(key));
        }
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to generate properties", e);
            throw new InternalServerErrorException();
        }
    }

}
