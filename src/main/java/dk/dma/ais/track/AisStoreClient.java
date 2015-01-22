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
package dk.dma.ais.track;

import java.io.InputStream;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.IVesselPositionMessage;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.reader.AisReader;
import dk.dma.ais.reader.AisReaders;
import dk.dma.ais.track.model.PastTrackPosition;
import dk.dma.ais.track.model.VesselTarget;
import dk.dma.ais.track.store.PastTrack;

public class AisStoreClient {

    static final Logger LOG = LoggerFactory.getLogger(AisStoreClient.class);

    private final AisTrackConfiguration cfg;

    @Inject
    public AisStoreClient(AisTrackConfiguration cfg) {
        this.cfg = cfg;

    }

    public List<PastTrackPosition> getPastTrack(int mmsi, Integer minDist, Duration age) {
        age = age != null ? age : cfg.defaultLongTrackAge();
        minDist = minDist == null ? cfg.defaultMinPastTrackDist() : minDist;
        ZonedDateTime now = ZonedDateTime.now();
        String from = now.format(DateTimeFormatter.ISO_INSTANT);
        ZonedDateTime end = now.minus(age);
        String to = end.format(DateTimeFormatter.ISO_INSTANT);
        String interval = String.format("%s/%s", to, from);
        Client client = ClientBuilder.newClient();
        if (cfg.aisStoreUsername() != null) {
            client.register(HttpAuthenticationFeature.basic(cfg.aisStoreUsername(), cfg.aisStorePassword()));
        }
        WebTarget t = client.target(cfg.aisStoreBaseUrl()).queryParam("mmsi", mmsi).queryParam("interval", interval);
        LOG.info("Calling AisStore: " + t);
        Response resp = t.request().get();
        if (resp.getStatus() != 200) {
            LOG.error("Failed to make REST query: " + resp.getStatus());
            throw new InternalError("REST endpoint failed");
        }
        InputStream stream = resp.readEntity(InputStream.class);
        AisReader aisReader = AisReaders.createReaderFromInputStream(stream);
        final List<PastTrackPosition> track = new ArrayList<>();
        aisReader.registerPacketHandler(new Consumer<AisPacket>() {
            @Override
            public void accept(AisPacket p) {
                AisMessage message = p.tryGetAisMessage();
                if (message == null || !(message instanceof IVesselPositionMessage)) {
                    return;
                }
                VesselTarget target = new VesselTarget(p);
                if (!target.isValidPos()) {
                    return;
                }
                track.add(new PastTrackPosition(target.getLat(), target.getLon(), target.getCog(), target.getSog(), target
                        .getLastPosReport().getTime()));
            }
        });
        aisReader.start();
        try {
            aisReader.join();
        } catch (InterruptedException e) {
            return null;
        }
        resp.close();
        client.close();
        LOG.info("AisStore returned track with " + track.size() + " points");
        return PastTrack.downSample(track, minDist, age.toMillis());    
    }

}
