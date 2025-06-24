package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.npc.NPCUtils;
import com.fix3dll.skyblockaddons.events.SkyblockEvents;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Iterator;
import java.util.Map;

public class ClientLevelHook {

    private static final Minecraft MC =  Minecraft.getInstance();

    public static void onRemoveEntity(int entityId) {
        NPCUtils.getNpcLocations().remove(entityId);
    }

    public static void blockUpdated(BlockPos pos, BlockState state) {
        if (MC.player != null && MC.level != null) {
            int BEDROCK_STATE = Block.getId(Blocks.BEDROCK.defaultBlockState());
            int AIR_STATE = Block.getId(Blocks.AIR.defaultBlockState());
            int stateBefore = Block.getId(MC.level.getBlockState(pos));
            Iterator<Map.Entry<BlockPos, Long>> itr = MinecraftHook.recentlyClickedBlocks.entrySet().iterator();
            long currTime = System.currentTimeMillis();
            while (itr.hasNext()) {
                Map.Entry<BlockPos, Long> entry = itr.next();
                if (currTime - entry.getValue() < 300) {
                    break;
                }
                itr.remove();
            }
            // Fire event if the client is breaking a block that is not being broken by another player, and the block is changing
            // One infrequent bug is if client mining stone and it turns into ore randomly. This will trigger this method currently
            if (/*mc.playerController.getIsHittingBlock() && */MinecraftHook.recentlyClickedBlocks.containsKey(pos) &&
                    stateBefore != Block.getId(state) && stateBefore != BEDROCK_STATE && stateBefore != AIR_STATE) {
                // Get the player's ID (0 on public islands and the player's entity ID on private islands)
                // Blocks broken on guest islands don't count
                if (LocationUtils.isOn(Island.PRIVATE_ISLAND)) {
                    return;
                }
                int playerID = MC.player.getId()/*location == Location.ISLAND || location == Location.GUEST_ISLAND ? mc.thePlayer.getEntityId() :*/;
                // Don't fire if anyone else is mining the same block...
                // This will undercount your blocks if you broke the block before the other person
                // But the alternative is to overcount your blocks if someone else breaks the block before you...not much better
                // Also could mathematically determine a probability based on pos, yaw, pitch of other entities...worth it? ehh...
                boolean noOneElseMining = true;
                for (Int2ObjectMap.Entry<BlockDestructionProgress> block : MC.levelRenderer.destroyingBlocks.int2ObjectEntrySet()) {
                    // TODO improve
                    boolean isPlayer = block.getIntKey() == 0 || block.getIntKey() == playerID;
                    if (!isPlayer && block.getValue().getPos().equals(pos)) {
                        noOneElseMining = false;
                    }
                }
                if (noOneElseMining) {
                    long mineTime = Math.max(System.currentTimeMillis() - MinecraftHook.startMineTime, 0);
                    SkyblockEvents.BLOCK_BREAK.invoker().onBlockBreak(pos, mineTime);
                }
            }
        }
    }

}