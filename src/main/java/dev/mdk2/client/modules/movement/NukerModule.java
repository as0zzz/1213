package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.TimeUtil;
import dev.mdk2.client.util.world.WorldInteractionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

import java.util.List;

public class NukerModule extends Module {
    private final ModeSetting mode;
    private final NumberSetting range;
    private final NumberSetting delay;
    private long lastMineTime;

    public NukerModule() {
        super("Nuker", "Auto mine, nuker and speed nuker modes.", Category.MOVEMENT);
        this.mode = register(new ModeSetting("Mode", "AutoMine", "AutoMine", "Nuker", "SpeedNuker"));
        this.range = register(new NumberSetting("Range", 4.0D, 1.0D, 6.0D, 0.5D));
        this.delay = register(new NumberSetting("Delay", 70.0D, 0.0D, 500.0D, 5.0D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null
            || !TimeUtil.passed(this.lastMineTime, this.delay.getValue().doubleValue())) {
            return;
        }

        if ("AutoMine".equalsIgnoreCase(this.mode.getValue())) {
            if (minecraft.hitResult != null && minecraft.hitResult.getType() == RayTraceResult.Type.BLOCK) {
                final BlockPos pos = ((BlockRayTraceResult) minecraft.hitResult).getBlockPos();
                if (WorldInteractionUtil.breakBlock(minecraft, pos)) {
                    this.lastMineTime = TimeUtil.now();
                }
            }
            return;
        }

        final int limit = "SpeedNuker".equalsIgnoreCase(this.mode.getValue()) ? 3 : 1;
        final List<BlockPos> targets = WorldInteractionUtil.findNearestBlocks(
            minecraft.level,
            minecraft.player.blockPosition(),
            this.range.getValue().intValue(),
            limit,
            state -> WorldInteractionUtil.isBreakable(state)
        );
        for (final BlockPos pos : targets) {
            if (WorldInteractionUtil.breakBlock(minecraft, pos)) {
                this.lastMineTime = TimeUtil.now();
            }
        }
    }
}
