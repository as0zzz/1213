package dev.mdk2.client.modules.combat;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.InventoryUtil;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class AutoTotemModule extends Module {
    private final NumberSetting health;
    private final NumberSetting crystalRange;
    private final BooleanSetting alwaysHold;
    private final BooleanSetting crystalCheck;
    private final NumberSetting delay;
    private long lastSwapTime;

    public AutoTotemModule() {
        super("Auto Totem", "Moves a totem into offhand when danger is detected.", Category.COMBAT);
        this.health = register(new NumberSetting("Health", 12.0D, 1.0D, 36.0D, 0.5D));
        this.crystalRange = register(new NumberSetting("Crystal Range", 7.0D, 1.0D, 16.0D, 0.1D));
        this.alwaysHold = register(new BooleanSetting("Always Hold", false));
        this.crystalCheck = register(new BooleanSetting("Crystal Check", true));
        this.delay = register(new NumberSetting("Delay", 120.0D, 0.0D, 800.0D, 5.0D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || InventoryUtil.hasTotemInOffhand(minecraft.player)
            || !InventoryUtil.canManipulate(minecraft.player) || !TimeUtil.passed(this.lastSwapTime, this.delay.getValue().doubleValue())) {
            return;
        }

        final boolean lowHealth = minecraft.player.getHealth() + minecraft.player.getAbsorptionAmount() <= this.health.getValue().doubleValue();
        if (!this.alwaysHold.getValue().booleanValue() && !lowHealth && !(this.crystalCheck.getValue().booleanValue() && hasNearbyCrystal())) {
            return;
        }

        final int totemSlot = InventoryUtil.findInventorySlot(minecraft.player, new java.util.function.Predicate<ItemStack>() {
            @Override
            public boolean test(final ItemStack stack) {
                return stack.getItem() == Items.TOTEM_OF_UNDYING;
            }
        });
        if (totemSlot >= 0 && InventoryUtil.moveToOffhand(minecraft.player, totemSlot)) {
            this.lastSwapTime = TimeUtil.now();
        }
    }

    private boolean hasNearbyCrystal() {
        final Minecraft minecraft = Minecraft.getInstance();
        final double rangeSq = this.crystalRange.getValue().doubleValue() * this.crystalRange.getValue().doubleValue();
        for (final Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof EnderCrystalEntity && entity.isAlive() && minecraft.player.distanceToSqr(entity) <= rangeSq) {
                return true;
            }
        }
        return false;
    }
}
