package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.ItemDropChecker;
import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.events.SkyblockBlockBreakEvent;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;

public class PlayerControllerMPHook {

    /**
     * Checks if an item is being dropped and if an item is being dropped, whether it is allowed to be dropped.
     * This check works only for mouse clicks, not presses of the "Drop Item" key.
     *
     * @param clickModifier the click modifier
     * @param slotNum the number of the slot that was clicked on
     * @param heldStack the item stack the player is holding with their mouse
     * @return {@code true} if the action should be cancelled, {@code false} otherwise
     */
    public static boolean checkItemDrop(int clickModifier, int slotNum, ItemStack heldStack) {
        // Is this a left or right click?
        if ((clickModifier == 0 || clickModifier == 1)) {
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

    public static void onPlayerDestroyBlock(BlockPos blockPos) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();

        if (main.getUtils().isOnSkyblock()) {
            IBlockState block = mc.theWorld.getBlockState(blockPos);
            // Use vanilla break mechanic to get breaking time
            double perTickIncrease = block.getBlock().getPlayerRelativeBlockHardness(mc.thePlayer, mc.thePlayer.worldObj, blockPos);
            int MILLISECONDS_PER_TICK = 1000 / 20;
            MinecraftForge.EVENT_BUS.post(new SkyblockBlockBreakEvent(blockPos, (long) (MILLISECONDS_PER_TICK / perTickIncrease)));
        }
    }

    public static void onResetBlockRemoving() {
        MinecraftHook.prevClickBlock = new BlockPos(-1, -1, -1);
    }

    /**
     * Cancels clicking a locked inventory slot, even from other mods
     */
    public static void onWindowClick(int slotNum, int mouseButtonClicked, int mode, EntityPlayer player, ReturnValue<ItemStack> returnValue) { // return null
        //if (Minecraft.getMinecraft().thePlayer.openContainer != null) {
        //    SkyblockAddons.getLogger().info("Handling windowclick--slotnum: " + slotNum + " should be locked: " + SkyblockAddons.getInstance().getConfigValues().getLockedSlots().contains(slotNum) + " mousebutton: " + mouseButtonClicked + " mode: " + mode + " container class: " + player.openContainer.getClass().toString());
        //}

        // Handle blocking the next click, sorry I did it this way
        if (Utils.blockNextClick) {
            Utils.blockNextClick = false;
            returnValue.cancel();
            return;
        }

        SkyblockAddons main = SkyblockAddons.getInstance();
        int slotId = slotNum;
        ItemStack itemStack = player.inventory.getItemStack();

        if (main.getUtils().isOnSkyblock()) {
            // Prevent dropping rare items
            if (Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled() && !main.getUtils().isInDungeon()) {
                if (checkItemDrop(mode, slotNum, itemStack)) {
                    returnValue.cancel();
                }
            }

            if (player.openContainer != null) {
                slotNum += main.getInventoryUtils().getSlotDifference(player.openContainer);

                final Container slots = player.openContainer;

                Slot slotIn;
                try {
                    slotIn = slots.getSlot(slotId);
                } catch (IndexOutOfBoundsException e) {
                    slotIn = null;
                }

                // Prevent clicking on locked slots.
                if (Feature.LOCK_SLOTS.isEnabled() && main.getPersistentValuesManager().getLockedSlots().contains(slotNum)
                        && (slotNum >= 9 || player.openContainer instanceof ContainerPlayer && slotNum >= 5)) {
                    if (mouseButtonClicked == 1 && mode == 0 && slotIn != null && slotIn.getHasStack() && slotIn.getStack().getItem() == Items.skull) {

                        String itemID = ItemUtils.getSkyblockItemID(slotIn.getStack());
                        if (itemID == null) itemID = "";

                        // Now that right-clicking backpacks is removed, remove this check and block right clicking on backpacks if locked
                        if (/*ItemUtils.isBuildersWand(slotIn.getStack()) || ItemUtils.isBackpack(slotIn.getStack()) || */itemID.contains("SACK")) {
                            return;
                        }
                    }

                    main.getUtils().playLoudSound("note.bass", 0.5);
                    returnValue.cancel();
                }
            }
        } else {
            if (checkItemDrop(mode, slotNum, itemStack)) {
                returnValue.cancel();
            }
        }
    }
}
