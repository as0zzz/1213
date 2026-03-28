package dev.mdk2.client.settings;

import dev.mdk2.client.util.MathUtil;

import java.awt.Color;

public class ColorSetting extends Setting<Integer> {
    public ColorSetting(final String name, final int color) {
        super(name, Integer.valueOf(color));
    }

    public int getColor() {
        return getValue().intValue();
    }

    public void setColor(final int color) {
        setValueInternal(Integer.valueOf(color));
    }

    public float getHue() {
        return getHsb()[0];
    }

    public float getSaturation() {
        return getHsb()[1];
    }

    public float getBrightness() {
        return getHsb()[2];
    }

    public float getAlpha() {
        return (getColor() >>> 24 & 255) / 255.0F;
    }

    public void setHue(final float hue) {
        setFromHsb(hue, getSaturation(), getBrightness(), getAlpha());
    }

    public void setSaturation(final float saturation) {
        setFromHsb(getHue(), saturation, getBrightness(), getAlpha());
    }

    public void setBrightness(final float brightness) {
        setFromHsb(getHue(), getSaturation(), brightness, getAlpha());
    }

    public void setAlpha(final float alpha) {
        setFromHsb(getHue(), getSaturation(), getBrightness(), alpha);
    }

    public void setFromHsb(final float hue, final float saturation, final float brightness, final float alpha) {
        final int rgb = Color.HSBtoRGB(
            (float) MathUtil.clamp(hue, 0.0D, 1.0D),
            (float) MathUtil.clamp(saturation, 0.0D, 1.0D),
            (float) MathUtil.clamp(brightness, 0.0D, 1.0D)
        );
        final int alphaChannel = (int) Math.round(MathUtil.clamp(alpha, 0.0D, 1.0D) * 255.0D);
        setColor((alphaChannel & 255) << 24 | (rgb & 0x00FFFFFF));
    }

    private float[] getHsb() {
        final int color = getColor();
        return Color.RGBtoHSB(color >>> 16 & 255, color >>> 8 & 255, color & 255, null);
    }
}
