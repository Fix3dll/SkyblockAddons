package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.ItemDropChecker;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class EntityPlayerSPHook {

    public static void dropOneItemConfirmation(CallbackInfoReturnable<EntityItem> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItemStack = mc.thePlayer.getHeldItem();

        if ((main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())) {
            if (Feature.LOCK_SLOTS.isEnabled() && !main.getUtils().isInDungeon()) {
                int slot = mc.thePlayer.inventory.currentItem + 36;
                if (main.getPersistentValuesManager().getLockedSlots().contains(slot)
                        && (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5)) {
                    main.getUtils().playLoudSound("note.bass", 0.5);
                    SkyblockAddons.getInstance().getUtils().sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.slotLocked"));
                    cir.cancel();
                    return;
                }

                if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                    main.getUtils().playLoudSound("note.bass", 0.5);
                    SkyblockAddons.getInstance().getUtils().sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.switchedSlots"));
                    cir.cancel();
                    return;
                }
            }

            if (heldItemStack != null && Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled() && !main.getUtils().isInDungeon()) {
                if (!ItemDropChecker.canDropItem(heldItemStack, true)) {
                    main.getUtils().sendMessage(Feature.STOP_DROPPING_SELLING_RARE_ITEMS.getRestrictedColor() + Translations.getMessage("messages.cancelledDropping"));
                    cir.cancel();
                    return;
                }

                if (System.currentTimeMillis() - MinecraftHook.getLastLockedSlotItemChange() < 200) {
                    main.getUtils().playLoudSound("note.bass", 0.5);
                    SkyblockAddons.getInstance().getUtils().sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.switchedSlots"));
                    cir.cancel();
                    return;
                }
            }
        }

    }
}