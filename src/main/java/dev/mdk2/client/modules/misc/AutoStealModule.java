package dev.mdk2.client.modules.misc;

import dev.mdk2.client.modules.Category;
import dev.mdk2.client.modules.Module;
import dev.mdk2.client.settings.BooleanSetting;
import dev.mdk2.client.settings.NumberSetting;
import dev.mdk2.client.util.combat.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class AutoStealModule extends Module {
    private final NumberSetting delay;
    private final BooleanSetting closeOnEmpty;
    private long lastStealTime;

    public AutoStealModule() {
        super("Auto Steal", "Quick moves loot from open containers.", Category.MISC);
        this.delay = register(new NumberSetting("Delay", 80.0D, 0.0D, 500.0D, 5.0D));
        this.closeOnEmpty = register(new BooleanSetting("Close On Empty", true));
    }

    @Override
    public void onTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gameMode == null || minecraft.player.containerMenu == null
            || minecraft.player.containerMenu == minecraft.player.inventoryMenu
            || !TimeUtil.passed(this.lastStealTime, this.delay.getValue().doubleValue())) {
            return;
        }

        final int containerSlots = Math.max(0, minecraft.player.containerMenu.slots.size() - 36);
        for (int slotIndex = 0; slotIndex < containerSlots; slotIndex++) {
            final Slot slot = minecraft.player.containerMenu.slots.get(slotIndex);
            final ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }

            minecraft.gameMode.handleInventoryMouseClick(
                minecraft.player.containerMenu.containerId,
                slotIndex,
                0,
                ClickType.QUICK_MOVE,
                minecraft.player
            );
            this.lastStealTime = TimeUtil.now();
            return;
        }

        if (this.closeOnEmpty.getValue().booleanValue() && minecraft.player.containerMenu.slots.size() > 0) {
            minecraft.player.closeContainer();
        }
    }
}
