package dev.mdk2.client.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThemeManagerTest {
    @Test
    public void glassStyleDisablesMenuTextShadow() {
        final ThemeManager manager = new ThemeManager();

        manager.setStyle("Glass");

        assertFalse(manager.drawsTextShadow());
    }

    @Test
    public void darkStyleKeepsMenuTextShadow() {
        final ThemeManager manager = new ThemeManager();

        manager.setStyle("Dark");

        assertTrue(manager.drawsTextShadow());
    }

    @Test
    public void outlinesUseThickerStrokeThanDefaultOnePixel() {
        final ThemeManager manager = new ThemeManager();

        assertEquals(2.3D, manager.windowOutlineWidth(), 0.0001D);
        assertEquals(2.2D, manager.containerOutlineWidth(), 0.0001D);
        assertEquals(2.0D, manager.controlOutlineWidth(), 0.0001D);
        assertEquals(2.2D, manager.popupOutlineWidth(), 0.0001D);
    }

    @Test
    public void footerProfileButtonHasNoOutline() {
        final ThemeManager manager = new ThemeManager();

        assertEquals(0.0D, manager.footerProfileOutlineWidth(), 0.0001D);
    }

    @Test
    public void darkStyleUsesStrongerOutlineAlphas() {
        final ThemeManager manager = new ThemeManager();

        manager.setStyle("Dark");

        assertEquals(36, manager.windowOutlineAlpha());
        assertEquals(30, manager.containerOutlineAlpha());
        assertEquals(28, manager.controlOutlineAlpha());
        assertEquals(30, manager.popupOutlineAlpha());
    }

    @Test
    public void darkStyleUsesElevatedShellDepth() {
        final ThemeManager manager = new ThemeManager();

        manager.setStyle("Dark");

        assertEquals(0, manager.shellShadowAlpha());
        assertEquals(0, manager.controlShadowAlpha());
        assertEquals(8, manager.controlShadowSpread());
        assertEquals(0, manager.shellUnderlayAlpha());
        assertEquals(0, manager.shellSheenTopAlpha());
        assertEquals(0, manager.shellSheenBottomAlpha());
        assertEquals(12, manager.shellInnerOutlineAlpha());
        assertEquals(0, manager.shellOuterStrokeAlpha());
        assertEquals(0, manager.shellOuterBandAlpha());
        assertEquals(0.0D, manager.shellOuterBandInset(), 0.0001D);
    }

    @Test
    public void glassStyleUsesSofterElevatedShellDepth() {
        final ThemeManager manager = new ThemeManager();

        manager.setStyle("Glass");

        assertEquals(0, manager.shellShadowAlpha());
        assertEquals(0, manager.controlShadowAlpha());
        assertEquals(8, manager.controlShadowSpread());
        assertEquals(0, manager.shellUnderlayAlpha());
        assertEquals(0, manager.shellSheenTopAlpha());
        assertEquals(0, manager.shellSheenBottomAlpha());
        assertEquals(18, manager.shellInnerOutlineAlpha());
        assertEquals(0, manager.shellOuterStrokeAlpha());
        assertEquals(0, manager.shellOuterBandAlpha());
        assertEquals(0.0D, manager.shellOuterBandInset(), 0.0001D);
    }

    @Test
    public void inWorldBackdropIsStrongerThanMenuBackdrop() {
        final ThemeManager manager = new ThemeManager();

        manager.setStyle("Dark");
        assertTrue(manager.backdropBaseAlpha(true) > manager.backdropBaseAlpha(false));
        assertTrue(manager.backdropBottomAlpha(true) > manager.backdropBottomAlpha(false));

        manager.setStyle("Glass");
        assertTrue(manager.backdropBaseAlpha(true) > manager.backdropBaseAlpha(false));
        assertTrue(manager.backdropBottomAlpha(true) > manager.backdropBottomAlpha(false));
    }
}
