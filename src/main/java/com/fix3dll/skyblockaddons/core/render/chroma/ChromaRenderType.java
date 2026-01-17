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
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;

/**
 * Chroma related codes adapted from SkyHanni under LGPL-2.1 license
 * @link <a href="https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE">github.com/hannibal002/SkyHanni/blob/beta/LICENSE</a>
 * @author hannibal2
 */
public class ChromaRenderType extends RenderType {

    public ChromaRenderType(
            String name, RenderSetup state
    ) {
        super(name, state);
    }

    @Override
    public void draw(@NonNull MeshData meshData) {
        RenderPipeline pipeline = this.pipeline();
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        Consumer<Matrix4fStack> consumer = this.state.layeringTransform.getModifier();
        if (consumer != null) {
            matrix4fStack.pushMatrix();
            consumer.accept(matrix4fStack);
        }

        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(),
                new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                new Vector3f(),
                this.state.textureTransform.getMatrix()
        );

        if (GuiRendererHook.chromaBufferSlice == null) {
            GuiRendererHook.computeChromaBufferSlice();
        }

        Map<String, RenderSetup.TextureAndSampler> map = this.state.getTextures();

        try {
            GpuBuffer gpuBuffer = pipeline.getVertexFormat().uploadImmediateVertexBuffer(meshData.vertexBuffer());
            GpuBuffer gpuBuffer2;
            VertexFormat.IndexType indexType;
            if (meshData.indexBuffer() == null) {
                AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(meshData.drawState().mode());
                gpuBuffer2 = autoStorageIndexBuffer.getBuffer(meshData.drawState().indexCount());
                indexType = autoStorageIndexBuffer.type();
            } else {
                gpuBuffer2 = pipeline.getVertexFormat().uploadImmediateIndexBuffer(meshData.indexBuffer());
                indexType = meshData.drawState().indexType();
            }

            RenderTarget renderTarget = this.state.outputTarget.getRenderTarget();
            GpuTextureView colorAttachment = renderTarget.getColorTextureView();
            GpuTextureView depthAttachment = renderTarget.useDepth ? renderTarget.getDepthTextureView() : null;

            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "SBA Immediate Chroma Pipeline Draw",
                    colorAttachment, OptionalInt.empty(),
                    depthAttachment, OptionalDouble.empty()
            )) {
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
                renderPass.setUniform("ChromaUniforms", GuiRendererHook.chromaBufferSlice);

                renderPass.setPipeline(pipeline);
                renderPass.setVertexBuffer(0, gpuBuffer);

                ScissorState scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
                if (scissorState.enabled()) {
                    scissorState.enable(scissorState.x(), scissorState.y(), scissorState.width(), scissorState.height());
                }

                for (Map.Entry<String, RenderSetup.TextureAndSampler> entry : map.entrySet()) {
                    renderPass.bindTexture(entry.getKey(), entry.getValue().textureView(), entry.getValue().sampler());
                }

                renderPass.setIndexBuffer(gpuBuffer2, indexType);
                renderPass.drawIndexed(0, 0, meshData.drawState().indexCount(), 1);
            }
        } catch (Throwable t) {
            try {
                meshData.close();
            } catch (Throwable t2) {
                t.addSuppressed(t2);
            }

            throw t;
        }

        meshData.close();
        if (consumer != null) {
            matrix4fStack.popMatrix();
        }
    }

}