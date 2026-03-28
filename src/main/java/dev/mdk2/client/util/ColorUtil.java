package dev.mdk2.client.util;

public final class ColorUtil {
    private ColorUtil() {
    }

    public static int rgba(final int red, final int green, final int blue, final int alpha) {
        return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
    }

    public static int withAlpha(final int color, final int alpha) {
        return (alpha & 255) << 24 | (color & 0x00FFFFFF);
    }

    public static int multiplyAlpha(final int color, final double multiplier) {
        final int alpha = (int) (((color >>> 24) & 255) * MathUtil.clamp(multiplier, 0.0D, 1.0D));
        return withAlpha(color, alpha);
    }

    public static int interpolate(final int startColor, final int endColor, final double progress) {
        final double clamped = MathUtil.clamp(progress, 0.0D, 1.0D);
        final int a1 = startColor >>> 24 & 255;
        final int r1 = startColor >>> 16 & 255;
        final int g1 = startColor >>> 8 & 255;
        final int b1 = startColor & 255;
        final int a2 = endColor >>> 24 & 255;
        final int r2 = endColor >>> 16 & 255;
        final int g2 = endColor >>> 8 & 255;
        final int b2 = endColor & 255;

        return rgba(
            (int) (r1 + (r2 - r1) * clamped),
            (int) (g1 + (g2 - g1) * clamped),
            (int) (b1 + (b2 - b1) * clamped),
            (int) (a1 + (a2 - a1) * clamped)
        );
    }
}
