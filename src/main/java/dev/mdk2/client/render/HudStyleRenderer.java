package dev.mdk2.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.core.ThemeManager;
import dev.mdk2.client.util.ColorUtil;

import java.awt.Font;

public final class HudStyleRenderer {
    private static MenuFontRenderer font;

    private HudStyleRenderer() {
    }

    public static void drawShell(final double x, final double y, final double width, final double height, final double radius,
                                 final ThemeManager themeManager, final int accentColor) {
        UiRenderer.drawRoundedRect(x, y, width, height, radius, themeManager.surfaceRaised(themeManager.isGlass() ? 154 : 228));
        UiRenderer.drawRoundedOutline(x, y, width, height, radius, themeManager.controlOutlineWidth(), themeManager.outline(themeManager.containerOutlineAlpha()));
        UiRenderer.drawRoundedOutline(x + 1.0D, y + 1.0D, width - 2.0D, height - 2.0D, Math.max(1.0D, radius - 1.0D), 1.0D,
            ColorUtil.rgba(255, 255, 255, themeManager.isGlass() ? 18 : 12));
        UiRenderer.drawGradientRect(
            x,
            y,
            width,
            height,
            ColorUtil.rgba(255, 255, 255, themeManager.isGlass() ? 16 : 10),
            ColorUtil.rgba(255, 255, 255, 0),
            ColorUtil.rgba(0, 0, 0, themeManager.isGlass() ? 24 : 18),
            ColorUtil.rgba(0, 0, 0, themeManager.isGlass() ? 20 : 14)
        );
        if (themeManager.shellOuterBandAlpha() <= 0 && themeManager.shellOuterStrokeAlpha() <= 0) {
            return;
        }
        final double bandInset = themeManager.shellOuterBandInset();
        UiRenderer.drawRoundedRect(
            x - bandInset,
            y - bandInset,
            width + bandInset * 2.0D,
            height + bandInset * 2.0D,
            radius + bandInset,
            ColorUtil.rgba(0, 0, 0, themeManager.shellOuterBandAlpha())
        );
        UiRenderer.drawShadow(
            x - 3.0D,
            y - 3.0D,
            width + 6.0D,
            height + 6.0D,
            18,
            ColorUtil.rgba(0, 0, 0, themeManager.shellOuterBandAlpha())
        );
        UiRenderer.drawShadow(
            x - 1.0D,
            y - 1.0D,
            width + 2.0D,
            height + 2.0D,
            10,
            ColorUtil.rgba(0, 0, 0, themeManager.shellOuterStrokeAlpha())
        );
        UiRenderer.drawRoundedOutline(
            x - 1.0D,
            y - 1.0D,
            width + 2.0D,
            height + 2.0D,
            radius + 1.0D,
            1.6D,
            ColorUtil.rgba(0, 0, 0, themeManager.shellOuterStrokeAlpha())
        );
    }

    public static void drawText(final MatrixStack matrixStack, final String text, final double x, final double y, final double scale,
                                final int color, final ThemeManager themeManager) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (themeManager.drawsTextShadow()) {
            renderer().drawString(text, x + 0.8D, y + 0.8D, ColorUtil.rgba(0, 0, 0, 96), scale);
        }
        renderer().drawString(text, x, y, color, scale);
    }

    public static double textWidth(final String text, final double scale) {
        return renderer().width(text, scale);
    }

    public static double lineHeight(final double scale) {
        return renderer().lineHeight(scale);
    }

    private static MenuFontRenderer renderer() {
        if (font == null) {
            font = new MenuFontRenderer("Segoe UI", 14, Font.PLAIN);
        }
        return font;
    }
}
