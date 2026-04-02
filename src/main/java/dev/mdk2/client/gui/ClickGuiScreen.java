package dev.mdk2.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.core.ThemeManager;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.modules.visual.HudModule;
import dev.mdk2.client.modules.visual.MenuParticlesModule;
import dev.mdk2.client.modules.visual.DeathCoordsModule;
import dev.mdk2.client.modules.visual.StatusEffectsModule;
import dev.mdk2.client.modules.visual.WatermarkModule;
import dev.mdk2.client.modules.xray.RadarModule;
import dev.mdk2.client.render.HudRenderer;
import dev.mdk2.client.render.UiRenderer;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.BindSetting;
import dev.mdk2.client.settings.ColorSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.settings.Setting;
import dev.mdk2.client.util.AnimationUtil;
import dev.mdk2.client.util.ColorUtil;
import dev.mdk2.client.util.MathUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {
    private final ClientRuntime runtime;
    private final List<CategoryPanel> panels = new ArrayList<CategoryPanel>();
    private final List<HitBox> hitBoxes = new ArrayList<HitBox>();

    private NumberSetting activeSlider;
    private HitBox activeSliderHitBox;
    private BindSetting activeBindSetting;
    private ColorSetting activeColorSetting;
    private HitBox activeColorHitBox;
    private ColorControl activeColorControl;
    private CategoryPanel draggingPanel;
    private double dragOffsetX;
    private double dragOffsetY;
    private double introAnimation;
    private HudDragTarget activeHudDrag;
    private double hudDragOffsetX;
    private double hudDragOffsetY;
    private boolean colorPickerVisible;
    private double colorPickerX;
    private double colorPickerY;
    private double colorPickerWidth;
    private double colorPickerHeight;
    private static final int[] COLOR_PRESETS = new int[]{
        ColorUtil.rgba(255, 90, 122, 255),
        ColorUtil.rgba(255, 152, 72, 255),
        ColorUtil.rgba(255, 214, 72, 255),
        ColorUtil.rgba(155, 229, 88, 255),
        ColorUtil.rgba(80, 222, 182, 255),
        ColorUtil.rgba(92, 174, 255, 255),
        ColorUtil.rgba(122, 108, 255, 255),
        ColorUtil.rgba(196, 92, 255, 255),
        ColorUtil.rgba(255, 82, 214, 255),
        ColorUtil.rgba(255, 255, 255, 255)
    };

    public ClickGuiScreen(final ClientRuntime runtime) {
        super(new StringTextComponent("MDK2 ClickGUI"));
        this.runtime = runtime;
    }

    @Override
    protected void init() {
        super.init();
        buildPanels();
        this.activeSlider = null;
        this.activeSliderHitBox = null;
        this.activeBindSetting = null;
        this.activeColorControl = null;
        this.draggingPanel = null;
        this.introAnimation = 0.0D;
        this.activeHudDrag = null;
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.hitBoxes.clear();
        this.colorPickerVisible = false;
        this.introAnimation = AnimationUtil.smooth(this.introAnimation, 1.0D, 0.16D);
        final ThemeManager themeManager = this.runtime.getThemeManager();
        final MenuParticlesModule particlesModule = this.runtime.getModuleManager().get(MenuParticlesModule.class);

        this.runtime.getMenuParticleEngine().render(matrixStack, themeManager, particlesModule);
        this.runtime.getMenuParticleEngine().renderRipples(matrixStack);

        for (final CategoryPanel panel : this.panels) {
            if (panel == this.draggingPanel) {
                continue;
            }
            renderPanel(matrixStack, panel, mouseX);
        }
        if (this.draggingPanel != null) {
            renderPanel(matrixStack, this.draggingPanel, mouseX);
        }

        if (this.activeColorSetting != null) {
            if (this.colorPickerVisible) {
                renderColorPicker(matrixStack);
            } else {
                this.activeColorSetting = null;
                this.activeColorHitBox = null;
                this.activeColorControl = null;
            }
        }

        if (isHudEditorActive()) {
            this.runtime.getHudRenderer().renderEditorPreview(matrixStack);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final ThemeManager themeManager = this.runtime.getThemeManager();
        final MenuParticlesModule particlesModule = this.runtime.getModuleManager().get(MenuParticlesModule.class);
        if (particlesModule != null && particlesModule.isEnabled() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.runtime.getMenuParticleEngine().spawnRipple(mouseX, mouseY, ColorUtil.withAlpha(themeManager.accent(0.0D), 42));
        }

        if (this.activeBindSetting != null) {
            this.activeBindSetting.setKey(BindSetting.fromMouseButton(button));
            this.activeBindSetting = null;
            return true;
        }

        if (handleColorPickerClick(mouseX, mouseY, button)) {
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isHudEditorActive() && tryStartHudDrag(mouseX, mouseY)) {
            return true;
        }

        for (int i = this.hitBoxes.size() - 1; i >= 0; i--) {
            final HitBox hitBox = this.hitBoxes.get(i);
            if (!hitBox.contains(mouseX, mouseY)) {
                continue;
            }

            focusPanel(hitBox.panel);

            if (hitBox.type == HitType.HEADER && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.draggingPanel = hitBox.panel;
                this.dragOffsetX = mouseX - hitBox.panel.x;
                this.dragOffsetY = mouseY - hitBox.panel.y;
                return true;
            }

            if (hitBox.type == HitType.MODULE) {
                if (!hitBox.module.isToggleable()) {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        hitBox.module.toggleExpanded();
                        return true;
                    }
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    hitBox.module.toggle();
                    return true;
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    hitBox.module.toggleExpanded();
                    return true;
                }
            }

            if (hitBox.type == HitType.COLOR_SETTING && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (this.activeColorSetting == hitBox.setting) {
                    this.activeColorSetting = null;
                    this.activeColorHitBox = null;
                    this.activeColorControl = null;
                } else {
                    this.activeColorSetting = (ColorSetting) hitBox.setting;
                    this.activeColorHitBox = hitBox;
                    this.activeColorControl = null;
                }
                return true;
            }

            if (hitBox.type == HitType.BIND_SETTING && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.activeBindSetting = this.activeBindSetting == hitBox.setting ? null : (BindSetting) hitBox.setting;
                return true;
            }

            if (hitBox.type == HitType.BOOLEAN_SETTING && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                ((BooleanSetting) hitBox.setting).toggle();
                return true;
            }

            if (hitBox.type == HitType.NUMBER_SETTING && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.activeSlider = (NumberSetting) hitBox.setting;
                this.activeSliderHitBox = hitBox;
                updateSlider(mouseX);
                return true;
            }

            if (hitBox.type == HitType.MODE_SETTING) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    ((ModeSetting) hitBox.setting).next();
                    return true;
                }
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    ((ModeSetting) hitBox.setting).previous();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        this.activeSlider = null;
        this.activeSliderHitBox = null;
        this.activeColorControl = null;
        this.draggingPanel = null;
        this.activeHudDrag = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double dragX, final double dragY) {
        if (this.activeSlider != null && this.activeSliderHitBox != null) {
            updateSlider(mouseX);
            return true;
        }

        if (this.activeColorSetting != null && this.activeColorControl != null) {
            updateColorPicker(mouseX, mouseY);
            return true;
        }

        if (this.activeHudDrag != null) {
            updateHudDrag(mouseX, mouseY);
            return true;
        }

        if (this.draggingPanel != null) {
            this.draggingPanel.x = MathUtil.clamp(mouseX - this.dragOffsetX, 4.0D - this.draggingPanel.width + 18.0D, this.width - 18.0D);
            this.draggingPanel.y = MathUtil.clamp(mouseY - this.dragOffsetY, 4.0D, this.height - this.draggingPanel.height - 4.0D);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double delta) {
        for (int i = this.panels.size() - 1; i >= 0; i--) {
            final CategoryPanel panel = this.panels.get(i);
            if (MathUtil.within(mouseX, mouseY, panel.contentX, panel.contentY, panel.contentWidth, panel.contentVisibleHeight)) {
                panel.targetScroll = MathUtil.clamp(panel.targetScroll + delta * 20.0D, panel.minScroll, 0.0D);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (this.activeBindSetting != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                this.activeBindSetting.setKey(BindSetting.NONE);
            } else {
                this.activeBindSetting.setKey(keyCode);
            }
            this.activeBindSetting = null;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void buildPanels() {
        this.panels.clear();

        final Category[] categories = Category.values();
        final int count = categories.length;
        final double outerPadding = MathUtil.clamp(this.width * 0.015D, 6.0D, 14.0D);
        final double gap = MathUtil.clamp(this.width * 0.008D, 4.0D, 10.0D);
        final double availableWidth = this.width - outerPadding * 2.0D - gap * (count - 1);
        final double panelWidth = availableWidth / count;
        final double panelHeight = Math.min(Math.max(156.0D, this.height - 18.0D), 250.0D);
        final double startX = outerPadding;
        final double startY = Math.max(6.0D, (this.height - panelHeight) / 2.0D);

        for (int i = 0; i < count; i++) {
            this.panels.add(new CategoryPanel(categories[i], startX + i * (panelWidth + gap), startY, panelWidth, panelHeight, i));
        }
    }

    private void renderPanel(final MatrixStack matrixStack, final CategoryPanel panel, final int mouseX) {
        final ThemeManager themeManager = this.runtime.getThemeManager();
        final double intro = AnimationUtil.easeOutQuint(MathUtil.clamp(this.introAnimation - panel.index * 0.04D, 0.0D, 1.0D));
        final boolean focused = panel == this.draggingPanel || this.panels.indexOf(panel) == this.panels.size() - 1;

        panel.renderX = panel.x;
        panel.renderY = panel.y + (1.0D - intro) * 10.0D;
        panel.scroll = AnimationUtil.smooth(panel.scroll, panel.targetScroll, 0.22D);

        final int panelColor = themeManager.isGlass() ? ColorUtil.rgba(24, 30, 42, 118) : ColorUtil.rgba(14, 17, 24, 238);
        final int headerColor = themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 18) : ColorUtil.rgba(24, 29, 40, 250);
        final int borderColor = ColorUtil.withAlpha(themeManager.accent(panel.index * 0.12D), focused ? 58 : themeManager.isGlass() ? 44 : 32);
        final int innerOutlineColor = focused
            ? ColorUtil.withAlpha(themeManager.accent(panel.index * 0.12D), 20)
            : themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 12) : ColorUtil.rgba(255, 255, 255, 8);

        UiRenderer.drawRoundedRect(panel.renderX, panel.renderY, panel.width, panel.height, 14.0D, panelColor);
        UiRenderer.drawRoundedRect(panel.renderX + 1.0D, panel.renderY + 1.0D, panel.width - 2.0D, 26.0D, 13.0D, headerColor);
        UiRenderer.drawRoundedOutline(panel.renderX, panel.renderY, panel.width, panel.height, 14.0D, 1.0D, borderColor);
        UiRenderer.drawRoundedOutline(panel.renderX + 1.0D, panel.renderY + 1.0D, panel.width - 2.0D, panel.height - 2.0D, 13.0D, 1.0D, innerOutlineColor);
        UiRenderer.drawRoundedOutline(panel.renderX - 0.5D, panel.renderY - 0.5D, panel.width + 1.0D, panel.height + 1.0D, 14.5D, 1.0D,
            ColorUtil.withAlpha(themeManager.accent(panel.index * 0.12D), focused ? 14 : 10));
        UiRenderer.drawRoundedRect(panel.renderX + 10.0D, panel.renderY + 9.0D, 2.0D, 12.0D, 1.0D, themeManager.accent(panel.index * 0.12D));

        final List<Module> modules = this.runtime.getModuleManager().getByCategory(panel.category);
        final String title = trimToWidth(panel.category.getTitle(), (int) (panel.width - 30.0D));
        UiRenderer.drawText(matrixStack, title, (float) (panel.renderX + 18.0D), (float) (panel.renderY + 10.0D), themeManager.textPrimary());

        this.hitBoxes.add(new HitBox(HitType.HEADER, panel.renderX, panel.renderY, panel.width, 26.0D, panel, null, null, 0.0D, 0.0D));

        panel.contentX = panel.renderX + 6.0D;
        panel.contentY = panel.renderY + 31.0D;
        panel.contentWidth = panel.width - 12.0D;
        panel.contentVisibleHeight = panel.height - 38.0D;

        UiRenderer.scissorStart(panel.contentX, panel.contentY, panel.contentWidth, panel.contentVisibleHeight);
        double currentY = panel.contentY + panel.scroll;
        for (final Module module : modules) {
            currentY += renderModuleCard(matrixStack, panel, module, panel.contentX, currentY, panel.contentWidth, mouseX);
            currentY += 5.0D;
        }
        UiRenderer.scissorEnd();

        panel.contentHeight = Math.max(0.0D, currentY - panel.contentY - panel.scroll);
        panel.minScroll = Math.min(0.0D, panel.contentVisibleHeight - panel.contentHeight - 2.0D);
        panel.targetScroll = MathUtil.clamp(panel.targetScroll, panel.minScroll, 0.0D);

        renderScrollbar(panel, themeManager);
    }

    private double renderModuleCard(final MatrixStack matrixStack, final CategoryPanel panel, final Module module,
                                    final double x, final double y, final double width, final int mouseX) {
        final ThemeManager themeManager = this.runtime.getThemeManager();
        final double expansion = module.getExpandAnimation();
        final double settingsHeight = getSettingsHeight(module);
        final double cardHeight = 30.0D + settingsHeight * expansion;

        final int cardColor = themeManager.isGlass()
            ? ColorUtil.rgba(32, 40, 54, module.isEnabled() ? 134 : 108)
            : ColorUtil.rgba(21, 25, 35, module.isEnabled() ? 246 : 226);
        final int insetColor = themeManager.isGlass()
            ? ColorUtil.rgba(255, 255, 255, 14)
            : ColorUtil.rgba(28, 33, 45, 255);
        final int cardOutline = module.isEnabled()
            ? ColorUtil.withAlpha(themeManager.accent(panel.index * 0.12D), themeManager.isGlass() ? 42 : 28)
            : themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 18) : ColorUtil.rgba(255, 255, 255, 9);

        UiRenderer.drawRoundedRect(x, y, width, cardHeight, 11.0D, cardColor);
        UiRenderer.drawRoundedRect(x + 1.0D, y + 1.0D, width - 2.0D, 28.0D, 10.0D, insetColor);
        UiRenderer.drawRoundedOutline(x, y, width, cardHeight, 11.0D, 1.0D, cardOutline);
        UiRenderer.drawRoundedRect(x + 7.0D, y + 7.0D, 2.0D, 14.0D, 1.0D, ColorUtil.withAlpha(themeManager.accent(panel.index * 0.12D), module.isEnabled() ? 255 : 76));

        final boolean toggleable = module.isToggleable();
        final double rightControlsWidth = toggleable ? 42.0D : 26.0D;
        final double textAreaWidth = width - rightControlsWidth - 20.0D;
        final String moduleName = trimToWidth(module.getName(), (int) (textAreaWidth / 0.95D));
        final String description = trimToWidth(module.getDescription(), (int) (textAreaWidth / 0.84D));
        drawClippedScaledText(matrixStack, moduleName, x + 15.0D, y + 7.0D, textAreaWidth, 9.0D, 0.95D, themeManager.textPrimary());
        drawClippedScaledText(matrixStack, description, x + 15.0D, y + 17.0D, textAreaWidth, 8.0D, 0.84D, themeManager.textSecondary());

        final double expandBoxX = toggleable ? x + width - 34.0D : x + width - 20.0D;
        UiRenderer.drawRoundedRect(expandBoxX, y + 6.0D, 10.0D, 10.0D, 4.0D, themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 16) : ColorUtil.rgba(36, 43, 57, 255));
        UiRenderer.drawText(matrixStack, module.isExpanded() ? "-" : "+", (float) (expandBoxX + 3.0D), (float) (y + 8.0D), themeManager.textPrimary());
        if (toggleable) {
            final double switchX = x + width - 20.0D;
            UiRenderer.drawRoundedRect(switchX, y + 6.0D, 14.0D, 10.0D, 5.0D,
                module.isEnabled() ? ColorUtil.withAlpha(themeManager.accent(panel.index * 0.12D), 164) : ColorUtil.rgba(41, 47, 61, 255));
            UiRenderer.drawCircle(switchX + 4.0D + module.getToggleAnimation() * 6.0D, y + 11.0D, 2.8D, ColorUtil.rgba(255, 255, 255, 255));
        }

        this.hitBoxes.add(new HitBox(HitType.MODULE, x, y, width, 28.0D, panel, module, null, 0.0D, 0.0D));

        if (expansion <= 0.01D || module.getSettings().isEmpty()) {
            return cardHeight;
        }

        final double settingsX = x + 4.0D;
        final double settingsY = y + 31.0D;
        final double settingsWidth = width - 8.0D;
        final double visibleSettingsHeight = Math.max(4.0D, settingsHeight * expansion - 1.0D);
        final int settingsColor = themeManager.isGlass() ? ColorUtil.rgba(16, 20, 28, 72) : ColorUtil.rgba(13, 16, 24, 182);

        UiRenderer.drawRoundedRect(settingsX, settingsY, settingsWidth, visibleSettingsHeight, 9.0D, settingsColor);
        UiRenderer.scissorStart(settingsX, settingsY, settingsWidth, visibleSettingsHeight);

        double settingY = settingsY + 5.0D + (1.0D - expansion) * 8.0D;
        for (final Setting<?> setting : module.getSettings()) {
            final double rowHeight = getSettingRowHeight(setting);
            renderSetting(matrixStack, panel, setting, settingsX + 5.0D, settingY, settingsWidth - 10.0D, rowHeight, mouseX, expansion);
            settingY += rowHeight + 4.0D;
        }
        UiRenderer.scissorEnd();

        return cardHeight;
    }

    private void renderSetting(final MatrixStack matrixStack, final CategoryPanel panel, final Setting<?> setting,
                               final double x, final double y, final double width, final double height, final int mouseX, final double expansion) {
        final ThemeManager themeManager = this.runtime.getThemeManager();
        final double easedExpansion = AnimationUtil.easeInOutCubic(expansion);
        final int rowColorBase = themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 10) : ColorUtil.rgba(22, 27, 37, 240);
        final int rowColor = ColorUtil.multiplyAlpha(rowColorBase, Math.max(0.15D, easedExpansion));
        final int primaryText = ColorUtil.multiplyAlpha(themeManager.textPrimary(), easedExpansion);
        final int secondaryText = ColorUtil.multiplyAlpha(themeManager.textSecondary(), easedExpansion);

        UiRenderer.drawRoundedRect(x, y, width, height, 7.0D, rowColor);

        if (setting instanceof BooleanSetting) {
            final BooleanSetting booleanSetting = (BooleanSetting) setting;
            final double toggleX = x + width - 23.0D;
            final String label = trimToWidth(setting.getName(), (int) ((width - 34.0D) / 0.88D));
            drawClippedScaledText(matrixStack, label, x + 6.0D, y + 5.0D, width - 34.0D, 8.0D, 0.88D, primaryText);
            UiRenderer.drawRoundedRect(toggleX, y + 4.0D, 16.0D, 10.0D, 5.0D,
                booleanSetting.getValue().booleanValue()
                    ? ColorUtil.multiplyAlpha(ColorUtil.withAlpha(themeManager.accent(panel.index * 0.14D), 170), easedExpansion)
                    : ColorUtil.multiplyAlpha(ColorUtil.rgba(42, 48, 62, 255), easedExpansion));
            UiRenderer.drawCircle(toggleX + (booleanSetting.getValue().booleanValue() ? 11.0D : 5.0D), y + 9.0D, 2.9D,
                ColorUtil.multiplyAlpha(ColorUtil.rgba(255, 255, 255, 255), easedExpansion));
            this.hitBoxes.add(new HitBox(HitType.BOOLEAN_SETTING, x, y, width, height, panel, null, setting, 0.0D, 0.0D));
            return;
        }

        if (setting instanceof ModeSetting) {
            final ModeSetting modeSetting = (ModeSetting) setting;
            final String value = trimToWidth(modeSetting.getValue(), (int) (width * 0.32D));
            final double pillWidth = this.font.width(value) + 12.0D;
            final double pillX = x + width - pillWidth - 6.0D;
            final int pillColorBase = themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 14) : ColorUtil.rgba(37, 43, 57, 255);
            final String label = trimToWidth(setting.getName(), (int) ((pillX - x - 12.0D) / 0.88D));
            drawClippedScaledText(matrixStack, label, x + 6.0D, y + 5.0D, pillX - x - 12.0D, 8.0D, 0.88D, primaryText);
            UiRenderer.drawRoundedRect(pillX, y + 3.0D, pillWidth, 11.0D, 5.0D, ColorUtil.multiplyAlpha(pillColorBase, easedExpansion));
            UiRenderer.drawText(matrixStack, value, (float) (pillX + 6.0D), (float) (y + 6.0D), secondaryText);
            this.hitBoxes.add(new HitBox(HitType.MODE_SETTING, x, y, width, height, panel, null, setting, 0.0D, 0.0D));
            return;
        }

        if (setting instanceof BindSetting) {
            final BindSetting bindSetting = (BindSetting) setting;
            final String value = this.activeBindSetting == bindSetting ? "Press key..." : bindSetting.getDisplayName();
            final double pillWidth = this.font.width(value) + 12.0D;
            final double pillX = x + width - pillWidth - 6.0D;
            final int pillColorBase = this.activeBindSetting == bindSetting
                ? ColorUtil.withAlpha(themeManager.accent(panel.index * 0.12D), 92)
                : themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 14) : ColorUtil.rgba(37, 43, 57, 255);
            final String label = trimToWidth(setting.getName(), (int) ((pillX - x - 12.0D) / 0.88D));
            drawClippedScaledText(matrixStack, label, x + 6.0D, y + 5.0D, pillX - x - 12.0D, 8.0D, 0.88D, primaryText);
            UiRenderer.drawRoundedRect(pillX, y + 3.0D, pillWidth, 11.0D, 5.0D, ColorUtil.multiplyAlpha(pillColorBase, easedExpansion));
            UiRenderer.drawText(matrixStack, value, (float) (pillX + 6.0D), (float) (y + 6.0D), secondaryText);
            this.hitBoxes.add(new HitBox(HitType.BIND_SETTING, x, y, width, height, panel, null, setting, 0.0D, 0.0D));
            return;
        }

        if (setting instanceof NumberSetting) {
            final NumberSetting numberSetting = (NumberSetting) setting;
            final String value = String.format("%.2f", numberSetting.getValue().doubleValue());
            final double valueWidth = this.font.width(value);
            final String label = trimToWidth(setting.getName(), (int) ((width - valueWidth - 18.0D) / 0.88D));
            drawClippedScaledText(matrixStack, label, x + 6.0D, y + 5.0D, width - valueWidth - 18.0D, 8.0D, 0.88D, primaryText);
            UiRenderer.drawText(matrixStack, value, (float) (x + width - valueWidth - 6.0D), (float) (y + 5.0D), secondaryText);

            final double sliderX = x + 6.0D;
            final double sliderWidth = width - 12.0D;
            final double sliderY = y + height - 7.0D;
            final double fillWidth = sliderWidth * numberSetting.getNormalizedValue();
            final int sliderTrackBase = themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 18) : ColorUtil.rgba(41, 47, 61, 255);
            final int accent = ColorUtil.multiplyAlpha(themeManager.accent(panel.index * 0.12D), easedExpansion);

            UiRenderer.drawRoundedRect(sliderX, sliderY, sliderWidth, 2.0D, 1.0D, ColorUtil.multiplyAlpha(sliderTrackBase, easedExpansion));
            UiRenderer.drawRoundedRect(sliderX, sliderY, fillWidth, 2.0D, 1.0D, accent);
            UiRenderer.drawCircle(sliderX + fillWidth, sliderY + 1.0D, 2.8D, accent);
            this.hitBoxes.add(new HitBox(HitType.NUMBER_SETTING, x, y, width, height, panel, null, setting, sliderX, sliderWidth));

            if (this.activeSlider == numberSetting && this.activeSliderHitBox != null) {
                updateSlider(mouseX);
            }
            return;
        }

        if (setting instanceof ColorSetting) {
            final ColorSetting colorSetting = (ColorSetting) setting;
            final double swatchSize = 12.0D;
            final double swatchX = x + width - swatchSize - 6.0D;
            final double swatchY = y + (height - swatchSize) / 2.0D;
            final String label = trimToWidth(setting.getName(), (int) ((swatchX - x - 12.0D) / 0.88D));
            drawClippedScaledText(matrixStack, label, x + 6.0D, y + 5.0D, swatchX - x - 12.0D, 8.0D, 0.88D, primaryText);
            UiRenderer.drawRoundedRect(swatchX - 1.0D, swatchY - 1.0D, swatchSize + 2.0D, swatchSize + 2.0D, 4.0D,
                ColorUtil.multiplyAlpha(themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 16) : ColorUtil.rgba(34, 40, 52, 255), easedExpansion));
            UiRenderer.drawRoundedRect(swatchX, swatchY, swatchSize, swatchSize, 3.5D, ColorUtil.multiplyAlpha(colorSetting.getColor(), easedExpansion));
            if (this.activeColorSetting == colorSetting) {
                UiRenderer.drawRoundedOutline(swatchX - 1.0D, swatchY - 1.0D, swatchSize + 2.0D, swatchSize + 2.0D, 4.0D, 1.0D,
                    ColorUtil.multiplyAlpha(ColorUtil.withAlpha(themeManager.accent(panel.index * 0.12D), 180), easedExpansion));
                this.colorPickerVisible = true;
                this.colorPickerWidth = 168.0D;
                this.colorPickerHeight = 214.0D;
                final double preferredX = x + width + 8.0D;
                final double fallbackX = x - this.colorPickerWidth - 8.0D;
                this.colorPickerX = preferredX + this.colorPickerWidth <= this.width - 6.0D
                    ? preferredX
                    : Math.max(6.0D, fallbackX);
                this.colorPickerY = MathUtil.clamp(y - 2.0D, 6.0D, this.height - this.colorPickerHeight - 6.0D);
            }
            this.hitBoxes.add(new HitBox(HitType.COLOR_SETTING, swatchX - 2.0D, y, swatchSize + 4.0D, height, panel, null, setting, 0.0D, 0.0D));
        }
    }

    private void renderScrollbar(final CategoryPanel panel, final ThemeManager themeManager) {
        if (panel.contentHeight <= panel.contentVisibleHeight + 1.0D) {
            return;
        }

        final double trackX = panel.renderX + panel.width - 4.0D;
        final double trackY = panel.contentY + 4.0D;
        final double trackHeight = panel.contentVisibleHeight - 8.0D;
        final double thumbHeight = Math.max(16.0D, trackHeight * (panel.contentVisibleHeight / panel.contentHeight));
        final double progress = Math.abs(panel.scroll / panel.minScroll);
        final double thumbY = trackY + (trackHeight - thumbHeight) * MathUtil.clamp(progress, 0.0D, 1.0D);

        UiRenderer.drawRoundedRect(trackX, trackY, 1.5D, trackHeight, 0.75D, themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 16) : ColorUtil.rgba(255, 255, 255, 12));
        UiRenderer.drawRoundedRect(trackX, thumbY, 1.5D, thumbHeight, 0.75D, ColorUtil.withAlpha(themeManager.accent(panel.index * 0.12D), 210));
    }

    private double getSettingsHeight(final Module module) {
        double total = 6.0D;
        for (final Setting<?> setting : module.getSettings()) {
            total += getSettingRowHeight(setting) + 4.0D;
        }
        return total;
    }

    private double getSettingRowHeight(final Setting<?> setting) {
        return setting instanceof NumberSetting ? 24.0D : 18.0D;
    }

    private void updateSlider(final double mouseX) {
        if (this.activeSlider == null || this.activeSliderHitBox == null) {
            return;
        }

        final double progress = MathUtil.clamp((mouseX - this.activeSliderHitBox.sliderX) / this.activeSliderHitBox.sliderWidth, 0.0D, 1.0D);
        final double value = this.activeSlider.getMinimum() + (this.activeSlider.getMaximum() - this.activeSlider.getMinimum()) * progress;
        this.activeSlider.setValue(value);
    }

    private boolean handleColorPickerClick(final double mouseX, final double mouseY, final int button) {
        if (this.activeColorSetting == null || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        if (!isPointInsideColorPicker(mouseX, mouseY)) {
            this.activeColorControl = null;
            this.activeColorSetting = null;
            this.activeColorHitBox = null;
            return false;
        }

        final double squareX = this.colorPickerX + 10.0D;
        final double squareY = this.colorPickerY + 28.0D;
        final double squareSize = 98.0D;
        final double sliderX = squareX;
        final double hueY = squareY + squareSize + 12.0D;
        final double alphaY = hueY + 18.0D;
        final double sliderWidth = squareSize;
        final double sliderHeight = 7.0D;

        if (MathUtil.within(mouseX, mouseY, squareX, squareY, squareSize, squareSize)) {
            this.activeColorControl = ColorControl.SATURATION_BRIGHTNESS;
            updateColorPicker(mouseX, mouseY);
            return true;
        }

        if (MathUtil.within(mouseX, mouseY, sliderX, hueY, sliderWidth, sliderHeight)) {
            this.activeColorControl = ColorControl.HUE;
            updateColorPicker(mouseX, mouseY);
            return true;
        }

        if (MathUtil.within(mouseX, mouseY, sliderX, alphaY, sliderWidth, sliderHeight)) {
            this.activeColorControl = ColorControl.ALPHA;
            updateColorPicker(mouseX, mouseY);
            return true;
        }

        final double swatchStartY = alphaY + 26.0D;
        for (int index = 0; index < COLOR_PRESETS.length; index++) {
            final double swatchX = this.colorPickerX + 10.0D + (index % 5) * 24.0D;
            final double swatchY = swatchStartY + (index / 5) * 24.0D;
            if (MathUtil.within(mouseX, mouseY, swatchX, swatchY, 16.0D, 16.0D)) {
                final int currentAlpha = this.activeColorSetting.getColor() >>> 24 & 255;
                this.activeColorSetting.setColor((currentAlpha & 255) << 24 | (COLOR_PRESETS[index] & 0x00FFFFFF));
                return true;
            }
        }

        return true;
    }

    private void updateColorPicker(final double mouseX, final double mouseY) {
        if (this.activeColorSetting == null || this.activeColorControl == null) {
            return;
        }

        final double squareX = this.colorPickerX + 10.0D;
        final double squareY = this.colorPickerY + 28.0D;
        final double squareSize = 98.0D;

        if (this.activeColorControl == ColorControl.SATURATION_BRIGHTNESS) {
            final float saturation = (float) MathUtil.clamp((mouseX - squareX) / squareSize, 0.0D, 1.0D);
            final float brightness = (float) MathUtil.clamp(1.0D - (mouseY - squareY) / squareSize, 0.0D, 1.0D);
            this.activeColorSetting.setFromHsb(this.activeColorSetting.getHue(), saturation, brightness, this.activeColorSetting.getAlpha());
            return;
        }

        if (this.activeColorControl == ColorControl.HUE) {
            final float hue = (float) MathUtil.clamp((mouseX - squareX) / squareSize, 0.0D, 1.0D);
            this.activeColorSetting.setHue(hue);
            return;
        }

        final float alpha = (float) MathUtil.clamp((mouseX - squareX) / squareSize, 0.0D, 1.0D);
        this.activeColorSetting.setAlpha(alpha);
    }

    private boolean isPointInsideColorPicker(final double mouseX, final double mouseY) {
        return this.colorPickerVisible && MathUtil.within(mouseX, mouseY, this.colorPickerX, this.colorPickerY, this.colorPickerWidth, this.colorPickerHeight);
    }

    private void renderColorPicker(final MatrixStack matrixStack) {
        if (this.activeColorSetting == null) {
            return;
        }

        final ThemeManager themeManager = this.runtime.getThemeManager();
        final double x = this.colorPickerX;
        final double y = this.colorPickerY;
        final double width = this.colorPickerWidth;
        final double height = this.colorPickerHeight;
        final double contentX = x + 8.0D;
        final double contentY = y + 10.0D;
        final double contentWidth = width - 16.0D;
        final double contentHeight = height - 18.0D;
        final double squareX = contentX + 2.0D;
        final double squareY = contentY + 18.0D;
        final double squareSize = 98.0D;
        final double hueY = squareY + squareSize + 12.0D;
        final double alphaY = hueY + 18.0D;
        final double stripWidth = squareSize;
        final double stripHeight = 7.0D;
        final double sideX = squareX + squareSize + 12.0D;
        final int outline = ColorUtil.withAlpha(themeManager.accent(0.0D), 46);
        final int background = themeManager.isGlass() ? ColorUtil.rgba(18, 22, 30, 202) : ColorUtil.rgba(14, 17, 24, 244);
        final int inner = themeManager.isGlass() ? ColorUtil.rgba(255, 255, 255, 12) : ColorUtil.rgba(26, 31, 42, 255);
        final int color = this.activeColorSetting.getColor();
        final int hueColor = 0xFF000000 | (Color.HSBtoRGB(this.activeColorSetting.getHue(), 1.0F, 1.0F) & 0x00FFFFFF);
        final double markerX = squareX + this.activeColorSetting.getSaturation() * squareSize;
        final double markerY = squareY + (1.0D - this.activeColorSetting.getBrightness()) * squareSize;
        final double hueMarkerX = squareX + this.activeColorSetting.getHue() * squareSize;
        final double alphaMarkerX = squareX + this.activeColorSetting.getAlpha() * squareSize;

        UiRenderer.drawRoundedRect(x, y, width, height, 12.0D, background);
        UiRenderer.drawRoundedRect(x + 1.0D, y + 1.0D, width - 2.0D, height - 2.0D, 11.0D, inner);
        UiRenderer.drawRoundedOutline(x, y, width, height, 12.0D, 1.0D, outline);
        UiRenderer.drawText(matrixStack, this.activeColorSetting.getName(), (float) (contentX + 2.0D), (float) (contentY + 1.0D), themeManager.textPrimary());

        UiRenderer.scissorStart(contentX, contentY, contentWidth, contentHeight);

        UiRenderer.drawGradientRect(squareX, squareY, squareSize, squareSize,
            ColorUtil.rgba(255, 255, 255, 255), hueColor, hueColor, ColorUtil.rgba(255, 255, 255, 255));
        UiRenderer.drawGradientRect(squareX, squareY, squareSize, squareSize,
            ColorUtil.rgba(0, 0, 0, 0), ColorUtil.rgba(0, 0, 0, 0), ColorUtil.rgba(0, 0, 0, 255), ColorUtil.rgba(0, 0, 0, 255));
        UiRenderer.drawRoundedOutline(squareX - 0.5D, squareY - 0.5D, squareSize + 1.0D, squareSize + 1.0D, 6.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 34));
        UiRenderer.drawCircle(markerX, markerY, 3.0D, ColorUtil.rgba(255, 255, 255, 255));
        UiRenderer.drawCircle(markerX, markerY, 1.3D, ColorUtil.rgba(0, 0, 0, 180));

        renderHueStrip(squareX, hueY, stripWidth, stripHeight);
        UiRenderer.drawCircle(hueMarkerX, hueY + stripHeight / 2.0D, 3.0D, ColorUtil.rgba(255, 255, 255, 255));
        UiRenderer.drawCircle(hueMarkerX, hueY + stripHeight / 2.0D, 1.2D, ColorUtil.rgba(0, 0, 0, 180));

        UiRenderer.drawGradientRect(squareX, alphaY, stripWidth, stripHeight,
            ColorUtil.withAlpha(color, 0), ColorUtil.withAlpha(color, 255), ColorUtil.withAlpha(color, 255), ColorUtil.withAlpha(color, 0));
        UiRenderer.drawRoundedOutline(squareX - 0.5D, alphaY - 0.5D, stripWidth + 1.0D, stripHeight + 1.0D, 3.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 28));
        UiRenderer.drawCircle(alphaMarkerX, alphaY + stripHeight / 2.0D, 3.0D, ColorUtil.rgba(255, 255, 255, 255));
        UiRenderer.drawCircle(alphaMarkerX, alphaY + stripHeight / 2.0D, 1.2D, ColorUtil.rgba(0, 0, 0, 180));

        UiRenderer.drawRoundedRect(sideX, squareY + 10.0D, 34.0D, 34.0D, 8.0D, color);
        UiRenderer.drawRoundedOutline(sideX, squareY + 10.0D, 34.0D, 34.0D, 8.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 48));
        UiRenderer.drawText(matrixStack, String.format("#%06X", color & 0x00FFFFFF), (float) (sideX - 4.0D), (float) (squareY + 54.0D), themeManager.textSecondary());
        UiRenderer.drawText(matrixStack, "A " + (int) Math.round(this.activeColorSetting.getAlpha() * 100.0D) + "%", (float) (sideX + 1.0D), (float) (squareY + 66.0D), themeManager.textSecondary());

        UiRenderer.drawText(matrixStack, "Saved", (float) (squareX), (float) (alphaY + 14.0D), themeManager.textSecondary());
        for (int index = 0; index < COLOR_PRESETS.length; index++) {
            final double swatchX = squareX + (index % 5) * 24.0D;
            final double swatchY = alphaY + 26.0D + (index / 5) * 24.0D;
            UiRenderer.drawRoundedRect(swatchX, swatchY, 16.0D, 16.0D, 5.0D, COLOR_PRESETS[index]);
            UiRenderer.drawRoundedOutline(swatchX, swatchY, 16.0D, 16.0D, 5.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 34));
        }

        UiRenderer.scissorEnd();
    }

    private void renderHueStrip(final double x, final double y, final double width, final double height) {
        final int steps = Math.max(24, (int) Math.round(width));
        final double stepWidth = width / steps;
        for (int index = 0; index < steps; index++) {
            final float hueA = index / (float) steps;
            final float hueB = (index + 1.0F) / (float) steps;
            final int colorA = 0xFF000000 | (Color.HSBtoRGB(hueA, 1.0F, 1.0F) & 0x00FFFFFF);
            final int colorB = 0xFF000000 | (Color.HSBtoRGB(hueB, 1.0F, 1.0F) & 0x00FFFFFF);
            UiRenderer.drawGradientRect(x + index * stepWidth, y, stepWidth + 1.0D, height, colorA, colorB, colorB, colorA);
        }
        UiRenderer.drawRoundedOutline(x - 0.5D, y - 0.5D, width + 1.0D, height + 1.0D, 3.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 28));
    }

    private void focusPanel(final CategoryPanel panel) {
        if (panel == null) {
            return;
        }

        if (this.panels.remove(panel)) {
            this.panels.add(panel);
        }
    }

    private String trimToWidth(final String text, final int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }

        if (this.font.width(text) <= maxWidth) {
            return text;
        }

        String result = text;
        while (result.length() > 1 && this.font.width(result + "...") > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + "...";
    }

    private boolean isHudEditorActive() {
        final HudModule hudModule = this.runtime.getModuleManager().get(HudModule.class);
        return hudModule != null && hudModule.isExpanded();
    }

    private boolean tryStartHudDrag(final double mouseX, final double mouseY) {
        final HudRenderer renderer = this.runtime.getHudRenderer();
        final HudRenderer.HudArea watermarkArea = renderer.getWatermarkArea();
        if (watermarkArea.isVisible() && watermarkArea.contains(mouseX, mouseY)) {
            this.activeHudDrag = HudDragTarget.WATERMARK;
            this.hudDragOffsetX = mouseX - watermarkArea.getX();
            this.hudDragOffsetY = mouseY - watermarkArea.getY();
            return true;
        }

        final HudRenderer.HudArea modulesArea = renderer.getModulesArea();
        if (modulesArea.isVisible() && modulesArea.contains(mouseX, mouseY)) {
            this.activeHudDrag = HudDragTarget.MODULES;
            this.hudDragOffsetX = mouseX - modulesArea.getX();
            this.hudDragOffsetY = mouseY - modulesArea.getY();
            return true;
        }

        final HudRenderer.HudArea radarArea = renderer.getRadarArea();
        if (radarArea.isVisible() && radarArea.contains(mouseX, mouseY)) {
            this.activeHudDrag = HudDragTarget.RADAR;
            this.hudDragOffsetX = mouseX - radarArea.getX();
            this.hudDragOffsetY = mouseY - radarArea.getY();
            return true;
        }

        final HudRenderer.HudArea deathArea = renderer.getDeathCoordsArea();
        if (deathArea.isVisible() && deathArea.contains(mouseX, mouseY)) {
            this.activeHudDrag = HudDragTarget.DEATH_COORDS;
            this.hudDragOffsetX = mouseX - deathArea.getX();
            this.hudDragOffsetY = mouseY - deathArea.getY();
            return true;
        }

        final HudRenderer.HudArea statusArea = renderer.getStatusEffectsArea();
        if (statusArea.isVisible() && statusArea.contains(mouseX, mouseY)) {
            this.activeHudDrag = HudDragTarget.STATUS_EFFECTS;
            this.hudDragOffsetX = mouseX - statusArea.getX();
            this.hudDragOffsetY = mouseY - statusArea.getY();
            return true;
        }

        return false;
    }

    private void updateHudDrag(final double mouseX, final double mouseY) {
        final HudModule hudModule = this.runtime.getModuleManager().get(HudModule.class);
        if (hudModule == null || this.activeHudDrag == null) {
            return;
        }

        final HudRenderer renderer = this.runtime.getHudRenderer();
        final int screenWidth = this.minecraft == null ? this.width : this.minecraft.getWindow().getGuiScaledWidth();
        final int screenHeight = this.minecraft == null ? this.height : this.minecraft.getWindow().getGuiScaledHeight();

        if (this.activeHudDrag == HudDragTarget.WATERMARK) {
            final HudRenderer.HudArea area = renderer.getWatermarkArea();
            final double newX = MathUtil.clamp(mouseX - this.hudDragOffsetX, 0.0D, screenWidth - area.getWidth());
            final double newY = MathUtil.clamp(mouseY - this.hudDragOffsetY, 0.0D, screenHeight - area.getHeight());
            final WatermarkModule watermarkModule = this.runtime.getModuleManager().get(WatermarkModule.class);
            if (watermarkModule != null) {
                watermarkModule.setPosition(newX, newY);
            }
            return;
        }

        final HudRenderer.HudArea area = renderer.getModulesArea();
        final double newX = MathUtil.clamp(mouseX - this.hudDragOffsetX, 0.0D, screenWidth - area.getWidth());
        final double newY = MathUtil.clamp(mouseY - this.hudDragOffsetY, 0.0D, screenHeight - area.getHeight());
        if (this.activeHudDrag == HudDragTarget.MODULES) {
            hudModule.setModulesPosition(newX, newY);
            return;
        }

        if (this.activeHudDrag == HudDragTarget.RADAR) {
            final RadarModule radarModule = this.runtime.getModuleManager().get(RadarModule.class);
            if (radarModule != null) {
                final HudRenderer.HudArea radarArea = renderer.getRadarArea();
                radarModule.setPosition(
                    MathUtil.clamp(mouseX - this.hudDragOffsetX, 0.0D, screenWidth - radarArea.getWidth()),
                    MathUtil.clamp(mouseY - this.hudDragOffsetY, 0.0D, screenHeight - radarArea.getHeight())
                );
            }
            return;
        }

        if (this.activeHudDrag == HudDragTarget.DEATH_COORDS) {
            final DeathCoordsModule deathCoordsModule = this.runtime.getModuleManager().get(DeathCoordsModule.class);
            if (deathCoordsModule != null) {
                final HudRenderer.HudArea deathArea = renderer.getDeathCoordsArea();
                deathCoordsModule.setPosition(
                    MathUtil.clamp(mouseX - this.hudDragOffsetX, 0.0D, screenWidth - deathArea.getWidth()),
                    MathUtil.clamp(mouseY - this.hudDragOffsetY, 0.0D, screenHeight - deathArea.getHeight())
                );
            }
            return;
        }

        if (this.activeHudDrag == HudDragTarget.STATUS_EFFECTS) {
            final StatusEffectsModule statusEffectsModule = this.runtime.getModuleManager().get(StatusEffectsModule.class);
            if (statusEffectsModule != null) {
                final HudRenderer.HudArea statusArea = renderer.getStatusEffectsArea();
                statusEffectsModule.setPosition(
                    MathUtil.clamp(mouseX - this.hudDragOffsetX, 0.0D, screenWidth - statusArea.getWidth()),
                    MathUtil.clamp(mouseY - this.hudDragOffsetY, 0.0D, screenHeight - statusArea.getHeight())
                );
            }
            return;
        }

        hudModule.setModulesPosition(newX, newY);
    }

    private void drawClippedScaledText(final MatrixStack matrixStack, final String text, final double x, final double y,
                                       final double clipWidth, final double clipHeight, final double scale, final int color) {
        if (clipWidth <= 0.0D || clipHeight <= 0.0D || text.isEmpty()) {
            return;
        }

        UiRenderer.scissorStart(x - 1.0D, y - 1.0D, clipWidth + 2.0D, clipHeight + 2.0D);
        UiRenderer.push();
        UiRenderer.scale(scale, scale, 1.0D);
        UiRenderer.drawText(matrixStack, text, (float) (x / scale), (float) (y / scale), color);
        UiRenderer.pop();
        UiRenderer.scissorEnd();
    }

    private enum HitType {
        HEADER,
        MODULE,
        BOOLEAN_SETTING,
        BIND_SETTING,
        NUMBER_SETTING,
        MODE_SETTING,
        COLOR_SETTING
    }

    private enum ColorControl {
        SATURATION_BRIGHTNESS,
        HUE,
        ALPHA
    }

    private enum HudDragTarget {
        WATERMARK,
        MODULES,
        RADAR,
        DEATH_COORDS,
        STATUS_EFFECTS
    }

    private static final class CategoryPanel {
        private final Category category;
        private final double width;
        private final double height;
        private final int index;

        private double x;
        private double y;
        private double renderX;
        private double renderY;
        private double contentX;
        private double contentY;
        private double contentWidth;
        private double contentVisibleHeight;
        private double contentHeight;
        private double scroll;
        private double targetScroll;
        private double minScroll;

        private CategoryPanel(final Category category, final double x, final double y, final double width, final double height, final int index) {
            this.category = category;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.index = index;
        }
    }

    private static final class HitBox {
        private final HitType type;
        private final double x;
        private final double y;
        private final double width;
        private final double height;
        private final CategoryPanel panel;
        private final Module module;
        private final Setting<?> setting;
        private final double sliderX;
        private final double sliderWidth;

        private HitBox(final HitType type, final double x, final double y, final double width, final double height,
                       final CategoryPanel panel, final Module module, final Setting<?> setting,
                       final double sliderX, final double sliderWidth) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.panel = panel;
            this.module = module;
            this.setting = setting;
            this.sliderX = sliderX;
            this.sliderWidth = sliderWidth;
        }

        private boolean contains(final double mouseX, final double mouseY) {
            return MathUtil.within(mouseX, mouseY, this.x, this.y, this.width, this.height);
        }
    }
}
