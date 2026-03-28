package dev.mdk2.client.modules.misc;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.InventoryUtil;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;

public class AutoDropModule extends Module {
    private final BooleanSetting junkOnly;
    private final BooleanSetting includeHotbar;
    private final BooleanSetting keepBlocks;
    private final BooleanSetting keepFood;
    private final NumberSetting delay;
    private long lastDropTime;

    public AutoDropModule() {
        super("Auto Drop", "Drops junk items out of your inventory with safer filters.", Category.MISC);
        this.junkOnly = register(new BooleanSetting("Junk Only", true));
        this.includeHotbar = register(new BooleanSetting("Include Hotbar", false));
        this.keepBlocks = register(new BooleanSetting("Keep Blocks", true));
        this.keepFood = register(new BooleanSetting("Keep Food", true));
        this.delay = register(new NumberSetting("Delay", 120.0D, 0.0D, 1000.0D, 10.0D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gameMode == null || !TimeUtil.passed(this.lastDropTime, this.delay.getValue().doubleValue())) {
            return;
        }

        final int start = this.includeHotbar.getValue().booleanValue() ? 0 : 9;
        for (int slot = start; slot < 36; slot++) {
            final ItemStack stack = minecraft.player.inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (this.junkOnly.getValue().booleanValue() && !isJunk(stack)) {
                continue;
            }

            final int containerSlot = InventoryUtil.findContainerSlot(minecraft.player, slot);
            if (containerSlot >= 0) {
                minecraft.gameMode.handleInventoryMouseClick(
                    minecraft.player.containerMenu.containerId,
                    containerSlot,
                    1,
                    ClickType.THROW,
                    minecraft.player
                );
                this.lastDropTime = TimeUtil.now();
                return;
            }
        }
    }

    private boolean isJunk(final ItemStack stack) {
        final Item item = stack.getItem();
        if (item instanceof ArmorItem || item instanceof SwordItem || item instanceof ToolItem || item instanceof BucketItem) {
            return false;
        }
        if (this.keepBlocks.getValue().booleanValue() && item instanceof BlockItem) {
            return false;
        }

        final Food food = item.getFoodProperties();
        if (this.keepFood.getValue().booleanValue() && food != null) {
            return false;
        }

        return item == Items.ROTTEN_FLESH
            || item == Items.STRING
            || item == Items.SPIDER_EYE
            || item == Items.POISONOUS_POTATO
            || item == Items.BONE
            || item == Items.WHEAT_SEEDS
            || item == Items.FLINT
            || item == Items.LEATHER
            || item == Items.DEAD_BUSH
            || item == Items.STICK
            || item == Items.FEATHER
            || item == Items.EGG
            || item == Items.GUNPOWDER && stack.getCount() <= 2;
    }
}
