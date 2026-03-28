package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.TimeUtil;
import dev.mdk2.client.util.movement.MovementUtil;
import dev.mdk2.client.util.world.WorldInteractionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class ScaffoldModule extends Module {
    private final ModeSetting mode;
    private final BooleanSetting fromInventory;
    private final BooleanSetting tower;
    private final BooleanSetting keepY;
    private final NumberSetting expand;
    private final NumberSetting delay;
    private long lastPlaceTime;
    private int anchorY;

    public ScaffoldModule() {
        super("Scaffold", "Places blocks under you or ahead while bridging.", Category.MOVEMENT);
        this.mode = register(new ModeSetting("Mode", "Scaffold", "Scaffold", "AutoBuild"));
        this.fromInventory = register(new BooleanSetting("From Inventory", true));
        this.tower = register(new BooleanSetting("Tower", true));
        this.keepY = register(new BooleanSetting("Keep Y", true));
        this.expand = register(new NumberSetting("Expand", 1.0D, 0.0D, 3.0D, 1.0D));
        this.delay = register(new NumberSetting("Delay", 40.0D, 0.0D, 250.0D, 5.0D));
    }

    @Override
    public void onEnable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            this.anchorY = minecraft.player.blockPosition().getY();
        }
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !TimeUtil.passed(this.lastPlaceTime, this.delay.getValue().doubleValue())) {
            return;
        }

        if (minecraft.player.isOnGround()) {
            this.anchorY = minecraft.player.blockPosition().getY();
        }

        final int slot = WorldInteractionUtil.findPlaceableBlockSlot(minecraft.player, this.fromInventory.getValue().booleanValue());
        if (slot < 0) {
            return;
        }

        final BlockPos target = resolvePlacementTarget(minecraft);
        if (target == null) {
            return;
        }

        if (WorldInteractionUtil.placeBlock(minecraft, target, slot)) {
            this.lastPlaceTime = TimeUtil.now();
            if (this.tower.getValue().booleanValue() && minecraft.options.keyJump.isDown()) {
                minecraft.player.setDeltaMovement(minecraft.player.getDeltaMovement().x, 0.42D, minecraft.player.getDeltaMovement().z);
            }
        }
    }

    private BlockPos resolvePlacementTarget(final Minecraft minecraft) {
        final int extend = Math.max(0, this.expand.getValue().intValue());
        final boolean autoBuild = "AutoBuild".equalsIgnoreCase(this.mode.getValue());
        final float moveYaw = MovementUtil.getMoveYaw(minecraft.player, minecraft);
        final double radians = Math.toRadians(moveYaw);
        final int offsetX = autoBuild ? -Math.round((float) Math.sin(radians) * Math.max(1, extend)) : 0;
        final int offsetZ = autoBuild ? Math.round((float) Math.cos(radians) * Math.max(1, extend)) : 0;
        final int baseY = this.keepY.getValue().booleanValue() ? this.anchorY - 1 : minecraft.player.blockPosition().below().getY();
        final BlockPos origin = new BlockPos(minecraft.player.getX() + offsetX, baseY, minecraft.player.getZ() + offsetZ);

        for (int down = 0; down <= 2; down++) {
            final BlockPos candidate = origin.below(down);
            if (minecraft.level.getBlockState(candidate).getMaterial().isReplaceable()) {
                return candidate;
            }
        }
        return null;
    }
}
