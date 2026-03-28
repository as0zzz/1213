package dev.mdk2.client.util;

public final class MathUtil {
    private MathUtil() {
    }

    public static double clamp(final double value, final double minimum, final double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public static float clamp(final float value, final float minimum, final float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public static double roundToStep(final double value, final double step) {
        if (step <= 0.0D) {
            return value;
        }
        return Math.round(value / step) * step;
    }

    public static boolean within(final double mouseX, final double mouseY, final double x, final double y, final double width, final double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
