package dev.mdk2.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.core.ThemeManager;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.modules.visual.DeathCoordsModule;
import dev.mdk2.client.modules.visual.HudModule;
import dev.mdk2.client.modules.visual.StatusEffectsModule;
import dev.mdk2.client.modules.visual.WatermarkModule;
import dev.mdk2.client.modules.xray.RadarModule;
import dev.mdk2.client.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.util.math.vector.Vector3d;

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
    private final WatermarkSystemSampler systemSampler = new WatermarkSystemSampler();

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
        final WatermarkModule watermarkModule = this.runtime.getModuleManager().get(WatermarkModule.class);
        if (hudModule == null || watermarkModule == null) {
            this.watermarkArea.clear();
            this.modulesArea.clear();
            return;
        }

        sampleFps();

        final ThemeManager themeManager = this.runtime.getThemeManager();
        final int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        final int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        renderWatermarkCluster(matrixStack, minecraft, hudModule, watermarkModule, themeManager, screenWidth, editorPreview);
        renderLeftArrayList(matrixStack, minecraft, hudModule, themeManager, editorPreview);
        renderWidgetEditorPreview(matrixStack, minecraft, themeManager, screenWidth, screenHeight, editorPreview);
    }

    private void renderWatermarkCluster(final MatrixStack matrixStack, final Minecraft minecraft, final HudModule hudModule,
                                        final WatermarkModule watermarkModule,
                                        final ThemeManager themeManager, final int screenWidth, final boolean editorPreview) {
        final List<HudChip> chips = new ArrayList<HudChip>();
        if (!hudModule.isWatermarkEnabled()) {
            this.watermarkArea.clear();
            return;
        }

        final WatermarkSystemSampler.Snapshot systemSnapshot = this.systemSampler.snapshot(watermarkModule.isGpuLoadEnabled());
        if (watermarkModule.isLogoEnabled()) {
            chips.add(createWatermarkChip(WatermarkMetricFormatter.logo()));
        }
        if (watermarkModule.isFramerateEnabled()) {
            chips.add(createMetricChip(WatermarkMetricFormatter.framerate(this.sampledFps)));
        }
        if (watermarkModule.isPingEnabled()) {
            chips.add(createMetricChip(WatermarkMetricFormatter.ping(getPing())));
        }
        if (watermarkModule.isSpeedEnabled()) {
            chips.add(createMetricChip(WatermarkMetricFormatter.speed(getMovementSpeed(minecraft))));
        }
        if (watermarkModule.isGpuLoadEnabled()) {
            chips.add(createMetricChip(WatermarkMetricFormatter.load("GPU", systemSnapshot.gpuLoad)));
        }
        if (watermarkModule.isCpuLoadEnabled()) {
            chips.add(createMetricChip(WatermarkMetricFormatter.load("CPU", systemSnapshot.cpuLoad)));
        }
        if (watermarkModule.isMemoryLoadEnabled()) {
            chips.add(createMetricChip(WatermarkMetricFormatter.load("MEM", systemSnapshot.memoryLoad)));
        }
        if (watermarkModule.isUsernameEnabled()) {
            chips.add(createMetricChip(WatermarkMetricFormatter.username(getUsername(minecraft))));
        }
        if (watermarkModule.isConfigNameEnabled()) {
            chips.add(createMetricChip(WatermarkMetricFormatter.configName(this.runtime.getConfigManager().getSelectedConfigName())));
        }
        if (watermarkModule.isServerIpEnabled()) {
            chips.add(createMetricChip(WatermarkMetricFormatter.server(getServerAddress(minecraft))));
        }

        if (chips.isEmpty()) {
            this.watermarkArea.clear();
            return;
        }

        if (hudModule.isSolidLayout()) {
            renderSolidCluster(matrixStack, themeManager, screenWidth, chips, watermarkModule, editorPreview);
        } else {
            renderSeparateCluster(matrixStack, themeManager, screenWidth, chips, watermarkModule, editorPreview);
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
        final double boxHeight = count >= 8 ? 11.0D : count >= 6 ? 11.8D : 12.6D;
        final double textScale = count >= 8 ? 0.56D : count >= 6 ? 0.60D : 0.64D;
        final double accentHeight = Math.max(3.5D, boxHeight - 5.2D);
        final double spacing = 2.4D;
        final double baseX = hudModule.getModulesOffsetX();
        double y = hudModule.getModulesOffsetY();
        final double startY = y;
        double maxWidth = 64.0D;

        int index = 0;
        for (final Module module : visibleModules) {
            final double animation = module.getToggleAnimation();
            final String text = trimToWidth(module.getName(), (int) Math.max(26.0D, 78.0D / textScale));
            final double textWidth = HudStyleRenderer.textWidth(text, textScale);
            final double boxWidth = Math.max(64.0D, textWidth + 16.0D);
            final double x = baseX - (1.0D - animation) * 10.0D;
            maxWidth = Math.max(maxWidth, boxWidth);

            HudStyleRenderer.drawShell(x, y, boxWidth, boxHeight, 7.0D, themeManager, themeManager.accent(index * 0.14D));
            UiRenderer.drawRoundedRect(x + 4.0D, y + (boxHeight - accentHeight) / 2.0D, 1.2D, accentHeight, 0.6D, themeManager.accent(index * 0.14D));
            HudStyleRenderer.drawText(
                matrixStack,
                text,
                x + Math.max(8.0D, (boxWidth - textWidth) / 2.0D),
                y + (boxHeight - HudStyleRenderer.lineHeight(textScale)) / 2.0D - 0.55D,
                textScale,
                themeManager.textPrimary(),
                themeManager
            );

            y += boxHeight + spacing;
            index++;
        }

        final double totalHeight = visibleModules.isEmpty() ? boxHeight : y - startY - spacing;
        this.modulesArea.set(baseX, startY, maxWidth, Math.max(boxHeight, totalHeight));
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
                                    final List<HudChip> chips, final WatermarkModule watermarkModule, final boolean editorPreview) {
        final double height = 12.0D;
        final double totalWidth = getClusterWidth(chips, 0.0D);
        final double x = watermarkModule.getRenderX(screenWidth, totalWidth);
        final double y = watermarkModule.getRenderY();

        this.watermarkArea.set(x, y, totalWidth, height);
        HudStyleRenderer.drawShell(x, y, totalWidth, height, 7.0D, themeManager, themeManager.accent(0.0D));

        double cursor = x;
        for (int i = 0; i < chips.size(); i++) {
            final HudChip chip = chips.get(i);
            renderChipContent(matrixStack, chip, cursor, y, height, themeManager, true, i);
            cursor += chip.width;

            if (i < chips.size() - 1) {
                UiRenderer.drawLine(cursor, y + 2.5D, cursor, y + height - 2.5D, 1.0D,
                    themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 18) : ColorUtil.rgba(255, 255, 255, 10));
            }
        }
        if (editorPreview) {
            drawEditorOutline(this.watermarkArea, matrixStack, themeManager, "Watermark");
        }
    }

    private void renderSeparateCluster(final MatrixStack matrixStack, final ThemeManager themeManager, final int screenWidth,
                                       final List<HudChip> chips, final WatermarkModule watermarkModule, final boolean editorPreview) {
        final double gap = 2.5D;
        final double height = 12.0D;
        final double totalWidth = getClusterWidth(chips, gap);
        final double y = watermarkModule.getRenderY();
        double x = watermarkModule.getRenderX(screenWidth, totalWidth);
        this.watermarkArea.set(x, y, totalWidth, height);

        for (int i = 0; i < chips.size(); i++) {
            final HudChip chip = chips.get(i);
            HudStyleRenderer.drawShell(x, y, chip.width, height, 7.0D, themeManager, themeManager.accent(i * 0.1D));
            renderChipContent(matrixStack, chip, x, y, height, themeManager, false, i);
            x += chip.width + gap;
        }
        if (editorPreview) {
            drawEditorOutline(this.watermarkArea, matrixStack, themeManager, "Watermark");
        }
    }

    private void renderChipContent(final MatrixStack matrixStack, final HudChip chip, final double x, final double y, final double height,
                                   final ThemeManager themeManager, final boolean solidLayout, final int index) {
        final double textScale = chip.type == HudChipType.WATERMARK ? 0.60D : 0.58D;
        final double accentX = x + 3.5D;
        final double accentHeight = height - 5.0D;
        final double textWidth = HudStyleRenderer.textWidth(chip.primaryText, textScale);
        UiRenderer.drawRoundedRect(accentX, y + (height - accentHeight) / 2.0D, 1.1D, accentHeight, 0.55D, themeManager.accent(index * 0.12D));
        HudStyleRenderer.drawText(
            matrixStack,
            chip.primaryText,
            x + Math.max(6.5D, (chip.width - textWidth) / 2.0D),
            y + (height - HudStyleRenderer.lineHeight(textScale)) / 2.0D - 0.55D,
            textScale,
            themeManager.textPrimary(),
            themeManager
        );
    }

    private HudChip createWatermarkChip(final String text) {
        final double width = Math.max(46.0D, HudStyleRenderer.textWidth(text, 0.60D) + 14.0D);
        return new HudChip(HudChipType.WATERMARK, text, width);
    }

    private HudChip createMetricChip(final String text) {
        final double width = Math.max(32.0D, HudStyleRenderer.textWidth(text, 0.58D) + 12.0D);
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

    private double getMovementSpeed(final Minecraft minecraft) {
        if (minecraft.player == null) {
            return 0.0D;
        }
        final Vector3d motion = minecraft.player.getDeltaMovement();
        return Math.sqrt(motion.x * motion.x + motion.z * motion.z) * 20.0D;
    }

    private String getUsername(final Minecraft minecraft) {
        if (minecraft.player != null && minecraft.player.getGameProfile() != null) {
            return minecraft.player.getGameProfile().getName();
        }
        return minecraft.getUser() != null ? minecraft.getUser().getName() : "Player";
    }

    private String getServerAddress(final Minecraft minecraft) {
        if (minecraft.hasSingleplayerServer()) {
            return "Singleplayer";
        }
        final ServerData serverData = minecraft.getCurrentServer();
        if (serverData == null) {
            return "Main Menu";
        }
        return serverData.ip == null || serverData.ip.trim().isEmpty() ? serverData.name : serverData.ip;
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
        HudStyleRenderer.drawText(matrixStack, label, area.x + 2.0D, area.y - 7.0D, 0.58D, ColorUtil.withAlpha(themeManager.textSecondary(), 170), themeManager);
    }

    private void drawWidgetPreview(final HudArea area, final MatrixStack matrixStack, final ThemeManager themeManager, final String label) {
        if (!area.isVisible()) {
            return;
        }

        HudStyleRenderer.drawShell(area.x, area.y, area.width, area.height, 8.0D, themeManager, themeManager.accent(0.0D));
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
