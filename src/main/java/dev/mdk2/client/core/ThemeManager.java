package dev.mdk2.client.core;

import dev.mdk2.client.util.ColorUtil;

public class ThemeManager {
    private ThemeStyle style = ThemeStyle.DARK;

    public void tick() {
    }

    public void setStyle(final String styleName) {
        if ("Glass".equalsIgnoreCase(styleName)) {
            this.style = ThemeStyle.GLASS;
        } else {
            this.style = ThemeStyle.DARK;
        }
    }

    public ThemeStyle getStyle() {
        return this.style;
    }

    public boolean isGlass() {
        return this.style == ThemeStyle.GLASS;
    }

    public int accent(final double offset) {
        if (this.style == ThemeStyle.GLASS) {
            return ColorUtil.rgba(168, 220, 255, 255);
        }
        return ColorUtil.rgba(118, 146, 255, 255);
    }

    public int accentSoft(final double offset, final int alpha) {
        return ColorUtil.withAlpha(accent(offset), alpha);
    }

    public int background(final int alpha) {
        if (this.style == ThemeStyle.GLASS) {
            return ColorUtil.rgba(18, 24, 33, Math.min(alpha, 110));
        }
        return ColorUtil.rgba(12, 14, 20, alpha);
    }

    public int surface(final int alpha) {
        if (this.style == ThemeStyle.GLASS) {
            return ColorUtil.rgba(24, 30, 42, Math.min(alpha, 128));
        }
        return ColorUtil.rgba(17, 20, 28, alpha);
    }

    public int surfaceRaised(final int alpha) {
        if (this.style == ThemeStyle.GLASS) {
            return ColorUtil.rgba(38, 46, 62, Math.min(alpha, 152));
        }
        return ColorUtil.rgba(26, 31, 42, alpha);
    }

    public int outline(final int alpha) {
        if (this.style == ThemeStyle.GLASS) {
            return ColorUtil.rgba(255, 255, 255, Math.min(alpha, 64));
        }
        return ColorUtil.rgba(255, 255, 255, alpha);
    }

    public int textPrimary() {
        return ColorUtil.rgba(242, 245, 255, 255);
    }

    public int textSecondary() {
        if (this.style == ThemeStyle.GLASS) {
            return ColorUtil.rgba(198, 208, 226, 255);
        }
        return ColorUtil.rgba(151, 161, 184, 255);
    }

    public enum ThemeStyle {
        DARK,
        GLASS
    }
}
