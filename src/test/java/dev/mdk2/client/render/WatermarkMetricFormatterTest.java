package dev.mdk2.client.render;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WatermarkMetricFormatterTest {
    @Test
    public void formatsCompactMetricLabels() {
        assertEquals("144 FPS", WatermarkMetricFormatter.framerate(144));
        assertEquals("28 MS", WatermarkMetricFormatter.ping(28));
        assertEquals("GPU 67%", WatermarkMetricFormatter.load("GPU", 67));
        assertEquals("CPU 42%", WatermarkMetricFormatter.load("CPU", 42));
        assertEquals("MEM 71%", WatermarkMetricFormatter.load("MEM", 71));
        assertEquals("3.25 BPS", WatermarkMetricFormatter.speed(3.25D));
        assertEquals("Singleplayer", WatermarkMetricFormatter.server("Singleplayer"));
        assertEquals("Config 2", WatermarkMetricFormatter.configName("Config 2"));
    }
}
