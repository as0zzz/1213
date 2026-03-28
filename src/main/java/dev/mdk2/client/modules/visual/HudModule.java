package dev.mdk2.client.modules.visual;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ModeSetting;

public class HudModule extends Module {
    private final BooleanSetting watermark;
    private final BooleanSetting functions;
    private final BooleanSetting fpsCounter;
    private final BooleanSetting ping;
    private final ModeSetting style;
    private double watermarkOffsetX = 8.0D;
    private double watermarkOffsetY = 7.0D;
    private double modulesOffsetX = 6.0D;
    private double modulesOffsetY = 8.0D;

    public HudModule() {
        super("HUD", "Displays active modules and session stats.", Category.VISUAL);
        this.watermark = register(new BooleanSetting("Watermark", true));
        this.functions = register(new BooleanSetting("Functions", true));
        this.fpsCounter = register(new BooleanSetting("FPS Counter", true));
        this.ping = register(new BooleanSetting("Ping", true));
        this.style = register(new ModeSetting("Layout", "Solid", "Solid", "Separate"));
        setEnabled(true);
    }

    public boolean isWatermarkEnabled() {
        return this.watermark.getValue().booleanValue();
    }

    public boolean isFpsCounterEnabled() {
        return this.fpsCounter.getValue().booleanValue();
    }

    public boolean isFunctionsEnabled() {
        return this.functions.getValue().booleanValue();
    }

    public boolean isPingEnabled() {
        return this.ping.getValue().booleanValue();
    }

    public boolean isSolidLayout() {
        return "Solid".equalsIgnoreCase(this.style.getValue());
    }

    public boolean isSeparateLayout() {
        return "Separate".equalsIgnoreCase(this.style.getValue());
    }

    public double getWatermarkOffsetX() {
        return this.watermarkOffsetX;
    }

    public double getWatermarkOffsetY() {
        return this.watermarkOffsetY;
    }

    public double getModulesOffsetX() {
        return this.modulesOffsetX;
    }

    public double getModulesOffsetY() {
        return this.modulesOffsetY;
    }

    public void setWatermarkPosition(final double offsetX, final double offsetY) {
        this.watermarkOffsetX = offsetX;
        this.watermarkOffsetY = offsetY;
    }

    public void setModulesPosition(final double offsetX, final double offsetY) {
        this.modulesOffsetX = offsetX;
        this.modulesOffsetY = offsetY;
    }
}
