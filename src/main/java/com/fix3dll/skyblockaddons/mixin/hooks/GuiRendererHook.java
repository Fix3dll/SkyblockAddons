package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.core.render.chroma.ChromaUniform;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.client.gui.render.state.GlyphRenderState;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.network.chat.TextColor;

/**
 * Chroma related codes adapted from SkyHanni under LGPL-2.1 license
 * @link <a href="https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE">github.com/hannibal002/SkyHanni/blob/beta/LICENSE</a>
 * @author hannibal2
 */
public class GuiRendererHook {

    public static GpuBufferSlice chromaBufferSlice;
    @Getter private static final ChromaUniform chromaUniform = new ChromaUniform();

    public static void computeChromaBufferSlice() {
        // Set custom chroma uniforms
        float chromaSize = Feature.CHROMA_SIZE.numberValue().floatValue() * (Minecraft.getInstance().getWindow().getWidth() / 100F);
        float ticks = (float) SkyblockAddons.getInstance().getScheduler().getTotalTicks() + Utils.getPartialTicks();
        float chromaSpeed = Feature.CHROMA_SPEED.numberValue().floatValue() / 360F;
        float timeOffset = ticks * chromaSpeed;
        float saturation = Feature.CHROMA_SATURATION.numberValue().floatValue();

        chromaBufferSlice = chromaUniform.writeWith(chromaSize, timeOffset, saturation);
    }

    // This 'should' be fine being injected into GuiRenderer's render pass since if the bound pipeline's shader doesn't
    // have a uniform with the given name, then the buffer slice will never be bound
    public static void insertChromaSetUniform(RenderPass renderPass) {
        // A very explicit name is given since the uniform will show up in RenderPassImpl's simpleUniforms
        // map, and so it is made clear where this uniform is from
        if (chromaBufferSlice != null) {
            renderPass.setUniform("ChromaUniforms",  chromaBufferSlice);
        }
    }

    public static RenderPipeline replacePipeline(GuiElementRenderState state, Operation<RenderPipeline> original) {
        if (state instanceof GlyphRenderState glyphRenderState
                && glyphRenderState.renderable() instanceof BakedSheetGlyph.GlyphInstance glyphInstance) {
            TextColor glyphColor = glyphInstance.style().getColor();
            if (glyphColor != null && "chroma".equals(glyphColor.name)) {
                return DrawUtils.CHROMA_TEXT;
            }
        }
        return original.call(state);
    }

}