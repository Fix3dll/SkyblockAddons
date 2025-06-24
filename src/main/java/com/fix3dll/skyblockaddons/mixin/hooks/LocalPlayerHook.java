package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.features.ItemDropChecker;
import com.fix3dll.skyblockaddons.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class LocalPlayerHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    public static boolean dropOneItemConfirmation() {
        if (MC.player == null) return true;
        ItemStack heldItemStack = MC.player.getMainHandItem();

        if (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer()) {
            if (Feature.LOCK_SLOTS.isEnabled() && !main.getUtils().isInDungeon()) {
                int slot = MC.player.getInventory().getSelectedSlot() + 36;
                if (main.getPersistentValuesManager().getLockedSlots().contains(slot)
                        && (slot >= 9 || MC.player.containerMenu instanceof InventoryMenu && slot >= 5)) {
                    main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
                    Utils.sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.slotLocked"));
                    return true;
                }

                if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                    main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
                    Utils.sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.switchedSlots"));
                    return true;
                }
            }

            if (heldItemStack != ItemStack.EMPTY && Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled() && !main.getUtils().isInDungeon()) {
                if (!ItemDropChecker.canDropItem(heldItemStack, true)) {
                    Utils.sendMessage(Feature.STOP_DROPPING_SELLING_RARE_ITEMS.getRestrictedColor() + Translations.getMessage("messages.cancelledDropping"));
                    return true;
                }

                if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                    main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
                    Utils.sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.switchedSlots"));
                    return true;
                }
            }
        }
        return false;
    }
}
