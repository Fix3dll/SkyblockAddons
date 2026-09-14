package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.GuiRendererHook;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PreparedRenderType.class)
public class PreparedRenderTypeMixin {

    @Inject(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/renderpearl/api/commands/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void sba$bindChromaUniforms(StagedVertexBuffer.ExecuteInfo info,
                                        RenderPass renderPass,
                                        RenderPipeline renderPipeline,
                                        CallbackInfo ci) {
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