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

import dk.dma.ais.packet.AisPacketSource;
import dk.dma.ais.tracker.targetTracker.TargetInfo;

import java.time.Duration;
import java.util.function.BiPredicate;

/**
 * Created by Jesper Tejlgaard on 10/11/15.
 */
public class LastReportFilter implements BiPredicate<AisPacketSource, TargetInfo>{

    private final Duration duration;

    public LastReportFilter(Duration duration){
        this.duration = duration;
    }

    public boolean test(AisPacketSource source, TargetInfo targetInfo) {
        if(targetInfo.getAisTarget() == null || targetInfo.getAisTarget().getLastReport() == null){
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long ageInSeconds = (currentTime - targetInfo.getAisTarget().getLastReport().getTime()) / 1000;
        return ageInSeconds > duration.getSeconds();
    }

}
