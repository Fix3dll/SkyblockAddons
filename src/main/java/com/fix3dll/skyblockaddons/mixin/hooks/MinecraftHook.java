package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.features.ItemDropChecker;
import com.fix3dll.skyblockaddons.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;

public class MinecraftHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    @Getter private static long lastLockedSlotItemChange = -1;

    protected static BlockPos prevClickBlock = new BlockPos(-1, -1, -1);
    protected static long startMineTime = Long.MAX_VALUE;
    protected static LinkedHashMap<BlockPos, Long> recentlyClickedBlocks = new LinkedHashMap<>();

    public static void rightClickMouse(CallbackInfo ci) {
        if (main.getUtils().isOnSkyblock() && MC.player != null) {
            if (MC.hitResult instanceof EntityHitResult result) {
                Entity entityIn = result.getEntity();

                if (Feature.LOCK_SLOTS.isEnabled() && entityIn instanceof ItemFrame itemFrame && itemFrame.getItem() == ItemStack.EMPTY) {
                    int slot = MC.player.getInventory().getSelectedSlot() + 36;
                    if (main.getPersistentValuesManager().getLockedSlots().contains(slot) && slot >= 9) {
                        main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
                        Utils.sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.slotLocked"));
                        ci.cancel();
                    }
                }
            }
        }
    }

    public static void onUpdateSelectedItem() {
        if (MC.player == null) return;

        if (Feature.LOCK_SLOTS.isEnabled() && (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())) {
            int slot = MC.player.getInventory().getSelectedSlot() + 36;
            if (Feature.LOCK_SLOTS.isEnabled() && main.getPersistentValuesManager().getLockedSlots().contains(slot)
                    && (slot >= 9 || MC.player.containerMenu instanceof InventoryMenu && slot >= 5)) {

                lastLockedSlotItemChange = System.currentTimeMillis();
            }

            ItemStack heldItemStack = MC.player.getMainHandItem();
            if (heldItemStack != ItemStack.EMPTY
                    && Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled()
                    && !main.getUtils().isInDungeon()
                    && !ItemDropChecker.canDropItem(heldItemStack, true, false)) {

                lastLockedSlotItemChange = System.currentTimeMillis();
            }
        }
    }

    public static void onClickMouse(CallbackInfo ci) {
        if (MC.level == null) return;

        if (MC.hitResult instanceof BlockHitResult result) {
            BlockPos blockPos = result.getBlockPos();

            if (MC.level.getBlockState(blockPos).is(Blocks.AIR)) {
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
    }

    public static void onSendClickBlockToController(boolean leftClick, CallbackInfo returnValue) {
        // If we aren't trying to break anything, don't change vanilla behavior (was causing false positive chat messages)
        if (!leftClick) {
            return;
        }
        onClickMouse(returnValue);
        // Canceling this is tricky. Not only do we have to reset block removing, but also reset the position we are breaking
        // This is because we want playerController.onClick to be called when they go back to that block
        // It's also important to stopDestroyBlock before changing current block, since then we'd be sending the server inaccurate info that could trigger wdr
        // This mirrors MultiPlayerGameMode.startDestroyBlock(), which sends an ABORT_DESTROY message, before calling destroyBlock, which changes "destroyBlockPos"
        if (returnValue.isCancelled() && MC.gameMode != null) {
            MC.gameMode.stopDestroyBlock();
            MC.gameMode.destroyBlockPos = new BlockPos(-1, -1, -1);
        }
    }

}