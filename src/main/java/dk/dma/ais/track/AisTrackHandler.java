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

import java.util.Date;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisTargetType;
import dk.dma.ais.message.IVesselPositionMessage;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.track.model.VesselTarget;
import dk.dma.ais.track.store.MapTargetStore;
import dk.dma.ais.track.store.TargetStore;

public class AisTrackHandler implements Consumer<AisPacket> {

    static final Logger LOG = LoggerFactory.getLogger(AisTrackHandler.class);
        
    private final TargetStore<VesselTarget> vesselStore = new MapTargetStore<>();
    
    public AisTrackHandler() {
    }

    @Override
    public void accept(AisPacket packet) {
        // Must have valid AIS message
        AisMessage message = packet.tryGetAisMessage();        
        if (message == null) {
            return;
        }
        AisTargetType type = message.getTargetType();
        if (type == null) {
            return;
        }

        // Handle different types
        switch (type) {
        case A:
        case B:
            handleVessel(packet);
            break;
        default:
            break;
        }
    }

    private void handleVessel(AisPacket packet) {
        AisMessage message = packet.tryGetAisMessage();
        
        // Create vessel target
        VesselTarget target = new VesselTarget(packet);
        
        // Get existing entry
        VesselTarget oldTarget = vesselStore.get(target.getMmsi());
        
        // Avoid updating with old pos message
        if (oldTarget != null && message instanceof IVesselPositionMessage) {
            Date oldPosReport = oldTarget.getLastPosReport();
            Date thisPosReport = target.getLastPosReport();
            if (oldPosReport != null && thisPosReport != null && oldPosReport.after(thisPosReport)) {
                return;
            }
        }
        
        // Handle target type change
        if (oldTarget != null && (target.getTargetType() != oldTarget.getTargetType())) {
            // Discard old target
            oldTarget = null;
        }
        
        // Merge and save
        if (oldTarget != null) {
            target = oldTarget.merge(target);
        }        
        vesselStore.put(target);
        
    }

}
