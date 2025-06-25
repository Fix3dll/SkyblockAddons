package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

public class ChestSpecialRendererHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final ResourceLocation BLANK_ENDERCHEST = SkyblockAddons.resourceLocation("blankenderchest.png");

    /**
     * @return false if default behavior is to be overridden
     */
    public static boolean renderToBuffer(ChestModel instance, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        if (main.getUtils().isOnSkyblock() && Minecraft.getInstance().screen == null
                && Feature.MAKE_ENDERCHESTS_GREEN_IN_END.isEnabled()
                && LocationUtils.isOnZealotSpawnLocation()) {
            instance.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, Feature.MAKE_ENDERCHESTS_GREEN_IN_END.getColor());
            return false;
        }
        return true;
    }

    /**
     * @return original if conditions are not met
     */
    public static VertexConsumer createBuffer(Material original, MultiBufferSource bufferSource) {
        if (main.getUtils().isOnSkyblock() && Minecraft.getInstance().screen == null
                && Feature.MAKE_ENDERCHESTS_GREEN_IN_END.isEnabled()
                && LocationUtils.isOnZealotSpawnLocation()) {
            return spriteBlankChest(original.atlasLocation()).wrap(
                    bufferSource.getBuffer(RenderType.entitySolid(BLANK_ENDERCHEST))
            );
        } else {
            return original.sprite().wrap(bufferSource.getBuffer(RenderType.entitySolid(original.atlasLocation())));
        }
    }

    private static TextureAtlasSprite spriteBlankChest(ResourceLocation atlasLocation) {
        return Minecraft.getInstance().getTextureAtlas(atlasLocation).apply(BLANK_ENDERCHEST);
    }

}