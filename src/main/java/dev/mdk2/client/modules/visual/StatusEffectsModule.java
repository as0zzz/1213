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
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StatusEffectsModule extends Module {
    private final NumberSetting scale;
    private final ColorSetting outlineColor;
    private double positionX = 8.0D;
    private double positionY = 26.0D;

    public StatusEffectsModule() {
        super("Status Effects", "Shows active potion effects in a compact list.", Category.VISUAL);
        this.scale = register(new NumberSetting("Scale", 1.0D, 0.65D, 1.60D, 0.05D));
        this.outlineColor = register(new ColorSetting("Outline Color", ColorUtil.rgba(118, 146, 255, 255)));
    }

    @Override
    public void onRender2D(final MatrixStack matrixStack, final float partialTicks, final int width, final int height) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.getActiveEffects().isEmpty()) {
            return;
        }

        final List<EffectInstance> effects = new ArrayList<EffectInstance>(minecraft.player.getActiveEffects());
        effects.sort(Comparator.comparingInt(EffectInstance::getDuration).reversed());
        renderEffectsList(matrixStack, effects, getRenderX(width, height), getRenderY(width, height));
    }

    public double getRenderX(final int width, final int height) {
        return this.positionX;
    }

    public double getRenderY(final int width, final int height) {
        return this.positionY;
    }

    public double getDisplayWidth(final boolean preview) {
        final Minecraft minecraft = Minecraft.getInstance();
        final List<String> lines = preview ? getPreviewLines() : getCurrentLines();
        double maxWidth = 72.0D;
        for (final String line : lines) {
            maxWidth = Math.max(maxWidth, minecraft.font.width(line) + 12.0D);
        }
        return maxWidth * this.scale.getValue().doubleValue();
    }

    public double getDisplayHeight(final boolean preview) {
        final int lines = preview ? getPreviewLines().size() : Math.max(1, getCurrentLines().size());
        return ((lines * 13.0D) + Math.max(0, lines - 1) * 2.0D) * this.scale.getValue().doubleValue();
    }

    public void setPosition(final double x, final double y) {
        this.positionX = x;
        this.positionY = y;
    }

    public boolean hasDisplayData() {
        return !getCurrentLines().isEmpty();
    }

    public void renderPreview(final MatrixStack matrixStack, final double x, final double y) {
        final List<EffectInstance> dummy = new ArrayList<EffectInstance>();
        renderLines(matrixStack, getPreviewLines(), x, y);
    }

    private void renderEffectsList(final MatrixStack matrixStack, final List<EffectInstance> effects, final double x, final double y) {
        final List<String> lines = new ArrayList<String>();
        for (final EffectInstance effect : effects) {
            final String amplifier = effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "";
            lines.add(I18n.get(effect.getEffect().getDescriptionId()) + amplifier + " " + EffectUtils.formatDuration(effect, 1.0F));
        }
        renderLines(matrixStack, lines, x, y);
    }

    private void renderLines(final MatrixStack matrixStack, final List<String> lines, final double x, final double y) {
        final double scaleValue = this.scale.getValue().doubleValue();
        final int background = ClientRuntime.getInstance().getThemeManager().surface(164);
        final int outline = ColorUtil.withAlpha(this.outlineColor.getColor(), 34);

        UiRenderer.push();
        UiRenderer.translate(x, y, 0.0D);
        UiRenderer.scale(scaleValue, scaleValue, 1.0D);
        double currentY = 0.0D;
        for (final String text : lines) {
            final double boxWidth = Minecraft.getInstance().font.width(text) + 12.0D;
            UiRenderer.drawRoundedRect(0.0D, currentY, boxWidth, 13.0D, 6.0D, background);
            UiRenderer.drawRoundedOutline(0.0D, currentY, boxWidth, 13.0D, 6.0D, 1.0D, outline);
            UiRenderer.drawText(matrixStack, text, 6.0F, (float) (currentY + 3.5D), ClientRuntime.getInstance().getThemeManager().textPrimary());
            currentY += 15.0D;
        }
        UiRenderer.pop();
    }

    private List<String> getCurrentLines() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.getActiveEffects().isEmpty()) {
            return new ArrayList<String>();
        }

        final List<EffectInstance> effects = new ArrayList<EffectInstance>(minecraft.player.getActiveEffects());
        effects.sort(Comparator.comparingInt(EffectInstance::getDuration).reversed());
        final List<String> lines = new ArrayList<String>();
        for (final EffectInstance effect : effects) {
            final String amplifier = effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "";
            lines.add(I18n.get(effect.getEffect().getDescriptionId()) + amplifier + " " + EffectUtils.formatDuration(effect, 1.0F));
        }
        return lines;
    }

    private List<String> getPreviewLines() {
        final List<String> lines = new ArrayList<String>();
        lines.add("Speed II 1:24");
        lines.add("Strength 0:48");
        return lines;
    }
}
