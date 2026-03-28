package dev.mdk2.client.modules.combat;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoRespawnModule extends Module {
    private final NumberSetting delay;
    private long deathTime;

    public AutoRespawnModule() {
        super("Auto Respawn", "Respawns automatically after death.", Category.COMBAT);
        this.delay = register(new NumberSetting("Delay", 250.0D, 0.0D, 5000.0D, 10.0D));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        if (minecraft.screen instanceof DeathScreen) {
            if (this.deathTime == 0L) {
                this.deathTime = TimeUtil.now();
            }
            if (TimeUtil.passed(this.deathTime, this.delay.getValue().doubleValue())) {
                minecraft.player.respawn();
                minecraft.setScreen(null);
                this.deathTime = 0L;
            }
            return;
        }

        this.deathTime = 0L;
    }
}
