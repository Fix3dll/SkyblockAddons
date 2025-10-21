package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.ChestSpecialRendererHook;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChestSpecialRenderer.class)
public class ChestSpecialRendererMixin {

    @ModifyArg(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), index = 6)
    public int sba$submit(int tintColor) {
        Integer customEnderChestColor = ChestSpecialRendererHook.getCustomEnderChestColor();
        return customEnderChestColor == null ? tintColor : customEnderChestColor;
    }

    @ModifyArg(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), index = 3)
    public RenderType sba$renderType(RenderType original) {
        RenderType customRenderType = ChestSpecialRendererHook.getRenderType();
        return customRenderType == null ? original : customRenderType;
    }

}