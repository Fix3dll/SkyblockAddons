package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.events.RenderEvents;
import com.fix3dll.skyblockaddons.features.outline.EntityOutlineRenderer;
import com.fix3dll.skyblockaddons.mixin.hooks.LevelRendererHook;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @ModifyExpressionValue(method = "submitEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/LevelRenderState;haveGlowingEntities:Z"))
    public boolean sba$submitEntities(boolean original, @Local EntityRenderState entityRenderState, @Local(argsOnly = true) SubmitNodeCollector nodeCollector) {
        if (original) {
            EntityOutlineRenderer.colorSkyblockEntityOutlines(entityRenderState);
        }
        return original;
    }

    @Inject(method = "destroyBlockProgress", at = @At("HEAD"))
    public void sba$destroyBlockProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        LevelRendererHook.onAddBlockBreakParticle(breakerId, pos, progress);
    }

    @Inject(method = "prepareCullFrustum", at = @At("RETURN"))
    public void sba$cullingFrustum(Matrix4f frustumMatrix, Matrix4f projectionMatrix, Vec3 cameraPosition, CallbackInfoReturnable<Frustum> cir) {
        LevelRendererHook.setCullingFrustum(cir.getReturnValue());
    }

    @Inject(method = "method_62214", at = @At("RETURN"))
    public void sba$addMainPassLambda(GpuBufferSlice gpuBufferSlice, LevelRenderState levelRenderState, ProfilerFiller profilerFiller, Matrix4f matrix4f, ResourceHandle resourceHandle, ResourceHandle resourceHandle2, boolean bl, Frustum frustum, ResourceHandle resourceHandle3, ResourceHandle resourceHandle4, CallbackInfo ci,
                                      @Local(ordinal = 0) MultiBufferSource.BufferSource bufferSource,
                                      @Local PoseStack poseStack) {
        RenderEvents.LEVEL_LAST.invoker().onRenderLevelLast(bufferSource, poseStack);
    }

}