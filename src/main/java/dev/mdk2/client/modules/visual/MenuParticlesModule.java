package dev.mdk2.client.modules.visual;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;

public class MenuParticlesModule extends Module {
    private final NumberSetting count;
    private final NumberSetting speed;
    private final ModeSetting style;
    private final BooleanSetting twinkle;

    public MenuParticlesModule() {
        super("Menu Particles", "Animated particle field behind the ClickGUI.", Category.VISUAL);
        this.count = register(new NumberSetting("Count", 72.0D, 20.0D, 160.0D, 1.0D));
        this.speed = register(new NumberSetting("Speed", 0.55D, 0.15D, 1.40D, 0.01D));
        this.style = register(new ModeSetting("Style", "Dots", "Dots", "Dust", "Snow", "Links"));
        this.twinkle = register(new BooleanSetting("Twinkle", true));
        setEnabled(false);
    }

    public int getParticleCount() {
        return this.count.getValue().intValue();
    }

    public double getParticleSpeed() {
        return this.speed.getValue().doubleValue();
    }

    public String getParticleStyle() {
        return this.style.getValue();
    }

    public boolean isTwinkleEnabled() {
        return this.twinkle.getValue().booleanValue();
    }
}
