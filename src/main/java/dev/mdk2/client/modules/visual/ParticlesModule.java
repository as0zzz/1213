package dev.mdk2.client.modules.visual;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.ModeSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.ParticleStatus;

public class ParticlesModule extends Module {
    private final ModeSetting amount;
    private ParticleStatus previousStatus;

    public ParticlesModule() {
        super("Particles", "Overrides the client particle setting for cleaner visuals or better performance.", Category.VISUAL);
        this.amount = register(new ModeSetting("Amount", "All", "All", "Decreased", "Minimal"));
    }

    @Override
    public void onEnable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (this.previousStatus == null) {
            this.previousStatus = minecraft.options.particles;
        }
        applySetting();
    }

    @Override
    public void onTick() {
        applySetting();
    }

    @Override
    public void onDisable() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (this.previousStatus != null) {
            minecraft.options.particles = this.previousStatus;
        }
        this.previousStatus = null;
    }

    private void applySetting() {
        final Minecraft minecraft = Minecraft.getInstance();
        if ("Minimal".equalsIgnoreCase(this.amount.getValue())) {
            minecraft.options.particles = ParticleStatus.MINIMAL;
            return;
        }
        if ("Decreased".equalsIgnoreCase(this.amount.getValue())) {
            minecraft.options.particles = ParticleStatus.DECREASED;
            return;
        }
        minecraft.options.particles = ParticleStatus.ALL;
    }
}
