package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderItemHook {

    private static final ResourceLocation BLANK = new ResourceLocation("skyblockaddons","blank.png");

    public static void renderArrowPoisonEffect(IBakedModel model, ItemStack stack) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock() && Feature.TURN_BOW_COLOR_WHEN_USING_ARROW_POISON.isEnabled()
                && (main.getInventoryUtils().isUsingToxicArrowPoison() || main.getInventoryUtils().isUsingTwilightArrowPoison())
                && Items.bow.equals(stack.getItem()) && main.getUtils().itemIsInHotbar(stack)) {
            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

            GlStateManager.depthMask(false);
            GlStateManager.depthFunc(514);
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(768, 1);
            textureManager.bindTexture(BLANK);
            GlStateManager.matrixMode(5890);

            GlStateManager.pushMatrix();

            if (main.getInventoryUtils().isUsingToxicArrowPoison())
                Minecraft.getMinecraft().getRenderItem().renderModel(model, 0x201cba41);
            else if (main.getInventoryUtils().isUsingTwilightArrowPoison())
                Minecraft.getMinecraft().getRenderItem().renderModel(model, 0x20ff37ff);
            GlStateManager.popMatrix();

            GlStateManager.matrixMode(5888);
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableLighting();
            GlStateManager.depthFunc(515);
            GlStateManager.depthMask(true);
            textureManager.bindTexture(TextureMap.locationBlocksTexture);
        }
    }
}
