package dev.mdk2.client.modules.visual;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.NumberSetting;

public class ZoomModule extends Module {
    private final NumberSetting fov;

    public ZoomModule() {
        super("Zoom", "Narrows the camera FOV for close inspection.", Category.VISUAL);
        this.fov = register(new NumberSetting("FOV", 28.0D, 8.0D, 70.0D, 1.0D));
    }

    public double getTargetFov() {
        return this.fov.getValue().doubleValue();
    }
}
