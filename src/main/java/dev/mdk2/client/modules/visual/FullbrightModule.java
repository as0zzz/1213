package dev.mdk2.client.modules.visual;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.NumberSetting;
import net.minecraft.client.Minecraft;

public class FullbrightModule extends Module {
    private final NumberSetting strength;
    private double previousGamma = -1.0D;

    public FullbrightModule() {
        super("Fullbright", "Boosts gamma so dark areas stay fully visible.", Category.VISUAL);
        this.strength = register(new NumberSetting("Strength", 12.0D, 2.0D, 24.0D, 0.5D));
    }

    @Override
    public void onEnable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (this.previousGamma < 0.0D) {
            this.previousGamma = minecraft.options.gamma;
        }
        minecraft.options.gamma = this.strength.getValue().doubleValue();
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.options.gamma = this.strength.getValue().doubleValue();
    }

    @Override
    public void onDisable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (this.previousGamma >= 0.0D) {
            minecraft.options.gamma = this.previousGamma;
        }
        this.previousGamma = -1.0D;
    }
}
