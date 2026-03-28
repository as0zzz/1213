package dev.mdk2.client.modules.combat;

import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.CombatUtil;
import dev.mdk2.client.util.combat.InventoryUtil;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;

public class TriggerBotModule extends Module {
    private final BooleanSetting players;
    private final BooleanSetting mobs;
    private final BooleanSetting animals;
    private final BooleanSetting invisibles;
    private final BooleanSetting ignoreTeams;
    private final BooleanSetting autoSword;
    private final BooleanSetting useCooldown;
    private final NumberSetting range;
    private final NumberSetting cooldown;
    private final NumberSetting aps;
    private long lastAttackTime;

    public TriggerBotModule() {
        super("TriggerBot", "Attacks entities when your crosshair is over them.", Category.COMBAT);
        this.players = register(new BooleanSetting("Players", true));
        this.mobs = register(new BooleanSetting("Mobs", true));
        this.animals = register(new BooleanSetting("Animals", false));
        this.invisibles = register(new BooleanSetting("Invisibles", false));
        this.ignoreTeams = register(new BooleanSetting("Ignore Teams", true));
        this.autoSword = register(new BooleanSetting("Auto Sword", true));
        this.useCooldown = register(new BooleanSetting("Use Cooldown", true));
        this.range = register(new NumberSetting("Range", 4.4D, 2.0D, 6.5D, 0.1D));
        this.cooldown = register(new NumberSetting("Cooldown", 0.92D, 0.10D, 1.0D, 0.01D));
        this.aps = register(new NumberSetting("APS", 8.0D, 1.0D, 20.0D, 0.5D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.crosshairPickEntity instanceof LivingEntity) || minecraft.player == null || minecraft.level == null) {
            return;
        }

        final LivingEntity target = (LivingEntity) minecraft.crosshairPickEntity;
        if (!CombatUtil.isValidTarget(minecraft.player, target, this.range.getValue().doubleValue(), this.range.getValue().doubleValue() * 0.7D, 360.0D,
            this.players.getValue().booleanValue(), this.mobs.getValue().booleanValue(), this.animals.getValue().booleanValue(),
            this.invisibles.getValue().booleanValue(), this.ignoreTeams.getValue().booleanValue())) {
            return;
        }

        if (!CombatUtil.canAttack(minecraft.player, this.useCooldown.getValue().booleanValue(), this.cooldown.getValue().doubleValue())
            || !TimeUtil.passed(this.lastAttackTime, 1000.0D / this.aps.getValue().doubleValue())) {
            return;
        }

        if (this.autoSword.getValue().booleanValue()) {
            final AutoSwordModule autoSwordModule = ClientRuntime.getInstance().getModuleManager().get(AutoSwordModule.class);
            if (autoSwordModule != null && autoSwordModule.isEnabled()) {
                autoSwordModule.equipBestSword();
            }
        }

        final CriticalsModule criticalsModule = ClientRuntime.getInstance().getModuleManager().get(CriticalsModule.class);
        if (criticalsModule != null && criticalsModule.isEnabled()) {
            criticalsModule.tryCritical(target);
        }

        CombatUtil.attack(target, true, true);
        this.lastAttackTime = TimeUtil.now();
    }
}
