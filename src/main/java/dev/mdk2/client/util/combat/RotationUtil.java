package dev.mdk2.client.util.combat;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public final class RotationUtil {
    private RotationUtil() {
    }

    public static float[] getRotations(final Vector3d from, final Vector3d to) {
        final double diffX = to.x - from.x;
        final double diffY = to.y - from.y;
        final double diffZ = to.z - from.z;
        final double horizontal = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        final float pitch = (float) -Math.toDegrees(Math.atan2(diffY, horizontal));
        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90.0F, 90.0F)};
    }

    public static Vector3d getEyePosition(final PlayerEntity player) {
        return new Vector3d(player.getX(), player.getEyeY(), player.getZ());
    }

    public static Vector3d getTargetPoint(final Entity entity) {
        if (entity instanceof LivingEntity) {
            return new Vector3d(entity.getX(), ((LivingEntity) entity).getEyeY() - entity.getBbHeight() * 0.14D, entity.getZ());
        }
        return entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    public static float getYawDelta(final PlayerEntity player, final Entity entity) {
        final float[] rotations = getRotations(getEyePosition(player), getTargetPoint(entity));
        return Math.abs(MathHelper.wrapDegrees(rotations[0] - player.yRot));
    }

    public static boolean isInFov(final PlayerEntity player, final Entity entity, final double fov) {
        return fov >= 360.0D || getYawDelta(player, entity) <= fov * 0.5D;
    }

    public static void face(final PlayerEntity player, final Entity entity, final float yawStep, final float pitchStep, final boolean snap) {
        face(player, getTargetPoint(entity), yawStep, pitchStep, snap);
    }

    public static void face(final PlayerEntity player, final Vector3d target, final float yawStep, final float pitchStep, final boolean snap) {
        final float[] rotations = getRotations(getEyePosition(player), target);
        final float yaw = snap ? rotations[0] : approachAngle(player.yRot, rotations[0], yawStep);
        final float pitch = snap ? rotations[1] : approach(player.xRot, rotations[1], pitchStep);
        apply(player, yaw, pitch);
    }

    public static void apply(final PlayerEntity player, final float yaw, final float pitch) {
        player.yRot = yaw;
        player.yHeadRot = yaw;
        player.yBodyRot = yaw;
        player.yRotO = yaw;
        player.xRot = pitch;
        player.xRotO = pitch;
        if (player instanceof ClientPlayerEntity) {
            ((ClientPlayerEntity) player).connection.send(new CPlayerPacket.RotationPacket(yaw, pitch, player.isOnGround()));
        }
    }

    private static float approachAngle(final float current, final float target, final float step) {
        return current + MathHelper.clamp(MathHelper.wrapDegrees(target - current), -step, step);
    }

    private static float approach(final float current, final float target, final float step) {
        return current + MathHelper.clamp(target - current, -step, step);
    }
}
