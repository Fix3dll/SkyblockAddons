package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.events.RenderEvents;
import com.fix3dll.skyblockaddons.mixin.hooks.EndermanRendererHook;
import com.fix3dll.skyblockaddons.mixin.hooks.LivingEntityRendererHook;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At("HEAD"))
    public void sba$shouldRenderLivingName(T livingEntity, double d, CallbackInfoReturnable<Boolean> cir) {
        RenderEvents.LIVING_NAME.invoker().shouldRenderLivingName(livingEntity, d, cir);
    }

    @Inject(method = "getModelTint", at = @At("HEAD"), cancellable = true)
    public void sba$getModelTint(S renderState, CallbackInfoReturnable<Integer> cir) {
        if (renderState instanceof EndermanRenderState) {
            cir.setReturnValue(EndermanRendererHook.getEndermanColor());
        }
    }

    @ModifyExpressionValue(method = "isUpsideDownName", at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0))
    private static boolean sba$isCoolPerson(boolean original, @Local(argsOnly = true) String name) {
        return LivingEntityRendererHook.isCoolPerson(name) || original;
    }

}