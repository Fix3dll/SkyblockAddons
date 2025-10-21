package com.fix3dll.skyblockaddons.core.render.chroma;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.renderer.DynamicUniformStorage;

import java.nio.ByteBuffer;

/**
 * Chroma related codes adapted from SkyHanni under LGPL-2.1 license
 * @link <a href="https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE">github.com/hannibal002/SkyHanni/blob/beta/LICENSE</a>
 * @author hannibal2
 */
public class ChromaUniform implements AutoCloseable {

    private final int UNIFORM_SIZE = new Std140SizeCalculator().putFloat().putFloat().get();

    private final DynamicUniformStorage<UniformValue> storage = new DynamicUniformStorage<>("SBA Chroma UBO", UNIFORM_SIZE, 2);

    public GpuBufferSlice writeWith(Float chromaSize, Float timeOffset, Float saturation) {
        return storage.writeUniform(
                new UniformValue(chromaSize, timeOffset, saturation)
        );
    }

    // Imperative to clear DynamicUniformStorage every frame.
    // Handled in MixinRenderSystem.
    public void endFrame() {
        storage.endFrame();
    }

    @Override
    public void close() throws Exception {
        storage.close();
    }

    record UniformValue(Float chromaSize, Float timeOffset, Float saturation) implements DynamicUniformStorage.DynamicUniform{
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                    .putFloat(chromaSize)
                    .putFloat(timeOffset)
                    .putFloat(saturation);
        }
    }

}