package dev.mdk2.client.gui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HudEditorSnapMathTest {
    @Test
    public void snapsIntoCenterWhenElementGetsCloseEnough() {
        final HudEditorSnapMath.SnapResult result = HudEditorSnapMath.snapAxis(172.0D, 56.0D, 400.0D, false);

        assertTrue(result.snapped);
        assertEquals(172.0D, result.position, 0.0001D);
    }

    @Test
    public void remainsSnappedUntilPointerMovesPastReleaseThreshold() {
        final HudEditorSnapMath.SnapResult result = HudEditorSnapMath.snapAxis(181.0D, 56.0D, 400.0D, true);

        assertTrue(result.snapped);
        assertEquals(172.0D, result.position, 0.0001D);
    }

    @Test
    public void releasesWhenDraggedFarEnoughAwayFromCenter() {
        final HudEditorSnapMath.SnapResult result = HudEditorSnapMath.snapAxis(191.0D, 56.0D, 400.0D, true);

        assertFalse(result.snapped);
        assertEquals(191.0D, result.position, 0.0001D);
    }

    @Test
    public void exposesScreenGuideCoordinates() {
        assertEquals(320.0D, HudEditorSnapMath.guideX(640.0D), 0.0001D);
        assertEquals(180.0D, HudEditorSnapMath.guideY(360.0D), 0.0001D);
    }
}
