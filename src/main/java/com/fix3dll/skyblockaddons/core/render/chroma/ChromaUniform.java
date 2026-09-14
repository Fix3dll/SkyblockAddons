package com.fix3dll.skyblockaddons.core.render.chroma;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.DynamicGpuDataStorage;
import net.minecraft.client.renderer.DynamicGpuDataStorageMapped;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

/**
 * Chroma related codes adapted from SkyHanni under LGPL-2.1 license
 * @link <a href="https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE">github.com/hannibal002/SkyHanni/blob/beta/LICENSE</a>
 * @author hannibal2
 */
public class ChromaUniform implements AutoCloseable {

    private final int UNIFORM_SIZE = new Std140SizeCalculator().putFloat().putFloat().putFloat().get();

    private final DynamicGpuDataStorageMapped<UniformValue> storage = new DynamicGpuDataStorageMapped<>("SBA Chroma UBO", UNIFORM_SIZE, 128, 2);

    public GpuBufferSlice writeWith(Float chromaSize, Float timeOffset, Float saturation) {
        return storage.writeData(
                new UniformValue(chromaSize, timeOffset, saturation)
        );
    }

    // Imperative to clear DynamicUniformStorage every frame.
    // Handled in MixinRenderSystem.
    public void endFrame() {
        storage.endFrame();
    }

    @Override
    public void close() {
        storage.close();
    }

    record UniformValue(Float chromaSize, Float timeOffset, Float saturation) implements DynamicGpuDataStorage.DynamicGpuData{
        @Override
        public void write(@NonNull ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                    .putFloat(chromaSize)
                    .putFloat(timeOffset)
                    .putFloat(saturation);
        }
    }

}