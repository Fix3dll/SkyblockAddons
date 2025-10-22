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
import net.minecraft.world.inventory.AbstractContainerMenu;
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
        //if (Minecraft.getMinecraft().thePlayer.openContainer != null) {
        //    SkyblockAddons.getLogger().info("Handling windowclick--slotnum: " + slotNum + " should be locked: " + SkyblockAddons.getInstance().getConfigValues().getLockedSlots().contains(slotNum) + " mousebutton: " + mouseButtonClicked + " mode: " + mode + " container class: " + player.openContainer.getClass().toString());
        //}

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
            if (Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled() && !main.getUtils().isInDungeon()) {
                if (checkItemDrop(clickType, slotId, itemStack)) {
                    ci.cancel();
                    return;
                }
            }

            if (player.containerMenu != null) {
                slotId += main.getInventoryUtils().getSlotDifference(player.containerMenu);

                final AbstractContainerMenu slots = player.containerMenu;

                Slot slotIn;
                try {
                    slotIn = slotNum == -999 ? null : slots.getSlot(slotNum);
                } catch (IndexOutOfBoundsException e) {
                    slotIn = null;
                }

                // Prevent clicking on locked slots.
                if (Feature.LOCK_SLOTS.isEnabled() && main.getPersistentValuesManager().getLockedSlots().contains(slotId)
                        && (slotId >= 9 || player.containerMenu instanceof InventoryMenu && slotId >= 5)) {
                    if (mouseButton == 1 && clickType == ClickType.PICKUP && slotIn != null && slotIn.hasItem() && slotIn.getItem().getItem() == Items.PLAYER_HEAD) {

                        String itemID = ItemUtils.getSkyblockItemID(slotIn.getItem());
                        if (itemID == null) itemID = "";

                        // Now that right-clicking backpacks is removed, remove this check and block right clicking on backpacks if locked
                        if (/*ItemUtils.isBuildersWand(slotIn.getStack()) || ItemUtils.isBackpack(slotIn.getStack()) || */itemID.contains("SACK")) {
                            return;
                        }
                    }

                    main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
                    ci.cancel();
                }
            }
        } else {
            if (checkItemDrop(clickType, slotId, itemStack)) {
                ci.cancel();
            }
        }
    }

}