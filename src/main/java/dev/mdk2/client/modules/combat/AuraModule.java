package dev.mdk2.client.modules.combat;

import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.CombatUtil;
import dev.mdk2.client.util.combat.RotationUtil;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;

import java.util.List;

public class AuraModule extends Module {
    private final ModeSetting mode;
    private final NumberSetting range;
    private final NumberSetting wallRange;
    private final NumberSetting blockRange;
    private final NumberSetting fov;
    private final NumberSetting aps;
    private final NumberSetting cooldown;
    private final NumberSetting maxTargets;
    private final NumberSetting switchDelay;
    private final BooleanSetting players;
    private final BooleanSetting mobs;
    private final BooleanSetting animals;
    private final BooleanSetting invisibles;
    private final BooleanSetting ignoreTeams;
    private final BooleanSetting autoBlock;
    private final BooleanSetting keepSprint;
    private final BooleanSetting swing;
    private final BooleanSetting useCooldown;
    private final BooleanSetting targetLock;
    private final ModeSetting priority;
    private final ModeSetting rotation;
    private LivingEntity renderTarget;
    private long lastAttackTime;
    private long lastSwitchTime;
    private boolean lastClickState;

    public AuraModule() {
        super("Aura", "KillAura, ClickAura and MultiAura in one configurable module.", Category.COMBAT);
        this.mode = register(new ModeSetting("Mode", "KillAura", "KillAura", "ClickAura", "MultiAura"));
        this.range = register(new NumberSetting("Range", 4.4D, 2.0D, 7.0D, 0.1D));
        this.wallRange = register(new NumberSetting("Wall Range", 3.2D, 0.0D, 7.0D, 0.1D));
        this.blockRange = register(new NumberSetting("Block Range", 4.8D, 0.0D, 7.0D, 0.1D));
        this.fov = register(new NumberSetting("FOV", 180.0D, 20.0D, 360.0D, 1.0D));
        this.aps = register(new NumberSetting("APS", 9.0D, 1.0D, 20.0D, 0.5D));
        this.cooldown = register(new NumberSetting("Cooldown", 0.92D, 0.10D, 1.0D, 0.01D));
        this.maxTargets = register(new NumberSetting("Max Targets", 3.0D, 1.0D, 8.0D, 1.0D));
        this.switchDelay = register(new NumberSetting("Switch Delay", 150.0D, 0.0D, 1000.0D, 10.0D));
        this.players = register(new BooleanSetting("Players", true));
        this.mobs = register(new BooleanSetting("Mobs", true));
        this.animals = register(new BooleanSetting("Animals", false));
        this.invisibles = register(new BooleanSetting("Invisibles", false));
        this.ignoreTeams = register(new BooleanSetting("Ignore Teams", true));
        this.autoBlock = register(new BooleanSetting("Auto Block", true));
        this.keepSprint = register(new BooleanSetting("Keep Sprint", true));
        this.swing = register(new BooleanSetting("Swing", true));
        this.useCooldown = register(new BooleanSetting("Use Cooldown", true));
        this.targetLock = register(new BooleanSetting("Target Lock", true));
        this.priority = register(new ModeSetting("Priority", "Distance", "Distance", "Health", "Angle"));
        this.rotation = register(new ModeSetting("Rotation", "Normal", "Off", "Normal", "Snap"));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            this.renderTarget = null;
            releaseBlock();
            return;
        }

        final List<LivingEntity> targets = CombatUtil.collectTargets(
            minecraft.player,
            this.range.getValue().doubleValue(),
            this.wallRange.getValue().doubleValue(),
            this.fov.getValue().doubleValue(),
            this.players.getValue().booleanValue(),
            this.mobs.getValue().booleanValue(),
            this.animals.getValue().booleanValue(),
            this.invisibles.getValue().booleanValue(),
            this.ignoreTeams.getValue().booleanValue(),
            this.priority.getValue()
        );
        if (targets.isEmpty()) {
            this.renderTarget = null;
            releaseBlock();
            return;
        }

        this.renderTarget = selectTarget(minecraft, targets);
        if (this.renderTarget == null) {
            releaseBlock();
            return;
        }

        if ("ClickAura".equalsIgnoreCase(this.mode.getValue()) && !pollClickState(minecraft.options.keyAttack.isDown())) {
            updateBlocking(minecraft);
            return;
        }

        final boolean readyToAttack = TimeUtil.passed(this.lastAttackTime, 1000.0D / this.aps.getValue().doubleValue())
            && CombatUtil.canAttack(minecraft.player, this.useCooldown.getValue().booleanValue(), this.cooldown.getValue().doubleValue());
        if (!readyToAttack) {
            updateBlocking(minecraft);
            return;
        }

        if (!"Off".equalsIgnoreCase(this.rotation.getValue())) {
            RotationUtil.face(minecraft.player, this.renderTarget, 20.0F, 16.0F, "Snap".equalsIgnoreCase(this.rotation.getValue()));
        }

        final AutoSwordModule autoSwordModule = ClientRuntime.getInstance().getModuleManager().get(AutoSwordModule.class);
        if (autoSwordModule != null && autoSwordModule.isEnabled()) {
            autoSwordModule.equipBestSword();
        }

