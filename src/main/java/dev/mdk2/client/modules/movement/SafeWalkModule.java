package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.util.world.WorldInteractionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.InputUpdateEvent;

public class SafeWalkModule extends Module {
    public SafeWalkModule() {
        super("Safe Walk", "Prevents you from walking off block edges.", Category.MOVEMENT);
    }

    @Override
    public void onInputUpdate(final InputUpdateEvent event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getPlayer() != minecraft.player || !minecraft.player.isOnGround()) {
            return;
        }

        final Vector3d projected = dev.mdk2.client.util.movement.MovementUtil.getHorizontalMotion(
            minecraft.player.yRot,
            event.getMovementInput().forwardImpulse,
            event.getMovementInput().leftImpulse,
            0.45D
        );
        if (WorldInteractionUtil.isEdgeUnsafe(minecraft.player, projected.x, projected.z)) {
            event.getMovementInput().forwardImpulse = 0.0F;
            event.getMovementInput().leftImpulse = 0.0F;
        }
    }
}
