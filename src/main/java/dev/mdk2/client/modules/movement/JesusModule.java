package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.movement.MovementUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class JesusModule extends Module {
    private final NumberSetting speed;
    private final NumberSetting buoyancy;

    public JesusModule() {
        super("Jesus", "Lets you walk across water surfaces.", Category.MOVEMENT);
        this.speed = register(new NumberSetting("Speed", 0.26D, 0.05D, 1.0D, 0.01D));
        this.buoyancy = register(new NumberSetting("Buoyancy", 0.11D, 0.01D, 0.4D, 0.01D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.player.isShiftKeyDown()) {
            return;
        }

        final BlockPos feetPos = new BlockPos(minecraft.player.getX(), minecraft.player.getY() - 0.05D, minecraft.player.getZ());
        final FluidState fluidState = minecraft.level.getFluidState(feetPos);
        if (!fluidState.is(FluidTags.WATER)) {
            return;
        }

        final Vector3d motion = minecraft.player.getDeltaMovement();
        minecraft.player.setDeltaMovement(motion.x, this.buoyancy.getValue().doubleValue(), motion.z);
        if (MovementUtil.isMoving(minecraft)) {
            MovementUtil.applyHorizontalMotion(minecraft.player, minecraft, this.speed.getValue().doubleValue());
        }
        minecraft.player.fallDistance = 0.0F;
    }
}
