package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.GuiRendererHook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * Chroma related codes adapted from SkyHanni under LGPL-2.1 license
 * @link <a href="https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE">github.com/hannibal002/SkyHanni/blob/beta/LICENSE</a>
 * @author hannibal2
 */
@Mixin(GuiRenderer.class)
public class GuiRendererMixin {

    @Inject(method = "executeDrawRange", at = @At("HEAD"))
    public void computeChromaBufferSlice(Supplier<String> debugGroup,
                                         RenderTarget renderTarget,
                                         GpuBufferSlice fogUniforms,
                                         GpuBufferSlice dynamicTransforms,
                                         GpuBuffer buffer,
                                         VertexFormat.IndexType indexType,
                                         int start,
                                         int end,
                                         CallbackInfo ci) {
        GuiRendererHook.computeChromaBufferSlice();
    }

    @Inject(method = "executeDrawRange", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))
    public void insertChromaSetUniform(Supplier<String> debugGroup,
                                       RenderTarget renderTarget,
                                       GpuBufferSlice fogUniforms,
                                       GpuBufferSlice dynamicTransforms,
                                       GpuBuffer buffer,
                                       VertexFormat.IndexType indexType,
                                       int start,
                                       int end,
                                       CallbackInfo ci,
                                       @Local RenderPass renderPass) {
        GuiRendererHook.insertChromaSetUniform(renderPass);
    }

    @WrapOperation(method = "addElementToMesh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/state/GuiElementRenderState;pipeline()Lcom/mojang/blaze3d/pipeline/RenderPipeline;"))
    public RenderPipeline replacePipeline(GuiElementRenderState state, Operation<RenderPipeline> original) {
        return GuiRendererHook.replacePipeline(state, original);
    }

}