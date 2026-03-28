package dev.mdk2.client.util.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class CombatUtil {
    private CombatUtil() {
    }

    public static LivingEntity findBestTarget(final PlayerEntity player, final double range, final double wallRange, final double fov,
                                              final boolean players, final boolean mobs, final boolean animals, final boolean invisibles,
                                              final boolean ignoreTeams, final String priority) {
        final List<LivingEntity> targets = collectTargets(player, range, wallRange, fov, players, mobs, animals, invisibles, ignoreTeams, priority);
        return targets.isEmpty() ? null : targets.get(0);
    }

    public static List<LivingEntity> collectTargets(final PlayerEntity player, final double range, final double wallRange, final double fov,
                                                    final boolean players, final boolean mobs, final boolean animals, final boolean invisibles,
                                                    final boolean ignoreTeams, final String priority) {
        if (player == null || player.level == null) {
            return Collections.emptyList();
        }

        final List<LivingEntity> targets = new ArrayList<LivingEntity>();
        for (final Entity entity : player.level.getEntities(player, player.getBoundingBox().inflate(range + 2.0D))) {
            if (entity instanceof LivingEntity && isValidTarget(player, (LivingEntity) entity, range, wallRange, fov, players, mobs, animals, invisibles, ignoreTeams)) {
                targets.add((LivingEntity) entity);
            }
        }

        final Comparator<LivingEntity> comparator = createComparator(player, priority);
        if (comparator != null) {
            Collections.sort(targets, comparator);
        }
        return targets;
    }

    public static boolean isValidTarget(final PlayerEntity player, final LivingEntity entity, final double range, final double wallRange,
                                        final double fov, final boolean players, final boolean mobs, final boolean animals,
                                        final boolean invisibles, final boolean ignoreTeams) {
        if (entity == null || entity == player || !entity.isAlive() || entity.isSpectator()) {
            return false;
        }

        if (!invisibles && entity.isInvisible()) {
            return false;
        }

        if (ignoreTeams && player.isAlliedTo(entity)) {
            return false;
        }

        final double distanceSq = player.distanceToSqr(entity);
        if (distanceSq > range * range) {
            return false;
        }

        if (!hasLineOfSight(player, entity) && distanceSq > wallRange * wallRange) {
            return false;
        }

        if (!RotationUtil.isInFov(player, entity, fov)) {
            return false;
        }

        if (entity instanceof PlayerEntity) {
            return players;
        }

        if (isAnimal(entity)) {
            return animals;
        }

        return mobs && entity instanceof net.minecraft.entity.MobEntity;
    }

    public static boolean canAttack(final PlayerEntity player, final boolean useCooldown, final double cooldownThreshold) {
        return player != null && (!useCooldown || player.getAttackStrengthScale(0.0F) >= cooldownThreshold);
    }

    public static void attack(final Entity target, final boolean swing, final boolean keepSprint) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gameMode == null || target == null) {
            return;
        }

        final boolean sprinting = minecraft.player.isSprinting();
        minecraft.gameMode.attack(minecraft.player, target);
        if (swing) {
            minecraft.player.swing(Hand.MAIN_HAND);
        }
        if (keepSprint && sprinting) {
            minecraft.player.setSprinting(true);
        }
    }

    public static ActionResultType useItem(final Hand hand) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.gameMode == null) {
            return ActionResultType.PASS;
        }

        return minecraft.gameMode.useItem(minecraft.player, minecraft.level, hand);
    }

    public static ActionResultType useItemOnBlock(final BlockPos pos, final Direction face, final Hand hand) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !(minecraft.level instanceof ClientWorld) || minecraft.gameMode == null) {
            return ActionResultType.PASS;
        }

        final Vector3d hit = new Vector3d(
            pos.getX() + 0.5D + face.getStepX() * 0.5D,
            pos.getY() + 0.5D + face.getStepY() * 0.5D,
            pos.getZ() + 0.5D + face.getStepZ() * 0.5D
        );
        return minecraft.gameMode.useItemOn(minecraft.player, (ClientWorld) minecraft.level, hand, new BlockRayTraceResult(hit, face, pos, false));
    }

    public static EnderCrystalEntity findNearestCrystal(final PlayerEntity player, final double range, final LivingEntity anchorTarget) {
        EnderCrystalEntity bestCrystal = null;
        double bestDistance = range * range;
        final AxisAlignedBB searchBox = player.getBoundingBox().inflate(range + 2.0D);
        for (final Entity entity : player.level.getEntities(player, searchBox)) {
            if (!(entity instanceof EnderCrystalEntity) || !entity.isAlive()) {
                continue;
            }

            final double distance = player.distanceToSqr(entity);
            if (distance > bestDistance) {
                continue;
            }

            if (anchorTarget != null && anchorTarget.distanceToSqr(entity) > range * range) {
                continue;
            }

            bestCrystal = (EnderCrystalEntity) entity;
            bestDistance = distance;
        }
        return bestCrystal;
    }

    public static boolean hasLineOfSight(final PlayerEntity player, final Entity entity) {
        if (player == null || entity == null || player.level == null) {
            return false;
        }

        final Vector3d start = RotationUtil.getEyePosition(player);
        final Vector3d[] points = new Vector3d[]{
            RotationUtil.getTargetPoint(entity),
            entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D),
            entity.position()
        };
        for (final Vector3d point : points) {
            final RayTraceResult trace = player.level.clip(new RayTraceContext(start, point,
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player));
            if (trace.getType() == RayTraceResult.Type.MISS) {
                return true;
            }
        }
        return false;
    }

    public static double estimateExplosionDamage(final LivingEntity entity, final Vector3d source, final double radius) {
        final double distance = Math.sqrt(entity.distanceToSqr(source)) / radius;
        if (distance > 1.0D) {
            return 0.0D;
        }

        final RayTraceResult trace = entity.level.clip(new RayTraceContext(source, new Vector3d(entity.getX(), entity.getEyeY(), entity.getZ()),
            RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
        final double exposure = trace.getType() == RayTraceResult.Type.MISS ? 1.0D : 0.65D;
        final double impact = (1.0D - distance) * exposure;
        double damage = (impact * impact + impact) * 0.5D * 7.0D * radius + 1.0D;
        damage *= Math.max(0.2D, 1.0D - Math.min(0.75D, entity.getArmorValue() * 0.03D));
        return damage;
    }

    private static Comparator<LivingEntity> createComparator(final PlayerEntity player, final String priority) {
        if ("Health".equalsIgnoreCase(priority)) {
            return new Comparator<LivingEntity>() {
                @Override
                public int compare(final LivingEntity first, final LivingEntity second) {
                    return Float.compare(first.getHealth() + first.getAbsorptionAmount(), second.getHealth() + second.getAbsorptionAmount());
                }
            };
        }

        if ("Angle".equalsIgnoreCase(priority)) {
            return new Comparator<LivingEntity>() {
                @Override
                public int compare(final LivingEntity first, final LivingEntity second) {
                    return Float.compare(RotationUtil.getYawDelta(player, first), RotationUtil.getYawDelta(player, second));
                }
            };
        }

        return new Comparator<LivingEntity>() {
            @Override
            public int compare(final LivingEntity first, final LivingEntity second) {
                return Double.compare(player.distanceToSqr(first), player.distanceToSqr(second));
            }
        };
    }

    private static boolean isAnimal(final LivingEntity entity) {
        return entity instanceof AnimalEntity
            || entity instanceof WaterMobEntity
            || entity instanceof AbstractHorseEntity
            || entity instanceof GolemEntity && !(entity instanceof MonsterEntity);
    }
}
