package dev.mdk2.client.modules.visual;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.render.UiRenderer;
import dev.mdk2.client.settings.ColorSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class DeathCoordsModule extends Module {
    private final NumberSetting scale;
    private final ColorSetting outlineColor;
    private BlockPos lastDeathPos;
    private boolean wasAlive = true;
    private double positionX = 8.0D;
    private double positionY = -1.0D;

    public DeathCoordsModule() {
        super("Death Coords", "Stores and displays the last death coordinates.", Category.VISUAL);
        this.scale = register(new NumberSetting("Scale", 1.0D, 0.65D, 1.60D, 0.05D));
        this.outlineColor = register(new ColorSetting("Outline Color", ColorUtil.rgba(118, 146, 255, 255)));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            this.wasAlive = true;
            return;
        }

        final boolean alive = minecraft.player.isAlive() && minecraft.player.getHealth() > 0.0F;
        if (this.wasAlive && !alive) {
            this.lastDeathPos = minecraft.player.blockPosition();
        }
        this.wasAlive = alive;
    }

    @Override
    public void onRender2D(final MatrixStack matrixStack, final float partialTicks, final int width, final int height) {
        if (this.lastDeathPos == null) {
            return;
        }

        final int background = ClientRuntime.getInstance().getThemeManager().surface(178);
        final int outline = ColorUtil.withAlpha(this.outlineColor.getColor(), 42);
        final String text = getDisplayText();
        final double scaleValue = this.scale.getValue().doubleValue();
        final double x = getRenderX(width, height);
        final double y = getRenderY(width, height);

        UiRenderer.push();
        UiRenderer.translate(x, y, 0.0D);
        UiRenderer.scale(scaleValue, scaleValue, 1.0D);
        UiRenderer.drawRoundedRect(0.0D, 0.0D, getBaseWidth(text), 14.0D, 7.0D, background);
        UiRenderer.drawRoundedOutline(0.0D, 0.0D, getBaseWidth(text), 14.0D, 7.0D, 1.0D, outline);
        UiRenderer.drawText(matrixStack, text, 7.0F, 4.0F, ClientRuntime.getInstance().getThemeManager().textPrimary());
        UiRenderer.pop();
    }

    public double getRenderX(final int width, final int height) {
        return this.positionX;
    }

    public double getRenderY(final int width, final int height) {
        if (this.positionY < 0.0D) {
            this.positionY = height - 24.0D;
        }
        return this.positionY;
    }

    public double getDisplayWidth(final boolean preview) {
        return getBaseWidth(preview ? "Death: -120 64 255" : getDisplayText()) * this.scale.getValue().doubleValue();
    }

    public double getDisplayHeight(final boolean preview) {
        return 14.0D * this.scale.getValue().doubleValue();
    }

    public void setPosition(final double x, final double y) {
        this.positionX = x;
        this.positionY = y;
    }

    public boolean hasDisplayData() {
        return this.lastDeathPos != null;
    }

    private String getDisplayText() {
        return "Death: " + this.lastDeathPos.getX() + " " + this.lastDeathPos.getY() + " " + this.lastDeathPos.getZ();
    }

    private double getBaseWidth(final String text) {
        return Minecraft.getInstance().font.width(text) + 14.0D;
    }
}
