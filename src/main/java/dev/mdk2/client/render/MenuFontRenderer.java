package dev.mdk2.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public final class MenuFontRenderer {
    private static final int FIRST_CHAR = 32;
    private static final int LAST_CHAR = 126;
    private static final int ATLAS_WIDTH = 1024;

    private final Glyph[] glyphs;
    private final int textureId;
    private final int atlasWidth;
    private final int atlasHeight;
    private final int lineHeight;

    public MenuFontRenderer(final String family, final int size, final int style) {
        this(new Font(family, style, size));
    }

    public MenuFontRenderer(final Font font) {
        this.glyphs = new Glyph[LAST_CHAR - FIRST_CHAR + 1];

        final BufferedImage measureImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D measureGraphics = measureImage.createGraphics();
        applyHints(measureGraphics);
        measureGraphics.setFont(font);
        final FontMetrics metrics = measureGraphics.getFontMetrics();
        this.lineHeight = metrics.getHeight();

        int x = 0;
        int y = 0;
        int rowHeight = 0;
        for (int code = FIRST_CHAR; code <= LAST_CHAR; code++) {
            final int width = Math.max(1, metrics.charWidth((char) code) + 8);
            final int height = this.lineHeight + 8;
            if (x + width >= ATLAS_WIDTH) {
                x = 0;
                y += rowHeight;
                rowHeight = 0;
            }
            this.glyphs[code - FIRST_CHAR] = new Glyph(x, y, width, height, metrics.charWidth((char) code));
            x += width;
            rowHeight = Math.max(rowHeight, height);
        }
        this.atlasWidth = ATLAS_WIDTH;
        this.atlasHeight = y + rowHeight;
        measureGraphics.dispose();

        final BufferedImage atlas = new BufferedImage(this.atlasWidth, this.atlasHeight, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = atlas.createGraphics();
        applyHints(graphics);
        graphics.setFont(font);
        graphics.setColor(java.awt.Color.WHITE);
        final FontMetrics drawMetrics = graphics.getFontMetrics();
        for (int code = FIRST_CHAR; code <= LAST_CHAR; code++) {
            final Glyph glyph = this.glyphs[code - FIRST_CHAR];
            graphics.drawString(String.valueOf((char) code), glyph.x + 2, glyph.y + 2 + drawMetrics.getAscent());
        }
        graphics.dispose();

        this.textureId = uploadAtlas(atlas);
    }

    public void drawString(final String text, final double x, final double y, final int color) {
        if (text == null || text.isEmpty()) {
            return;
        }

        final float alpha = (color >>> 24 & 255) / 255.0F;
        final float red = (color >>> 16 & 255) / 255.0F;
        final float green = (color >>> 8 & 255) / 255.0F;
        final float blue = (color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableTexture();
        RenderSystem.disableDepthTest();
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glColor4f(red, green, blue, alpha);

        double cursorX = x;
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (c < FIRST_CHAR || c > LAST_CHAR) {
                cursorX += this.lineHeight * 0.35D;
                continue;
            }
            final Glyph glyph = this.glyphs[c - FIRST_CHAR];
            drawGlyph(cursorX, y, glyph);
            cursorX += glyph.advance;
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_CULL_FACE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public void drawString(final String text, final double x, final double y, final int color, final double scale) {
        if (text == null || text.isEmpty() || scale <= 0.0D) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, 0.0D);
        GL11.glScaled(scale, scale, 1.0D);
        drawString(text, 0.0D, 0.0D, color);
        GL11.glPopMatrix();
    }

    public double width(final String text) {
        if (text == null || text.isEmpty()) {
            return 0.0D;
        }
        double width = 0.0D;
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (c < FIRST_CHAR || c > LAST_CHAR) {
                width += this.lineHeight * 0.35D;
                continue;
            }
            width += this.glyphs[c - FIRST_CHAR].advance;
        }
        return width;
    }

    public double width(final String text, final double scale) {
        return width(text) * Math.max(0.0D, scale);
    }

    public double lineHeight() {
        return this.lineHeight;
    }

    public double lineHeight(final double scale) {
        return this.lineHeight * Math.max(0.0D, scale);
    }

    private void drawGlyph(final double x, final double y, final Glyph glyph) {
        final double u1 = glyph.x / (double) this.atlasWidth;
        final double v1 = glyph.y / (double) this.atlasHeight;
        final double u2 = (glyph.x + glyph.width) / (double) this.atlasWidth;
        final double v2 = (glyph.y + glyph.height) / (double) this.atlasHeight;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(u1, v1);
        GL11.glVertex2d(x, y);
        GL11.glTexCoord2d(u2, v1);
        GL11.glVertex2d(x + glyph.width, y);
        GL11.glTexCoord2d(u2, v2);
        GL11.glVertex2d(x + glyph.width, y + glyph.height);
        GL11.glTexCoord2d(u1, v2);
        GL11.glVertex2d(x, y + glyph.height);
        GL11.glEnd();
    }

    private static void applyHints(final Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private static int uploadAtlas(final BufferedImage atlas) {
        final int width = atlas.getWidth();
        final int height = atlas.getHeight();
        final ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int argb = atlas.getRGB(x, y);
                buffer.put((byte) ((argb >> 16) & 0xFF));
                buffer.put((byte) ((argb >> 8) & 0xFF));
                buffer.put((byte) (argb & 0xFF));
                buffer.put((byte) ((argb >> 24) & 0xFF));
            }
        }
        buffer.flip();

        final int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return textureId;
    }

    private static class Glyph {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final int advance;

        private Glyph(final int x, final int y, final int width, final int height, final int advance) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.advance = advance;
        }
    }
}
