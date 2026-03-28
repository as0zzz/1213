package dev.mdk2.client.modules.combat;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class AutoLeaveModule extends Module {
    private final BooleanSetting playerCheck;
    private final BooleanSetting crystalCheck;
    private final BooleanSetting multiplayerOnly;
    private final NumberSetting health;
    private final NumberSetting playerRange;
    private final NumberSetting crystalRange;

    public AutoLeaveModule() {
        super("Auto Leave", "Disconnects when health is low or danger gets too close.", Category.COMBAT);
        this.playerCheck = register(new BooleanSetting("Players", true));
        this.crystalCheck = register(new BooleanSetting("Crystals", true));
        this.multiplayerOnly = register(new BooleanSetting("Multiplayer Only", true));
        this.health = register(new NumberSetting("Health", 6.0D, 1.0D, 36.0D, 0.5D));
        this.playerRange = register(new NumberSetting("Player Range", 8.0D, 1.0D, 24.0D, 0.1D));
        this.crystalRange = register(new NumberSetting("Crystal Range", 6.0D, 1.0D, 20.0D, 0.1D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        if (this.multiplayerOnly.getValue().booleanValue() && minecraft.isLocalServer()) {
            return;
        }

        String reason = null;
        if (minecraft.player.getHealth() + minecraft.player.getAbsorptionAmount() <= this.health.getValue().doubleValue()) {
            reason = "Low health";
        } else if (this.crystalCheck.getValue().booleanValue() && hasCrystal(minecraft)) {
            reason = "Nearby crystal";
        } else if (this.playerCheck.getValue().booleanValue() && hasPlayer(minecraft)) {
            reason = "Nearby player";
        }

        if (reason == null) {
            return;
        }

        minecraft.level.disconnect();
        minecraft.clearLevel(new DisconnectedScreen(new MultiplayerScreen(new MainMenuScreen()), new StringTextComponent("Auto Leave"), new StringTextComponent(reason)));
        setEnabled(false);
    }

    private boolean hasCrystal(final Minecraft minecraft) {
        final double rangeSq = this.crystalRange.getValue().doubleValue() * this.crystalRange.getValue().doubleValue();
        for (final Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof EnderCrystalEntity && entity.isAlive() && minecraft.player.distanceToSqr(entity) <= rangeSq) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPlayer(final Minecraft minecraft) {
        final double rangeSq = this.playerRange.getValue().doubleValue() * this.playerRange.getValue().doubleValue();
        for (final Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof PlayerEntity && entity != minecraft.player && entity.isAlive() && minecraft.player.distanceToSqr(entity) <= rangeSq) {
                return true;
            }
        }
        return false;
    }
}
