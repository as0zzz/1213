package dev.mdk2.client.modules.combat;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.CombatUtil;
import dev.mdk2.client.util.combat.InventoryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;

public class ProtectModule extends Module {
    private final BooleanSetting players;
    private final BooleanSetting mobs;
    private final BooleanSetting projectiles;
    private final BooleanSetting autoSwapShield;
    private final NumberSetting meleeRange;
    private final NumberSetting projectileRange;
    private final NumberSetting health;

    public ProtectModule() {
        super("Protect", "Automatically raises a shield against nearby threats.", Category.COMBAT);
        this.players = register(new BooleanSetting("Players", true));
        this.mobs = register(new BooleanSetting("Mobs", true));
        this.projectiles = register(new BooleanSetting("Projectiles", true));
        this.autoSwapShield = register(new BooleanSetting("Auto Swap Shield", true));
        this.meleeRange = register(new NumberSetting("Melee Range", 5.5D, 1.0D, 12.0D, 0.1D));
        this.projectileRange = register(new NumberSetting("Projectile Range", 7.0D, 1.0D, 20.0D, 0.1D));
        this.health = register(new NumberSetting("Health", 10.0D, 1.0D, 36.0D, 0.5D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null) {
            return;
        }

        if (!InventoryUtil.hasShieldInOffhand(minecraft.player) && this.autoSwapShield.getValue().booleanValue() && InventoryUtil.canManipulate(minecraft.player)) {
            final int shieldSlot = InventoryUtil.findInventorySlot(minecraft.player, new java.util.function.Predicate<ItemStack>() {
                @Override
                public boolean test(final ItemStack stack) {
                    return stack.getItem() instanceof ShieldItem;
                }
            });
            if (shieldSlot >= 0) {
                InventoryUtil.moveToOffhand(minecraft.player, shieldSlot);
            }
        }

        final boolean threat = hasMeleeThreat(minecraft)
            || this.projectiles.getValue().booleanValue() && hasProjectileThreat(minecraft)
            || minecraft.player.getHealth() + minecraft.player.getAbsorptionAmount() <= this.health.getValue().doubleValue();

        if (threat && InventoryUtil.hasShieldInOffhand(minecraft.player)) {
            if (!minecraft.player.isUsingItem()) {
                CombatUtil.useItem(Hand.OFF_HAND);
            }
            return;
        }

        if (minecraft.player.isUsingItem() && minecraft.player.getUsedItemHand() == Hand.OFF_HAND
            && minecraft.player.getOffhandItem().getItem() instanceof ShieldItem) {
            minecraft.gameMode.releaseUsingItem(minecraft.player);
        }
    }

    private boolean hasMeleeThreat(final Minecraft minecraft) {
        return CombatUtil.findBestTarget(minecraft.player, this.meleeRange.getValue().doubleValue(), this.meleeRange.getValue().doubleValue() * 0.6D, 360.0D,
            this.players.getValue().booleanValue(), this.mobs.getValue().booleanValue(), false, false, true, "Distance") != null;
    }

    private boolean hasProjectileThreat(final Minecraft minecraft) {
        final double rangeSq = this.projectileRange.getValue().doubleValue() * this.projectileRange.getValue().doubleValue();
        for (final Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof ProjectileEntity) || !entity.isAlive() || minecraft.player.distanceToSqr(entity) > rangeSq) {
                continue;
            }
            return true;
        }
        return false;
    }
}
