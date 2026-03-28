package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class NoFallModule extends Module {
    private final ModeSetting mode;
    private final NumberSetting triggerDistance;

    public NoFallModule() {
        super("No Fall", "Cancels or softens client-side fall damage setup.", Category.MOVEMENT);
        this.mode = register(new ModeSetting("Mode", "Reset", "Reset", "Glide"));
        this.triggerDistance = register(new NumberSetting("Distance", 2.5D, 1.0D, 10.0D, 0.5D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        if (minecraft.player.fallDistance < this.triggerDistance.getValue().doubleValue()) {
            return;
        }

        if ("Glide".equalsIgnoreCase(this.mode.getValue())) {
            final Vector3d motion = minecraft.player.getDeltaMovement();
            minecraft.player.setDeltaMovement(motion.x, Math.max(-0.16D, motion.y * 0.55D), motion.z);
        }

        minecraft.player.fallDistance = 0.0F;
    }
}
