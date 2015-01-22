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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class IntervalFormatTest {
    
    @Test
    public void formatInterval() {
        Duration age = Duration.parse("P2D");
        ZonedDateTime now = ZonedDateTime.now();
        String from = now.format(DateTimeFormatter.ISO_INSTANT);
        ZonedDateTime end = now.plus(age);
        String to = end.format(DateTimeFormatter.ISO_INSTANT);
        String interval = String.format("%s/%s", from, to);
        System.out.println("interval: " + interval);
    }

}
