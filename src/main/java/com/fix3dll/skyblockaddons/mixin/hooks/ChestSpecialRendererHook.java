package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ChestSpecialRendererHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final ResourceLocation BLANK_ENDERCHEST = SkyblockAddons.resourceLocation("blankenderchest.png");

    public static Integer getCustomEnderChestColor() {
        if (main.getUtils().isOnSkyblock() && Minecraft.getInstance().screen == null
                && Feature.MAKE_ENDERCHESTS_GREEN_IN_END.isEnabled()
                && LocationUtils.isOnZealotSpawnLocation()) {
            return Feature.MAKE_ENDERCHESTS_GREEN_IN_END.getColor();
        }
        return null;
    }

    /**
     * @return original if conditions are not met
     */
    public static RenderType getRenderType() {
        if (main.getUtils().isOnSkyblock() && Minecraft.getInstance().screen == null
                && Feature.MAKE_ENDERCHESTS_GREEN_IN_END.isEnabled()
                && LocationUtils.isOnZealotSpawnLocation()) {
            return RenderType.entitySolid(BLANK_ENDERCHEST);
        }
        return null;
    }

}