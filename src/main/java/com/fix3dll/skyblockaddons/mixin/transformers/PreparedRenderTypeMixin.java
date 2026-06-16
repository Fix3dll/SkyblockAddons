package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.GuiRendererHook;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PreparedRenderType.class)
public class PreparedRenderTypeMixin {

    @Inject(
            method = "drawFromBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/IndexType;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void sba$bindChromaUniforms(
            GpuBuffer vertexBuffer, GpuBuffer indexBuffer, IndexType indexType,
            int baseVertex, int firstIndex, int indexCount,
            CallbackInfo ci,
            @Local(name = "renderPass") RenderPass renderPass
    ) {
        RenderPipeline pipeline = ((PreparedRenderType) (Object) this).pipeline();
        if (pipeline != DrawUtils.CHROMA_TEXT && pipeline != DrawUtils.CHROMA_STANDARD) {
            return;
        }
        if (GuiRendererHook.chromaBufferSlice == null) {
            GuiRendererHook.computeChromaBufferSlice();
        }
        renderPass.setUniform("ChromaUniforms", GuiRendererHook.chromaBufferSlice);
    }

}