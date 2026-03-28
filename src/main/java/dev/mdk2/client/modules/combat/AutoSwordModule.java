package dev.mdk2.client.modules.combat;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.CombatUtil;
import dev.mdk2.client.util.combat.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class AutoSwordModule extends Module {
    private final BooleanSetting onlyWithTarget;
    private final BooleanSetting swapFromInventory;
    private final BooleanSetting players;
    private final BooleanSetting mobs;
    private final BooleanSetting animals;
    private final NumberSetting range;

    public AutoSwordModule() {
        super("Auto Sword", "Selects the best sword when combat starts.", Category.COMBAT);
        this.onlyWithTarget = register(new BooleanSetting("Only Target", true));
        this.swapFromInventory = register(new BooleanSetting("Swap From Inventory", true));
        this.players = register(new BooleanSetting("Players", true));
        this.mobs = register(new BooleanSetting("Mobs", true));
        this.animals = register(new BooleanSetting("Animals", false));
        this.range = register(new NumberSetting("Range", 6.0D, 2.0D, 12.0D, 0.1D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        if (this.onlyWithTarget.getValue().booleanValue()) {
            final LivingEntity target = CombatUtil.findBestTarget(minecraft.player, this.range.getValue().doubleValue(),
                this.range.getValue().doubleValue() * 0.65D, 180.0D, this.players.getValue().booleanValue(),
                this.mobs.getValue().booleanValue(), this.animals.getValue().booleanValue(), false, true, "Distance");
            if (target == null) {
                return;
            }
        }

        equipBestSword();
    }

    public boolean equipBestSword() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }

        final int swordSlot = InventoryUtil.findBestSwordSlot(minecraft.player, !this.swapFromInventory.getValue().booleanValue());
        if (swordSlot < 0) {
            return false;
        }

        int hotbarSlot = swordSlot;
        if (swordSlot > 8) {
            hotbarSlot = InventoryUtil.ensureHotbarItem(minecraft.player, new java.util.function.Predicate<ItemStack>() {
                @Override
                public boolean test(final ItemStack stack) {
                    return InventoryUtil.getSwordScore(stack) > 0.0D;
                }
            }, minecraft.player.inventory.selected);
        }

        return hotbarSlot >= 0 && InventoryUtil.selectHotbarSlot(minecraft.player, hotbarSlot);
    }
}
