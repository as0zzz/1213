package dev.mdk2.client.modules.visual;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.render.HudStyleRenderer;
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
        this.scale = register(new NumberSetting("Scale", 0.82D, 0.55D, 1.30D, 0.05D));
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

        final int textColor = ClientRuntime.getInstance().getThemeManager().textPrimary();
        final String text = getDisplayText();
        final double scaleValue = displayScale();
        final double x = getRenderX(width, height);
        final double y = getRenderY(width, height);
        final double baseWidth = getBaseWidth(text);

        UiRenderer.push();
        UiRenderer.translate(x, y, 0.0D);
        UiRenderer.scale(scaleValue, scaleValue, 1.0D);
        HudStyleRenderer.drawShell(0.0D, 0.0D, baseWidth, 11.5D, 6.0D, ClientRuntime.getInstance().getThemeManager(), this.outlineColor.getColor());
        HudStyleRenderer.drawText(
            matrixStack,
            text,
            (baseWidth - HudStyleRenderer.textWidth(text, 0.60D)) / 2.0D,
            (11.5D - HudStyleRenderer.lineHeight(0.60D)) / 2.0D - 0.45D,
            0.60D,
            textColor,
            ClientRuntime.getInstance().getThemeManager()
        );
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
        return getBaseWidth(preview ? "Death: -120 64 255" : getDisplayText()) * displayScale();
    }

    public double getDisplayHeight(final boolean preview) {
        return 11.5D * displayScale();
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
        return HudStyleRenderer.textWidth(text, 0.60D) + 12.0D;
    }

    private double displayScale() {
        return this.scale.getValue().doubleValue() * 0.94D;
    }
}
