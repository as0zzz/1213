package dev.mdk2.client.modules.combat;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.InventoryUtil;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import java.util.Collections;

public class AutoArmorModule extends Module {
    private final NumberSetting delay;
    private final BooleanSetting preferProtection;
    private final BooleanSetting ignoreBinding;
    private long lastEquipTime;

    public AutoArmorModule() {
        super("Auto Armor", "Equips the best armor pieces from inventory.", Category.COMBAT);
        this.delay = register(new NumberSetting("Delay", 150.0D, 0.0D, 1000.0D, 5.0D));
        this.preferProtection = register(new BooleanSetting("Prefer Protection", true));
        this.ignoreBinding = register(new BooleanSetting("Ignore Binding", true));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !InventoryUtil.canManipulate(minecraft.player) || !TimeUtil.passed(this.lastEquipTime, this.delay.getValue().doubleValue())) {
            return;
        }

        for (final EquipmentSlotType slotType : new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET}) {
            final ItemStack equipped = minecraft.player.getItemBySlot(slotType);
            if (this.ignoreBinding.getValue().booleanValue() && !equipped.isEmpty() && EnchantmentHelper.hasBindingCurse(equipped)) {
                continue;
            }

            double bestScore = getArmorScore(equipped, slotType);
            int bestSlot = -1;
            for (int slot = 0; slot < 36; slot++) {
                final ItemStack stack = minecraft.player.inventory.getItem(slot);
                final double score = getArmorScore(stack, slotType);
                if (score > bestScore + 0.05D) {
                    bestScore = score;
                    bestSlot = slot;
                }
            }

            if (bestSlot >= 0 && InventoryUtil.swapInventorySlots(minecraft.player, bestSlot, getArmorInventoryIndex(slotType))) {
                this.lastEquipTime = TimeUtil.now();
                return;
            }
        }
    }

    private double getArmorScore(final ItemStack stack, final EquipmentSlotType slotType) {
        if (stack.isEmpty() || !stack.canEquip(slotType, Minecraft.getInstance().player)) {
            return 0.0D;
        }

        if (slotType == EquipmentSlotType.CHEST && stack.getItem() instanceof ElytraItem) {
            return 4.0D;
        }

        if (!(stack.getItem() instanceof ArmorItem)) {
            return 0.0D;
        }

        final ArmorItem armorItem = (ArmorItem) stack.getItem();
        if (armorItem.getSlot() != slotType) {
            return 0.0D;
        }

        double score = armorItem.getDefense() * 6.0D + armorItem.getToughness() * 2.5D;
        if (this.preferProtection.getValue().booleanValue()) {
            score += EnchantmentHelper.getDamageProtection(Collections.singletonList(stack), DamageSource.GENERIC) * 1.7D;
        }
        if (stack.getMaxDamage() > 0) {
            score += (double) (stack.getMaxDamage() - stack.getDamageValue()) / (double) stack.getMaxDamage();
        }
        return score;
    }

    private int getArmorInventoryIndex(final EquipmentSlotType slotType) {
        switch (slotType) {
            case HEAD:
                return 39;
            case CHEST:
                return 38;
            case LEGS:
                return 37;
            case FEET:
            default:
                return 36;
        }
    }
}
