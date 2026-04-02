package dev.mdk2.client.modules.visual;

import dev.mdk2.client.settings.Setting;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WatermarkModuleTest {
    @Test
    public void watermarkModuleStartsWithMetricsEnabled() {
        final WatermarkModule module = new WatermarkModule();

        assertTrue(module.isLogoEnabled());
        assertTrue(module.isFramerateEnabled());
        assertTrue(module.isPingEnabled());
        assertFalse(module.isSpeedEnabled());
        assertFalse(module.isGpuLoadEnabled());
        assertFalse(module.isCpuLoadEnabled());
        assertFalse(module.isMemoryLoadEnabled());
        assertFalse(module.isUsernameEnabled());
        assertFalse(module.isConfigNameEnabled());
        assertFalse(module.isServerIpEnabled());
        assertFalse(module.isToggleable());
    }

    @Test
    public void watermarkModuleExposesExpandedWatermarkSettings() {
        final WatermarkModule module = new WatermarkModule();
        final List<String> names = new ArrayList<String>();

        for (final Setting<?> setting : module.getSettings()) {
            names.add(setting.getName());
        }

        assertEquals(
            Arrays.asList("Logo", "Framerate", "Ping", "Speed", "GPU Load", "CPU Load", "Memory Load", "Username", "Config Name", "Server IP"),
            names
        );
    }

    @Test
    public void watermarkModuleStoresAbsolutePosition() {
        final WatermarkModule module = new WatermarkModule();

        module.setPosition(140.0D, 18.0D);

        assertEquals(140.0D, module.getPositionX(), 0.0001D);
        assertEquals(18.0D, module.getPositionY(), 0.0001D);
    }
}
