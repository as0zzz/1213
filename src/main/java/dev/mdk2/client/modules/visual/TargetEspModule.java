package dev.mdk2.client.modules.visual;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.core.ThemeManager;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.modules.combat.AuraModule;
import dev.mdk2.client.render.WorldRenderUtil;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ColorSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class TargetEspModule extends Module {
    private final ModeSetting mode;
    private final BooleanSetting depthTest;
    private final BooleanSetting targetBox;
    private final NumberSetting lineWidth;
    private final NumberSetting scale;
    private final NumberSetting speed;
    private final NumberSetting glow;
    private final ColorSetting primaryColor;
    private final ColorSetting secondaryColor;

    public TargetEspModule() {
        super("Target ESP", "Renders animated target effects around Aura's current target.", Category.VISUAL);
        this.mode = register(new ModeSetting("Mode", "Chains",
            "Chains", "Orbit", "Rings", "Pulse", "Light", "DNA", "Ghost", "Lightning", "Marker", "Satellites"));
        this.depthTest = register(new BooleanSetting("Depth Test", false));
        this.targetBox = register(new BooleanSetting("Target Box", false));
        this.lineWidth = register(new NumberSetting("Line Width", 2.2D, 0.75D, 5.0D, 0.05D));
        this.scale = register(new NumberSetting("Scale", 1.0D, 0.60D, 1.85D, 0.05D));
        this.speed = register(new NumberSetting("Speed", 1.0D, 0.30D, 2.60D, 0.05D));
        this.glow = register(new NumberSetting("Glow", 0.82D, 0.00D, 1.00D, 0.05D));
        this.primaryColor = register(new ColorSetting("Primary Color", ColorUtil.rgba(255, 82, 214, 255)));
        this.secondaryColor = register(new ColorSetting("Secondary Color", ColorUtil.rgba(255, 190, 244, 255)));
    }

    @Override
    public void onRender3D(final MatrixStack matrixStack, final float partialTicks) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        final AuraModule auraModule = ClientRuntime.getInstance().getModuleManager().get(AuraModule.class);
        if (auraModule == null || !auraModule.isEnabled()) {
            return;
        }

        final LivingEntity target = auraModule.getRenderTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        final TargetRenderData renderData = buildRenderData(target, partialTicks, this.scale.getValue().doubleValue());
        final double animationTime = (minecraft.player.tickCount + partialTicks) * 0.055D * this.speed.getValue().doubleValue();
        final float width = this.lineWidth.getValue().floatValue();
        final boolean useDepthTest = this.depthTest.getValue().booleanValue();
        final int accent = this.primaryColor.getColor();
        final int softAccent = this.secondaryColor.getColor();
        final int hotAccent = ColorUtil.interpolate(this.primaryColor.getColor(), this.secondaryColor.getColor(), 0.45D);

        renderEffect(matrixStack, renderData, animationTime, width, useDepthTest, accent, softAccent, hotAccent);

        if (this.targetBox.getValue().booleanValue()) {
            final double pulse = 0.035D + (0.018D + this.glow.getValue().doubleValue() * 0.035D) * (0.5D + 0.5D * Math.sin(animationTime * 3.2D));
            WorldRenderUtil.drawFilledOutlinedBox(
                matrixStack,
                renderData.boundingBox.inflate(pulse),
                ColorUtil.withAlpha(accent, 16 + (int) Math.round(this.glow.getValue().doubleValue() * 24.0D)),
                ColorUtil.withAlpha(hotAccent, 160),
                1.15F,
                useDepthTest
            );
        }
    }

    private void renderEffect(final MatrixStack matrixStack, final TargetRenderData renderData, final double animationTime,
                              final float width, final boolean useDepthTest, final int accent,
                              final int softAccent, final int hotAccent) {
        final String modeName = this.mode.getValue();
        if ("Orbit".equalsIgnoreCase(modeName)) {
            renderOrbit(matrixStack, renderData, animationTime, width, useDepthTest, accent, softAccent, hotAccent);
            return;
        }
        if ("Rings".equalsIgnoreCase(modeName)) {
            renderRings(matrixStack, renderData, animationTime, width, useDepthTest, accent, softAccent, hotAccent);
            return;
        }
        if ("Pulse".equalsIgnoreCase(modeName)) {
            renderPulse(matrixStack, renderData, animationTime, width, useDepthTest, accent, softAccent, hotAccent);
            return;
        }
        if ("Light".equalsIgnoreCase(modeName) || "Cyclone".equalsIgnoreCase(modeName)) {
            renderLight(matrixStack, renderData, animationTime, width, useDepthTest, accent, softAccent, hotAccent);
            return;
        }
        if ("DNA".equalsIgnoreCase(modeName)) {
            renderDna(matrixStack, renderData, animationTime, width, useDepthTest, accent, softAccent, hotAccent);
            return;
        }
        if ("Ghost".equalsIgnoreCase(modeName) || "Crown".equalsIgnoreCase(modeName)) {
            renderGhost(matrixStack, renderData, animationTime, width, useDepthTest, accent, softAccent, hotAccent);
            return;
        }
        if ("Lightning".equalsIgnoreCase(modeName) || "Prism".equalsIgnoreCase(modeName)) {
            renderLightning(matrixStack, renderData, animationTime, width, useDepthTest, accent, softAccent, hotAccent);
            return;
        }
        if ("Marker".equalsIgnoreCase(modeName) || "Sigil".equalsIgnoreCase(modeName)) {
            renderMarker(matrixStack, renderData, animationTime, width, useDepthTest, accent, softAccent, hotAccent);
            return;
        }
        if ("Satellites".equalsIgnoreCase(modeName)) {
            renderSatellites(matrixStack, renderData, animationTime, width, useDepthTest, accent, softAccent, hotAccent);
            return;
        }
        renderChains(matrixStack, renderData, animationTime, width, useDepthTest, accent, softAccent, hotAccent);
    }

    private void renderChains(final MatrixStack matrixStack, final TargetRenderData renderData, final double animationTime,
                              final float width, final boolean useDepthTest, final int accent,
                              final int softAccent, final int hotAccent) {
        final int chainOuter = ColorUtil.interpolate(accent, hotAccent, 0.62D);
        final int chainInner = ColorUtil.interpolate(softAccent, hotAccent, 0.48D);
        for (int chainIndex = 0; chainIndex < 2; chainIndex++) {
            final double chainPhase = chainIndex * Math.PI;
            final double squeeze = 0.84D + 0.22D * Math.sin(animationTime * 2.45D + chainPhase);
            final double helixBaseRadius = renderData.radius * (0.98D + squeeze * 0.40D);
            final double linkHorizontal = renderData.radius * (0.33D + squeeze * 0.10D);
            final double linkVertical = renderData.height * (0.052D + (1.0D - squeeze) * 0.026D);
            final double rotationSpeed = chainIndex == 0 ? 2.15D : -2.15D;

            for (int linkIndex = 0; linkIndex < 9; linkIndex++) {
                final double progress = (double) linkIndex / 8.0D;
                final double helixAngle = chainPhase + animationTime * rotationSpeed + progress * Math.PI * 2.35D;
                final double orbitRadius = helixBaseRadius * (0.92D + 0.12D * Math.sin(animationTime * 1.9D + progress * 5.0D + chainPhase));
                final double linkCenterX = renderData.center.x + Math.cos(helixAngle) * orbitRadius;
                final double linkCenterY = renderData.baseY + renderData.height * (0.11D + progress * 0.78D)
                    + Math.sin(animationTime * 2.1D + progress * 4.4D + chainPhase) * 0.018D * renderData.height;
                final double linkCenterZ = renderData.center.z + Math.sin(helixAngle) * orbitRadius;
                final double yaw = helixAngle + Math.PI * 0.5D + (linkIndex % 2 == 0 ? 0.0D : Math.PI * 0.16D);

                final List<Vector3d> outerLink = buildChainLink(linkCenterX, linkCenterY, linkCenterZ,
                    linkHorizontal, linkVertical, yaw);
                final List<Vector3d> innerLink = buildChainLink(linkCenterX, linkCenterY, linkCenterZ,
                    linkHorizontal * 0.72D, linkVertical * 0.74D, yaw);
                final double nodeSize = 0.020D + renderData.radius * 0.06D + this.glow.getValue().doubleValue() * 0.016D;
                renderChainGlyph(matrixStack, outerLink, chainOuter, useDepthTest, nodeSize, 0.65D);
                renderChainGlyph(matrixStack, innerLink, chainInner, useDepthTest, nodeSize * 0.72D, 0.46D);
            }
        }

        final List<Vector3d> coreHalo = buildRing(renderData.center.x, renderData.baseY + renderData.height * 0.51D, renderData.center.z,
            renderData.radius * (0.70D + 0.10D * Math.sin(animationTime * 2.3D)), animationTime * 0.95D, 0.015D, 28);
        drawEffectLine(matrixStack, coreHalo, chainOuter, chainInner, Math.max(1.0F, width - 0.8F), useDepthTest, animationTime * 0.22D + 0.19D);
    }

    private void renderOrbit(final MatrixStack matrixStack, final TargetRenderData renderData, final double animationTime,
                             final float width, final boolean useDepthTest, final int accent,
                             final int softAccent, final int hotAccent) {
        final List<Vector3d> firstSpiral = buildSpiral(renderData, animationTime * 2.8D, 1.85D, 0.0D, 0.96D, 64);
        final List<Vector3d> secondSpiral = buildSpiral(renderData, animationTime * 2.8D, 1.85D, Math.PI, 0.96D, 64);
        final List<Vector3d> baseRing = buildRing(renderData.center.x, renderData.baseY + renderData.height * 0.12D, renderData.center.z,
            renderData.radius * 0.98D, animationTime * 1.9D, 0.05D, 46);

        drawEffectLine(matrixStack, firstSpiral, accent, hotAccent, width, useDepthTest, animationTime * 0.7D);
        drawEffectLine(matrixStack, secondSpiral, accent, softAccent, width, useDepthTest, animationTime * 0.7D + 0.32D);
        drawEffectLine(matrixStack, baseRing, softAccent, hotAccent, Math.max(1.1F, width - 0.35F), useDepthTest, animationTime * 0.5D + 0.48D);

        renderOrbitNode(matrixStack, firstSpiral.get(firstSpiral.size() - 1), hotAccent, useDepthTest, animationTime);
        renderOrbitNode(matrixStack, secondSpiral.get(secondSpiral.size() / 2), softAccent, useDepthTest, animationTime + 0.65D);
    }

    private void renderRings(final MatrixStack matrixStack, final TargetRenderData renderData, final double animationTime,
                             final float width, final boolean useDepthTest, final int accent,
                             final int softAccent, final int hotAccent) {
        for (int index = 0; index < 3; index++) {
            final double progress = 0.20D + index * 0.27D;
            final double y = renderData.baseY + renderData.height * progress + Math.sin(animationTime * 3.0D + index * 1.7D) * 0.08D * renderData.height;
            final double radius = renderData.radius * (0.82D + index * 0.12D)
                * (0.94D + 0.10D * Math.sin(animationTime * 2.5D + index * 0.9D));
            final List<Vector3d> ring = buildRing(renderData.center.x, y, renderData.center.z, radius,
                animationTime * (2.0D + index * 0.25D) + index * 0.9D, 0.06D + index * 0.01D, 56);
            final int startColor = index % 2 == 0 ? accent : softAccent;
            final int endColor = index % 2 == 0 ? hotAccent : accent;
            drawEffectLine(matrixStack, ring, startColor, endColor, Math.max(1.0F, width - index * 0.22F), useDepthTest, animationTime * 0.45D + index * 0.2D);
            renderParticleTrail(matrixStack, ring, startColor, endColor, useDepthTest, animationTime + index * 0.23D, 0.055D + index * 0.008D, 4);
        }

        final List<Vector3d> outerHalo = buildRing(renderData.center.x, renderData.baseY + 0.03D, renderData.center.z,
            renderData.radius * 1.22D, -animationTime * 1.45D, 0.0D, 48);
        drawEffectLine(matrixStack, outerHalo, softAccent, hotAccent, Math.max(1.0F, width - 0.55F), useDepthTest, animationTime * 0.35D + 0.76D);
        renderParticleTrail(matrixStack, outerHalo, softAccent, hotAccent, useDepthTest, animationTime + 0.34D, 0.048D, 5);
    }

    private void renderPulse(final MatrixStack matrixStack, final TargetRenderData renderData, final double animationTime,
                             final float width, final boolean useDepthTest, final int accent,
                             final int softAccent, final int hotAccent) {
        final double riseA = animationTime * 0.85D - Math.floor(animationTime * 0.85D);
        final double riseB = animationTime * 0.85D + 0.5D - Math.floor(animationTime * 0.85D + 0.5D);
        renderPulseBand(matrixStack, renderData, riseA, animationTime, width, useDepthTest, accent, hotAccent);
        renderPulseBand(matrixStack, renderData, riseB, animationTime + 0.4D, Math.max(1.0F, width - 0.3F), useDepthTest, softAccent, hotAccent);

        final double shellPulse = 0.85D + 0.15D * Math.sin(animationTime * 4.2D);
        final Vector3d shellCenter = new Vector3d(renderData.center.x, renderData.baseY + renderData.height * 0.48D, renderData.center.z);
        WorldRenderUtil.drawBillboardGlow(
            matrixStack,
            shellCenter,
            (renderData.radius * 0.28D + this.glow.getValue().doubleValue() * 0.05D) * shellPulse,
            ColorUtil.withAlpha(hotAccent, 220),
            ColorUtil.withAlpha(accent, 110),
            useDepthTest
        );
        WorldRenderUtil.drawCircle(
            shellCenter,
            renderData.radius * (0.88D + 0.18D * shellPulse),
            ColorUtil.withAlpha(softAccent, 145),
            Math.max(1.0F, width - 0.4F),
            26,
            useDepthTest
        );
    }

    private void renderLight(final MatrixStack matrixStack, final TargetRenderData renderData, final double animationTime,
                             final float width, final boolean useDepthTest, final int accent,
                             final int softAccent, final int hotAccent) {
        final int orbCore = ColorUtil.withAlpha(ColorUtil.interpolate(softAccent, ColorUtil.rgba(255, 255, 255, 255), 0.34D), 255);
        final int orbHalo = ColorUtil.withAlpha(ColorUtil.interpolate(accent, hotAccent, 0.58D), 150);
        final int arcColor = ColorUtil.withAlpha(ColorUtil.interpolate(accent, softAccent, 0.42D), 220);
        final double orbAngle = animationTime * 1.18D;
        final Vector3d orbCenter = new Vector3d(
            renderData.center.x + Math.cos(orbAngle) * renderData.radius * 0.58D,
            renderData.baseY + renderData.height * (0.56D + 0.10D * Math.sin(animationTime * 0.9D)),
            renderData.center.z + Math.sin(orbAngle) * renderData.radius * 0.58D
        );

        for (int strike = 0; strike < 4; strike++) {
            final double seed = strike * 1.42D;
            final double anchorAngle = orbAngle + seed + Math.sin(animationTime * 0.7D + seed) * 0.45D;
            final Vector3d anchor = new Vector3d(
                renderData.center.x + Math.cos(anchorAngle) * renderData.radius * (0.24D + strike * 0.12D),
                renderData.baseY + renderData.height * (0.18D + strike * 0.17D),
                renderData.center.z + Math.sin(anchorAngle) * renderData.radius * (0.24D + strike * 0.12D)
            );
            final List<Vector3d> bolt = buildLightningPath(anchor, orbCenter, renderData.radius * 0.10D, animationTime, seed, 7);
            drawEffectLine(matrixStack, bolt, arcColor, orbCore, Math.max(1.0F, width - 0.35F), useDepthTest, animationTime * 0.34D + strike * 0.12D);
            renderLightningContacts(matrixStack, bolt, orbHalo, orbCore, useDepthTest, animationTime + seed, 2);
        }

        for (int corona = 0; corona < 5; corona++) {
            final double seed = corona * 0.94D + 0.5D;
            final double startAngle = animationTime * (1.9D + corona * 0.08D) + seed;
            final Vector3d start = new Vector3d(
                orbCenter.x + Math.cos(startAngle) * renderData.radius * 0.18D,
                orbCenter.y + Math.sin(animationTime * 2.3D + seed) * renderData.height * 0.06D,
                orbCenter.z + Math.sin(startAngle) * renderData.radius * 0.18D
            );
            final Vector3d end = new Vector3d(
                orbCenter.x + Math.cos(startAngle + 0.9D) * renderData.radius * 0.28D,
                orbCenter.y + Math.cos(animationTime * 1.8D + seed) * renderData.height * 0.08D,
                orbCenter.z + Math.sin(startAngle + 0.9D) * renderData.radius * 0.28D
            );
            final List<Vector3d> coronaBolt = buildLightningPath(start, end, renderData.radius * 0.05D, animationTime, seed, 5);
            drawEffectLine(matrixStack, coronaBolt, arcColor, hotAccent, Math.max(1.0F, width - 0.7F), useDepthTest, animationTime * 0.46D + corona * 0.08D);
        }

        WorldRenderUtil.drawBillboardGlow(matrixStack, orbCenter, renderData.radius * 0.28D, orbCore, orbHalo, useDepthTest);
        WorldRenderUtil.drawBillboardCloud(
            matrixStack,
            orbCenter,
            renderData.radius * 0.18D,
            ColorUtil.withAlpha(hotAccent, 165),
            ColorUtil.withAlpha(softAccent, 72),
            useDepthTest
        );

        final List<Vector3d> orbitRing = buildRing(orbCenter.x, orbCenter.y, orbCenter.z, renderData.radius * 0.32D, -animationTime * 1.65D, 0.012D, 22);
        drawEffectLine(matrixStack, orbitRing, softAccent, hotAccent, Math.max(1.0F, width - 0.9F), useDepthTest, animationTime * 0.24D);
        renderParticleTrail(matrixStack, orbitRing, softAccent, hotAccent, useDepthTest, animationTime + 0.2D, 0.036D, 3);
    }

    private void renderDna(final MatrixStack matrixStack, final TargetRenderData renderData, final double animationTime,
                           final float width, final boolean useDepthTest, final int accent,
                           final int softAccent, final int hotAccent) {
        final List<Vector3d> firstStrand = buildSpiral(renderData, animationTime * 2.5D, 2.35D, 0.0D, 0.84D, 72);
        final List<Vector3d> secondStrand = buildSpiral(renderData, animationTime * 2.5D, 2.35D, Math.PI, 0.84D, 72);

        drawEffectLine(matrixStack, firstStrand, accent, softAccent, width, useDepthTest, animationTime * 0.72D);
        drawEffectLine(matrixStack, secondStrand, softAccent, hotAccent, width, useDepthTest, animationTime * 0.72D + 0.22D);
        renderParticleTrail(matrixStack, firstStrand, accent, softAccent, useDepthTest, animationTime + 0.10D, 0.048D, 5);
        renderParticleTrail(matrixStack, secondStrand, softAccent, hotAccent, useDepthTest, animationTime + 0.36D, 0.048D, 5);
        for (int index = 6; index < firstStrand.size() - 1; index += 8) {
            drawConnector(matrixStack, firstStrand.get(index), secondStrand.get(index), accent, hotAccent,
                Math.max(1.0F, width - 0.95F), useDepthTest, animationTime * 0.33D + index * 0.03D);
            renderSoftParticleNode(
                matrixStack,
                firstStrand.get(index).add(secondStrand.get(index)).scale(0.5D),
                accent,
                hotAccent,
                useDepthTest,
                animationTime + index * 0.04D,
                0.040D
            );
        }
    }

    private void renderGhost(final MatrixStack matrixStack, final TargetRenderData renderData, final double animationTime,
                             final float width, final boolean useDepthTest, final int accent,
                             final int softAccent, final int hotAccent) {
        final int ghostCore = shadeColor(ColorUtil.interpolate(accent, softAccent, 0.45D), 0.16D, 210);
        final int ghostEdge = ColorUtil.withAlpha(ColorUtil.interpolate(accent, softAccent, 0.60D), 112);
        final int ghostHalo = ColorUtil.withAlpha(ColorUtil.interpolate(softAccent, hotAccent, 0.35D), 92);
        final List<Vector3d> hazeRing = buildRing(
            renderData.center.x,
            renderData.baseY + renderData.height * 0.46D,
            renderData.center.z,
            renderData.radius * 0.82D,
            -animationTime * 0.84D,
            0.035D,
            34
        );
        renderGhostTrail(matrixStack, hazeRing, ghostCore, ghostEdge, ghostHalo, useDepthTest, animationTime + 0.28D);

        for (int orbIndex = 0; orbIndex < 3; orbIndex++) {
            final double seed = orbIndex * 1.91D;
            final double orbitAngle = animationTime * (0.95D + orbIndex * 0.21D)
                + Math.sin(animationTime * (0.54D + orbIndex * 0.12D) + seed) * 1.08D
                + seed;
            final double radius = renderData.radius * (0.70D + 0.18D * Math.sin(animationTime * 0.92D + seed));
            final double heightProgress = 0.28D + 0.16D * orbIndex
                + 0.12D * Math.sin(animationTime * (0.86D + orbIndex * 0.11D) + seed * 0.8D);
            final double wobbleX = Math.sin(animationTime * (1.22D + orbIndex * 0.07D) + seed) * renderData.radius * 0.15D;
            final double wobbleZ = Math.cos(animationTime * (1.08D + orbIndex * 0.09D) + seed * 1.3D) * renderData.radius * 0.15D;
            final Vector3d orb = new Vector3d(
                renderData.center.x + Math.cos(orbitAngle) * radius + wobbleX,
                renderData.baseY + renderData.height * heightProgress,
                renderData.center.z + Math.sin(orbitAngle) * radius + wobbleZ
            );

            final List<Vector3d> trail = buildGhostTrail(renderData, animationTime, orbIndex, 14);
            renderGhostTrail(matrixStack, trail, ghostCore, ghostEdge, ghostHalo, useDepthTest, animationTime + orbIndex * 0.27D);
            renderGhostNode(matrixStack, orb, ghostCore, ghostEdge, ghostHalo, useDepthTest, animationTime + seed);
        }

        final Vector3d chest = new Vector3d(renderData.center.x, renderData.baseY + renderData.height * 0.46D, renderData.center.z);
        final double chestPulse = 0.94D + 0.12D * Math.sin(animationTime * 1.8D);
        WorldRenderUtil.drawBillboardCloud(
            matrixStack,
            chest,
            (renderData.radius * 0.62D + this.glow.getValue().doubleValue() * 0.08D) * chestPulse,
            ghostCore,
            ghostHalo,
            useDepthTest
        );
        WorldRenderUtil.drawBillboardGlow(
            matrixStack,
            chest,
            renderData.radius * 0.22D,
            ColorUtil.withAlpha(ghostEdge, 96),
            ColorUtil.withAlpha(ghostHalo, 44),
            useDepthTest
        );
    }

    private void renderLightning(final MatrixStack matrixStack, final TargetRenderData renderData, final double animationTime,
                                 final float width, final boolean useDepthTest, final int accent,
                                 final int softAccent, final int hotAccent) {
        final int boltColor = ColorUtil.interpolate(accent, hotAccent, 0.58D);
        final int coreColor = ColorUtil.interpolate(softAccent, hotAccent, 0.66D);
        final int flashColor = ColorUtil.interpolate(softAccent, ColorUtil.withAlpha(hotAccent, 255), 0.82D);
        final double flashPulse = 0.60D + 0.40D * Math.sin(animationTime * 8.2D);

        final Vector3d topAnchor = new Vector3d(
            renderData.center.x,
            renderData.baseY + renderData.height * 0.88D,
            renderData.center.z
        );
        final Vector3d bottomAnchor = new Vector3d(
            renderData.center.x,
            renderData.baseY + renderData.height * 0.18D,
            renderData.center.z
        );

        final List<Vector3d> spineBolt = buildLightningPath(topAnchor, bottomAnchor, renderData.radius * 0.72D, animationTime, 0.0D, 9);
        drawEffectLine(matrixStack, spineBolt, boltColor, flashColor, width + 1.35F, useDepthTest, animationTime * 0.43D);
        drawEffectLine(matrixStack, spineBolt, coreColor, flashColor, Math.max(1.0F, width - 0.05F), useDepthTest, animationTime * 0.51D + 0.12D);

        for (int boltIndex = 0; boltIndex < 4; boltIndex++) {
            final double sideSeed = boltIndex * 1.47D + 0.8D;
            final double sideAngle = animationTime * (1.15D + boltIndex * 0.12D) + sideSeed;
            final double lateralRadius = renderData.radius * (0.48D + 0.22D * Math.sin(animationTime * 1.6D + sideSeed));
            final Vector3d sideTop = new Vector3d(
                renderData.center.x + Math.cos(sideAngle) * lateralRadius,
                renderData.baseY + renderData.height * (0.72D + 0.10D * Math.sin(animationTime * 1.7D + sideSeed)),
                renderData.center.z + Math.sin(sideAngle) * lateralRadius
            );
            final Vector3d sideBottom = new Vector3d(
                renderData.center.x + Math.cos(sideAngle + Math.PI * 0.35D) * renderData.radius * 0.26D,
                renderData.baseY + renderData.height * (0.30D + 0.08D * Math.sin(animationTime * 1.3D + sideSeed * 1.6D)),
                renderData.center.z + Math.sin(sideAngle + Math.PI * 0.35D) * renderData.radius * 0.26D
            );
            final List<Vector3d> sideBolt = buildLightningPath(sideTop, sideBottom, renderData.radius * 0.36D, animationTime, sideSeed, 7);
            drawEffectLine(matrixStack, sideBolt, boltColor, coreColor, Math.max(1.0F, width - 0.55F), useDepthTest, animationTime * 0.37D + boltIndex * 0.14D);
            renderLightningContacts(matrixStack, sideBolt, coreColor, flashColor, useDepthTest, animationTime + sideSeed, 2);
        }

        for (int sparkIndex = 0; sparkIndex < 6; sparkIndex++) {
            final List<Vector3d> shockArc = buildShockArc(renderData, animationTime, sparkIndex);
            drawEffectLine(matrixStack, shockArc, boltColor, flashColor, Math.max(1.0F, width - 0.95F), useDepthTest, animationTime * 0.26D + sparkIndex * 0.18D);
        }

        renderLightningContacts(matrixStack, spineBolt, coreColor, flashColor, useDepthTest, animationTime, 4);

        final Vector3d chest = new Vector3d(renderData.center.x, renderData.baseY + renderData.height * 0.52D, renderData.center.z);
        WorldRenderUtil.drawBillboardGlow(
            matrixStack,
            chest,
            (0.090D + this.glow.getValue().doubleValue() * 0.060D) * flashPulse,
            ColorUtil.withAlpha(flashColor, 250),
            ColorUtil.withAlpha(coreColor, 135),
            useDepthTest
        );
        WorldRenderUtil.drawCircle(
            chest,
            renderData.radius * (0.36D + flashPulse * 0.26D),
            ColorUtil.withAlpha(flashColor, 150),
            Math.max(1.2F, width - 0.4F),
            28,
            useDepthTest
        );

        for (int orbIndex = 0; orbIndex < 3; orbIndex++) {
            final Vector3d orb = buildElectricOrb(renderData, animationTime, orbIndex);
            WorldRenderUtil.drawBillboardGlow(
                matrixStack,
                orb,
                (0.075D + this.glow.getValue().doubleValue() * 0.040D) * (0.82D + 0.18D * Math.sin(animationTime * 7.0D + orbIndex)),
                ColorUtil.withAlpha(flashColor, 235),
                ColorUtil.withAlpha(coreColor, 175),
                useDepthTest
            );
        }
    }

    private void renderMarker(final MatrixStack matrixStack, final TargetRenderData renderData, final double animationTime,
                              final float width, final boolean useDepthTest, final int accent,
                              final int softAccent, final int hotAccent) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null || minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }

        final int markerOuter = ColorUtil.interpolate(accent, softAccent, 0.35D);
        final int markerInner = ColorUtil.interpolate(softAccent, hotAccent, 0.55D);
        final double pulse = 0.94D + 0.08D * Math.sin(animationTime * 2.2D);
        final double rotation = animationTime * 1.45D;
        final double halfWidth = renderData.radius * 0.52D * pulse;
        final double halfHeight = renderData.height * 0.115D * pulse;
        final double cornerRadius = Math.min(halfWidth, halfHeight) * 0.52D;
        final double markerY = renderData.baseY + renderData.height * 0.55D;
        final Vector3d planeCenter = new Vector3d(renderData.center.x, markerY, renderData.center.z);
        final Vector3d cameraPosition = minecraft.gameRenderer.getMainCamera().getPosition();
        final Vector3d forward = normalizeOrDefault(cameraPosition.subtract(planeCenter), new Vector3d(0.0D, 0.0D, 1.0D));
        final Vector3d worldUp = new Vector3d(0.0D, 1.0D, 0.0D);
        final Vector3d right = normalizeOrDefault(cross(worldUp, forward), new Vector3d(1.0D, 0.0D, 0.0D));
        final Vector3d up = normalizeOrDefault(cross(forward, right), worldUp);
        final Vector3d offsetCenter = planeCenter.add(forward.scale(renderData.radius * 0.06D));

        final List<List<Vector3d>> corners = new ArrayList<List<Vector3d>>(4);
        corners.add(buildBillboardCorner(offsetCenter, right, up, -1.0D, 1.0D, halfWidth, halfHeight, cornerRadius, rotation, 8));
        corners.add(buildBillboardCorner(offsetCenter, right, up, 1.0D, 1.0D, halfWidth, halfHeight, cornerRadius, rotation, 8));
        corners.add(buildBillboardCorner(offsetCenter, right, up, 1.0D, -1.0D, halfWidth, halfHeight, cornerRadius, rotation, 8));
        corners.add(buildBillboardCorner(offsetCenter, right, up, -1.0D, -1.0D, halfWidth, halfHeight, cornerRadius, rotation, 8));

        for (int index = 0; index < corners.size(); index++) {
            final List<Vector3d> corner = corners.get(index);
            final double phase = animationTime * 0.21D + index * 0.19D;
            drawEffectLine(matrixStack, corner, markerOuter, softAccent, width + 1.15F, useDepthTest, phase);
            drawEffectLine(matrixStack, corner, markerInner, hotAccent, Math.max(1.0F, width - 0.1F), useDepthTest, phase + 0.08D);
        }
    }

    private void renderSatellites(final MatrixStack matrixStack, final TargetRenderData renderData, final double animationTime,
                                  final float width, final boolean useDepthTest, final int accent,
                                  final int softAccent, final int hotAccent) {
        final List<Vector3d> midRing = buildRing(renderData.center.x, renderData.baseY + renderData.height * 0.54D, renderData.center.z,
            renderData.radius * 0.78D, -animationTime * 1.05D, 0.02D, 36);
        drawEffectLine(matrixStack, midRing, accent, softAccent, Math.max(1.0F, width - 0.7F), useDepthTest, animationTime * 0.3D + 0.44D);
        renderParticleTrail(matrixStack, midRing, accent, softAccent, useDepthTest, animationTime + 0.18D, 0.050D, 4);

        for (int index = 0; index < 4; index++) {
            final double orbitAngle = animationTime * (1.5D + index * 0.18D) + index * (Math.PI * 0.5D);
            final double radius = renderData.radius * (0.82D + 0.16D * Math.sin(animationTime * 2.0D + index));
            final double y = renderData.baseY + renderData.height * (0.22D + index * 0.18D)
                + Math.sin(animationTime * 2.6D + index * 1.4D) * 0.08D * renderData.height;
            final Vector3d satellite = new Vector3d(
                renderData.center.x + Math.cos(orbitAngle) * radius,
                y,
                renderData.center.z + Math.sin(orbitAngle) * radius
            );
            final List<Vector3d> trail = buildArc(renderData.center.x, y, renderData.center.z, radius, orbitAngle, 0.90D, 0.04D, 18);
            final int startColor = index % 2 == 0 ? softAccent : accent;
            drawEffectLine(matrixStack, trail, startColor, hotAccent, Math.max(1.0F, width - 0.9F), useDepthTest, animationTime * 0.28D + index * 0.19D);
            renderParticleTrail(matrixStack, trail, startColor, hotAccent, useDepthTest, animationTime + index * 0.27D, 0.040D, 3);
            renderOrbitNode(matrixStack, satellite, hotAccent, useDepthTest, animationTime + index * 0.55D);
        }
    }

    private void renderPulseBand(final MatrixStack matrixStack, final TargetRenderData renderData, final double riseProgress,
                                 final double animationTime, final float width, final boolean useDepthTest,
                                 final int startColor, final int endColor) {
        final double eased = Math.sin(riseProgress * Math.PI);
        final double y = renderData.baseY + renderData.height * riseProgress;
        final double radius = renderData.radius * (0.70D + eased * 0.62D);
        final double wave = 0.02D + eased * 0.05D;
        final List<Vector3d> ring = buildRing(renderData.center.x, y, renderData.center.z, radius, animationTime * 2.2D, wave, 48);
        drawEffectLine(matrixStack, ring, startColor, endColor, width, useDepthTest, animationTime * 0.55D + riseProgress);
        renderParticleTrail(matrixStack, ring, startColor, endColor, useDepthTest, animationTime + riseProgress, 0.046D + eased * 0.018D, 4);
        renderOrbitNode(matrixStack, ring.get(ring.size() / 3), endColor, useDepthTest, animationTime + riseProgress);
    }

    private void renderChainGlyph(final MatrixStack matrixStack, final List<Vector3d> points, final int color,
                                  final boolean useDepthTest, final double size, final double fillStrength) {
        final int glint = ColorUtil.interpolate(color, ColorUtil.rgba(255, 255, 255, 255), 0.34D);
        drawEffectLine(matrixStack, points, ColorUtil.withAlpha(color, 190), ColorUtil.withAlpha(glint, 230), 1.15F, useDepthTest, 0.18D);
        for (int index = 0; index < points.size(); index += 2) {
            final Vector3d point = points.get(index);
            renderSoftParticleNode(matrixStack, point, color, glint, useDepthTest, index * 0.17D, size * (0.72D + fillStrength * 0.30D));
        }
    }

    private void drawEffectLine(final MatrixStack matrixStack, final List<Vector3d> points, final int startColor,
                                final int endColor, final float width, final boolean useDepthTest, final double phase) {
        final double glowStrength = this.glow.getValue().doubleValue();
        final int baseColor = ColorUtil.withAlpha(startColor, 150 + (int) Math.round(glowStrength * 70.0D));
        final int highlightColor = ColorUtil.withAlpha(endColor, 210 + (int) Math.round(glowStrength * 45.0D));
        WorldRenderUtil.drawGradientLineStrip(matrixStack, points, baseColor, highlightColor, width, useDepthTest, phase);
    }

    private void drawConnector(final MatrixStack matrixStack, final Vector3d from, final Vector3d to, final int startColor,
                               final int endColor, final float width, final boolean useDepthTest, final double phase) {
        final List<Vector3d> points = new ArrayList<Vector3d>(2);
        points.add(from);
        points.add(to);
        drawEffectLine(matrixStack, points, startColor, endColor, width, useDepthTest, phase);
    }

    private void renderOrbitNode(final MatrixStack matrixStack, final Vector3d position, final int color,
                                 final boolean useDepthTest, final double animationTime) {
        final int highlight = ColorUtil.interpolate(color, ColorUtil.rgba(255, 255, 255, 255), 0.40D);
        renderSoftParticleNode(matrixStack, position, color, highlight, useDepthTest, animationTime, 0.052D);
    }

    private void renderGhostNode(final MatrixStack matrixStack, final Vector3d position, final int coreColor,
                                 final int edgeColor, final int haloColor, final boolean useDepthTest,
                                 final double animationTime) {
        final double pulse = 0.88D + 0.18D * Math.sin(animationTime * 4.1D);
        final double cloudSize = (0.135D + this.glow.getValue().doubleValue() * 0.070D) * pulse;
        WorldRenderUtil.drawBillboardCloud(
            matrixStack,
            position,
            cloudSize,
            coreColor,
            haloColor,
            useDepthTest
        );
        WorldRenderUtil.drawBillboardCloud(
            matrixStack,
            position.add(0.0D, cloudSize * 0.08D, 0.0D),
            cloudSize * 0.68D,
            edgeColor,
            ColorUtil.multiplyAlpha(haloColor, 0.75D),
            useDepthTest
        );
        WorldRenderUtil.drawBillboardGlow(
            matrixStack,
            position,
            cloudSize * 0.52D,
            ColorUtil.withAlpha(edgeColor, 165),
            ColorUtil.withAlpha(haloColor, 82),
            useDepthTest
        );
    }

    private void renderSoftParticleNode(final MatrixStack matrixStack, final Vector3d position, final int coreColor,
                                        final int edgeColor, final boolean useDepthTest, final double animationTime,
                                        final double baseSize) {
        final double pulse = 0.82D + 0.18D * Math.sin(animationTime * 4.2D);
        final double size = (baseSize + this.glow.getValue().doubleValue() * 0.024D) * pulse;
        WorldRenderUtil.drawBillboardGlow(
            matrixStack,
            position,
            size,
            ColorUtil.withAlpha(edgeColor, 235),
            ColorUtil.withAlpha(coreColor, 105),
            useDepthTest
        );
        WorldRenderUtil.drawBillboardCloud(
            matrixStack,
            position,
            size * 0.82D,
            ColorUtil.withAlpha(coreColor, 150),
            ColorUtil.withAlpha(edgeColor, 58),
            useDepthTest
        );
    }

    private void renderParticleTrail(final MatrixStack matrixStack, final List<Vector3d> points, final int startColor,
                                     final int endColor, final boolean useDepthTest, final double animationTime,
                                     final double baseSize, final int stride) {
        if (points.isEmpty()) {
            return;
        }

        final int lastIndex = points.size() - 1;
        final int step = Math.max(1, stride);
        for (int index = 0; index < points.size(); index += step) {
            final double progress = lastIndex <= 0 ? 1.0D : (double) index / (double) lastIndex;
            final int mixed = ColorUtil.interpolate(startColor, endColor, progress);
            final int highlight = ColorUtil.interpolate(mixed, ColorUtil.rgba(255, 255, 255, 255), 0.26D);
            final Vector3d point = points.get(index);
            renderSoftParticleNode(matrixStack, point, mixed, highlight, useDepthTest, animationTime + index * 0.09D, baseSize);
        }
    }

    private List<Vector3d> buildSpiral(final TargetRenderData renderData, final double angleOffset, final double turns,
                                       final double phaseOffset, final double radiusScale, final int points) {
        final List<Vector3d> path = new ArrayList<Vector3d>(points + 1);
        for (int index = 0; index <= points; index++) {
            final double progress = (double) index / (double) points;
            final double angle = phaseOffset + angleOffset + progress * Math.PI * 2.0D * turns;
            final double radius = renderData.radius * radiusScale * (0.94D + 0.08D * Math.sin(angle * 1.35D));
            path.add(new Vector3d(
                renderData.center.x + Math.cos(angle) * radius,
                renderData.baseY + renderData.height * progress,
                renderData.center.z + Math.sin(angle) * radius
            ));
        }
        return path;
    }

    private List<Vector3d> buildGhostTrail(final TargetRenderData renderData, final double animationTime,
                                           final int orbIndex, final int points) {
        final List<Vector3d> trail = new ArrayList<Vector3d>(points);
        final double timeStep = 0.085D;
        for (int index = points - 1; index >= 0; index--) {
            final double sampledTime = animationTime - index * timeStep;
            final double seed = orbIndex * 1.91D;
            final double orbitAngle = sampledTime * (0.95D + orbIndex * 0.21D)
                + Math.sin(sampledTime * (0.54D + orbIndex * 0.12D) + seed) * 1.08D
                + seed;
            final double radius = renderData.radius * (0.84D + 0.18D * Math.sin(sampledTime * 0.92D + seed));
            final double heightProgress = 0.24D + 0.16D * orbIndex
                + 0.14D * Math.sin(sampledTime * (0.86D + orbIndex * 0.11D) + seed * 0.8D);
            final double wobbleX = Math.sin(sampledTime * (1.22D + orbIndex * 0.07D) + seed) * renderData.radius * 0.16D;
            final double wobbleZ = Math.cos(sampledTime * (1.08D + orbIndex * 0.09D) + seed * 1.3D) * renderData.radius * 0.16D;
            trail.add(new Vector3d(
                renderData.center.x + Math.cos(orbitAngle) * radius + wobbleX,
                renderData.baseY + renderData.height * heightProgress,
                renderData.center.z + Math.sin(orbitAngle) * radius + wobbleZ
            ));
        }
        return trail;
    }

    private void renderGhostTrail(final MatrixStack matrixStack, final List<Vector3d> points,
                                  final int coreColor, final int edgeColor, final int haloColor, final boolean useDepthTest,
                                  final double animationTime) {
        final int lastIndex = points.size() - 1;
        for (int index = 0; index < points.size(); index++) {
            final double progress = lastIndex <= 0 ? 1.0D : (double) index / (double) lastIndex;
            final double pulse = 0.86D + 0.14D * Math.sin(animationTime * 3.6D + index * 0.35D);
            final double size = (0.090D + this.glow.getValue().doubleValue() * 0.040D) * (0.58D + progress * 1.12D) * pulse;
            final int trailCore = ColorUtil.multiplyAlpha(coreColor, 0.20D + progress * 0.54D);
            final int trailEdge = ColorUtil.multiplyAlpha(edgeColor, 0.18D + progress * 0.38D);
            final int trailHalo = ColorUtil.multiplyAlpha(haloColor, 0.12D + progress * 0.32D);
            final Vector3d point = points.get(index);
            WorldRenderUtil.drawBillboardCloud(matrixStack, point, size, trailCore, trailHalo, useDepthTest);
            WorldRenderUtil.drawBillboardGlow(matrixStack, point, size * 0.55D, trailEdge, ColorUtil.multiplyAlpha(trailHalo, 0.72D), useDepthTest);
            if (index % 2 == 0) {
                WorldRenderUtil.drawBillboardCloud(
                    matrixStack,
                    point.add(Math.sin(animationTime + index) * size * 0.12D, size * 0.05D, Math.cos(animationTime + index) * size * 0.12D),
                    size * 0.72D,
                    trailEdge,
                    ColorUtil.multiplyAlpha(trailHalo, 0.85D),
                    useDepthTest
                );
            }
        }
    }

    private void renderLightningContacts(final MatrixStack matrixStack, final List<Vector3d> points,
                                         final int coreColor, final int flashColor, final boolean useDepthTest,
                                         final double animationTime, final int contactCount) {
        if (points.isEmpty()) {
            return;
        }

        for (int index = 0; index < contactCount; index++) {
            final int pointIndex = Math.min(points.size() - 1, Math.max(0, (int) Math.round((points.size() - 1) * ((index + 1.0D) / (contactCount + 1.0D)))));
            final Vector3d point = points.get(pointIndex);
            final double pulse = 0.84D + 0.16D * Math.sin(animationTime * 5.2D + index * 0.8D);
            WorldRenderUtil.drawBillboardGlow(
                matrixStack,
                point,
                (0.040D + this.glow.getValue().doubleValue() * 0.026D) * pulse,
                ColorUtil.withAlpha(flashColor, 240),
                ColorUtil.withAlpha(coreColor, 135),
                useDepthTest
            );
            WorldRenderUtil.drawBillboardGlow(
                matrixStack,
                point,
                (0.075D + this.glow.getValue().doubleValue() * 0.040D) * pulse,
                ColorUtil.withAlpha(flashColor, 255),
                ColorUtil.withAlpha(flashColor, 85),
                useDepthTest
            );
            WorldRenderUtil.drawCircle(
                point,
                (0.085D + this.glow.getValue().doubleValue() * 0.045D) * pulse,
                ColorUtil.withAlpha(flashColor, 170),
                1.3F,
                18,
                useDepthTest
            );
        }
    }

    private List<Vector3d> buildLightningPath(final Vector3d start, final Vector3d end, final double amplitude,
                                              final double animationTime, final double seed, final int segments) {
        final List<Vector3d> path = new ArrayList<Vector3d>(segments + 1);
        final Vector3d direction = end.subtract(start);
        final Vector3d axis = normalizeOrDefault(direction, new Vector3d(0.0D, -1.0D, 0.0D));
        final Vector3d sideA = buildPerpendicular(axis);
        final Vector3d sideB = normalizeOrDefault(cross(axis, sideA), new Vector3d(0.0D, 0.0D, 1.0D));

        for (int index = 0; index <= segments; index++) {
            final double progress = (double) index / (double) segments;
            final Vector3d base = start.add(direction.scale(progress));
            final double envelope = Math.sin(progress * Math.PI);
            final double offsetA = Math.sin(animationTime * 6.4D + seed + progress * 11.0D) * amplitude * envelope;
            final double offsetB = Math.cos(animationTime * 5.1D + seed * 1.7D + progress * 8.0D) * amplitude * 0.58D * envelope;
            path.add(base.add(sideA.scale(offsetA)).add(sideB.scale(offsetB)));
        }
        return path;
    }

    private List<Vector3d> buildShockArc(final TargetRenderData renderData, final double animationTime, final int arcIndex) {
        final double seed = 0.95D + arcIndex * 1.21D;
        final double startAngle = animationTime * (1.9D + arcIndex * 0.08D) + seed;
        final double endAngle = startAngle + 0.35D + 0.65D * Math.sin(animationTime * 1.5D + seed);
        final double startRadius = renderData.radius * (0.55D + 0.22D * Math.sin(seed + animationTime));
        final double endRadius = renderData.radius * (0.20D + 0.14D * Math.cos(animationTime * 1.3D + seed));
        final Vector3d start = new Vector3d(
            renderData.center.x + Math.cos(startAngle) * startRadius,
            renderData.baseY + renderData.height * (0.20D + 0.10D * arcIndex + 0.04D * Math.sin(animationTime * 2.0D + seed)),
            renderData.center.z + Math.sin(startAngle) * startRadius
        );
        final Vector3d end = new Vector3d(
            renderData.center.x + Math.cos(endAngle) * endRadius,
            renderData.baseY + renderData.height * (0.30D + 0.08D * Math.sin(animationTime * 1.8D + seed * 1.2D)),
            renderData.center.z + Math.sin(endAngle) * endRadius
        );
        return buildLightningPath(start, end, renderData.radius * 0.14D, animationTime, seed, 5);
    }

    private Vector3d buildElectricOrb(final TargetRenderData renderData, final double animationTime, final int orbIndex) {
        final double seed = orbIndex * 2.03D;
        final double angle = animationTime * (2.5D + orbIndex * 0.22D) + seed;
        final double radius = renderData.radius * (0.30D + 0.10D * Math.sin(animationTime * 2.2D + seed));
        return new Vector3d(
            renderData.center.x + Math.cos(angle) * radius,
            renderData.baseY + renderData.height * (0.34D + 0.18D * orbIndex + 0.08D * Math.sin(animationTime * 2.8D + seed)),
            renderData.center.z + Math.sin(angle) * radius
        );
    }

    private List<Vector3d> buildCyclone(final TargetRenderData renderData, final double angleOffset, final double turns,
                                        final double baseRadiusScale, final double topRadiusScale, final int points) {
        final List<Vector3d> path = new ArrayList<Vector3d>(points + 1);
        for (int index = 0; index <= points; index++) {
            final double progress = (double) index / (double) points;
            final double angle = angleOffset + progress * Math.PI * 2.0D * turns;
            final double radiusScale = baseRadiusScale + (topRadiusScale - baseRadiusScale) * progress;
            final double radius = renderData.radius * radiusScale * (0.92D + 0.08D * Math.sin(angle * 1.25D + progress * 4.0D));
            path.add(new Vector3d(
                renderData.center.x + Math.cos(angle) * radius,
                renderData.baseY + renderData.height * progress,
                renderData.center.z + Math.sin(angle) * radius
            ));
        }
        return path;
    }

    private List<Vector3d> buildRing(final double centerX, final double centerY, final double centerZ,
                                     final double radius, final double angleOffset, final double wave,
                                     final int segments) {
        final List<Vector3d> ring = new ArrayList<Vector3d>(segments + 1);
        for (int index = 0; index <= segments; index++) {
            final double progress = (double) index / (double) segments;
            final double angle = angleOffset + progress * Math.PI * 2.0D;
            ring.add(new Vector3d(
                centerX + Math.cos(angle) * radius,
                centerY + Math.sin(angle * 2.0D) * wave,
                centerZ + Math.sin(angle) * radius
            ));
        }
        return ring;
    }

    private List<Vector3d> buildVerticalLoop(final double centerX, final double centerY, final double centerZ,
                                             final double horizontalRadius, final double verticalRadius,
                                             final double yaw, final int segments) {
        final List<Vector3d> loop = new ArrayList<Vector3d>(segments + 1);
        final double cosYaw = Math.cos(yaw);
        final double sinYaw = Math.sin(yaw);
        for (int index = 0; index <= segments; index++) {
            final double progress = (double) index / (double) segments;
            final double angle = progress * Math.PI * 2.0D;
            final double planar = Math.cos(angle) * horizontalRadius;
            loop.add(new Vector3d(
                centerX + planar * cosYaw,
                centerY + Math.sin(angle) * verticalRadius,
                centerZ + planar * sinYaw
            ));
        }
        return loop;
    }

    private List<Vector3d> buildChainLink(final double centerX, final double centerY, final double centerZ,
                                          final double horizontalRadius, final double verticalRadius,
                                          final double yaw) {
        final List<Vector3d> link = new ArrayList<Vector3d>(11);
        final double[] horizontalPoints = new double[]{0.0D, 0.52D, 0.96D, 0.96D, 0.52D, 0.0D, -0.52D, -0.96D, -0.96D, -0.52D, 0.0D};
        final double[] verticalPoints = new double[]{1.0D, 0.80D, 0.38D, -0.38D, -0.80D, -1.0D, -0.80D, -0.38D, 0.38D, 0.80D, 1.0D};
        final double cosYaw = Math.cos(yaw);
        final double sinYaw = Math.sin(yaw);

        for (int index = 0; index < horizontalPoints.length; index++) {
            final double planar = horizontalPoints[index] * horizontalRadius;
            link.add(new Vector3d(
                centerX + planar * cosYaw,
                centerY + verticalPoints[index] * verticalRadius,
                centerZ + planar * sinYaw
            ));
        }
        return link;
    }

    private List<Vector3d> buildVerticalArc(final double centerX, final double centerY, final double centerZ,
                                            final double horizontalRadius, final double verticalRadius,
                                            final double yaw, final double startAngle, final double arcSpan,
                                            final int segments) {
        final List<Vector3d> arc = new ArrayList<Vector3d>(segments + 1);
        final double cosYaw = Math.cos(yaw);
        final double sinYaw = Math.sin(yaw);
        for (int index = 0; index <= segments; index++) {
            final double progress = (double) index / (double) segments;
            final double angle = startAngle + progress * arcSpan;
            final double planar = Math.cos(angle) * horizontalRadius;
            arc.add(new Vector3d(
                centerX + planar * cosYaw,
                centerY + Math.sin(angle) * verticalRadius,
                centerZ + planar * sinYaw
            ));
        }
        return arc;
    }

    private List<Vector3d> buildBillboardCorner(final Vector3d center, final Vector3d right, final Vector3d up,
                                                final double signX, final double signY, final double halfWidth,
                                                final double halfHeight, final double cornerRadius,
                                                final double rotation, final int segments) {
        final List<Vector3d> corner = new ArrayList<Vector3d>(segments + 1);
        final double arcCenterX = signX * (halfWidth - cornerRadius);
        final double arcCenterY = signY * (halfHeight - cornerRadius);

        final double startAngle;
        final double endAngle;
        if (signX < 0.0D && signY > 0.0D) {
            startAngle = Math.PI * 0.5D;
            endAngle = Math.PI;
        } else if (signX > 0.0D && signY > 0.0D) {
            startAngle = Math.PI * 0.5D;
            endAngle = 0.0D;
        } else if (signX > 0.0D) {
            startAngle = Math.PI * 1.5D;
            endAngle = Math.PI * 2.0D;
        } else {
            startAngle = Math.PI * 1.5D;
            endAngle = Math.PI;
        }

        for (int index = 0; index <= segments; index++) {
            final double progress = (double) index / (double) segments;
            final double angle = startAngle + (endAngle - startAngle) * progress;
            addBillboardPoint(
                corner,
                center,
                right,
                up,
                arcCenterX + Math.cos(angle) * cornerRadius,
                arcCenterY + Math.sin(angle) * cornerRadius,
                rotation
            );
        }
        return corner;
    }

    private void addBillboardPoint(final List<Vector3d> points, final Vector3d center, final Vector3d right,
                                   final Vector3d up, final double localX, final double localY,
                                   final double rotation) {
        final double cos = Math.cos(rotation);
        final double sin = Math.sin(rotation);
        final double rotatedX = localX * cos - localY * sin;
        final double rotatedY = localX * sin + localY * cos;
        points.add(center.add(right.scale(rotatedX)).add(up.scale(rotatedY)));
    }

    private Vector3d normalizeOrDefault(final Vector3d vector, final Vector3d fallback) {
        final double lengthSquared = vector.x * vector.x + vector.y * vector.y + vector.z * vector.z;
        if (lengthSquared < 1.0E-6D) {
            return fallback;
        }
        final double inverseLength = 1.0D / Math.sqrt(lengthSquared);
        return new Vector3d(vector.x * inverseLength, vector.y * inverseLength, vector.z * inverseLength);
    }

    private Vector3d cross(final Vector3d first, final Vector3d second) {
        return new Vector3d(
            first.y * second.z - first.z * second.y,
            first.z * second.x - first.x * second.z,
            first.x * second.y - first.y * second.x
        );
    }

    private int shadeColor(final int color, final double multiplier, final int alpha) {
        final int red = (int) Math.round((color >>> 16 & 255) * multiplier);
        final int green = (int) Math.round((color >>> 8 & 255) * multiplier);
        final int blue = (int) Math.round((color & 255) * multiplier);
        return ColorUtil.rgba(red, green, blue, alpha);
    }

    private Vector3d buildPerpendicular(final Vector3d axis) {
        final Vector3d up = Math.abs(axis.y) < 0.92D ? new Vector3d(0.0D, 1.0D, 0.0D) : new Vector3d(1.0D, 0.0D, 0.0D);
        return normalizeOrDefault(cross(axis, up), new Vector3d(1.0D, 0.0D, 0.0D));
    }

    private List<Vector3d> buildArc(final double centerX, final double centerY, final double centerZ,
                                    final double radius, final double startAngle, final double arcSpan,
                                    final double wave, final int segments) {
        final List<Vector3d> arc = new ArrayList<Vector3d>(segments + 1);
        for (int index = 0; index <= segments; index++) {
            final double progress = (double) index / (double) segments;
            final double angle = startAngle - progress * arcSpan;
            arc.add(new Vector3d(
                centerX + Math.cos(angle) * radius,
                centerY + Math.sin(progress * Math.PI) * wave,
                centerZ + Math.sin(angle) * radius
            ));
        }
        return arc;
    }

    private List<Vector3d> buildPolygon(final double centerX, final double centerY, final double centerZ,
                                        final double radius, final int sides, final double angleOffset) {
        final List<Vector3d> polygon = new ArrayList<Vector3d>(sides + 1);
        for (int index = 0; index <= sides; index++) {
            final double progress = (double) index / (double) sides;
            final double angle = angleOffset + progress * Math.PI * 2.0D;
            polygon.add(new Vector3d(
                centerX + Math.cos(angle) * radius,
                centerY,
                centerZ + Math.sin(angle) * radius
            ));
        }
        return polygon;
    }

    private List<Vector3d> buildStar(final double centerX, final double centerY, final double centerZ,
                                     final double outerRadius, final double innerRadius,
                                     final int points, final double angleOffset) {
        final List<Vector3d> star = new ArrayList<Vector3d>(points * 2 + 1);
        for (int index = 0; index <= points * 2; index++) {
            final double angle = angleOffset + index * Math.PI / points;
            final double radius = index % 2 == 0 ? outerRadius : innerRadius;
            star.add(new Vector3d(
                centerX + Math.cos(angle) * radius,
                centerY,
                centerZ + Math.sin(angle) * radius
            ));
        }
        return star;
    }

    private TargetRenderData buildRenderData(final LivingEntity target, final float partialTicks, final double scale) {
        final double renderX = target.xo + (target.getX() - target.xo) * partialTicks;
        final double renderY = target.yo + (target.getY() - target.yo) * partialTicks;
        final double renderZ = target.zo + (target.getZ() - target.zo) * partialTicks;
        final AxisAlignedBB box = target.getBoundingBox().move(renderX - target.getX(), renderY - target.getY(), renderZ - target.getZ());
        final double radius = Math.max(0.34D, target.getBbWidth() * 0.72D) * scale;
        final double height = Math.max(0.9D, box.maxY - box.minY) + 0.12D * scale;
        return new TargetRenderData(
            new Vector3d((box.minX + box.maxX) * 0.5D, box.minY, (box.minZ + box.maxZ) * 0.5D),
            box.minY,
            height,
            radius,
            box
        );
    }

    private static final class TargetRenderData {
        private final Vector3d center;
        private final double baseY;
        private final double height;
        private final double radius;
        private final AxisAlignedBB boundingBox;

        private TargetRenderData(final Vector3d center, final double baseY, final double height,
                                 final double radius, final AxisAlignedBB boundingBox) {
            this.center = center;
            this.baseY = baseY;
            this.height = height;
            this.radius = radius;
            this.boundingBox = boundingBox;
        }
    }
}
