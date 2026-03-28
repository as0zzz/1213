package dev.mdk2.client.modules.visual;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import net.minecraft.client.Minecraft;

public class FakeWeatherModule extends Module {
    private final ModeSetting mode;
    private final NumberSetting strength;

    public FakeWeatherModule() {
        super("Fake Weather", "Forces local rain or thunder visuals on the client.", Category.VISUAL);
        this.mode = register(new ModeSetting("Mode", "Rain", "Rain", "Thunder", "Clear"));
        this.strength = register(new NumberSetting("Strength", 0.80D, 0.10D, 1.00D, 0.05D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        final float level = (float) this.strength.getValue().doubleValue();
        if ("Clear".equalsIgnoreCase(this.mode.getValue())) {
            minecraft.level.setRainLevel(0.0F);
            minecraft.level.setThunderLevel(0.0F);
            return;
        }

        minecraft.level.setRainLevel(level);
        minecraft.level.setThunderLevel("Thunder".equalsIgnoreCase(this.mode.getValue()) ? level : 0.0F);
    }

    @Override
    public void onDisable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        minecraft.level.setRainLevel(0.0F);
        minecraft.level.setThunderLevel(0.0F);
    }
}
