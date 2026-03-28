package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import net.minecraft.core.BlockPos;

public class LevelRendererHook {

    public static void onAddBlockBreakParticle(int breakerId, BlockPos pos, int progress) {
        // On public islands, hypixel sends a progress = 10 update once it registers the start of block breaking
        if (breakerId == 0 && !LocationUtils.isOn(Island.PRIVATE_ISLAND) && pos.equals(MinecraftHook.prevClickBlock)
                && progress == 10) {
            //System.out.println(progress);
            MinecraftHook.startMineTime = System.currentTimeMillis();
        }
    }

}