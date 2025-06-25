package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.chroma.ChromaRenderType;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.Util;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

import java.util.function.Function;

/**
 * Chroma related codes adapted from SkyHanni under LGPL-2.1 license
 * @link <a href="https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE">github.com/hannibal002/SkyHanni/blob/beta/LICENSE</a>
 * @author hannibal2
 */
public class FontHook {

    private static final RenderPipeline CHROMA_TEXT = RenderPipeline.builder(RenderPipelines.MATRICES_SNIPPET)
            .withLocation(SkyblockAddons.resourceLocation("sba_chroma_text"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withVertexShader(SkyblockAddons.resourceLocation("chroma_textured"))
            .withFragmentShader(SkyblockAddons.resourceLocation("chroma_textured"))
            .withSampler("Sampler0")
            .withUniform("chromaSize", UniformType.FLOAT)
            .withUniform("timeOffset", UniformType.FLOAT)
            .withUniform("saturation", UniformType.FLOAT)
            .build();
    private static final Function<ResourceLocation, RenderType> CHROMA_TEXTURED = Util.memoize(
            (texture -> new ChromaRenderType(
                    "sba_chroma_textured",
                    RenderType.SMALL_BUFFER_SIZE,
                    false,
                    false,
                    CHROMA_TEXT,
                    RenderType.CompositeState.builder()
                            .setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
                            .createCompositeState(false)
            ))
    );
    private static final TextColor textColor = new TextColor(0xFFFFFF, "chroma");
    private static final TextColor textColorOffWhite = new TextColor(0xFFFFFE, "chroma");
    @Getter @Setter private static boolean allTextChroma = false;
    @Getter private static boolean glyphChroma = false;
    @Setter private static boolean haltChroma = false;

    public static RenderType getChromaTextured(ResourceLocation identifier) {
        return CHROMA_TEXTURED.apply(identifier);
    }

    public static void checkIfGlyphIsChroma(BakedGlyph.GlyphInstance glyphInstance) {
        TextColor color = glyphInstance.style().getColor();
        glyphChroma = color != null && "chroma".equals(color.name);
    }

    public static Style setChromaColorStyle(Style style, String text, char colorCode) {
        if (colorCode == 'z') {
            return Style.EMPTY.withColor(textColor);
        }
        return style;
    }

    public static TextColor forceWhiteTextColorForChroma(TextColor color) {
        if (allTextChroma && !haltChroma) {
            return textColor;
        }

        return color;
    }

    public static Style forceChromaStyleIfNecessary(Style style) {
        if (allTextChroma) {
            return style.withColor(textColorOffWhite);
        }
        return style;
    }

}