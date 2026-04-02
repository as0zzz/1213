package dev.mdk2.client.gui;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MenuDebugProfileTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void resetRestoresDefaultTypographyValues() {
        final MenuDebugProfile profile = MenuDebugProfile.defaults();
        profile.fontSize = 22.0D;
        profile.textScale = 0.51D;
        profile.valueScale = 0.47D;
        profile.miniScale = 0.66D;

        profile.reset();

        assertEquals(34.0D, profile.fontSize, 0.0001D);
        assertEquals(0.36D, profile.textScale, 0.0001D);
        assertEquals(0.30D, profile.valueScale, 0.0001D);
        assertEquals(0.30D, profile.miniScale, 0.0001D);
    }

    @Test
    public void resetRestoresTunedMenuLayoutDefaults() {
        final MenuDebugProfile profile = MenuDebugProfile.defaults();
        profile.designWidth = 1280.0D;
        profile.designHeight = 820.0D;
        profile.sidebarWidth = 244.0D;
        profile.contentXOffset = 28.0D;
        profile.contentYOffset = 74.0D;
        profile.topbarPresetWidth = 154.0D;
        profile.topbarModeWidth = 128.0D;
        profile.dropdownWidth = 96.0D;
        profile.sliderTrackWidth = 92.0D;
        profile.reset();

        assertEquals(900.0D, profile.designWidth, 0.0001D);
        assertEquals(600.0D, profile.designHeight, 0.0001D);
        assertEquals(200.0D, profile.sidebarWidth, 0.0001D);
        assertEquals(20.0D, profile.contentXOffset, 0.0001D);
        assertEquals(70.0D, profile.contentYOffset, 0.0001D);
        assertEquals(140.0D, profile.topbarPresetWidth, 0.0001D);
        assertEquals(120.0D, profile.topbarModeWidth, 0.0001D);
        assertEquals(90.0D, profile.dropdownWidth, 0.0001D);
        assertEquals(75.0D, profile.sliderTrackWidth, 0.0001D);
        assertTrue(Double.isNaN(profile.debugOverlayX));
        assertTrue(Double.isNaN(profile.debugOverlayY));
    }

    @Test
    public void saveAndLoadRoundTripsEditedValues() throws Exception {
        final MenuDebugProfile profile = MenuDebugProfile.defaults();
        profile.fontSize = 28.0D;
        profile.sidebarWidth = 270.0D;
        profile.panelRowStep = 36.0D;
        profile.logoTitleScale = 0.61D;
        profile.menuOffsetX = 48.0D;
        profile.menuOffsetY = -16.0D;
        profile.debugOverlayX = 140.0D;
        profile.debugOverlayY = 88.0D;
        final File file = this.temporaryFolder.newFile("menu-debug.properties");

        profile.save(file.toPath());
        final MenuDebugProfile loaded = MenuDebugProfile.load(file.toPath());

        assertEquals(28.0D, loaded.fontSize, 0.0001D);
        assertEquals(270.0D, loaded.sidebarWidth, 0.0001D);
        assertEquals(36.0D, loaded.panelRowStep, 0.0001D);
        assertEquals(0.61D, loaded.logoTitleScale, 0.0001D);
        assertEquals(48.0D, loaded.menuOffsetX, 0.0001D);
        assertEquals(-16.0D, loaded.menuOffsetY, 0.0001D);
        assertEquals(140.0D, loaded.debugOverlayX, 0.0001D);
        assertEquals(88.0D, loaded.debugOverlayY, 0.0001D);
    }

    @Test
    public void exportBlockContainsReadableJavaAssignments() {
        final MenuDebugProfile profile = MenuDebugProfile.defaults();
        profile.fontSize = 27.0D;
        profile.contentYOffset = 82.0D;
        profile.menuOffsetX = 12.0D;
        profile.debugOverlayX = 420.0D;

        final String export = profile.exportBlock();

        assertTrue(export.contains("fontSize = 27.0D;"));
        assertTrue(export.contains("contentYOffset = 82.0D;"));
        assertTrue(export.contains("menuOffsetX = 12.0D;"));
        assertTrue(export.contains("debugOverlayX = 420.0D;"));
    }
}
