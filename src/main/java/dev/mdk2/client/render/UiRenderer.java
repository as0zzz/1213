package dev.mdk2.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.util.ColorUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;
import java.util.Deque;

public final class UiRenderer {
    private static final Deque<int[]> SCISSOR_STACK = new ArrayDeque<int[]>();

    private UiRenderer() {
    }

    public static void drawRect(final double x, final double y, final double width, final double height, final int color) {
        enableShapeState();
        applyColor(color);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();
        disableShapeState();
    }

    public static void drawGradientRect(final double x, final double y, final double width, final double height,
                                        final int topLeft, final int topRight, final int bottomRight, final int bottomLeft) {
        enableShapeState();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);
        applyColor(topLeft);
        GL11.glVertex2d(x, y);
        applyColor(topRight);
        GL11.glVertex2d(x + width, y);
        applyColor(bottomRight);
        GL11.glVertex2d(x + width, y + height);
        applyColor(bottomLeft);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        disableShapeState();
    }

    public static void drawRoundedRect(final double x, final double y, final double width, final double height, final double radius, final int color) {
        enableShapeState();
        applyColor(color);
        drawRoundedPolygon(x, y, width, height, radius, 18, false);
        disableShapeState();
    }

    public static void drawRoundedOutline(final double x, final double y, final double width, final double height,
                                          final double radius, final double lineWidth, final int color) {
        enableShapeState();
        GL11.glLineWidth((float) lineWidth);
        applyColor(color);
        drawRoundedPolygon(x, y, width, height, radius, 22, true);
        GL11.glLineWidth(1.0F);
        disableShapeState();
    }

    public static void drawCircle(final double x, final double y, final double radius, final int color) {
        enableShapeState();
        applyColor(color);
        GL11.glBegin(GL11.GL_POLYGON);
        for (int i = 0; i <= 32; i++) {
            final double angle = Math.PI * 2.0D * i / 32.0D;
            GL11.glVertex2d(x + Math.cos(angle) * radius, y + Math.sin(angle) * radius);
        }
        GL11.glEnd();
        disableShapeState();
    }

    public static void drawLine(final double startX, final double startY, final double endX, final double endY, final double lineWidth, final int color) {
        enableShapeState();
        GL11.glLineWidth((float) lineWidth);
        applyColor(color);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(startX, startY);
        GL11.glVertex2d(endX, endY);
        GL11.glEnd();
        GL11.glLineWidth(1.0F);
        disableShapeState();
    }

    public static void drawShadow(final double x, final double y, final double width, final double height, final int spread, final int color) {
        for (int i = spread; i >= 1; i--) {
            final double progress = i / (double) spread;
            drawRoundedRect(
                x - i,
                y - i,
                width + i * 2.0D,
                height + i * 2.0D,
                12.0D + i * 0.35D,
                ColorUtil.multiplyAlpha(color, progress * 0.34D)
            );
        }
    }

    public static void drawText(final MatrixStack matrixStack, final String text, final float x, final float y, final int color) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().font.draw(matrixStack, text, x, y, color);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void drawCenteredText(final MatrixStack matrixStack, final String text, final float centerX, final float y, final int color) {
        final int width = Minecraft.getInstance().font.width(text);
        drawText(matrixStack, text, centerX - width / 2.0F, y, color);
    }

    public static void scissorStart(final double x, final double y, final double width, final double height) {
        final MainWindow window = Minecraft.getInstance().getWindow();
        final double scaleX = window.getWidth() / (double) window.getGuiScaledWidth();
        final double scaleY = window.getHeight() / (double) window.getGuiScaledHeight();

        final int rawX = (int) Math.round(x * scaleX);
        final int rawY = (int) Math.round(window.getHeight() - (y + height) * scaleY);
        final int rawWidth = Math.max(0, (int) Math.round(width * scaleX));
        final int rawHeight = Math.max(0, (int) Math.round(height * scaleY));

        int clipX = rawX;
        int clipY = rawY;
        int clipWidth = rawWidth;
        int clipHeight = rawHeight;

        if (!SCISSOR_STACK.isEmpty()) {
            final int[] current = SCISSOR_STACK.peek();
            final int x2 = Math.min(clipX + clipWidth, current[0] + current[2]);
            final int y2 = Math.min(clipY + clipHeight, current[1] + current[3]);
            clipX = Math.max(clipX, current[0]);
            clipY = Math.max(clipY, current[1]);
            clipWidth = Math.max(0, x2 - clipX);
            clipHeight = Math.max(0, y2 - clipY);
        }

        SCISSOR_STACK.push(new int[]{clipX, clipY, clipWidth, clipHeight});
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(clipX, clipY, clipWidth, clipHeight);
    }

    public static void scissorEnd() {
        if (SCISSOR_STACK.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            return;
        }

        SCISSOR_STACK.pop();
        if (SCISSOR_STACK.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            return;
        }

        final int[] current = SCISSOR_STACK.peek();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(current[0], current[1], current[2], current[3]);
    }

    public static void push() {
        GL11.glPushMatrix();
    }

    public static void pop() {
        GL11.glPopMatrix();
    }

    public static void translate(final double x, final double y, final double z) {
        GL11.glTranslated(x, y, z);
    }

    public static void scale(final double x, final double y, final double z) {
        GL11.glScaled(x, y, z);
    }

    private static void drawRoundedPolygon(final double x, final double y, final double width, final double height,
                                           final double radius, final int samples, final boolean outline) {
        final double safeRadius = Math.max(0.0D, Math.min(radius, Math.min(width, height) / 2.0D));

        GL11.glBegin(outline ? GL11.GL_LINE_LOOP : GL11.GL_POLYGON);
        emitArc(x + width - safeRadius, y + safeRadius, safeRadius, -90.0D, 0.0D, samples);
        emitArc(x + width - safeRadius, y + height - safeRadius, safeRadius, 0.0D, 90.0D, samples);
        emitArc(x + safeRadius, y + height - safeRadius, safeRadius, 90.0D, 180.0D, samples);
        emitArc(x + safeRadius, y + safeRadius, safeRadius, 180.0D, 270.0D, samples);
        GL11.glEnd();
    }

    private static void emitArc(final double centerX, final double centerY, final double radius,
                                final double startAngle, final double endAngle, final int samples) {
        if (radius <= 0.0D) {
            GL11.glVertex2d(centerX, centerY);
            return;
        }

        for (int i = 0; i <= samples; i++) {
            final double progress = i / (double) samples;
            final double angle = Math.toRadians(startAngle + (endAngle - startAngle) * progress);
            GL11.glVertex2d(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius);
        }
    }

    private static void enableShapeState() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
    }

    private static void disableShapeState() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void applyColor(final int color) {
        final float alpha = (color >>> 24 & 255) / 255.0F;
        final float red = (color >>> 16 & 255) / 255.0F;
        final float green = (color >>> 8 & 255) / 255.0F;
        final float blue = (color & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }
}
