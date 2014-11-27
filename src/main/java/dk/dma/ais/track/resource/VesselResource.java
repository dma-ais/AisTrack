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
import javax.ws.rs.core.MediaType;

import dk.dma.ais.track.AisTrackHandler;
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
    
    @GET
    @Path("/list")
    public List<VesselTarget> getTargetList() {
        // TODO Filtering
        // filtering class parsing from arguments
        // geo
        // ttlSat and ttlLive
        // List of mmsi numbers (should may
        return handler().getVesselList();
    }
    
    @GET
    @Path("/count")
    public String getTargetCount() {
        return String.format("{\"count\" : %d}", handler().getVesselStore().size());
    }
    
    private AisTrackHandler handler() {
        return get(AisTrackHandler.class);
    }

}
