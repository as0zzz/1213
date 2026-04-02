package dev.mdk2.client.core;

import dev.mdk2.client.config.ConfigManager;
import dev.mdk2.client.discord.DiscordRpcService;
import dev.mdk2.client.gui.ModernClickGuiScreen;
import dev.mdk2.client.modules.combat.AuraModule;
import dev.mdk2.client.modules.combat.AimAssistModule;
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
import dev.mdk2.client.modules.movement.SprintAssistModule;
import dev.mdk2.client.modules.movement.SpiderModule;
import dev.mdk2.client.modules.movement.StepModule;
import dev.mdk2.client.modules.visual.FullbrightModule;
import dev.mdk2.client.modules.visual.DeathCoordsModule;
import dev.mdk2.client.modules.visual.FakeWeatherModule;
import dev.mdk2.client.modules.visual.HudModule;
import dev.mdk2.client.modules.visual.HitChamsModule;
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
import dev.mdk2.client.modules.xray.BlockEspModule;
import dev.mdk2.client.modules.xray.EntityEspModule;
import dev.mdk2.client.modules.xray.FreecamModule;
import dev.mdk2.client.modules.xray.RadarModule;
import dev.mdk2.client.modules.xray.SearchModule;
import dev.mdk2.client.modules.xray.StorageEspModule;
import dev.mdk2.client.render.HudRenderer;
import dev.mdk2.client.render.MenuParticleEngine;
import dev.mdk2.client.settings.BindSetting;
import dev.mdk2.client.util.AnimationUtil;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Path;

public class ClientRuntime {
    private static ClientRuntime instance;

    private final ThemeManager themeManager;
    private final ModuleManager moduleManager;
    private final ClientEventBus eventBus;
    private final MenuParticleEngine menuParticleEngine;
    private final HudRenderer hudRenderer;
    private final ConfigManager configManager;
    private final DiscordRpcService discordRpcService;

