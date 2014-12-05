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

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import dk.dma.ais.track.AisTrackHandler;
import dk.dma.ais.track.VesselTargetFilter;
import dk.dma.ais.track.model.VesselTarget;

@Singleton
@Path("/target/vessel")
@Produces(MediaType.APPLICATION_JSON)
public class VesselResource extends AbstractResource {
    
    @GET
    @Path("{mmsi}")
    public VesselTarget getTarget(@PathParam("mmsi") Integer mmsi) {
        VesselTarget target = handler().getVessel(mmsi);
        if (target == null) {
            throw new NotFoundException();
        }
        return target;
    }
    
    /**
     * Filtering parameters:
     *  - ttlLive: See https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-
     *  - ttlSat: See https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-
     *  - mmsi: Filter on mmsi number, multiple arguments can be given
     *  - geo: Filter on geography, multiple arguments on the form: 'circle,lat,lon,radius(m)' or 'bb,lat1,lon1,lat2,lon2'  (bounding box) 
     * 
     * @param uriInfo
     * @return
     */
    @GET
    @Path("/list")
    public List<VesselTarget> getTargetList(@Context UriInfo uriInfo) {
        return handler().getVesselList(VesselTargetFilter.create(uriInfo));
    }
    
    @GET
    @Path("/count")
    public String getTargetCount(@Context UriInfo uriInfo) {
        return String.format("{\"count\" : %d}", handler().getVesselList(VesselTargetFilter.create(uriInfo)).size());
    }
    
    @GET
    @Path("/track/{mmsi}")
    public String getTrack(@PathParam("mmsi") Integer mmsi) {
        return "{\"todo\" : \"yes\"}";
    }
    
    private AisTrackHandler handler() {
        return get(AisTrackHandler.class);
    }

}
