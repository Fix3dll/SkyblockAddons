package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.ItemDropChecker;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;

public class EntityPlayerSPHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getMinecraft();

    public static boolean dropOneItemConfirmation() {
        ItemStack heldItemStack = MC.thePlayer.getHeldItem();

        if (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer()) {
            if (Feature.LOCK_SLOTS.isEnabled() && !main.getUtils().isInDungeon()) {
                int slot = MC.thePlayer.inventory.currentItem + 36;
                System.out.println(slot);
                if (main.getPersistentValuesManager().getLockedSlots().contains(slot)
                        && (slot >= 9 || MC.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5)) {
                    main.getUtils().playLoudSound("note.bass", 0.5);
                    Utils.sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.slotLocked"));
                    return true;
                }

                if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                    main.getUtils().playLoudSound("note.bass", 0.5);
                    Utils.sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.switchedSlots"));
                    return true;
                }
            }

            if (heldItemStack != null && Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled() && !main.getUtils().isInDungeon()) {
                if (!ItemDropChecker.canDropItem(heldItemStack, true)) {
                    Utils.sendMessage(Feature.STOP_DROPPING_SELLING_RARE_ITEMS.getRestrictedColor() + Translations.getMessage("messages.cancelledDropping"));
                    return true;
                }

                if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                    main.getUtils().playLoudSound("note.bass", 0.5);
                    Utils.sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.switchedSlots"));
                    return true;
                }
            }
        }
        return false;
    }
}