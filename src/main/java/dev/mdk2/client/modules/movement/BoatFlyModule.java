package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.movement.MovementUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.math.vector.Vector3d;

public class BoatFlyModule extends Module {
    private final NumberSetting speed;
    private final NumberSetting vertical;

    public BoatFlyModule() {
        super("Boat Fly", "Controls boats in the air.", Category.MOVEMENT);
        this.speed = register(new NumberSetting("Speed", 0.7D, 0.05D, 3.0D, 0.05D));
        this.vertical = register(new NumberSetting("Vertical", 0.35D, 0.05D, 1.5D, 0.05D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        final Entity vehicle = minecraft.player.getVehicle();
        if (!(vehicle instanceof BoatEntity)) {
            return;
        }

        vehicle.setNoGravity(true);
        final Vector3d horizontal = MovementUtil.getHorizontalMotion(minecraft.player.yRot,
            MovementUtil.getForwardInput(minecraft), MovementUtil.getStrafeInput(minecraft), this.speed.getValue().doubleValue());
        double verticalMotion = 0.0D;
        if (minecraft.options.keyJump.isDown()) {
            verticalMotion += this.vertical.getValue().doubleValue();
        }
        if (minecraft.options.keyShift.isDown()) {
            verticalMotion -= this.vertical.getValue().doubleValue();
        }
        vehicle.setDeltaMovement(horizontal.x, verticalMotion, horizontal.z);
    }
}
