package dev.mdk2.client.render;

import java.util.Locale;

public final class WatermarkMetricFormatter {
    private WatermarkMetricFormatter() {
    }

    public static String logo() {
        return "MDK2";
    }

    public static String framerate(final int fps) {
        return Math.max(0, fps) + " FPS";
    }

    public static String ping(final int milliseconds) {
        return Math.max(0, milliseconds) + " MS";
    }

    public static String speed(final double blocksPerSecond) {
        return String.format(Locale.US, "%.2f BPS", Math.max(0.0D, blocksPerSecond));
    }

    public static String load(final String label, final int percent) {
        return safe(label, "") + " " + Math.max(0, Math.min(100, percent)) + "%";
    }

    public static String username(final String username) {
        return safe(username, "Player");
    }

    public static String configName(final String configName) {
        return safe(configName, "Unnamed");
    }

    public static String server(final String server) {
        return safe(server, "Singleplayer");
    }

    private static String safe(final String value, final String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
