package dev.mdk2.client.modules.xray;

import com.mojang.authlib.GameProfile;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.movement.MovementUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.InputUpdateEvent;

public class FreecamModule extends Module {
    private final NumberSetting speed;
    private final NumberSetting verticalSpeed;
    private final BooleanSetting freezeBody;
    private RemoteClientPlayerEntity cameraEntity;
    private net.minecraft.entity.Entity previousCameraEntity;
    private double anchorX;
    private double anchorY;
    private double anchorZ;
    private float anchorYaw;
    private float anchorPitch;
    private float anchorHeadYaw;
    private float anchorBodyYaw;

    public FreecamModule() {
        super("Freecam", "Detaches the camera for local noclip exploration.", Category.VISUAL);
        this.speed = register(new NumberSetting("Speed", 0.55D, 0.10D, 2.50D, 0.05D));
        this.verticalSpeed = register(new NumberSetting("Vertical Speed", 0.45D, 0.05D, 2.50D, 0.05D));
        this.freezeBody = register(new BooleanSetting("Freeze Body", true));
    }

    @Override
    public void onEnable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            setEnabled(false);
            return;
        }

        final GameProfile profile = minecraft.player.getGameProfile();
        this.anchorX = minecraft.player.getX();
        this.anchorY = minecraft.player.getY();
        this.anchorZ = minecraft.player.getZ();
        this.anchorYaw = minecraft.player.yRot;
        this.anchorPitch = minecraft.player.xRot;
        this.anchorHeadYaw = minecraft.player.yHeadRot;
        this.anchorBodyYaw = minecraft.player.yBodyRot;
        this.previousCameraEntity = minecraft.getCameraEntity();
        this.cameraEntity = new RemoteClientPlayerEntity(minecraft.level, profile);
        this.cameraEntity.copyPosition(minecraft.player);
        this.cameraEntity.yRot = minecraft.player.yRot;
        this.cameraEntity.xRot = minecraft.player.xRot;
        this.cameraEntity.yRotO = minecraft.player.yRot;
        this.cameraEntity.xRotO = minecraft.player.xRot;
        this.cameraEntity.yHeadRot = minecraft.player.yHeadRot;
        this.cameraEntity.yBodyRot = minecraft.player.yBodyRot;
        this.cameraEntity.noPhysics = true;
        this.cameraEntity.setOnGround(false);
        this.cameraEntity.setDeltaMovement(Vector3d.ZERO);
        minecraft.setCameraEntity(this.cameraEntity);
    }

    @Override
    public void onDisable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.setPos(this.anchorX, this.anchorY, this.anchorZ);
            minecraft.player.yRot = this.anchorYaw;
            minecraft.player.xRot = this.anchorPitch;
            minecraft.player.yHeadRot = this.anchorHeadYaw;
            minecraft.player.yBodyRot = this.anchorBodyYaw;
            minecraft.player.yRotO = this.anchorYaw;
            minecraft.player.xRotO = this.anchorPitch;
            minecraft.player.setDeltaMovement(Vector3d.ZERO);
            minecraft.setCameraEntity(this.previousCameraEntity == null ? minecraft.player : this.previousCameraEntity);
        }
        this.previousCameraEntity = null;
        this.cameraEntity = null;
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || this.cameraEntity == null) {
            setEnabled(false);
            return;
        }

        if (minecraft.getCameraEntity() != this.cameraEntity) {
            minecraft.setCameraEntity(this.cameraEntity);
        }

        this.cameraEntity.yRotO = this.cameraEntity.yRot;
        this.cameraEntity.xRotO = this.cameraEntity.xRot;
        this.cameraEntity.yRot = minecraft.player.yRot;
        this.cameraEntity.xRot = minecraft.player.xRot;
        this.cameraEntity.yHeadRot = this.cameraEntity.yRot;
        this.cameraEntity.yHeadRotO = this.cameraEntity.yRot;
        this.cameraEntity.yBodyRot = this.cameraEntity.yRot;
        this.cameraEntity.yBodyRotO = this.cameraEntity.yRot;
        this.cameraEntity.setOnGround(false);
        this.cameraEntity.noPhysics = true;
        this.cameraEntity.xo = this.cameraEntity.getX();
        this.cameraEntity.yo = this.cameraEntity.getY();
        this.cameraEntity.zo = this.cameraEntity.getZ();

        if (this.freezeBody.getValue().booleanValue()) {
            minecraft.player.setPos(this.anchorX, this.anchorY, this.anchorZ);
            minecraft.player.xo = this.anchorX;
            minecraft.player.yo = this.anchorY;
            minecraft.player.zo = this.anchorZ;
        }
        minecraft.player.setDeltaMovement(Vector3d.ZERO);
        minecraft.player.fallDistance = 0.0F;

        Vector3d motion = MovementUtil.getHorizontalMotion(
            this.cameraEntity.yRot,
            MovementUtil.getForwardInput(minecraft),
            MovementUtil.getStrafeInput(minecraft),
            this.speed.getValue().doubleValue()
        );
        double vertical = 0.0D;
        if (minecraft.options.keyJump.isDown()) {
            vertical += this.verticalSpeed.getValue().doubleValue();
        }
        if (minecraft.options.keyShift.isDown()) {
            vertical -= this.verticalSpeed.getValue().doubleValue();
        }
        motion = motion.add(0.0D, vertical, 0.0D);

        this.cameraEntity.setPos(this.cameraEntity.getX() + motion.x, this.cameraEntity.getY() + motion.y, this.cameraEntity.getZ() + motion.z);
        this.cameraEntity.setDeltaMovement(Vector3d.ZERO);
    }

    @Override
    public void onInputUpdate(final InputUpdateEvent event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getPlayer() != minecraft.player || this.cameraEntity == null) {
            return;
        }

        event.getMovementInput().forwardImpulse = 0.0F;
        event.getMovementInput().leftImpulse = 0.0F;
        event.getMovementInput().jumping = false;
        event.getMovementInput().shiftKeyDown = false;
    }
}
