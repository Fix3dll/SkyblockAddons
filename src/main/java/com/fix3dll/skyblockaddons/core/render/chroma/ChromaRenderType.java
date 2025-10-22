package com.fix3dll.skyblockaddons.core.render.chroma;

import com.fix3dll.skyblockaddons.mixin.hooks.GuiRendererHook;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeRenderType;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Chroma related codes adapted from SkyHanni under LGPL-2.1 license
 * @link <a href="https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE">github.com/hannibal002/SkyHanni/blob/beta/LICENSE</a>
 * @author hannibal2
 */
public class ChromaRenderType extends CompositeRenderType {

    public ChromaRenderType(
            String name, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, RenderPipeline renderPipeline, RenderType.CompositeState state
    ) {
        super(name, bufferSize, affectsCrumbling, sortOnUpload, renderPipeline, state);
    }

    @Override
    public void draw(MeshData meshData) {
        RenderPipeline renderPipeline = this.pipeline();
        this.setupRenderState();

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(),
                new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                new Vector3f(),
                RenderSystem.getTextureMatrix(),
                RenderSystem.getShaderLineWidth()
        );

        if (GuiRendererHook.chromaBufferSlice == null) {
            GuiRendererHook.computeChromaBufferSlice();
        }

        try {
            GpuBuffer gpuBuffer = renderPipeline.getVertexFormat().uploadImmediateVertexBuffer(meshData.vertexBuffer());
            GpuBuffer gpuBuffer2;
            VertexFormat.IndexType indexType;
            if (meshData.indexBuffer() == null) {
                AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(meshData.drawState().mode());
                gpuBuffer2 = shapeIndexBuffer.getBuffer(meshData.drawState().indexCount());
                indexType = shapeIndexBuffer.type();
            } else {
                gpuBuffer2 = renderPipeline.getVertexFormat().uploadImmediateIndexBuffer(meshData.indexBuffer());
                indexType = meshData.drawState().indexType();
            }

            RenderTarget framebuffer = this.state.outputState.getRenderTarget();
            GpuTextureView colorAttachment = framebuffer.getColorTextureView();
            GpuTextureView depthAttachment = framebuffer.useDepth ? framebuffer.getDepthTextureView() : null;

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "SBA Immediate Chroma Pipeline Draw",
                    colorAttachment, OptionalInt.empty(),
                    depthAttachment, OptionalDouble.empty()
            )) {

                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                renderPass.setUniform("ChromaUniforms", GuiRendererHook.chromaBufferSlice);

                renderPass.setPipeline(renderPipeline);
                renderPass.setVertexBuffer(0, gpuBuffer);

                ScissorState scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
                if (scissorState.enabled()) {
                    scissorState.enable(scissorState.x(), scissorState.y(), scissorState.width(), scissorState.height());
                }

                for (int i = 0; i <= 11; i++) {
                    GpuTextureView gpuTextureView = RenderSystem.getShaderTexture(i);
                    if (gpuTextureView != null) {
                        renderPass.bindSampler("Sampler" + i, gpuTextureView);
                    }
                }

                renderPass.setIndexBuffer(gpuBuffer2, indexType);
                renderPass.drawIndexed(0, 0, meshData.drawState().indexCount(), 1);
            }
        } catch (Throwable ex) {
            try {
                meshData.close();
            } catch (Throwable ex2) {
                ex.addSuppressed(ex2);
            }

            throw ex;
        }

        meshData.close();
        this.clearRenderState();

    }

}