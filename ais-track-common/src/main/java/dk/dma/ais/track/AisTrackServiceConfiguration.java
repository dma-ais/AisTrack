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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.exit;

@Configuration
public class AisTrackServiceConfiguration {

    static final Logger LOG = LoggerFactory.getLogger(AisTrackServiceConfiguration.class);

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

}
