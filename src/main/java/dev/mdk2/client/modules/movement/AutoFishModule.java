package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.InventoryUtil;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

public class AutoFishModule extends Module {
    private final BooleanSetting fromInventory;
    private final BooleanSetting autoCast;
    private final NumberSetting castDelay;
    private final NumberSetting reelDelay;
    private final NumberSetting biteVertical;
    private final NumberSetting biteHorizontal;
    private long lastActionTime;

    public AutoFishModule() {
        super("Auto Fish", "Automatically casts and reels fishing rods when the bobber bites.", Category.MOVEMENT);
        this.fromInventory = register(new BooleanSetting("From Inventory", true));
        this.autoCast = register(new BooleanSetting("Auto Cast", true));
        this.castDelay = register(new NumberSetting("Cast Delay", 550.0D, 0.0D, 3000.0D, 25.0D));
        this.reelDelay = register(new NumberSetting("Reel Delay", 250.0D, 0.0D, 1500.0D, 25.0D));
        this.biteVertical = register(new NumberSetting("Bite Vertical", 0.04D, 0.01D, 0.25D, 0.01D));
        this.biteHorizontal = register(new NumberSetting("Bite Horizontal", 0.02D, 0.0D, 0.2D, 0.005D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null || minecraft.screen != null) {
            return;
        }

        final java.util.function.Predicate<ItemStack> predicate = stack -> !stack.isEmpty() && stack.getItem() instanceof FishingRodItem;
        final int slot = this.fromInventory.getValue().booleanValue()
            ? InventoryUtil.ensureHotbarItem(minecraft.player, predicate, minecraft.player.inventory.selected)
            : InventoryUtil.findHotbarSlot(minecraft.player, predicate);
        if (slot < 0 || !InventoryUtil.selectHotbarSlot(minecraft.player, slot)) {
            return;
        }

        if (minecraft.player.fishing != null) {
            if (shouldReel(minecraft) && TimeUtil.passed(this.lastActionTime, this.reelDelay.getValue().doubleValue())) {
                minecraft.gameMode.useItem(minecraft.player, minecraft.level, Hand.MAIN_HAND);
                this.lastActionTime = TimeUtil.now();
            }
            return;
        }

        if (this.autoCast.getValue().booleanValue() && TimeUtil.passed(this.lastActionTime, this.castDelay.getValue().doubleValue())) {
            minecraft.gameMode.useItem(minecraft.player, minecraft.level, Hand.MAIN_HAND);
            this.lastActionTime = TimeUtil.now();
        }
    }

    private boolean shouldReel(final Minecraft minecraft) {
        if (minecraft.player == null || minecraft.player.fishing == null) {
            return false;
        }

        final Vector3d motion = minecraft.player.fishing.getDeltaMovement();
        final double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        return minecraft.player.fishing.tickCount > 10
            && (motion.y <= -this.biteVertical.getValue().doubleValue()
            || horizontal >= this.biteHorizontal.getValue().doubleValue());
    }
}
