package dk.dma.ais.track.rest.resource;

import com.google.common.collect.Sets;
import dk.dma.ais.tracker.targetTracker.TargetInfo;
import dk.dma.enav.model.geometry.BoundingBox;
import dk.dma.enav.model.geometry.CoordinateSystem;
import dk.dma.enav.model.geometry.Position;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TrackResourceTest {

    @Test
    public void testCreateTargetFilterPredicate() throws Exception {
        Predicate<TargetInfo> predicate = TrackResource.createTargetFilterPredicate(
            Sets.newHashSet(219000000, 219000001, 219000002),
            Sets.newHashSet(
                BoundingBox.create(Position.create(55.0, 10.0), Position.create(55.1, 10.1), CoordinateSystem.CARTESIAN),
                BoundingBox.create(Position.create(56.0, 11.0), Position.create(56.5, 11.5), CoordinateSystem.CARTESIAN)
            )
        );

        // Test a target not matching any mmsi or area
        TargetInfo targetInfo = Mockito.mock(TargetInfo.class);
        when(targetInfo.getMmsi()).thenReturn(123123123);
        when(targetInfo.getPosition()).thenReturn(Position.create(54.9, 9.9));
        assertFalse(predicate.test(targetInfo));

        // Test a target matching an mmsi but not an area
        targetInfo = Mockito.mock(TargetInfo.class);
        when(targetInfo.getMmsi()).thenReturn(219000001);
        when(targetInfo.getPosition()).thenReturn(Position.create(54.9, 9.9));
        assertTrue(predicate.test(targetInfo));

        // Test a target matching an area but not an mmsi
        targetInfo = Mockito.mock(TargetInfo.class);
        when(targetInfo.getMmsi()).thenReturn(123123123);
        when(targetInfo.getPosition()).thenReturn(Position.create(56.1, 11.1));
        assertTrue(predicate.test(targetInfo));

        // Test a target matching both an mmsi and an area
        targetInfo = Mockito.mock(TargetInfo.class);
        when(targetInfo.getMmsi()).thenReturn(219000001);
        when(targetInfo.getPosition()).thenReturn(Position.create(56.1, 11.1));
        assertTrue(predicate.test(targetInfo));
    }
}