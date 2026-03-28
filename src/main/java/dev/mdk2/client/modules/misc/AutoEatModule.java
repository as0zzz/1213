package dev.mdk2.client.modules.misc;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.InventoryUtil;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoEatModule extends Module {
    private final NumberSetting hunger;
    private final NumberSetting health;
    private final NumberSetting delay;
    private final BooleanSetting fromInventory;
    private long lastUseTime;

    public AutoEatModule() {
        super("Auto Eat", "Automatically uses food when you are hungry or low.", Category.MISC);
        this.hunger = register(new NumberSetting("Hunger", 14.0D, 1.0D, 20.0D, 1.0D));
        this.health = register(new NumberSetting("Health", 12.0D, 1.0D, 36.0D, 0.5D));
        this.delay = register(new NumberSetting("Delay", 350.0D, 0.0D, 2000.0D, 25.0D));
        this.fromInventory = register(new BooleanSetting("From Inventory", true));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.player.isUsingItem()
            || !TimeUtil.passed(this.lastUseTime, this.delay.getValue().doubleValue())) {
            return;
        }

        final boolean needFood = minecraft.player.getFoodData().getFoodLevel() <= this.hunger.getValue().intValue();
        final boolean needHealing = minecraft.player.getHealth() + minecraft.player.getAbsorptionAmount() <= this.health.getValue().doubleValue();
        if (!needFood && !needHealing) {
            return;
        }

        final java.util.function.Predicate<ItemStack> predicate = stack -> !stack.isEmpty() && stack.isEdible();
        final int slot = this.fromInventory.getValue().booleanValue()
            ? InventoryUtil.ensureHotbarItem(minecraft.player, predicate, minecraft.player.inventory.selected)
            : InventoryUtil.findHotbarSlot(minecraft.player, predicate);
        if (slot >= 0 && InventoryUtil.selectHotbarSlot(minecraft.player, slot)) {
            minecraft.gameMode.useItem(minecraft.player, minecraft.level, Hand.MAIN_HAND);
            this.lastUseTime = TimeUtil.now();
        }
    }
}
