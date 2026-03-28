package dev.mdk2.client.modules.combat;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class VelocityModule extends Module {
    private final NumberSetting horizontal;
    private final NumberSetting vertical;
    private final ModeSetting mode;

    public VelocityModule() {
        super("Anti Knockback", "Reduces or cancels incoming velocity on the client.", Category.COMBAT);
        this.horizontal = register(new NumberSetting("Horizontal", 0.0D, 0.0D, 100.0D, 1.0D));
        this.vertical = register(new NumberSetting("Vertical", 0.0D, 0.0D, 100.0D, 1.0D));
        this.mode = register(new ModeSetting("Mode", "Cancel", "Cancel", "Reduce", "Matrix"));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.hurtTime <= 0) {
            return;
        }

        final Vector3d movement = minecraft.player.getDeltaMovement();
        if ("Matrix".equalsIgnoreCase(this.mode.getValue())) {
            minecraft.player.setDeltaMovement(movement.x * 0.45D, movement.y * 0.98D, movement.z * 0.45D);
            if (minecraft.player.isOnGround()) {
                minecraft.player.setSprinting(true);
            }
            return;
        }

        if ("Reduce".equalsIgnoreCase(this.mode.getValue())) {
            final double horizontalScale = this.horizontal.getValue().doubleValue() / 100.0D;
            final double verticalScale = this.vertical.getValue().doubleValue() / 100.0D;
            minecraft.player.setDeltaMovement(movement.x * horizontalScale, movement.y * verticalScale, movement.z * horizontalScale);
            return;
        }

        minecraft.player.setDeltaMovement(0.0D, 0.0D, 0.0D);
    }
}
