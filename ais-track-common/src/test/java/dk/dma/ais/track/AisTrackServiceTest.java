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

import dk.dma.ais.packet.AisPacketSourceFilters;
import dk.dma.ais.tracker.targetTracker.TargetInfo;
import dk.dma.enav.model.geometry.BoundingBox;
import dk.dma.enav.model.geometry.CoordinateSystem;
import dk.dma.enav.model.geometry.Position;
import org.hamcrest.CustomMatcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes ={AisTrackServiceTestConfiguration.class})
public class AisTrackServiceTest {

    @Inject
    private AisTrackService aisTrackService;

    static int[] mmsiInTestData = {
            220476000,
            259662000,
            219000185,
            219018833,
            228330600,
            219000549,
            219001013,
            256396000,
            236204000,
            219000225,
            219002067,
            244736000,
            376827000,
            636092131,
            219011074,
            244503000,
            219015373,
            310620000,
            258476000,
            235083559,
            266331000,
            219000553,
            235008400,
            636016396,
            232160000,
            257182000,
            236381000,
            636012813,
            219075000,
            246355000,
            219005747,
            209629000,
            219103000,
            636015479,
            235088102,
            219849000,
            219000188,
            220436000,
            220434000,
            266273000,
            231734000,
            256072000,
            240609000
    };

    @BeforeClass
    public static void prepareTestInstance() throws IOException {

    }

    /** Test target count */
    @Test
    public void testTargets() throws Exception {
        assertEquals(43, mmsiInTestData.length);
        assertEquals(43, aisTrackService.numberOfTargets());
        assertEquals(43, aisTrackService.targets().size());
    }

    /** Test all targets can be extracted from tracker */
    @Test
    public void testThatAllExpectedMmsisAreReturnedByTracker() throws Exception {
        Set<TargetInfo> targets = aisTrackService.targets();

        int[] mmsis = targets.stream().mapToInt(t -> t.getMmsi()).distinct().toArray();

        for (int mmsi : mmsis) {
            assertThat(mmsi, new CustomMatcher<Integer>("Tracked MMSI must in in list of expected MMSI's") {
                @Override
                public boolean matches(Object o) {
                    for (int i = 0; i < mmsiInTestData.length; i++) {
                        if (mmsiInTestData[i] == mmsi)
                            return true;
                    }
                    return false;
                }
            });
        }
    }

    /** Test all targets can be filtered and extracted from tracker by source */
    @Test
    public void testTargetsFilteredBySource() throws Exception {

        assertEquals(43, aisTrackService.targets(AisPacketSourceFilters.parseSourceFilter("s.country = DNK"),    target -> true).size());
        assertEquals(43, aisTrackService.targets(AisPacketSourceFilters.parseSourceFilter("s.id = AISW"),        target -> true).size());
        assertEquals(43, aisTrackService.targets(AisPacketSourceFilters.parseSourceFilter("s.bs > 0"),           target -> true).size());
        assertEquals( 0, aisTrackService.targets(AisPacketSourceFilters.parseSourceFilter("s.bs < 0"),           target -> true).size());
        assertEquals(43, aisTrackService.targets(AisPacketSourceFilters.parseSourceFilter("s.region = 58"),      target -> true).size());
        assertEquals( 0, aisTrackService.targets(AisPacketSourceFilters.parseSourceFilter("s.region = 57"),      target -> true).size());
        assertEquals( 0, aisTrackService.targets(AisPacketSourceFilters.parseSourceFilter("s.type = SAT"),       target -> true).size());
    }

    /** Test all targets can be filtered and extracted from tracker by TargetInfo */
    @Test
    public void testTargetsFilteredByTargetInfo() throws Exception {
        BoundingBox boundingBox = BoundingBox.create(
            Position.create(55.0, 11.0),
            Position.create(56.0, 12.0),
            CoordinateSystem.CARTESIAN
        );

        assertEquals(22, aisTrackService.targets(
                            src -> true,
                                target -> {
                                    final boolean test = boundingBox.contains(target.getPosition());
                                    System.out.println("Testing if " + target.getPosition() + " lies within " + boundingBox + ": " + (test ? "OK":"NOK"));
                                    return test;
                                })
                            .size()
        );
    }
}
