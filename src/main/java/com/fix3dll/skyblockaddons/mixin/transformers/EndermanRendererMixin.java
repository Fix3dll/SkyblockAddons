package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.EndermanRendererHook;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndermanRenderer.class)
public abstract class EndermanRendererMixin extends MobRenderer<EnderMan, EndermanRenderState, EndermanModel<EndermanRenderState>> {

    public EndermanRendererMixin(EntityRendererProvider.Context context, EndermanModel<EndermanRenderState> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/EndermanRenderState;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    public void sba$getTexture(EndermanRenderState endermanRenderState, CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation texture = EndermanRendererHook.getEndermanTexture();
        if (texture != null) cir.setReturnValue(texture);
    }

}