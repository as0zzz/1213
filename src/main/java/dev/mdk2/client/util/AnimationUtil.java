package dev.mdk2.client.util;

public final class AnimationUtil {
    private static double animationSync = 0.65D;

    private AnimationUtil() {
    }

    public static void setAnimationSync(final double sync) {
        animationSync = MathUtil.clamp(sync, 0.0D, 1.0D);
    }

    public static double getAnimationSync() {
        return animationSync;
    }

    public static double smooth(final double current, final double target, final double speed) {
        final double multiplier = 0.15D + animationSync * 1.35D;
        final double clampedSpeed = MathUtil.clamp(speed * multiplier, 0.0D, 1.0D);
        return current + (target - current) * clampedSpeed;
    }

    public static double easeOutQuint(final double progress) {
        final double clamped = MathUtil.clamp(progress, 0.0D, 1.0D);
        return 1.0D - Math.pow(1.0D - clamped, 5.0D);
    }

    public static double easeInOutCubic(final double progress) {
        final double clamped = MathUtil.clamp(progress, 0.0D, 1.0D);
        if (clamped < 0.5D) {
            return 4.0D * clamped * clamped * clamped;
        }
        return 1.0D - Math.pow(-2.0D * clamped + 2.0D, 3.0D) / 2.0D;
    }
}
