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

package dk.dma.ais.track.rest.resource;

import com.google.common.collect.Sets;
import dk.dma.ais.packet.AisPacketSource;
import dk.dma.ais.packet.AisPacketSourceFilters;
import dk.dma.ais.track.AisTrackService;
import dk.dma.ais.track.rest.resource.exceptions.CannotParseFilterExpressionException;
import dk.dma.ais.track.rest.resource.exceptions.TargetNotFoundException;
import dk.dma.ais.tracker.targetTracker.TargetInfo;
import dk.dma.enav.model.geometry.Area;
import dk.dma.enav.model.geometry.BoundingBox;
import dk.dma.enav.model.geometry.Circle;
import dk.dma.enav.model.geometry.CoordinateSystem;
import dk.dma.enav.model.geometry.Position;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author tbsalling
 */

@RestController
@RequestMapping(value="ais-track")
public class TrackResource {

    @Inject
    AisTrackService trackService;

    @PostConstruct
    private void init() {
        trackService.start();
    }

    private final Instant timeStarted = Instant.now();

    /**
     * Show status page.
     *
     * Example URL:
     * - http://localhost:8080/?sourceFilter=s.country%20in%20(DK)
     *
     * @param sourceFilterExpression Source filter expression
     * @return
     */
    @RequestMapping(value="/", produces = MediaType.TEXT_PLAIN_VALUE)
    String home(@RequestParam(value="sourceFilter", required = false) String sourceFilterExpression) {
        StringBuilder sb = new StringBuilder();

        sb.append("Danish Maritime Authority - AIS Tracker\n")
          .append("---------------------------------------\n")
          .append("\n")
          .append("Tracking started: ").append(timeStarted).append('\n')
          .append("Current time: ").append(Instant.now()).append('\n')
          .append("Total targets tracked: ").append(trackService.numberOfTargets()).append('\n');

        if (!isBlank(sourceFilterExpression))
          sb.append("Targets matching source filter expression: ").append(trackService.numberOfTargets(createSourceFilterPredicate(sourceFilterExpression))).append('\n');

        return sb.toString();
    }

    /**
     * Return JSON for the TargetInfo matching mmsi and sourceFilter.
     *
     * URL examples (with URL encoded filter expressions):
     * - http://localhost:8080/track/257742710?sourceFilter=s.country%20in%20(DK%2C%20NO)
     * - http://localhost:8080/track/257742710?sourceFilter=s.country%20in%20(DK)
     *
     * @param sourceFilterExpression
     * @return
     */
    @RequestMapping(value = "/track/{mmsi}", produces = MediaType.APPLICATION_JSON_VALUE)
    TargetInfo track(@PathVariable int mmsi, @RequestParam(value="sourceFilter", required = false) String sourceFilterExpression) {
        TargetInfo target = trackService.target(mmsi, createSourceFilterPredicate(sourceFilterExpression));

        if (target == null) {
            throw new TargetNotFoundException(mmsi);
        }
        return target;
    }

    /**
     * Return JSON for all TargetInfo's matching sourceFilter and targetFilter.
     *
     * URL examples (with URL encoded filter expressions):
     * - http://localhost:8080/tracks?sourceFilter=s.region%3D806
     * - http://localhost:8080/tracks?sourceFilter=s.country%3DDK
     * - http://localhost:8080/tracks?sourceFilter=s.country%3DDK%26s.bs%3D2190047
     * - http://localhost:8080/tracks?sourceFilter=s.country%20in%20(DK%2C%20NO)
     *
     * - http://localhost:8080/tracks?mmsi=244820404&mmsi=345070335
     *
     * - http://localhost:8080/tracks?area=52.3|4.8|52.5|4.9
     * - http://localhost:8080/tracks?area=52.3|4.8|52.5|4.9&area=20.0|100.0|21.0|110.0
     *
     * - http://localhost:8080/tracks?mmsi=244820404&mmsi=345070335&area=52.0|4.0|52.5|5.0
     *
     * - http://localhost:8080/tracks?mmsi=244820404&mmsi=345070335&area=52.0|4.0|52.5|5.0&area=20.0|100.0|21.0|110.0&sourceFilter=s.region%3D806|s.country%20in%20DK)
     *
     * @param sourceFilterExpression
     * @param mmsiParams mmsi numbers to include in the result
     * @param areaParams areas to include in the result
     * @return
     */
    @RequestMapping(value = "/tracks", produces = MediaType.APPLICATION_JSON_VALUE)
    Set<TargetInfo> tracks(
            @RequestParam(value="sourceFilter", required = false) String sourceFilterExpression,
            @RequestParam(value="baseArea", required = false) List<String> baseAreaParams,
            @RequestParam(value="area", required = false) List<String> areaParams,
            @RequestParam(value="mmsi", required = false) List<String> mmsiParams){

        Set<Integer> mmsis = Sets.newHashSet();
        if (mmsiParams != null && mmsiParams.size() > 0) {
            mmsiParams.forEach(mmsi -> mmsis.add(Integer.valueOf(mmsi)));
        }

        Set<Area> areas = map2Areas(areaParams);
        Set<Area> baseAreas = map2Areas(baseAreaParams);

        return trackService.targets(
            createSourceFilterPredicate(sourceFilterExpression),
            createTargetFilterPredicate(mmsis, baseAreas, areas)
        );
    }