    private ClientRuntime() {
        this.themeManager = new ThemeManager();
        this.moduleManager = new ModuleManager();
        this.eventBus = new ClientEventBus(this.moduleManager);
        this.menuParticleEngine = new MenuParticleEngine();
        this.hudRenderer = new HudRenderer(this);
        this.discordRpcService = new DiscordRpcService();
        registerModules();
        this.configManager = new ConfigManager(configDirectory());
        bootstrapConfigs();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void bootstrap() {
        if (instance == null) {
            instance = new ClientRuntime();
        }
    }

    public static ClientRuntime getInstance() {
        return instance;
    }

    public ThemeManager getThemeManager() {
        return this.themeManager;
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public MenuParticleEngine getMenuParticleEngine() {
        return this.menuParticleEngine;
    }

    public HudRenderer getHudRenderer() {
        return this.hudRenderer;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public DiscordRpcService getDiscordRpcService() {
        return this.discordRpcService;
    }

    private void registerModules() {
        this.moduleManager.register(new HudModule());
        this.moduleManager.register(new WatermarkModule());
        this.moduleManager.register(new MenuParticlesModule());
        this.moduleManager.register(new ViewModelModule());
        this.moduleManager.register(new HitChamsModule());
        this.moduleManager.register(new FullbrightModule());
        this.moduleManager.register(new DeathCoordsModule());
        this.moduleManager.register(new StatusEffectsModule());
        this.moduleManager.register(new ParticlesModule());
        this.moduleManager.register(new TrajectoryModule());
        this.moduleManager.register(new TargetEspModule());
        this.moduleManager.register(new NoFogModule());
        this.moduleManager.register(new NoWeatherModule());
        this.moduleManager.register(new FakeWeatherModule());
        this.moduleManager.register(new NoFireOverlayModule());
        this.moduleManager.register(new ZoomModule());
        this.moduleManager.register(new EntityEspModule());
        this.moduleManager.register(new BlockEspModule());
        this.moduleManager.register(new StorageEspModule());
        this.moduleManager.register(new SearchModule());
        this.moduleManager.register(new RadarModule());
        this.moduleManager.register(new FreecamModule());
        this.moduleManager.register(new AuraModule());
        this.moduleManager.register(new TriggerBotModule());
        this.moduleManager.register(new AimAssistModule());
        this.moduleManager.register(new CriticalsModule());
        this.moduleManager.register(new VelocityModule());
        this.moduleManager.register(new AutoArmorModule());
        this.moduleManager.register(new AutoTotemModule());
        this.moduleManager.register(new AutoPotionModule());
        this.moduleManager.register(new AutoSwordModule());
        this.moduleManager.register(new CrystalAuraModule());
        this.moduleManager.register(new TpAuraModule());
        this.moduleManager.register(new FightBotModule());
        this.moduleManager.register(new ProtectModule());
        this.moduleManager.register(new AutoLeaveModule());
        this.moduleManager.register(new AutoRespawnModule());
        this.moduleManager.register(new FlightModule());
        this.moduleManager.register(new SpeedModule());
        this.moduleManager.register(new StepModule());
        this.moduleManager.register(new SpiderModule());
        this.moduleManager.register(new HighJumpModule());
        this.moduleManager.register(new NoFallModule());
        this.moduleManager.register(new NoSlowModule());
        this.moduleManager.register(new JesusModule());
        this.moduleManager.register(new DolphinModule());
        this.moduleManager.register(new AutoMoveModule());
        this.moduleManager.register(new PhaseModule());
        this.moduleManager.register(new NukerModule());
        this.moduleManager.register(new ScaffoldModule());
        this.moduleManager.register(new FarmBotModule());
        this.moduleManager.register(new AutoFishModule());
        this.moduleManager.register(new SafeWalkModule());
        this.moduleManager.register(new BoatFlyModule());
        this.moduleManager.register(new FastLadderModule());
        this.moduleManager.register(new SprintAssistModule());
        this.moduleManager.register(new AutoEatModule());
        this.moduleManager.register(new AutoStealModule());
        this.moduleManager.register(new AutoDropModule());
        this.moduleManager.register(new DiscordRpcModule());
        this.moduleManager.register(new ThemeModule());
    }

    private void bootstrapConfigs() {
        try {
            this.configManager.ensureDefaultConfig(this.moduleManager.getModules());
        } catch (final IOException ignored) {
        }
    }

    private Path configDirectory() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("mdk2").resolve("configs");
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        this.themeManager.tick();
        final ThemeModule themeModule = this.moduleManager.get(ThemeModule.class);
        if (themeModule != null) {
            this.themeManager.setStyle(themeModule.getThemeName());
            AnimationUtil.setAnimationSync(themeModule.getAnimationSync());
        }
        this.eventBus.postTick();
        this.menuParticleEngine.update(
            minecraft.getWindow().getGuiScaledWidth(),
            minecraft.getWindow().getGuiScaledHeight(),
            this.moduleManager.get(MenuParticlesModule.class),
            this.themeManager
        );
    }

    @SubscribeEvent
    public void onKeyInput(final InputEvent.KeyInputEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        if (event.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof ModernClickGuiScreen) {
                minecraft.setScreen(null);
            } else {
                minecraft.setScreen(new ModernClickGuiScreen(this));
            }
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            return;
        }

        handleBindPress(event.getKey());
    }

    @SubscribeEvent
    public void onMouseInput(final InputEvent.MouseInputEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            return;
        }

        handleBindPress(BindSetting.fromMouseButton(event.getButton()));
    }

    private void handleBindPress(final int inputCode) {
        for (final dev.mdk2.client.modules.Module module : this.moduleManager.getModules()) {
            if (!module.isToggleable()) {
                continue;
            }

            final BindSetting bind = module.getBind();
            if (bind != null && bind.getKey() != BindSetting.NONE && bind.getKey() == inputCode) {
                module.toggle();
                playBindToggleSound(module.isEnabled());
            }
        }
    }

