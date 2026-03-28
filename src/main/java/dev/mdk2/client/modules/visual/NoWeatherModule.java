package dev.mdk2.client.modules.visual;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import net.minecraft.client.Minecraft;

public class NoWeatherModule extends Module {
    public NoWeatherModule() {
        super("No Weather", "Stops rain and thunder visuals on the client.", Category.VISUAL);
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        minecraft.level.setRainLevel(0.0F);
        minecraft.level.setThunderLevel(0.0F);
    }
}
