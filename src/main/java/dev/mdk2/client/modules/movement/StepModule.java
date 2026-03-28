package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.NumberSetting;
import net.minecraft.client.Minecraft;

public class StepModule extends Module {
    private final NumberSetting height;
    private float previousStepHeight = 0.6F;

    public StepModule() {
        super("Step", "Steps up full blocks without jumping.", Category.MOVEMENT);
        this.height = register(new NumberSetting("Height", 1.2D, 0.6D, 3.0D, 0.1D));
    }

    @Override
    public void onEnable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            this.previousStepHeight = minecraft.player.maxUpStep;
        }
    }

    @Override
    public void onDisable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.maxUpStep = this.previousStepHeight;
        }
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.maxUpStep = this.height.getValue().floatValue();
        }
    }
}
