package dev.mdk2.client.modules.xray;

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
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Matrix4f;

public class EntityEspModule extends Module {
    private final BooleanSetting players;
    private final BooleanSetting mobs;
    private final BooleanSetting items;
    private final BooleanSetting nameTags;
    private final BooleanSetting healthTags;
    private final BooleanSetting itemCounts;
    private final NumberSetting range;
    private final NumberSetting fillAlpha;
    private final NumberSetting outlineAlpha;
    private final ColorSetting playerColor;
    private final ColorSetting mobColor;
    private final ColorSetting itemColor;

    public EntityEspModule() {
        super("Entity ESP", "Highlights players, mobs and dropped items.", Category.VISUAL);
        this.players = register(new BooleanSetting("Players", true));
        this.mobs = register(new BooleanSetting("Mobs", true));
        this.items = register(new BooleanSetting("Items", false));
        this.nameTags = register(new BooleanSetting("Name Tags", false));
        this.healthTags = register(new BooleanSetting("Health Tags", false));
        this.itemCounts = register(new BooleanSetting("Item Counts", true));
        this.range = register(new NumberSetting("Range", 48.0D, 8.0D, 128.0D, 1.0D));
        this.fillAlpha = register(new NumberSetting("Fill Alpha", 0.12D, 0.02D, 0.50D, 0.01D));
        this.outlineAlpha = register(new NumberSetting("Line Alpha", 0.86D, 0.10D, 1.00D, 0.01D));
        this.playerColor = register(new ColorSetting("Player Color", ColorUtil.rgba(118, 146, 255, 255)));
        this.mobColor = register(new ColorSetting("Mob Color", ColorUtil.rgba(255, 102, 102, 255)));
        this.itemColor = register(new ColorSetting("Item Color", ColorUtil.rgba(108, 230, 176, 255)));
    }

    @Override
    public void onRender3D(final MatrixStack matrixStack, final float partialTicks) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.gameRenderer == null || minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }

        final IRenderTypeBuffer.Impl buffer = minecraft.renderBuffers().bufferSource();
        final FontRenderer font = minecraft.font;
        final double maxDistanceSq = this.range.getValue().doubleValue() * this.range.getValue().doubleValue();
        for (final Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity == minecraft.player || !entity.isAlive() || minecraft.player.distanceToSqr(entity) > maxDistanceSq) {
                continue;
            }

            final int baseColor = resolveColor(entity);
            if (baseColor == 0) {
                continue;
            }

            final double renderX = entity.xo + (entity.getX() - entity.xo) * partialTicks;
            final double renderY = entity.yo + (entity.getY() - entity.yo) * partialTicks;
            final double renderZ = entity.zo + (entity.getZ() - entity.zo) * partialTicks;
            AxisAlignedBB box = entity.getBoundingBox().move(renderX - entity.getX(), renderY - entity.getY(), renderZ - entity.getZ());
            if (entity instanceof ItemEntity) {
                box = box.inflate(0.08D);
            }

            WorldRenderUtil.drawBufferedBox(
                matrixStack,
                buffer,
                box,
                ColorUtil.withAlpha(baseColor, toAlpha(this.fillAlpha.getValue().doubleValue())),
                ColorUtil.withAlpha(baseColor, toAlpha(this.outlineAlpha.getValue().doubleValue()))
            );

            if (shouldRenderTag(entity)) {
                final String label = buildLabel(entity);
                renderTag(matrixStack, buffer, font, label, entity, baseColor, partialTicks);
            }
        }
        buffer.endBatch();
    }

    private int resolveColor(final Entity entity) {
        if (entity instanceof PlayerEntity && this.players.getValue().booleanValue()) {
            return this.playerColor.getColor();
        }
        if (entity instanceof MobEntity && this.mobs.getValue().booleanValue()) {
            return this.mobColor.getColor();
        }
        if (entity instanceof ItemEntity && this.items.getValue().booleanValue()) {
            return this.itemColor.getColor();
        }
        return 0;
    }

    private String buildLabel(final Entity entity) {
        if (entity instanceof ItemEntity && this.itemCounts.getValue().booleanValue()) {
            final ItemStack stack = ((ItemEntity) entity).getItem();
            return stack.getHoverName().getString() + " [" + stack.getCount() + "]";
        }
        if (this.healthTags.getValue().booleanValue() && entity instanceof LivingEntity) {
            final LivingEntity livingEntity = (LivingEntity) entity;
            return entity.getDisplayName().getString() + " [" + (int) Math.ceil(livingEntity.getHealth()) + "]";
        }
        return entity.getDisplayName().getString();
    }

    private boolean shouldRenderTag(final Entity entity) {
        if (entity instanceof ItemEntity) {
            return this.items.getValue().booleanValue() && this.itemCounts.getValue().booleanValue();
        }
        return this.nameTags.getValue().booleanValue();
    }

    private void renderTag(final MatrixStack matrixStack, final IRenderTypeBuffer.Impl buffer, final FontRenderer font,
                           final String text, final Entity entity, final int color, final float partialTicks) {
        final Minecraft minecraft = Minecraft.getInstance();
        final double x = entity.xo + (entity.getX() - entity.xo) * partialTicks;
        final double y = entity.yo + (entity.getY() - entity.yo) * partialTicks + entity.getBbHeight() + 0.35D;
        final double z = entity.zo + (entity.getZ() - entity.zo) * partialTicks;
        final double cameraX = minecraft.gameRenderer.getMainCamera().getPosition().x;
        final double cameraY = minecraft.gameRenderer.getMainCamera().getPosition().y;
        final double cameraZ = minecraft.gameRenderer.getMainCamera().getPosition().z;

        matrixStack.pushPose();
        matrixStack.translate(x - cameraX, y - cameraY, z - cameraZ);
        matrixStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        matrixStack.scale(-0.025F, -0.025F, 0.025F);

        final Matrix4f pose = matrixStack.last().pose();
        final float textX = -font.width(text) / 2.0F;
        font.drawInBatch(text, textX, 0.0F, color, false, pose, buffer, false, 0, 15728880);
        matrixStack.popPose();
    }

    private int toAlpha(final double normalized) {
        return (int) Math.round(normalized * 255.0D);
    }
}
