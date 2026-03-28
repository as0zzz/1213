package dev.mdk2.client.util.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.client.CHeldItemChangePacket;

import java.util.function.Predicate;

public final class InventoryUtil {
    private InventoryUtil() {
    }

    public static boolean canManipulate(final PlayerEntity player) {
        return player != null && player.containerMenu != null && player.inventory.getCarried().isEmpty();
    }

    public static int findHotbarSlot(final PlayerEntity player, final Predicate<ItemStack> predicate) {
        for (int slot = 0; slot < 9; slot++) {
            final ItemStack stack = player.inventory.getItem(slot);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return slot;
            }
        }
        return -1;
    }

    public static int findInventorySlot(final PlayerEntity player, final Predicate<ItemStack> predicate) {
        for (int slot = 0; slot < 36; slot++) {
            final ItemStack stack = player.inventory.getItem(slot);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return slot;
            }
        }
        return -1;
    }

    public static int findBestSwordSlot(final PlayerEntity player, final boolean hotbarOnly) {
        final int end = hotbarOnly ? 9 : 36;
        double bestScore = 0.0D;
        int bestSlot = -1;
        for (int slot = 0; slot < end; slot++) {
            final ItemStack stack = player.inventory.getItem(slot);
            final double score = getSwordScore(stack);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    public static double getSwordScore(final ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof SwordItem)) {
            return 0.0D;
        }

        final SwordItem swordItem = (SwordItem) stack.getItem();
        double score = swordItem.getDamage();
        score += EnchantmentHelper.getDamageBonus(stack, CreatureAttribute.UNDEFINED);
        score += stack.getMaxDamage() > 0 ? (double) (stack.getMaxDamage() - stack.getDamageValue()) / (double) stack.getMaxDamage() : 0.0D;
        return score;
    }

    public static boolean selectHotbarSlot(final PlayerEntity player, final int slot) {
        if (slot < 0 || slot > 8 || player.inventory.selected == slot) {
            return slot >= 0 && slot <= 8;
        }

        player.inventory.selected = slot;
        if (player instanceof ClientPlayerEntity) {
            ((ClientPlayerEntity) player).connection.send(new CHeldItemChangePacket(slot));
        }
        return true;
    }

    public static int ensureHotbarItem(final PlayerEntity player, final Predicate<ItemStack> predicate, final int preferredHotbarSlot) {
        final int hotbarSlot = findHotbarSlot(player, predicate);
        if (hotbarSlot >= 0) {
            return hotbarSlot;
        }

        final int inventorySlot = findInventorySlot(player, predicate);
        if (inventorySlot < 9 || inventorySlot >= 36 || !canManipulate(player)) {
            return -1;
        }

        final int targetHotbarSlot = preferredHotbarSlot >= 0 && preferredHotbarSlot < 9 ? preferredHotbarSlot : player.inventory.selected;
        if (swapInventoryToHotbar(player, inventorySlot, targetHotbarSlot)) {
            return targetHotbarSlot;
        }
        return -1;
    }

    public static boolean moveToOffhand(final PlayerEntity player, final int inventorySlot) {
        return swapInventorySlots(player, inventorySlot, 40);
    }

    public static boolean swapInventoryToHotbar(final PlayerEntity player, final int inventorySlot, final int hotbarSlot) {
        if (!canManipulate(player) || inventorySlot < 0 || hotbarSlot < 0 || hotbarSlot > 8) {
            return false;
        }

        final int containerSlot = findContainerSlot(player, inventorySlot);
        if (containerSlot < 0) {
            return false;
        }

        clickSlot(player, containerSlot, hotbarSlot, ClickType.SWAP);
        return true;
    }

    public static boolean swapInventorySlots(final PlayerEntity player, final int firstInventorySlot, final int secondInventorySlot) {
        if (!canManipulate(player) || firstInventorySlot < 0 || secondInventorySlot < 0 || firstInventorySlot == secondInventorySlot) {
            return false;
        }

        final int firstContainerSlot = findContainerSlot(player, firstInventorySlot);
        final int secondContainerSlot = findContainerSlot(player, secondInventorySlot);
        if (firstContainerSlot < 0 || secondContainerSlot < 0) {
            return false;
        }

        clickSlot(player, firstContainerSlot, 0, ClickType.PICKUP);
        clickSlot(player, secondContainerSlot, 0, ClickType.PICKUP);
        clickSlot(player, firstContainerSlot, 0, ClickType.PICKUP);
        return true;
    }

    public static int findContainerSlot(final PlayerEntity player, final int inventorySlot) {
        if (player.containerMenu == null) {
            return -1;
        }

        for (int slot = 0; slot < player.containerMenu.slots.size(); slot++) {
            final Slot menuSlot = player.containerMenu.slots.get(slot);
            if (menuSlot.container == player.inventory && menuSlot.getSlotIndex() == inventorySlot) {
                return slot;
            }
        }
        return -1;
    }

    public static void clickSlot(final PlayerEntity player, final int containerSlot, final int mouseButton, final ClickType clickType) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode == null || player.containerMenu == null) {
            return;
        }

        minecraft.gameMode.handleInventoryMouseClick(player.containerMenu.containerId, containerSlot, mouseButton, clickType, player);
    }

    public static boolean hasShieldInOffhand(final PlayerEntity player) {
        return !player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem() instanceof ShieldItem;
    }

    public static boolean hasTotemInOffhand(final PlayerEntity player) {
        return player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING;
    }
}
