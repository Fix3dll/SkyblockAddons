package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.events.SkyblockEvents;
import com.fix3dll.skyblockaddons.features.ItemDropChecker;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class MultiPlayerGameModeHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    /**
     * Checks if an item is being dropped and if an item is being dropped, whether it is allowed to be dropped.
     * This check works only for mouse clicks, not presses of the "Drop Item" key.
     * @param clickType the click modifier
     * @param slotNum the number of the slot that was clicked on
     * @param heldStack the item stack the player is holding with their mouse
     * @return {@code true} if the action should be cancelled, {@code false} otherwise
     */
    public static boolean checkItemDrop(ClickType clickType, int slotNum, ItemStack heldStack) {
        // Is this a left or right click?
        if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE)) {
            // Is the player clicking outside their inventory?
            if (slotNum == -999) {
                // Is the player holding an item stack with their mouse?
                if (heldStack != null) {
                    return !ItemDropChecker.canDropItem(heldStack);
                }
            }
        }

        // The player is not dropping an item. Don't cancel this action.
        return false;
    }

    public static void onDestroyBlock(BlockPos blockPos) {
        LocalPlayer localPlayer = MC.player;
        if (main.getUtils().isOnSkyblock() && localPlayer != null) {
            BlockState block = localPlayer.level().getBlockState(blockPos);
            // Use vanilla break mechanic to get breaking time
            double perTickIncrease = block.getDestroyProgress(localPlayer, localPlayer.level(), blockPos);
            final int MILLISECONDS_PER_TICK = 1000 / 20;
            SkyblockEvents.BLOCK_BREAK.invoker().onBlockBreak(blockPos, (long) (MILLISECONDS_PER_TICK / perTickIncrease));
        }
    }

    public static void onStopDestroyBlock() {
        MinecraftHook.prevClickBlock = new BlockPos(-1, -1, -1);
    }

    /**
     * Cancels clicking a locked inventory slot, even from other mods
     */
    public static void handleInventoryMouseClick(int slotId, int mouseButton, ClickType clickType, Player player, CallbackInfo ci) {
//        SkyblockAddons.getLogger().info(
//                "Handling containerInput. slotNum: {}, should be locked: {}, buttonNum: {}, mode: {}, container class: {}",
//                slotNum, main.getPersistentValuesManager().getLockedSlots().contains(slotNum), buttonNum, containerInput, player.containerMenu.getClass()
//        );

        // Handle blocking the next click, sorry I did it this way
        if (Utils.blockNextClick) {
            Utils.blockNextClick = false;
            ci.cancel();
            return;
        }

        ItemStack itemStack = player.inventoryMenu.getCarried();
        int slotNum = slotId;

        if (main.getUtils().isOnSkyblock()) {
            // Prevent dropping rare items
            if (checkItemDrop(clickType, slotId, itemStack)) {
                ci.cancel();
                return;
            }

            slotId += main.getInventoryUtils().getSlotDifference(player.containerMenu);

            Slot slotIn;
            try {
                slotIn = slotNum == -999 ? null : player.containerMenu.getSlot(slotNum);
            } catch (IndexOutOfBoundsException e) {
                slotIn = null;
            }

            // Prevent clicking on locked slots.
            if (Feature.LOCK_SLOTS.isEnabled() && main.getPersistentValuesManager().getLockedSlots().contains(slotId)
                    && (slotId >= 9 || player.containerMenu instanceof InventoryMenu && slotId >= 5)) {
                if (mouseButton == 1 && clickType == ClickType.PICKUP && slotIn != null && slotIn.hasItem()
                        && slotIn.getItem().getItem() == Items.PLAYER_HEAD) {
                    String itemID = ItemUtils.getSkyblockItemID(slotIn.getItem());
                    // Even if it's locked, allow right-clicking if it's a sack
                    if (itemID != null && itemID.contains("SACK")) {
                        return;
                    }
                }

                main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
                ci.cancel();
            }
        } else {
            if (checkItemDrop(clickType, slotId, itemStack)) {
                ci.cancel();
            }
        }
    }

}