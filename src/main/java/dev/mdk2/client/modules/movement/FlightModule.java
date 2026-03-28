package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.movement.MovementUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class FlightModule extends Module {
    private final ModeSetting mode;
    private final NumberSetting speed;
    private final NumberSetting vertical;
    private boolean previousMayfly;
    private boolean previousFlying;

    public FlightModule() {
        super("Flight", "Fly, creative flight and glide movement modes.", Category.MOVEMENT);
        this.mode = register(new ModeSetting("Mode", "Fly", "Fly", "CreativeFlight", "Glide"));
        this.speed = register(new NumberSetting("Speed", 0.7D, 0.1D, 3.5D, 0.05D));
        this.vertical = register(new NumberSetting("Vertical", 0.42D, 0.05D, 1.5D, 0.05D));
    }

    @Override
    public void onEnable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            setEnabled(false);
            return;
        }

        this.previousMayfly = minecraft.player.abilities.mayfly;
        this.previousFlying = minecraft.player.abilities.flying;
    }

    @Override
    public void onDisable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        minecraft.player.setNoGravity(false);
        minecraft.player.abilities.mayfly = this.previousMayfly;
        minecraft.player.abilities.flying = this.previousFlying;
        minecraft.player.onUpdateAbilities();
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            setEnabled(false);
            return;
        }

        final String modeName = this.mode.getValue();
        if ("CreativeFlight".equalsIgnoreCase(modeName)) {
            minecraft.player.abilities.mayfly = true;
            minecraft.player.abilities.flying = true;
            minecraft.player.onUpdateAbilities();
            minecraft.player.setNoGravity(true);
            MovementUtil.applyFlightMotion(minecraft.player, minecraft, this.speed.getValue().doubleValue(), this.vertical.getValue().doubleValue());
            return;
        }

        if ("Glide".equalsIgnoreCase(modeName)) {
            minecraft.player.setNoGravity(false);
            final Vector3d motion = minecraft.player.getDeltaMovement();
            if (!minecraft.player.isOnGround() && motion.y < 0.0D) {
                final double glideFall = -Math.max(0.04D, this.vertical.getValue().doubleValue() * 0.18D);
                minecraft.player.setDeltaMovement(motion.x, Math.max(glideFall, motion.y * 0.65D), motion.z);
            }
            if (MovementUtil.isMoving(minecraft)) {
                MovementUtil.applyHorizontalMotion(minecraft.player, minecraft, this.speed.getValue().doubleValue());
            }
            return;
        }

        minecraft.player.setNoGravity(true);
        minecraft.player.fallDistance = 0.0F;
        MovementUtil.applyFlightMotion(minecraft.player, minecraft, this.speed.getValue().doubleValue(), this.vertical.getValue().doubleValue());
    }
}
