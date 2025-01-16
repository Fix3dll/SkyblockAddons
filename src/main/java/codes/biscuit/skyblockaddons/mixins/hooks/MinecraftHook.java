package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.ItemDropChecker;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import lombok.Getter;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;

public class MinecraftHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getMinecraft();

    @Getter private static long lastLockedSlotItemChange = -1;

    protected static BlockPos prevClickBlock = new BlockPos(-1, -1, -1);
    protected static long startMineTime = Long.MAX_VALUE;
    protected static LinkedHashMap<BlockPos, Long> recentlyClickedBlocks = new LinkedHashMap<>();

    public static void rightClickMouse(CallbackInfo ci) {
        if (main.getUtils().isOnSkyblock()) {
            if (MC.objectMouseOver != null && MC.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity entityIn = MC.objectMouseOver.entityHit;

                if (Feature.LOCK_SLOTS.isEnabled() && entityIn instanceof EntityItemFrame && ((EntityItemFrame)entityIn).getDisplayedItem() == null) {
                    int slot = MC.thePlayer.inventory.currentItem + 36;
                    if (main.getPersistentValuesManager().getLockedSlots().contains(slot) && slot >= 9) {
                        main.getUtils().playLoudSound("note.bass", 0.5);
                        main.getUtils().sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.slotLocked"));
                        ci.cancel();
                    }
                }
            }
        }
    }

    public static void onUpdateCurrentItem() {
        if (Feature.LOCK_SLOTS.isEnabled() && (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())) {
            int slot = MC.thePlayer.inventory.currentItem + 36;
            if (Feature.LOCK_SLOTS.isEnabled() && main.getPersistentValuesManager().getLockedSlots().contains(slot)
                    && (slot >= 9 || MC.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5)) {

                lastLockedSlotItemChange = System.currentTimeMillis();
            }

            ItemStack heldItemStack = MC.thePlayer.getHeldItem();
            if (heldItemStack != null
                    && Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled()
                    && !main.getUtils().isInDungeon()
                    && !ItemDropChecker.canDropItem(heldItemStack, true, false)) {

                lastLockedSlotItemChange = System.currentTimeMillis();
            }
        }
    }

    public static void onClickMouse(CallbackInfo ci) {
        if (MC.objectMouseOver == null || MC.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }
        BlockPos blockPos = MC.objectMouseOver.getBlockPos();
        if (MC.theWorld.getBlockState(blockPos).getBlock().getMaterial() == Material.air) {
            return;
        }


        if (!ci.isCancelled() && !prevClickBlock.equals(blockPos)) {
            startMineTime = System.currentTimeMillis();
        }
        prevClickBlock = blockPos;
        if (!ci.isCancelled()) {
            recentlyClickedBlocks.put(blockPos, System.currentTimeMillis());
        }
    }

    public static void onSendClickBlockToController(boolean leftClick, CallbackInfo returnValue) {
        // If we aren't trying to break anything, don't change vanilla behavior (was causing false positive chat messages)
        if (!leftClick) {
            return;
        }
        onClickMouse(returnValue);
        // Canceling this is tricky. Not only do we have to reset block removing, but also reset the position we are breaking
        // This is because we want playerController.onClick to be called when they go back to that block
        // It's also important to resetBlockRemoving before changing current block, since then we'd be sending the server inaccurate info that could trigger wdr
        // This mirrors PlayerControllerMP.clickBlock(), which sends an ABORT_DESTROY message, before calling onPlayerDestroyBlock, which changes "currentBlock"
        if (returnValue.isCancelled()) {
            MC.playerController.resetBlockRemoving();
            MC.playerController.currentBlock = new BlockPos(-1, -1, -1);
        }
    }

}
