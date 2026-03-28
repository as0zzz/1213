package dev.mdk2.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.util.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.List;

public final class WorldRenderUtil {
    private WorldRenderUtil() {
    }

    public static void drawFilledOutlinedBox(final AxisAlignedBB worldBox, final int fillColor, final int outlineColor, final float lineWidth) {
        drawFilledOutlinedBox(new MatrixStack(), worldBox, fillColor, outlineColor, lineWidth, false);
    }

    public static void drawFilledOutlinedBox(final AxisAlignedBB worldBox, final int fillColor, final int outlineColor,
                                             final float lineWidth, final boolean depthTest) {
        drawFilledOutlinedBox(new MatrixStack(), worldBox, fillColor, outlineColor, lineWidth, depthTest);
    }

    public static void drawFilledOutlinedBox(final MatrixStack matrixStack, final AxisAlignedBB worldBox, final int fillColor,
                                             final int outlineColor, final float lineWidth, final boolean depthTest) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null || minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }

        final Vector3d camera = minecraft.gameRenderer.getMainCamera().getPosition();
        matrixStack.pushPose();
        matrixStack.translate(-camera.x, -camera.y, -camera.z);

        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrixStack.last().pose());
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        if (!depthTest) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);

        drawFilledBox(matrixStack, worldBox, fillColor);
        drawOutlineBox(worldBox, outlineColor, lineWidth);

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.popMatrix();
        matrixStack.popPose();
    }

    public static void drawTracer(final Vector3d fromWorld, final Vector3d toWorld, final int color, final float lineWidth) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null || minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }

        final Vector3d camera = minecraft.gameRenderer.getMainCamera().getPosition();
        final Vector3d from = fromWorld.subtract(camera);
        final Vector3d to = toWorld.subtract(camera);

        RenderSystem.pushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glLineWidth(lineWidth);

        applyColor(color);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(from.x, from.y, from.z);
        GL11.glVertex3d(to.x, to.y, to.z);
        GL11.glEnd();

        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.popMatrix();
    }

    public static void drawLineStrip(final List<Vector3d> worldPoints, final int color, final float lineWidth) {
        drawLineStrip(worldPoints, color, lineWidth, false);
    }

    public static void drawLineStrip(final List<Vector3d> worldPoints, final int color, final float lineWidth, final boolean depthTest) {
        drawLineStrip(new MatrixStack(), worldPoints, color, lineWidth, depthTest);
    }

    public static void drawLineStrip(final MatrixStack matrixStack, final List<Vector3d> worldPoints, final int color,
                                     final float lineWidth, final boolean depthTest) {
        drawGradientLineStrip(matrixStack, worldPoints, color, color, lineWidth, depthTest, 0.0D);
    }

    public static void drawGradientLineStrip(final MatrixStack matrixStack, final List<Vector3d> worldPoints, final int baseColor,
                                             final int highlightColor, final float lineWidth, final boolean depthTest,
                                             final double animationTime) {
        if (worldPoints == null || worldPoints.size() < 2) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null || minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }

        final Vector3d camera = minecraft.gameRenderer.getMainCamera().getPosition();
        final double phase = animationTime - Math.floor(animationTime);
        drawGradientLineStripPass(matrixStack, worldPoints, camera, baseColor, highlightColor, lineWidth + 2.25F, depthTest, phase, 0.26D, 0.55D);
        drawGradientLineStripPass(matrixStack, worldPoints, camera, baseColor, highlightColor, Math.max(1.0F, lineWidth), depthTest, phase, 0.72D, 1.0D);
    }

    public static void drawCircle(final Vector3d centerWorld, final double radius, final int color,
                                  final float lineWidth, final int segments) {
        drawCircle(centerWorld, radius, color, lineWidth, segments, false);
    }

    public static void drawCircle(final Vector3d centerWorld, final double radius, final int color,
                                  final float lineWidth, final int segments, final boolean depthTest) {
        if (segments < 8) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null || minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }

        final Vector3d camera = minecraft.gameRenderer.getMainCamera().getPosition();

        RenderSystem.pushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        if (!depthTest) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glLineWidth(lineWidth);

        applyColor(color);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int index = 0; index < segments; index++) {
            final double angle = Math.PI * 2.0D * index / segments;
            final double x = centerWorld.x + Math.cos(angle) * radius - camera.x;
            final double z = centerWorld.z + Math.sin(angle) * radius - camera.z;
            GL11.glVertex3d(x, centerWorld.y - camera.y, z);
        }
        GL11.glEnd();

        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.popMatrix();
    }

    public static void drawBillboardGlow(final MatrixStack matrixStack, final Vector3d worldCenter, final double radius,
                                         final int innerColor, final int outerColor, final boolean depthTest) {
        drawBillboardAura(matrixStack, worldCenter, radius, innerColor, outerColor, depthTest, true);
    }

    public static void drawBillboardCloud(final MatrixStack matrixStack, final Vector3d worldCenter, final double radius,
                                          final int innerColor, final int outerColor, final boolean depthTest) {
        drawBillboardAura(matrixStack, worldCenter, radius, innerColor, outerColor, depthTest, false);
    }

    private static void drawBillboardAura(final MatrixStack matrixStack, final Vector3d worldCenter, final double radius,
                                          final int innerColor, final int outerColor, final boolean depthTest,
                                          final boolean additiveBlend) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null || minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }

        final Vector3d camera = minecraft.gameRenderer.getMainCamera().getPosition();
        final Vector3d toCamera = normalize(camera.subtract(worldCenter), new Vector3d(0.0D, 0.0D, 1.0D));
        final Vector3d worldUp = new Vector3d(0.0D, 1.0D, 0.0D);
        final Vector3d right = normalize(cross(worldUp, toCamera), new Vector3d(1.0D, 0.0D, 0.0D));
        final Vector3d up = normalize(cross(toCamera, right), worldUp);

        matrixStack.pushPose();
        matrixStack.translate(-camera.x, -camera.y, -camera.z);

        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrixStack.last().pose());
        RenderSystem.disableTexture();
        if (!depthTest) {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, additiveBlend ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();

        drawBillboardQuad(worldCenter, right, up, radius * 2.2D, ColorUtil.multiplyAlpha(outerColor, 0.12D));
        drawBillboardQuad(worldCenter, right, up, radius * 1.5D, ColorUtil.multiplyAlpha(outerColor, 0.28D));
        drawBillboardQuad(worldCenter, right, up, radius, ColorUtil.multiplyAlpha(innerColor, 0.95D));

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.popMatrix();
        matrixStack.popPose();
    }

    public static void drawBufferedBox(final MatrixStack matrixStack, final IRenderTypeBuffer buffer,
                                       final AxisAlignedBB worldBox, final int fillColor, final int outlineColor) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null || minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }

        final Vector3d camera = minecraft.gameRenderer.getMainCamera().getPosition();
        final float lineRed = (outlineColor >>> 16 & 255) / 255.0F;
        final float lineGreen = (outlineColor >>> 8 & 255) / 255.0F;
        final float lineBlue = (outlineColor & 255) / 255.0F;
        final float lineAlpha = (outlineColor >>> 24 & 255) / 255.0F;

        matrixStack.pushPose();
        matrixStack.translate(-camera.x, -camera.y, -camera.z);
        drawFilledBox(matrixStack, worldBox, fillColor);
        WorldRenderer.renderLineBox(
            matrixStack,
            buffer.getBuffer(net.minecraft.client.renderer.RenderType.lines()),
            worldBox.minX,
            worldBox.minY,
            worldBox.minZ,
            worldBox.maxX,
            worldBox.maxY,
            worldBox.maxZ,
            lineRed,
            lineGreen,
            lineBlue,
            lineAlpha
        );
        matrixStack.popPose();
    }

    private static void drawFilledBox(final MatrixStack matrixStack, final AxisAlignedBB box, final int color) {
        final float alpha = (color >>> 24 & 255) / 255.0F;
        final float red = (color >>> 16 & 255) / 255.0F;
        final float green = (color >>> 8 & 255) / 255.0F;
        final float blue = (color & 255) / 255.0F;
        final Matrix4f pose = matrixStack.last().pose();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder builder = tessellator.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        vertex(builder, pose, box.minX, box.minY, box.minZ, red, green, blue, alpha);
        vertex(builder, pose, box.maxX, box.minY, box.minZ, red, green, blue, alpha);
        vertex(builder, pose, box.maxX, box.maxY, box.minZ, red, green, blue, alpha);
        vertex(builder, pose, box.minX, box.maxY, box.minZ, red, green, blue, alpha);

        vertex(builder, pose, box.minX, box.minY, box.maxZ, red, green, blue, alpha);
        vertex(builder, pose, box.maxX, box.minY, box.maxZ, red, green, blue, alpha);
        vertex(builder, pose, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
        vertex(builder, pose, box.minX, box.maxY, box.maxZ, red, green, blue, alpha);

        vertex(builder, pose, box.minX, box.minY, box.minZ, red, green, blue, alpha);
        vertex(builder, pose, box.minX, box.minY, box.maxZ, red, green, blue, alpha);
        vertex(builder, pose, box.minX, box.maxY, box.maxZ, red, green, blue, alpha);
        vertex(builder, pose, box.minX, box.maxY, box.minZ, red, green, blue, alpha);

        vertex(builder, pose, box.maxX, box.minY, box.minZ, red, green, blue, alpha);
        vertex(builder, pose, box.maxX, box.minY, box.maxZ, red, green, blue, alpha);
        vertex(builder, pose, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
        vertex(builder, pose, box.maxX, box.maxY, box.minZ, red, green, blue, alpha);

        vertex(builder, pose, box.minX, box.maxY, box.minZ, red, green, blue, alpha);
        vertex(builder, pose, box.maxX, box.maxY, box.minZ, red, green, blue, alpha);
        vertex(builder, pose, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
        vertex(builder, pose, box.minX, box.maxY, box.maxZ, red, green, blue, alpha);

        vertex(builder, pose, box.minX, box.minY, box.minZ, red, green, blue, alpha);
        vertex(builder, pose, box.maxX, box.minY, box.minZ, red, green, blue, alpha);
        vertex(builder, pose, box.maxX, box.minY, box.maxZ, red, green, blue, alpha);
        vertex(builder, pose, box.minX, box.minY, box.maxZ, red, green, blue, alpha);

        tessellator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void vertex(final BufferBuilder builder, final Matrix4f pose,
                               final double x, final double y, final double z,
                               final float red, final float green, final float blue, final float alpha) {
        builder.vertex(pose, (float) x, (float) y, (float) z).color(red, green, blue, alpha).endVertex();
    }

    private static void drawOutlineBox(final AxisAlignedBB box, final int color, final float lineWidth) {
        GL11.glLineWidth(lineWidth);
        applyColor(color);
        GL11.glBegin(GL11.GL_LINES);

        edge(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ);
        edge(box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
        edge(box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ);
        edge(box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ);

        edge(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ);
        edge(box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
        edge(box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ);
        edge(box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ);

        edge(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ);
        edge(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
        edge(box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
        edge(box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ);

        GL11.glEnd();
        GL11.glLineWidth(1.0F);
    }

    private static void edge(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y2, z2);
    }

    private static void applyColor(final int color) {
        final float alpha = (color >>> 24 & 255) / 255.0F;
        final float red = (color >>> 16 & 255) / 255.0F;
        final float green = (color >>> 8 & 255) / 255.0F;
        final float blue = (color & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    private static void drawBillboardQuad(final Vector3d center, final Vector3d right, final Vector3d up,
                                          final double radius, final int color) {
        final float alpha = (color >>> 24 & 255) / 255.0F;
        final float red = (color >>> 16 & 255) / 255.0F;
        final float green = (color >>> 8 & 255) / 255.0F;
        final float blue = (color & 255) / 255.0F;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder builder = tessellator.getBuilder();
        final int segments = 18;

        builder.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        builder.vertex(center.x, center.y, center.z)
            .color(red, green, blue, alpha).endVertex();
        for (int index = 0; index <= segments; index++) {
            final double angle = Math.PI * 2.0D * index / segments;
            final double offsetX = Math.cos(angle) * radius;
            final double offsetY = Math.sin(angle) * radius;
            final Vector3d point = center.add(right.scale(offsetX)).add(up.scale(offsetY));
            builder.vertex(point.x, point.y, point.z)
                .color(red, green, blue, 0.0F).endVertex();
        }
        tessellator.end();
    }

    private static Vector3d normalize(final Vector3d vector, final Vector3d fallback) {
        final double lengthSquared = vector.x * vector.x + vector.y * vector.y + vector.z * vector.z;
        if (lengthSquared < 1.0E-6D) {
            return fallback;
        }
        final double inverseLength = 1.0D / Math.sqrt(lengthSquared);
        return new Vector3d(vector.x * inverseLength, vector.y * inverseLength, vector.z * inverseLength);
    }

    private static Vector3d cross(final Vector3d first, final Vector3d second) {
        return new Vector3d(
            first.y * second.z - first.z * second.y,
            first.z * second.x - first.x * second.z,
            first.x * second.y - first.y * second.x
        );
    }

    private static void drawGradientLineStripPass(final MatrixStack matrixStack, final List<Vector3d> worldPoints, final Vector3d camera,
                                                  final int baseColor, final int highlightColor, final float lineWidth,
                                                  final boolean depthTest, final double phase, final double alphaScale,
                                                  final double shimmerStrength) {
        matrixStack.pushPose();
        matrixStack.translate(-camera.x, -camera.y, -camera.z);

        RenderSystem.pushMatrix();
        RenderSystem.multMatrix(matrixStack.last().pose());
        RenderSystem.disableTexture();
        if (!depthTest) {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        GL11.glLineWidth(lineWidth);

        GL11.glBegin(GL11.GL_LINE_STRIP);
        final int lastIndex = worldPoints.size() - 1;
        for (int index = 0; index < worldPoints.size(); index++) {
            final Vector3d point = worldPoints.get(index);
            final double progress = lastIndex <= 0 ? 1.0D : (double) index / (double) lastIndex;
            final double distanceToPhase = Math.abs(progress - phase);
            final double wrappedDistance = Math.min(distanceToPhase, 1.0D - distanceToPhase);
            final double shimmer = Math.max(0.0D, 1.0D - wrappedDistance * 5.0D);
            final int mixedColor = ColorUtil.interpolate(baseColor, highlightColor, shimmer * shimmerStrength);
            final double alphaMultiplier = Math.min(1.0D, (0.28D + progress * 0.72D + shimmer * 0.45D) * alphaScale);
            applyVertexColor(ColorUtil.multiplyAlpha(mixedColor, alphaMultiplier));
            GL11.glVertex3d(point.x, point.y, point.z);
        }
        GL11.glEnd();

        GL11.glLineWidth(1.0F);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.popMatrix();
        matrixStack.popPose();
    }

    private static void applyVertexColor(final int color) {
        final float alpha = (color >>> 24 & 255) / 255.0F;
        final float red = (color >>> 16 & 255) / 255.0F;
        final float green = (color >>> 8 & 255) / 255.0F;
        final float blue = (color & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }
}
