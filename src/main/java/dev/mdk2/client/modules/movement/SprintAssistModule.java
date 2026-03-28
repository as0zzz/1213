package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.movement.MovementUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputUpdateEvent;

public class SprintAssistModule extends Module {
    private final BooleanSetting omniSprint;
    private final BooleanSetting keepSprint;
    private final BooleanSetting allowWater;
    private final BooleanSetting checkHunger;
    private final BooleanSetting allowUsingItems;
    private final NumberSetting momentum;

    public SprintAssistModule() {
        super("Sprint Assist", "Maintains sprint while moving and boosts momentum slightly.", Category.MOVEMENT);
        this.omniSprint = register(new BooleanSetting("Omni Sprint", true));
        this.keepSprint = register(new BooleanSetting("Keep Sprint", true));
        this.allowWater = register(new BooleanSetting("Allow Water", false));
        this.checkHunger = register(new BooleanSetting("Check Hunger", true));
        this.allowUsingItems = register(new BooleanSetting("While Using Item", false));
        this.momentum = register(new NumberSetting("Momentum", 1.00D, 1.0D, 1.18D, 0.01D));
        setEnabled(true);
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        if (!canSprint(minecraft)) {
            return;
        }

        minecraft.player.setSprinting(true);
        if (this.momentum.getValue().doubleValue() > 1.0D && MovementUtil.isMoving(minecraft)) {
            minecraft.player.setDeltaMovement(MovementUtil.clampHorizontal(
                minecraft.player.getDeltaMovement().multiply(this.momentum.getValue().doubleValue(), 1.0D, this.momentum.getValue().doubleValue()),
                0.42D * this.momentum.getValue().doubleValue()
            ));
        }
    }

    @Override
    public void onInputUpdate(final InputUpdateEvent event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getPlayer() != minecraft.player) {
            return;
        }

        if (!this.omniSprint.getValue().booleanValue() || !canSprint(minecraft)) {
            return;
        }

        if (Math.abs(event.getMovementInput().forwardImpulse) < 1.0E-3F && Math.abs(event.getMovementInput().leftImpulse) > 1.0E-3F) {
            event.getMovementInput().forwardImpulse = 1.0E-3F;
        }
    }

    private boolean canSprint(final Minecraft minecraft) {
        if (!MovementUtil.isMoving(minecraft) || minecraft.player.isShiftKeyDown()) {
            return false;
        }

        if (!this.allowWater.getValue().booleanValue() && minecraft.player.isInWater()) {
            return false;
        }

        if (!this.allowUsingItems.getValue().booleanValue() && minecraft.player.isUsingItem()) {
            return false;
        }

        if (this.checkHunger.getValue().booleanValue() && minecraft.player.getFoodData().getFoodLevel() <= 6) {
            return false;
        }

        return this.keepSprint.getValue().booleanValue() || minecraft.player.input == null || minecraft.player.input.forwardImpulse > 0.0F;
    }
}
