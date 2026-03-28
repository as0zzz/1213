package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputUpdateEvent;

public class AutoMoveModule extends Module {
    private final BooleanSetting autoSprint;
    private final BooleanSetting autoWalk;
    private final BooleanSetting autoSwim;
    private final NumberSetting swimBoost;

    public AutoMoveModule() {
        super("Auto Move", "Auto sprint, walk and swim helpers.", Category.MOVEMENT);
        this.autoSprint = register(new BooleanSetting("Auto Sprint", true));
        this.autoWalk = register(new BooleanSetting("Auto Walk", false));
        this.autoSwim = register(new BooleanSetting("Auto Swim", true));
        this.swimBoost = register(new NumberSetting("Swim Boost", 0.03D, 0.0D, 0.2D, 0.01D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        if (this.autoSprint.getValue().booleanValue()
            && (this.autoWalk.getValue().booleanValue() || minecraft.player.input != null && minecraft.player.input.forwardImpulse > 0.0F)
            && !minecraft.player.isUsingItem()) {
            minecraft.player.setSprinting(true);
        }

        if (this.autoSwim.getValue().booleanValue() && minecraft.player.isInWater()) {
            minecraft.player.setSprinting(true);
            minecraft.player.setDeltaMovement(
                minecraft.player.getDeltaMovement().x,
                minecraft.player.getDeltaMovement().y + this.swimBoost.getValue().doubleValue(),
                minecraft.player.getDeltaMovement().z
            );
        }
    }

    @Override
    public void onInputUpdate(final InputUpdateEvent event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getPlayer() != minecraft.player) {
            return;
        }

        if (this.autoWalk.getValue().booleanValue()) {
            event.getMovementInput().forwardImpulse = Math.max(event.getMovementInput().forwardImpulse, 1.0F);
        }
    }
}
