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

import org.junit.Assert;
import org.junit.Test;

import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.track.model.VesselTarget;

public class TargetMergeTest {
    
    @Test
    public void vesselMergeTest() {
        AisPacket posPacket = AisPacket.from("$PGHP,1,2014,11,27,14,17,6,0,,808,,1,49*19\r\n!AIVDM,1,1,,B,13:laf001l0qqi4OpdT`=6b>0L1s,0*40");
        AisPacket statPacket = AisPacket.from("$PGHP,1,2014,11,27,14,17,6,0,,808,,1,49*19\r\n!AIVDM,2,1,2,A,53:laf02=t><?ALs800P4q<F1HU<Ttp000000017@@<;96uf0ABkhQCR0ES`,0*44\r\n!AIVDM,2,2,2,A,42C3m888888,2*0D");
        Assert.assertNotNull(posPacket);
        Assert.assertNotNull(statPacket);
        // Pos
        VesselTarget target = new VesselTarget(posPacket);          
        System.out.println(target);
        Assert.assertEquals(target.getMmsi(), 212675000);
        Assert.assertNotNull(target.getLastPosReport());
        Assert.assertNotNull(target.getLastReport());
        Assert.assertNotNull(target.getLat());
        Assert.assertNotNull(target.getLon());
        Assert.assertNotNull(target.getSog());
        Assert.assertNotNull(target.getCog());
        Assert.assertNotNull(target.getHeading());
        Assert.assertNotNull(target.getRot());
        Assert.assertNotNull(target.getNavStatus());
        Assert.assertNotNull(target.getMoored());
        // Stat
        VesselTarget target2 = new VesselTarget(statPacket);  
        System.out.println(target2);
        Assert.assertNotNull(target2.getLastReport());
        Assert.assertNotNull(target2.getLastStaticReport());
        Assert.assertNotNull(target2.getName());
        Assert.assertNotNull(target2.getCallsign());
        Assert.assertNotNull(target2.getImoNo());
        Assert.assertNotNull(target2.getDestination());
        Assert.assertNotNull(target2.getDraught());
        Assert.assertNotNull(target2.getEta());
        Assert.assertNotNull(target2.getLength());
        Assert.assertNotNull(target2.getWidth());
        Assert.assertNotNull(target2.getVesselType());
        Assert.assertNotNull(target2.getVesselCargo());
        // Merge
        target = target.merge(target2);
        System.out.println(target);
        Assert.assertEquals(target.getMmsi(), 212675000);
        Assert.assertNotNull(target.getLastPosReport());
        Assert.assertNotNull(target.getLastReport());
        Assert.assertNotNull(target.getLat());
        Assert.assertNotNull(target.getLon());
        Assert.assertNotNull(target.getSog());
        Assert.assertNotNull(target.getCog());
        Assert.assertNotNull(target.getHeading());
        Assert.assertNotNull(target.getRot());
        Assert.assertNotNull(target.getNavStatus());
        Assert.assertNotNull(target.getMoored());
        Assert.assertNotNull(target.getLastReport());
        Assert.assertNotNull(target.getLastStaticReport());
        Assert.assertNotNull(target.getName());
        Assert.assertNotNull(target.getCallsign());
        Assert.assertNotNull(target.getImoNo());
        Assert.assertNotNull(target.getDestination());
        Assert.assertNotNull(target.getDraught());
        Assert.assertNotNull(target.getEta());
        Assert.assertNotNull(target.getLength());
        Assert.assertNotNull(target.getWidth());
        Assert.assertNotNull(target.getVesselType());
        Assert.assertNotNull(target.getVesselCargo());
    }

}
