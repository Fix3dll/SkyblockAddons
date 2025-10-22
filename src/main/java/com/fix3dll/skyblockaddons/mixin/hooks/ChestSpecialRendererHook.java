package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;

public class ChestSpecialRendererHook {

    public static final Material BLANK_ENDER_CHEST_MATERIAL = Sheets.CHEST_MAPPER.apply(
            SkyblockAddons.resourceLocation("blankenderchest")
    );

    public static TextureAtlasSprite getBlankSprite() {
        return Minecraft.getInstance().getAtlasManager().get(BLANK_ENDER_CHEST_MATERIAL);
    }

    public static Integer getCustomEnderChestColor() {
        if (Minecraft.getInstance().screen == null
                && SkyblockAddons.getInstance().getUtils().isOnSkyblock()
                && Feature.MAKE_ENDERCHESTS_GREEN_IN_END.isEnabled()
                && LocationUtils.isOnZealotSpawnLocation()) {
            return Feature.MAKE_ENDERCHESTS_GREEN_IN_END.getColor();
        }
        return null;
    }

}