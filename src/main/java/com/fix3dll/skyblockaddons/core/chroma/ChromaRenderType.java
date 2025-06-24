package com.fix3dll.skyblockaddons.core.chroma;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeRenderType;

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
        RenderPipeline renderPipeline = this.getRenderPipeline();
        this.setupRenderState();

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
            GpuTexture colorAttachment = framebuffer.getColorTexture();
            GpuTexture depthAttachment = framebuffer.useDepth ? framebuffer.getDepthTexture() : null;

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    colorAttachment, OptionalInt.empty(),
                    depthAttachment, OptionalDouble.empty()
            )) {
                // Set custom chroma uniforms
                float chromaSize = Feature.CHROMA_SIZE.numberValue().floatValue() * (Minecraft.getInstance().getWindow().getWidth() / 100F);
                float ticks = (float) SkyblockAddons.getInstance().getScheduler().getTotalTicks() + Utils.getPartialTicks();
                float chromaSpeed = Feature.CHROMA_SPEED.numberValue().floatValue() / 360F;
                float timeOffset = ticks * chromaSpeed;
                float saturation = Feature.CHROMA_SATURATION.numberValue().floatValue();

                renderPass.setUniform("chromaSize", chromaSize);
                renderPass.setUniform("timeOffset", timeOffset);
                renderPass.setUniform("saturation", saturation);

                renderPass.setPipeline(renderPipeline);
                renderPass.setVertexBuffer(0, gpuBuffer);

                if (RenderSystem.SCISSOR_STATE.isEnabled()) {
                    renderPass.enableScissor(RenderSystem.SCISSOR_STATE);
                }

                for (int i = 0; i <= 11; i++) {
                    GpuTexture gpuTexture = RenderSystem.getShaderTexture(i);
                    if (gpuTexture != null) {
                        renderPass.bindSampler("Sampler" + i, gpuTexture);
                    }
                }

                renderPass.setIndexBuffer(gpuBuffer2, indexType);
                renderPass.drawIndexed(0, meshData.drawState().indexCount());
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