    private static Set<Area> map2Areas(List<String> areaParams){
        Set<Area> areas = Sets.newHashSet();
        if (areaParams != null && areaParams.size() > 0) {
            areaParams.forEach(area -> {
                if(area.trim().startsWith("circle")){
                    try{
                        area = URLDecoder.decode(area, "UTF-8");
                        System.out.println(area);
                        int p1 = area.indexOf("(");
                        int p2 = area.indexOf(")");
                        String[] values = area.substring(p1 + 1, p2).split(",");
                        Double lat = Double.valueOf(values[0].trim());
                        Double lon = Double.valueOf(values[1].trim());
                        Double radius = Double.valueOf(values[2].trim());
                        areas.add(new Circle(Position.create(lat, lon), radius, CoordinateSystem.CARTESIAN));
                    }catch(UnsupportedEncodingException e){
                        throw new RuntimeException(e);
                    }
                }else{
                    String[] bordersAsString = area.split("\\|");
                    if (bordersAsString == null || bordersAsString.length != 4)
                        throw new IllegalArgumentException("Expected four floating point values for area argument separated by vertical bar, not: " + area);
                    Double lat1 = Double.valueOf(bordersAsString[0]);
                    Double lon1 = Double.valueOf(bordersAsString[1]);
                    Double lat2 = Double.valueOf(bordersAsString[2]);
                    Double lon2 = Double.valueOf(bordersAsString[3]);
                    areas.add(BoundingBox.create(Position.create(lat1, lon1), Position.create(lat2, lon2), CoordinateSystem.CARTESIAN));
                }
            });
        }
        return areas;
    }


    /** Create a Predicate<AisPacketSource> out of a user supplied expression string */
    static Predicate<AisPacketSource> createSourceFilterPredicate(String sourceFilterExpression) {
        Predicate<AisPacketSource> sourceFilter;

        if (! isBlank(sourceFilterExpression)) {
            try {
                sourceFilter = AisPacketSourceFilters.parseSourceFilter(sourceFilterExpression);
            } catch (Exception e) {
                throw new CannotParseFilterExpressionException(e, sourceFilterExpression);
            }
        } else {
            sourceFilter = src -> true;
        }

        return sourceFilter;
    }

    /** Create a Predicate<TargetInfo> out of user supplied mmsi and area information */
    static Predicate<TargetInfo> createTargetFilterPredicate(Set<Integer> mmsis, Set<Area> baseAreas, Set<Area> areas) {
        Predicate<TargetInfo> mmsiPredicate = null;
        if (mmsis != null && mmsis.size() > 0) {
            mmsiPredicate =  targetInfo -> mmsis.contains(targetInfo.getMmsi());
        }

        Predicate<TargetInfo> baseAreaPredicate = null;
        if (baseAreas != null && baseAreas.size() > 0) {
            baseAreaPredicate =  targetInfo -> baseAreas.stream().anyMatch(area -> targetInfo.getPosition() != null && area.contains(targetInfo.getPosition()));
        }

        Predicate<TargetInfo> areaPredicate = null;
        if (areas != null && areas.size() > 0) {
            areaPredicate =  targetInfo -> areas.stream().anyMatch(area -> targetInfo.getPosition() != null && area.contains(targetInfo.getPosition()));
        }

        Predicate<TargetInfo> resultingAreaPredicate = null;
        if(baseAreaPredicate != null && areaPredicate == null){
            resultingAreaPredicate = baseAreaPredicate;
        }else if (baseAreaPredicate != null && areaPredicate != null){
            resultingAreaPredicate = baseAreaPredicate.and(areaPredicate);
        }else{
            resultingAreaPredicate = areaPredicate;
        }

        if (mmsiPredicate == null && resultingAreaPredicate == null)
            return t -> true;
        else if (mmsiPredicate != null && resultingAreaPredicate == null)
            return mmsiPredicate;
        else if (mmsiPredicate == null && resultingAreaPredicate != null)
            return resultingAreaPredicate;
        else
            return mmsiPredicate.or(resultingAreaPredicate);
    }

}
