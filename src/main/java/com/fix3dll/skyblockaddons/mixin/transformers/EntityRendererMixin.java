package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.events.RenderEvents;
import com.fix3dll.skyblockaddons.mixin.extensions.EntityRenderStateExtension;
import com.fix3dll.skyblockaddons.mixin.hooks.EntityRendererHook;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    public void sba$shouldRender(T livingEntity, Frustum camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (!EntityRendererHook.shouldRender(livingEntity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "submitNameTag", at = @At("HEAD"), cancellable = true)
    public void sba$onsubmitNameTag(S renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (RenderEvents.SUBMIT_ENTITY_NAME_TAG.invoker().onSubmitEntityNameTag(renderState, poseStack, nodeCollector, cameraRenderState, ci)) {
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void sba$setEntityId(T entity, S reusedState, float partialTick, CallbackInfo ci) {
        EntityRenderStateExtension entityRenderStateExtension = (EntityRenderStateExtension) reusedState;
        entityRenderStateExtension.sba$setEntityId(entity.getId());
    }

}