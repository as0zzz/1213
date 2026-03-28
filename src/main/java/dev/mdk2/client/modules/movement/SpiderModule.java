package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class SpiderModule extends Module {
    private final NumberSetting climbSpeed;
    private final NumberSetting clingBoost;

    public SpiderModule() {
        super("Spider", "Climbs vertical walls when you collide with them.", Category.MOVEMENT);
        this.climbSpeed = register(new NumberSetting("Climb Speed", 0.24D, 0.05D, 1.0D, 0.01D));
        this.clingBoost = register(new NumberSetting("Cling Boost", 0.03D, 0.0D, 0.25D, 0.01D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        if (minecraft.player.horizontalCollision && !minecraft.player.isInWater() && !minecraft.player.isOnGround()) {
            final Vector3d motion = minecraft.player.getDeltaMovement();
            minecraft.player.setDeltaMovement(motion.x, this.climbSpeed.getValue().doubleValue(), motion.z);
        } else if (minecraft.player.horizontalCollision && minecraft.player.isOnGround()) {
            final Vector3d motion = minecraft.player.getDeltaMovement();
            minecraft.player.setDeltaMovement(motion.x, this.clingBoost.getValue().doubleValue(), motion.z);
        }
    }
}