    private void playBindToggleSound(final boolean enabled) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() == null) {
            return;
        }

        minecraft.getSoundManager().play(SimpleSound.forUI(
            SoundEvents.UI_BUTTON_CLICK,
            0.16F,
            enabled ? 1.18F : 0.92F
        ));
    }

    @SubscribeEvent
    public void onOverlayRender(final RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }

        this.eventBus.postRender2D(
            event.getMatrixStack(),
            event.getPartialTicks(),
            minecraft.getWindow().getGuiScaledWidth(),
            minecraft.getWindow().getGuiScaledHeight()
        );

        final HudModule hudModule = this.moduleManager.get(HudModule.class);
        if (hudModule != null && hudModule.isEnabled()) {
            this.hudRenderer.render(event.getMatrixStack(), event.getPartialTicks());
        }
    }

    @SubscribeEvent
    public void onWorldRender(final RenderWorldLastEvent event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        this.eventBus.postRender3D(event.getMatrixStack(), event.getPartialTicks());
    }

    @SubscribeEvent
    public void onAttackEntity(final AttackEntityEvent event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getPlayer() != minecraft.player) {
            return;
        }

        this.eventBus.postAttackEntity(event);
    }

    @SubscribeEvent
    public void onFogDensity(final EntityViewRenderEvent.FogDensity event) {
        final NoFogModule noFogModule = this.moduleManager.get(NoFogModule.class);
        if (noFogModule == null || !noFogModule.isEnabled()) {
            return;
        }

        event.setDensity(0.0F);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onFovUpdate(final FOVUpdateEvent event) {
        final ZoomModule zoomModule = this.moduleManager.get(ZoomModule.class);
        if (zoomModule == null || !zoomModule.isEnabled()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final double baseFov = Math.max(1.0D, minecraft.options.fov);
        final float zoomFactor = (float) MathHelper.clamp(zoomModule.getTargetFov() / baseFov, 0.08D, 1.0D);
        event.setNewfov(zoomFactor);
    }

    @SubscribeEvent
    public void onBlockOverlay(final RenderBlockOverlayEvent event) {
        if (event.getOverlayType() != RenderBlockOverlayEvent.OverlayType.FIRE) {
            return;
        }

        final NoFireOverlayModule noFireOverlayModule = this.moduleManager.get(NoFireOverlayModule.class);
        if (noFireOverlayModule != null && noFireOverlayModule.isEnabled()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onInputUpdate(final InputUpdateEvent event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getPlayer() != minecraft.player) {
            return;
        }

        this.eventBus.postInputUpdate(event);
    }

    @SubscribeEvent
    public void onRenderHand(final RenderHandEvent event) {
        final ViewModelModule viewModelModule = this.moduleManager.get(ViewModelModule.class);
        if (viewModelModule == null || !viewModelModule.isEnabled()) {
            return;
        }

        final float swing = (float) Math.max(0.1D, viewModelModule.getSwingScale());
        final boolean mainHand = event.getHand() == Hand.MAIN_HAND;
        final float direction = mainHand ? 1.0F : -1.0F;
        final String preset = viewModelModule.getPreset();
        double offsetX = viewModelModule.getOffsetX();
        double offsetY = viewModelModule.getOffsetY();
        double offsetZ = viewModelModule.getOffsetZ();
        double yaw = viewModelModule.getRotateYaw();
        double pitch = viewModelModule.getRotatePitch();
        double roll = viewModelModule.getRotateRoll();
        double scale = viewModelModule.getScaleValue();

        if ("Sharp".equalsIgnoreCase(preset)) {
            offsetX = 0.06D;
            offsetY = -0.02D;
            offsetZ = -0.05D;
            yaw = 10.0D;
            pitch = -8.0D;
            roll = 0.0D;
            scale = 1.06D;
        } else if ("Classic".equalsIgnoreCase(preset)) {
            offsetX = 0.02D;
            offsetY = 0.01D;
            offsetZ = 0.02D;
            yaw = 4.0D;
            pitch = -4.0D;
            roll = 0.0D;
            scale = 0.98D;
        } else if ("Minimal".equalsIgnoreCase(preset)) {
            offsetX = 0.01D;
            offsetY = 0.00D;
            offsetZ = 0.00D;
            yaw = 0.0D;
            pitch = 0.0D;
            roll = 0.0D;
            scale = 1.0D;
        } else if ("Cinematic".equalsIgnoreCase(preset)) {
            offsetX = 0.08D;
            offsetY = -0.03D;
            offsetZ = -0.08D;
            yaw = 14.0D;
            pitch = -10.0D;
            roll = 4.0D;
            scale = 1.04D;
        }

        event.getMatrixStack().translate(offsetX * direction, offsetY, offsetZ);
        event.getMatrixStack().mulPose(Vector3f.YP.rotationDegrees((float) yaw * direction));
        event.getMatrixStack().mulPose(Vector3f.XP.rotationDegrees((float) pitch * swing));
        event.getMatrixStack().mulPose(Vector3f.ZP.rotationDegrees((float) roll * direction));
        event.getMatrixStack().scale((float) scale, (float) scale, (float) scale);
    }
}
