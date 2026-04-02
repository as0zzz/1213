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
            return ColorUtil.rgba(104, 166, 255, 255);
        }
        return ColorUtil.rgba(118, 146, 255, 255);
    }

    public int accentSoft(final double offset, final int alpha) {
        return ColorUtil.withAlpha(accent(offset), alpha);
    }

    public int background(final int alpha) {
        if (this.style == ThemeStyle.GLASS) {
            return ColorUtil.rgba(18, 28, 42, Math.min(alpha, 128));
        }
        return ColorUtil.rgba(12, 14, 20, alpha);
    }

    public int surface(final int alpha) {
        if (this.style == ThemeStyle.GLASS) {
            return ColorUtil.rgba(34, 46, 64, Math.min(alpha, 154));
        }
        return ColorUtil.rgba(17, 20, 28, alpha);
    }

    public int surfaceRaised(final int alpha) {
        if (this.style == ThemeStyle.GLASS) {
            return ColorUtil.rgba(54, 68, 94, Math.min(alpha, 180));
        }
        return ColorUtil.rgba(26, 31, 42, alpha);
    }

    public int outline(final int alpha) {
        if (this.style == ThemeStyle.GLASS) {
            return ColorUtil.rgba(255, 255, 255, Math.min(alpha, 74));
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

    public boolean drawsTextShadow() {
        return this.style != ThemeStyle.GLASS;
    }

    public int windowOutlineAlpha() {
        return this.style == ThemeStyle.GLASS ? 44 : 36;
    }

    public int containerOutlineAlpha() {
        return this.style == ThemeStyle.GLASS ? 38 : 30;
    }

    public int controlOutlineAlpha() {
        return this.style == ThemeStyle.GLASS ? 34 : 28;
    }

    public int popupOutlineAlpha() {
        return containerOutlineAlpha();
    }

    public int shellShadowAlpha() {
        return 0;
    }

    public int controlShadowAlpha() {
        return 0;
    }

    public int controlShadowSpread() {
        return 8;
    }

    public int shellUnderlayAlpha() {
        return 0;
    }

    public int backdropBaseAlpha(final boolean inWorld) {
        if (this.style == ThemeStyle.GLASS) {
            return inWorld ? 126 : 84;
        }
        return inWorld ? 150 : 118;
    }

    public int backdropTopAlpha(final boolean inWorld) {
        if (this.style == ThemeStyle.GLASS) {
            return inWorld ? 36 : 24;
        }
        return inWorld ? 34 : 26;
    }

    public int backdropBottomAlpha(final boolean inWorld) {
        if (this.style == ThemeStyle.GLASS) {
            return inWorld ? 166 : 126;
        }
        return inWorld ? 146 : 104;
    }

    public int shellSheenTopAlpha() {
        return 0;
    }

    public int shellSheenBottomAlpha() {
        return 0;
    }

    public int shellInnerOutlineAlpha() {
        return this.style == ThemeStyle.GLASS ? 18 : 12;
    }

    public int shellOuterStrokeAlpha() {
        return 0;
    }

    public int shellOuterBandAlpha() {
        return 0;
    }

    public double shellOuterBandInset() {
        return 0.0D;
    }

    public double windowOutlineWidth() {
        return 2.3D;
    }

    public double containerOutlineWidth() {
        return 2.2D;
    }

    public double controlOutlineWidth() {
        return 2.0D;
    }

    public double popupOutlineWidth() {
        return 2.2D;
    }

    public double footerProfileOutlineWidth() {
        return 0.0D;
    }

    public enum ThemeStyle {
        DARK,
        GLASS
    }
}
