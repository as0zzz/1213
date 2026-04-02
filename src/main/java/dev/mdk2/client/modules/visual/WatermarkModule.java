package dev.mdk2.client.modules.visual;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;

public class WatermarkModule extends Module {
    private final BooleanSetting logo;
    private final BooleanSetting framerate;
    private final BooleanSetting ping;
    private final BooleanSetting speed;
    private final BooleanSetting gpuLoad;
    private final BooleanSetting cpuLoad;
    private final BooleanSetting memoryLoad;
    private final BooleanSetting username;
    private final BooleanSetting configName;
    private final BooleanSetting serverIp;
    private double positionX = -1.0D;
    private double positionY = 7.0D;

    public WatermarkModule() {
        super("Watermark", "Displays the compact menu-style watermark widget.", Category.VISUAL);
        this.logo = register(new BooleanSetting("Logo", true));
        this.framerate = register(new BooleanSetting("Framerate", true));
        this.ping = register(new BooleanSetting("Ping", true));
        this.speed = register(new BooleanSetting("Speed", false));
        this.gpuLoad = register(new BooleanSetting("GPU Load", false));
        this.cpuLoad = register(new BooleanSetting("CPU Load", false));
        this.memoryLoad = register(new BooleanSetting("Memory Load", false));
        this.username = register(new BooleanSetting("Username", false));
        this.configName = register(new BooleanSetting("Config Name", false));
        this.serverIp = register(new BooleanSetting("Server IP", false));
        hideBindSetting();
        setToggleable(false);
        setEnabled(true);
    }

    public boolean isLogoEnabled() {
        return this.logo.getValue().booleanValue();
    }

    public boolean isFramerateEnabled() {
        return this.framerate.getValue().booleanValue();
    }

    public boolean isPingEnabled() {
        return this.ping.getValue().booleanValue();
    }

    public boolean isSpeedEnabled() {
        return this.speed.getValue().booleanValue();
    }

    public boolean isGpuLoadEnabled() {
        return this.gpuLoad.getValue().booleanValue();
    }

    public boolean isCpuLoadEnabled() {
        return this.cpuLoad.getValue().booleanValue();
    }

    public boolean isMemoryLoadEnabled() {
        return this.memoryLoad.getValue().booleanValue();
    }

    public boolean isUsernameEnabled() {
        return this.username.getValue().booleanValue();
    }

    public boolean isConfigNameEnabled() {
        return this.configName.getValue().booleanValue();
    }

    public boolean isServerIpEnabled() {
        return this.serverIp.getValue().booleanValue();
    }

    public double getPositionX() {
        return this.positionX;
    }

    public double getPositionY() {
        return this.positionY;
    }

    public double getRenderX(final int screenWidth, final double widgetWidth) {
        if (this.positionX < 0.0D) {
            this.positionX = screenWidth - widgetWidth - 8.0D;
        }
        return this.positionX;
    }

    public double getRenderY() {
        return this.positionY;
    }

    public void setPosition(final double x, final double y) {
        this.positionX = x;
        this.positionY = y;
    }
}
