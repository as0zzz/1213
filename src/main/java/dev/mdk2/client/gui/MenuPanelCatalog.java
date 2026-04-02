package dev.mdk2.client.gui;

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
import dev.mdk2.client.modules.visual.ZoomModule;
import dev.mdk2.client.modules.xray.BlockEspModule;
import dev.mdk2.client.modules.xray.EntityEspModule;
import dev.mdk2.client.modules.xray.FreecamModule;
import dev.mdk2.client.modules.xray.RadarModule;
import dev.mdk2.client.modules.xray.SearchModule;
import dev.mdk2.client.modules.xray.StorageEspModule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class MenuPanelCatalog {
    private MenuPanelCatalog() {
    }

    @SafeVarargs
    private static List<Class<? extends Module>> list(final Class<? extends Module>... types) {
        return Arrays.asList(types);
    }

    static List<Class<? extends Module>> modulesFor(final String page, final String visualPage, final String title) {
        if ("RAGE".equals(page)) {
            if ("MAIN".equals(title)) return list(AuraModule.class, TriggerBotModule.class, CrystalAuraModule.class, TpAuraModule.class);
            if ("ASSIST".equals(title)) return list(CriticalsModule.class);
            if ("DEFENSE".equals(title)) return list(VelocityModule.class, ProtectModule.class, FightBotModule.class);
            if ("AUTO".equals(title)) return list(AutoSwordModule.class);
        } else if ("LEGIT".equals(page)) {
            if ("MAIN".equals(title)) return list(AimAssistModule.class, TriggerBotModule.class);
            if ("TRACK".equals(title)) return list(AutoArmorModule.class, AutoTotemModule.class);
            if ("SURVIVAL".equals(title)) return list(AutoPotionModule.class, AutoLeaveModule.class);
            if ("UTILITY".equals(title)) return list(AutoRespawnModule.class);
        } else if ("VISUALS".equals(page)) {
            if ("PLAYERS".equals(visualPage)) {
                if ("ENEMY".equals(title)) return list(EntityEspModule.class, HitChamsModule.class, TargetEspModule.class);
                if ("ENEMY MODEL".equals(title)) return list(RadarModule.class, FreecamModule.class, ViewModelModule.class);
                if ("EFFECTS".equals(title)) return list(ParticlesModule.class, ZoomModule.class);
            } else {
                if ("VIEW".equals(title)) return list(FullbrightModule.class, NoFogModule.class, NoWeatherModule.class, FakeWeatherModule.class);
                if ("WORLD ESP".equals(title)) return list(BlockEspModule.class, StorageEspModule.class, SearchModule.class, TrajectoryModule.class);
                if ("MISCELLANEOUS".equals(title)) return list(NoFireOverlayModule.class, HudModule.class, MenuParticlesModule.class, DeathCoordsModule.class, StatusEffectsModule.class);
            }
        } else if ("INVENTORY".equals(page)) {
            if ("MOTION".equals(title)) return list(FlightModule.class, SpeedModule.class, StepModule.class, HighJumpModule.class, NoSlowModule.class, NoFallModule.class);
            if ("POSITION".equals(title)) return list(SpiderModule.class, PhaseModule.class, SafeWalkModule.class, FastLadderModule.class, SprintAssistModule.class);
            if ("WORLD".equals(title)) return list(NukerModule.class, ScaffoldModule.class, FarmBotModule.class, AutoFishModule.class);
            if ("AQUA".equals(title)) return list(JesusModule.class, DolphinModule.class, BoatFlyModule.class, AutoMoveModule.class);
        } else if ("MISC".equals(page)) {
            if ("GENERAL".equals(title)) return list(DiscordRpcModule.class);
            if ("TOOLS".equals(title)) return list(AutoEatModule.class, AutoStealModule.class, AutoDropModule.class);
        }
        return Collections.emptyList();
    }
}
