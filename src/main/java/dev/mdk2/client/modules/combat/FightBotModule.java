package dev.mdk2.client.modules.combat;

import dev.mdk2.client.core.ClientRuntime;
import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.CombatUtil;
import dev.mdk2.client.util.combat.RotationUtil;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.client.event.InputUpdateEvent;

public class FightBotModule extends Module {
    private final BooleanSetting players;
    private final BooleanSetting mobs;
    private final BooleanSetting animals;
    private final BooleanSetting strafe;
    private final BooleanSetting sprint;
    private final BooleanSetting jump;
    private final NumberSetting targetRange;
    private final NumberSetting attackRange;
    private final NumberSetting cooldown;
    private LivingEntity currentTarget;
    private long lastAttackTime;

    public FightBotModule() {
        super("Fight Bot", "Chases targets, rotates automatically and handles attacks.", Category.COMBAT);
        this.players = register(new BooleanSetting("Players", true));
        this.mobs = register(new BooleanSetting("Mobs", true));
        this.animals = register(new BooleanSetting("Animals", false));
        this.strafe = register(new BooleanSetting("Strafe", true));
        this.sprint = register(new BooleanSetting("Sprint", true));
        this.jump = register(new BooleanSetting("Jump", true));
        this.targetRange = register(new NumberSetting("Target Range", 10.0D, 2.0D, 24.0D, 0.1D));
        this.attackRange = register(new NumberSetting("Attack Range", 3.1D, 1.5D, 6.0D, 0.1D));
        this.cooldown = register(new NumberSetting("Cooldown", 0.90D, 0.10D, 1.0D, 0.01D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            this.currentTarget = null;
            return;
        }

        this.currentTarget = CombatUtil.findBestTarget(minecraft.player, this.targetRange.getValue().doubleValue(),
            this.targetRange.getValue().doubleValue() * 0.7D, 360.0D, this.players.getValue().booleanValue(),
            this.mobs.getValue().booleanValue(), this.animals.getValue().booleanValue(), false, true, "Distance");
        if (this.currentTarget == null) {
            return;
        }

        RotationUtil.face(minecraft.player, this.currentTarget, 16.0F, 14.0F, false);
        if (this.sprint.getValue().booleanValue()) {
            minecraft.player.setSprinting(true);
        }

        if (minecraft.player.distanceTo(this.currentTarget) <= this.attackRange.getValue().doubleValue()
            && CombatUtil.canAttack(minecraft.player, true, this.cooldown.getValue().doubleValue())
            && TimeUtil.passed(this.lastAttackTime, 100.0D)) {
            final AutoSwordModule autoSwordModule = ClientRuntime.getInstance().getModuleManager().get(AutoSwordModule.class);
            if (autoSwordModule != null && autoSwordModule.isEnabled()) {
                autoSwordModule.equipBestSword();
            }

            final CriticalsModule criticalsModule = ClientRuntime.getInstance().getModuleManager().get(CriticalsModule.class);
            if (criticalsModule != null && criticalsModule.isEnabled()) {
                criticalsModule.tryCritical(this.currentTarget);
            }

            CombatUtil.attack(this.currentTarget, true, true);
            this.lastAttackTime = TimeUtil.now();
        }
    }

    @Override
    public void onInputUpdate(final InputUpdateEvent event) {
        if (this.currentTarget == null || event.getPlayer() == null || event.getPlayer() != Minecraft.getInstance().player) {
            return;
        }

        final double distance = event.getPlayer().distanceTo(this.currentTarget);
        if (distance > this.attackRange.getValue().doubleValue() + 0.3D) {
            event.getMovementInput().forwardImpulse = 1.0F;
            event.getMovementInput().up = true;
        } else if (distance < this.attackRange.getValue().doubleValue() * 0.75D) {
            event.getMovementInput().forwardImpulse = -0.45F;
            event.getMovementInput().down = true;
        }

        if (this.strafe.getValue().booleanValue()) {
            final float strafeDirection = (Minecraft.getInstance().player.tickCount / 18) % 2 == 0 ? 0.65F : -0.65F;
            event.getMovementInput().leftImpulse = strafeDirection;
            event.getMovementInput().left = strafeDirection > 0.0F;
            event.getMovementInput().right = strafeDirection < 0.0F;
        }

        if (this.jump.getValue().booleanValue() && event.getPlayer().horizontalCollision) {
            event.getMovementInput().jumping = true;
        }
    }
}
