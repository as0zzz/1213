package dev.mdk2.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.mdk2.client.config.ConfigManager;
import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.core.ThemeManager;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.modules.combat.AimAssistModule;
import dev.mdk2.client.modules.combat.AuraModule;
import dev.mdk2.client.modules.combat.AutoArmorModule;
import dev.mdk2.client.modules.combat.AutoLeaveModule;
import dev.mdk2.client.modules.combat.AutoPotionModule;
import dev.mdk2.client.modules.combat.AutoRespawnModule;
import dev.mdk2.client.modules.combat.AutoSwordModule;
import dev.mdk2.client.modules.combat.AutoTotemModule;
import dev.mdk2.client.modules.combat.CriticalsModule;
import dev.mdk2.client.modules.combat.CrystalAuraModule;
import dev.mdk2.client.modules.combat.FightBotModule;
import dev.mdk2.client.modules.combat.ProtectModule;
import dev.mdk2.client.modules.combat.TpAuraModule;
import dev.mdk2.client.modules.combat.TriggerBotModule;
import dev.mdk2.client.modules.combat.VelocityModule;
import dev.mdk2.client.modules.misc.AutoDropModule;
import dev.mdk2.client.modules.misc.AutoEatModule;
import dev.mdk2.client.modules.misc.AutoStealModule;
import dev.mdk2.client.modules.misc.DiscordRpcModule;
import dev.mdk2.client.modules.misc.ThemeModule;
import dev.mdk2.client.modules.movement.AutoFishModule;
import dev.mdk2.client.modules.movement.AutoMoveModule;
import dev.mdk2.client.modules.movement.BoatFlyModule;
import dev.mdk2.client.modules.movement.DolphinModule;
import dev.mdk2.client.modules.movement.FarmBotModule;
import dev.mdk2.client.modules.movement.FastLadderModule;
import dev.mdk2.client.modules.movement.FlightModule;
import dev.mdk2.client.modules.movement.HighJumpModule;
import dev.mdk2.client.modules.movement.JesusModule;
import dev.mdk2.client.modules.movement.NoFallModule;
import dev.mdk2.client.modules.movement.NoSlowModule;
import dev.mdk2.client.modules.movement.NukerModule;
import dev.mdk2.client.modules.movement.PhaseModule;
import dev.mdk2.client.modules.movement.SafeWalkModule;
import dev.mdk2.client.modules.movement.ScaffoldModule;
import dev.mdk2.client.modules.movement.SpeedModule;
import dev.mdk2.client.modules.movement.SpiderModule;
import dev.mdk2.client.modules.movement.SprintAssistModule;
import dev.mdk2.client.modules.movement.StepModule;
import dev.mdk2.client.modules.visual.DeathCoordsModule;
import dev.mdk2.client.modules.visual.FakeWeatherModule;
import dev.mdk2.client.modules.visual.FullbrightModule;
import dev.mdk2.client.modules.visual.HitChamsModule;
import dev.mdk2.client.modules.visual.HudModule;
import dev.mdk2.client.modules.visual.MenuParticlesModule;
import dev.mdk2.client.modules.visual.NoFireOverlayModule;
import dev.mdk2.client.modules.visual.NoFogModule;
import dev.mdk2.client.modules.visual.NoWeatherModule;
import dev.mdk2.client.modules.visual.ParticlesModule;
import dev.mdk2.client.modules.visual.StatusEffectsModule;
import dev.mdk2.client.modules.visual.TargetEspModule;
import dev.mdk2.client.modules.visual.TrajectoryModule;
import dev.mdk2.client.modules.visual.ViewModelModule;
import dev.mdk2.client.modules.visual.WatermarkModule;
import dev.mdk2.client.modules.visual.ZoomModule;
import dev.mdk2.client.render.HudRenderer;
import dev.mdk2.client.render.MenuFontRenderer;
import dev.mdk2.client.render.UiRenderer;
import dev.mdk2.client.settings.BindSetting;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ColorSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.settings.Setting;
import dev.mdk2.client.util.AnimationUtil;
import dev.mdk2.client.util.ColorUtil;
import dev.mdk2.client.util.MathUtil;
import dev.mdk2.client.modules.xray.BlockEspModule;
import dev.mdk2.client.modules.xray.EntityEspModule;
import dev.mdk2.client.modules.xray.FreecamModule;
import dev.mdk2.client.modules.xray.RadarModule;
import dev.mdk2.client.modules.xray.SearchModule;
import dev.mdk2.client.modules.xray.StorageEspModule;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class ModernClickGuiScreen extends Screen {
    private static final int WINDOW = ColorUtil.rgba(10, 12, 18, 228);
    private static final int SIDEBAR = ColorUtil.rgba(8, 10, 15, 206);
    private static final int SURFACE = ColorUtil.rgba(18, 20, 28, 236);
    private static final int SURFACE_RAISED = ColorUtil.rgba(26, 29, 40, 242);
    private static final int SURFACE_HOVER = ColorUtil.rgba(34, 38, 52, 248);
    private static final int OUTLINE = ColorUtil.rgba(255, 255, 255, 14);
    private static final int DIVIDER = ColorUtil.rgba(255, 255, 255, 22);
    private static final int DIVIDER_SOFT = ColorUtil.rgba(255, 255, 255, 12);
    private static final int TEXT = ColorUtil.rgba(239, 243, 255, 255);
    private static final int TEXT_SOFT = ColorUtil.rgba(144, 153, 177, 255);
    private static final int TEXT_MUTED = ColorUtil.rgba(104, 112, 135, 255);
    private static final int ACCENT = ColorUtil.rgba(118, 146, 255, 255);
    private static final int SHADOW = ColorUtil.rgba(0, 0, 0, 72);
    private static final double DEBUG_TITLE_SCALE = 0.78D;
    private static final double DEBUG_SUBTITLE_SCALE = 0.58D;
    private static final double DEBUG_TAB_SCALE = 0.54D;
    private static final double DEBUG_FIELD_SCALE = 0.60D;
    private static final double DEBUG_VALUE_SCALE = 0.58D;
    private static final double DEBUG_HELP_TITLE_SCALE = 0.64D;
    private static final double DEBUG_HELP_TEXT_SCALE = 0.54D;
    private static final double DEBUG_STATUS_SCALE = 0.52D;
    private static final double FUNCTION_POPUP_WIDTH = 236.0D;
    private static final double FUNCTION_POPUP_MIN_HEIGHT = 172.0D;
    private static final String[] PROFILE_SCALE_OPTIONS = new String[]{"Auto", "75%", "100%", "125%", "150%", "175%", "200%"};
    private static final String[] PROFILE_LANGUAGE_OPTIONS = new String[]{"English", "Russian"};
    private static final String[] PROFILE_UNIT_OPTIONS = new String[]{"Auto", "Metric", "Imperial"};
    private static final String[] PROFILE_SAFE_MODE_OPTIONS = new String[]{"Automatic", "Balanced", "Strict"};

    private final ClientRuntime runtime;
    private final List<ClickTarget> clickTargets = new ArrayList<ClickTarget>();
    private final List<ClickTarget> debugClickTargets = new ArrayList<ClickTarget>();
    private final MenuDebugProfile debugProfile = MenuDebugProfile.defaults();
    private final MenuDebugOverlayState debugOverlayState = new MenuDebugOverlayState(0.0D, 0.0D);
    private Layout renderLayout;
    private MenuFontRenderer menuFont;
    private MenuFontRenderer debugFont;
    private double cachedFontSize = -1.0D;
    private boolean debugOverlayInitialized;
    private final MenuProfileSettings profileSettings = MenuProfileSettings.defaults();

    private Page page = Page.RAGE;
    private VisualPage visualPage = VisualPage.PLAYERS;
    private Dropdown openDropdown;
    private SliderId activeSlider;
    private SliderTrack activeSliderTrack;
    private ViewPopup viewPopup = ViewPopup.NONE;
    private FunctionPopupState functionPopup;
    private PopupModeDropdown popupModeDropdown;
    private Module pendingFunctionPopupModule;
    private ColorSlot activeColorSlot;
    private ColorDrag colorDrag = ColorDrag.NONE;
    private NumberSetting activePopupSliderSetting;
    private PopupSliderTrack activePopupSliderTrack;
    private BindSetting activePopupBindSetting;
    private ColorSetting activePopupColorSetting;
    private ColorDrag activePopupColorDrag = ColorDrag.NONE;
    private double popupColorPickerX;
    private double popupColorPickerY;
    private double popupColorPickerWidth;
    private double popupColorPickerHeight;
    private boolean popupColorPickerVisible;
    private boolean presetInfoVisible;
    private boolean presetsVisible;
    private boolean profileVisible;
    private boolean profileScaleVisible;
    private boolean searchVisible;
    private double introAnimation;
    private String searchQuery = "";
    private String configSearchQuery = "";
    private TextInputContext activeTextInput = TextInputContext.NONE;
    private int enemyTab;
    private boolean debugEditorVisible;
    private boolean draggingDebugEditor;
    private boolean draggingMainMenu;
    private double debugDragOffsetX;
    private double debugDragOffsetY;
    private double mainMenuDragOffsetX;
    private double mainMenuDragOffsetY;
    private HudDragTarget activeHudDrag;
    private double hudDragOffsetX;
    private double hudDragOffsetY;
    private boolean hudDragSnapX;
    private boolean hudDragSnapY;
    private DebugSection debugSection = DebugSection.TYPOGRAPHY;
    private String debugStatus = "F6 to toggle";
    private List<DebugField> typographyFields;
    private List<DebugField> windowFields;
    private List<DebugField> sidebarFields;
    private List<DebugField> topbarFields;
    private List<DebugField> layoutFields;
    private List<DebugField> panelFields;
    private List<DebugField> brandingFields;
    private List<DebugField> footerFields;
    private List<DebugField> controlFields;
    private boolean debugProfileDirty;
    private RemoteClientPlayerEntity previewSteveEntity;

    private boolean rageEnabled;
    private boolean silentAim;
    private boolean automaticFire = true;
    private boolean aimThroughWalls = true;
    private boolean quickStop;
    private boolean quickScope;
    private boolean delayShot;
    private boolean removeRecoil;
    private boolean removeSpread;
    private boolean duckPeekAssist;
    private boolean quickPeekAssist;
    private boolean doubleTap;
    private boolean antiAimEnabled;
    private double fieldOfView = 180.0D;
    private double hitChance = 65.0D;
    private double minDamage = 22.0D;
    private int historyIndex;
    private int targetIndex;
    private int hitboxesIndex;
    private int multipointIndex;

    private boolean legitEnabled = true;
    private boolean recoilControl = true;
    private boolean backtrackAssist;
    private boolean visibleOnly = true;
    private boolean throughSmoke;
    private boolean magnetTrigger = true;
    private boolean slowWalk = true;
    private boolean autoScope = true;
    private boolean quickBuy;
    private boolean autoRebuy = true;
    private boolean favoriteSlots = true;
    private boolean miscDiscord = true;
    private boolean miscNotifications = true;
    private boolean miscCloudSync = true;
    private boolean miscAutoBackup = true;
    private boolean miscCompactLogs = true;
    private int profileLanguageIndex;
    private int profileMenuScaleIndex = 4;
    private int profileEspScaleIndex = 4;
    private int profileWindowsScaleIndex;
    private int profileUnitsIndex;
    private int profileSafeModeIndex;
    private boolean profileStyleEnabled = true;
    private boolean profileSynchronizationEnabled = true;
    private double previewEspOffsetX;
    private double previewEspOffsetY;
    private double legitSmoothing = 34.0D;
    private double legitReaction = 86.0D;
    private double legitFov = 4.5D;
    private double legitBurst = 2.0D;
    private double wearOverride = 18.0D;
    private double animationSpeed = 64.0D;
    private int legitTargetIndex = 1;
    private int legitBoneIndex = 2;
    private int stickerIndex = 1;
    private int sortingIndex = 1;
    private int miscStyleIndex = 1;

    private boolean visualsEnabled = true;
    private boolean visualsOffscreen;
    private boolean visualsSounds;
    private boolean soulParticles = true;
    private boolean glow = true;
    private int playerModeIndex;
    private int wallsModeIndex = 1;
    private int onShotModeIndex;
    private int historyModeIndex;
    private int ragdollModeIndex;

    private final PickerColor primaryColor = new PickerColor(0.63F, 0.42F, 1.0F, 1.0F);
    private final PickerColor wallColor = new PickerColor(0.50F, 0.72F, 0.93F, 1.0F);
    private final PickerColor historyColor = new PickerColor(0.71F, 0.18F, 0.93F, 0.70F);
    private final PickerColor soulColor = new PickerColor(0.66F, 0.61F, 0.96F, 1.0F);
    private final PickerColor glowColor = new PickerColor(0.69F, 0.18F, 1.0F, 0.92F);

    public ModernClickGuiScreen(final ClientRuntime runtime) {
        super(new StringTextComponent("Mdk2 Menu"));
        this.runtime = runtime;
        initDebugFields();
    }

    @Override
    protected void init() {
        super.init();
        loadDebugProfile();
        loadProfileSettings();
        this.activeSlider = null;
        this.activeSliderTrack = null;
        this.colorDrag = ColorDrag.NONE;
        this.activePopupSliderSetting = null;
        this.activePopupSliderTrack = null;
        this.activePopupBindSetting = null;
        this.activePopupColorSetting = null;
        this.activePopupColorDrag = ColorDrag.NONE;
        this.popupColorPickerVisible = false;
        this.introAnimation = 0.0D;
        this.activeHudDrag = null;
        this.hudDragSnapX = false;
        this.hudDragSnapY = false;
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.clickTargets.clear();
        this.debugClickTargets.clear();
        this.introAnimation = AnimationUtil.smooth(this.introAnimation, 1.0D, 0.14D);
        final Layout layout = createLayout();
        this.renderLayout = layout;
        final double virtualMouseX = toVirtualX(layout, mouseX);
        final double virtualMouseY = toVirtualY(layout, mouseY);
        renderBackdrop(matrixStack, layout);
        UiRenderer.push();
        UiRenderer.translate(layout.originX, layout.originY, 0.0D);
        UiRenderer.scale(layout.scale, layout.scale, 1.0D);
        renderWindow(matrixStack, layout, (int) virtualMouseX, (int) virtualMouseY);
        UiRenderer.pop();
        if (isHudEditorActive()) {
            this.runtime.getHudRenderer().renderEditorPreview(matrixStack);
        }
        UiRenderer.push();
        UiRenderer.translate(layout.originX, layout.originY, 0.0D);
        UiRenderer.scale(layout.scale, layout.scale, 1.0D);
        renderWindowOverlays(matrixStack, layout);
        UiRenderer.pop();
        if (this.activeHudDrag != null) {
            renderHudSnapGuides();
        }
        if (this.debugEditorVisible) {
            renderDebugEditorOverlay(matrixStack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final Layout layout = createLayout();
        final double virtualMouseX = toVirtualX(layout, mouseX);
        final double virtualMouseY = toVirtualY(layout, mouseY);
        if (this.activePopupBindSetting != null) {
            this.activePopupBindSetting.setKey(BindSetting.fromMouseButton(button));
            this.activePopupBindSetting = null;
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            for (int i = this.debugClickTargets.size() - 1; i >= 0; i--) {
                final ClickTarget target = this.debugClickTargets.get(i);
                if (target.button == button && target.contains(mouseX, mouseY)) {
                    target.action.run();
                    return true;
                }
            }
        }
        if (this.debugEditorVisible && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            final DebugOverlayFrame frame = debugOverlayFrame();
            if (MathUtil.within(mouseX, mouseY, frame.x, frame.y, frame.width, frame.height)) {
                this.draggingDebugEditor = true;
                this.debugDragOffsetX = mouseX - frame.x;
                this.debugDragOffsetY = mouseY - frame.y;
                return true;
            }
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            return super.mouseClicked(virtualMouseX, virtualMouseY, button);
        }
        if (handlePopupColorPickerClick(virtualMouseX, virtualMouseY, button)) {
            return true;
        }
        if (handlePopupModeDropdownClick(virtualMouseX, virtualMouseY, button)) {
            return true;
        }
        if (handleColorPickerClick(virtualMouseX, virtualMouseY, button)) {
            return true;
        }
        if (handleDropdownClick(virtualMouseX, virtualMouseY, button)) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && isHudEditorActive() && tryOpenHudWidgetPopup(mouseX, mouseY, layout)) {
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isHudEditorActive() && tryStartHudDrag(mouseX, mouseY)) {
            return true;
        }
        for (int i = this.clickTargets.size() - 1; i >= 0; i--) {
            final ClickTarget target = this.clickTargets.get(i);
            if (target.button == button && target.contains(virtualMouseX, virtualMouseY)) {
                target.action.run();
                return true;
            }
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
            && insideMainWindow(mouseX, mouseY, layout)
            && !isInsideAnyNonSearchPopup(virtualMouseX, virtualMouseY, layout)
            && !hasTransientPopupOpen()) {
            this.draggingMainMenu = true;
            this.mainMenuDragOffsetX = mouseX - layout.originX;
            this.mainMenuDragOffsetY = mouseY - layout.originY;
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (isInsideAnyNonSearchPopup(virtualMouseX, virtualMouseY, layout)) {
                return true;
            }
            closeTransientPopups();
        }
        this.openDropdown = null;
        this.activeColorSlot = null;
        this.colorDrag = ColorDrag.NONE;
        this.activePopupColorSetting = null;
        this.activePopupColorDrag = ColorDrag.NONE;
        this.popupColorPickerVisible = false;
        return super.mouseClicked(virtualMouseX, virtualMouseY, button);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        this.activeSlider = null;
        this.activeSliderTrack = null;
        this.colorDrag = ColorDrag.NONE;
        this.activePopupSliderSetting = null;
        this.activePopupSliderTrack = null;
        this.activePopupColorDrag = ColorDrag.NONE;
        this.draggingDebugEditor = false;
        this.draggingMainMenu = false;
        this.activeHudDrag = null;
        this.hudDragSnapX = false;
        this.hudDragSnapY = false;
        final Layout layout = createLayout();
        return super.mouseReleased(toVirtualX(layout, mouseX), toVirtualY(layout, mouseY), button);
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double dragX, final double dragY) {
        final Layout layout = createLayout();
        final double virtualMouseX = toVirtualX(layout, mouseX);
        final double virtualMouseY = toVirtualY(layout, mouseY);
        if (this.draggingDebugEditor && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            final DebugOverlayFrame frame = debugOverlayFrame();
            this.debugOverlayState.x = mouseX - this.debugDragOffsetX;
            this.debugOverlayState.y = mouseY - this.debugDragOffsetY;
            clampDebugOverlayOnDrag(frame.width, frame.height);
            this.debugProfileDirty = true;
            return true;
        }
        if (this.activeHudDrag != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            updateHudDrag(mouseX, mouseY);
            return true;
        }
        if (this.draggingMainMenu && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            final double baseOriginX = (this.width - layout.width * layout.scale) / 2.0D;
            final double baseOriginY = (this.height - layout.height * layout.scale) / 2.0D;
            this.debugProfile.menuOffsetX = mouseX - this.mainMenuDragOffsetX - baseOriginX;
            this.debugProfile.menuOffsetY = mouseY - this.mainMenuDragOffsetY - baseOriginY;
            this.debugProfileDirty = true;
            return true;
        }
        if (this.activeSlider != null) {
            updateSlider(virtualMouseX);
            return true;
        }
        if (this.activePopupSliderSetting != null) {
            updatePopupSlider(virtualMouseX);
            return true;
        }
        if (this.activePopupColorSetting != null && this.activePopupColorDrag != ColorDrag.NONE) {
            updatePopupColorPicker(virtualMouseX, virtualMouseY);
            return true;
        }
        if (this.activeColorSlot != null && this.colorDrag != ColorDrag.NONE) {
            updateColor(virtualMouseX, virtualMouseY);
            return true;
        }
        return super.mouseDragged(virtualMouseX, virtualMouseY, button, dragX / layout.scale, dragY / layout.scale);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double delta) {
        final Layout layout = createLayout();
        final double virtualMouseX = toVirtualX(layout, mouseX);
        final double virtualMouseY = toVirtualY(layout, mouseY);
        if (this.functionPopup != null
            && this.functionPopup.scrollMax > 0.0D
            && MathUtil.within(virtualMouseX, virtualMouseY, this.functionPopup.x, this.functionPopup.y, this.functionPopup.width, this.functionPopup.height)) {
            this.functionPopup.scrollOffset = MathUtil.clamp(this.functionPopup.scrollOffset - delta * 16.0D, 0.0D, this.functionPopup.scrollMax);
            return true;
        }
        return super.mouseScrolled(virtualMouseX, virtualMouseY, delta);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (this.activePopupBindSetting != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                this.activePopupBindSetting.setKey(BindSetting.NONE);
            } else {
                this.activePopupBindSetting.setKey(keyCode);
            }
            this.activePopupBindSetting = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_F6) {
            this.debugEditorVisible = !this.debugEditorVisible;
            this.debugStatus = this.debugEditorVisible ? "Debug editor enabled" : "Debug editor hidden";
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.searchVisible) {
                this.searchVisible = false;
                this.activeTextInput = TextInputContext.NONE;
                return true;
            }
            if (this.debugEditorVisible) {
                this.debugEditorVisible = false;
                return true;
            }
            if (this.profileVisible) {
                this.profileVisible = false;
                this.profileScaleVisible = false;
                return true;
            }
            if (this.presetsVisible) {
                this.presetsVisible = false;
                this.activeTextInput = TextInputContext.NONE;
                return true;
            }
            if (this.activeColorSlot != null) {
                this.activeColorSlot = null;
                return true;
            }
            if (this.activePopupColorSetting != null) {
                this.activePopupColorSetting = null;
                this.activePopupColorDrag = ColorDrag.NONE;
                this.popupColorPickerVisible = false;
                return true;
            }
        }
        if (this.activeTextInput == TextInputContext.MENU_SEARCH || this.activeTextInput == TextInputContext.CONFIG_SEARCH) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (this.activeTextInput == TextInputContext.MENU_SEARCH) {
                    if (!this.searchQuery.isEmpty()) {
                        this.searchQuery = this.searchQuery.substring(0, this.searchQuery.length() - 1);
                    }
                } else if (!this.configSearchQuery.isEmpty()) {
                    this.configSearchQuery = this.configSearchQuery.substring(0, this.configSearchQuery.length() - 1);
                }
            }
            return true;
        }
        if (this.debugEditorVisible) {
            if (keyCode == GLFW.GLFW_KEY_S && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                saveDebugProfile();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_C && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                copyDebugProfile();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_R && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                resetDebugProfile();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(final char codePoint, final int modifiers) {
        if (this.activeTextInput == TextInputContext.MENU_SEARCH && !Character.isISOControl(codePoint)) {
            this.searchQuery += codePoint;
            return true;
        }
        if (this.activeTextInput == TextInputContext.CONFIG_SEARCH && !Character.isISOControl(codePoint)) {
            this.configSearchQuery += codePoint;
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        saveProfileSettings();
        if (this.debugProfileDirty) {
            saveDebugProfile();
        }
        super.removed();
    }

    private void renderBackdrop(final MatrixStack matrixStack, final Layout layout) {
        final boolean inWorld = this.minecraft != null && this.minecraft.level != null;
        if (themeManager().isGlass()) {
            fill(matrixStack, 0, 0, this.width, this.height, ColorUtil.rgba(24, 30, 42, themeManager().backdropBaseAlpha(inWorld)));
            UiRenderer.drawGradientRect(0.0D, 0.0D, this.width, this.height,
                ColorUtil.rgba(140, 188, 255, themeManager().backdropTopAlpha(inWorld)),
                ColorUtil.rgba(108, 158, 255, Math.max(12, themeManager().backdropTopAlpha(inWorld) - 6)),
                ColorUtil.rgba(10, 16, 28, themeManager().backdropBottomAlpha(inWorld)),
                ColorUtil.rgba(18, 22, 36, Math.max(0, themeManager().backdropBottomAlpha(inWorld) - 8)));
            return;
        }
        fill(matrixStack, 0, 0, this.width, this.height, ColorUtil.rgba(4, 6, 10, themeManager().backdropBaseAlpha(inWorld)));
        UiRenderer.drawGradientRect(0.0D, 0.0D, this.width, this.height,
            ColorUtil.rgba(22, 28, 40, themeManager().backdropTopAlpha(inWorld)),
            ColorUtil.rgba(22, 28, 40, themeManager().backdropTopAlpha(inWorld)),
            ColorUtil.rgba(4, 5, 8, themeManager().backdropBottomAlpha(inWorld)),
            ColorUtil.rgba(4, 5, 8, themeManager().backdropBottomAlpha(inWorld)));
    }

    private void renderWindow(final MatrixStack matrixStack, final Layout layout, final int mouseX, final int mouseY) {
        UiRenderer.drawRoundedRect(layout.x, layout.y, layout.width, layout.height, this.debugProfile.windowRadius, windowColor());
        UiRenderer.drawRoundedOutline(layout.x, layout.y, layout.width, layout.height, this.debugProfile.windowRadius, themeManager().windowOutlineWidth(), outlineColor());

        UiRenderer.drawRoundedRect(layout.x, layout.y, layout.sidebarWidth, layout.height, this.debugProfile.windowRadius, sidebarColor());
        UiRenderer.drawRoundedOutline(layout.x, layout.y, layout.sidebarWidth, layout.height, this.debugProfile.windowRadius, themeManager().containerOutlineWidth(), containerOutlineColor());
        drawShellOuterFrame(layout.x, layout.y, layout.width, layout.height, this.debugProfile.windowRadius);

        UiRenderer.scissorStart(layout.originX, layout.originY, layout.width * layout.scale, layout.height * layout.scale);
        renderSidebar(matrixStack, layout);
        renderTopBar(matrixStack, layout);
        renderPage(matrixStack, layout);
        renderFooter(matrixStack, layout);
        UiRenderer.scissorEnd();
    }

    private void renderWindowOverlays(final MatrixStack matrixStack, final Layout layout) {
        if (this.presetInfoVisible) {
            renderPresetInfo(matrixStack, layout);
        }
        if (this.presetsVisible) {
            renderPresetsPopup(matrixStack, layout);
        }
        if (this.profileVisible) {
            renderProfilePopup(matrixStack, layout);
        }
        if (this.searchVisible) {
            renderSearchPopup(matrixStack, layout);
        }
        if (this.functionPopup != null) {
            renderFunctionPopup(matrixStack, layout);
        }
        if (this.popupModeDropdown != null) {
            renderPopupModeDropdown(matrixStack);
        }
        if (this.activePopupColorSetting != null && this.popupColorPickerVisible) {
            renderPopupColorPicker(matrixStack);
        }
        if (this.viewPopup != ViewPopup.NONE) {
            renderViewPopup(matrixStack, layout);
        }
        if (this.activeColorSlot != null) {
            renderColorPicker(matrixStack, layout);
        }
        if (this.openDropdown != null) {
            renderDropdownPopup(matrixStack);
        }
    }

    private void renderSidebar(final MatrixStack matrixStack, final Layout layout) {
        final double x = layout.x + this.debugProfile.sidebarXPadding;
        double y = layout.y + this.debugProfile.sidebarTopPadding;
        drawLogo(matrixStack, x, y);
        y += 72.0D;
        final double dividerWidth = Math.max(72.0D, layout.sidebarWidth - this.debugProfile.sidebarXPadding * 2.0D - 4.0D);

        drawMiniText(matrixStack, "AIMBOT", x + 2.0D, y, TEXT_MUTED, 0.84D);
        renderSidebarSeparator(x, y + 14.0D, dividerWidth);
        y += 20.0D;
        renderSidebarEntry(matrixStack, x, y, "Rage", this.page == Page.RAGE, new Runnable() {
            @Override
            public void run() {
                page = Page.RAGE;
            }
        });
        renderSidebarSeparator(x, y + this.debugProfile.sidebarEntrySpacing * 0.55D, dividerWidth);
        y += this.debugProfile.sidebarEntrySpacing;
        renderSidebarEntry(matrixStack, x, y, "Legit", this.page == Page.LEGIT, new Runnable() {
            @Override
            public void run() {
                page = Page.LEGIT;
            }
        });
        renderSidebarSeparator(x, y + 28.0D, dividerWidth);
        y += 52.0D;

        drawMiniText(matrixStack, "COMMON", x + 2.0D, y, TEXT_MUTED, 0.84D);
        renderSidebarSeparator(x, y + 14.0D, dividerWidth);
        y += 20.0D;
        renderSidebarEntry(matrixStack, x, y, "Visuals", this.page == Page.VISUALS, new Runnable() {
            @Override
            public void run() {
                page = Page.VISUALS;
            }
        });
        renderSidebarSeparator(x, y + this.debugProfile.sidebarEntrySpacing * 0.45D, dividerWidth);
        y += this.debugProfile.sidebarEntrySpacing - 4.0D;
        if (this.page == Page.VISUALS) {
            renderSidebarSubEntry(matrixStack, x + 20.0D, y - 1.0D, "Players", this.visualPage == VisualPage.PLAYERS, new Runnable() {
                @Override
                public void run() {
                    visualPage = VisualPage.PLAYERS;
                }
            });
            renderSidebarSeparator(x + 20.0D, y + this.debugProfile.sidebarSubEntrySpacing * 0.5D, dividerWidth - 20.0D);
            y += this.debugProfile.sidebarSubEntrySpacing;
            renderSidebarSubEntry(matrixStack, x + 20.0D, y - 1.0D, "World", this.visualPage == VisualPage.WORLD, new Runnable() {
                @Override
                public void run() {
                    visualPage = VisualPage.WORLD;
                }
            });
            renderSidebarSeparator(x, y + this.debugProfile.sidebarSubEntrySpacing * 0.65D, dividerWidth);
            y += this.debugProfile.sidebarSubEntrySpacing + 1.0D;
        }
        renderSidebarEntry(matrixStack, x, y, "Movement", this.page == Page.INVENTORY, new Runnable() {
            @Override
            public void run() {
                page = Page.INVENTORY;
            }
        });
        renderSidebarSeparator(x, y + this.debugProfile.sidebarEntrySpacing * 0.55D, dividerWidth);
        y += this.debugProfile.sidebarEntrySpacing;
        renderSidebarEntry(matrixStack, x, y, "Miscellaneous", this.page == Page.MISC, new Runnable() {
            @Override
            public void run() {
                page = Page.MISC;
            }
        });
    }

    private void renderSidebarEntry(final MatrixStack matrixStack, final double x, final double y, final String label, final boolean selected, final Runnable action) {
        final double width = sidebarEntryWidth(label, false);
        if (selected) {
            UiRenderer.drawRoundedRect(x - 2.0D, y - 4.0D, width, 28.0D, 8.0D, ColorUtil.rgba(255, 255, 255, 12));
            UiRenderer.drawRoundedRect(x - 6.0D, y + 4.0D, 3.0D, 10.0D, 2.0D, ACCENT);
        }
        UiRenderer.drawCircle(x + 8.0D, y + 9.5D, 3.6D, selected ? ACCENT : TEXT_SOFT);
        drawBodyText(matrixStack, label, x + 22.0D, y - 1.0D, selected ? TEXT : TEXT_SOFT);
        this.clickTargets.add(new ClickTarget(x - 2.0D, y - 4.0D, width, 28.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, action));
    }

    private void renderSidebarSubEntry(final MatrixStack matrixStack, final double x, final double y, final String label, final boolean selected, final Runnable action) {
        final double width = sidebarEntryWidth(label, true);
        if (selected) {
            UiRenderer.drawRoundedRect(x - 2.0D, y - 3.0D, width, 22.0D, 7.0D, ColorUtil.rgba(255, 255, 255, 7));
        }
        UiRenderer.drawCircle(x + 5.0D, y + 7.5D, 2.8D, selected ? ACCENT : TEXT_MUTED);
        drawMiniText(matrixStack, label, x + 14.0D, y, selected ? TEXT : TEXT_SOFT, 0.92D);
        this.clickTargets.add(new ClickTarget(x - 2.0D, y - 3.0D, width, 22.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, action));
    }

    private void renderTopBar(final MatrixStack matrixStack, final Layout layout) {
        final double y = layout.y + this.debugProfile.topbarYOffset;
        final double saveWidth = this.debugProfile.topbarButtonSize;
        renderSmallButton(layout.contentX, y, saveWidth, this.debugProfile.topbarButtonSize, true, new Runnable() {
            @Override
            public void run() {
                saveSelectedConfig();
            }
        });
        drawSaveIcon(layout.contentX + (saveWidth - 10.0D) / 2.0D, y + (this.debugProfile.topbarButtonSize - 10.0D) / 2.0D, TEXT);

        final double presetX = layout.contentX + saveWidth + this.debugProfile.topbarGap;
        final double presetWidth = this.debugProfile.topbarPresetWidth;
        renderSelector(matrixStack, presetX, y, presetWidth, configManager().getSelectedConfigName(), this.presetsVisible, new Runnable() {
            @Override
            public void run() {
                presetsVisible = !presetsVisible;
                activeTextInput = presetsVisible ? TextInputContext.CONFIG_SEARCH : TextInputContext.NONE;
            }
        });

        final double searchSize = Math.max(20.0D, this.debugProfile.topbarButtonSize - 2.0D);
        final double searchX = layout.x + layout.width - searchSize - 14.0D;
        renderSmallButton(searchX, y, searchSize, searchSize, false, new Runnable() {
            @Override
            public void run() {
                searchVisible = !searchVisible;
                activeTextInput = searchVisible ? TextInputContext.MENU_SEARCH : TextInputContext.NONE;
            }
        });
        drawSearchIcon(searchX + (searchSize - 12.0D) / 2.0D, y + (searchSize - 12.0D) / 2.0D, TEXT);
    }

    private void renderPage(final MatrixStack matrixStack, final Layout layout) {
        if (this.page == Page.RAGE) {
            renderRagePage(matrixStack, layout);
        } else if (this.page == Page.LEGIT) {
            renderLegitPage(matrixStack, layout);
        } else if (this.page == Page.VISUALS) {
            if (this.visualPage == VisualPage.PLAYERS) {
                renderPlayersPage(matrixStack, layout);
            } else {
                renderWorldPage(matrixStack, layout);
            }
        } else if (this.page == Page.INVENTORY) {
            renderInventoryPage(matrixStack, layout);
        } else {
            renderMiscPage(matrixStack, layout);
        }
    }

    private void renderRagePage(final MatrixStack matrixStack, final Layout layout) {
        final double leftLowerY = layout.contentY + panelHeight("MAIN") + this.debugProfile.panelStackGap;
        final double rightLowerY = layout.contentY + panelHeight("DEFENSE") + this.debugProfile.panelStackGap;
        renderPanel(matrixStack, layout.contentX, layout.contentY, layout.columnWidth, "MAIN");
        renderPanel(matrixStack, layout.contentX, leftLowerY, layout.columnWidth, "ASSIST");
        renderPanel(matrixStack, layout.contentX + layout.columnWidth + layout.columnGap, layout.contentY, layout.columnWidth, "DEFENSE");
        renderPanel(matrixStack, layout.contentX + layout.columnWidth + layout.columnGap, rightLowerY, layout.columnWidth, "AUTO");
    }

    private void renderLegitPage(final MatrixStack matrixStack, final Layout layout) {
        final double leftLowerY = layout.contentY + panelHeight("MAIN") + this.debugProfile.panelStackGap;
        final double rightLowerY = layout.contentY + panelHeight("SURVIVAL") + this.debugProfile.panelStackGap;
        renderPanel(matrixStack, layout.contentX, layout.contentY, layout.columnWidth, "MAIN");
        renderPanel(matrixStack, layout.contentX, leftLowerY, layout.columnWidth, "TRACK");
        renderPanel(matrixStack, layout.contentX + layout.columnWidth + layout.columnGap, layout.contentY, layout.columnWidth, "SURVIVAL");
        renderPanel(matrixStack, layout.contentX + layout.columnWidth + layout.columnGap, rightLowerY, layout.columnWidth, "UTILITY");
    }

    private void renderPlayersPage(final MatrixStack matrixStack, final Layout layout) {
        final double leftLowerY = layout.contentY + panelHeight("ENEMY") + this.debugProfile.panelStackGap;
        final double effectsY = MenuLayoutMath.playersEffectsY(layout.contentY, panelHeight("ENEMY"), panelHeight("ENEMY MODEL"), this.debugProfile.panelStackGap);
        final double previewY = MenuLayoutMath.playersPreviewY(layout.contentY);
        renderPanel(matrixStack, layout.contentX, layout.contentY, layout.columnWidth, "ENEMY");
        renderPanel(matrixStack, layout.contentX, leftLowerY, layout.columnWidth, "ENEMY MODEL");
        renderPanel(matrixStack, layout.contentX, effectsY, layout.columnWidth, "EFFECTS");
        renderPanel(matrixStack, layout.contentX + layout.columnWidth + layout.columnGap, previewY, layout.columnWidth, "PREVIEW");
    }

    private void renderWorldPage(final MatrixStack matrixStack, final Layout layout) {
        final double lowerY = layout.contentY + panelHeight("VIEW") + this.debugProfile.panelStackGap;
        renderPanel(matrixStack, layout.contentX, layout.contentY, layout.columnWidth, "VIEW");
        renderPanel(matrixStack, layout.contentX, lowerY, layout.columnWidth, "WORLD ESP");
        renderPanel(matrixStack, layout.contentX + layout.columnWidth + layout.columnGap, layout.contentY, layout.columnWidth, "MISCELLANEOUS");
    }

    private void renderInventoryPage(final MatrixStack matrixStack, final Layout layout) {
        final double leftLowerY = layout.contentY + panelHeight("MOTION") + this.debugProfile.panelStackGap;
        final double rightLowerY = layout.contentY + panelHeight("WORLD") + this.debugProfile.panelStackGap;
        renderPanel(matrixStack, layout.contentX, layout.contentY, layout.columnWidth, "MOTION");
        renderPanel(matrixStack, layout.contentX, leftLowerY, layout.columnWidth, "POSITION");
        renderPanel(matrixStack, layout.contentX + layout.columnWidth + layout.columnGap, layout.contentY, layout.columnWidth, "WORLD");
        renderPanel(matrixStack, layout.contentX + layout.columnWidth + layout.columnGap, rightLowerY, layout.columnWidth, "AQUA");
    }

    private void renderMiscPage(final MatrixStack matrixStack, final Layout layout) {
        renderPanel(matrixStack, layout.contentX, layout.contentY, layout.columnWidth, "GENERAL");
        renderPanel(matrixStack, layout.contentX + layout.columnWidth + layout.columnGap, layout.contentY, layout.columnWidth, "TOOLS");
    }

    private void renderPanel(final MatrixStack matrixStack, final double x, final double y, final double width, final String title) {
        final double height = panelHeight(title);
        renderElevatedShell(
            x,
            y,
            width,
            height,
            this.debugProfile.panelRadius,
            12,
            surfaceColor(),
            themeManager().containerOutlineWidth(),
            containerOutlineColor()
        );
        if (!title.isEmpty()) {
            drawMiniText(matrixStack, title.toUpperCase(), x + 14.0D, y + 11.0D, textMutedColor(), 0.82D);
        }
        renderDynamicPanelRows(matrixStack, x, y, width, title);
    }

    private void renderFooter(final MatrixStack matrixStack, final Layout layout) {
        final double x = layout.x + this.debugProfile.sidebarXPadding - 6.0D;
        final double y = layout.y + layout.height - this.debugProfile.footerBottomInset;
        final double width = layout.sidebarWidth - this.debugProfile.sidebarXPadding * 2.0D + 12.0D;
        UiRenderer.drawRoundedRect(x, y, width, this.debugProfile.footerHeight, 10.0D, ColorUtil.rgba(255, 255, 255, 5));
        if (themeManager().footerProfileOutlineWidth() > 0.0D) {
            UiRenderer.drawRoundedOutline(x, y, width, this.debugProfile.footerHeight, 10.0D, themeManager().footerProfileOutlineWidth(), containerOutlineColor());
        }
        UiRenderer.drawCircle(x + 14.0D, y + this.debugProfile.footerHeight / 2.0D - 2.0D, this.debugProfile.footerAvatarRadius, ColorUtil.rgba(210, 193, 172, 255));
        drawScaledText(matrixStack, "panic", x + this.debugProfile.footerTextXOffset, y + 8.0D, TEXT, this.debugProfile.footerNameScale);
        drawScaledText(matrixStack, "58 days left", x + this.debugProfile.footerTextXOffset, y + 22.0D, TEXT_MUTED, this.debugProfile.footerStatusScale);
        drawChevronRight(x + width - 18.0D, y + this.debugProfile.footerHeight / 2.0D - 4.0D, TEXT_SOFT);
        this.clickTargets.add(new ClickTarget(x, y, width, this.debugProfile.footerHeight, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
            @Override
            public void run() {
                profileVisible = !profileVisible;
                profileScaleVisible = false;
            }
        }));
    }

    private void renderPresetInfo(final MatrixStack matrixStack, final Layout layout) {
        final double x = layout.contentX + 92.0D;
        final double y = layout.y + 8.0D;
        renderElevatedShell(x, y, 150.0D, 58.0D, 10.0D, 12, surfaceColor(), themeManager().popupOutlineWidth(), popupOutlineColor());
        this.clickTargets.add(new ClickTarget(x, y, 150.0D, 58.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, noop()));
        this.clickTargets.add(new ClickTarget(x, y, 150.0D, 58.0D, GLFW.GLFW_MOUSE_BUTTON_RIGHT, noop()));
        drawBodyText(matrixStack, "Unnamed", x + 12.0D, y + 8.0D, TEXT);
        drawMiniText(matrixStack, "Author: panic", x + 12.0D, y + 24.0D, TEXT_SOFT, 0.84D);
        drawMiniText(matrixStack, "Modified: 3 minutes ago", x + 12.0D, y + 36.0D, ACCENT, 0.84D);
    }

    private void renderPresetsPopup(final MatrixStack matrixStack, final Layout layout) {
        final double x = layout.contentX - 98.0D;
        final double y = layout.y + 18.0D;
        final double popupWidth = 296.0D;
        final double popupHeight = 326.0D;
        renderElevatedShell(x, y, popupWidth, popupHeight, 14.0D, 12, surfaceColor(), themeManager().popupOutlineWidth(), popupOutlineColor());
        this.clickTargets.add(new ClickTarget(x, y, popupWidth, popupHeight, GLFW.GLFW_MOUSE_BUTTON_LEFT, noop()));
        this.clickTargets.add(new ClickTarget(x, y, popupWidth, popupHeight, GLFW.GLFW_MOUSE_BUTTON_RIGHT, noop()));
        UiRenderer.drawCircle(x + 18.0D, y + 18.0D, 4.6D, ACCENT);
        drawBodyText(matrixStack, "Configs", x + 42.0D, y + 12.0D, ACCENT);
        renderTopbarActionButton(matrixStack, x + popupWidth - 168.0D, y + 10.0D, 70.0D, "New", new Runnable() {
            @Override
            public void run() {
                createConfig();
            }
        });
        renderTopbarActionButton(matrixStack, x + popupWidth - 92.0D, y + 10.0D, 76.0D, "Delete", new Runnable() {
            @Override
            public void run() {
                deleteSelectedConfig();
            }
        });
        final String searchLabel = this.configSearchQuery.isEmpty() ? "Search configs" : this.configSearchQuery;
        renderSearchBar(matrixStack, x + 12.0D, y + 42.0D, 272.0D, searchLabel, "Search configs");
        this.clickTargets.add(new ClickTarget(x + 12.0D, y + 42.0D, 272.0D, 30.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
            @Override
            public void run() {
                activeTextInput = TextInputContext.CONFIG_SEARCH;
            }
        }));
        final List<String> configs = filteredConfigNames();
        double entryY = y + 80.0D;
        for (int i = 0; i < configs.size(); i++) {
            final String configName = configs.get(i);
            final boolean selected = configName.equals(configManager().getSelectedConfigName());
            UiRenderer.drawRoundedRect(x + 12.0D, entryY, 272.0D, 30.0D, 9.0D, selected ? SURFACE_HOVER : ColorUtil.rgba(255, 255, 255, 4));
            UiRenderer.drawRoundedOutline(x + 12.0D, entryY, 272.0D, 30.0D, 9.0D, themeManager().controlOutlineWidth(), selected ? ColorUtil.withAlpha(ACCENT, 72) : controlOutlineColor());
            drawBodyText(matrixStack, configName, x + 24.0D, entryY + 8.0D, TEXT);
            if (selected) {
                drawPopupMiniText(matrixStack, "Selected", x + 210.0D, entryY + 10.0D, ACCENT, 0.74D);
            } else {
                drawChevronRight(x + 258.0D, entryY + 10.0D, TEXT_SOFT);
            }
            this.clickTargets.add(new ClickTarget(x + 12.0D, entryY, 272.0D, 30.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
                @Override
                public void run() {
                    loadSelectedConfig(configName);
                }
            }));
            entryY += 36.0D;
        }
        if (configs.isEmpty()) {
            drawPopupMiniText(matrixStack, "No configs match the current search.", x + 20.0D, y + 90.0D, TEXT_SOFT, 0.80D);
        }
    }

    private void renderProfilePopup(final MatrixStack matrixStack, final Layout layout) {
        final double x = layout.x + 102.0D;
        final double popupWidth = 208.0D;
        final double popupHeight = 214.0D;
        final double y = Math.max(layout.y + 18.0D, layout.y + layout.height - popupHeight - 18.0D);
        renderElevatedShell(x, y, popupWidth, popupHeight, 14.0D, 12, surfaceColor(), themeManager().popupOutlineWidth(), popupOutlineColor());
        this.clickTargets.add(new ClickTarget(x, y, popupWidth, popupHeight, GLFW.GLFW_MOUSE_BUTTON_LEFT, noop()));
        this.clickTargets.add(new ClickTarget(x, y, popupWidth, popupHeight, GLFW.GLFW_MOUSE_BUTTON_RIGHT, noop()));
        UiRenderer.drawCircle(x + 22.0D, y + 24.0D, 12.0D, ColorUtil.rgba(210, 193, 172, 255));
        drawPopupBodyText(matrixStack, "panic", x + 42.0D, y + 10.0D, TEXT);
        drawPopupMiniText(matrixStack, "Till: January 08 2026", x + 42.0D, y + 24.0D, TEXT_SOFT, 0.80D);
        drawPopupMiniText(matrixStack, "Renew", x + 42.0D, y + 35.0D, ACCENT, 0.82D);
        final ThemeModule themeModule = this.runtime.getModuleManager().get(ThemeModule.class);
        final String themeStyle = themeModule != null ? themeModule.getThemeName() : "Dark";
        final String[][] entries = new String[][]{
            {"Language", PROFILE_LANGUAGE_OPTIONS[this.profileLanguageIndex]},
            {"Menu Scale", PROFILE_SCALE_OPTIONS[this.profileMenuScaleIndex]},
            {"Style", themeStyle},
            {"Safe Mode", PROFILE_SAFE_MODE_OPTIONS[this.profileSafeModeIndex]},
            {"About", "Open"}
        };
        double rowY = y + 58.0D;
        for (int i = 0; i < entries.length; i++) {
            final String label = entries[i][0];
            final double valueX = MenuLayoutMath.profilePopupValueX(x, popupWidth, popupTextWidth(entries[i][1], this.debugProfile.valueScale * 0.88D));
            final double chevronX = MenuLayoutMath.profilePopupChevronX(x, popupWidth);
            drawPopupBodyText(matrixStack, label, x + 12.0D, rowY - 2.0D, TEXT);
            drawPopupValueText(matrixStack, entries[i][1], valueX, rowY - 1.0D, TEXT_SOFT);
            drawChevronRight(chevronX, rowY + 2.0D, TEXT_SOFT);
            this.clickTargets.add(new ClickTarget(x + 8.0D, rowY - 4.0D, popupWidth - 16.0D, 18.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
                @Override
                public void run() {
                    if ("Menu Scale".equals(label)) {
                        profileScaleVisible = !profileScaleVisible;
                        return;
                    }
                    if ("Language".equals(label)) {
                        profileLanguageIndex = (profileLanguageIndex + 1) % PROFILE_LANGUAGE_OPTIONS.length;
                        saveProfileSettings();
                    } else if ("Style".equals(label)) {
                        toggleThemeStyle();
                    } else if ("Safe Mode".equals(label)) {
                        profileSafeModeIndex = (profileSafeModeIndex + 1) % PROFILE_SAFE_MODE_OPTIONS.length;
                        saveProfileSettings();
                    } else if ("About".equals(label)) {
                        debugStatus = "Mdk2 Minecraft client";
                    }
                }
            }));
            rowY += 22.0D;
        }
        if (this.profileScaleVisible) {
            renderProfileScalePopup(matrixStack, Math.min(layout.x + layout.width - 108.0D, x + popupWidth - 10.0D), y + 82.0D);
        }
    }

    private void renderProfileScalePopup(final MatrixStack matrixStack, final double x, final double y) {
        final String[] items = PROFILE_SCALE_OPTIONS;
        renderElevatedShell(x, y, 102.0D, 134.0D, 12.0D, 12, surfaceColor(), themeManager().popupOutlineWidth(), popupOutlineColor());
        double rowY = y + 12.0D;
        for (int i = 0; i < items.length; i++) {
            final int optionIndex = i;
            drawPopupValueText(matrixStack, items[i], x + 12.0D, rowY - 1.0D, this.profileMenuScaleIndex == i ? TEXT : TEXT_SOFT);
            this.clickTargets.add(new ClickTarget(x + 6.0D, rowY - 4.0D, 90.0D, 16.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
                @Override
                public void run() {
                    profileMenuScaleIndex = optionIndex;
                    profileScaleVisible = false;
                    saveProfileSettings();
                }
            }));
            rowY += 16.0D;
        }
    }

    private void renderSearchPopup(final MatrixStack matrixStack, final Layout layout) {
        UiRenderer.drawRoundedRect(layout.x + 10.0D, layout.y + 10.0D, layout.width - 20.0D, layout.height - 20.0D, 16.0D, ColorUtil.rgba(6, 7, 11, 154));
        final double x = layout.x + layout.width / 2.0D - 230.0D;
        final double y = layout.y + layout.height / 2.0D - 160.0D;
        renderElevatedShell(x, y, 460.0D, 320.0D, 16.0D, 12, surfaceColor(), themeManager().popupOutlineWidth(), popupOutlineColor());
        renderSearchBar(matrixStack, x + 16.0D, y + 16.0D, 428.0D, this.searchQuery.isEmpty() ? "Search" : this.searchQuery, "Search");
        this.clickTargets.add(new ClickTarget(x + 16.0D, y + 16.0D, 428.0D, 30.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
            @Override
            public void run() {
                activeTextInput = TextInputContext.MENU_SEARCH;
            }
        }));
        UiRenderer.drawRoundedRect(x + 18.0D, y + 72.0D, 424.0D, 210.0D, 12.0D, ColorUtil.rgba(255, 255, 255, 3));
        UiRenderer.drawRoundedOutline(x + 18.0D, y + 72.0D, 424.0D, 210.0D, 12.0D, themeManager().containerOutlineWidth(), containerOutlineColor());
        drawMiniText(matrixStack, "MENU SEARCH", x + 28.0D, y + 86.0D, textMutedColor(), 0.80D);
        final List<SearchResult> results = searchResults();
        if (results.isEmpty()) {
            drawBodyText(matrixStack, this.searchQuery.isEmpty() ? "Type to search modules and settings." : "No matching functions found.", x + 28.0D, y + 112.0D, textColor());
            return;
        }
        double rowY = y + 104.0D;
        for (int i = 0; i < Math.min(6, results.size()); i++) {
            final SearchResult result = results.get(i);
            UiRenderer.drawRoundedRect(x + 28.0D, rowY, 404.0D, 26.0D, 8.0D, ColorUtil.rgba(255, 255, 255, 4));
            UiRenderer.drawRoundedOutline(x + 28.0D, rowY, 404.0D, 26.0D, 8.0D, themeManager().controlOutlineWidth(), controlOutlineColor());
            drawBodyText(matrixStack, result.module.getName(), x + 40.0D, rowY + 6.0D, textColor());
            drawMiniText(matrixStack, result.path, x + 220.0D, rowY + 8.0D, textSoftColor(), 0.74D);
            this.clickTargets.add(new ClickTarget(x + 28.0D, rowY, 404.0D, 26.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
                @Override
                public void run() {
                    navigateToModule(result.module, result.page, result.visualPage);
                }
            }));
            rowY += 30.0D;
        }
    }

    private void renderFunctionPopup(final MatrixStack matrixStack, final Layout layout) {
        final FunctionPopupState popup = this.functionPopup;
        final double headerBottom = popup.y + popupHeaderHeight(popup.module, popup.width) - 8.0D;
        renderElevatedShell(
            popup.x,
            popup.y,
            popup.width,
            popup.height,
            14.0D,
            12,
            surfaceColor(),
            themeManager().popupOutlineWidth(),
            popupOutlineColor()
        );
        this.clickTargets.add(new ClickTarget(popup.x, popup.y, popup.width, popup.height, GLFW.GLFW_MOUSE_BUTTON_LEFT, noop()));
        this.clickTargets.add(new ClickTarget(popup.x, popup.y, popup.width, popup.height, GLFW.GLFW_MOUSE_BUTTON_RIGHT, noop()));
        drawPopupMiniText(matrixStack, "SETTINGS", popup.x + 12.0D, popup.y + 10.0D, TEXT_MUTED, 0.72D);
        drawPopupBodyText(matrixStack, popup.module.getName(), popup.x + 12.0D, popup.y + 23.0D, TEXT);
        final List<Setting<?>> settings = popup.module.getSettings();
        if (settings.isEmpty()) {
            drawPopupMiniText(matrixStack, "No additional settings.", popup.x + 12.0D, headerBottom + 12.0D, TEXT_MUTED, 0.80D);
            return;
        }
        final double contentX = popup.x + 12.0D;
        final double contentY = headerBottom + 8.0D;
        final double contentWidth = popup.width - 24.0D;
        final double contentHeight = popup.contentHeight;
        final double scrollableHeight = popup.scrollMax > 0.0D ? contentHeight + 2.0D : contentHeight;
        UiRenderer.scissorStart(
            MenuLayoutMath.popupViewportX(layout.originX, contentX - 2.0D, layout.scale),
            MenuLayoutMath.popupViewportY(layout.originY, contentY - 2.0D, layout.scale),
            MenuLayoutMath.popupViewportSize(contentWidth + 4.0D, layout.scale),
            MenuLayoutMath.popupViewportSize(scrollableHeight + 4.0D, layout.scale)
        );
        double rowY = contentY - popup.scrollOffset;
        for (int i = 0; i < settings.size(); i++) {
            final Setting<?> setting = settings.get(i);
            final double rowHeight = popupSettingRowHeight(setting);
            renderPopupSettingRow(matrixStack, contentX, rowY, contentWidth, setting, i > 0);
            rowY += rowHeight + 6.0D;
        }
        UiRenderer.scissorEnd();

        if (popup.scrollMax > 0.0D) {
            final double trackX = popup.x + popup.width - 8.0D;
            final double trackY = contentY;
            final double trackHeight = popup.contentHeight;
            final double thumbHeight = Math.max(24.0D, trackHeight * (trackHeight / (popup.contentHeight + popup.scrollMax)));
            final double progress = popup.scrollMax <= 0.0D ? 0.0D : popup.scrollOffset / popup.scrollMax;
            final double thumbY = trackY + (trackHeight - thumbHeight) * progress;
            UiRenderer.drawRoundedRect(trackX, trackY, 2.0D, trackHeight, 1.0D, ColorUtil.rgba(255, 255, 255, 18));
            UiRenderer.drawRoundedRect(trackX - 0.5D, thumbY, 3.0D, thumbHeight, 1.5D, ColorUtil.rgba(255, 255, 255, 92));
        }
    }

    private void renderViewPopup(final MatrixStack matrixStack, final Layout layout) {
        final double x = layout.contentX + layout.columnWidth - 26.0D;
        final double y = layout.contentY - 8.0D;
        renderElevatedShell(x, y, 236.0D, 172.0D, 14.0D, 12, surfaceColor(), themeManager().popupOutlineWidth(), popupOutlineColor());
        this.clickTargets.add(new ClickTarget(x, y, 236.0D, 172.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, noop()));
        this.clickTargets.add(new ClickTarget(x, y, 236.0D, 172.0D, GLFW.GLFW_MOUSE_BUTTON_RIGHT, noop()));
        final String title = this.viewPopup == ViewPopup.VIEW_OPTIONS ? "View Options" :
            this.viewPopup == ViewPopup.SCOPE_OPTIONS ? "Scope Options" :
            this.viewPopup == ViewPopup.VIEWMODEL_OPTIONS ? "Viewmodel Options" : "Perspective Options";
        drawBodyText(matrixStack, title, x + 14.0D, y + 10.0D, TEXT);
        renderMiniToggleRow(matrixStack, x + 14.0D, y + 36.0D, 208.0D, "Override FOV", false);
        renderMiniSliderRow(matrixStack, x + 14.0D, y + 62.0D, 208.0D, "Value", 0.52D);
        renderMiniToggleRow(matrixStack, x + 14.0D, y + 88.0D, 208.0D, "Override Aspect Ratio", false);
        renderMiniSliderRow(matrixStack, x + 14.0D, y + 114.0D, 208.0D, "Scaling", 0.43D);
        renderMiniToggleRow(matrixStack, x + 14.0D, y + 140.0D, 208.0D, "Disable Landing Bob", false);
    }

    private void renderColorPicker(final MatrixStack matrixStack, final Layout layout) {
        final double x = layout.x + layout.width - 240.0D;
        final double y = layout.y + 84.0D;
        final PickerColor color = getColor(this.activeColorSlot);
        renderElevatedShell(x, y, 206.0D, 226.0D, 14.0D, 12, surfaceColor(), themeManager().popupOutlineWidth(), popupOutlineColor());
        renderColorSquare(x + 14.0D, y + 14.0D, 138.0D, color);
        renderHueBar(x + 14.0D, y + 160.0D, 178.0D);
        renderAlphaBar(x + 14.0D, y + 178.0D, 178.0D, color);
        final double pickerX = x + 14.0D + color.saturation * 138.0D;
        final double pickerY = y + 14.0D + (1.0F - color.brightness) * 138.0D;
        UiRenderer.drawCircle(pickerX, pickerY, 4.6D, ColorUtil.rgba(255, 255, 255, 255));
        UiRenderer.drawCircle(pickerX, pickerY, 2.2D, WINDOW);
        UiRenderer.drawCircle(x + 14.0D + color.hue * 178.0D, y + 163.0D, 4.2D, ColorUtil.rgba(255, 255, 255, 255));
        UiRenderer.drawCircle(x + 14.0D + color.alpha * 178.0D, y + 181.0D, 4.2D, ColorUtil.rgba(255, 255, 255, 255));
        drawMiniText(matrixStack, "HEX", x + 14.0D, y + 192.0D, TEXT_MUTED, 0.78D);
        drawValueText(matrixStack, String.format("#%06X", color.color() & 0xFFFFFF), x + 42.0D, y + 191.0D, TEXT);
        drawValueText(matrixStack, (int) (color.alpha * 100.0F) + "%", x + 160.0D, y + 191.0D, TEXT);
        renderColorSwatch(x + 14.0D, y + 208.0D, this.primaryColor);
        renderColorSwatch(x + 36.0D, y + 208.0D, this.wallColor);
        renderColorSwatch(x + 58.0D, y + 208.0D, this.soulColor);
        renderColorSwatch(x + 80.0D, y + 208.0D, this.glowColor);
    }

    private void renderDropdownPopup(final MatrixStack matrixStack) {
        final double x = dropdownX(this.openDropdown);
        final double y = dropdownY(this.openDropdown);
        final String[] options = dropdownOptions(this.openDropdown);
        final double width = dropdownWidth(this.openDropdown);
        renderElevatedShell(x, y, width, options.length * 20.0D + 8.0D, 10.0D, 12, surfaceColor(), themeManager().popupOutlineWidth(), popupOutlineColor());
        double rowY = y + 6.0D;
        for (int i = 0; i < options.length; i++) {
            if (i == dropdownIndex(this.openDropdown)) {
                UiRenderer.drawRoundedRect(x + 4.0D, rowY - 1.0D, width - 8.0D, 18.0D, 6.0D, ColorUtil.rgba(255, 255, 255, 8));
            }
            drawValueText(matrixStack, options[i], x + 10.0D, rowY - 1.0D, i == dropdownIndex(this.openDropdown) ? TEXT : TEXT_SOFT);
            rowY += 20.0D;
        }
    }

    private void renderPopupModeDropdown(final MatrixStack matrixStack) {
        final PopupModeDropdown popup = this.popupModeDropdown;
        renderElevatedShell(popup.x, popup.y, popup.width, popup.height, 10.0D, 12, surfaceColor(), themeManager().popupOutlineWidth(), popupOutlineColor());
        double rowY = popup.y + 8.0D;
        for (int i = 0; i < popup.options.size(); i++) {
            final boolean selected = i == popup.selectedIndex;
            if (selected) {
                UiRenderer.drawRoundedRect(popup.x + 6.0D, rowY - 2.0D, popup.width - 12.0D, 18.0D, 6.0D, surfaceHoverColor());
            }
            drawPopupValueText(matrixStack, popup.options.get(i), popup.x + 10.0D, rowY + 1.0D, selected ? textColor() : textSoftColor());
            rowY += 18.0D;
        }
    }

    private void initDebugFields() {
        this.typographyFields = Arrays.asList(
            debugField("Font Size", "Base font size for all menu text.", 1.0D, 12.0D, 64.0D, () -> this.debugProfile.fontSize, value -> this.debugProfile.fontSize = value),
            debugField("Text Scale", "Main row labels and sidebar entry text.", 0.02D, 0.30D, 1.60D, () -> this.debugProfile.textScale, value -> this.debugProfile.textScale = value),
            debugField("Value Scale", "Right-side values, dropdown values, numbers.", 0.02D, 0.30D, 1.60D, () -> this.debugProfile.valueScale, value -> this.debugProfile.valueScale = value),
            debugField("Mini Scale", "Section labels like AIMBOT, COMMON, MAIN.", 0.02D, 0.30D, 1.60D, () -> this.debugProfile.miniScale, value -> this.debugProfile.miniScale = value)
        );
        this.windowFields = Arrays.asList(
            debugField("Design Width", "Virtual menu width before final screen scaling.", 10.0D, 900.0D, 1800.0D, () -> this.debugProfile.designWidth, value -> this.debugProfile.designWidth = value),
            debugField("Design Height", "Virtual menu height before final screen scaling.", 10.0D, 600.0D, 1200.0D, () -> this.debugProfile.designHeight, value -> this.debugProfile.designHeight = value),
            debugField("Scale Cap", "Maximum on-screen menu scale; lower makes menu physically smaller.", 0.01D, 0.20D, 1.00D, () -> this.debugProfile.displayScaleCap, value -> this.debugProfile.displayScaleCap = value),
            debugField("Radius", "Outer rounding of the main window and sidebar shell.", 1.0D, 8.0D, 32.0D, () -> this.debugProfile.windowRadius, value -> this.debugProfile.windowRadius = value)
        );
        this.sidebarFields = Arrays.asList(
            debugField("Width", "Sidebar panel width on the left.", 2.0D, 180.0D, 380.0D, () -> this.debugProfile.sidebarWidth, value -> this.debugProfile.sidebarWidth = value),
            debugField("Pad X", "Left inset for logo and entries inside the sidebar.", 1.0D, 8.0D, 40.0D, () -> this.debugProfile.sidebarXPadding, value -> this.debugProfile.sidebarXPadding = value),
            debugField("Pad Top", "Top start position of the logo block.", 1.0D, 8.0D, 60.0D, () -> this.debugProfile.sidebarTopPadding, value -> this.debugProfile.sidebarTopPadding = value),
            debugField("Entry Gap", "Vertical spacing between main sidebar entries.", 1.0D, 24.0D, 54.0D, () -> this.debugProfile.sidebarEntrySpacing, value -> this.debugProfile.sidebarEntrySpacing = value),
            debugField("Sub Gap", "Vertical spacing between Visuals subentries.", 1.0D, 18.0D, 42.0D, () -> this.debugProfile.sidebarSubEntrySpacing, value -> this.debugProfile.sidebarSubEntrySpacing = value),
            debugField("Footer Y", "Bottom inset of the profile footer card.", 1.0D, 32.0D, 96.0D, () -> this.debugProfile.sidebarFooterInset, value -> this.debugProfile.sidebarFooterInset = value)
        );
        this.topbarFields = Arrays.asList(
            debugField("Topbar Y", "Vertical start of top controls above panels.", 1.0D, 4.0D, 44.0D, () -> this.debugProfile.topbarYOffset, value -> this.debugProfile.topbarYOffset = value),
            debugField("Button Size", "Size of lock and search topbar buttons.", 1.0D, 20.0D, 38.0D, () -> this.debugProfile.topbarButtonSize, value -> this.debugProfile.topbarButtonSize = value),
            debugField("Selector H", "Height of top preset/global selector boxes.", 1.0D, 20.0D, 38.0D, () -> this.debugProfile.topbarSelectorHeight, value -> this.debugProfile.topbarSelectorHeight = value),
            debugField("Preset W", "Width of the preset selector.", 2.0D, 100.0D, 220.0D, () -> this.debugProfile.topbarPresetWidth, value -> this.debugProfile.topbarPresetWidth = value),
            debugField("Mode W", "Width of the Global selector.", 2.0D, 90.0D, 200.0D, () -> this.debugProfile.topbarModeWidth, value -> this.debugProfile.topbarModeWidth = value),
            debugField("Topbar Gap", "Horizontal spacing between topbar controls.", 1.0D, 8.0D, 36.0D, () -> this.debugProfile.topbarGap, value -> this.debugProfile.topbarGap = value)
        );
        this.layoutFields = Arrays.asList(
            debugField("Content X", "Gap between sidebar and first content column.", 1.0D, 8.0D, 120.0D, () -> this.debugProfile.contentXOffset, value -> this.debugProfile.contentXOffset = value),
            debugField("Content Y", "Vertical start of page panels below the topbar.", 1.0D, 24.0D, 160.0D, () -> this.debugProfile.contentYOffset, value -> this.debugProfile.contentYOffset = value),
            debugField("Right Pad", "Right-side padding between last column and window edge.", 1.0D, 8.0D, 120.0D, () -> this.debugProfile.contentRightPadding, value -> this.debugProfile.contentRightPadding = value),
            debugField("Column Gap", "Spacing between left and right content columns.", 1.0D, 8.0D, 80.0D, () -> this.debugProfile.columnGap, value -> this.debugProfile.columnGap = value)
        );
        this.panelFields = Arrays.asList(
            debugField("Header Offset", "Distance from panel title to the first row.", 1.0D, 18.0D, 80.0D, () -> this.debugProfile.panelHeaderOffset, value -> this.debugProfile.panelHeaderOffset = value),
            debugField("Row Step", "Vertical spacing between rows inside panels.", 1.0D, 20.0D, 64.0D, () -> this.debugProfile.panelRowStep, value -> this.debugProfile.panelRowStep = value),
            debugField("Stack Gap", "Gap between upper and lower panels in one column.", 1.0D, 8.0D, 48.0D, () -> this.debugProfile.panelStackGap, value -> this.debugProfile.panelStackGap = value),
            debugField("Panel Radius", "Rounding of content cards.", 1.0D, 8.0D, 24.0D, () -> this.debugProfile.panelRadius, value -> this.debugProfile.panelRadius = value)
        );
        this.brandingFields = Arrays.asList(
            debugField("Logo Title", "Scale of the Neverlose title text.", 0.02D, 0.20D, 1.20D, () -> this.debugProfile.logoTitleScale, value -> this.debugProfile.logoTitleScale = value),
            debugField("Logo Sub", "Scale of the Counter-Strike 2 subtitle.", 0.02D, 0.20D, 1.20D, () -> this.debugProfile.logoSubtitleScale, value -> this.debugProfile.logoSubtitleScale = value),
            debugField("Logo Text X", "Horizontal offset of logo text from the icon.", 1.0D, 16.0D, 80.0D, () -> this.debugProfile.logoTextXOffset, value -> this.debugProfile.logoTextXOffset = value),
            debugField("Logo Title Y", "Vertical offset of the Neverlose title.", 1.0D, -4.0D, 18.0D, () -> this.debugProfile.logoTitleYOffset, value -> this.debugProfile.logoTitleYOffset = value),
            debugField("Logo Sub Y", "Vertical offset of the subtitle under Neverlose.", 1.0D, 8.0D, 40.0D, () -> this.debugProfile.logoSubtitleYOffset, value -> this.debugProfile.logoSubtitleYOffset = value)
        );
        this.footerFields = Arrays.asList(
            debugField("Footer H", "Height of the bottom profile card.", 1.0D, 30.0D, 60.0D, () -> this.debugProfile.footerHeight, value -> this.debugProfile.footerHeight = value),
            debugField("Footer Inset", "Distance from the bottom of sidebar to the footer.", 1.0D, 24.0D, 96.0D, () -> this.debugProfile.footerBottomInset, value -> this.debugProfile.footerBottomInset = value),
            debugField("Footer Text X", "Text offset inside the footer from the left side.", 1.0D, 28.0D, 80.0D, () -> this.debugProfile.footerTextXOffset, value -> this.debugProfile.footerTextXOffset = value),
            debugField("Footer Avatar", "Radius of the avatar circle in the footer.", 1.0D, 6.0D, 16.0D, () -> this.debugProfile.footerAvatarRadius, value -> this.debugProfile.footerAvatarRadius = value),
            debugField("Footer Name", "Scale of the footer username text.", 0.02D, 0.20D, 1.20D, () -> this.debugProfile.footerNameScale, value -> this.debugProfile.footerNameScale = value),
            debugField("Footer Sub", "Scale of the footer status text.", 0.02D, 0.20D, 1.20D, () -> this.debugProfile.footerStatusScale, value -> this.debugProfile.footerStatusScale = value)
        );
        this.controlFields = Arrays.asList(
            debugField("Dropdown W", "Width of dropdown pills inside panel rows.", 1.0D, 72.0D, 148.0D, () -> this.debugProfile.dropdownWidth, value -> this.debugProfile.dropdownWidth = value),
            debugField("Slider W", "Length of slider tracks inside rows.", 1.0D, 64.0D, 148.0D, () -> this.debugProfile.sliderTrackWidth, value -> this.debugProfile.sliderTrackWidth = value)
        );
    }

    private DebugField debugField(final String label, final String description, final double step, final double min, final double max, final DoubleSupplier getter, final DoubleConsumer setter) {
        return new DebugField(label, description, step, min, max, getter, setter);
    }

    private void loadDebugProfile() {
        try {
            this.debugProfile.copyFrom(MenuDebugProfile.load(debugProfilePath()));
            this.debugStatus = "Profile loaded";
        } catch (final IOException ignored) {
            this.debugProfile.reset();
            this.debugStatus = "Using defaults";
        }
        this.debugProfileDirty = false;
        this.menuFont = null;
        this.debugFont = null;
        this.debugOverlayInitialized = false;
    }

    private void loadProfileSettings() {
        try {
            final MenuProfileSettings loaded = MenuProfileSettings.load(profileSettingsPath());
            this.profileSettings.languageIndex = loaded.languageIndex;
            this.profileSettings.menuScaleIndex = loaded.menuScaleIndex;
            this.profileSettings.safeModeIndex = loaded.safeModeIndex;
            this.profileSettings.style = loaded.style;
        } catch (final IOException ignored) {
        }
        this.profileLanguageIndex = this.profileSettings.languageIndex;
        this.profileMenuScaleIndex = this.profileSettings.menuScaleIndex;
        this.profileSafeModeIndex = this.profileSettings.safeModeIndex;
        applySavedThemeStyle();
    }

    private void saveDebugProfile() {
        try {
            this.debugProfile.debugOverlayX = this.debugOverlayState.x;
            this.debugProfile.debugOverlayY = this.debugOverlayState.y;
            this.debugProfile.save(debugProfilePath());
            this.debugProfileDirty = false;
            this.debugStatus = "Saved to config";
        } catch (final IOException ignored) {
            this.debugStatus = "Save failed";
        }
    }

    private void saveProfileSettings() {
        this.profileSettings.languageIndex = this.profileLanguageIndex;
        this.profileSettings.menuScaleIndex = this.profileMenuScaleIndex;
        this.profileSettings.safeModeIndex = this.profileSafeModeIndex;
        final ThemeModule themeModule = this.runtime.getModuleManager().get(ThemeModule.class);
        this.profileSettings.style = themeModule != null ? themeModule.getThemeName() : this.profileSettings.style;
        try {
            this.profileSettings.save(profileSettingsPath());
        } catch (final IOException ignored) {
        }
    }

    private void applySavedThemeStyle() {
        final ThemeModule themeModule = this.runtime.getModuleManager().get(ThemeModule.class);
        if (themeModule == null) {
            return;
        }
        for (final Setting<?> setting : themeModule.getSettings()) {
            if (setting instanceof ModeSetting && "Style".equals(setting.getName())) {
                final ModeSetting modeSetting = (ModeSetting) setting;
                final String target = this.profileSettings.style == null ? "Dark" : this.profileSettings.style;
                if (modeSetting.getModes().contains(target)) {
                    while (!modeSetting.getValue().equals(target)) {
                        modeSetting.next();
                    }
                }
                themeManager().setStyle(target);
                return;
            }
        }
    }

    private void copyDebugProfile() {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(this.debugProfile.exportBlock()), null);
            this.debugStatus = "Copied values";
        } catch (final IllegalStateException ignored) {
            this.debugStatus = "Copy failed";
        }
    }

    private void resetDebugProfile() {
        this.debugProfile.reset();
        final DebugOverlayFrame frame = debugOverlayFrame();
        final MenuDebugOverlayState defaults = MenuDebugOverlayState.defaultFor(this.width, this.height, frame.width, frame.height);
        this.debugOverlayState.x = defaults.x;
        this.debugOverlayState.y = defaults.y;
        this.debugProfile.debugOverlayX = this.debugOverlayState.x;
        this.debugProfile.debugOverlayY = this.debugOverlayState.y;
        this.debugProfileDirty = true;
        this.menuFont = null;
        this.debugStatus = "Profile reset";
    }

    private Path debugProfilePath() {
        final Path base = this.minecraft != null && this.minecraft.gameDirectory != null
            ? this.minecraft.gameDirectory.toPath()
            : Paths.get(".");
        return base.resolve("config").resolve("mdk2-menu-debug.properties");
    }

    private Path profileSettingsPath() {
        final Path base = this.minecraft != null && this.minecraft.gameDirectory != null
            ? this.minecraft.gameDirectory.toPath()
            : Paths.get(".");
        return base.resolve("config").resolve("mdk2-menu-profile.properties");
    }

    private DebugOverlayFrame debugOverlayFrame() {
        final double width = 350.0D;
        final double height = 338.0D;
        if (!this.debugOverlayInitialized) {
            if (Double.isNaN(this.debugProfile.debugOverlayX) || Double.isNaN(this.debugProfile.debugOverlayY)) {
                final MenuDebugOverlayState defaults = MenuDebugOverlayState.defaultFor(this.width, this.height, width, height);
                this.debugOverlayState.x = defaults.x;
                this.debugOverlayState.y = defaults.y;
                this.debugProfile.debugOverlayX = this.debugOverlayState.x;
                this.debugProfile.debugOverlayY = this.debugOverlayState.y;
            } else {
                this.debugOverlayState.x = this.debugProfile.debugOverlayX;
                this.debugOverlayState.y = this.debugProfile.debugOverlayY;
            }
            this.debugOverlayInitialized = true;
        }
        this.debugProfile.debugOverlayX = this.debugOverlayState.x;
        this.debugProfile.debugOverlayY = this.debugOverlayState.y;
        return new DebugOverlayFrame(this.debugOverlayState.x, this.debugOverlayState.y, width, height);
    }

    private void clampDebugOverlayOnDrag(final double overlayWidth, final double overlayHeight) {
        this.debugProfile.debugOverlayX = this.debugOverlayState.x;
        this.debugProfile.debugOverlayY = this.debugOverlayState.y;
    }

    private void renderDebugEditorOverlay(final MatrixStack matrixStack, final int mouseX, final int mouseY) {
        final DebugOverlayFrame frame = debugOverlayFrame();
        final double width = frame.width;
        final double height = frame.height;
        final double x = frame.x;
        final double y = frame.y;
        UiRenderer.drawRoundedRect(x, y, width, height, 14.0D, ColorUtil.rgba(12, 15, 24, 244));
        UiRenderer.drawRoundedOutline(x, y, width, height, 14.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 18));
        drawDebugText("Menu Debug", x + 14.0D, y + 14.0D, TEXT, DEBUG_TITLE_SCALE);
        drawDebugMiniText("Fixed overlay. Main menu updates live.", x + 14.0D, y + 31.0D, TEXT_MUTED, DEBUG_SUBTITLE_SCALE);

        final double tabsY = y + 50.0D;
        renderDebugTab(x + 12.0D, tabsY, 44.0D, "Type", this.debugSection == DebugSection.TYPOGRAPHY, () -> this.debugSection = DebugSection.TYPOGRAPHY);
        renderDebugTab(x + 60.0D, tabsY, 50.0D, "Window", this.debugSection == DebugSection.WINDOW, () -> this.debugSection = DebugSection.WINDOW);
        renderDebugTab(x + 114.0D, tabsY, 42.0D, "Side", this.debugSection == DebugSection.SIDEBAR, () -> this.debugSection = DebugSection.SIDEBAR);
        renderDebugTab(x + 160.0D, tabsY, 38.0D, "Top", this.debugSection == DebugSection.TOPBAR, () -> this.debugSection = DebugSection.TOPBAR);
        renderDebugTab(x + 202.0D, tabsY, 46.0D, "Panel", this.debugSection == DebugSection.PANELS, () -> this.debugSection = DebugSection.PANELS);
        renderDebugTab(x + 252.0D, tabsY, 48.0D, "Brand", this.debugSection == DebugSection.BRANDING, () -> this.debugSection = DebugSection.BRANDING);
        renderDebugTab(x + 12.0D, tabsY + 26.0D, 48.0D, "Footer", this.debugSection == DebugSection.FOOTER, () -> this.debugSection = DebugSection.FOOTER);
        renderDebugTab(x + 64.0D, tabsY + 26.0D, 52.0D, "Control", this.debugSection == DebugSection.CONTROLS, () -> this.debugSection = DebugSection.CONTROLS);
        renderDebugTab(x + 120.0D, tabsY + 26.0D, 48.0D, "Layout", this.debugSection == DebugSection.LAYOUT, () -> this.debugSection = DebugSection.LAYOUT);

        final List<DebugField> fields = debugFields();
        DebugField hoveredField = null;
        double rowY = y + 102.0D;
        for (int i = 0; i < fields.size(); i++) {
            if (renderDebugField(x + 12.0D, rowY, 186.0D, fields.get(i), mouseX, mouseY)) {
                hoveredField = fields.get(i);
            }
            rowY += 24.0D;
        }

        renderDebugHelp(x + 206.0D, y + 102.0D, 132.0D, 150.0D, hoveredField);
        final double buttonsY = y + height - 46.0D;
        renderDebugActionButton(x + 12.0D, buttonsY, 74.0D, "Reset", this::resetDebugProfile);
        renderDebugActionButton(x + 92.0D, buttonsY, 74.0D, "Copy", this::copyDebugProfile);
        renderDebugActionButton(x + 172.0D, buttonsY, 78.0D, "Save", this::saveDebugProfile);
        drawDebugMiniText(this.debugStatus, x + 14.0D, y + height - 18.0D, this.debugProfileDirty ? ACCENT : TEXT_MUTED, DEBUG_STATUS_SCALE);
    }

    private void renderDebugTab(final double x, final double y, final double width, final String label, final boolean selected, final Runnable action) {
        UiRenderer.drawRoundedRect(x, y, width, 18.0D, 6.0D, selected ? SURFACE_HOVER : ColorUtil.rgba(255, 255, 255, 4));
        UiRenderer.drawRoundedOutline(x, y, width, 18.0D, 6.0D, 1.0D, ColorUtil.rgba(255, 255, 255, selected ? 18 : 10));
        drawDebugMiniText(label, x + width / 2.0D - debugTextWidth(label, DEBUG_TAB_SCALE) / 2.0D, y + 5.0D, selected ? TEXT : TEXT_SOFT, DEBUG_TAB_SCALE);
        this.debugClickTargets.add(new ClickTarget(x, y, width, 18.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, action));
    }

    private boolean renderDebugField(final double x, final double y, final double width, final DebugField field, final int mouseX, final int mouseY) {
        final boolean hovered = MathUtil.within(mouseX, mouseY, x, y, width, 20.0D);
        UiRenderer.drawRoundedRect(x, y, width, 20.0D, 7.0D, ColorUtil.rgba(255, 255, 255, 3));
        UiRenderer.drawRoundedOutline(x, y, width, 20.0D, 7.0D, 1.0D, hovered ? ColorUtil.rgba(255, 255, 255, 18) : ColorUtil.rgba(255, 255, 255, 8));
        drawDebugMiniText(field.label, x + 8.0D, y + 6.0D, hovered ? TEXT : TEXT_SOFT, DEBUG_FIELD_SCALE);

        final double minusX = x + width - 76.0D;
        final double valueX = x + width - 56.0D;
        final double plusX = x + width - 16.0D;
        renderDebugStepperButton(minusX, y + 2.0D, "-", () -> adjustDebugField(field, -1.0D));
        UiRenderer.drawRoundedRect(valueX, y + 2.0D, 42.0D, 16.0D, 5.0D, SURFACE_RAISED);
        UiRenderer.drawRoundedOutline(valueX, y + 2.0D, 42.0D, 16.0D, 5.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 10));
        final String valueText = field.display();
        drawDebugText(valueText, valueX + 21.0D - debugTextWidth(valueText, DEBUG_VALUE_SCALE) / 2.0D, y + 6.0D, TEXT, DEBUG_VALUE_SCALE);
        renderDebugStepperButton(plusX, y + 2.0D, "+", () -> adjustDebugField(field, 1.0D));
        return hovered;
    }

    private void renderDebugStepperButton(final double x, final double y, final String label, final Runnable action) {
        UiRenderer.drawRoundedRect(x, y, 14.0D, 16.0D, 5.0D, SURFACE_RAISED);
        UiRenderer.drawRoundedOutline(x, y, 14.0D, 16.0D, 5.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 10));
        drawDebugText(label, x + 4.0D, y + 4.0D, TEXT, DEBUG_VALUE_SCALE);
        this.debugClickTargets.add(new ClickTarget(x, y, 14.0D, 16.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, action));
    }

    private void renderDebugActionButton(final double x, final double y, final double width, final String label, final Runnable action) {
        UiRenderer.drawRoundedRect(x, y, width, 18.0D, 6.0D, SURFACE_RAISED);
        UiRenderer.drawRoundedOutline(x, y, width, 18.0D, 6.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 10));
        drawDebugMiniText(label, x + width / 2.0D - debugTextWidth(label, DEBUG_FIELD_SCALE) / 2.0D, y + 5.0D, TEXT, DEBUG_FIELD_SCALE);
        this.debugClickTargets.add(new ClickTarget(x, y, width, 18.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, action));
    }

    private void renderDebugHelp(final double x, final double y, final double width, final double height, final DebugField hoveredField) {
        UiRenderer.drawRoundedRect(x, y, width, height, 10.0D, ColorUtil.rgba(255, 255, 255, 3));
        UiRenderer.drawRoundedOutline(x, y, width, height, 10.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 10));
        drawDebugText("Hover Help", x + 10.0D, y + 12.0D, TEXT, DEBUG_HELP_TITLE_SCALE);
        if (hoveredField == null) {
            drawDebugWrappedText("Move the mouse over a field to see what it changes.", x + 10.0D, y + 34.0D, width - 20.0D, TEXT_MUTED);
            return;
        }
        drawDebugMiniText(hoveredField.label, x + 10.0D, y + 34.0D, ACCENT, DEBUG_FIELD_SCALE);
        drawDebugWrappedText(hoveredField.description, x + 10.0D, y + 52.0D, width - 20.0D, TEXT_SOFT);
    }

    private List<DebugField> debugFields() {
        if (this.debugSection == DebugSection.WINDOW) {
            return this.windowFields;
        }
        if (this.debugSection == DebugSection.SIDEBAR) {
            return this.sidebarFields;
        }
        if (this.debugSection == DebugSection.TOPBAR) {
            return this.topbarFields;
        }
        if (this.debugSection == DebugSection.LAYOUT) {
            return this.layoutFields;
        }
        if (this.debugSection == DebugSection.PANELS) {
            return this.panelFields;
        }
        if (this.debugSection == DebugSection.BRANDING) {
            return this.brandingFields;
        }
        if (this.debugSection == DebugSection.FOOTER) {
            return this.footerFields;
        }
        if (this.debugSection == DebugSection.CONTROLS) {
            return this.controlFields;
        }
        return this.typographyFields;
    }

    private void adjustDebugField(final DebugField field, final double direction) {
        final double next = clamp(roundToStep(field.getter.getAsDouble() + field.step * direction, field.step), field.min, field.max);
        field.setter.accept(next);
        this.debugProfileDirty = true;
        this.menuFont = null;
        this.debugStatus = field.label + " = " + field.display();
    }

    private double roundToStep(final double value, final double step) {
        if (step <= 0.0D) {
            return value;
        }
        return Math.round(value / step) * step;
    }

    private double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void drawDebugText(final String text, final double x, final double y, final int color, final double scale) {
        debugFont().drawString(text, x + 1.0D, y + 1.0D, ColorUtil.multiplyAlpha(ColorUtil.rgba(0, 0, 0, 255), 0.34D), scale);
        debugFont().drawString(text, x, y, color, scale);
    }

    private void drawDebugMiniText(final String text, final double x, final double y, final int color, final double scale) {
        drawDebugText(text, x, y, color, scale);
    }

    private double debugTextWidth(final String text, final double scale) {
        return debugFont().width(text, scale);
    }

    private void drawDebugWrappedText(final String text, final double x, final double y, final double width, final int color) {
        final String[] words = text.split(" ");
        final StringBuilder line = new StringBuilder();
        double lineY = y;
        for (int i = 0; i < words.length; i++) {
            final String candidate = line.length() == 0 ? words[i] : line + " " + words[i];
            if (debugTextWidth(candidate, DEBUG_HELP_TEXT_SCALE) > width && line.length() > 0) {
                drawDebugMiniText(line.toString(), x, lineY, color, DEBUG_HELP_TEXT_SCALE);
                line.setLength(0);
                line.append(words[i]);
                lineY += 12.0D;
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }
        if (line.length() > 0) {
            drawDebugMiniText(line.toString(), x, lineY, color, DEBUG_HELP_TEXT_SCALE);
        }
    }

    private void renderDynamicPanelRows(final MatrixStack matrixStack, final double x, final double y, final double width, final String title) {
        if (this.page == Page.VISUALS && this.visualPage == VisualPage.PLAYERS && "PREVIEW".equals(title)) {
            renderEnemyPreview(matrixStack, x, y, width, panelHeight(title));
            return;
        }
        renderModuleList(matrixStack, x, y, width, modulesForPanel(title));
    }

    private void renderModuleList(final MatrixStack matrixStack, final double x, final double y, final double width, final List<Module> modules) {
        for (int index = 0; index < modules.size(); index++) {
            renderModuleEntryRow(matrixStack, x, y, width, index, modules.get(index));
        }
    }

    private void renderModuleEntryRow(final MatrixStack matrixStack, final double x, final double y, final double width, final int index, final Module module) {
        final double rowY = y + this.debugProfile.panelHeaderOffset + index * this.debugProfile.panelRowStep;
        renderDivider(x, y, width, index);
        if (module.isEnabled()) {
            UiRenderer.drawRoundedRect(x + 8.0D, rowY + 1.0D, width - 16.0D, 22.0D, 7.0D, ColorUtil.rgba(255, 255, 255, 5));
        }
        drawBodyText(matrixStack, module.getName(), x + 12.0D, rowY + 4.0D, TEXT);
        if (this.pendingFunctionPopupModule == module) {
            openFunctionPopup(module, x + 8.0D, rowY, width - 16.0D);
            this.pendingFunctionPopupModule = null;
        }
        if (module.isToggleable()) {
            renderToggle(x + width - 36.0D, rowY + 4.0D, module.isEnabled(), ACCENT);
            this.clickTargets.add(new ClickTarget(x + width - 36.0D, rowY + 4.0D, 24.0D, 14.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
                @Override
                public void run() {
                    module.toggle();
                }
            }));
        }
        this.clickTargets.add(new ClickTarget(x + 8.0D, rowY, width - 16.0D, 24.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
            @Override
            public void run() {
                if (module.isToggleable()) {
                    module.toggle();
                }
            }
        }));
        registerFunctionPopupTarget(x + 8.0D, rowY, width - 16.0D, 24.0D, module);
    }

    @SafeVarargs
    private final List<Module> modules(final Class<? extends Module>... types) {
        final List<Module> result = new ArrayList<Module>();
        for (final Class<? extends Module> type : types) {
            final Module module = this.runtime.getModuleManager().get(type);
            if (module != null) {
                result.add(module);
            }
        }
        return result;
    }

    private List<Module> modulesForPanel(final String title) {
        final String visualPageKey = this.page == Page.VISUALS ? this.visualPage.name() : null;
        final List<Class<? extends Module>> types = MenuPanelCatalog.modulesFor(this.page.name(), visualPageKey, title);
        if (!types.isEmpty()) {
            final List<Module> result = new ArrayList<Module>();
            for (final Class<? extends Module> type : types) {
                final Module module = this.runtime.getModuleManager().get(type);
                if (module != null) {
                    result.add(module);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private void renderRageRows(final MatrixStack matrixStack, final double x, final double y, final double width, final String title) {
        if ("MAIN".equals(title)) {
            renderToggleRow(matrixStack, x, y, width, 0, "Enabled", this.rageEnabled, null);
            renderToggleRow(matrixStack, x, y, width, 1, "Silent Aim", this.silentAim, null);
            renderToggleRow(matrixStack, x, y, width, 2, "Automatic Fire", this.automaticFire, null);
            renderToggleRow(matrixStack, x, y, width, 3, "Aim Through Walls", this.aimThroughWalls, null);
            renderSliderRow(matrixStack, x, y, width, 4, "Field of View", SliderId.FIELD_OF_VIEW, this.fieldOfView, "180.0");
        } else if ("SELECTION".equals(title)) {
            renderDropdownRow(matrixStack, x, y, width, 0, "Target", Dropdown.TARGET, true);
            renderDropdownRow(matrixStack, x, y, width, 1, "Hitboxes", Dropdown.HITBOXES, false);
            renderDropdownRow(matrixStack, x, y, width, 2, "Multipoint", Dropdown.MULTIPOINT, true);
            renderSliderRow(matrixStack, x, y, width, 3, "Hit Chance", SliderId.HIT_CHANCE, this.hitChance, "65%");
            renderSliderRow(matrixStack, x, y, width, 4, "Min Damage", SliderId.MIN_DAMAGE, this.minDamage, this.minDamage <= 24.0D ? "Auto" : Integer.toString((int) Math.round(this.minDamage)));
            renderToggleRow(matrixStack, x, y, width, 5, "Quick Stop", this.quickStop, "dots");
            renderToggleRow(matrixStack, x, y, width, 6, "Quick Scope", this.quickScope, null);
        } else if ("OTHER".equals(title)) {
            renderDropdownRow(matrixStack, x, y, width, 0, "History", Dropdown.HISTORY, false);
            renderToggleRow(matrixStack, x, y, width, 1, "Delay Shot", this.delayShot, null);
            renderToggleRow(matrixStack, x, y, width, 2, "Remove Recoil", this.removeRecoil, null);
            renderToggleRow(matrixStack, x, y, width, 3, "Remove Spread", this.removeSpread, null);
            renderToggleRow(matrixStack, x, y, width, 4, "Duck Peek Assist", this.duckPeekAssist, null);
            renderToggleRow(matrixStack, x, y, width, 5, "Quick Peek Assist", this.quickPeekAssist, "dots");
            renderToggleRow(matrixStack, x, y, width, 6, "Double Tap", this.doubleTap, null);
        } else if ("ANTI-AIM".equals(title)) {
            renderToggleRow(matrixStack, x, y, width, 0, "Enabled", this.antiAimEnabled, "dots");
            renderActionRow(matrixStack, x, y, width, 1, "Pitch");
            renderActionRow(matrixStack, x, y, width, 2, "Yaw");
            renderActionRow(matrixStack, x, y, width, 3, "Freestanding");
            renderActionRow(matrixStack, x, y, width, 4, "Mouse Override");
        }
    }

    private void renderLegitRows(final MatrixStack matrixStack, final double x, final double y, final double width, final String title) {
        if ("MAIN".equals(title)) {
            renderToggleRow(matrixStack, x, y, width, 0, "Enabled", this.legitEnabled, null);
            renderSliderRow(matrixStack, x, y, width, 1, "Aim Smoothing", SliderId.LEGIT_SMOOTHING, this.legitSmoothing, "34%");
            renderSliderRow(matrixStack, x, y, width, 2, "Reaction Time", SliderId.LEGIT_REACTION, this.legitReaction, "86ms");
            renderToggleRow(matrixStack, x, y, width, 3, "Recoil Control", this.recoilControl, null);
            renderToggleRow(matrixStack, x, y, width, 4, "Backtrack Assist", this.backtrackAssist, null);
        } else if ("SELECTION".equals(title)) {
            renderDropdownRow(matrixStack, x, y, width, 0, "Target", Dropdown.LEGIT_TARGET, false);
            renderDropdownRow(matrixStack, x, y, width, 1, "Bone", Dropdown.LEGIT_BONE, false);
            renderSliderRow(matrixStack, x, y, width, 2, "FOV", SliderId.LEGIT_FOV, this.legitFov, "4.5");
            renderToggleRow(matrixStack, x, y, width, 3, "Visible Only", this.visibleOnly, null);
            renderToggleRow(matrixStack, x, y, width, 4, "Through Smoke", this.throughSmoke, null);
        } else if ("OTHER".equals(title)) {
            renderToggleRow(matrixStack, x, y, width, 0, "Magnet Trigger", this.magnetTrigger, null);
            renderSliderRow(matrixStack, x, y, width, 1, "Burst Delay", SliderId.LEGIT_BURST, this.legitBurst, "2");
            renderActionRow(matrixStack, x, y, width, 2, "Target Switch");
            renderActionRow(matrixStack, x, y, width, 3, "Assist Style");
            renderActionRow(matrixStack, x, y, width, 4, "Visibility Filter");
        } else {
            renderToggleRow(matrixStack, x, y, width, 0, "Slow Walk", this.slowWalk, null);
            renderToggleRow(matrixStack, x, y, width, 1, "Auto Scope", this.autoScope, null);
            renderActionRow(matrixStack, x, y, width, 2, "Quick Stop");
            renderActionRow(matrixStack, x, y, width, 3, "Peek Assist");
            renderActionRow(matrixStack, x, y, width, 4, "Strafe Manager");
        }
    }

    private void renderPlayersRows(final MatrixStack matrixStack, final double x, final double y, final double width, final String title) {
        if ("ENEMY".equals(title)) {
            renderToggleRow(matrixStack, x, y, width, 0, "Enabled", this.visualsEnabled, null);
            renderToggleRow(matrixStack, x, y, width, 1, "Offscreen Arrow", this.visualsOffscreen, null);
            renderToggleRow(matrixStack, x, y, width, 2, "Sounds", this.visualsSounds, "pink");
        } else if ("ENEMY MODEL".equals(title)) {
            renderVisualModelRow(matrixStack, x, y, width, 0, "Player", Dropdown.PLAYER_MODE, ColorSlot.PRIMARY, false);
            renderVisualModelRow(matrixStack, x, y, width, 1, "Behind Walls", Dropdown.WALLS_MODE, ColorSlot.WALLS, false);
            renderVisualModelRow(matrixStack, x, y, width, 2, "On Shot", Dropdown.ONSHOT_MODE, null, false);
            renderVisualModelRow(matrixStack, x, y, width, 3, "History", Dropdown.HISTORY_MODE, ColorSlot.HISTORY, false);
            renderVisualModelRow(matrixStack, x, y, width, 4, "Ragdolls", Dropdown.RAGDOLL_MODE, null, false);
            renderVisualModelRow(matrixStack, x, y, width, 5, "Soul Particles", null, ColorSlot.SOUL, true);
            renderVisualModelRow(matrixStack, x, y, width, 6, "Glow", null, ColorSlot.GLOW, true);
        } else {
            renderEnemyPreview(matrixStack, x, y, width, panelHeight(title));
        }
    }

    private void renderWorldRows(final MatrixStack matrixStack, final double x, final double y, final double width, final String title) {
        if ("VIEW".equals(title)) {
            renderPopupRow(matrixStack, x, y, width, 0, "View Options", ViewPopup.VIEW_OPTIONS);
            renderPopupRow(matrixStack, x, y, width, 1, "Scope Options", ViewPopup.SCOPE_OPTIONS);
            renderPopupRow(matrixStack, x, y, width, 2, "Viewmodel Options", ViewPopup.VIEWMODEL_OPTIONS);
            renderPopupRow(matrixStack, x, y, width, 3, "Perspective Options", ViewPopup.PERSPECTIVE_OPTIONS);
            renderActionValueRow(matrixStack, x, y, width, 4, "Unlock Spectating", "Select");
            renderActionValueRow(matrixStack, x, y, width, 5, "Visual Recoil", "Select");
        } else if ("WORLD ESP".equals(title)) {
            renderActionRow(matrixStack, x, y, width, 0, "Bomb");
            renderActionRow(matrixStack, x, y, width, 1, "Weapons");
            renderActionRow(matrixStack, x, y, width, 2, "Hostages");
            renderActionRow(matrixStack, x, y, width, 3, "Grenades");
            renderToggleRow(matrixStack, x, y, width, 4, "Grenade Trajectory", true, null);
            renderToggleRow(matrixStack, x, y, width, 5, "Grenade Proximity Warnings", false, null);
        } else {
            renderActionRow(matrixStack, x, y, width, 0, "Windows");
            renderActionRow(matrixStack, x, y, width, 1, "Removals");
            renderActionRow(matrixStack, x, y, width, 2, "Ambience");
            renderActionRow(matrixStack, x, y, width, 3, "Hit Marker");
            renderActionRow(matrixStack, x, y, width, 4, "Bullet Tracers");
            renderToggleRow(matrixStack, x, y, width, 5, "Bullet Impacts", true, "pink");
        }
    }

    private void renderInventoryRows(final MatrixStack matrixStack, final double x, final double y, final double width, final String title) {
        if ("LOADOUT".equals(title)) {
            renderToggleRow(matrixStack, x, y, width, 0, "Skin Changer", true, null);
            renderToggleRow(matrixStack, x, y, width, 1, "Knife Preview", true, null);
            renderToggleRow(matrixStack, x, y, width, 2, "Agent Preview", false, null);
            renderDropdownRow(matrixStack, x, y, width, 3, "Sticker Mode", Dropdown.STICKER, false);
            renderSliderRow(matrixStack, x, y, width, 4, "Wear Override", SliderId.WEAR_OVERRIDE, this.wearOverride, "18%");
        } else {
            renderToggleRow(matrixStack, x, y, width, 0, "Quick Buy", this.quickBuy, null);
            renderToggleRow(matrixStack, x, y, width, 1, "Auto Rebuy", this.autoRebuy, null);
            renderToggleRow(matrixStack, x, y, width, 2, "Favorite Slots", this.favoriteSlots, null);
            renderDropdownRow(matrixStack, x, y, width, 3, "Sorting", Dropdown.SORTING, false);
            renderActionRow(matrixStack, x, y, width, 4, "StatTrak Spoof");
        }
    }

    private void renderMiscRows(final MatrixStack matrixStack, final double x, final double y, final double width, final String title) {
        if ("GENERAL".equals(title)) {
            renderToggleRow(matrixStack, x, y, width, 0, "Discord RPC", this.miscDiscord, null);
        }
    }

    private void renderEnemyPreview(final MatrixStack matrixStack, final double x, final double y, final double width, final double height) {
        final EntityEspModule entityEspModule = this.runtime.getModuleManager().get(EntityEspModule.class);
        final HitChamsModule hitChamsModule = this.runtime.getModuleManager().get(HitChamsModule.class);
        final TargetEspModule targetEspModule = this.runtime.getModuleManager().get(TargetEspModule.class);
        final int previewPrimary = this.primaryColor.color();
        final int previewWall = this.wallColor.color();
        final int previewGlow = this.glowColor.color();
        final int previewSoul = this.soulColor.color();

        final double headerY = y + 14.0D;
        renderPreviewIconTab(x + width - 74.0D, headerY - 3.0D, entityEspModule != null && entityEspModule.isEnabled());
        renderPreviewIconTab(x + width - 52.0D, headerY - 3.0D, hitChamsModule != null && hitChamsModule.isEnabled());
        renderPreviewIconTab(x + width - 30.0D, headerY - 3.0D, targetEspModule != null && targetEspModule.isEnabled());

        final double stageX = x + 18.0D;
        final double stageY = MenuLayoutMath.previewStageY(y);
        final double stageW = width - 36.0D;
        final double stageH = MenuLayoutMath.previewStageHeight(height);
        UiRenderer.drawRoundedRect(stageX, stageY, stageW, stageH, 12.0D, ColorUtil.rgba(255, 255, 255, themeManager().isGlass() ? 10 : 4));
        UiRenderer.drawRoundedOutline(stageX, stageY, stageW, stageH, 12.0D, themeManager().containerOutlineWidth(), containerOutlineColor());
        UiRenderer.drawGradientRect(stageX, stageY, stageW, stageH,
            ColorUtil.rgba(255, 255, 255, themeManager().isGlass() ? 14 : 6),
            ColorUtil.rgba(255, 255, 255, 0),
            ColorUtil.rgba(0, 0, 0, themeManager().isGlass() ? 36 : 22),
            ColorUtil.rgba(0, 0, 0, themeManager().isGlass() ? 28 : 18));

        final double centerX = stageX + stageW * 0.50D;
        final double centerY = stageY + stageH * 0.60D;
        final double bodyTop = centerY - 78.0D;
        final double bodyHeight = 146.0D;
        final double boxX = centerX - 20.0D;
        final double boxY = bodyTop - 8.0D;
        final double boxW = 40.0D;
        final double boxH = bodyHeight + 12.0D;

        if (entityEspModule != null && entityEspModule.isEnabled()) {
            UiRenderer.drawRoundedOutline(boxX, boxY, boxW, boxH, 10.0D, 2.0D, ColorUtil.withAlpha(previewPrimary, 214));
            UiRenderer.drawRoundedOutline(boxX - 5.0D, boxY - 5.0D, boxW + 10.0D, boxH + 10.0D, 12.0D, 1.0D, ColorUtil.withAlpha(previewWall, 88));
        }
        if (hitChamsModule != null && hitChamsModule.isEnabled()) {
            UiRenderer.drawRoundedRect(boxX + 2.0D, boxY + 2.0D, boxW - 4.0D, boxH - 4.0D, 9.0D, ColorUtil.withAlpha(previewWall, 26));
        }
        if (targetEspModule != null && targetEspModule.isEnabled()) {
            UiRenderer.drawCircle(centerX, centerY - 22.0D, 42.0D, ColorUtil.multiplyAlpha(previewGlow, 0.12D));
            UiRenderer.drawCircle(centerX, centerY - 22.0D, 26.0D, ColorUtil.multiplyAlpha(previewSoul, 0.10D));
        }

        renderStevePreviewEntity(centerX, centerY + bodyHeight * 0.47D, bodyHeight);
    }

    private void renderPreviewTab(final MatrixStack matrixStack, final double x, final double y, final double width, final String label, final boolean active) {
        UiRenderer.drawRoundedRect(x, y, width, 18.0D, 8.0D, active ? ColorUtil.withAlpha(accentColor(), 42) : ColorUtil.rgba(255, 255, 255, 4));
        UiRenderer.drawRoundedOutline(x, y, width, 18.0D, 8.0D, themeManager().controlOutlineWidth(), active ? ColorUtil.withAlpha(accentColor(), 92) : controlOutlineColor());
        UiRenderer.drawCircle(x + 10.0D, y + 9.0D, 3.0D, active ? accentColor() : textSoftColor());
        drawMiniText(matrixStack, label, x + 18.0D, y + 4.0D, active ? textColor() : textSoftColor(), 0.78D);
    }

    private void renderPreviewIconTab(final double x, final double y, final boolean active) {
        UiRenderer.drawRoundedRect(x, y, 18.0D, 18.0D, 7.0D, active ? ColorUtil.withAlpha(accentColor(), 30) : ColorUtil.rgba(255, 255, 255, 4));
        UiRenderer.drawRoundedOutline(x, y, 18.0D, 18.0D, 7.0D, themeManager().controlOutlineWidth(), active ? ColorUtil.withAlpha(accentColor(), 84) : controlOutlineColor());
        UiRenderer.drawCircle(x + 9.0D, y + 9.0D, 3.2D, active ? accentColor() : textSoftColor());
    }

    private void renderStevePreviewEntity(final double centerX, final double baselineY, final double bodyHeight) {
        final RemoteClientPlayerEntity steve = previewSteveEntity();
        if (steve == null) {
            renderStevePreview(centerX, baselineY - bodyHeight, bodyHeight);
            return;
        }

        steve.yBodyRot = 24.0F;
        steve.yBodyRotO = 24.0F;
        steve.yHeadRot = 24.0F;
        steve.yHeadRotO = 24.0F;
        steve.yRot = 18.0F;
        steve.yRotO = 18.0F;
        steve.xRot = 0.0F;
        steve.xRotO = 0.0F;

        InventoryScreen.renderEntityInInventory(
            (int) Math.round(centerX),
            (int) Math.round(baselineY),
            Math.max(28, (int) Math.round(bodyHeight * 0.60D)),
            0.0F,
            0.0F,
            steve
        );
    }

    private RemoteClientPlayerEntity previewSteveEntity() {
        final net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.level == null) {
            this.previewSteveEntity = null;
            return null;
        }
        if (this.previewSteveEntity == null || this.previewSteveEntity.level != minecraft.level) {
            this.previewSteveEntity = new RemoteClientPlayerEntity(
                minecraft.level,
                new com.mojang.authlib.GameProfile(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"), "Steve")
            );
            this.previewSteveEntity.noPhysics = true;
        }
        return this.previewSteveEntity;
    }

    private void renderStevePreview(final double centerX, final double topY, final double bodyHeight) {
        final double scale = bodyHeight / 146.0D;
        final int skin = ColorUtil.rgba(222, 178, 127, 255);
        final int hair = ColorUtil.rgba(88, 57, 37, 255);
        final int shirt = ColorUtil.rgba(73, 151, 214, 255);
        final int pants = ColorUtil.rgba(52, 83, 174, 255);
        final int shoe = ColorUtil.rgba(70, 54, 44, 255);

        final double headX = centerX - 13.0D * scale;
        final double headY = topY;
        final double headSize = 26.0D * scale;
        UiRenderer.drawRoundedRect(headX, headY, headSize, headSize, 6.0D * scale, skin);
        UiRenderer.drawRoundedRect(headX, headY, headSize, 8.0D * scale, 6.0D * scale, hair);
        UiRenderer.drawRoundedRect(headX + 4.0D * scale, headY + 10.0D * scale, 4.0D * scale, 4.0D * scale, 1.4D * scale, ColorUtil.rgba(255, 255, 255, 240));
        UiRenderer.drawRoundedRect(headX + 18.0D * scale, headY + 10.0D * scale, 4.0D * scale, 4.0D * scale, 1.4D * scale, ColorUtil.rgba(255, 255, 255, 240));
        UiRenderer.drawRoundedRect(headX + 9.0D * scale, headY + 15.0D * scale, 8.0D * scale, 4.0D * scale, 1.2D * scale, ColorUtil.rgba(120, 90, 70, 255));

        final double torsoX = centerX - 15.0D * scale;
        final double torsoY = topY + 28.0D * scale;
        UiRenderer.drawRoundedRect(torsoX, torsoY, 30.0D * scale, 36.0D * scale, 7.0D * scale, shirt);
        UiRenderer.drawRoundedRect(torsoX + 12.0D * scale, torsoY + 6.0D * scale, 6.0D * scale, 22.0D * scale, 2.0D * scale, ColorUtil.rgba(36, 108, 176, 160));

        UiRenderer.drawRoundedRect(centerX - 29.0D * scale, torsoY + 2.0D * scale, 11.0D * scale, 33.0D * scale, 4.0D * scale, shirt);
        UiRenderer.drawRoundedRect(centerX + 18.0D * scale, torsoY + 2.0D * scale, 11.0D * scale, 33.0D * scale, 4.0D * scale, shirt);
        UiRenderer.drawRoundedRect(centerX - 29.0D * scale, torsoY + 22.0D * scale, 11.0D * scale, 13.0D * scale, 4.0D * scale, skin);
        UiRenderer.drawRoundedRect(centerX + 18.0D * scale, torsoY + 22.0D * scale, 11.0D * scale, 13.0D * scale, 4.0D * scale, skin);

        final double legY = torsoY + 37.0D * scale;
        UiRenderer.drawRoundedRect(centerX - 15.0D * scale, legY, 12.0D * scale, 34.0D * scale, 4.0D * scale, pants);
        UiRenderer.drawRoundedRect(centerX + 3.0D * scale, legY, 12.0D * scale, 34.0D * scale, 4.0D * scale, pants);
        UiRenderer.drawRoundedRect(centerX - 15.0D * scale, legY + 28.0D * scale, 12.0D * scale, 8.0D * scale, 3.5D * scale, shoe);
        UiRenderer.drawRoundedRect(centerX + 3.0D * scale, legY + 28.0D * scale, 12.0D * scale, 8.0D * scale, 3.5D * scale, shoe);
    }

    private void renderPreviewSlider(final MatrixStack matrixStack, final double x, final double y, final double width,
                                     final String label, final SliderId sliderId, final double value, final double maxAbs) {
        drawMiniText(matrixStack, label, x, y - 1.0D, textSoftColor(), 0.78D);
        final double trackX = x + 54.0D;
        final double trackWidth = width - 92.0D;
        final double progress = (value + maxAbs) / (maxAbs * 2.0D);
        UiRenderer.drawRoundedRect(trackX, y + 5.0D, trackWidth, 2.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 18));
        UiRenderer.drawRoundedRect(trackX, y + 5.0D, trackWidth * progress, 2.0D, 1.0D, accentColor());
        UiRenderer.drawCircle(trackX + trackWidth * progress, y + 6.0D, 2.8D, ColorUtil.rgba(255, 255, 255, 240));
        drawMiniText(matrixStack, Integer.toString((int) Math.round(value)), x + width - 22.0D, y - 1.0D, textSoftColor(), 0.78D);
        this.clickTargets.add(new ClickTarget(trackX - 3.0D, y, trackWidth + 6.0D, 12.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, bindSlider(sliderId, trackX, trackWidth)));
    }

    private void renderToggleRow(final MatrixStack matrixStack, final double x, final double y, final double width, final int index, final String label, final boolean value, final String style) {
        final double rowY = y + this.debugProfile.panelHeaderOffset + index * this.debugProfile.panelRowStep;
        renderDivider(x, y, width, index);
        drawBodyText(matrixStack, label, x + 12.0D, rowY + 4.0D, TEXT);
        if ("pink".equals(style)) {
            renderToggle(x + width - 36.0D, rowY + 4.0D, value, ColorUtil.rgba(242, 118, 184, 255));
            this.clickTargets.add(new ClickTarget(x + width - 36.0D, rowY + 4.0D, 24.0D, 14.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, bindToggle(label)));
            registerFunctionPopupTarget(x + 8.0D, rowY, width - 16.0D, 24.0D, label);
            return;
        }
        renderToggle(x + width - 36.0D, rowY + 4.0D, value, ACCENT);
        this.clickTargets.add(new ClickTarget(x + width - 36.0D, rowY + 4.0D, 24.0D, 14.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, bindToggle(label)));
        registerFunctionPopupTarget(x + 8.0D, rowY, width - 16.0D, 24.0D, label);
    }

    private void renderDropdownRow(final MatrixStack matrixStack, final double x, final double y, final double width, final int index, final String label, final Dropdown dropdown, final boolean dots) {
        final double rowY = y + this.debugProfile.panelHeaderOffset + index * this.debugProfile.panelRowStep;
        renderDivider(x, y, width, index);
        drawBodyText(matrixStack, label, x + 12.0D, rowY + 4.0D, TEXT);
        final double selectWidth = this.debugProfile.dropdownWidth;
        final double selectorX = x + width - selectWidth - 12.0D;
        UiRenderer.drawRoundedRect(selectorX, rowY + 2.0D, selectWidth, 22.0D, 7.0D, surfaceRaisedColor());
        UiRenderer.drawRoundedOutline(selectorX, rowY + 2.0D, selectWidth, 22.0D, 7.0D, themeManager().controlOutlineWidth(), controlOutlineColor());
        drawValueText(matrixStack, dropdownValue(dropdown), selectorX + 10.0D, rowY + 7.0D, TEXT);
        drawChevronDown(selectorX + selectWidth - 15.0D, rowY + 10.0D, TEXT_SOFT);
        this.clickTargets.add(new ClickTarget(selectorX, rowY + 2.0D, selectWidth, 22.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, bindDropdown(dropdown)));
        registerFunctionPopupTarget(x + 8.0D, rowY, width - 16.0D, 24.0D, label);
    }

    private void renderSliderRow(final MatrixStack matrixStack, final double x, final double y, final double width, final int index, final String label, final SliderId slider, final double value, final String text) {
        final double rowY = y + this.debugProfile.panelHeaderOffset + index * this.debugProfile.panelRowStep;
        renderDivider(x, y, width, index);
        drawBodyText(matrixStack, label, x + 12.0D, rowY + 4.0D, TEXT);
        drawValueText(matrixStack, text, x + width - 12.0D - textWidth(text, this.debugProfile.valueScale), rowY + 4.0D, TEXT);
        final double trackX = x + width - this.debugProfile.sliderTrackWidth - 46.0D;
        final double progress = sliderProgress(slider);
        UiRenderer.drawRoundedRect(trackX, rowY + 13.0D, this.debugProfile.sliderTrackWidth, 3.0D, 1.5D, ColorUtil.rgba(255, 255, 255, 18));
        UiRenderer.drawRoundedRect(trackX, rowY + 13.0D, this.debugProfile.sliderTrackWidth * progress, 3.0D, 1.5D, ACCENT);
        UiRenderer.drawCircle(trackX + this.debugProfile.sliderTrackWidth * progress, rowY + 14.5D, 4.2D, ColorUtil.rgba(255, 255, 255, 240));
        this.clickTargets.add(new ClickTarget(trackX - 4.0D, rowY + 6.0D, this.debugProfile.sliderTrackWidth + 8.0D, 16.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, bindSlider(slider, trackX, this.debugProfile.sliderTrackWidth)));
        registerFunctionPopupTarget(x + 8.0D, rowY, width - 16.0D, 24.0D, label);
    }

    private void renderActionRow(final MatrixStack matrixStack, final double x, final double y, final double width, final int index, final String label) {
        final double rowY = y + this.debugProfile.panelHeaderOffset + index * this.debugProfile.panelRowStep;
        renderDivider(x, y, width, index);
        drawBodyText(matrixStack, label, x + 12.0D, rowY + 4.0D, TEXT);
        drawChevronRight(x + width - 18.0D, rowY + 8.0D, TEXT_SOFT);
        registerFunctionPopupTarget(x + 8.0D, rowY, width - 16.0D, 24.0D, label);
    }

    private void renderActionValueRow(final MatrixStack matrixStack, final double x, final double y, final double width, final int index, final String label, final String value) {
        final double rowY = y + this.debugProfile.panelHeaderOffset + index * this.debugProfile.panelRowStep;
        renderDivider(x, y, width, index);
        drawBodyText(matrixStack, label, x + 12.0D, rowY + 4.0D, TEXT);
        drawValueText(matrixStack, value, x + width - 20.0D - textWidth(value, this.debugProfile.valueScale), rowY + 4.0D, TEXT_SOFT);
        registerFunctionPopupTarget(x + 8.0D, rowY, width - 16.0D, 24.0D, label);
    }

    private void renderPopupRow(final MatrixStack matrixStack, final double x, final double y, final double width, final int index, final String label, final ViewPopup popup) {
        final double rowY = y + this.debugProfile.panelHeaderOffset + index * this.debugProfile.panelRowStep;
        renderDivider(x, y, width, index);
        drawBodyText(matrixStack, label, x + 12.0D, rowY + 4.0D, TEXT);
        drawChevronRight(x + width - 18.0D, rowY + 8.0D, TEXT_SOFT);
        this.clickTargets.add(new ClickTarget(x + 8.0D, rowY, width - 16.0D, 24.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, bindPopup(popup)));
        registerFunctionPopupTarget(x + 8.0D, rowY, width - 16.0D, 24.0D, label);
    }

    private void renderVisualModelRow(final MatrixStack matrixStack, final double x, final double y, final double width, final int index, final String label, final Dropdown dropdown, final ColorSlot colorSlot, final boolean toggle) {
        final double rowY = y + this.debugProfile.panelHeaderOffset + index * this.debugProfile.panelRowStep;
        renderDivider(x, y, width, index);
        drawBodyText(matrixStack, label, x + 12.0D, rowY + 4.0D, TEXT);
        final double selectWidth = this.debugProfile.dropdownWidth;
        final double selectorX = MenuLayoutMath.visualModelSelectorX(x, width, selectWidth);
        if (colorSlot != null) {
            final double swatchX = MenuLayoutMath.visualModelColorX(selectorX);
            renderColorSwatch(swatchX, rowY + 6.0D, getColor(colorSlot));
            this.clickTargets.add(new ClickTarget(swatchX, rowY + 6.0D, 12.0D, 12.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, bindColor(colorSlot)));
        }
        if (dropdown != null) {
            UiRenderer.drawRoundedRect(selectorX, rowY + 2.0D, selectWidth, 22.0D, 7.0D, surfaceRaisedColor());
            UiRenderer.drawRoundedOutline(selectorX, rowY + 2.0D, selectWidth, 22.0D, 7.0D, themeManager().controlOutlineWidth(), controlOutlineColor());
            drawValueText(matrixStack, dropdownValue(dropdown), selectorX + 10.0D, rowY + 7.0D, TEXT);
            drawChevronDown(selectorX + selectWidth - 15.0D, rowY + 10.0D, TEXT_SOFT);
            this.clickTargets.add(new ClickTarget(selectorX, rowY + 2.0D, selectWidth, 22.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, bindDropdown(dropdown)));
        }
        if (toggle) {
            final boolean enabled = "Soul Particles".equals(label) ? this.soulParticles : this.glow;
            renderToggle(x + width - 36.0D, rowY + 6.0D, enabled, ACCENT);
            this.clickTargets.add(new ClickTarget(x + width - 36.0D, rowY + 6.0D, 24.0D, 14.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, bindToggle(label)));
        }
        registerFunctionPopupTarget(x + 8.0D, rowY, width - 16.0D, 24.0D, label);
    }

    private void renderMiniToggleRow(final MatrixStack matrixStack, final double x, final double y, final double width, final String label, final boolean enabled) {
        drawBodyText(matrixStack, label, x, y - 2.0D, TEXT);
        renderToggle(x + width - 24.0D, y - 2.0D, enabled, ACCENT);
    }

    private void renderMiniSliderRow(final MatrixStack matrixStack, final double x, final double y, final double width, final String label, final double progress) {
        drawValueText(matrixStack, label, x, y - 1.0D, TEXT_SOFT);
        UiRenderer.drawRoundedRect(x + 88.0D, y + 7.0D, 74.0D, 3.0D, 1.5D, ColorUtil.rgba(255, 255, 255, 18));
        UiRenderer.drawRoundedRect(x + 88.0D, y + 7.0D, 74.0D * progress, 3.0D, 1.5D, ACCENT);
        UiRenderer.drawCircle(x + 88.0D + 74.0D * progress, y + 8.5D, 3.6D, ColorUtil.rgba(255, 255, 255, 240));
    }

    private void renderDivider(final double x, final double y, final double width, final int index) {
    }

    private void renderSidebarSeparator(final double x, final double y, final double width) {
    }

    private void renderSoftHorizontalSeparator(final double x, final double y, final double width) {
    }

    private void renderSoftVerticalSeparator(final double x, final double y, final double height) {
    }

    private void drawCenteredHorizontalDivider(final double x, final double y, final double width, final double inset, final double gap) {
    }

    private double sidebarEntryWidth(final String label, final boolean subEntry) {
        final double available = subEntry
            ? Math.max(84.0D, this.debugProfile.sidebarWidth - this.debugProfile.sidebarXPadding * 2.0D - 22.0D)
            : Math.max(112.0D, this.debugProfile.sidebarWidth - this.debugProfile.sidebarXPadding * 2.0D + 12.0D);
        final double textPadding = subEntry ? 30.0D : 42.0D;
        final double scale = subEntry ? 0.92D * this.debugProfile.miniScale : this.debugProfile.textScale;
        return Math.min(available, Math.max(subEntry ? 82.0D : 110.0D, textWidth(label, scale) + textPadding));
    }

    private void closeTransientPopups() {
        this.openDropdown = null;
        this.activeColorSlot = null;
        this.colorDrag = ColorDrag.NONE;
        this.activePopupColorSetting = null;
        this.activePopupColorDrag = ColorDrag.NONE;
        this.popupColorPickerVisible = false;
        this.activePopupBindSetting = null;
        this.activePopupSliderSetting = null;
        this.activePopupSliderTrack = null;
        this.popupModeDropdown = null;
        this.presetInfoVisible = false;
        this.presetsVisible = false;
        this.profileVisible = false;
        this.profileScaleVisible = false;
        this.functionPopup = null;
        this.popupModeDropdown = null;
        this.viewPopup = ViewPopup.NONE;
        this.activeTextInput = TextInputContext.NONE;
    }

    private boolean hasTransientPopupOpen() {
        return this.presetInfoVisible
            || this.presetsVisible
            || this.profileVisible
            || this.profileScaleVisible
            || this.functionPopup != null
            || this.popupModeDropdown != null
            || this.viewPopup != ViewPopup.NONE
            || this.openDropdown != null
            || this.activeColorSlot != null
            || this.popupColorPickerVisible;
    }

    private ConfigManager configManager() {
        return this.runtime.getConfigManager();
    }

    private List<String> configNames() {
        try {
            return this.runtime.getConfigManager().listConfigNames();
        } catch (final IOException ignored) {
            return Collections.singletonList(this.runtime.getConfigManager().getSelectedConfigName());
        }
    }

    private List<String> filteredConfigNames() {
        final List<String> names = configNames();
        if (this.configSearchQuery == null || this.configSearchQuery.trim().isEmpty()) {
            return names;
        }
        final List<String> result = new ArrayList<String>();
        final String query = this.configSearchQuery.toLowerCase();
        for (final String name : names) {
            if (name.toLowerCase().contains(query)) {
                result.add(name);
            }
        }
        return result;
    }

    private void saveSelectedConfig() {
        try {
            saveProfileSettings();
            this.runtime.getConfigManager().saveSelected(this.runtime.getModuleManager().getModules(), this.profileSettings.copy());
            this.debugStatus = "Saved " + this.runtime.getConfigManager().getSelectedConfigName();
        } catch (final IOException exception) {
            this.debugStatus = "Config save failed";
        }
    }

    private void loadSelectedConfig(final String configName) {
        try {
            this.runtime.getConfigManager().setSelectedConfigName(configName);
            this.runtime.getConfigManager().loadSelected(this.runtime.getModuleManager().getModules());
            final MenuProfileSettings loadedProfile = this.runtime.getConfigManager().loadSelectedProfile(this.profileSettings);
            this.profileSettings.languageIndex = loadedProfile.languageIndex;
            this.profileSettings.menuScaleIndex = loadedProfile.menuScaleIndex;
            this.profileSettings.safeModeIndex = loadedProfile.safeModeIndex;
            this.profileSettings.style = loadedProfile.style;
            this.profileLanguageIndex = loadedProfile.languageIndex;
            this.profileMenuScaleIndex = loadedProfile.menuScaleIndex;
            this.profileSafeModeIndex = loadedProfile.safeModeIndex;
            applySavedThemeStyle();
            saveProfileSettings();
            this.presetsVisible = false;
            this.functionPopup = null;
            this.debugStatus = "Loaded " + configName;
        } catch (final IOException exception) {
            this.debugStatus = "Config load failed";
        }
    }

    private void createConfig() {
        try {
            final ConfigManager manager = this.runtime.getConfigManager();
            final String newName = manager.createNextName();
            manager.setSelectedConfigName(newName);
            saveProfileSettings();
            manager.saveSelected(this.runtime.getModuleManager().getModules(), this.profileSettings.copy());
            this.debugStatus = "Created " + newName;
        } catch (final IOException exception) {
            this.debugStatus = "Config create failed";
        }
    }

    private void deleteSelectedConfig() {
        try {
            final String selected = this.runtime.getConfigManager().getSelectedConfigName();
            this.runtime.getConfigManager().delete(selected);
            final MenuProfileSettings loadedProfile = this.runtime.getConfigManager().loadSelectedProfile(this.profileSettings);
            this.profileSettings.languageIndex = loadedProfile.languageIndex;
            this.profileSettings.menuScaleIndex = loadedProfile.menuScaleIndex;
            this.profileSettings.safeModeIndex = loadedProfile.safeModeIndex;
            this.profileSettings.style = loadedProfile.style;
            this.profileLanguageIndex = loadedProfile.languageIndex;
            this.profileMenuScaleIndex = loadedProfile.menuScaleIndex;
            this.profileSafeModeIndex = loadedProfile.safeModeIndex;
            applySavedThemeStyle();
            saveProfileSettings();
            this.debugStatus = "Deleted " + selected;
        } catch (final IOException exception) {
            this.debugStatus = "Config delete failed";
        }
    }

    private List<SearchResult> searchResults() {
        final List<SearchResult> results = new ArrayList<SearchResult>();
        final String query = this.searchQuery == null ? "" : this.searchQuery.trim().toLowerCase();
        if (query.isEmpty()) {
            return results;
        }
        for (final Page candidatePage : Page.values()) {
            if (candidatePage == Page.VISUALS) {
                for (final VisualPage candidateVisualPage : VisualPage.values()) {
                    collectSearchResults(results, candidatePage, candidateVisualPage, query);
                }
            } else {
                collectSearchResults(results, candidatePage, null, query);
            }
        }
        return results;
    }

    private void collectSearchResults(final List<SearchResult> results, final Page candidatePage, final VisualPage candidateVisualPage, final String query) {
        final Page previousPage = this.page;
        final VisualPage previousVisualPage = this.visualPage;
        this.page = candidatePage;
        if (candidateVisualPage != null) {
            this.visualPage = candidateVisualPage;
        }
        final String[] panelTitles;
        if (candidatePage == Page.RAGE) {
            panelTitles = new String[]{"MAIN", "ASSIST", "DEFENSE", "AUTO"};
        } else if (candidatePage == Page.LEGIT) {
            panelTitles = new String[]{"MAIN", "TRACK", "SURVIVAL", "UTILITY"};
        } else if (candidatePage == Page.VISUALS && candidateVisualPage == VisualPage.PLAYERS) {
            panelTitles = new String[]{"ENEMY", "ENEMY MODEL"};
        } else if (candidatePage == Page.VISUALS) {
            panelTitles = new String[]{"VIEW", "WORLD ESP", "MISCELLANEOUS"};
        } else if (candidatePage == Page.INVENTORY) {
            panelTitles = new String[]{"MOTION", "POSITION", "WORLD", "AQUA"};
        } else {
            panelTitles = new String[]{"GENERAL", "TOOLS"};
        }
        for (final String panelTitle : panelTitles) {
            for (final Module module : modulesForPanel(panelTitle)) {
                if (matchesSearch(module, query)) {
                    results.add(new SearchResult(module, candidatePage, candidateVisualPage, candidatePage.name() + " > " + panelTitle));
                }
            }
        }
        this.page = previousPage;
        this.visualPage = previousVisualPage;
    }

    private boolean matchesSearch(final Module module, final String query) {
        if (module.getName().toLowerCase().contains(query) || module.getDescription().toLowerCase().contains(query)) {
            return true;
        }
        for (final Setting<?> setting : module.getSettings()) {
            if (setting.getName().toLowerCase().contains(query)) {
                return true;
            }
        }
        return false;
    }

    private void navigateToModule(final Module module, final Page targetPage, final VisualPage targetVisualPage) {
        this.page = targetPage;
        if (targetVisualPage != null) {
            this.visualPage = targetVisualPage;
        }
        this.pendingFunctionPopupModule = module;
        this.searchVisible = false;
        this.activeTextInput = TextInputContext.NONE;
    }

    private void toggleThemeStyle() {
        final ThemeModule themeModule = this.runtime.getModuleManager().get(ThemeModule.class);
        if (themeModule == null) {
            return;
        }
        for (final Setting<?> setting : themeModule.getSettings()) {
            if (setting instanceof ModeSetting && "Style".equals(setting.getName())) {
                ((ModeSetting) setting).next();
                this.profileSettings.style = ((ModeSetting) setting).getValue();
                themeManager().setStyle(this.profileSettings.style);
                saveProfileSettings();
                return;
            }
        }
    }

    private double menuScaleFactor() {
        final String label = PROFILE_SCALE_OPTIONS[Math.max(0, Math.min(this.profileMenuScaleIndex, PROFILE_SCALE_OPTIONS.length - 1))];
        if ("75%".equals(label)) return 0.75D;
        if ("100%".equals(label)) return 1.0D;
        if ("125%".equals(label)) return 1.25D;
        if ("150%".equals(label)) return 1.5D;
        if ("175%".equals(label)) return 1.75D;
        if ("200%".equals(label)) return 2.0D;
        return 1.0D;
    }

    private Layout createLayout() {
        return new Layout(this.width, this.height, this.debugProfile, menuScaleFactor());
    }

    private boolean isInsideAnyNonSearchPopup(final double mouseX, final double mouseY, final Layout layout) {
        return insidePresetInfo(mouseX, mouseY, layout)
            || insidePresetsPopup(mouseX, mouseY, layout)
            || insideProfilePopup(mouseX, mouseY, layout)
            || insideProfileScalePopup(mouseX, mouseY, layout)
            || insideFunctionPopup(mouseX, mouseY)
            || insidePopupModeDropdown(mouseX, mouseY)
            || insideViewPopup(mouseX, mouseY, layout)
            || insideColorPicker(mouseX, mouseY, layout)
            || insideDropdown(mouseX, mouseY);
    }

    private boolean insidePresetInfo(final double mouseX, final double mouseY, final Layout layout) {
        if (!this.presetInfoVisible) return false;
        return MathUtil.within(mouseX, mouseY, layout.contentX + 92.0D, layout.y + 8.0D, 150.0D, 58.0D);
    }

    private boolean insidePresetsPopup(final double mouseX, final double mouseY, final Layout layout) {
        if (!this.presetsVisible) return false;
        return MathUtil.within(mouseX, mouseY, layout.contentX - 98.0D, layout.y + 18.0D, 296.0D, 326.0D);
    }

    private boolean insideProfilePopup(final double mouseX, final double mouseY, final Layout layout) {
        if (!this.profileVisible) return false;
        final double x = layout.x + 102.0D;
        final double popupHeight = 214.0D;
        final double y = Math.max(layout.y + 18.0D, layout.y + layout.height - popupHeight - 18.0D);
        return MathUtil.within(mouseX, mouseY, x, y, 224.0D, popupHeight);
    }

    private boolean insideProfileScalePopup(final double mouseX, final double mouseY, final Layout layout) {
        if (!this.profileVisible || !this.profileScaleVisible) return false;
        final double x = layout.x + 102.0D;
        final double popupHeight = 214.0D;
        final double y = Math.max(layout.y + 18.0D, layout.y + layout.height - popupHeight - 18.0D);
        final double scaleX = Math.min(layout.x + layout.width - 108.0D, x + 224.0D - 10.0D);
        return MathUtil.within(mouseX, mouseY, scaleX, y + 82.0D, 96.0D, 134.0D);
    }

    private boolean insideFunctionPopup(final double mouseX, final double mouseY) {
        return this.functionPopup != null && MathUtil.within(mouseX, mouseY, this.functionPopup.x, this.functionPopup.y, this.functionPopup.width, this.functionPopup.height);
    }

    private boolean insidePopupModeDropdown(final double mouseX, final double mouseY) {
        return this.popupModeDropdown != null
            && MathUtil.within(mouseX, mouseY, this.popupModeDropdown.x, this.popupModeDropdown.y, this.popupModeDropdown.width, this.popupModeDropdown.height);
    }

    private boolean insideViewPopup(final double mouseX, final double mouseY, final Layout layout) {
        if (this.viewPopup == ViewPopup.NONE) return false;
        return MathUtil.within(mouseX, mouseY, layout.contentX + layout.columnWidth - 26.0D, layout.contentY - 8.0D, 236.0D, 172.0D);
    }

    private boolean insideColorPicker(final double mouseX, final double mouseY, final Layout layout) {
        if (this.popupColorPickerVisible && this.activePopupColorSetting != null) {
            return MathUtil.within(mouseX, mouseY, this.popupColorPickerX, this.popupColorPickerY, this.popupColorPickerWidth, this.popupColorPickerHeight);
        }
        if (this.activeColorSlot == null) return false;
        return MathUtil.within(mouseX, mouseY, layout.x + layout.width - 240.0D, layout.y + 84.0D, 206.0D, 226.0D);
    }

    private boolean insideDropdown(final double mouseX, final double mouseY) {
        if (this.openDropdown == null) return false;
        final double x = dropdownX(this.openDropdown);
        final double y = dropdownY(this.openDropdown);
        final double width = dropdownWidth(this.openDropdown);
        final double height = dropdownOptions(this.openDropdown).length * 20.0D + 8.0D;
        return MathUtil.within(mouseX, mouseY, x, y, width, height);
    }

    private boolean insideMainWindow(final double mouseX, final double mouseY, final Layout layout) {
        return MathUtil.within(mouseX, mouseY, layout.originX, layout.originY, layout.width * layout.scale, layout.height * layout.scale);
    }

    private void registerFunctionPopupTarget(final double x, final double y, final double width, final double height, final Module module) {
        this.clickTargets.add(new ClickTarget(x, y, width, height, GLFW.GLFW_MOUSE_BUTTON_RIGHT, new Runnable() {
            @Override
            public void run() {
                openFunctionPopup(module, x, y, width);
            }
        }));
    }

    private void registerFunctionPopupTarget(final double x, final double y, final double width, final double height, final String label) {
    }

    private void openFunctionPopup(final Module module, final double rowX, final double rowY, final double rowWidth) {
        if (this.functionPopup != null
            && this.functionPopup.module == module
            && Math.abs(this.functionPopup.anchorX - rowX) < 0.5D
            && Math.abs(this.functionPopup.anchorY - rowY) < 0.5D) {
            this.functionPopup = null;
            return;
        }
        final Layout layout = this.renderLayout != null ? this.renderLayout : createLayout();
        final FunctionPopupLayout popupLayout = FunctionPopupLayout.forSettingCount(module.getSettings().size(), FUNCTION_POPUP_WIDTH);
        final double popupWidth = popupLayout.width;
        final double settingsHeight = popupSettingsHeight(module.getSettings(), popupLayout);
        final double maxContentHeight = Math.max(48.0D, layout.height - popupHeaderHeight(module, popupWidth) - 34.0D);
        final double visibleContentHeight = Math.min(settingsHeight, maxContentHeight);
        final double popupHeight = Math.max(FUNCTION_POPUP_MIN_HEIGHT, popupHeaderHeight(module, popupWidth) + visibleContentHeight + 12.0D);
        final double rightX = rowX + rowWidth + 10.0D;
        final double leftX = rowX - popupWidth - 10.0D;
        final double rightBound = MenuLayoutMath.popupRightBound(layout.x, layout.width, popupWidth, 12.0D);
        final double x = rightX <= rightBound
            ? rightX
            : MenuLayoutMath.popupClampX(leftX, popupWidth, layout.x, layout.width, 12.0D);
        final double y = MenuLayoutMath.popupClampY(rowY - 8.0D, layout.y, layout.height, popupHeight, 16.0D);
        this.functionPopup = new FunctionPopupState(module, rowX, rowY, x, y, popupWidth, popupHeight, visibleContentHeight, Math.max(0.0D, settingsHeight - visibleContentHeight));
    }

    private PopupModeDropdown createPopupModeDropdown(final ModeSetting modeSetting, final double desiredX, final double desiredY) {
        double width = 86.0D;
        for (final String option : modeSetting.getModes()) {
            width = Math.max(width, popupTextWidth(option, this.debugProfile.valueScale * 0.88D) + 24.0D);
        }
        final double height = 8.0D + modeSetting.getModes().size() * 18.0D + 6.0D;
        final Layout layout = this.renderLayout != null ? this.renderLayout : createLayout();
        double x = desiredX;
        if (x > MenuLayoutMath.popupRightBound(layout.x, layout.width, width, 12.0D)) {
            x = MenuLayoutMath.popupClampX(desiredX - width - 120.0D, width, layout.x, layout.width, 12.0D);
        }
        final double y = MenuLayoutMath.popupClampY(desiredY, layout.y, layout.height, height, 16.0D);
        return new PopupModeDropdown(modeSetting, new ArrayList<String>(modeSetting.getModes()), modeSetting.getModes().indexOf(modeSetting.getValue()), x, y, width, height);
    }

    private double popupHeaderHeight(final Module module, final double popupWidth) {
        return FunctionPopupLayout.compactHeaderHeight();
    }

    private double popupSettingsHeight(final List<Setting<?>> settings, final FunctionPopupLayout popupLayout) {
        if (!popupLayout.twoColumn) {
            return popupColumnHeight(settings, 0, settings.size());
        }
        return Math.max(
            popupColumnHeight(settings, 0, popupLayout.leftCount),
            popupColumnHeight(settings, popupLayout.leftCount, popupLayout.leftCount + popupLayout.rightCount)
        );
    }

    private double popupColumnHeight(final List<Setting<?>> settings, final int startInclusive, final int endExclusive) {
        double total = 0.0D;
        for (int i = startInclusive; i < endExclusive; i++) {
            total += popupSettingRowHeight(settings.get(i));
            if (i < endExclusive - 1) {
                total += 6.0D;
            }
        }
        return total;
    }

    private double popupSettingRowHeight(final Setting<?> setting) {
        return setting instanceof NumberSetting ? 30.0D : 22.0D;
    }

    private void renderPopupSettingRow(final MatrixStack matrixStack, final double x, final double y, final double width, final Setting<?> setting, final boolean showDivider) {
        final double rowHeight = popupSettingRowHeight(setting);
        drawControlShadow(x, y, width, rowHeight, 8.0D);
        UiRenderer.drawRoundedRect(x, y, width, rowHeight, 8.0D, surfaceRaisedColor());
        UiRenderer.drawRoundedOutline(x, y, width, rowHeight, 8.0D, themeManager().controlOutlineWidth(), controlOutlineColor());

        if (setting instanceof BooleanSetting) {
            final BooleanSetting booleanSetting = (BooleanSetting) setting;
            drawPopupBodyText(matrixStack, setting.getName(), x + 10.0D, MenuLayoutMath.popupRowTextY(y, rowHeight), TEXT);
            renderToggle(x + width - 34.0D, y + 4.0D, booleanSetting.getValue().booleanValue(), ACCENT);
            this.clickTargets.add(new ClickTarget(x, y, width, rowHeight, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
                @Override
                public void run() {
                    booleanSetting.toggle();
                }
            }));
            return;
        }

        if (setting instanceof ModeSetting) {
            final ModeSetting modeSetting = (ModeSetting) setting;
            drawPopupBodyText(matrixStack, setting.getName(), x + 10.0D, MenuLayoutMath.popupRowTextY(y, rowHeight), textColor());
            final String value = modeSetting.getValue();
            final double valueWidth = popupTextWidth(value, this.debugProfile.valueScale * 0.88D);
            final double pillWidth = Math.min(width * 0.48D, Math.max(74.0D, valueWidth + 24.0D));
            final double pillX = x + width - pillWidth - 10.0D;
            drawControlShadow(pillX, y + 3.0D, pillWidth, 16.0D, 6.0D);
            UiRenderer.drawRoundedRect(pillX, y + 3.0D, pillWidth, 16.0D, 6.0D, surfaceRaisedColor());
            UiRenderer.drawRoundedOutline(pillX, y + 3.0D, pillWidth, 16.0D, 6.0D, themeManager().controlOutlineWidth(), controlOutlineColor());
            drawPopupValueText(matrixStack, value, MenuLayoutMath.popupPillTextX(pillX - 6.0D, pillWidth, valueWidth), MenuLayoutMath.popupPillTextY(y), textColor());
            drawChevronDown(pillX + pillWidth - 14.0D, y + 8.0D, textSoftColor());
            this.clickTargets.add(new ClickTarget(pillX, y + 3.0D, pillWidth, 16.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
                @Override
                public void run() {
                    popupModeDropdown = popupModeDropdown != null && popupModeDropdown.modeSetting == modeSetting
                        ? null
                        : createPopupModeDropdown(modeSetting, pillX + pillWidth + 8.0D, y + 1.0D);
                }
            }));
            return;
        }

        if (setting instanceof BindSetting) {
            final BindSetting bindSetting = (BindSetting) setting;
            drawPopupBodyText(matrixStack, setting.getName(), x + 10.0D, MenuLayoutMath.popupRowTextY(y, rowHeight), TEXT);
            final String value = this.activePopupBindSetting == bindSetting ? "Press key..." : bindSetting.getDisplayName();
            final double valueWidth = popupTextWidth(value, this.debugProfile.valueScale * 0.88D);
            final double pillWidth = MenuLayoutMath.popupBindPillWidth(width, valueWidth);
            final double pillX = x + width - pillWidth - 10.0D;
            drawControlShadow(pillX, y + 3.0D, pillWidth, 16.0D, 6.0D);
            UiRenderer.drawRoundedRect(pillX, y + 3.0D, pillWidth, 16.0D, 6.0D, this.activePopupBindSetting == bindSetting ? ColorUtil.withAlpha(ACCENT, 90) : surfaceRaisedColor());
            UiRenderer.drawRoundedOutline(pillX, y + 3.0D, pillWidth, 16.0D, 6.0D, themeManager().controlOutlineWidth(), controlOutlineColor());
            drawPopupValueText(matrixStack, value, MenuLayoutMath.popupPillTextX(pillX, pillWidth, valueWidth), MenuLayoutMath.popupPillTextY(y), TEXT);
            this.clickTargets.add(new ClickTarget(pillX, y + 3.0D, pillWidth, 16.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
                @Override
                public void run() {
                    activePopupBindSetting = activePopupBindSetting == bindSetting ? null : bindSetting;
                }
            }));
            return;
        }

        if (setting instanceof NumberSetting) {
            final NumberSetting numberSetting = (NumberSetting) setting;
            final String value = formatNumberSetting(numberSetting);
            drawPopupBodyText(matrixStack, setting.getName(), x + 10.0D, y + 3.5D, TEXT);
            drawPopupValueText(matrixStack, value, x + width - 10.0D - popupTextWidth(value, this.debugProfile.valueScale * 0.88D), y + 3.5D, TEXT_SOFT);
            final double sliderX = x + 10.0D;
            final double sliderWidth = width - 20.0D;
            final double sliderY = y + rowHeight - 6.0D;
            final double fillWidth = sliderWidth * numberSetting.getNormalizedValue();
            UiRenderer.drawRoundedRect(sliderX, sliderY, sliderWidth, 2.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 18));
            UiRenderer.drawRoundedRect(sliderX, sliderY, fillWidth, 2.0D, 1.0D, ACCENT);
            UiRenderer.drawCircle(sliderX + fillWidth, sliderY + 1.0D, 2.9D, ColorUtil.rgba(255, 255, 255, 240));
            this.clickTargets.add(new ClickTarget(sliderX - 3.0D, y + rowHeight - 13.0D, sliderWidth + 6.0D, 12.0D, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
                @Override
                public void run() {
                    activePopupSliderSetting = numberSetting;
                    activePopupSliderTrack = new PopupSliderTrack(sliderX, sliderWidth);
                }
            }));
            return;
        }

        if (setting instanceof ColorSetting) {
            final ColorSetting colorSetting = (ColorSetting) setting;
            drawPopupBodyText(matrixStack, setting.getName(), x + 10.0D, MenuLayoutMath.popupRowTextY(y, rowHeight), TEXT);
            final double swatchX = x + width - 22.0D;
            final double swatchY = MenuLayoutMath.popupSwatchY(y, rowHeight);
        UiRenderer.drawRoundedRect(swatchX - 1.0D, swatchY - 1.0D, 14.0D, 14.0D, 4.0D, surfaceRaisedColor());
            UiRenderer.drawRoundedRect(swatchX, swatchY, 12.0D, 12.0D, 3.5D, colorSetting.getColor());
            UiRenderer.drawRoundedOutline(swatchX - 1.0D, swatchY - 1.0D, 14.0D, 14.0D, 4.0D, 1.0D, this.activePopupColorSetting == colorSetting ? ACCENT : ColorUtil.rgba(255, 255, 255, 14));
            this.clickTargets.add(new ClickTarget(swatchX - 2.0D, y, 18.0D, rowHeight, GLFW.GLFW_MOUSE_BUTTON_LEFT, new Runnable() {
                @Override
                public void run() {
                    bindPopupColorSetting(colorSetting, x + width + 10.0D, y - 2.0D);
                }
            }));
        }
    }

    private void bindPopupColorSetting(final ColorSetting setting, final double preferredX, final double preferredY) {
        this.activePopupColorSetting = setting;
        this.popupColorPickerWidth = 168.0D;
        this.popupColorPickerHeight = 214.0D;
        final Layout layout = this.renderLayout != null ? this.renderLayout : createLayout();
        final double fallbackX = preferredX - this.popupColorPickerWidth - FUNCTION_POPUP_WIDTH - 20.0D;
        this.popupColorPickerX = preferredX + this.popupColorPickerWidth <= layout.width - 8.0D ? preferredX : Math.max(8.0D, fallbackX);
        this.popupColorPickerY = MathUtil.clamp(preferredY, 8.0D, layout.height - this.popupColorPickerHeight - 8.0D);
        this.popupColorPickerVisible = true;
        this.activePopupColorDrag = ColorDrag.NONE;
    }

    private String formatNumberSetting(final NumberSetting setting) {
        final double value = setting.getValue().doubleValue();
        if (Math.abs(setting.getIncrement() - Math.rint(setting.getIncrement())) < 0.0001D) {
            return Integer.toString((int) Math.round(value));
        }
        if (Math.abs(value - Math.rint(value)) < 0.0001D) {
            return Integer.toString((int) Math.round(value));
        }
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private boolean handlePopupColorPickerClick(final double mouseX, final double mouseY, final int button) {
        if (this.activePopupColorSetting == null || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (!MathUtil.within(mouseX, mouseY, this.popupColorPickerX, this.popupColorPickerY, this.popupColorPickerWidth, this.popupColorPickerHeight)) {
            this.activePopupColorSetting = null;
            this.activePopupColorDrag = ColorDrag.NONE;
            this.popupColorPickerVisible = false;
            return false;
        }
        final double squareX = this.popupColorPickerX + 10.0D;
        final double squareY = this.popupColorPickerY + 28.0D;
        final double squareSize = 98.0D;
        final double sliderX = squareX;
        final double hueY = squareY + squareSize + 12.0D;
        final double alphaY = hueY + 18.0D;
        if (MathUtil.within(mouseX, mouseY, squareX, squareY, squareSize, squareSize)) {
            this.activePopupColorDrag = ColorDrag.SQUARE;
            updatePopupColorPicker(mouseX, mouseY);
            return true;
        }
        if (MathUtil.within(mouseX, mouseY, sliderX, hueY, squareSize, 7.0D)) {
            this.activePopupColorDrag = ColorDrag.HUE;
            updatePopupColorPicker(mouseX, mouseY);
            return true;
        }
        if (MathUtil.within(mouseX, mouseY, sliderX, alphaY, squareSize, 7.0D)) {
            this.activePopupColorDrag = ColorDrag.ALPHA;
            updatePopupColorPicker(mouseX, mouseY);
            return true;
        }
        return true;
    }

    private void updatePopupSlider(final double mouseX) {
        if (this.activePopupSliderSetting == null || this.activePopupSliderTrack == null) {
            return;
        }
        final double progress = MathUtil.clamp((mouseX - this.activePopupSliderTrack.x) / this.activePopupSliderTrack.width, 0.0D, 1.0D);
        final double value = this.activePopupSliderSetting.getMinimum()
            + (this.activePopupSliderSetting.getMaximum() - this.activePopupSliderSetting.getMinimum()) * progress;
        this.activePopupSliderSetting.setValue(value);
    }

    private void updatePopupColorPicker(final double mouseX, final double mouseY) {
        if (this.activePopupColorSetting == null || this.activePopupColorDrag == ColorDrag.NONE) {
            return;
        }
        final double squareX = this.popupColorPickerX + 10.0D;
        final double squareY = this.popupColorPickerY + 28.0D;
        final double squareSize = 98.0D;
        if (this.activePopupColorDrag == ColorDrag.SQUARE) {
            final float saturation = (float) MathUtil.clamp((mouseX - squareX) / squareSize, 0.0D, 1.0D);
            final float brightness = (float) MathUtil.clamp(1.0D - (mouseY - squareY) / squareSize, 0.0D, 1.0D);
            this.activePopupColorSetting.setFromHsb(this.activePopupColorSetting.getHue(), saturation, brightness, this.activePopupColorSetting.getAlpha());
            return;
        }
        if (this.activePopupColorDrag == ColorDrag.HUE) {
            final float hue = (float) MathUtil.clamp((mouseX - squareX) / squareSize, 0.0D, 1.0D);
            this.activePopupColorSetting.setHue(hue);
            return;
        }
        final float alpha = (float) MathUtil.clamp((mouseX - squareX) / squareSize, 0.0D, 1.0D);
        this.activePopupColorSetting.setAlpha(alpha);
    }

    private void renderPopupColorPicker(final MatrixStack matrixStack) {
        if (this.activePopupColorSetting == null || !this.popupColorPickerVisible) {
            return;
        }
        final double x = this.popupColorPickerX;
        final double y = this.popupColorPickerY;
        final double width = this.popupColorPickerWidth;
        final double height = this.popupColorPickerHeight;
        final double contentX = x + 8.0D;
        final double contentY = y + 10.0D;
        final double squareX = contentX + 2.0D;
        final double squareY = contentY + 18.0D;
        final double squareSize = 98.0D;
        final double hueY = squareY + squareSize + 12.0D;
        final double alphaY = hueY + 18.0D;
        final double stripWidth = squareSize;
        final double stripHeight = 7.0D;
        final double sideX = squareX + squareSize + 12.0D;
        final int color = this.activePopupColorSetting.getColor();
        final int hueColor = 0xFF000000 | (Color.HSBtoRGB(this.activePopupColorSetting.getHue(), 1.0F, 1.0F) & 0x00FFFFFF);
        final double markerX = squareX + this.activePopupColorSetting.getSaturation() * squareSize;
        final double markerY = squareY + (1.0D - this.activePopupColorSetting.getBrightness()) * squareSize;
        final double hueMarkerX = squareX + this.activePopupColorSetting.getHue() * squareSize;
        final double alphaMarkerX = squareX + this.activePopupColorSetting.getAlpha() * squareSize;

        renderElevatedShell(x, y, width, height, 12.0D, 12, surfaceColor(), themeManager().popupOutlineWidth(), popupOutlineColor());
        drawMiniText(matrixStack, this.activePopupColorSetting.getName(), contentX + 2.0D, contentY + 1.0D, TEXT, 0.80D);

        UiRenderer.drawGradientRect(squareX, squareY, squareSize, squareSize,
            ColorUtil.rgba(255, 255, 255, 255), hueColor, hueColor, ColorUtil.rgba(255, 255, 255, 255));
        UiRenderer.drawGradientRect(squareX, squareY, squareSize, squareSize,
            ColorUtil.rgba(0, 0, 0, 0), ColorUtil.rgba(0, 0, 0, 0), ColorUtil.rgba(0, 0, 0, 255), ColorUtil.rgba(0, 0, 0, 255));
        UiRenderer.drawRoundedOutline(squareX - 0.5D, squareY - 0.5D, squareSize + 1.0D, squareSize + 1.0D, 6.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 28));
        UiRenderer.drawCircle(markerX, markerY, 3.0D, ColorUtil.rgba(255, 255, 255, 255));
        UiRenderer.drawCircle(markerX, markerY, 1.2D, ColorUtil.rgba(0, 0, 0, 180));

        renderHueStrip(squareX, hueY, stripWidth, stripHeight);
        UiRenderer.drawCircle(hueMarkerX, hueY + stripHeight / 2.0D, 3.0D, ColorUtil.rgba(255, 255, 255, 255));
        UiRenderer.drawCircle(hueMarkerX, hueY + stripHeight / 2.0D, 1.2D, ColorUtil.rgba(0, 0, 0, 180));

        UiRenderer.drawGradientRect(squareX, alphaY, stripWidth, stripHeight,
            ColorUtil.withAlpha(color, 0), ColorUtil.withAlpha(color, 255), ColorUtil.withAlpha(color, 255), ColorUtil.withAlpha(color, 0));
        UiRenderer.drawRoundedOutline(squareX - 0.5D, alphaY - 0.5D, stripWidth + 1.0D, stripHeight + 1.0D, 3.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 28));
        UiRenderer.drawCircle(alphaMarkerX, alphaY + stripHeight / 2.0D, 3.0D, ColorUtil.rgba(255, 255, 255, 255));
        UiRenderer.drawCircle(alphaMarkerX, alphaY + stripHeight / 2.0D, 1.2D, ColorUtil.rgba(0, 0, 0, 180));

        UiRenderer.drawRoundedRect(sideX, squareY + 10.0D, 34.0D, 34.0D, 8.0D, color);
        UiRenderer.drawRoundedOutline(sideX, squareY + 10.0D, 34.0D, 34.0D, 8.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 40));
        drawMiniText(matrixStack, String.format("#%06X", color & 0x00FFFFFF), sideX - 4.0D, squareY + 54.0D, TEXT_SOFT, 0.74D);
        drawMiniText(matrixStack, "A " + (int) Math.round(this.activePopupColorSetting.getAlpha() * 100.0D) + "%", sideX + 1.0D, squareY + 66.0D, TEXT_SOFT, 0.74D);
    }

    private Runnable noop() {
        return new Runnable() {
            @Override
            public void run() {
            }
        };
    }

    private boolean handleDropdownClick(final double mouseX, final double mouseY, final int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || this.openDropdown == null) {
            return false;
        }
        final double x = dropdownX(this.openDropdown);
        final double y = dropdownY(this.openDropdown);
        final double width = dropdownWidth(this.openDropdown);
        final String[] options = dropdownOptions(this.openDropdown);
        final double height = options.length * 20.0D + 8.0D;
        if (!MathUtil.within(mouseX, mouseY, x, y, width, height)) {
            this.openDropdown = null;
            return false;
        }
        final int index = (int) ((mouseY - y - 6.0D) / 20.0D);
        if (index >= 0 && index < options.length) {
            setDropdownIndex(this.openDropdown, index);
            this.openDropdown = null;
            return true;
        }
        return false;
    }

    private boolean handlePopupModeDropdownClick(final double mouseX, final double mouseY, final int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || this.popupModeDropdown == null) {
            return false;
        }
        if (!MathUtil.within(mouseX, mouseY, this.popupModeDropdown.x, this.popupModeDropdown.y, this.popupModeDropdown.width, this.popupModeDropdown.height)) {
            this.popupModeDropdown = null;
            return false;
        }
        final int index = (int) ((mouseY - this.popupModeDropdown.y - 8.0D) / 18.0D);
        if (index >= 0 && index < this.popupModeDropdown.options.size()) {
            this.popupModeDropdown.modeSetting.setValue(this.popupModeDropdown.options.get(index));
            this.popupModeDropdown = null;
            return true;
        }
        return false;
    }

    private boolean handleColorPickerClick(final double mouseX, final double mouseY, final int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || this.activeColorSlot == null) {
            return false;
        }
        final Layout layout = createLayout();
        final double x = layout.x + layout.width - 240.0D;
        final double y = layout.y + 84.0D;
        if (!MathUtil.within(mouseX, mouseY, x, y, 206.0D, 226.0D)) {
            this.activeColorSlot = null;
            this.colorDrag = ColorDrag.NONE;
            return false;
        }
        if (MathUtil.within(mouseX, mouseY, x + 14.0D, y + 14.0D, 138.0D, 138.0D)) {
            this.colorDrag = ColorDrag.SQUARE;
            updateColor(mouseX, mouseY);
            return true;
        }
        if (MathUtil.within(mouseX, mouseY, x + 14.0D, y + 160.0D, 178.0D, 6.0D)) {
            this.colorDrag = ColorDrag.HUE;
            updateColor(mouseX, mouseY);
            return true;
        }
        if (MathUtil.within(mouseX, mouseY, x + 14.0D, y + 178.0D, 178.0D, 6.0D)) {
            this.colorDrag = ColorDrag.ALPHA;
            updateColor(mouseX, mouseY);
            return true;
        }
        return true;
    }

    private void updateSlider(final double mouseX) {
        if (this.activeSliderTrack == null) {
            return;
        }
        final double progress = MathUtil.clamp((mouseX - this.activeSliderTrack.x) / this.activeSliderTrack.width, 0.0D, 1.0D);
        if (this.activeSlider == SliderId.FIELD_OF_VIEW) this.fieldOfView = progress * 180.0D;
        if (this.activeSlider == SliderId.HIT_CHANCE) this.hitChance = progress * 100.0D;
        if (this.activeSlider == SliderId.MIN_DAMAGE) this.minDamage = progress * 120.0D;
        if (this.activeSlider == SliderId.LEGIT_SMOOTHING) this.legitSmoothing = progress * 100.0D;
        if (this.activeSlider == SliderId.LEGIT_REACTION) this.legitReaction = progress * 250.0D;
        if (this.activeSlider == SliderId.LEGIT_FOV) this.legitFov = progress * 15.0D;
        if (this.activeSlider == SliderId.LEGIT_BURST) this.legitBurst = progress * 10.0D;
        if (this.activeSlider == SliderId.WEAR_OVERRIDE) this.wearOverride = progress * 100.0D;
        if (this.activeSlider == SliderId.ANIMATION_SPEED) this.animationSpeed = progress * 100.0D;
        if (this.activeSlider == SliderId.PREVIEW_X) this.previewEspOffsetX = progress * 160.0D - 80.0D;
        if (this.activeSlider == SliderId.PREVIEW_Y) this.previewEspOffsetY = progress * 120.0D - 60.0D;
    }

    private void updateColor(final double mouseX, final double mouseY) {
        final Layout layout = createLayout();
        final double x = layout.x + layout.width - 240.0D;
        final double y = layout.y + 84.0D;
        final PickerColor color = getColor(this.activeColorSlot);
        if (this.colorDrag == ColorDrag.SQUARE) {
            color.saturation = (float) MathUtil.clamp((mouseX - (x + 14.0D)) / 138.0D, 0.0D, 1.0D);
            color.brightness = (float) MathUtil.clamp(1.0D - (mouseY - (y + 14.0D)) / 138.0D, 0.0D, 1.0D);
        } else if (this.colorDrag == ColorDrag.HUE) {
            color.hue = (float) MathUtil.clamp((mouseX - (x + 14.0D)) / 178.0D, 0.0D, 1.0D);
        } else if (this.colorDrag == ColorDrag.ALPHA) {
            color.alpha = (float) MathUtil.clamp((mouseX - (x + 14.0D)) / 178.0D, 0.0D, 1.0D);
        }
    }

    private Runnable bindDropdown(final Dropdown dropdown) {
        return new Runnable() {
            @Override
            public void run() {
                openDropdown = openDropdown == dropdown ? null : dropdown;
            }
        };
    }

    private Runnable bindSlider(final SliderId slider, final double trackX, final double trackWidth) {
        return new Runnable() {
            @Override
            public void run() {
                activeSlider = slider;
                activeSliderTrack = new SliderTrack(trackX, trackWidth);
            }
        };
    }

    private Runnable bindPopup(final ViewPopup popup) {
        return new Runnable() {
            @Override
            public void run() {
                viewPopup = popup;
            }
        };
    }

    private Runnable bindColor(final ColorSlot slot) {
        return new Runnable() {
            @Override
            public void run() {
                activeColorSlot = slot;
                openDropdown = null;
            }
        };
    }

    private boolean isHudEditorActive() {
        return this.functionPopup != null && isHudEditorModule(this.functionPopup.module);
    }

    private boolean isHudEditorModule(final Module module) {
        return module instanceof HudModule
            || module instanceof WatermarkModule
            || module instanceof RadarModule
            || module instanceof DeathCoordsModule
            || module instanceof StatusEffectsModule;
    }

    private boolean tryOpenHudWidgetPopup(final double mouseX, final double mouseY, final Layout layout) {
        final HudRenderer renderer = this.runtime.getHudRenderer();
        final HudRenderer.HudArea watermarkArea = renderer.getWatermarkArea();
        final WatermarkModule watermarkModule = this.runtime.getModuleManager().get(WatermarkModule.class);
        if (watermarkModule != null && watermarkArea.isVisible() && watermarkArea.contains(mouseX, mouseY)) {
            openFunctionPopup(
                watermarkModule,
                toVirtualX(layout, watermarkArea.getX()),
                toVirtualY(layout, watermarkArea.getY()),
                watermarkArea.getWidth() / layout.scale
            );
            return true;
        }
        return false;
    }

    private boolean tryStartHudDrag(final double mouseX, final double mouseY) {
        final HudRenderer renderer = this.runtime.getHudRenderer();
        final HudRenderer.HudArea watermarkArea = renderer.getWatermarkArea();
        if (watermarkArea.isVisible() && watermarkArea.contains(mouseX, mouseY)) {
            this.activeHudDrag = HudDragTarget.WATERMARK;
            this.hudDragOffsetX = mouseX - watermarkArea.getX();
            this.hudDragOffsetY = mouseY - watermarkArea.getY();
            this.hudDragSnapX = false;
            this.hudDragSnapY = false;
            return true;
        }

        final HudRenderer.HudArea modulesArea = renderer.getModulesArea();
        if (modulesArea.isVisible() && modulesArea.contains(mouseX, mouseY)) {
            this.activeHudDrag = HudDragTarget.MODULES;
            this.hudDragOffsetX = mouseX - modulesArea.getX();
            this.hudDragOffsetY = mouseY - modulesArea.getY();
            this.hudDragSnapX = false;
            this.hudDragSnapY = false;
            return true;
        }

        final HudRenderer.HudArea radarArea = renderer.getRadarArea();
        if (radarArea.isVisible() && radarArea.contains(mouseX, mouseY)) {
            this.activeHudDrag = HudDragTarget.RADAR;
            this.hudDragOffsetX = mouseX - radarArea.getX();
            this.hudDragOffsetY = mouseY - radarArea.getY();
            this.hudDragSnapX = false;
            this.hudDragSnapY = false;
            return true;
        }

        final HudRenderer.HudArea deathArea = renderer.getDeathCoordsArea();
        if (deathArea.isVisible() && deathArea.contains(mouseX, mouseY)) {
            this.activeHudDrag = HudDragTarget.DEATH_COORDS;
            this.hudDragOffsetX = mouseX - deathArea.getX();
            this.hudDragOffsetY = mouseY - deathArea.getY();
            this.hudDragSnapX = false;
            this.hudDragSnapY = false;
            return true;
        }

        final HudRenderer.HudArea statusArea = renderer.getStatusEffectsArea();
        if (statusArea.isVisible() && statusArea.contains(mouseX, mouseY)) {
            this.activeHudDrag = HudDragTarget.STATUS_EFFECTS;
            this.hudDragOffsetX = mouseX - statusArea.getX();
            this.hudDragOffsetY = mouseY - statusArea.getY();
            this.hudDragSnapX = false;
            this.hudDragSnapY = false;
            return true;
        }

        return false;
    }

    private void updateHudDrag(final double mouseX, final double mouseY) {
        if (this.minecraft == null || this.activeHudDrag == null) {
            return;
        }

        final int screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        final int screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        final HudRenderer renderer = this.runtime.getHudRenderer();

        if (this.activeHudDrag == HudDragTarget.WATERMARK) {
            final HudRenderer.HudArea area = renderer.getWatermarkArea();
            final WatermarkModule watermarkModule = this.runtime.getModuleManager().get(WatermarkModule.class);
            if (!area.isVisible() || watermarkModule == null) {
                return;
            }
            final HudEditorSnapMath.SnapResult snappedX = HudEditorSnapMath.snapAxis(
                MathUtil.clamp(mouseX - this.hudDragOffsetX, 0.0D, screenWidth - area.getWidth()),
                area.getWidth(),
                screenWidth,
                this.hudDragSnapX
            );
            final HudEditorSnapMath.SnapResult snappedY = HudEditorSnapMath.snapAxis(
                MathUtil.clamp(mouseY - this.hudDragOffsetY, 0.0D, screenHeight - area.getHeight()),
                area.getHeight(),
                screenHeight,
                this.hudDragSnapY
            );
            this.hudDragSnapX = snappedX.snapped;
            this.hudDragSnapY = snappedY.snapped;
            watermarkModule.setPosition(snappedX.position, snappedY.position);
            return;
        }

        if (this.activeHudDrag == HudDragTarget.MODULES) {
            final HudRenderer.HudArea area = renderer.getModulesArea();
            final HudModule hudModule = this.runtime.getModuleManager().get(HudModule.class);
            if (!area.isVisible() || hudModule == null) {
                return;
            }
            final HudEditorSnapMath.SnapResult snappedX = HudEditorSnapMath.snapAxis(
                MathUtil.clamp(mouseX - this.hudDragOffsetX, 0.0D, screenWidth - area.getWidth()),
                area.getWidth(),
                screenWidth,
                this.hudDragSnapX
            );
            final HudEditorSnapMath.SnapResult snappedY = HudEditorSnapMath.snapAxis(
                MathUtil.clamp(mouseY - this.hudDragOffsetY, 0.0D, screenHeight - area.getHeight()),
                area.getHeight(),
                screenHeight,
                this.hudDragSnapY
            );
            this.hudDragSnapX = snappedX.snapped;
            this.hudDragSnapY = snappedY.snapped;
            hudModule.setModulesPosition(snappedX.position, snappedY.position);
            return;
        }

        if (this.activeHudDrag == HudDragTarget.RADAR) {
            final HudRenderer.HudArea area = renderer.getRadarArea();
            final RadarModule radarModule = this.runtime.getModuleManager().get(RadarModule.class);
            if (!area.isVisible() || radarModule == null) {
                return;
            }
            final HudEditorSnapMath.SnapResult snappedX = HudEditorSnapMath.snapAxis(
                MathUtil.clamp(mouseX - this.hudDragOffsetX, 0.0D, screenWidth - area.getWidth()),
                area.getWidth(),
                screenWidth,
                this.hudDragSnapX
            );
            final HudEditorSnapMath.SnapResult snappedY = HudEditorSnapMath.snapAxis(
                MathUtil.clamp(mouseY - this.hudDragOffsetY, 0.0D, screenHeight - area.getHeight()),
                area.getHeight(),
                screenHeight,
                this.hudDragSnapY
            );
            this.hudDragSnapX = snappedX.snapped;
            this.hudDragSnapY = snappedY.snapped;
            radarModule.setPosition(snappedX.position, snappedY.position);
            return;
        }

        if (this.activeHudDrag == HudDragTarget.DEATH_COORDS) {
            final HudRenderer.HudArea area = renderer.getDeathCoordsArea();
            final DeathCoordsModule deathCoordsModule = this.runtime.getModuleManager().get(DeathCoordsModule.class);
            if (!area.isVisible() || deathCoordsModule == null) {
                return;
            }
            final HudEditorSnapMath.SnapResult snappedX = HudEditorSnapMath.snapAxis(
                MathUtil.clamp(mouseX - this.hudDragOffsetX, 0.0D, screenWidth - area.getWidth()),
                area.getWidth(),
                screenWidth,
                this.hudDragSnapX
            );
            final HudEditorSnapMath.SnapResult snappedY = HudEditorSnapMath.snapAxis(
                MathUtil.clamp(mouseY - this.hudDragOffsetY, 0.0D, screenHeight - area.getHeight()),
                area.getHeight(),
                screenHeight,
                this.hudDragSnapY
            );
            this.hudDragSnapX = snappedX.snapped;
            this.hudDragSnapY = snappedY.snapped;
            deathCoordsModule.setPosition(snappedX.position, snappedY.position);
            return;
        }

        if (this.activeHudDrag == HudDragTarget.STATUS_EFFECTS) {
            final HudRenderer.HudArea area = renderer.getStatusEffectsArea();
            final StatusEffectsModule statusEffectsModule = this.runtime.getModuleManager().get(StatusEffectsModule.class);
            if (!area.isVisible() || statusEffectsModule == null) {
                return;
            }
            final HudEditorSnapMath.SnapResult snappedX = HudEditorSnapMath.snapAxis(
                MathUtil.clamp(mouseX - this.hudDragOffsetX, 0.0D, screenWidth - area.getWidth()),
                area.getWidth(),
                screenWidth,
                this.hudDragSnapX
            );
            final HudEditorSnapMath.SnapResult snappedY = HudEditorSnapMath.snapAxis(
                MathUtil.clamp(mouseY - this.hudDragOffsetY, 0.0D, screenHeight - area.getHeight()),
                area.getHeight(),
                screenHeight,
                this.hudDragSnapY
            );
            this.hudDragSnapX = snappedX.snapped;
            this.hudDragSnapY = snappedY.snapped;
            statusEffectsModule.setPosition(snappedX.position, snappedY.position);
        }
    }

    private void renderHudSnapGuides() {
        if (this.minecraft == null) {
            return;
        }
        final double screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        final double screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
        final double guideX = HudEditorSnapMath.guideX(screenWidth);
        final double guideY = HudEditorSnapMath.guideY(screenHeight);

        UiRenderer.drawLine(guideX, 0.0D, guideX, screenHeight, this.hudDragSnapX ? 1.6D : 1.0D,
            ColorUtil.rgba(255, 255, 255, this.hudDragSnapX ? 196 : 122));
        UiRenderer.drawLine(0.0D, guideY, screenWidth, guideY, this.hudDragSnapY ? 1.6D : 1.0D,
            ColorUtil.rgba(255, 255, 255, this.hudDragSnapY ? 196 : 122));
    }

    private Runnable bindToggle(final String label) {
        return new Runnable() {
            @Override
            public void run() {
                if ("Enabled".equals(label) && page == Page.RAGE) rageEnabled = !rageEnabled;
                else if ("Enabled".equals(label) && page == Page.LEGIT) legitEnabled = !legitEnabled;
                else if ("Enabled".equals(label) && page == Page.VISUALS) visualsEnabled = !visualsEnabled;
                else if ("Silent Aim".equals(label)) silentAim = !silentAim;
                else if ("Automatic Fire".equals(label)) automaticFire = !automaticFire;
                else if ("Aim Through Walls".equals(label)) aimThroughWalls = !aimThroughWalls;
                else if ("Quick Stop".equals(label)) quickStop = !quickStop;
                else if ("Quick Scope".equals(label)) quickScope = !quickScope;
                else if ("Delay Shot".equals(label)) delayShot = !delayShot;
                else if ("Remove Recoil".equals(label)) removeRecoil = !removeRecoil;
                else if ("Remove Spread".equals(label)) removeSpread = !removeSpread;
                else if ("Duck Peek Assist".equals(label)) duckPeekAssist = !duckPeekAssist;
                else if ("Quick Peek Assist".equals(label)) quickPeekAssist = !quickPeekAssist;
                else if ("Double Tap".equals(label)) doubleTap = !doubleTap;
                else if ("Recoil Control".equals(label)) recoilControl = !recoilControl;
                else if ("Backtrack Assist".equals(label)) backtrackAssist = !backtrackAssist;
                else if ("Visible Only".equals(label)) visibleOnly = !visibleOnly;
                else if ("Through Smoke".equals(label)) throughSmoke = !throughSmoke;
                else if ("Magnet Trigger".equals(label)) magnetTrigger = !magnetTrigger;
                else if ("Slow Walk".equals(label)) slowWalk = !slowWalk;
                else if ("Auto Scope".equals(label)) autoScope = !autoScope;
                else if ("Quick Buy".equals(label)) quickBuy = !quickBuy;
                else if ("Auto Rebuy".equals(label)) autoRebuy = !autoRebuy;
                else if ("Favorite Slots".equals(label)) favoriteSlots = !favoriteSlots;
                else if ("Discord RPC".equals(label)) miscDiscord = !miscDiscord;
                else if ("Notifications".equals(label)) miscNotifications = !miscNotifications;
                else if ("Cloud Sync".equals(label)) miscCloudSync = !miscCloudSync;
                else if ("Auto Backup".equals(label)) miscAutoBackup = !miscAutoBackup;
                else if ("Compact Logs".equals(label)) miscCompactLogs = !miscCompactLogs;
                else if ("Offscreen Arrow".equals(label)) visualsOffscreen = !visualsOffscreen;
                else if ("Sounds".equals(label)) visualsSounds = !visualsSounds;
                else if ("Soul Particles".equals(label)) soulParticles = !soulParticles;
                else if ("Glow".equals(label)) glow = !glow;
                else if ("Bullet Impacts".equals(label)) {
                } else if ("Grenade Trajectory".equals(label)) {
                } else if ("Grenade Proximity Warnings".equals(label)) {
                }
            }
        };
    }

    private PickerColor getColor(final ColorSlot slot) {
        if (slot == ColorSlot.WALLS) return this.wallColor;
        if (slot == ColorSlot.HISTORY) return this.historyColor;
        if (slot == ColorSlot.SOUL) return this.soulColor;
        if (slot == ColorSlot.GLOW) return this.glowColor;
        return this.primaryColor;
    }

    private String[] dropdownOptions(final Dropdown dropdown) {
        if (dropdown == Dropdown.HISTORY) return new String[]{"Low", "Medium", "High"};
        if (dropdown == Dropdown.TARGET) return new String[]{"Hit Chance", "Distance", "Cycle"};
        if (dropdown == Dropdown.HITBOXES) return new String[]{"Head", "Chest", "Pelvis"};
        if (dropdown == Dropdown.MULTIPOINT) return new String[]{"Select", "Sparse", "Aggressive"};
        if (dropdown == Dropdown.LEGIT_TARGET) return new String[]{"Distance", "Crosshair", "Health"};
        if (dropdown == Dropdown.LEGIT_BONE) return new String[]{"Head", "Chest", "Nearest"};
        if (dropdown == Dropdown.STICKER) return new String[]{"Default", "Raw", "Mixed"};
        if (dropdown == Dropdown.SORTING) return new String[]{"Classic", "Compact", "Minimal"};
        if (dropdown == Dropdown.MISC_STYLE) return new String[]{"Dark", "Glass"};
        if (dropdown == Dropdown.PLAYER_MODE) return new String[]{"Glow", "Chams", "Flat"};
        if (dropdown == Dropdown.WALLS_MODE) return new String[]{"Disabled", "Glow", "Outline"};
        if (dropdown == Dropdown.ONSHOT_MODE) return new String[]{"Disabled", "Glow", "Ghost"};
        if (dropdown == Dropdown.HISTORY_MODE) return new String[]{"Disabled", "Trail", "Skeleton"};
        return new String[]{"Disabled", "Highlight", "Pulse"};
    }

    private int dropdownIndex(final Dropdown dropdown) {
        if (dropdown == Dropdown.HISTORY) return this.historyIndex;
        if (dropdown == Dropdown.TARGET) return this.targetIndex;
        if (dropdown == Dropdown.HITBOXES) return this.hitboxesIndex;
        if (dropdown == Dropdown.MULTIPOINT) return this.multipointIndex;
        if (dropdown == Dropdown.LEGIT_TARGET) return this.legitTargetIndex;
        if (dropdown == Dropdown.LEGIT_BONE) return this.legitBoneIndex;
        if (dropdown == Dropdown.STICKER) return this.stickerIndex;
        if (dropdown == Dropdown.SORTING) return this.sortingIndex;
        if (dropdown == Dropdown.MISC_STYLE) return this.miscStyleIndex;
        if (dropdown == Dropdown.PLAYER_MODE) return this.playerModeIndex;
        if (dropdown == Dropdown.WALLS_MODE) return this.wallsModeIndex;
        if (dropdown == Dropdown.ONSHOT_MODE) return this.onShotModeIndex;
        if (dropdown == Dropdown.HISTORY_MODE) return this.historyModeIndex;
        return this.ragdollModeIndex;
    }

    private void setDropdownIndex(final Dropdown dropdown, final int index) {
        if (dropdown == Dropdown.HISTORY) this.historyIndex = index;
        else if (dropdown == Dropdown.TARGET) this.targetIndex = index;
        else if (dropdown == Dropdown.HITBOXES) this.hitboxesIndex = index;
        else if (dropdown == Dropdown.MULTIPOINT) this.multipointIndex = index;
        else if (dropdown == Dropdown.LEGIT_TARGET) this.legitTargetIndex = index;
        else if (dropdown == Dropdown.LEGIT_BONE) this.legitBoneIndex = index;
        else if (dropdown == Dropdown.STICKER) this.stickerIndex = index;
        else if (dropdown == Dropdown.SORTING) this.sortingIndex = index;
        else if (dropdown == Dropdown.MISC_STYLE) this.miscStyleIndex = index;
        else if (dropdown == Dropdown.PLAYER_MODE) this.playerModeIndex = index;
        else if (dropdown == Dropdown.WALLS_MODE) this.wallsModeIndex = index;
        else if (dropdown == Dropdown.ONSHOT_MODE) this.onShotModeIndex = index;
        else if (dropdown == Dropdown.HISTORY_MODE) this.historyModeIndex = index;
        else this.ragdollModeIndex = index;
    }

    private String dropdownValue(final Dropdown dropdown) {
        return dropdownOptions(dropdown)[dropdownIndex(dropdown)];
    }

    private double dropdownX(final Dropdown dropdown) {
        final Layout layout = createLayout();
        final double baseX = layout.contentX;
        if (dropdown == Dropdown.HISTORY) return baseX + layout.columnWidth + layout.columnGap + layout.columnWidth - this.debugProfile.dropdownWidth - 12.0D;
        if (dropdown == Dropdown.TARGET || dropdown == Dropdown.HITBOXES || dropdown == Dropdown.MULTIPOINT) return baseX + layout.columnWidth - this.debugProfile.dropdownWidth - 12.0D;
        if (dropdown == Dropdown.LEGIT_TARGET || dropdown == Dropdown.LEGIT_BONE) return baseX + layout.columnWidth - this.debugProfile.dropdownWidth - 12.0D;
        if (dropdown == Dropdown.STICKER) return baseX + layout.columnWidth - this.debugProfile.dropdownWidth - 12.0D;
        if (dropdown == Dropdown.SORTING) return baseX + layout.columnWidth + layout.columnGap + layout.columnWidth - this.debugProfile.dropdownWidth - 12.0D;
        if (dropdown == Dropdown.MISC_STYLE) return baseX + layout.columnWidth - this.debugProfile.dropdownWidth - 12.0D;
        return baseX + layout.columnWidth - this.debugProfile.dropdownWidth - 40.0D;
    }

    private double dropdownY(final Dropdown dropdown) {
        final Layout layout = createLayout();
        if (dropdown == Dropdown.HISTORY) return layout.contentY + 32.0D;
        if (dropdown == Dropdown.TARGET) return layout.contentY + 236.0D;
        if (dropdown == Dropdown.HITBOXES) return layout.contentY + 270.0D;
        if (dropdown == Dropdown.MULTIPOINT) return layout.contentY + 304.0D;
        if (dropdown == Dropdown.LEGIT_TARGET) return layout.contentY + 236.0D;
        if (dropdown == Dropdown.LEGIT_BONE) return layout.contentY + 270.0D;
        if (dropdown == Dropdown.STICKER) return layout.contentY + 134.0D;
        if (dropdown == Dropdown.SORTING) return layout.contentY + 134.0D;
        if (dropdown == Dropdown.MISC_STYLE) return layout.contentY + 100.0D;
        if (dropdown == Dropdown.PLAYER_MODE) return layout.contentY + 150.0D;
        if (dropdown == Dropdown.WALLS_MODE) return layout.contentY + 184.0D;
        if (dropdown == Dropdown.ONSHOT_MODE) return layout.contentY + 218.0D;
        if (dropdown == Dropdown.HISTORY_MODE) return layout.contentY + 252.0D;
        return layout.contentY + 286.0D;
    }

    private double dropdownWidth(final Dropdown dropdown) {
        return this.debugProfile.dropdownWidth;
    }

    private void renderSearchBar(final MatrixStack matrixStack, final double x, final double y, final double width, final String value, final String placeholder) {
        final boolean placeholderVisible = placeholder.equals(value);
        final double iconX = placeholderVisible
            ? MenuLayoutMath.searchPlaceholderIconX(x, width, textWidth(value, this.debugProfile.textScale))
            : MenuLayoutMath.searchFieldIconX(x);
        final double textX = placeholderVisible
            ? MenuLayoutMath.searchPlaceholderTextX(x, width, textWidth(value, this.debugProfile.textScale))
            : MenuLayoutMath.searchFieldTextX(x);
        drawControlShadow(x, y, width, 32.0D, 9.0D);
        UiRenderer.drawRoundedRect(x, y, width, 32.0D, 9.0D, surfaceRaisedColor());
        UiRenderer.drawRoundedOutline(x, y, width, 32.0D, 9.0D, themeManager().controlOutlineWidth(), controlOutlineColor());
        drawSearchIcon(iconX, MenuLayoutMath.searchFieldIconY(y, 32.0D, 12.0D), TEXT_MUTED);
        drawBodyText(matrixStack, value, textX, y + 10.0D, placeholderVisible ? TEXT_MUTED : TEXT);
    }

    private void renderSelector(final MatrixStack matrixStack, final double x, final double y, final double width, final String label, final boolean selected, final Runnable action) {
        final double height = this.debugProfile.topbarSelectorHeight;
        drawControlShadow(x, y, width, height, 7.0D);
        UiRenderer.drawRoundedRect(x, y, width, height, 7.0D, selected ? surfaceHoverColor() : ColorUtil.rgba(255, 255, 255, themeManager().isGlass() ? 10 : 5));
        UiRenderer.drawRoundedOutline(x, y, width, height, 7.0D, themeManager().controlOutlineWidth(), controlOutlineColor());
        drawBodyText(matrixStack, label, x + 10.0D, y + Math.max(4.0D, (height - 14.0D) / 2.0D), textColor());
        drawChevronDown(x + width - 16.0D, y + Math.max(8.0D, (height - 6.0D) / 2.0D), textSoftColor());
        this.clickTargets.add(new ClickTarget(x, y, width, height, GLFW.GLFW_MOUSE_BUTTON_LEFT, action));
    }

    private void renderTopbarActionButton(final MatrixStack matrixStack, final double x, final double y, final double width, final String label, final Runnable action) {
        final double height = this.debugProfile.topbarSelectorHeight;
        drawControlShadow(x, y, width, height, 7.0D);
        UiRenderer.drawRoundedRect(x, y, width, height, 7.0D, ColorUtil.rgba(255, 255, 255, themeManager().isGlass() ? 10 : 5));
        UiRenderer.drawRoundedOutline(x, y, width, height, 7.0D, themeManager().controlOutlineWidth(), controlOutlineColor());
        drawBodyText(matrixStack, label, MenuLayoutMath.topbarActionLabelX(x, width, textWidth(label, this.debugProfile.textScale)), MenuLayoutMath.topbarActionLabelY(y, height), textColor());
        this.clickTargets.add(new ClickTarget(x, y, width, height, GLFW.GLFW_MOUSE_BUTTON_LEFT, action));
    }

    private void renderSmallButton(final double x, final double y, final double width, final double height, final boolean selected, final Runnable action) {
        drawControlShadow(x, y, width, height, 7.0D);
        UiRenderer.drawRoundedRect(x, y, width, height, 7.0D, selected ? surfaceHoverColor() : ColorUtil.rgba(255, 255, 255, themeManager().isGlass() ? 10 : 5));
        UiRenderer.drawRoundedOutline(x, y, width, height, 7.0D, themeManager().controlOutlineWidth(), controlOutlineColor());
        this.clickTargets.add(new ClickTarget(x, y, width, height, GLFW.GLFW_MOUSE_BUTTON_LEFT, action));
    }

    private void renderToggle(final double x, final double y, final boolean enabled, final int accent) {
        final double knobX = x + (enabled ? 18.0D : 6.0D);
        final double knobY = y + 7.0D;
        final int trackColor = enabled ? accent : ColorUtil.rgba(255, 255, 255, themeManager().isGlass() ? 30 : 22);
        final int trackOutline = enabled
            ? ColorUtil.withAlpha(accent, themeManager().isGlass() ? 96 : 124)
            : controlOutlineColor();
        final int knobOutline = ColorUtil.rgba(255, 255, 255, enabled ? (themeManager().isGlass() ? 150 : 180) : (themeManager().isGlass() ? 68 : 92));

        UiRenderer.drawRoundedRect(x, y, 24.0D, 14.0D, 7.0D, trackColor);
        UiRenderer.drawRoundedOutline(x, y, 24.0D, 14.0D, 7.0D, themeManager().controlOutlineWidth() + 0.35D, trackOutline);
        UiRenderer.drawCircle(knobX, knobY, 5.8D, knobOutline);
        UiRenderer.drawCircle(knobX, knobY, 5.0D, ColorUtil.rgba(255, 255, 255, enabled ? 255 : 190));
    }

    private void renderColorSwatch(final double x, final double y, final PickerColor color) {
        UiRenderer.drawRoundedRect(x, y, 12.0D, 12.0D, 4.0D, color.color());
        UiRenderer.drawRoundedOutline(x, y, 12.0D, 12.0D, 4.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 30));
    }

    private void renderColorSquare(final double x, final double y, final double size, final PickerColor color) {
        UiRenderer.drawGradientRect(x, y, size, size, ColorUtil.rgba(255, 255, 255, 255), color.opaque(), color.opaque(), ColorUtil.rgba(255, 255, 255, 255));
        UiRenderer.drawGradientRect(x, y, size, size, ColorUtil.rgba(0, 0, 0, 0), ColorUtil.rgba(0, 0, 0, 0), ColorUtil.rgba(0, 0, 0, 255), ColorUtil.rgba(0, 0, 0, 255));
        UiRenderer.drawRoundedOutline(x, y, size, size, 10.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 14));
    }

    private void renderHueBar(final double x, final double y, final double width) {
        final int[] colors = new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
        final double segment = width / 6.0D;
        for (int i = 0; i < 6; i++) {
            UiRenderer.drawGradientRect(x + segment * i, y, segment + 1.0D, 6.0D, colors[i], colors[i + 1], colors[i + 1], colors[i]);
        }
        UiRenderer.drawRoundedOutline(x, y, width, 6.0D, 3.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 18));
    }

    private void renderHueStrip(final double x, final double y, final double width, final double height) {
        final int[] colors = new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
        final double segment = width / 6.0D;
        for (int i = 0; i < 6; i++) {
            UiRenderer.drawGradientRect(x + segment * i, y, segment + 1.0D, height, colors[i], colors[i + 1], colors[i + 1], colors[i]);
        }
        UiRenderer.drawRoundedOutline(x - 0.5D, y - 0.5D, width + 1.0D, height + 1.0D, 3.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 24));
    }

    private void renderAlphaBar(final double x, final double y, final double width, final PickerColor color) {
        UiRenderer.drawGradientRect(x, y, width, 6.0D, ColorUtil.withAlpha(color.opaque(), 0), color.color(), color.color(), ColorUtil.withAlpha(color.opaque(), 0));
        UiRenderer.drawRoundedOutline(x, y, width, 6.0D, 3.0D, 1.0D, ColorUtil.rgba(255, 255, 255, 18));
    }

    private void drawLogo(final MatrixStack matrixStack, final double x, final double y) {
        UiRenderer.drawRoundedRect(x, y + 2.0D, 24.0D, 24.0D, 8.0D, surfaceAccentShell());
        UiRenderer.drawRoundedRect(x + 3.0D, y + 5.0D, 18.0D, 18.0D, 6.0D, accentColor(180));
        UiRenderer.drawLine(x + 7.0D, y + 19.0D, x + 7.0D, y + 10.0D, 1.6D, ColorUtil.rgba(210, 232, 255, 255));
        UiRenderer.drawLine(x + 7.0D, y + 10.0D, x + 12.0D, y + 16.0D, 1.6D, ColorUtil.rgba(210, 232, 255, 255));
        UiRenderer.drawLine(x + 12.0D, y + 16.0D, x + 17.0D, y + 10.0D, 1.6D, ColorUtil.rgba(210, 232, 255, 255));
        UiRenderer.drawLine(x + 17.0D, y + 10.0D, x + 17.0D, y + 19.0D, 1.6D, ColorUtil.rgba(210, 232, 255, 255));
        drawScaledText(matrixStack, "Mdk2", x + this.debugProfile.logoTextXOffset, y + this.debugProfile.logoTitleYOffset, textColor(), this.debugProfile.logoTitleScale);
        drawScaledText(matrixStack, "Minecraft", x + this.debugProfile.logoTextXOffset, y + this.debugProfile.logoSubtitleYOffset, textMutedColor(), this.debugProfile.logoSubtitleScale);
    }

    private void drawBodyText(final MatrixStack matrixStack, final String text, final double x, final double y, final int color) {
        drawScaledText(matrixStack, text, x, y, color, this.debugProfile.textScale);
    }

    private ThemeManager themeManager() {
        return this.runtime.getThemeManager();
    }

    private int windowColor() {
        return themeManager().background(228);
    }

    private int sidebarColor() {
        return themeManager().isGlass() ? ColorUtil.rgba(12, 18, 28, 172) : SIDEBAR;
    }

    private int surfaceColor() {
        return themeManager().surface(236);
    }

    private int surfaceRaisedColor() {
        return themeManager().surfaceRaised(242);
    }

    private int surfaceHoverColor() {
        return themeManager().isGlass() ? ColorUtil.rgba(64, 74, 98, 132) : SURFACE_HOVER;
    }

    private int outlineColor() {
        return themeManager().outline(themeManager().windowOutlineAlpha());
    }

    private int containerOutlineColor() {
        return themeManager().outline(themeManager().containerOutlineAlpha());
    }

    private int controlOutlineColor() {
        return themeManager().outline(themeManager().controlOutlineAlpha());
    }

    private int popupOutlineColor() {
        return themeManager().outline(themeManager().popupOutlineAlpha());
    }

    private int dividerColor() {
        return themeManager().outline(themeManager().isGlass() ? 24 : 18);
    }

    private int dividerSoftColor() {
        return themeManager().outline(themeManager().isGlass() ? 14 : 10);
    }

    private int textColor() {
        return themeManager().textPrimary();
    }

    private int textSoftColor() {
        return themeManager().textSecondary();
    }

    private int textMutedColor() {
        return themeManager().isGlass() ? ColorUtil.rgba(150, 163, 184, 220) : TEXT_MUTED;
    }

    private int accentColor(final int alpha) {
        return ColorUtil.withAlpha(themeManager().accent(0.0D), alpha);
    }

    private int accentColor() {
        return themeManager().accent(0.0D);
    }

    private int shadowColor() {
        return ColorUtil.rgba(0, 0, 0, themeManager().shellShadowAlpha());
    }

    private void drawControlShadow(final double x, final double y, final double width, final double height, final double radius) {
        if (themeManager().controlShadowAlpha() <= 0) {
            return;
        }
        UiRenderer.drawShadow(
            x,
            y,
            width,
            height,
            themeManager().controlShadowSpread(),
            ColorUtil.rgba(0, 0, 0, themeManager().controlShadowAlpha())
        );
    }

    private int surfaceAccentShell() {
        return themeManager().isGlass() ? ColorUtil.rgba(255, 255, 255, 18) : ColorUtil.rgba(255, 255, 255, 8);
    }

    private int shellInnerOutlineColor() {
        return ColorUtil.rgba(255, 255, 255, themeManager().shellInnerOutlineAlpha());
    }

    private void renderElevatedShell(final double x, final double y, final double width, final double height, final double radius,
                                     final int shadowRadius, final int fillColor, final double outlineWidth, final int outlineColor) {
        UiRenderer.drawRoundedRect(x, y, width, height, radius, fillColor);
        UiRenderer.drawRoundedOutline(x, y, width, height, radius, outlineWidth, outlineColor);
        UiRenderer.drawRoundedOutline(
            x + 1.0D,
            y + 1.0D,
            width - 2.0D,
            height - 2.0D,
            Math.max(1.0D, radius - 1.0D),
            1.0D,
            shellInnerOutlineColor()
        );
        if (themeManager().shellSheenTopAlpha() > 0 || themeManager().shellSheenBottomAlpha() > 0) {
            UiRenderer.drawGradientRect(
                x,
                y,
                width,
                height,
                ColorUtil.rgba(255, 255, 255, themeManager().shellSheenTopAlpha()),
                ColorUtil.rgba(255, 255, 255, 0),
                ColorUtil.rgba(0, 0, 0, themeManager().shellSheenBottomAlpha()),
                ColorUtil.rgba(0, 0, 0, Math.max(0, themeManager().shellSheenBottomAlpha() - 4))
            );
        }
        drawShellOuterFrame(x, y, width, height, radius);
    }

    private void drawShellOuterFrame(final double x, final double y, final double width, final double height, final double radius) {
        if (themeManager().shellOuterBandAlpha() <= 0 && themeManager().shellOuterStrokeAlpha() <= 0) {
            return;
        }
        final double bandInset = themeManager().shellOuterBandInset();
        UiRenderer.drawRoundedRect(
            x - bandInset,
            y - bandInset,
            width + bandInset * 2.0D,
            height + bandInset * 2.0D,
            radius + bandInset,
            ColorUtil.rgba(0, 0, 0, themeManager().shellOuterBandAlpha())
        );
        UiRenderer.drawShadow(
            x - 3.0D,
            y - 3.0D,
            width + 6.0D,
            height + 6.0D,
            18,
            ColorUtil.rgba(0, 0, 0, themeManager().shellOuterBandAlpha())
        );
        UiRenderer.drawShadow(
            x - 1.0D,
            y - 1.0D,
            width + 2.0D,
            height + 2.0D,
            10,
            ColorUtil.rgba(0, 0, 0, themeManager().shellOuterStrokeAlpha())
        );
        UiRenderer.drawRoundedOutline(
            x - 1.0D,
            y - 1.0D,
            width + 2.0D,
            height + 2.0D,
            radius + 1.0D,
            1.6D,
            ColorUtil.rgba(0, 0, 0, themeManager().shellOuterStrokeAlpha())
        );
    }

    private void drawValueText(final MatrixStack matrixStack, final String text, final double x, final double y, final int color) {
        drawScaledText(matrixStack, text, x, y, color, this.debugProfile.valueScale);
    }

    private void drawPopupBodyText(final MatrixStack matrixStack, final String text, final double x, final double y, final int color) {
        drawScaledText(matrixStack, text, x, y, color, this.debugProfile.textScale * 0.88D);
    }

    private void drawPopupValueText(final MatrixStack matrixStack, final String text, final double x, final double y, final int color) {
        drawScaledText(matrixStack, text, x, y, color, this.debugProfile.valueScale * 0.88D);
    }

    private void drawPopupMiniText(final MatrixStack matrixStack, final String text, final double x, final double y, final int color, final double scale) {
        drawScaledText(matrixStack, text, x, y, color, scale * this.debugProfile.miniScale * 0.90D);
    }

    private void drawScaledText(final MatrixStack matrixStack, final String text, final double x, final double y, final int color, final double scale) {
        final Layout layout = this.renderLayout != null ? this.renderLayout : createLayout();
        final double screenX = layout.originX + x * layout.scale;
        final double screenY = layout.originY + y * layout.scale;
        final double screenScale = scale * layout.scale;
        UiRenderer.push();
        UiRenderer.scale(1.0D / layout.scale, 1.0D / layout.scale, 1.0D);
        UiRenderer.translate(-layout.originX, -layout.originY, 0.0D);
        if (themeManager().drawsTextShadow()) {
            menuFont().drawString(text, screenX + 1.0D, screenY + 1.0D, ColorUtil.multiplyAlpha(ColorUtil.rgba(0, 0, 0, 255), 0.38D), screenScale);
        }
        menuFont().drawString(text, screenX, screenY, color, screenScale);
        UiRenderer.pop();
    }

    private double textWidth(final String text, final double scale) {
        return menuFont().width(text, scale);
    }

    private double popupTextWidth(final String text, final double scale) {
        return menuFont().width(text, scale);
    }

    private void drawMiniText(final MatrixStack matrixStack, final String text, final double x, final double y, final int color, final double scale) {
        drawScaledText(matrixStack, text, x, y, color, scale * this.debugProfile.miniScale);
    }

    private List<String> wrapPopupText(final String text, final double width, final double scale) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> lines = new ArrayList<String>();
        final String[] words = text.split(" ");
        final StringBuilder line = new StringBuilder();
        for (final String word : words) {
            final String candidate = line.length() == 0 ? word : line + " " + word;
            if (popupTextWidth(candidate, scale * this.debugProfile.miniScale * 0.90D) > width && line.length() > 0) {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    private MenuFontRenderer menuFont() {
        final double desiredSize = Math.max(8.0D, this.debugProfile.fontSize);
        if (this.menuFont == null || Math.abs(this.cachedFontSize - desiredSize) > 0.001D) {
            this.cachedFontSize = desiredSize;
            this.menuFont = new MenuFontRenderer("Segoe UI", (int) Math.round(desiredSize), Font.PLAIN);
        }
        return this.menuFont;
    }

    private MenuFontRenderer debugFont() {
        if (this.debugFont == null) {
            this.debugFont = new MenuFontRenderer("Segoe UI", 14, Font.PLAIN);
        }
        return this.debugFont;
    }

    private void drawSearchIcon(final double x, final double y, final int color) {
        UiRenderer.drawCircle(x + 4.0D, y + 4.0D, 4.2D, color);
        UiRenderer.drawCircle(x + 4.0D, y + 4.0D, 2.4D, WINDOW);
        UiRenderer.drawLine(x + 7.2D, y + 7.2D, x + 11.5D, y + 11.5D, 1.5D, color);
    }

    private void drawSaveIcon(final double x, final double y, final int color) {
        UiRenderer.drawRoundedOutline(x, y, 10.0D, 10.0D, 2.0D, 1.2D, color);
        UiRenderer.drawRect(x + 2.0D, y + 2.0D, 5.0D, 2.0D, color);
        UiRenderer.drawRect(x + 6.0D, y + 2.0D, 2.0D, 3.0D, color);
        UiRenderer.drawRect(x + 3.0D, y + 6.0D, 4.0D, 2.0D, color);
    }

    private void drawLockIcon(final double x, final double y, final int color) {
        UiRenderer.drawRoundedOutline(x, y + 4.0D, 10.0D, 8.0D, 2.0D, 1.5D, color);
        UiRenderer.drawRoundedOutline(x + 2.0D, y, 6.0D, 7.0D, 3.0D, 1.5D, color);
    }

    private void drawDots(final double x, final double y, final int color) {
        UiRenderer.drawCircle(x, y, 1.2D, color);
        UiRenderer.drawCircle(x + 4.0D, y, 1.2D, color);
        UiRenderer.drawCircle(x + 8.0D, y, 1.2D, color);
    }

    private void drawChevronDown(final double x, final double y, final int color) {
        UiRenderer.drawLine(x, y, x + 4.0D, y + 4.0D, 1.4D, color);
        UiRenderer.drawLine(x + 4.0D, y + 4.0D, x + 8.0D, y, 1.4D, color);
    }

    private void drawChevronRight(final double x, final double y, final int color) {
        UiRenderer.drawLine(x, y, x + 4.0D, y + 4.0D, 1.4D, color);
        UiRenderer.drawLine(x + 4.0D, y + 4.0D, x, y + 8.0D, 1.4D, color);
    }

    private double panelHeight(final String title) {
        if ("PREVIEW".equals(title)) {
            if (this.page == Page.VISUALS && this.visualPage == VisualPage.PLAYERS) {
                return 286.0D + this.debugProfile.panelStackGap + panelHeight("EFFECTS");
            }
            return 286.0D;
        }
        final int rows = panelRowCount(title);
        if (rows > 0) {
            return this.debugProfile.panelHeaderOffset + Math.max(0, rows - 1) * this.debugProfile.panelRowStep + 34.0D;
        }
        return this.debugProfile.panelHeaderOffset + 8.0D * this.debugProfile.panelRowStep + 34.0D;
    }

    private int panelRowCount(final String title) {
        if ("PREVIEW".equals(title)) {
            return 0;
        }
        return modulesForPanel(title).size();
    }

    private double sliderProgress(final SliderId slider) {
        if (slider == SliderId.FIELD_OF_VIEW) return this.fieldOfView / 180.0D;
        if (slider == SliderId.HIT_CHANCE) return this.hitChance / 100.0D;
        if (slider == SliderId.MIN_DAMAGE) return this.minDamage / 120.0D;
        if (slider == SliderId.LEGIT_SMOOTHING) return this.legitSmoothing / 100.0D;
        if (slider == SliderId.LEGIT_REACTION) return this.legitReaction / 250.0D;
        if (slider == SliderId.LEGIT_FOV) return this.legitFov / 15.0D;
        if (slider == SliderId.LEGIT_BURST) return this.legitBurst / 10.0D;
        if (slider == SliderId.WEAR_OVERRIDE) return this.wearOverride / 100.0D;
        if (slider == SliderId.PREVIEW_X) return (this.previewEspOffsetX + 80.0D) / 160.0D;
        if (slider == SliderId.PREVIEW_Y) return (this.previewEspOffsetY + 60.0D) / 120.0D;
        return this.animationSpeed / 100.0D;
    }

    private double toVirtualX(final Layout layout, final double screenX) {
        return (screenX - layout.originX) / layout.scale;
    }

    private double toVirtualY(final Layout layout, final double screenY) {
        return (screenY - layout.originY) / layout.scale;
    }

    private enum Page {
        RAGE,
        LEGIT,
        VISUALS,
        INVENTORY,
        MISC
    }

    private enum VisualPage {
        PLAYERS,
        WORLD
    }

    private enum Dropdown {
        HISTORY,
        TARGET,
        HITBOXES,
        MULTIPOINT,
        LEGIT_TARGET,
        LEGIT_BONE,
        STICKER,
        SORTING,
        MISC_STYLE,
        PLAYER_MODE,
        WALLS_MODE,
        ONSHOT_MODE,
        HISTORY_MODE,
        RAGDOLL_MODE
    }

    private enum SliderId {
        FIELD_OF_VIEW,
        HIT_CHANCE,
        MIN_DAMAGE,
        LEGIT_SMOOTHING,
        LEGIT_REACTION,
        LEGIT_FOV,
        LEGIT_BURST,
        WEAR_OVERRIDE,
        ANIMATION_SPEED,
        PREVIEW_X,
        PREVIEW_Y
    }

    private enum ViewPopup {
        NONE,
        VIEW_OPTIONS,
        SCOPE_OPTIONS,
        VIEWMODEL_OPTIONS,
        PERSPECTIVE_OPTIONS
    }

    private enum ColorSlot {
        PRIMARY,
        WALLS,
        HISTORY,
        SOUL,
        GLOW
    }

    private enum ColorDrag {
        NONE,
        SQUARE,
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

    private enum TextInputContext {
        NONE,
        CONFIG_SEARCH,
        MENU_SEARCH
    }

    private enum DebugSection {
        TYPOGRAPHY,
        WINDOW,
        SIDEBAR,
        TOPBAR,
        LAYOUT,
        PANELS,
        BRANDING,
        FOOTER,
        CONTROLS
    }

    private static class DebugField {
        private final String label;
        private final String description;
        private final double step;
        private final double min;
        private final double max;
        private final DoubleSupplier getter;
        private final DoubleConsumer setter;

        private DebugField(final String label, final String description, final double step, final double min, final double max, final DoubleSupplier getter, final DoubleConsumer setter) {
            this.label = label;
            this.description = description;
            this.step = step;
            this.min = min;
            this.max = max;
            this.getter = getter;
            this.setter = setter;
        }

        private String display() {
            if (this.step >= 1.0D) {
                return Integer.toString((int) Math.round(this.getter.getAsDouble()));
            }
            return String.format(java.util.Locale.US, "%.2f", this.getter.getAsDouble());
        }
    }

    private static class FunctionPopupState {
        private final Module module;
        private final double anchorX;
        private final double anchorY;
        private final double x;
        private final double y;
        private final double width;
        private final double height;
        private final double contentHeight;
        private final double scrollMax;
        private double scrollOffset;

        private FunctionPopupState(final Module module, final double anchorX, final double anchorY, final double x, final double y,
                                   final double width, final double height, final double contentHeight, final double scrollMax) {
            this.module = module;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.contentHeight = contentHeight;
            this.scrollMax = scrollMax;
            this.scrollOffset = 0.0D;
        }
    }

    private static class SearchResult {
        private final Module module;
        private final Page page;
        private final VisualPage visualPage;
        private final String path;

        private SearchResult(final Module module, final Page page, final VisualPage visualPage, final String path) {
            this.module = module;
            this.page = page;
            this.visualPage = visualPage;
            this.path = path;
        }
    }

    private static class PopupSliderTrack {
        private final double x;
        private final double width;

        private PopupSliderTrack(final double x, final double width) {
            this.x = x;
            this.width = width;
        }
    }

    private static class SliderTrack {
        private final double x;
        private final double width;

        private SliderTrack(final double x, final double width) {
            this.x = x;
            this.width = width;
        }
    }

    private static class PopupModeDropdown {
        private final ModeSetting modeSetting;
        private final List<String> options;
        private final int selectedIndex;
        private final double x;
        private final double y;
        private final double width;
        private final double height;

        private PopupModeDropdown(final ModeSetting modeSetting, final List<String> options, final int selectedIndex,
                                  final double x, final double y, final double width, final double height) {
            this.modeSetting = modeSetting;
            this.options = options;
            this.selectedIndex = selectedIndex;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static class DebugOverlayFrame {
        private final double x;
        private final double y;
        private final double width;
        private final double height;

        private DebugOverlayFrame(final double x, final double y, final double width, final double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static class Layout {
        private final double x;
        private final double y;
        private final double width;
        private final double height;
        private final double originX;
        private final double originY;
        private final double scale;
        private final double sidebarWidth;
        private final double contentX;
        private final double contentY;
        private final double contentWidth;
        private final double columnGap;
        private final double columnWidth;

        private Layout(final int screenWidth, final int screenHeight, final MenuDebugProfile profile, final double menuScaleFactor) {
            this.width = profile.designWidth;
            this.height = profile.designHeight;
            this.scale = Math.min(Math.min((screenWidth - 56.0D) / this.width, (screenHeight - 56.0D) / this.height), profile.displayScaleCap) * menuScaleFactor;
            this.originX = (screenWidth - this.width * this.scale) / 2.0D + profile.menuOffsetX;
            this.originY = (screenHeight - this.height * this.scale) / 2.0D + profile.menuOffsetY;
            this.x = 0.0D;
            this.y = 0.0D;
            this.sidebarWidth = profile.sidebarWidth;
            this.contentX = this.x + this.sidebarWidth + profile.contentXOffset;
            this.contentY = this.y + profile.contentYOffset;
            this.contentWidth = this.width - this.sidebarWidth - profile.contentXOffset - profile.contentRightPadding;
            this.columnGap = profile.columnGap;
            this.columnWidth = (this.contentWidth - this.columnGap) / 2.0D;
        }
    }

    private static class ClickTarget {
        private final double x;
        private final double y;
        private final double width;
        private final double height;
        private final int button;
        private final Runnable action;

        private ClickTarget(final double x, final double y, final double width, final double height, final int button, final Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.button = button;
            this.action = action;
        }

        private boolean contains(final double mouseX, final double mouseY) {
            return MathUtil.within(mouseX, mouseY, this.x, this.y, this.width, this.height);
        }
    }

    private static class PickerColor {
        private float hue;
        private float saturation;
        private float brightness;
        private float alpha;

        private PickerColor(final float hue, final float saturation, final float brightness, final float alpha) {
            this.hue = hue;
            this.saturation = saturation;
            this.brightness = brightness;
            this.alpha = alpha;
        }

        private int color() {
            final int rgb = Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 0x00FFFFFF;
            return ((int) (this.alpha * 255.0F) << 24) | rgb;
        }

        private int opaque() {
            return 0xFF000000 | (Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 0x00FFFFFF);
        }
    }
}
