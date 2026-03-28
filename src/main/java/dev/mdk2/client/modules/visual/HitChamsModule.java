package dev.mdk2.client.modules.visual;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.core.ThemeManager;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ColorSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class HitChamsModule extends Module {
    private static final int FULL_BRIGHT = 15728880;

    private final NumberSetting lifetime;
    private final NumberSetting alpha;
    private final ModeSetting colorMode;
    private final ColorSetting color;
    private final Deque<TraceRecord> traces = new ArrayDeque<TraceRecord>();

    public HitChamsModule() {
        super("Hit Chams", "Leaves short-lived hit ghosts at attacked entity positions.", Category.VISUAL);
        this.lifetime = register(new NumberSetting("Lifetime", 1.35D, 0.80D, 2.00D, 0.05D));
        this.alpha = register(new NumberSetting("Alpha", 0.42D, 0.05D, 1.00D, 0.01D));
        this.colorMode = register(new ModeSetting("Color Mode", "Theme", "Theme", "Custom"));
        this.color = register(new ColorSetting("Color", ColorUtil.rgba(106, 216, 255, 255)));
        setEnabled(false);
    }

    @Override
    public void onTick() {
        final long now = System.currentTimeMillis();
        final long maxAge = (long) (this.lifetime.getValue().doubleValue() * 1000.0D);
        final Iterator<TraceRecord> iterator = this.traces.iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().createdAt > maxAge) {
                iterator.remove();
            }
        }
    }

    @Override
    public void onAttackEntity(final AttackEntityEvent event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getPlayer() != minecraft.player) {
            return;
        }

        final Entity target = event.getTarget();
        if (!(target instanceof LivingEntity)) {
            return;
        }

        while (this.traces.size() >= 3) {
            this.traces.removeFirst();
        }
        this.traces.addLast(new TraceRecord((LivingEntity) target, System.currentTimeMillis()));
    }

    @Override
    public void onRender3D(final MatrixStack matrixStack, final float partialTicks) {
        if (this.traces.isEmpty()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        final EntityRendererManager dispatcher = minecraft.getEntityRenderDispatcher();
        final IRenderTypeBuffer.Impl buffer = minecraft.renderBuffers().bufferSource();
        final Vector3d camera = minecraft.gameRenderer.getMainCamera().getPosition();
        final long now = System.currentTimeMillis();
        final double maxAge = this.lifetime.getValue().doubleValue() * 1000.0D;
        int index = 0;
        for (final TraceRecord trace : this.traces) {
            final double progress = Math.min(1.0D, (now - trace.createdAt) / maxAge);
            final int accent = getColor(index * 0.15D);
            renderGhost(trace, matrixStack, dispatcher, buffer, camera, partialTicks, accent, progress);
            index++;
        }
        buffer.endBatch();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        dispatcher.setRenderShadow(true);
    }

    private int getColor(final double offset) {
        if ("Custom".equalsIgnoreCase(this.colorMode.getValue())) {
            return this.color.getColor();
        }

        final ThemeManager themeManager = ClientRuntime.getInstance() != null ? ClientRuntime.getInstance().getThemeManager() : null;
        return themeManager == null ? ColorUtil.rgba(118, 146, 255, 255) : themeManager.accent(offset);
    }

    private void renderGhost(final TraceRecord trace, final MatrixStack matrixStack, final EntityRendererManager dispatcher,
                             final IRenderTypeBuffer.Impl buffer, final Vector3d camera, final float partialTicks,
                             final int accent, final double progress) {
        final LivingEntity entity = trace.entity;
        if (entity == null) {
            return;
        }

        final EntitySnapshot previous = EntitySnapshot.capture(entity);
        applySnapshot(entity, trace);

        final float alpha = (float) Math.max(0.02D, this.alpha.getValue().doubleValue() * (1.0D - progress));
        final float red = (accent >>> 16 & 255) / 255.0F;
        final float green = (accent >>> 8 & 255) / 255.0F;
        final float blue = (accent & 255) / 255.0F;

        dispatcher.setRenderShadow(false);
        matrixStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.disableTexture();
        RenderSystem.color4f(red, green, blue, alpha);
        dispatcher.render(entity, trace.x - camera.x, trace.y - camera.y, trace.z - camera.z, trace.yRot, partialTicks, matrixStack, buffer, FULL_BRIGHT);
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrixStack.popPose();

        previous.restore(entity);
    }

    private void applySnapshot(final LivingEntity entity, final TraceRecord trace) {
        entity.setPos(trace.x, trace.y, trace.z);
        entity.xo = trace.x;
        entity.yo = trace.y;
        entity.zo = trace.z;
        entity.yRot = trace.yRot;
        entity.yRotO = trace.yRot;
        entity.xRot = trace.xRot;
        entity.xRotO = trace.xRot;
        entity.yHeadRot = trace.yHeadRot;
        entity.yHeadRotO = trace.yHeadRot;
        entity.yBodyRot = trace.yBodyRot;
        entity.yBodyRotO = trace.yBodyRot;
        entity.tickCount = trace.tickCount;
        entity.hurtTime = 0;
        entity.deathTime = 0;
        entity.animationSpeed = trace.animationSpeed;
        entity.animationSpeedOld = trace.animationSpeedOld;
        entity.animationPosition = trace.animationPosition;
        entity.attackAnim = trace.attackAnim;
        entity.oAttackAnim = trace.attackAnim;
        entity.swinging = trace.swinging;
        entity.swingTime = trace.swingTime;
    }

    private static final class TraceRecord {
        private final LivingEntity entity;
        private final double x;
        private final double y;
        private final double z;
        private final float yRot;
        private final float xRot;
        private final float yHeadRot;
        private final float yBodyRot;
        private final int tickCount;
        private final float animationSpeed;
        private final float animationSpeedOld;
        private final float animationPosition;
        private final float attackAnim;
        private final boolean swinging;
        private final int swingTime;
        private final long createdAt;

        private TraceRecord(final LivingEntity entity, final long createdAt) {
            this.entity = entity;
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
            this.yRot = entity.yRot;
            this.xRot = entity.xRot;
            this.yHeadRot = entity.yHeadRot;
            this.yBodyRot = entity.yBodyRot;
            this.tickCount = entity.tickCount;
            this.animationSpeed = entity.animationSpeed;
            this.animationSpeedOld = entity.animationSpeedOld;
            this.animationPosition = entity.animationPosition;
            this.attackAnim = entity.attackAnim;
            this.swinging = entity.swinging;
            this.swingTime = entity.swingTime;
            this.createdAt = createdAt;
        }
    }

    private static final class EntitySnapshot {
        private final double x;
        private final double y;
        private final double z;
        private final double xo;
        private final double yo;
        private final double zo;
        private final float yRot;
        private final float yRotO;
        private final float xRot;
        private final float xRotO;
        private final float yHeadRot;
        private final float yHeadRotO;
        private final float yBodyRot;
        private final float yBodyRotO;
        private final int tickCount;
        private final int hurtTime;
        private final int deathTime;
        private final float animationSpeed;
        private final float animationSpeedOld;
        private final float animationPosition;
        private final float attackAnim;
        private final float oAttackAnim;
        private final boolean swinging;
        private final int swingTime;

        private EntitySnapshot(final LivingEntity entity) {
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
            this.xo = entity.xo;
            this.yo = entity.yo;
            this.zo = entity.zo;
            this.yRot = entity.yRot;
            this.yRotO = entity.yRotO;
            this.xRot = entity.xRot;
            this.xRotO = entity.xRotO;
            this.yHeadRot = entity.yHeadRot;
            this.yHeadRotO = entity.yHeadRotO;
            this.yBodyRot = entity.yBodyRot;
            this.yBodyRotO = entity.yBodyRotO;
            this.tickCount = entity.tickCount;
            this.hurtTime = entity.hurtTime;
            this.deathTime = entity.deathTime;
            this.animationSpeed = entity.animationSpeed;
            this.animationSpeedOld = entity.animationSpeedOld;
            this.animationPosition = entity.animationPosition;
            this.attackAnim = entity.attackAnim;
            this.oAttackAnim = entity.oAttackAnim;
            this.swinging = entity.swinging;
            this.swingTime = entity.swingTime;
        }

        private static EntitySnapshot capture(final LivingEntity entity) {
            return new EntitySnapshot(entity);
        }

        private void restore(final LivingEntity entity) {
            entity.setPos(this.x, this.y, this.z);
            entity.xo = this.xo;
            entity.yo = this.yo;
            entity.zo = this.zo;
            entity.yRot = this.yRot;
            entity.yRotO = this.yRotO;
            entity.xRot = this.xRot;
            entity.xRotO = this.xRotO;
            entity.yHeadRot = this.yHeadRot;
            entity.yHeadRotO = this.yHeadRotO;
            entity.yBodyRot = this.yBodyRot;
            entity.yBodyRotO = this.yBodyRotO;
            entity.tickCount = this.tickCount;
            entity.hurtTime = this.hurtTime;
            entity.deathTime = this.deathTime;
            entity.animationSpeed = this.animationSpeed;
            entity.animationSpeedOld = this.animationSpeedOld;
            entity.animationPosition = this.animationPosition;
            entity.attackAnim = this.attackAnim;
            entity.oAttackAnim = this.oAttackAnim;
            entity.swinging = this.swinging;
            entity.swingTime = this.swingTime;
        }
    }
}
