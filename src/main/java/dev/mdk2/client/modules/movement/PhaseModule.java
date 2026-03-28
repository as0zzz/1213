package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.movement.MovementUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class PhaseModule extends Module {
    private final ModeSetting mode;
    private final NumberSetting speed;
    private final NumberSetting vertical;
    private final NumberSetting hitboxScale;
    private final NumberSetting clipDistance;
    private double baseWidth;
    private double baseHeight;

    public PhaseModule() {
        super("Phase", "NoClip, wall phase and hitbox shrink modes.", Category.MOVEMENT);
        this.mode = register(new ModeSetting("Mode", "Phase", "Phase", "NoClip", "Hitbox"));
        this.speed = register(new NumberSetting("Speed", 0.32D, 0.05D, 1.5D, 0.01D));
        this.vertical = register(new NumberSetting("Vertical", 0.32D, 0.05D, 1.5D, 0.01D));
        this.hitboxScale = register(new NumberSetting("Hitbox Scale", 1.75D, 1.00D, 4.00D, 0.05D));
        this.clipDistance = register(new NumberSetting("Clip Distance", 0.18D, 0.02D, 0.8D, 0.01D));
    }

    @Override
    public void onEnable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            this.baseWidth = minecraft.player.getBbWidth();
            this.baseHeight = minecraft.player.getBbHeight();
        }
    }

    @Override
    public void onDisable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.noPhysics = false;
            minecraft.player.setNoGravity(false);
            minecraft.player.refreshDimensions();
        }
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        if (!"Hitbox".equalsIgnoreCase(this.mode.getValue())) {
            minecraft.player.refreshDimensions();
        }

        final String modeName = this.mode.getValue();
        if ("Hitbox".equalsIgnoreCase(modeName)) {
            minecraft.player.noPhysics = false;
            minecraft.player.setNoGravity(false);
            applyHitbox(minecraft);
            return;
        }

        if ("NoClip".equalsIgnoreCase(modeName)) {
            minecraft.player.noPhysics = true;
            minecraft.player.setNoGravity(true);
            MovementUtil.applyFlightMotion(minecraft.player, minecraft, this.speed.getValue().doubleValue(), this.vertical.getValue().doubleValue());
            minecraft.player.fallDistance = 0.0F;
            return;
        }

        minecraft.player.noPhysics = false;
        minecraft.player.setNoGravity(false);
        if (minecraft.player.horizontalCollision || minecraft.player.isInWall()) {
            final Vector3d motion = MovementUtil.getHorizontalMotion(
                minecraft.player.yRot,
                MovementUtil.getForwardInput(minecraft),
                MovementUtil.getStrafeInput(minecraft),
                this.speed.getValue().doubleValue()
            );
            final Vector3d clip = motion.lengthSqr() > 1.0E-4D
                ? motion.normalize().scale(this.clipDistance.getValue().doubleValue())
                : Vector3d.ZERO;
            double verticalMotion = 0.0D;
            if (minecraft.options.keyJump.isDown()) {
                verticalMotion += this.vertical.getValue().doubleValue() * 0.5D;
            }
            if (minecraft.options.keyShift.isDown()) {
                verticalMotion -= this.vertical.getValue().doubleValue() * 0.5D;
            }
            minecraft.player.setPos(
                minecraft.player.getX() + clip.x,
                minecraft.player.getY() + verticalMotion,
                minecraft.player.getZ() + clip.z
            );
            minecraft.player.setDeltaMovement(clip.x, verticalMotion, clip.z);
            minecraft.player.fallDistance = 0.0F;
        }
    }

    private void applyHitbox(final Minecraft minecraft) {
        if (this.baseWidth <= 0.0D || this.baseHeight <= 0.0D) {
            this.baseWidth = minecraft.player.getBbWidth();
            this.baseHeight = minecraft.player.getBbHeight();
        }

        final double scale = this.hitboxScale.getValue().doubleValue();
        final double width = Math.max(0.12D, this.baseWidth * scale);
        final double height = Math.max(this.baseHeight, this.baseHeight * (0.92D + (scale - 1.0D) * 0.55D));
        final double halfWidth = width * 0.5D;
        minecraft.player.setBoundingBox(new AxisAlignedBB(
            minecraft.player.getX() - halfWidth,
            minecraft.player.getY(),
            minecraft.player.getZ() - halfWidth,
            minecraft.player.getX() + halfWidth,
            minecraft.player.getY() + height,
            minecraft.player.getZ() + halfWidth
        ));
    }
}
