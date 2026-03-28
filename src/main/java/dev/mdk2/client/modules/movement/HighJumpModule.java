package dev.mdk2.client.modules.movement;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.ModeSetting;
import dev.mdk2.client.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector3d;

public class HighJumpModule extends Module {
    private final ModeSetting mode;
    private final NumberSetting height;
    private final BooleanSetting autoDisable;
    private boolean armed;

    public HighJumpModule() {
        super("High Jump", "Increases jump height on demand.", Category.MOVEMENT);
        this.mode = register(new ModeSetting("Mode", "Vanilla", "Vanilla", "Burst"));
        this.height = register(new NumberSetting("Height", 0.72D, 0.42D, 2.0D, 0.02D));
        this.autoDisable = register(new BooleanSetting("Auto Disable", false));
    }

    @Override
    public void onDisable() {
        this.armed = false;
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        final boolean jumpDown = minecraft.options.keyJump.isDown();
        if ("Burst".equalsIgnoreCase(this.mode.getValue())) {
            if (jumpDown && !this.armed && minecraft.player.isOnGround()) {
                final Vector3d motion = minecraft.player.getDeltaMovement();
                minecraft.player.setDeltaMovement(motion.x, this.height.getValue().doubleValue(), motion.z);
                this.armed = true;
                if (this.autoDisable.getValue().booleanValue()) {
                    setEnabled(false);
                }
            }
            if (!jumpDown) {
                this.armed = false;
            }
            return;
        }

        if (jumpDown && minecraft.player.isOnGround()) {
            final Vector3d motion = minecraft.player.getDeltaMovement();
            minecraft.player.setDeltaMovement(motion.x, this.height.getValue().doubleValue(), motion.z);
        }
    }
}
