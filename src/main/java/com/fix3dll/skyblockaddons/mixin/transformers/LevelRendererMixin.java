package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.events.RenderEvents;
import com.fix3dll.skyblockaddons.features.outline.EntityOutlineRenderer;
import com.fix3dll.skyblockaddons.mixin.hooks.LevelRendererHook;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4fc;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @ModifyExpressionValue(method = "submitEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/LevelRenderState;haveGlowingEntities:Z", opcode = Opcodes.GETFIELD))
    public boolean sba$submitEntities(boolean original, @Local(name = "state") EntityRenderState entityRenderState) {
        if (original) {
            EntityOutlineRenderer.colorSkyblockEntityOutlines(entityRenderState);
        }
        return original;
    }

    @Inject(method = "destroyBlockProgress", at = @At("HEAD"))
    public void sba$destroyBlockProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        LevelRendererHook.onAddBlockBreakParticle(breakerId, pos, progress);
    }

    @Inject(method = "lambda$addMainPass$0", at = @At("RETURN"))
    public void sba$addMainPassLambda(GpuBufferSlice terrainFog, LevelRenderState levelRenderState, ProfilerFiller profiler, ChunkSectionsToRender chunkSectionsToRender, ResourceHandle<RenderTarget> entityOutlineTarget, ResourceHandle<RenderTarget> translucentTarget, ResourceHandle<RenderTarget> mainTarget, ResourceHandle<RenderTarget> itemEntityTarget, ResourceHandle<RenderTarget> particleTarget, boolean renderOutline, Matrix4fc modelViewMatrix, CallbackInfo ci,
                                      @Local(name = "bufferSource") MultiBufferSource.BufferSource bufferSource, @Local(name = "poseStack") PoseStack poseStack) {
        RenderEvents.LEVEL_LAST.invoker().onRenderLevelLast(bufferSource, poseStack);
    }

}