package dev.mdk2.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.core.ThemeManager;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.modules.visual.DeathCoordsModule;
import dev.mdk2.client.modules.visual.HudModule;
import dev.mdk2.client.modules.visual.StatusEffectsModule;
import dev.mdk2.client.modules.xray.RadarModule;
import dev.mdk2.client.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HudRenderer {
    private final ClientRuntime runtime;
    private final HudArea watermarkArea = new HudArea();
    private final HudArea modulesArea = new HudArea();
    private final HudArea radarArea = new HudArea();
    private final HudArea deathCoordsArea = new HudArea();
    private final HudArea statusEffectsArea = new HudArea();

    private int frames;
    private int sampledFps = 60;
    private long lastSample = System.currentTimeMillis();

    public HudRenderer(final ClientRuntime runtime) {
        this.runtime = runtime;
    }

    public void render(final MatrixStack matrixStack, final float partialTicks) {
        renderInternal(matrixStack, false);
    }

    public void renderEditorPreview(final MatrixStack matrixStack) {
        renderInternal(matrixStack, true);
    }

    public HudArea getWatermarkArea() {
        return this.watermarkArea.copy();
    }

    public HudArea getModulesArea() {
        return this.modulesArea.copy();
    }

    public HudArea getRadarArea() {
        return this.radarArea.copy();
    }

    public HudArea getDeathCoordsArea() {
        return this.deathCoordsArea.copy();
    }

    public HudArea getStatusEffectsArea() {
        return this.statusEffectsArea.copy();
    }

    private void renderInternal(final MatrixStack matrixStack, final boolean editorPreview) {
        final Minecraft minecraft = Minecraft.getInstance();
        final HudModule hudModule = this.runtime.getModuleManager().get(HudModule.class);
        if (hudModule == null) {
            this.watermarkArea.clear();
            this.modulesArea.clear();
            return;
        }

        sampleFps();

        final ThemeManager themeManager = this.runtime.getThemeManager();
        final int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        final int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        renderWatermarkCluster(matrixStack, minecraft, hudModule, themeManager, screenWidth, editorPreview);
        renderLeftArrayList(matrixStack, minecraft, hudModule, themeManager, editorPreview);
        renderWidgetEditorPreview(matrixStack, minecraft, themeManager, screenWidth, screenHeight, editorPreview);
    }

    private void renderWatermarkCluster(final MatrixStack matrixStack, final Minecraft minecraft, final HudModule hudModule,
                                        final ThemeManager themeManager, final int screenWidth, final boolean editorPreview) {
        final List<HudChip> chips = new ArrayList<HudChip>();
        if (hudModule.isWatermarkEnabled()) {
            chips.add(createWatermarkChip(minecraft));
        }
        if (hudModule.isFpsCounterEnabled()) {
            chips.add(createMetricChip(minecraft, this.sampledFps + " FPS"));
        }
        if (hudModule.isPingEnabled()) {
            chips.add(createMetricChip(minecraft, getPing() + " MS"));
        }

        if (chips.isEmpty()) {
            this.watermarkArea.clear();
            return;
        }

        if (hudModule.isSolidLayout()) {
            renderSolidCluster(matrixStack, themeManager, screenWidth, chips, hudModule, editorPreview);
        } else {
            renderSeparateCluster(matrixStack, themeManager, screenWidth, chips, hudModule, editorPreview);
        }
    }

    private void renderLeftArrayList(final MatrixStack matrixStack, final Minecraft minecraft, final HudModule hudModule,
                                     final ThemeManager themeManager, final boolean editorPreview) {
        if (!hudModule.isFunctionsEnabled()) {
            this.modulesArea.clear();
            return;
        }

        final List<Module> visibleModules = new ArrayList<Module>();
        for (final Module module : this.runtime.getModuleManager().getModules()) {
            if (module == hudModule || !module.isToggleable()) {
                continue;
            }
            if (module.getToggleAnimation() > 0.03D) {
                visibleModules.add(module);
            }
        }

        Collections.sort(visibleModules, new Comparator<Module>() {
            @Override
            public int compare(final Module first, final Module second) {
                return Integer.compare(minecraft.font.width(second.getName()), minecraft.font.width(first.getName()));
            }
        });

        final int count = visibleModules.size();
        final double boxWidth = 94.0D;
        final double boxHeight = count >= 8 ? 12.0D : count >= 6 ? 13.5D : 15.0D;
        final double textScale = count >= 8 ? 0.76D : count >= 6 ? 0.82D : 0.88D;
        final double accentHeight = Math.max(5.0D, boxHeight - 6.0D);
        final double spacing = count >= 8 ? 3.0D : 3.5D;
        final double baseX = hudModule.getModulesOffsetX();
        double y = hudModule.getModulesOffsetY();
        final double startY = y;

        int index = 0;
        for (final Module module : visibleModules) {
            final double animation = module.getToggleAnimation();
            final double x = baseX - (1.0D - animation) * 10.0D;
            final int surface = themeManager.isGlass() ? ColorUtil.rgba(24, 30, 42, 108) : ColorUtil.rgba(15, 19, 27, 228);
            final int outline = themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 24) : ColorUtil.rgba(255, 255, 255, 14);
            final String text = trimToWidth(module.getName(), (int) ((boxWidth - 18.0D) / textScale));

            UiRenderer.drawRoundedRect(x, y, boxWidth, boxHeight, 7.0D, surface);
            UiRenderer.drawRoundedOutline(x, y, boxWidth, boxHeight, 7.0D, 1.0D, outline);
            UiRenderer.drawRoundedRect(x + 6.0D, y + (boxHeight - accentHeight) / 2.0D, 1.5D, accentHeight, 0.75D, themeManager.accent(index * 0.14D));
            drawScaledText(matrixStack, text, x + 11.0D, y + (boxHeight - 8.0D * textScale) / 2.0D - 0.2D, textScale, themeManager.textPrimary());

            y += boxHeight + spacing;
            index++;
        }

        final double totalHeight = visibleModules.isEmpty() ? boxHeight : y - startY - spacing;
        this.modulesArea.set(baseX, startY, boxWidth, Math.max(boxHeight, totalHeight));
        if (editorPreview) {
            drawEditorOutline(this.modulesArea, matrixStack, themeManager, "Modules");
        }
    }

    private void renderWidgetEditorPreview(final MatrixStack matrixStack, final Minecraft minecraft, final ThemeManager themeManager,
                                           final int screenWidth, final int screenHeight, final boolean editorPreview) {
        final RadarModule radarModule = this.runtime.getModuleManager().get(RadarModule.class);
        if (radarModule != null && radarModule.isEnabled()) {
            this.radarArea.set(
                radarModule.getRenderX(screenWidth, screenHeight),
                radarModule.getRenderY(screenWidth, screenHeight),
                radarModule.getDisplayWidth(),
                radarModule.getDisplayHeight()
            );
            if (editorPreview) {
                drawWidgetPreview(this.radarArea, matrixStack, themeManager, "Radar");
            }
        } else {
            this.radarArea.clear();
        }

        final DeathCoordsModule deathCoordsModule = this.runtime.getModuleManager().get(DeathCoordsModule.class);
        if (deathCoordsModule != null && deathCoordsModule.isEnabled()) {
            this.deathCoordsArea.set(
                deathCoordsModule.getRenderX(screenWidth, screenHeight),
                deathCoordsModule.getRenderY(screenWidth, screenHeight),
                deathCoordsModule.getDisplayWidth(true),
                deathCoordsModule.getDisplayHeight(true)
            );
            if (editorPreview) {
                drawWidgetPreview(this.deathCoordsArea, matrixStack, themeManager, "Death Coords");
            }
        } else {
            this.deathCoordsArea.clear();
        }

        final StatusEffectsModule statusEffectsModule = this.runtime.getModuleManager().get(StatusEffectsModule.class);
        if (statusEffectsModule != null && statusEffectsModule.isEnabled()) {
            this.statusEffectsArea.set(
                statusEffectsModule.getRenderX(screenWidth, screenHeight),
                statusEffectsModule.getRenderY(screenWidth, screenHeight),
                statusEffectsModule.getDisplayWidth(true),
                statusEffectsModule.getDisplayHeight(true)
            );
            if (editorPreview) {
                drawWidgetPreview(this.statusEffectsArea, matrixStack, themeManager, "Status Effects");
            }
        } else {
            this.statusEffectsArea.clear();
        }
    }

    private void renderSolidCluster(final MatrixStack matrixStack, final ThemeManager themeManager, final int screenWidth,
                                    final List<HudChip> chips, final HudModule hudModule, final boolean editorPreview) {
        final double height = 17.0D;
        final double totalWidth = getClusterWidth(chips, 0.0D);
        final double x = screenWidth - totalWidth - hudModule.getWatermarkOffsetX();
        final double y = hudModule.getWatermarkOffsetY();
        final int surface = themeManager.isGlass() ? ColorUtil.rgba(23, 29, 40, 112) : ColorUtil.rgba(12, 16, 24, 232);
        final int outline = themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 34) : ColorUtil.rgba(255, 255, 255, 18);
        final int accentGlow = ColorUtil.withAlpha(themeManager.accent(0.0D), themeManager.isGlass() ? 18 : 14);

        this.watermarkArea.set(x, y, totalWidth, height);
        UiRenderer.drawShadow(x, y, totalWidth, height, 3, accentGlow);
        UiRenderer.drawRoundedRect(x, y, totalWidth, height, 8.0D, surface);
        UiRenderer.drawRoundedOutline(x, y, totalWidth, height, 8.0D, 1.0D, outline);
        UiRenderer.drawRoundedOutline(x + 0.5D, y + 0.5D, totalWidth - 1.0D, height - 1.0D, 7.5D, 1.0D, ColorUtil.withAlpha(themeManager.accent(0.0D), 30));

        double cursor = x;
        for (int i = 0; i < chips.size(); i++) {
            final HudChip chip = chips.get(i);
            renderChipContent(matrixStack, chip, cursor, y, height, themeManager, true, i);
            cursor += chip.width;

            if (i < chips.size() - 1) {
                UiRenderer.drawLine(cursor, y + 4.0D, cursor, y + height - 4.0D, 1.0D,
                    themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 18) : ColorUtil.rgba(255, 255, 255, 10));
            }
        }
        if (editorPreview) {
            drawEditorOutline(this.watermarkArea, matrixStack, themeManager, "Watermark");
        }
    }

    private void renderSeparateCluster(final MatrixStack matrixStack, final ThemeManager themeManager, final int screenWidth,
                                       final List<HudChip> chips, final HudModule hudModule, final boolean editorPreview) {
        final double gap = 4.0D;
        final double height = 17.0D;
        final double totalWidth = getClusterWidth(chips, gap);
        final double y = hudModule.getWatermarkOffsetY();
        double x = screenWidth - totalWidth - hudModule.getWatermarkOffsetX();
        this.watermarkArea.set(x, y, totalWidth, height);

        for (int i = 0; i < chips.size(); i++) {
            final HudChip chip = chips.get(i);
            final int surface = themeManager.isGlass() ? ColorUtil.rgba(23, 29, 40, 112) : ColorUtil.rgba(12, 16, 24, 232);
            final int outline = themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 34) : ColorUtil.rgba(255, 255, 255, 18);
            final int accentGlow = ColorUtil.withAlpha(themeManager.accent(i * 0.1D), themeManager.isGlass() ? 18 : 14);

            UiRenderer.drawShadow(x, y, chip.width, height, 3, accentGlow);
            UiRenderer.drawRoundedRect(x, y, chip.width, height, 8.0D, surface);
            UiRenderer.drawRoundedOutline(x, y, chip.width, height, 8.0D, 1.0D, outline);
            UiRenderer.drawRoundedOutline(x + 0.5D, y + 0.5D, chip.width - 1.0D, height - 1.0D, 7.5D, 1.0D,
                ColorUtil.withAlpha(themeManager.accent(i * 0.1D), 28));
            renderChipContent(matrixStack, chip, x, y, height, themeManager, false, i);
            x += chip.width + gap;
        }
        if (editorPreview) {
            drawEditorOutline(this.watermarkArea, matrixStack, themeManager, "Watermark");
        }
    }

    private void renderChipContent(final MatrixStack matrixStack, final HudChip chip, final double x, final double y, final double height,
                                   final ThemeManager themeManager, final boolean solidLayout, final int index) {
        final double accentX = x + 5.0D;
        UiRenderer.drawRoundedRect(accentX, y + 4.0D, 1.5D, height - 8.0D, 0.75D, themeManager.accent(index * 0.12D));

        if (chip.type == HudChipType.WATERMARK) {
            UiRenderer.drawText(matrixStack, chip.primaryText, (float) (x + 10.0D), (float) (y + 5.0D), themeManager.textPrimary());

            final double avatarRadius = 4.0D;
            final double avatarCenterX = x + chip.width - 9.0D;
            final double avatarCenterY = y + height / 2.0D;
            UiRenderer.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 28) : ColorUtil.rgba(45, 52, 70, 255));
            UiRenderer.drawRoundedOutline(
                avatarCenterX - avatarRadius,
                avatarCenterY - avatarRadius,
                avatarRadius * 2.0D,
                avatarRadius * 2.0D,
                avatarRadius,
                1.0D,
                solidLayout ? ColorUtil.rgba(255, 255, 255, 22) : themeManager.outline(22)
            );
            return;
        }

        UiRenderer.drawText(matrixStack, chip.primaryText, (float) (x + 10.0D), (float) (y + 5.0D), themeManager.textPrimary());
    }

    private HudChip createWatermarkChip(final Minecraft minecraft) {
        final String title = "MDK2 CLIENT";
        final double width = Math.max(70.0D, minecraft.font.width(title) + 26.0D);
        return new HudChip(HudChipType.WATERMARK, title, width);
    }

    private HudChip createMetricChip(final Minecraft minecraft, final String text) {
        final double width = Math.max(38.0D, minecraft.font.width(text) + 16.0D);
        return new HudChip(HudChipType.METRIC, text, width);
    }

    private double getClusterWidth(final List<HudChip> chips, final double gap) {
        double width = 0.0D;
        for (int i = 0; i < chips.size(); i++) {
            width += chips.get(i).width;
            if (i < chips.size() - 1) {
                width += gap;
            }
        }
        return width;
    }

    private void drawScaledText(final MatrixStack matrixStack, final String text, final double x, final double y, final double scale, final int color) {
        UiRenderer.push();
        UiRenderer.scale(scale, scale, 1.0D);
        UiRenderer.drawText(matrixStack, text, (float) (x / scale), (float) (y / scale), color);
        UiRenderer.pop();
    }

    private int getPing() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.connection == null) {
            return 0;
        }

        final NetworkPlayerInfo info = minecraft.player.connection.getPlayerInfo(minecraft.player.getUUID());
        return info == null ? 0 : info.getLatency();
    }

    private void sampleFps() {
        this.frames++;
        final long now = System.currentTimeMillis();
        if (now - this.lastSample >= 1000L) {
            this.sampledFps = this.frames;
            this.frames = 0;
            this.lastSample = now;
        }
    }

    private String trimToWidth(final String text, final int maxWidth) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.font.width(text) <= maxWidth) {
            return text;
        }

        String result = text;
        while (result.length() > 1 && minecraft.font.width(result + "...") > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + "...";
    }

    private void drawEditorOutline(final HudArea area, final MatrixStack matrixStack, final ThemeManager themeManager, final String label) {
        if (!area.isVisible()) {
            return;
        }

        UiRenderer.drawRoundedOutline(area.x - 1.0D, area.y - 1.0D, area.width + 2.0D, area.height + 2.0D, 9.0D, 1.0D,
            ColorUtil.withAlpha(themeManager.accent(0.0D), 90));
        UiRenderer.drawText(matrixStack, label, (float) (area.x + 2.0D), (float) (area.y - 8.0D), ColorUtil.withAlpha(themeManager.textSecondary(), 170));
    }

    private void drawWidgetPreview(final HudArea area, final MatrixStack matrixStack, final ThemeManager themeManager, final String label) {
        if (!area.isVisible()) {
            return;
        }

        UiRenderer.drawRoundedRect(area.x, area.y, area.width, area.height, 10.0D, themeManager.isGlass()
            ? ColorUtil.rgba(22, 28, 39, 68)
            : ColorUtil.rgba(14, 18, 26, 118));
        drawEditorOutline(area, matrixStack, themeManager, label);
    }

    private static final class HudChip {
        private final HudChipType type;
        private final String primaryText;
        private final double width;

        private HudChip(final HudChipType type, final String primaryText, final double width) {
            this.type = type;
            this.primaryText = primaryText;
            this.width = width;
        }
    }

    private enum HudChipType {
        WATERMARK,
        METRIC
    }

    public static final class HudArea {
        private double x;
        private double y;
        private double width;
        private double height;

        private void set(final double x, final double y, final double width, final double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private void clear() {
            this.width = 0.0D;
            this.height = 0.0D;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getWidth() {
            return this.width;
        }

        public double getHeight() {
            return this.height;
        }

        public boolean contains(final double mouseX, final double mouseY) {
            return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
        }

        public boolean isVisible() {
            return this.width > 0.0D && this.height > 0.0D;
        }

        public HudArea copy() {
            final HudArea copy = new HudArea();
            copy.set(this.x, this.y, this.width, this.height);
            return copy;
        }
    }
}
