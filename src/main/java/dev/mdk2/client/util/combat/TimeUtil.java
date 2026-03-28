package dev.mdk2.client.util.combat;

public final class TimeUtil {
    private TimeUtil() {
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static boolean passed(final long time, final double delayMs) {
        return now() - time >= delayMs;
    }
}
