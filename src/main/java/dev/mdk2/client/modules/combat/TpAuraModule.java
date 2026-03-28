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
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector3d;

public class TpAuraModule extends Module {
    private final BooleanSetting players;
    private final BooleanSetting mobs;
    private final BooleanSetting animals;
    private final BooleanSetting rotate;
    private final BooleanSetting useCooldown;
    private final NumberSetting range;
    private final NumberSetting delay;
    private final NumberSetting packets;
    private final NumberSetting cooldown;
    private long lastTeleportAttackTime;

    public TpAuraModule() {
        super("TP-Aura", "Sends packet teleports to targets before attacking.", Category.COMBAT);
        this.players = register(new BooleanSetting("Players", true));
        this.mobs = register(new BooleanSetting("Mobs", true));
        this.animals = register(new BooleanSetting("Animals", false));
        this.rotate = register(new BooleanSetting("Rotate", true));
        this.useCooldown = register(new BooleanSetting("Use Cooldown", true));
        this.range = register(new NumberSetting("Range", 16.0D, 3.0D, 64.0D, 0.5D));
        this.delay = register(new NumberSetting("Delay", 350.0D, 0.0D, 3000.0D, 10.0D));
        this.packets = register(new NumberSetting("Packets", 8.0D, 2.0D, 24.0D, 1.0D));
        this.cooldown = register(new NumberSetting("Cooldown", 0.92D, 0.10D, 1.0D, 0.01D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.player instanceof ClientPlayerEntity) || minecraft.level == null
            || !TimeUtil.passed(this.lastTeleportAttackTime, this.delay.getValue().doubleValue())) {
            return;
        }

        final LivingEntity target = CombatUtil.findBestTarget(minecraft.player, this.range.getValue().doubleValue(),
            this.range.getValue().doubleValue(), 360.0D, this.players.getValue().booleanValue(),
            this.mobs.getValue().booleanValue(), this.animals.getValue().booleanValue(), false, true, "Distance");
        if (target == null || !CombatUtil.canAttack(minecraft.player, this.useCooldown.getValue().booleanValue(), this.cooldown.getValue().doubleValue())) {
            return;
        }

        final AutoSwordModule autoSwordModule = ClientRuntime.getInstance().getModuleManager().get(AutoSwordModule.class);
        if (autoSwordModule != null && autoSwordModule.isEnabled()) {
            autoSwordModule.equipBestSword();
        }

        if (this.rotate.getValue().booleanValue()) {
            RotationUtil.face(minecraft.player, target, 180.0F, 180.0F, true);
        }

        final Vector3d start = minecraft.player.position();
        final Vector3d end = target.position().add(0.0D, 0.01D, 0.0D);
        final int steps = this.packets.getValue().intValue();
        final ClientPlayerEntity player = (ClientPlayerEntity) minecraft.player;
        for (int step = 1; step <= steps; step++) {
            final Vector3d point = interpolate(start, end, (double) step / (double) steps);
            player.connection.send(new CPlayerPacket.PositionPacket(point.x, point.y, point.z, false));
        }

        final CriticalsModule criticalsModule = ClientRuntime.getInstance().getModuleManager().get(CriticalsModule.class);
        if (criticalsModule != null && criticalsModule.isEnabled()) {
            criticalsModule.tryCritical(target);
        }

        CombatUtil.attack(target, true, true);

        for (int step = steps - 1; step >= 0; step--) {
            final Vector3d point = interpolate(start, end, (double) step / (double) steps);
            player.connection.send(new CPlayerPacket.PositionPacket(point.x, point.y, point.z, step == 0 && minecraft.player.isOnGround()));
        }
        this.lastTeleportAttackTime = TimeUtil.now();
    }

    private Vector3d interpolate(final Vector3d start, final Vector3d end, final double progress) {
        return start.add(end.subtract(start).scale(progress));
    }
}
