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

import dk.dma.ais.bus.AisBus;
import dk.dma.ais.configuration.bus.AisBusConfiguration;
import dk.dma.ais.packet.AisPacketReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.exit;

@Configuration
public class AisTrackServiceTestConfiguration {

    static final Logger LOG = LoggerFactory.getLogger(AisTrackServiceTestConfiguration.class);

    @Inject
    ApplicationContext ctx;

    @Bean
    public AisBus provideAisBus() {
        AisBus aisBus = null;
        try {
            AisBusConfiguration aisBusConf = AisBusConfiguration.load(ClassLoader.getSystemResourceAsStream("aisbus.xml"));
            aisBus = aisBusConf.getInstance();
        } catch (Exception e) {
            LOG.error("Failed to create AisBus", e);
            exit(-1);
        }
        return aisBus;
    }

    @Bean
    public AisTrackService provideAisTrackService() throws IOException {
        AisTrackService aisTrackService = new AisTrackService();
        aisTrackService.setAisBus(provideAisBus());
        aisTrackService.start();

        String testdataResourceName = "testdata-00.ais";

        System.out.println("Reading AisPackets from " + testdataResourceName);
        final int[] packetsRead = {0};
        InputStream aisInputStream = ClassLoader.getSystemResourceAsStream(testdataResourceName);
        AisPacketReader aisPacketReader = new AisPacketReader(aisInputStream);
        aisPacketReader.forEachRemaining(p -> {

            aisTrackService.getAisBus().push(p, true);

            if (packetsRead[0]%100 == 0) {
                System.out.printf("%d packets read.\r", packetsRead[0]);
            }
            packetsRead[0]++;

            long start = System.nanoTime();
            final long delayNanos = 1000;
            while (start + delayNanos >= System.nanoTime()) ;

        });
        System.out.println("Finished reading AisPackets from " + testdataResourceName);
        System.out.println("Read a total of " + packetsRead[0] + " AisPackets.");

        return aisTrackService;
    }
}
