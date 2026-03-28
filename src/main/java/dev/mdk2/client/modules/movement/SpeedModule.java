package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.movement.MovementUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class SpeedModule extends Module {
    private final ModeSetting mode;
    private final NumberSetting speed;
    private final NumberSetting hopMotion;
    private final NumberSetting airControl;

    public SpeedModule() {
        super("Speed", "Horizontal speed, speedhack and bunnyhop movement.", Category.MOVEMENT);
        this.mode = register(new ModeSetting("Mode", "Speed", "Speed", "SpeedHack", "BunnyHop"));
        this.speed = register(new NumberSetting("Speed", 0.34D, 0.1D, 2.0D, 0.01D));
        this.hopMotion = register(new NumberSetting("Hop Motion", 0.42D, 0.2D, 1.2D, 0.01D));
        this.airControl = register(new NumberSetting("Air Control", 0.98D, 0.1D, 1.2D, 0.01D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        if (!MovementUtil.isMoving(minecraft) || minecraft.player.isShiftKeyDown() || minecraft.player.isInWater()) {
            return;
        }

        minecraft.player.setSprinting(true);
        final String modeName = this.mode.getValue();
        if ("BunnyHop".equalsIgnoreCase(modeName)) {
            if (minecraft.player.isOnGround()) {
                final Vector3d motion = minecraft.player.getDeltaMovement();
                minecraft.player.setDeltaMovement(motion.x, this.hopMotion.getValue().doubleValue(), motion.z);
            }
            MovementUtil.applyHorizontalMotion(minecraft.player, minecraft, this.speed.getValue().doubleValue());
            return;
        }

        if ("SpeedHack".equalsIgnoreCase(modeName)) {
            MovementUtil.applyHorizontalMotion(minecraft.player, minecraft, this.speed.getValue().doubleValue() * 1.18D);
            return;
        }

        final Vector3d current = minecraft.player.getDeltaMovement();
        if (minecraft.player.isOnGround()) {
            MovementUtil.applyHorizontalMotion(minecraft.player, minecraft, this.speed.getValue().doubleValue());
        } else {
            minecraft.player.setDeltaMovement(
                current.x * this.airControl.getValue().doubleValue(),
                current.y,
                current.z * this.airControl.getValue().doubleValue()
            );
        }
    }
}
