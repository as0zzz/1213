package dev.mdk2.client.modules.combat;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.InventoryUtil;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class CriticalsModule extends Module {
    private final ModeSetting mode;
    private final BooleanSetting onlyGround;
    private final BooleanSetting weaponOnly;
    private final NumberSetting delay;
    private long lastCriticalTime;

    public CriticalsModule() {
        super("Criticals", "Forces critical hits with packet or mini-jump timing.", Category.COMBAT);
        this.mode = register(new ModeSetting("Mode", "Packet", "Packet", "Mini Jump", "Jump"));
        this.onlyGround = register(new BooleanSetting("Only Ground", true));
        this.weaponOnly = register(new BooleanSetting("Weapon Only", true));
        this.delay = register(new NumberSetting("Delay", 180.0D, 0.0D, 750.0D, 5.0D));
    }

    @Override
    public void onAttackEntity(final AttackEntityEvent event) {
        if (event.getTarget() instanceof LivingEntity) {
            tryCritical((LivingEntity) event.getTarget());
        }
    }

    public boolean tryCritical(final LivingEntity target) {
        final Minecraft minecraft = Minecraft.getInstance();
        final PlayerEntity player = minecraft.player;
        if (player == null || !(player instanceof ClientPlayerEntity) || target == null || !target.isAlive()) {
            return false;
        }

        if (this.weaponOnly.getValue().booleanValue() && !isWeapon(player.getMainHandItem())) {
            return false;
        }

        if (!canCritical(player) || !TimeUtil.passed(this.lastCriticalTime, this.delay.getValue().doubleValue())) {
            return false;
        }

        this.lastCriticalTime = TimeUtil.now();
        final ClientPlayerEntity clientPlayer = (ClientPlayerEntity) player;
        if ("Jump".equalsIgnoreCase(this.mode.getValue())) {
            player.jumpFromGround();
            return true;
        }

        if ("Mini Jump".equalsIgnoreCase(this.mode.getValue())) {
            player.push(0.0D, 0.10D, 0.0D);
            player.setOnGround(false);
            player.fallDistance = 0.08F;
            return true;
        }

        sendPacketCritical(clientPlayer);
        player.setOnGround(false);
        player.fallDistance = 0.1F;
        return true;
    }

    private boolean canCritical(final PlayerEntity player) {
        if (this.onlyGround.getValue().booleanValue() && !player.isOnGround()) {
            return false;
        }

        return !player.isPassenger()
            && !player.isInWater()
            && !player.isInLava()
            && !player.onClimbable()
            && !player.abilities.flying;
    }

    private void sendPacketCritical(final ClientPlayerEntity player) {
        final double x = player.getX();
        final double y = player.getY();
        final double z = player.getZ();
        player.connection.send(new CPlayerPacket.PositionPacket(x, y + 0.0625D, z, true));
        player.connection.send(new CPlayerPacket.PositionPacket(x, y, z, false));
        player.connection.send(new CPlayerPacket.PositionPacket(x, y + 1.1E-5D, z, false));
        player.connection.send(new CPlayerPacket.PositionPacket(x, y, z, false));
    }

    private boolean isWeapon(final ItemStack stack) {
        return !stack.isEmpty()
            && (stack.getItem() instanceof SwordItem
            || stack.getItem() instanceof AxeItem
            || stack.getItem() instanceof TridentItem
            || InventoryUtil.getSwordScore(stack) > 0.0D);
    }
}