        releaseBlock();

        final CriticalsModule criticalsModule = ClientRuntime.getInstance().getModuleManager().get(CriticalsModule.class);
        final List<LivingEntity> attackTargets = collectAttackTargets(minecraft, targets);
        for (final LivingEntity target : attackTargets) {
            if (!CombatUtil.isValidTarget(
                minecraft.player,
                target,
                this.range.getValue().doubleValue(),
                this.wallRange.getValue().doubleValue(),
                this.fov.getValue().doubleValue(),
                this.players.getValue().booleanValue(),
                this.mobs.getValue().booleanValue(),
                this.animals.getValue().booleanValue(),
                this.invisibles.getValue().booleanValue(),
                this.ignoreTeams.getValue().booleanValue()
            )) {
                continue;
            }

            if (criticalsModule != null && criticalsModule.isEnabled()) {
                criticalsModule.tryCritical(target);
            }
            CombatUtil.attack(target, this.swing.getValue().booleanValue(), this.keepSprint.getValue().booleanValue());
            this.lastAttackTime = TimeUtil.now();
            if (this.useCooldown.getValue().booleanValue()) {
                break;
            }
        }

        updateBlocking(minecraft);
    }

    @Override
    public void onDisable() {
        this.renderTarget = null;
        this.lastClickState = false;
        this.lastSwitchTime = 0L;
        releaseBlock();
    }

    public LivingEntity getRenderTarget() {
        return this.renderTarget;
    }

    private LivingEntity selectTarget(final Minecraft minecraft, final List<LivingEntity> targets) {
        if (targets.isEmpty()) {
            return null;
        }

        if (this.renderTarget != null && targets.contains(this.renderTarget)) {
            if (this.targetLock.getValue().booleanValue()) {
                return this.renderTarget;
            }

            final LivingEntity bestTarget = targets.get(0);
            if (bestTarget != this.renderTarget && !TimeUtil.passed(this.lastSwitchTime, this.switchDelay.getValue().doubleValue())) {
                return this.renderTarget;
            }
        }

        final LivingEntity selected = targets.get(0);
        if (selected != this.renderTarget) {
            this.lastSwitchTime = TimeUtil.now();
        }
        return selected;
    }

    private boolean pollClickState(final boolean currentState) {
        final boolean clicked = currentState && !this.lastClickState;
        this.lastClickState = currentState;
        return clicked;
    }

    private List<LivingEntity> collectAttackTargets(final Minecraft minecraft, final List<LivingEntity> targets) {
        final int attackCount = "MultiAura".equalsIgnoreCase(this.mode.getValue())
            ? Math.min(targets.size(), this.maxTargets.getValue().intValue())
            : 1;
        final List<LivingEntity> attackTargets = new java.util.ArrayList<LivingEntity>(attackCount);
        if (this.renderTarget != null && targets.contains(this.renderTarget)) {
            attackTargets.add(this.renderTarget);
        }

        for (final LivingEntity candidate : targets) {
            if (attackTargets.size() >= attackCount) {
                break;
            }
            if (candidate == this.renderTarget) {
                continue;
            }
            if (!CombatUtil.isValidTarget(
                minecraft.player,
                candidate,
                this.range.getValue().doubleValue(),
                this.wallRange.getValue().doubleValue(),
                this.fov.getValue().doubleValue(),
                this.players.getValue().booleanValue(),
                this.mobs.getValue().booleanValue(),
                this.animals.getValue().booleanValue(),
                this.invisibles.getValue().booleanValue(),
                this.ignoreTeams.getValue().booleanValue()
            )) {
                continue;
            }
            attackTargets.add(candidate);
        }
        return attackTargets;
    }

    private void updateBlocking(final Minecraft minecraft) {
        if (!shouldBlock(minecraft)) {
            releaseBlock();
            return;
        }

        if (!minecraft.player.isUsingItem()) {
            CombatUtil.useItem(Hand.OFF_HAND);
        }
    }

    private boolean shouldBlock(final Minecraft minecraft) {
        if (!this.autoBlock.getValue().booleanValue() || minecraft.player == null || minecraft.gameMode == null || this.renderTarget == null) {
            return false;
        }

        if (minecraft.player.distanceToSqr(this.renderTarget) > this.blockRange.getValue().doubleValue() * this.blockRange.getValue().doubleValue()) {
            return false;
        }

        if (minecraft.player.getOffhandItem().isEmpty() || !(minecraft.player.getOffhandItem().getItem() instanceof ShieldItem)) {
            return false;
        }

        return !minecraft.player.isUsingItem() || minecraft.player.getUsedItemHand() == Hand.OFF_HAND;
    }

    private void releaseBlock() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gameMode == null) {
            return;
        }

        if (minecraft.player.isUsingItem()
            && minecraft.player.getUsedItemHand() == Hand.OFF_HAND
            && minecraft.player.getOffhandItem().getItem() instanceof ShieldItem) {
            minecraft.gameMode.releaseUsingItem(minecraft.player);
        }
    }
}
