package dev.mdk2.client.modules;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.settings.Setting;
import dev.mdk2.client.settings.BindSetting;
import dev.mdk2.client.util.AnimationUtil;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<Setting<?>>();
    private final BindSetting bind;

    private boolean enabled;
    private boolean expanded;
    private boolean toggleable = true;
    private double toggleAnimation;
    private double expandAnimation;

    protected Module(final String name, final String description, final Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.bind = register(new BindSetting("Bind", BindSetting.NONE));
    }

    protected <T extends Setting<?>> T register(final T setting) {
        this.settings.add(setting);
        return setting;
    }

    protected void setToggleable(final boolean toggleable) {
        this.toggleable = toggleable;
    }

    protected void hideBindSetting() {
        this.settings.remove(this.bind);
    }

    public void tickFrame() {
        this.toggleAnimation = AnimationUtil.smooth(this.toggleAnimation, this.enabled ? 1.0D : 0.0D, 0.18D);
        this.expandAnimation = AnimationUtil.smooth(this.expandAnimation, this.expanded ? 1.0D : 0.0D, 0.24D);
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void setEnabled(final boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void setExpanded(final boolean expanded) {
        this.expanded = expanded;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }

    public void onRender2D(final MatrixStack matrixStack, final float partialTicks, final int width, final int height) {
    }

    public void onRender3D(final MatrixStack matrixStack, final float partialTicks) {
    }

    public void onAttackEntity(final AttackEntityEvent event) {
    }

    public void onInputUpdate(final InputUpdateEvent event) {
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Category getCategory() {
        return this.category;
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(this.settings);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public boolean isToggleable() {
        return this.toggleable;
    }

    public BindSetting getBind() {
        return this.bind;
    }

    public double getToggleAnimation() {
        return this.toggleAnimation;
    }

    public double getExpandAnimation() {
        return this.expandAnimation;
    }
}
