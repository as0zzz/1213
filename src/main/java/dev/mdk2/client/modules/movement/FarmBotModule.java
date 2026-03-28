package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.TimeUtil;
import dev.mdk2.client.util.world.WorldInteractionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class FarmBotModule extends Module {
    private final ModeSetting mode;
    private final NumberSetting range;
    private final NumberSetting delay;
    private final BooleanSetting fromInventory;
    private long lastActionTime;

    public FarmBotModule() {
        super("Farm Bot", "Automates farming, tree chopping and bonemeal usage.", Category.MOVEMENT);
        this.mode = register(new ModeSetting("Mode", "AutoFarm", "AutoFarm", "TreeBot", "BonemealAura"));
        this.range = register(new NumberSetting("Range", 4.0D, 1.0D, 6.0D, 0.5D));
        this.delay = register(new NumberSetting("Delay", 120.0D, 0.0D, 1000.0D, 10.0D));
        this.fromInventory = register(new BooleanSetting("From Inventory", true));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !TimeUtil.passed(this.lastActionTime, this.delay.getValue().doubleValue())) {
            return;
        }

        if ("TreeBot".equalsIgnoreCase(this.mode.getValue())) {
            final BlockPos logPos = WorldInteractionUtil.findNearestBlock(
                minecraft.level,
                minecraft.player.blockPosition(),
                this.range.getValue().intValue(),
                state -> WorldInteractionUtil.isTreeLog(state)
            );
            if (logPos != null && WorldInteractionUtil.breakBlock(minecraft, logPos)) {
                this.lastActionTime = TimeUtil.now();
            }
            return;
        }

        if ("BonemealAura".equalsIgnoreCase(this.mode.getValue())) {
            final BlockPos cropPos = WorldInteractionUtil.findNearestBlock(
                minecraft.level,
                minecraft.player.blockPosition(),
                this.range.getValue().intValue(),
                state -> !state.isAir() && !WorldInteractionUtil.isMatureCrop(state)
            );
            final int slot = WorldInteractionUtil.findBonemealSlot(minecraft.player, this.fromInventory.getValue().booleanValue());
            if (cropPos != null && slot >= 0 && dev.mdk2.client.util.combat.InventoryUtil.selectHotbarSlot(minecraft.player, slot)) {
                if (WorldInteractionUtil.useItemOn(minecraft, cropPos, net.minecraft.util.Direction.UP, Hand.MAIN_HAND)) {
                    this.lastActionTime = TimeUtil.now();
                }
            }
            return;
        }

        final BlockPos cropPos = WorldInteractionUtil.findNearestBlock(
            minecraft.level,
            minecraft.player.blockPosition(),
            this.range.getValue().intValue(),
            state -> WorldInteractionUtil.isMatureCrop(state)
        );
        if (cropPos != null && WorldInteractionUtil.breakBlock(minecraft, cropPos)) {
            this.lastActionTime = TimeUtil.now();
        }
    }
}
