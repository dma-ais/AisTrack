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

import java.lang.reflect.Method;
import java.time.Duration;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Converter;

@LoadPolicy(LoadType.MERGE)
@Sources({ "classpath:aistrack.properties" })
public interface AisTrackConfiguration extends Accessible {

    @DefaultValue("8080")
    int port();

    @DefaultValue("aisbus.xml")
    String aisbusConfFile();

    @DefaultValue("backup")
    String backup();

    @DefaultValue("true")
    Boolean pastTrack();

    @DefaultValue("false")
    Boolean registerMaxSpeed();

    @DefaultValue("dk.dma.ais.track.store.MapDbTargetStore")
    Class<?> targetStoreClass();

    @DefaultValue("dk.dma.ais.track.store.MapPastTrackStore")
    Class<?> pastTrackStoreClass();

    @DefaultValue("P2D")
    @ConverterClass(DurationConverter.class)
    Duration targetExpire();

    @DefaultValue("PT10M")
    @ConverterClass(DurationConverter.class)
    Duration cleanupInterval();

    @DefaultValue("100")
    Integer defaultMinPastTrackDist();

    @DefaultValue("PT1H")
    @ConverterClass(DurationConverter.class)
    Duration pastTrackTtl();

    // See https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-
    public class DurationConverter implements Converter<Duration> {
        @Override
        public Duration convert(Method targetMethod, String text) {
            return Duration.parse(text);
        }
    }

}
