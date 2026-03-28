package dev.mdk2.client.modules.misc;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;

public class ThemeModule extends Module {
    private final ModeSetting theme;
    private final NumberSetting animationSync;

    public ThemeModule() {
        super("Theme", "Switches between Dark and Glass interface styles.", Category.MISC);
        hideBindSetting();
        this.theme = register(new ModeSetting("Style", "Dark", "Dark", "Glass"));
        this.animationSync = register(new NumberSetting("Animation Sync", 0.65D, 0.0D, 1.0D, 0.01D));
        setToggleable(false);
        setEnabled(true);
    }

    public String getThemeName() {
        return this.theme.getValue();
    }

    public double getAnimationSync() {
        return this.animationSync.getValue().doubleValue();
    }
}
