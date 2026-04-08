package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.features.ItemDropChecker;
import com.fix3dll.skyblockaddons.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class LocalPlayerHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    public static boolean dropOneItemConfirmation() {
        if (MC.player == null) return true;

        ItemStack heldItemStack = MC.player.getMainHandItem();
        boolean isSkyblock = main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer();

        if (isSkyblock && Feature.LOCK_SLOTS.isEnabled() && !main.getUtils().isInDungeon()) {
            int slot = MC.player.getInventory().getSelectedSlot() + 36;
            if (main.getPersistentValuesManager().getLockedSlots().contains(slot)
                    && (slot >= 9 || MC.player.containerMenu instanceof InventoryMenu && slot >= 5)) {
                main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
                Utils.sendMessage(Component
                        .literal(Translations.getMessage("messages.slotLocked"))
                        .withColor(Feature.DROP_CONFIRMATION.getColor())
                );
                return true;
            }

            if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
                Utils.sendMessage(Component
                        .literal(Translations.getMessage("messages.switchedSlots"))
                        .withColor(Feature.DROP_CONFIRMATION.getColor())
                );
                return true;
            }
        }

        if (heldItemStack != ItemStack.EMPTY) {
            if (!ItemDropChecker.canDropItem(heldItemStack, true)) {
                Utils.sendMessage(Component
                        .literal(Translations.getMessage("messages.cancelledDropping"))
                        .withColor(Feature.STOP_DROPPING_SELLING_RARE_ITEMS.getColor())
                );
                return true;
            }

            if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
                Utils.sendMessage(Component
                        .literal(Translations.getMessage("messages.switchedSlots"))
                        .withColor(Feature.DROP_CONFIRMATION.getColor())
                );
                return true;
            }
        }
        return false;
    }
}
