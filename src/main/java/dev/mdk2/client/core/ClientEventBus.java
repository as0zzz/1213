package dev.mdk2.client.core;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.modules.Module;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class ClientEventBus {
    private final ModuleManager moduleManager;

    public ClientEventBus(final ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void postTick() {
        for (final Module module : this.moduleManager.getModules()) {
            module.tickFrame();
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public void postRender2D(final MatrixStack matrixStack, final float partialTicks, final int width, final int height) {
        for (final Module module : this.moduleManager.getModules()) {
            if (module.isEnabled()) {
                module.onRender2D(matrixStack, partialTicks, width, height);
            }
        }
    }

    public void postRender3D(final MatrixStack matrixStack, final float partialTicks) {
        for (final Module module : this.moduleManager.getModules()) {
            if (module.isEnabled()) {
                module.onRender3D(matrixStack, partialTicks);
            }
        }
    }

    public void postAttackEntity(final AttackEntityEvent event) {
        for (final Module module : this.moduleManager.getModules()) {
            if (module.isEnabled()) {
                module.onAttackEntity(event);
            }
        }
    }

    public void postInputUpdate(final InputUpdateEvent event) {
        for (final Module module : this.moduleManager.getModules()) {
            if (module.isEnabled()) {
                module.onInputUpdate(event);
            }
        }
    }
}
