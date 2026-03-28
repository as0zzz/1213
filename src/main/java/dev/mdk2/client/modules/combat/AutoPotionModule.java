package dev.mdk2.client.modules.combat;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.CombatUtil;
import dev.mdk2.client.util.combat.InventoryUtil;
import dev.mdk2.client.util.combat.RotationUtil;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Hand;

import java.util.List;

public class AutoPotionModule extends Module {
    private final ModeSetting mode;
    private final BooleanSetting fromInventory;
    private final BooleanSetting useBuffs;
    private final NumberSetting health;
    private final NumberSetting delay;
    private long lastUseTime;

    public AutoPotionModule() {
        super("Auto Potion", "Uses splash potions or soups when health drops.", Category.COMBAT);
        this.mode = register(new ModeSetting("Mode", "Potion", "Potion", "Soup"));
        this.fromInventory = register(new BooleanSetting("From Inventory", true));
        this.useBuffs = register(new BooleanSetting("Use Buffs", true));
        this.health = register(new NumberSetting("Health", 12.0D, 1.0D, 36.0D, 0.5D));
        this.delay = register(new NumberSetting("Delay", 300.0D, 0.0D, 2000.0D, 10.0D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.player.isUsingItem()
            || !TimeUtil.passed(this.lastUseTime, this.delay.getValue().doubleValue())) {
            return;
        }

        if ("Soup".equalsIgnoreCase(this.mode.getValue())) {
            useSoup(minecraft);
            return;
        }

        usePotion(minecraft);
    }

    private void useSoup(final Minecraft minecraft) {
        if (minecraft.player.getHealth() + minecraft.player.getAbsorptionAmount() > this.health.getValue().doubleValue()) {
            return;
        }

        final java.util.function.Predicate<ItemStack> predicate = new java.util.function.Predicate<ItemStack>() {
            @Override
            public boolean test(final ItemStack stack) {
                return isSoup(stack);
            }
        };
        final int slot = this.fromInventory.getValue().booleanValue()
            ? InventoryUtil.ensureHotbarItem(minecraft.player, predicate, minecraft.player.inventory.selected)
            : InventoryUtil.findHotbarSlot(minecraft.player, predicate);
        if (slot >= 0 && InventoryUtil.selectHotbarSlot(minecraft.player, slot)) {
            CombatUtil.useItem(Hand.MAIN_HAND);
            this.lastUseTime = TimeUtil.now();
        }
    }

    private void usePotion(final Minecraft minecraft) {
        final boolean needsHealing = minecraft.player.getHealth() + minecraft.player.getAbsorptionAmount() <= this.health.getValue().doubleValue();
        if (!needsHealing && !this.useBuffs.getValue().booleanValue()) {
            return;
        }

        final int slot = findPotionSlot(minecraft.player, needsHealing);
        if (slot < 0 || !InventoryUtil.selectHotbarSlot(minecraft.player, slot)) {
            return;
        }

        final float previousPitch = minecraft.player.xRot;
        RotationUtil.apply(minecraft.player, minecraft.player.yRot, 87.0F);
        CombatUtil.useItem(Hand.MAIN_HAND);
        RotationUtil.apply(minecraft.player, minecraft.player.yRot, previousPitch);
        this.lastUseTime = TimeUtil.now();
    }

    private int findPotionSlot(final PlayerEntity player, final boolean needsHealing) {
        final java.util.function.Predicate<ItemStack> predicate = new java.util.function.Predicate<ItemStack>() {
            @Override
            public boolean test(final ItemStack stack) {
                return isUsefulPotion(player, stack, needsHealing);
            }
        };
        if (this.fromInventory.getValue().booleanValue()) {
            return InventoryUtil.ensureHotbarItem(player, predicate, player.inventory.selected);
        }
        return InventoryUtil.findHotbarSlot(player, predicate);
    }

    private boolean isUsefulPotion(final PlayerEntity player, final ItemStack stack, final boolean needsHealing) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ThrowablePotionItem)) {
            return false;
        }

        final List<EffectInstance> effects = PotionUtils.getMobEffects(stack);
        for (final EffectInstance effect : effects) {
            final Effect type = effect.getEffect();
            if (needsHealing && (type == Effects.HEAL || type == Effects.REGENERATION)) {
                return true;
            }

            if (this.useBuffs.getValue().booleanValue()
                && (type == Effects.MOVEMENT_SPEED || type == Effects.DAMAGE_BOOST || type == Effects.FIRE_RESISTANCE)
                && !player.hasEffect(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSoup(final ItemStack stack) {
        return stack.getItem() == Items.MUSHROOM_STEW
            || stack.getItem() == Items.BEETROOT_SOUP
            || stack.getItem() == Items.RABBIT_STEW
            || stack.getItem() == Items.SUSPICIOUS_STEW;
    }
}
