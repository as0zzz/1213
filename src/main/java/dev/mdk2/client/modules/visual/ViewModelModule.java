package dev.mdk2.client.modules.visual;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;

public class ViewModelModule extends Module {
    private final NumberSetting swing;
    private final NumberSetting offsetX;
    private final NumberSetting offsetY;
    private final NumberSetting offsetZ;
    private final NumberSetting rotateYaw;
    private final NumberSetting rotatePitch;
    private final NumberSetting rotateRoll;
    private final NumberSetting scale;
    private final ModeSetting preset;

    public ViewModelModule() {
        super("View Model", "Customizes hand offsets, rotation and scale.", Category.VISUAL);
        this.swing = register(new NumberSetting("Swing", 0.90D, 0.10D, 1.80D, 0.01D));
        this.offsetX = register(new NumberSetting("Offset X", 0.08D, -0.50D, 0.50D, 0.01D));
        this.offsetY = register(new NumberSetting("Offset Y", -0.03D, -0.50D, 0.50D, 0.01D));
        this.offsetZ = register(new NumberSetting("Offset Z", -0.08D, -0.80D, 0.80D, 0.01D));
        this.rotateYaw = register(new NumberSetting("Rotate Yaw", 14.0D, -90.0D, 90.0D, 1.0D));
        this.rotatePitch = register(new NumberSetting("Rotate Pitch", -10.0D, -90.0D, 90.0D, 1.0D));
        this.rotateRoll = register(new NumberSetting("Rotate Roll", 4.0D, -90.0D, 90.0D, 1.0D));
        this.scale = register(new NumberSetting("Scale", 1.04D, 0.50D, 2.0D, 0.01D));
        this.preset = register(new ModeSetting("Preset", "Cinematic", "Cinematic", "Sharp", "Classic", "Minimal", "Custom"));
    }

    public double getSwingScale() {
        return this.swing.getValue().doubleValue();
    }

    public double getOffsetX() {
        return this.offsetX.getValue().doubleValue();
    }

    public double getOffsetY() {
        return this.offsetY.getValue().doubleValue();
    }

    public double getOffsetZ() {
        return this.offsetZ.getValue().doubleValue();
    }

    public double getRotateYaw() {
        return this.rotateYaw.getValue().doubleValue();
    }

    public double getRotatePitch() {
        return this.rotatePitch.getValue().doubleValue();
    }

    public double getRotateRoll() {
        return this.rotateRoll.getValue().doubleValue();
    }

    public double getScaleValue() {
        return this.scale.getValue().doubleValue();
    }

    public String getPreset() {
        return this.preset.getValue();
    }
}
