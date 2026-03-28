package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.ChestSpecialRendererHook;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static com.fix3dll.skyblockaddons.mixin.hooks.ChestSpecialRendererHook.BLANK_ENDER_CHEST_MATERIAL;

@Mixin(ChestSpecialRenderer.class)
public class ChestSpecialRendererMixin {

    @ModifyArgs(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    public void sba$submit(Args args) {
        Integer customEnderChestColor = ChestSpecialRendererHook.getCustomEnderChestColor();
        if (customEnderChestColor != null) {
            args.set(3, LightCoordsUtil.FULL_BRIGHT);
            args.set(5, customEnderChestColor);
            args.set(6, BLANK_ENDER_CHEST_MATERIAL);
            //args.set(8, customEnderChestColor);
        }
    }

}