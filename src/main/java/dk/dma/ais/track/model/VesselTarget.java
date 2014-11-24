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

import dk.dma.ais.data.AisTargetDimensions;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage5;
import dk.dma.ais.message.AisPositionMessage;
import dk.dma.ais.message.AisStaticCommon;
import dk.dma.ais.message.AisTargetType;
import dk.dma.ais.message.IVesselPositionMessage;
import dk.dma.ais.message.NavigationalStatus;
import dk.dma.ais.message.ShipTypeCargo;
import dk.dma.ais.packet.AisPacket;
import dk.dma.enav.model.geometry.Position;

/**
 * Vessel class A and B target
 */
public class VesselTarget extends Target {

    protected Double lat;
    protected Double lon;
    protected Double cog;
    protected Double sog;
    protected Double heading;
    protected Double rot;
    protected Integer length;
    protected Integer width;
    protected String name;
    protected String callsign;
    protected Integer imoNo;
    protected String destination;
    protected Double draught;
    protected String navStatus;
    protected Boolean moored;
    protected Date eta;
    protected String vesselType;
    protected String vesselCargo;
    protected Date lastPosReport;
    protected Date lastStaticReport;

    public VesselTarget() {

    }

    public VesselTarget(AisPacket packet) {
        super(packet);
        AisMessage message = packet.tryGetAisMessage();
        if (message instanceof IVesselPositionMessage) {
            this.lastPosReport = packet.getTimestamp();
            IVesselPositionMessage posMessage = (IVesselPositionMessage) message;
            sog = posMessage.isSogValid() ? posMessage.getSog() / 10.0 : null;
            cog = posMessage.isCogValid() ? posMessage.getCog() / 10.0 : null;
            heading = posMessage.isHeadingValid() ? (double) posMessage.getTrueHeading() : null;
            if (posMessage.isPositionValid()) {
                Position pos = posMessage.getPos().getGeoLocation();
                lat = pos.getLatitude();
                lon = pos.getLongitude();
            }
            if (message instanceof AisPositionMessage) {
                AisPositionMessage classAposMessage = (AisPositionMessage) message;
                rot = classAposMessage.isRotValid() ? (double) classAposMessage.getRot() : null;
                NavigationalStatus navigationalStatus = NavigationalStatus.get(classAposMessage.getNavStatus());
                navStatus = navigationalStatus.prettyStatus();
                moored = classAposMessage.getNavStatus() == 1 || classAposMessage.getNavStatus() == 5;
            }
        }
        if (message instanceof AisStaticCommon) {
            this.lastStaticReport = packet.getTimestamp();
            AisStaticCommon stat = (AisStaticCommon) message;
            this.name = AisMessage.trimText(stat.getName());
            this.callsign = AisMessage.trimText(stat.getCallsign());
            ShipTypeCargo shipTypeCargo = new ShipTypeCargo(stat.getShipType());
            this.vesselType = shipTypeCargo.prettyType();
            this.vesselCargo = shipTypeCargo.prettyCargo();
            if (message instanceof AisMessage5) {
                AisMessage5 msg5 = (AisMessage5) message;
                AisTargetDimensions dim = new AisTargetDimensions(msg5);
                if (dim != null) {
                    this.length = dim.getDimBow() + dim.getDimStern();
                    this.width = dim.getDimPort() + dim.getDimStarboard();
                }
                this.destination = AisMessage.trimText(msg5.getDest());
                if (this.destination.length() == 0) {
                    this.destination = null;
                }
                this.draught = msg5.getDraught() == 0 ? null : msg5.getDraught() / 10.0;
                this.eta = msg5.getEtaDate();
                if (msg5.getImo() > 0) {
                    this.imoNo = (int) msg5.getImo();
                }
            }
        }
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getCog() {
        return cog;
    }

    public void setCog(Double cog) {
        this.cog = cog;
    }

    public Double getSog() {
        return sog;
    }

    public void setSog(Double sog) {
        this.sog = sog;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public Double getRot() {
        return rot;
    }

    public void setRot(Double rot) {
        this.rot = rot;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public Integer getImoNo() {
        return imoNo;
    }

    public void setImoNo(Integer imoNo) {
        this.imoNo = imoNo;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getDraught() {
        return draught;
    }

    public void setDraught(Double draught) {
        this.draught = draught;
    }

    public String getNavStatus() {
        return navStatus;
    }

    public void setNavStatus(String navStatus) {
        this.navStatus = navStatus;
    }

    public Boolean getMoored() {
        return moored;
    }

    public void setMoored(Boolean moored) {
        this.moored = moored;
    }

    public Date getEta() {
        return eta;
    }

    public void setEta(Date eta) {
        this.eta = eta;
    }

    public String getVesselType() {
        return vesselType;
    }

    public void setVesselType(String vesselType) {
        this.vesselType = vesselType;
    }

    public String getVesselCargo() {
        return vesselCargo;
    }

    public void setVesselCargo(String vesselCargo) {
        this.vesselCargo = vesselCargo;
    }

    public Date getLastPosReport() {
        return lastPosReport;
    }

    public void setLastPosReport(Date lastPosReport) {
        this.lastPosReport = lastPosReport;
    }

    public Date getLastStaticReport() {
        return lastStaticReport;
    }

    public void setLastStaticReport(Date lastStaticReport) {
        this.lastStaticReport = lastStaticReport;
    }

    public VesselTarget merge(VesselTarget t) {
        boolean posUpdate = t.getLastPosReport() != null;
        boolean classB = t.getTargetType() != null && t.getTargetType() == AisTargetType.B;

        // Metadata
        VesselTarget newT = new VesselTarget();
        newT.targetType = t.targetType != null ? t.targetType : this.targetType;
        newT.lastReport = t.lastReport;
        newT.sourceType = t.sourceType;
        newT.sourceCountry = t.sourceCountry;
        newT.lastPosReport = t.lastPosReport != null ? t.lastPosReport : this.lastPosReport;
        newT.lastStaticReport = t.lastStaticReport != null ? t.lastStaticReport : this.lastStaticReport;

        // Navigational data
        if (posUpdate) {
            newT.lat = t.lat;
            newT.lon = t.lon;
            newT.cog = t.cog;
            newT.sog = t.sog;
            newT.heading = t.heading;
            newT.rot = t.rot;
            newT.navStatus = t.navStatus;
            newT.moored = t.moored;
        } else {
            // Static update
            newT.name = t.name != null ? t.name : (classB ? this.name : null);
            newT.callsign = t.callsign != null ? t.callsign : (classB ? this.callsign : null);
            newT.length = t.length != null ? t.length : (classB ? this.length : null);
            newT.width = t.width != null ? t.width : (classB ? this.width : null);
            newT.imoNo = t.imoNo != null ? t.imoNo : (classB ? this.imoNo : null);
            newT.destination = t.destination != null ? t.destination : (classB ? this.destination : null);
            newT.callsign = t.callsign != null ? t.callsign : (classB ? this.callsign : null);
            newT.draught = t.draught != null ? t.draught : (classB ? this.draught : null);
            newT.eta = t.eta != null ? t.eta : (classB ? this.eta : null);
            newT.vesselType = t.vesselType != null ? t.vesselType : (classB ? this.vesselType : null);
            newT.vesselCargo = t.vesselCargo != null ? t.vesselCargo : (classB ? this.vesselCargo : null);
        }

        return newT;
    }

}
