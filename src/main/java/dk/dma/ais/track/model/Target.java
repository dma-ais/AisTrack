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
package dk.dma.ais.track.model;

import java.util.Date;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisTargetType;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketTags;
import dk.dma.enav.model.Country;

/**
 * Base class for all AIS targets
 */
public class Target {

    protected AisTargetType targetType;
    protected int mmsi;
    protected String country;
    protected Date lastReport;
    protected String sourceType;
    protected String sourceCountry;
    
    public Target() {
    }
    
    public Target(AisPacket packet) {
        AisMessage message = packet.tryGetAisMessage();        
        this.targetType = message.getTargetType();
        this.mmsi = message.getUserId();
        Country c = Country.getCountryForMmsi(message.getUserId());
        if (c != null) {
            this.country = c.getTwoLetter();
        }
        this.lastReport = packet.getTimestamp();
        AisPacketTags tags = packet.getTags();
        this.sourceType = (tags.getSourceType() == null) ? "LIVE" : tags.getSourceType().encode();
        Country sc = tags.getSourceCountry();
        if (sc != null) {
            this.sourceCountry = sc.getTwoLetter();
        }
    }

    public AisTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(AisTargetType targetType) {
        this.targetType = targetType;
    }

    public int getMmsi() {
        return mmsi;
    }

    public void setMmsi(int mmsi) {
        this.mmsi = mmsi;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getLastReport() {
        return lastReport;
    }

    public void setLastReport(Date lastReport) {
        this.lastReport = lastReport;
    }
    
}
