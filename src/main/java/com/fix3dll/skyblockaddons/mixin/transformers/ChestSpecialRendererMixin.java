package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.ChestSpecialRendererHook;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static com.fix3dll.skyblockaddons.mixin.hooks.ChestSpecialRendererHook.BLANK_ENDER_CHEST_MATERIAL;

@Mixin(ChestSpecialRenderer.class)
public class ChestSpecialRendererMixin {

    @ModifyArgs(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    public void sba$submit(Args args) {
        Integer customEnderChestColor = ChestSpecialRendererHook.getCustomEnderChestColor();
        if (customEnderChestColor != null) {
            args.set(3, BLANK_ENDER_CHEST_MATERIAL.renderType(DrawUtils::getEntitySolidZOffset));
            args.set(4, LightTexture.FULL_BRIGHT);
            args.set(6, customEnderChestColor);
            args.set(7, ChestSpecialRendererHook.getBlankSprite());
            //args.set(8, customEnderChestColor);
        }
    }

}