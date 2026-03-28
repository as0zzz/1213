package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputUpdateEvent;

public class NoSlowModule extends Module {
    private final BooleanSetting food;
    private final BooleanSetting bow;
    private final NumberSetting multiplier;

    public NoSlowModule() {
        super("No Slow", "Prevents heavy slowdown while using items.", Category.MOVEMENT);
        this.food = register(new BooleanSetting("Food", true));
        this.bow = register(new BooleanSetting("Bow", true));
        this.multiplier = register(new NumberSetting("Multiplier", 5.0D, 1.0D, 10.0D, 0.25D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.isUsingItem()) {
            minecraft.player.setSprinting(true);
        }
    }

    @Override
    public void onInputUpdate(final InputUpdateEvent event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getPlayer() != minecraft.player || !minecraft.player.isUsingItem()) {
            return;
        }

        final boolean foodUse = minecraft.player.getUseItem().isEdible();
        final boolean rangedUse = !foodUse;
        if ((foodUse && !this.food.getValue().booleanValue()) || (rangedUse && !this.bow.getValue().booleanValue())) {
            return;
        }

        final float scale = this.multiplier.getValue().floatValue();
        event.getMovementInput().forwardImpulse *= scale;
        event.getMovementInput().leftImpulse *= scale;
    }
}
