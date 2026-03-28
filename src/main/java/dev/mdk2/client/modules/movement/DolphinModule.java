package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.movement.MovementUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class DolphinModule extends Module {
    private final NumberSetting speed;
    private final NumberSetting rise;

    public DolphinModule() {
        super("Dolphin", "Boosts swimming speed in water.", Category.MOVEMENT);
        this.speed = register(new NumberSetting("Speed", 0.42D, 0.05D, 1.6D, 0.01D));
        this.rise = register(new NumberSetting("Rise", 0.04D, 0.0D, 0.25D, 0.01D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !minecraft.player.isInWater()) {
            return;
        }

        if (MovementUtil.isMoving(minecraft)) {
            MovementUtil.applyHorizontalMotion(minecraft.player, minecraft, this.speed.getValue().doubleValue());
        }
        final Vector3d motion = minecraft.player.getDeltaMovement();
        minecraft.player.setDeltaMovement(motion.x, motion.y + this.rise.getValue().doubleValue(), motion.z);
        minecraft.player.setSprinting(true);
    }
}
