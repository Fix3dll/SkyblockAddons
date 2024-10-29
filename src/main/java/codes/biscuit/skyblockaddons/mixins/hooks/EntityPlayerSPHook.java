package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;

public class EntityPlayerSPHook {

    private static String lastItemName = null;
    private static long lastDrop = Minecraft.getSystemTime();

    public static EntityItem dropOneItemConfirmation(ReturnValue<?> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItemStack = mc.thePlayer.getHeldItem();

        if ((main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())) {
            if (Feature.LOCK_SLOTS.isEnabled() && !main.getUtils().isInDungeon()) {
                int slot = mc.thePlayer.inventory.currentItem + 36;
                if (main.getConfigValues().getLockedSlots().contains(slot) && (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5)) {
                    main.getUtils().playLoudSound("note.bass", 0.5);
                    SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Translations.getMessage("messages.slotLocked"));
                    returnValue.cancel();
                    return null;
                }

                if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                    main.getUtils().playLoudSound("note.bass", 0.5);
                    SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Translations.getMessage("messages.switchedSlots"));
                    returnValue.cancel();
                    return null;
                }
            }

            if (heldItemStack != null && Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled() && !main.getUtils().isInDungeon()) {
                if (!main.getUtils().getItemDropChecker().canDropItem(heldItemStack, true)) {
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) + Translations.getMessage("messages.cancelledDropping"));
                    returnValue.cancel();
                    return null;
                }

                if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                    main.getUtils().playLoudSound("note.bass", 0.5);
                    SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Translations.getMessage("messages.switchedSlots"));
                    returnValue.cancel();
                    return null;
                }
            }
        }

        if (heldItemStack != null && Feature.DROP_CONFIRMATION.isEnabled() && !main.getUtils().isInDungeon()
                && (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer() || Feature.DOUBLE_DROP_IN_OTHER_GAMES.isEnabled())) {
            lastDrop = Minecraft.getSystemTime();

            String heldItemName = heldItemStack.hasDisplayName() ? heldItemStack.getDisplayName() : heldItemStack.getUnlocalizedName();

            if (lastItemName == null || !lastItemName.equals(heldItemName) || Minecraft.getSystemTime() - lastDrop >= 3000L) {
                SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Translations.getMessage("messages.dropConfirmation"));
                lastItemName = heldItemName;
                returnValue.cancel();
            }
        }

        return null;
    }
}
