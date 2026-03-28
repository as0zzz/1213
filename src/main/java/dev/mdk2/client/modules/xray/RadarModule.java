package dev.mdk2.client.modules.xray;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.core.ThemeManager;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.render.UiRenderer;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ColorSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.ColorUtil;
import dev.mdk2.client.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

public class RadarModule extends Module {
    private final BooleanSetting players;
    private final BooleanSetting mobs;
    private final BooleanSetting items;
    private final BooleanSetting rotate;
    private final NumberSetting size;
    private final NumberSetting range;
    private final ColorSetting accentColor;
    private final ColorSetting playerColor;
    private final ColorSetting mobColor;
    private final ColorSetting itemColor;
    private double positionX = -1.0D;
    private double positionY = -1.0D;

    public RadarModule() {
        super("Radar", "Shows nearby entities on a compact 2D radar.", Category.VISUAL);
        this.players = register(new BooleanSetting("Players", true));
        this.mobs = register(new BooleanSetting("Mobs", true));
        this.items = register(new BooleanSetting("Items", false));
        this.rotate = register(new BooleanSetting("Rotate", true));
        this.size = register(new NumberSetting("Size", 82.0D, 56.0D, 140.0D, 1.0D));
        this.range = register(new NumberSetting("Range", 56.0D, 12.0D, 128.0D, 1.0D));
        this.accentColor = register(new ColorSetting("Accent Color", ColorUtil.rgba(118, 146, 255, 255)));
        this.playerColor = register(new ColorSetting("Player Color", ColorUtil.rgba(118, 146, 255, 255)));
        this.mobColor = register(new ColorSetting("Mob Color", ColorUtil.rgba(255, 112, 112, 255)));
        this.itemColor = register(new ColorSetting("Item Color", ColorUtil.rgba(104, 236, 176, 255)));
    }

    @Override
    public void onRender2D(final MatrixStack matrixStack, final float partialTicks, final int width, final int height) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        final ThemeManager themeManager = ClientRuntime.getInstance().getThemeManager();
        final double sizeValue = this.size.getValue().doubleValue();
        final double x = getRenderX(width, height);
        final double y = getRenderY(width, height);
        final double centerX = x + sizeValue / 2.0D;
        final double centerY = y + sizeValue / 2.0D;
        final double radius = sizeValue / 2.0D - 7.0D;

        UiRenderer.drawShadow(x, y, sizeValue, sizeValue, 2, ColorUtil.withAlpha(this.accentColor.getColor(), 10));
        UiRenderer.drawRoundedRect(x, y, sizeValue, sizeValue, 12.0D, themeManager.isGlass() ? ColorUtil.rgba(18, 22, 32, 120) : ColorUtil.rgba(13, 16, 24, 214));
        UiRenderer.drawRoundedOutline(x, y, sizeValue, sizeValue, 12.0D, 1.0D, ColorUtil.withAlpha(this.accentColor.getColor(), 34));
        UiRenderer.drawText(matrixStack, "Radar", (float) (x + 7.0D), (float) (y + 6.0D), themeManager.textPrimary());
        UiRenderer.drawLine(centerX, y + 18.0D, centerX, y + sizeValue - 6.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 18));
        UiRenderer.drawLine(x + 6.0D, centerY, x + sizeValue - 6.0D, centerY, 1.0D, ColorUtil.rgba(255, 255, 255, 18));
        UiRenderer.drawCircle(centerX, centerY, 2.1D, this.accentColor.getColor());

        final double maxRange = this.range.getValue().doubleValue();
        final double maxRangeSq = maxRange * maxRange;
        final double yaw = Math.toRadians(minecraft.player.yRot);
        final double sin = Math.sin(yaw);
        final double cos = Math.cos(yaw);

        for (final Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity == minecraft.player || !entity.isAlive() || minecraft.player.distanceToSqr(entity) > maxRangeSq) {
                continue;
            }

            final int color = classify(entity);
            if (color == 0) {
                continue;
            }

            double relX = entity.getX() - minecraft.player.getX();
            double relZ = entity.getZ() - minecraft.player.getZ();
            if (this.rotate.getValue().booleanValue()) {
                final double rotatedX = -(relX * cos + relZ * sin);
                final double rotatedZ = relZ * cos - relX * sin;
                relX = rotatedX;
                relZ = rotatedZ;
            }

            final double plotX = MathUtil.clamp(relX / maxRange, -1.0D, 1.0D) * radius;
            final double plotY = -MathUtil.clamp(relZ / maxRange, -1.0D, 1.0D) * radius;
            UiRenderer.drawCircle(centerX + plotX, centerY + plotY, entity instanceof ItemEntity ? 1.6D : 2.0D, color);
        }
    }

    public double getRenderX(final int width, final int height) {
        if (this.positionX < 0.0D) {
            this.positionX = width - this.size.getValue().doubleValue() - 10.0D;
        }
        return this.positionX;
    }

    public double getRenderY(final int width, final int height) {
        if (this.positionY < 0.0D) {
            this.positionY = height - this.size.getValue().doubleValue() - 54.0D;
        }
        return this.positionY;
    }

    public double getDisplayWidth() {
        return this.size.getValue().doubleValue();
    }

    public double getDisplayHeight() {
        return this.size.getValue().doubleValue();
    }

    public void setPosition(final double x, final double y) {
        this.positionX = x;
        this.positionY = y;
    }

    private int classify(final Entity entity) {
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
}
