package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class MinecraftHook {

    @Getter private static long lastLockedSlotItemChange = -1;

    // The room with the puzzle is made of wood that you have to mine
    private static final AxisAlignedBB DWARVEN_PUZZLE_ROOM = new AxisAlignedBB(171, 195, 125, 192, 196, 146);

    private static final Set<Block> DEEP_CAVERNS_MINEABLE_BLOCKS = new HashSet<>(Arrays.asList(Blocks.coal_ore, Blocks.iron_ore, Blocks.gold_ore, Blocks.redstone_ore, Blocks.emerald_ore,
            Blocks.diamond_ore, Blocks.diamond_block, Blocks.obsidian, Blocks.lapis_ore, Blocks.lit_redstone_ore));

    private static final Set<Block> NETHER_MINEABLE_BLOCKS = new HashSet<>(Arrays.asList(Blocks.glowstone, Blocks.quartz_ore, Blocks.nether_wart));

    // TODO: Make this less computationally expensive
    // More specifically, should be cyan hardened clay, grey/light blue wool, dark prismarine, prismarine brick, prismarine, polished diorite
    private static final Set<String> DWARVEN_MINEABLE_BLOCKS = new HashSet<>(Arrays.asList("minecraft:prismarine0",
            "minecraft:prismarine1", "minecraft:prismarine2", "minecraft:stone4", "minecraft:wool3", "minecraft:wool7",
            "minecraft:stained_hardened_clay9"));

    private static final Set<Block> LOGS = new HashSet<>(Arrays.asList(Blocks.log, Blocks.log2));

    private static final long lastStemMessage = -1;
    private static final long lastUnmineableMessage = -1;
    public static BlockPos prevClickBlock = new BlockPos(-1, -1, -1);
    public static long startMineTime = Long.MAX_VALUE;

    public static LinkedHashMap<BlockPos, Long> recentlyClickedBlocks = new LinkedHashMap<>();

    public static void rightClickMouse(ReturnValue<?> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity entityIn = mc.objectMouseOver.entityHit;

                if (Feature.LOCK_SLOTS.isEnabled() && entityIn instanceof EntityItemFrame && ((EntityItemFrame)entityIn).getDisplayedItem() == null) {
                    int slot = mc.thePlayer.inventory.currentItem + 36;
                    if (main.getConfigValues().getLockedSlots().contains(slot) && slot >= 9) {
                        main.getUtils().playLoudSound("note.bass", 0.5);
                        main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Translations.getMessage("messages.slotLocked"));
                        returnValue.cancel();
                    }
                }
            }
        }
    }

    public static void updatedCurrentItem() {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (Feature.LOCK_SLOTS.isEnabled() && (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())) {
            int slot = mc.thePlayer.inventory.currentItem + 36;
            if (Feature.LOCK_SLOTS.isEnabled() && main.getConfigValues().getLockedSlots().contains(slot)
                    && (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5)) {

                MinecraftHook.lastLockedSlotItemChange = System.currentTimeMillis();
            }

            ItemStack heldItemStack = mc.thePlayer.getHeldItem();
            if (heldItemStack != null && Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled() && !main.getUtils().isInDungeon()
                    && !main.getUtils().getItemDropChecker().canDropItem(heldItemStack, true, false)) {

                MinecraftHook.lastLockedSlotItemChange = System.currentTimeMillis();
            }
        }
    }

    public static void onClickMouse(ReturnValue<?> returnValue) {

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }
        BlockPos blockPos = mc.objectMouseOver.getBlockPos();
        if (mc.theWorld.getBlockState(blockPos).getBlock().getMaterial() == Material.air) {
            return;
        }


        if (!returnValue.isCancelled() && !prevClickBlock.equals(blockPos)) {
            startMineTime = System.currentTimeMillis();
        }
        prevClickBlock = blockPos;
        if (!returnValue.isCancelled()) {
            recentlyClickedBlocks.put(blockPos, System.currentTimeMillis());
        }
    }

    public static void onSendClickBlockToController(boolean leftClick, ReturnValue<?> returnValue) {
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
            Minecraft.getMinecraft().playerController.resetBlockRemoving();
            Minecraft.getMinecraft().playerController.currentBlock = new BlockPos(-1, -1, -1);
        }
    }

}
