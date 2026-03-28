package dev.mdk2.client.util.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public final class MovementUtil {
    private MovementUtil() {
    }

    public static boolean isMoving(final Minecraft minecraft) {
        return Math.abs(getForwardInput(minecraft)) > 1.0E-3D || Math.abs(getStrafeInput(minecraft)) > 1.0E-3D;
    }

    public static double getForwardInput(final Minecraft minecraft) {
        double forward = 0.0D;
        if (minecraft.options.keyUp.isDown()) {
            forward += 1.0D;
        }
        if (minecraft.options.keyDown.isDown()) {
            forward -= 1.0D;
        }
        return forward;
    }

    public static double getStrafeInput(final Minecraft minecraft) {
        double strafe = 0.0D;
        if (minecraft.options.keyLeft.isDown()) {
            strafe += 1.0D;
        }
        if (minecraft.options.keyRight.isDown()) {
            strafe -= 1.0D;
        }
        return strafe;
    }

    public static Vector3d getHorizontalMotion(final float yawDegrees, final double forward, final double strafe, final double speed) {
        double adjustedForward = forward;
        double adjustedStrafe = strafe;
        float yaw = yawDegrees;

        if (adjustedForward != 0.0D) {
            if (adjustedStrafe > 0.0D) {
                yaw += adjustedForward > 0.0D ? -45.0F : 45.0F;
            } else if (adjustedStrafe < 0.0D) {
                yaw += adjustedForward > 0.0D ? 45.0F : -45.0F;
            }
            adjustedStrafe = 0.0D;
            adjustedForward = adjustedForward > 0.0D ? 1.0D : -1.0D;
        }

        if (adjustedStrafe != 0.0D) {
            adjustedStrafe = adjustedStrafe > 0.0D ? 1.0D : -1.0D;
        }

        final double radians = Math.toRadians(yaw + 90.0F);
        final double sin = Math.sin(radians);
        final double cos = Math.cos(radians);
        return new Vector3d(
            adjustedForward * speed * cos + adjustedStrafe * speed * sin,
            0.0D,
            adjustedForward * speed * sin - adjustedStrafe * speed * cos
        );
    }

    public static void applyHorizontalMotion(final PlayerEntity player, final Minecraft minecraft, final double speed) {
        final Vector3d horizontal = getHorizontalMotion(player.yRot, getForwardInput(minecraft), getStrafeInput(minecraft), speed);
        player.setDeltaMovement(horizontal.x, player.getDeltaMovement().y, horizontal.z);
    }

    public static void applyFlightMotion(final PlayerEntity player, final Minecraft minecraft,
                                         final double horizontalSpeed, final double verticalSpeed) {
        final Vector3d horizontal = getHorizontalMotion(player.yRot, getForwardInput(minecraft), getStrafeInput(minecraft), horizontalSpeed);
        double vertical = 0.0D;
        if (minecraft.options.keyJump.isDown()) {
            vertical += verticalSpeed;
        }
        if (minecraft.options.keyShift.isDown()) {
            vertical -= verticalSpeed;
        }
        player.setDeltaMovement(horizontal.x, vertical, horizontal.z);
    }

    public static float getMoveYaw(final PlayerEntity player, final Minecraft minecraft) {
        final double forward = getForwardInput(minecraft);
        final double strafe = getStrafeInput(minecraft);
        if (Math.abs(forward) < 1.0E-3D && Math.abs(strafe) < 1.0E-3D) {
            return player.yRot;
        }

        float yaw = player.yRot;
        if (forward < 0.0D) {
            yaw += 180.0F;
        }

        float factor = 1.0F;
        if (forward < 0.0D) {
            factor = -0.5F;
        } else if (forward > 0.0D) {
            factor = 0.5F;
        }

        if (strafe > 0.0D) {
            yaw -= 90.0F * factor;
        }
        if (strafe < 0.0D) {
            yaw += 90.0F * factor;
        }
        return yaw;
    }

    public static boolean hasSolidGround(final PlayerEntity player, final double offset) {
        final BlockPos below = new BlockPos(
            MathHelper.floor(player.getX()),
            MathHelper.floor(player.getY() - offset),
            MathHelper.floor(player.getZ())
        );
        return !player.level.getBlockState(below).isAir();
    }

    public static Vector3d clampHorizontal(final Vector3d motion, final double maxSpeed) {
        final double horizontalLength = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontalLength <= maxSpeed || horizontalLength < 1.0E-4D) {
            return motion;
        }

        final double scale = maxSpeed / horizontalLength;
        return new Vector3d(motion.x * scale, motion.y, motion.z * scale);
    }
}
