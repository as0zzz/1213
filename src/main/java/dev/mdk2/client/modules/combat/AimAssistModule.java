package dev.mdk2.client.modules.combat;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.CombatUtil;
import dev.mdk2.client.util.combat.InventoryUtil;
import dev.mdk2.client.util.combat.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.vector.Vector3d;

public class AimAssistModule extends Module {
    private final ModeSetting mode;
    private final BooleanSetting players;
    private final BooleanSetting mobs;
    private final BooleanSetting animals;
    private final BooleanSetting invisibles;
    private final BooleanSetting ignoreTeams;
    private final BooleanSetting requireClick;
    private final BooleanSetting onlyWeapon;
    private final BooleanSetting projectileLead;
    private final NumberSetting range;
    private final NumberSetting fov;
    private final NumberSetting smooth;

    public AimAssistModule() {
        super("Aim Assist", "Smoothly aims at nearby targets and supports bow aimbot.", Category.COMBAT);
        this.mode = register(new ModeSetting("Mode", "AimAssist", "AimAssist", "BowAimbot"));
        this.players = register(new BooleanSetting("Players", true));
        this.mobs = register(new BooleanSetting("Mobs", true));
        this.animals = register(new BooleanSetting("Animals", false));
        this.invisibles = register(new BooleanSetting("Invisibles", false));
        this.ignoreTeams = register(new BooleanSetting("Ignore Teams", true));
        this.requireClick = register(new BooleanSetting("Require Click", true));
        this.onlyWeapon = register(new BooleanSetting("Only Weapon", true));
        this.projectileLead = register(new BooleanSetting("Projectile Lead", true));
        this.range = register(new NumberSetting("Range", 6.0D, 2.0D, 64.0D, 0.1D));
        this.fov = register(new NumberSetting("FOV", 110.0D, 10.0D, 360.0D, 1.0D));
        this.smooth = register(new NumberSetting("Smooth", 12.0D, 1.0D, 40.0D, 0.5D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        if ("BowAimbot".equalsIgnoreCase(this.mode.getValue())) {
            handleBowAimbot(minecraft);
            return;
        }

        if (this.requireClick.getValue().booleanValue() && !minecraft.options.keyAttack.isDown()) {
            return;
        }

        if (this.onlyWeapon.getValue().booleanValue() && InventoryUtil.getSwordScore(minecraft.player.getMainHandItem()) <= 0.0D) {
            return;
        }

        final LivingEntity target = CombatUtil.findBestTarget(minecraft.player, this.range.getValue().doubleValue(), this.range.getValue().doubleValue() * 0.7D,
            this.fov.getValue().doubleValue(), this.players.getValue().booleanValue(), this.mobs.getValue().booleanValue(),
            this.animals.getValue().booleanValue(), this.invisibles.getValue().booleanValue(), this.ignoreTeams.getValue().booleanValue(), "Angle");
        if (target != null) {
            RotationUtil.face(minecraft.player, target, this.smooth.getValue().floatValue(), this.smooth.getValue().floatValue() * 0.7F, false);
        }
    }

    private void handleBowAimbot(final Minecraft minecraft) {
        final ItemStack stack = minecraft.player.getUseItem();
        if (!minecraft.player.isUsingItem() || !isSupportedRangedWeapon(stack)) {
            return;
        }

        final LivingEntity target = CombatUtil.findBestTarget(minecraft.player, this.range.getValue().doubleValue(), this.range.getValue().doubleValue() * 0.7D,
            this.fov.getValue().doubleValue(), this.players.getValue().booleanValue(), this.mobs.getValue().booleanValue(),
            this.animals.getValue().booleanValue(), this.invisibles.getValue().booleanValue(), this.ignoreTeams.getValue().booleanValue(), "Angle");
        if (target == null) {
            return;
        }

        Vector3d aimPoint = RotationUtil.getTargetPoint(target);
        if (this.projectileLead.getValue().booleanValue()) {
            final double projectileSpeed = getProjectileSpeed(minecraft.player, stack);
            final double travelTime = projectileSpeed <= 0.0D ? 0.0D : minecraft.player.distanceTo(target) / projectileSpeed;
            aimPoint = aimPoint.add(target.getDeltaMovement().scale(travelTime));
        }

        RotationUtil.face(minecraft.player, aimPoint, this.smooth.getValue().floatValue(), this.smooth.getValue().floatValue(), false);
    }

    private boolean isSupportedRangedWeapon(final ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof TridentItem;
    }

    private double getProjectileSpeed(final net.minecraft.entity.player.PlayerEntity player, final ItemStack stack) {
        if (stack.getItem() instanceof BowItem) {
            final int useTicks = stack.getUseDuration() - player.getUseItemRemainingTicks();
            return Math.max(0.1D, BowItem.getPowerForTime(useTicks) * 3.0D);
        }
        if (stack.getItem() instanceof CrossbowItem) {
            return 3.15D;
        }
        if (stack.getItem() instanceof TridentItem) {
            return 2.5D;
        }
        return 1.5D;
    }
}
