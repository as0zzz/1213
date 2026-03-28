package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class FastLadderModule extends Module {
    private final NumberSetting speed;

    public FastLadderModule() {
        super("Fast Ladder", "Climbs ladders and vines faster.", Category.MOVEMENT);
        this.speed = register(new NumberSetting("Speed", 0.28D, 0.05D, 1.0D, 0.01D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !minecraft.player.onClimbable()) {
            return;
        }

        final Vector3d motion = minecraft.player.getDeltaMovement();
        minecraft.player.setDeltaMovement(motion.x, Math.max(motion.y, this.speed.getValue().doubleValue()), motion.z);
    }
}
