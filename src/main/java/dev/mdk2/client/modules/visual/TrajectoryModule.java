package dev.mdk2.client.modules.visual;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.core.ThemeManager;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.render.WorldRenderUtil;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ColorSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.TridentItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TrajectoryModule extends Module {
    private static final int MAX_STEPS = 160;
    private static final double MAX_DISTANCE_SQUARED = 8192.0D;

    private final BooleanSetting bow;
    private final BooleanSetting crossbow;
    private final BooleanSetting potions;
    private final BooleanSetting pearls;
    private final BooleanSetting flyingPearls;
    private final BooleanSetting snowballs;
    private final BooleanSetting trident;
    private final BooleanSetting depthTest;
    private final BooleanSetting impactMarker;
    private final BooleanSetting targetBox;
    private final NumberSetting lineWidth;
    private final NumberSetting markerSize;
    private final ColorSetting lineColor;
    private final ColorSetting blockImpactColor;
    private final ColorSetting entityImpactColor;
    private final ColorSetting pearlColor;

    public TrajectoryModule() {
        super("Trajectories", "Renders predicted projectile arcs for bows, crossbows and throwables.", Category.VISUAL);
        this.bow = register(new BooleanSetting("Bow", true));
        this.crossbow = register(new BooleanSetting("Crossbow", true));
        this.potions = register(new BooleanSetting("Potions", true));
        this.pearls = register(new BooleanSetting("Pearls", true));
        this.flyingPearls = register(new BooleanSetting("Flying Pearls", true));
        this.snowballs = register(new BooleanSetting("Snowballs", true));
        this.trident = register(new BooleanSetting("Trident", true));
        this.depthTest = register(new BooleanSetting("Depth Test", false));
        this.impactMarker = register(new BooleanSetting("Impact Marker", true));
        this.targetBox = register(new BooleanSetting("Target Box", false));
        this.lineWidth = register(new NumberSetting("Line Width", 2.0D, 0.5D, 5.0D, 0.25D));
        this.markerSize = register(new NumberSetting("Marker Size", 0.18D, 0.05D, 0.60D, 0.01D));
        this.lineColor = register(new ColorSetting("Line Color", ColorUtil.rgba(118, 146, 255, 255)));
        this.blockImpactColor = register(new ColorSetting("Block Hit Color", ColorUtil.rgba(255, 188, 92, 255)));
        this.entityImpactColor = register(new ColorSetting("Entity Hit Color", ColorUtil.rgba(255, 102, 102, 255)));
        this.pearlColor = register(new ColorSetting("Pearl Color", ColorUtil.rgba(110, 255, 218, 255)));
    }

    @Override
    public void onRender3D(final MatrixStack matrixStack, final float partialTicks) {
        final Minecraft minecraft = Minecraft.getInstance();
        final PlayerEntity player = minecraft.player;
        final World world = minecraft.level;
        if (player == null || world == null) {
            return;
        }

        final boolean useDepthTest = this.depthTest.getValue().booleanValue();
        final float width = this.lineWidth.getValue().floatValue();
        final double animationTime = player.tickCount + partialTicks;
        final List<TrajectoryRequest> requests = resolveRequests(player);

        for (int index = 0; index < requests.size(); index++) {
            final TrajectoryRequest request = requests.get(index);
            final TrajectoryResult result = simulate(world, player, player, request);
            if (result.points.size() < 2) {
                continue;
            }

            renderTrajectory(matrixStack, result, width, useDepthTest, animationTime * 0.12D + index * 0.19D,
                this.lineColor.getColor(), this.markerSize.getValue().doubleValue(), animationTime);
        }

        if (this.flyingPearls.getValue().booleanValue()) {
            renderFlyingPearls(matrixStack, world, player, width, useDepthTest, animationTime);
        }
    }

    private List<TrajectoryRequest> resolveRequests(final PlayerEntity player) {
        final List<TrajectoryRequest> requests = new ArrayList<TrajectoryRequest>();
        if (player.isUsingItem()) {
            final ItemStack usingStack = player.getUseItem();
            if (this.bow.getValue().booleanValue() && usingStack.getItem() instanceof BowItem) {
                final TrajectoryRequest bowRequest = createBowRequest(player, usingStack);
                if (bowRequest != null) {
                    requests.add(bowRequest);
                }
                return requests;
            }
            if (this.trident.getValue().booleanValue() && usingStack.getItem() instanceof TridentItem) {
                final TrajectoryRequest tridentRequest = createTridentRequest(player, usingStack);
                if (tridentRequest != null) {
                    requests.add(tridentRequest);
                }
                return requests;
            }
        }

        if (this.crossbow.getValue().booleanValue()) {
            if (tryAddCrossbowRequests(player, player.getItemInHand(Hand.MAIN_HAND), requests)) {
                return requests;
            }
            if (tryAddCrossbowRequests(player, player.getItemInHand(Hand.OFF_HAND), requests)) {
                return requests;
            }
        }

        if (tryAddThrowableRequest(player, player.getItemInHand(Hand.MAIN_HAND), requests)) {
            return requests;
        }
        if (tryAddThrowableRequest(player, player.getItemInHand(Hand.OFF_HAND), requests)) {
            return requests;
        }
        return requests;
    }

    private TrajectoryRequest createBowRequest(final PlayerEntity player, final ItemStack stack) {
        final int useTicks = stack.getUseDuration() - player.getUseItemRemainingTicks();
        final float power = BowItem.getPowerForTime(useTicks);
        if (power < 0.1F) {
            return null;
        }

        return new TrajectoryRequest(
            getArrowStart(player),
            calculateShootVector(player, power * 3.0D, 0.0F),
            0.05D,
            0.99D,
            0.60D
        );
    }

    private TrajectoryRequest createTridentRequest(final PlayerEntity player, final ItemStack stack) {
        final int useTicks = stack.getUseDuration() - player.getUseItemRemainingTicks();
        if (useTicks < 10 || net.minecraft.enchantment.EnchantmentHelper.getRiptide(stack) > 0) {
            return null;
        }

        return new TrajectoryRequest(
            getArrowStart(player),
            calculateShootVector(player, 2.5D, 0.0F),
            0.05D,
            0.99D,
            0.60D
        );
    }

    private boolean tryAddCrossbowRequests(final PlayerEntity player, final ItemStack stack, final List<TrajectoryRequest> requests) {
        if (!(stack.getItem() instanceof CrossbowItem) || !CrossbowItem.isCharged(stack)) {
            return false;
        }

        final List<ItemStack> chargedProjectiles = getChargedProjectiles(stack);
        if (chargedProjectiles.isEmpty()) {
            return false;
        }

        final boolean hasFirework = CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET);
        final double velocity = hasFirework ? 1.6D : 3.15D;
        final double gravity = hasFirework ? 0.0D : 0.05D;
        final double drag = hasFirework ? 1.0D : 0.99D;
        final double waterDrag = hasFirework ? 1.0D : 0.60D;
        final Vector3d start = hasFirework ? getFireworkStart(player) : getArrowStart(player);

        for (int index = 0; index < chargedProjectiles.size(); index++) {
            final float angle;
            if (index == 1) {
                angle = -10.0F;
            } else if (index == 2) {
                angle = 10.0F;
            } else {
                angle = 0.0F;
            }
            requests.add(new TrajectoryRequest(
                start,
                calculateCrossbowVector(player, velocity, angle),
                gravity,
                drag,
                waterDrag
            ));
        }
        return !requests.isEmpty();
    }

    private boolean tryAddThrowableRequest(final PlayerEntity player, final ItemStack stack, final List<TrajectoryRequest> requests) {
        if (stack.isEmpty()) {
            return false;
        }

        if (this.potions.getValue().booleanValue() && stack.getItem() instanceof ThrowablePotionItem) {
            requests.add(new TrajectoryRequest(
                getThrowableStart(player),
                calculateShootVector(player, 0.5D, -20.0F),
                0.03D,
                0.99D,
                0.80D
            ));
            return true;
        }

        if (this.pearls.getValue().booleanValue() && stack.getItem() instanceof EnderPearlItem) {
            requests.add(new TrajectoryRequest(
                getThrowableStart(player),
                calculateShootVector(player, 1.5D, 0.0F),
                0.03D,
                0.99D,
                0.80D
            ));
            return true;
        }

        if (this.snowballs.getValue().booleanValue() && stack.getItem() instanceof SnowballItem) {
            requests.add(new TrajectoryRequest(
                getThrowableStart(player),
                calculateShootVector(player, 1.5D, 0.0F),
                0.03D,
                0.99D,
                0.80D
            ));
            return true;
        }

        return false;
    }

    private void renderFlyingPearls(final MatrixStack matrixStack, final World world, final PlayerEntity player,
                                    final float width, final boolean useDepthTest, final double animationTime) {
        int pearlIndex = 0;
        for (final Entity entity : world.getEntities(player, player.getBoundingBox().inflate(160.0D), candidate -> candidate instanceof EnderPearlEntity)) {
            if (!(entity instanceof EnderPearlEntity) || entity.removed || player.distanceToSqr(entity) > MAX_DISTANCE_SQUARED * 2.0D) {
                continue;
            }

            final EnderPearlEntity pearl = (EnderPearlEntity) entity;
            final Vector3d position = new Vector3d(
                pearl.xo + (pearl.getX() - pearl.xo) * Minecraft.getInstance().getFrameTime(),
                pearl.yo + (pearl.getY() - pearl.yo) * Minecraft.getInstance().getFrameTime(),
                pearl.zo + (pearl.getZ() - pearl.zo) * Minecraft.getInstance().getFrameTime()
            );
            final TrajectoryRequest request = new TrajectoryRequest(position, pearl.getDeltaMovement(), 0.03D, 0.99D, 0.80D);
            final TrajectoryResult result = simulate(world, pearl, pearl.getOwner(), request);
            if (result.points.size() >= 2) {
                renderTrajectory(
                    matrixStack,
                    result,
                    Math.max(1.0F, width - 0.25F),
                    useDepthTest,
                    animationTime * 0.16D + pearl.tickCount * 0.07D + pearlIndex * 0.11D,
                    this.pearlColor.getColor(),
                    this.markerSize.getValue().doubleValue() * 0.88D,
                    animationTime
                );
            }

            renderFlyingPearlPulse(matrixStack, pearl, useDepthTest, animationTime + pearlIndex * 0.35D);
            pearlIndex++;
        }
    }

    private void renderTrajectory(final MatrixStack matrixStack, final TrajectoryResult result, final float width,
                                  final boolean useDepthTest, final double shimmerTime, final int baseAccent,
                                  final double markerBaseSize, final double animationTime) {
        final int baseColor = ColorUtil.withAlpha(baseAccent, 210);
        final int impactColor = result.hitEntity == null
            ? this.blockImpactColor.getColor()
            : this.entityImpactColor.getColor();
        final int highlightColor = ColorUtil.withAlpha(ColorUtil.interpolate(baseColor, impactColor, 0.55D), 255);
        WorldRenderUtil.drawGradientLineStrip(
            matrixStack,
            result.points,
            baseColor,
            highlightColor,
            width,
            useDepthTest,
            shimmerTime
        );

        if (!this.impactMarker.getValue().booleanValue() || result.impactPoint == null) {
            return;
        }

        final int outlineColor = result.hitEntity == null
            ? ColorUtil.withAlpha(this.blockImpactColor.getColor(), 235)
            : ColorUtil.withAlpha(this.entityImpactColor.getColor(), 235);
        final int fillColor = ColorUtil.withAlpha(outlineColor, result.hitEntity == null ? 52 : 68);
        final double pulse = 0.90D + Math.sin(animationTime * 0.20D + shimmerTime * 3.0D) * 0.16D;
        final double markerHalfSize = markerBaseSize * pulse;
        final AxisAlignedBB impactBox = new AxisAlignedBB(
            result.impactPoint.x - markerHalfSize,
            result.impactPoint.y - markerHalfSize,
            result.impactPoint.z - markerHalfSize,
            result.impactPoint.x + markerHalfSize,
            result.impactPoint.y + markerHalfSize,
            result.impactPoint.z + markerHalfSize
        );
        WorldRenderUtil.drawFilledOutlinedBox(matrixStack, impactBox, fillColor, outlineColor, 1.2F, useDepthTest);
        WorldRenderUtil.drawFilledOutlinedBox(
            matrixStack,
            impactBox.inflate(0.045D + markerHalfSize * 0.25D),
            ColorUtil.withAlpha(outlineColor, 16),
            ColorUtil.withAlpha(outlineColor, 72),
            1.0F,
            useDepthTest
        );

        if (this.targetBox.getValue().booleanValue() && result.hitEntity != null && result.hitEntity.isAlive()) {
            final AxisAlignedBB box = result.hitEntity.getBoundingBox().inflate(0.03D + 0.02D * pulse);
            WorldRenderUtil.drawFilledOutlinedBox(
                matrixStack,
                box,
                ColorUtil.withAlpha(this.entityImpactColor.getColor(), 24),
                ColorUtil.withAlpha(this.entityImpactColor.getColor(), 170),
                1.1F,
                useDepthTest
            );
        }
    }

    private void renderFlyingPearlPulse(final MatrixStack matrixStack, final EnderPearlEntity pearl,
                                        final boolean useDepthTest, final double animationTime) {
        final double pulse = 0.72D + Math.sin(animationTime * 0.35D) * 0.18D;
        final double coreSize = 0.08D * pulse;
        final Vector3d pearlPos = new Vector3d(pearl.getX(), pearl.getY(), pearl.getZ());
        final AxisAlignedBB coreBox = new AxisAlignedBB(
            pearlPos.x - coreSize,
            pearlPos.y - coreSize,
            pearlPos.z - coreSize,
            pearlPos.x + coreSize,
            pearlPos.y + coreSize,
            pearlPos.z + coreSize
        );
        WorldRenderUtil.drawFilledOutlinedBox(
            matrixStack,
            coreBox,
            ColorUtil.withAlpha(this.pearlColor.getColor(), 38),
            ColorUtil.withAlpha(ColorUtil.interpolate(this.pearlColor.getColor(), ColorUtil.rgba(255, 255, 255, 255), 0.38D), 210),
            1.0F,
            useDepthTest
        );
        WorldRenderUtil.drawFilledOutlinedBox(
            matrixStack,
            coreBox.inflate(0.05D + coreSize * 0.55D),
            ColorUtil.withAlpha(this.pearlColor.getColor(), 10),
            ColorUtil.withAlpha(this.pearlColor.getColor(), 90),
            1.0F,
            useDepthTest
        );
    }

    private TrajectoryResult simulate(final World world, final Entity traceEntity, final Entity owner, final TrajectoryRequest request) {
        final List<Vector3d> points = new ArrayList<Vector3d>();
        Vector3d currentPosition = request.start;
        Vector3d currentVelocity = request.velocity;
        points.add(currentPosition);

        for (int step = 0; step < MAX_STEPS; step++) {
            final Vector3d nextPosition = currentPosition.add(currentVelocity);
            final BlockRayTraceResult blockHit = world.clip(new RayTraceContext(
                currentPosition,
                nextPosition,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                traceEntity == null ? owner : traceEntity
            ));
            final EntityRayTraceResult entityHit = ProjectileHelper.getEntityHitResult(
                world,
                traceEntity == null ? owner : traceEntity,
                currentPosition,
                nextPosition,
                createSearchBox(currentPosition, nextPosition),
                validEntityTarget(traceEntity, owner)
            );

            final Optional<Vector3d> entityImpact = resolveEntityImpact(entityHit, currentPosition, nextPosition);
            final Vector3d blockImpact = blockHit.getType() == RayTraceResult.Type.MISS ? null : blockHit.getLocation();

            if (entityImpact.isPresent() && isEntityHitCloser(currentPosition, entityImpact.get(), blockImpact)) {
                points.add(entityImpact.get());
                return new TrajectoryResult(points, entityImpact.get(), entityHit.getEntity());
            }

            if (blockImpact != null) {
                points.add(blockImpact);
                return new TrajectoryResult(points, blockImpact, null);
            }

            currentPosition = nextPosition;
            points.add(currentPosition);

            if (points.get(0).distanceToSqr(currentPosition) > MAX_DISTANCE_SQUARED || currentPosition.y < -64.0D) {
                break;
            }

            currentVelocity = currentVelocity.scale(resolveDrag(world, currentPosition, request));
            if (request.gravity > 0.0D) {
                currentVelocity = currentVelocity.add(0.0D, -request.gravity, 0.0D);
            }
        }

        return new TrajectoryResult(points, null, null);
    }

    private Predicate<Entity> validEntityTarget(final Entity traceEntity, final Entity owner) {
        return entity -> entity != null
            && entity.isAlive()
            && entity.isPickable()
            && !entity.isSpectator()
            && entity != traceEntity
            && entity != owner
            && (owner == null || entity.getRootVehicle() != owner.getRootVehicle());
    }

    private AxisAlignedBB createSearchBox(final Vector3d start, final Vector3d end) {
        return new AxisAlignedBB(
            Math.min(start.x, end.x),
            Math.min(start.y, end.y),
            Math.min(start.z, end.z),
            Math.max(start.x, end.x),
            Math.max(start.y, end.y),
            Math.max(start.z, end.z)
        ).inflate(1.0D);
    }

    private Optional<Vector3d> resolveEntityImpact(final EntityRayTraceResult entityHit, final Vector3d start, final Vector3d end) {
        if (entityHit == null || entityHit.getEntity() == null) {
            return Optional.empty();
        }

        return entityHit.getEntity().getBoundingBox().inflate(0.3D).clip(start, end);
    }

    private boolean isEntityHitCloser(final Vector3d start, final Vector3d entityImpact, final Vector3d blockImpact) {
        if (blockImpact == null) {
            return true;
        }
        return start.distanceToSqr(entityImpact) <= start.distanceToSqr(blockImpact);
    }

    private double resolveDrag(final World world, final Vector3d position, final TrajectoryRequest request) {
        final BlockPos blockPos = new BlockPos(position.x, position.y, position.z);
        if (!world.getFluidState(blockPos).isEmpty()) {
            return request.waterDrag;
        }
        return request.drag;
    }

    private Vector3d calculateShootVector(final PlayerEntity player, final double velocity, final float xRotOffset) {
        final float yaw = player.yRot;
        final float pitch = player.xRot + xRotOffset;
        final double x = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        final double y = -Math.sin(Math.toRadians(pitch));
        final double z = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        return applyShooterMotion(player, new Vector3d(x, y, z), velocity);
    }

    private Vector3d calculateCrossbowVector(final PlayerEntity player, final double velocity, final float angle) {
        final Vector3d upVector = player.getUpVector(1.0F);
        final Vector3d viewVector = player.getViewVector(1.0F);
        final Quaternion rotation = new Quaternion(new Vector3f(upVector), angle, true);
        final Vector3f rotatedView = new Vector3f(viewVector);
        rotatedView.transform(rotation);
        return applyShooterMotion(player, new Vector3d(rotatedView.x(), rotatedView.y(), rotatedView.z()), velocity);
    }

    private Vector3d applyShooterMotion(final PlayerEntity player, final Vector3d direction, final double velocity) {
        Vector3d movement = direction.normalize().scale(velocity);
        final Vector3d playerMotion = player.getDeltaMovement();
        movement = movement.add(playerMotion.x, player.isOnGround() ? 0.0D : playerMotion.y, playerMotion.z);
        return movement;
    }

    private Vector3d getArrowStart(final PlayerEntity player) {
        return new Vector3d(player.getX(), player.getEyeY() - 0.1D, player.getZ());
    }

    private Vector3d getThrowableStart(final PlayerEntity player) {
        return new Vector3d(player.getX(), player.getEyeY() - 0.1D, player.getZ());
    }

    private Vector3d getFireworkStart(final PlayerEntity player) {
        return new Vector3d(player.getX(), player.getEyeY() - 0.15D, player.getZ());
    }

    private List<ItemStack> getChargedProjectiles(final ItemStack stack) {
        final CompoundNBT tag = stack.getTag();
        if (tag == null || !tag.contains("ChargedProjectiles", 9)) {
            return Collections.emptyList();
        }

        final ListNBT projectiles = tag.getList("ChargedProjectiles", 10);
        final List<ItemStack> items = new ArrayList<ItemStack>();
        for (int index = 0; index < projectiles.size(); index++) {
            items.add(ItemStack.of(projectiles.getCompound(index)));
        }
        return items;
    }

    private static final class TrajectoryRequest {
        private final Vector3d start;
        private final Vector3d velocity;
        private final double gravity;
        private final double drag;
        private final double waterDrag;

        private TrajectoryRequest(final Vector3d start, final Vector3d velocity, final double gravity, final double drag, final double waterDrag) {
            this.start = start;
            this.velocity = velocity;
            this.gravity = gravity;
            this.drag = drag;
            this.waterDrag = waterDrag;
        }
    }

    private static final class TrajectoryResult {
        private final List<Vector3d> points;
        private final Vector3d impactPoint;
        private final Entity hitEntity;

        private TrajectoryResult(final List<Vector3d> points, final Vector3d impactPoint, final Entity hitEntity) {
            this.points = points;
            this.impactPoint = impactPoint;
            this.hitEntity = hitEntity;
        }
    }
}
