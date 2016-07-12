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

import dk.dma.ais.data.AisTarget;
import dk.dma.ais.tracker.targetTracker.TargetInfo;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Date;

/**
 * Created by Jesper Tejlgaard on 10/11/15.
 */
public class LastReportFilterTest {

    @Test
    public void testAisTargetNull(){
        TargetInfo targetInfo = Mockito.mock(TargetInfo.class);
        Mockito.when(targetInfo.getAisTarget()).thenReturn(null);

        LastReportFilter filter = new LastReportFilter(Duration.ZERO);
        Assert.assertFalse(filter.test(null, targetInfo));
    }

    @Test
    public void testLastReportNull(){
        TargetInfo targetInfo = createTargetInfo(null);

        LastReportFilter filter = new LastReportFilter(Duration.ZERO);
        Assert.assertFalse(filter.test(null, targetInfo));
    }


    @Test
    public void testLastReportNow(){
        Date lastReport = new Date(System.currentTimeMillis());
        Duration duration = Duration.parse("P2D");

        TargetInfo targetInfo = createTargetInfo(lastReport);

        LastReportFilter filter = new LastReportFilter(duration);
        Assert.assertFalse(filter.test(null, targetInfo));
    }


    @Test
    public void testLastReport24HoursOld(){
        Date lastReport = new Date(System.currentTimeMillis() - 1000*3600*24);
        Duration duration = Duration.parse("P2D");

        TargetInfo targetInfo = createTargetInfo(lastReport);

        LastReportFilter filter = new LastReportFilter(duration);
        Assert.assertFalse(filter.test(null, targetInfo));
    }

    @Test
    public void testLastReport50HoursOld(){
        Date lastReport = new Date(System.currentTimeMillis() - 1000*3600*50);
        Duration duration = Duration.parse("P2D");

        TargetInfo targetInfo = createTargetInfo(lastReport);

        LastReportFilter filter = new LastReportFilter(duration);
        Assert.assertTrue(filter.test(null, targetInfo));
    }

    @Test
    public void testLastReportIsFromTheFuture100Hours(){
        Date lastReport = new Date(System.currentTimeMillis() + 1000*3600*100);
        Duration duration = Duration.parse("P2D");

        TargetInfo targetInfo = createTargetInfo(lastReport);

        LastReportFilter filter = new LastReportFilter(duration);
        Assert.assertTrue(filter.test(null, targetInfo));
    }
    @Test
    public void testLastReportIsFromTheFuture10Hours(){
        Date lastReport = new Date(System.currentTimeMillis() + 1000*3600*10);
        Duration duration = Duration.parse("P2D");

        TargetInfo targetInfo = createTargetInfo(lastReport);

        LastReportFilter filter = new LastReportFilter(duration);
        Assert.assertFalse(filter.test(null, targetInfo));
    }


    private TargetInfo createTargetInfo(Date lastReport){
        AisTarget aisTarget = Mockito.mock(AisTarget.class);
        Mockito.when(aisTarget.getLastReport()).thenReturn(lastReport);

        TargetInfo targetInfo = Mockito.mock(TargetInfo.class);
        Mockito.when(targetInfo.getAisTarget()).thenReturn(aisTarget);

        return targetInfo;
    }

}
