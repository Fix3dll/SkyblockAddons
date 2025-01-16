package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.utils.LocationUtils;
import net.minecraft.util.ResourceLocation;

public class RenderEndermanHook {

    private static final ResourceLocation BLANK_ENDERMAN_TEXTURE = new ResourceLocation("skyblockaddons", "blankenderman.png");

    public static ResourceLocation getEndermanTexture(ResourceLocation endermanTexture) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && Feature.CHANGE_ZEALOT_COLOR.isEnabled() && LocationUtils.isOnZealotSpawnLocation()) {
            return BLANK_ENDERMAN_TEXTURE;
        }
        return endermanTexture;
    }
}
