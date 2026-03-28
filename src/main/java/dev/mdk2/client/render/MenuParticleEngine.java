package dev.mdk2.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.core.ThemeManager;
import dev.mdk2.client.modules.visual.MenuParticlesModule;
import dev.mdk2.client.util.ColorUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MenuParticleEngine {
    private final Random random = new Random();
    private final List<Particle> particles = new ArrayList<Particle>();
    private final List<Ripple> ripples = new ArrayList<Ripple>();

    public void update(final int width, final int height, final MenuParticlesModule module, final ThemeManager themeManager) {
        final int targetCount = module != null && module.isEnabled() ? module.getParticleCount() : 0;
        final String style = module != null ? module.getParticleStyle() : "Dots";

        while (this.particles.size() < targetCount) {
            this.particles.add(createParticle(width, height));
        }
        while (this.particles.size() > targetCount && !this.particles.isEmpty()) {
            this.particles.remove(this.particles.size() - 1);
        }

        final double speedMultiplier = module != null ? module.getParticleSpeed() : 0.55D;
        for (final Particle particle : this.particles) {
            if ("Snow".equalsIgnoreCase(style)) {
                particle.x += particle.velocityX * speedMultiplier * 0.28D;
                particle.y += (Math.abs(particle.velocityY) * 0.75D + 0.16D) * speedMultiplier;
            } else if ("Dust".equalsIgnoreCase(style)) {
                particle.x += particle.velocityX * speedMultiplier * 0.18D;
                particle.y += particle.velocityY * speedMultiplier * 0.12D;
            } else if ("Links".equalsIgnoreCase(style)) {
                particle.x += particle.velocityX * speedMultiplier * 0.52D;
                particle.y += particle.velocityY * speedMultiplier * 0.42D;
            } else {
                particle.x += particle.velocityX * speedMultiplier;
                particle.y += particle.velocityY * speedMultiplier;
            }
            particle.phase += 0.018D + particle.size * 0.0004D;

            if (particle.x < -24.0D) {
                particle.x = width + 12.0D;
            } else if (particle.x > width + 24.0D) {
                particle.x = -12.0D;
            }

            if (particle.y < -24.0D) {
                particle.y = height + 12.0D;
            } else if (particle.y > height + 24.0D) {
                particle.y = "Snow".equalsIgnoreCase(style) ? -18.0D : -12.0D;
            }
        }

        final long now = System.currentTimeMillis();
        final Iterator<Ripple> iterator = this.ripples.iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next().createdAt > 900L) {
                iterator.remove();
            }
        }
    }

    public void render(final MatrixStack matrixStack, final ThemeManager themeManager, final MenuParticlesModule module) {
        if (module == null || !module.isEnabled()) {
            return;
        }

        final String style = module.getParticleStyle();
        final boolean twinkle = module.isTwinkleEnabled();

        if ("Links".equalsIgnoreCase(style)) {
            renderLinks(themeManager);
        }

        int index = 0;
        for (final Particle particle : this.particles) {
            final double pulse = twinkle ? (Math.sin(particle.phase) + 1.0D) * 0.5D : 0.5D;
            final int accent = themeManager.accent(index * 0.11D);
            final int baseColor = ColorUtil.withAlpha(accent, 16 + (int) (pulse * 28.0D));

            if ("Dust".equalsIgnoreCase(style)) {
                UiRenderer.drawCircle(particle.x, particle.y, particle.size * 3.1D, ColorUtil.withAlpha(accent, 4 + (int) (pulse * 10.0D)));
                UiRenderer.drawCircle(particle.x, particle.y, particle.size * 2.1D, ColorUtil.withAlpha(accent, 8 + (int) (pulse * 14.0D)));
                UiRenderer.drawCircle(particle.x, particle.y, particle.size * 0.95D, baseColor);
            } else if ("Snow".equalsIgnoreCase(style)) {
                final int snowColor = ColorUtil.withAlpha(ColorUtil.rgba(235, 240, 255, 255), 20 + (int) (pulse * 24.0D));
                UiRenderer.drawCircle(particle.x, particle.y, particle.size * 1.1D, snowColor);
                UiRenderer.drawLine(particle.x, particle.y - 1.4D, particle.x, particle.y + 1.4D, 1.0D, ColorUtil.withAlpha(ColorUtil.rgba(235, 240, 255, 255), 10));
            } else if ("Links".equalsIgnoreCase(style)) {
                UiRenderer.drawCircle(particle.x, particle.y, particle.size * 0.9D, ColorUtil.withAlpha(accent, 18 + (int) (pulse * 24.0D)));
            } else {
                UiRenderer.drawCircle(particle.x, particle.y, particle.size, baseColor);
            }
            index++;
        }
    }

    public void renderRipples(final MatrixStack matrixStack) {
        final long now = System.currentTimeMillis();
        for (final Ripple ripple : this.ripples) {
            final double progress = Math.min(1.0D, (now - ripple.createdAt) / 900.0D);
            final double radius = 10.0D + progress * ripple.maxRadius;
            final int color = ColorUtil.multiplyAlpha(ripple.color, 1.0D - progress);
            UiRenderer.drawRoundedOutline(ripple.x - radius, ripple.y - radius, radius * 2.0D, radius * 2.0D, radius, 1.4D, color);
        }
    }

    public void spawnRipple(final double x, final double y, final int color) {
        this.ripples.add(new Ripple(x, y, 42.0D + this.random.nextDouble() * 18.0D, System.currentTimeMillis(), color));
    }

    private void renderLinks(final ThemeManager themeManager) {
        final int size = this.particles.size();
        for (int i = 0; i < size; i++) {
            final Particle first = this.particles.get(i);
            int linked = 0;
            for (int j = i + 1; j < size; j++) {
                final Particle second = this.particles.get(j);
                final double dx = first.x - second.x;
                final double dy = first.y - second.y;
                final double distanceSq = dx * dx + dy * dy;
                if (distanceSq > 2500.0D) {
                    continue;
                }

                final double progress = 1.0D - Math.min(1.0D, distanceSq / 2500.0D);
                UiRenderer.drawLine(first.x, first.y, second.x, second.y, 1.0D, ColorUtil.withAlpha(themeManager.accent(i * 0.07D), 4 + (int) (progress * 16.0D)));
                linked++;
                if (linked >= 3) {
                    break;
                }
            }
        }
    }

    private Particle createParticle(final int width, final int height) {
        final double x = this.random.nextDouble() * Math.max(width, 1);
        final double y = this.random.nextDouble() * Math.max(height, 1);
        final double velocityX = -0.24D + this.random.nextDouble() * 0.48D;
        final double velocityY = -0.28D + this.random.nextDouble() * 0.56D;
        final double size = 1.2D + this.random.nextDouble() * 2.2D;
        final double phase = this.random.nextDouble() * Math.PI * 2.0D;
        return new Particle(x, y, velocityX, velocityY, size, phase);
    }

    private static final class Particle {
        private double x;
        private double y;
        private final double velocityX;
        private final double velocityY;
        private final double size;
        private double phase;

        private Particle(final double x, final double y, final double velocityX, final double velocityY, final double size, final double phase) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.size = size;
            this.phase = phase;
        }
    }

    private static final class Ripple {
        private final double x;
        private final double y;
        private final double maxRadius;
        private final long createdAt;
        private final int color;

        private Ripple(final double x, final double y, final double maxRadius, final long createdAt, final int color) {
            this.x = x;
            this.y = y;
            this.maxRadius = maxRadius;
            this.createdAt = createdAt;
            this.color = color;
        }
    }
}
