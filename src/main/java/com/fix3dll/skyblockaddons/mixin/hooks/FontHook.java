package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.render.chroma.ManualChromaManager;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils.ChromaMode;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Chroma related codes adapted from SkyHanni under LGPL-2.1 license
 * @link <a href="https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE">github.com/hannibal002/SkyHanni/blob/beta/LICENSE</a>
 * @author hannibal2
 */
public class FontHook {

    @Getter @Setter private static boolean allTextChroma = false;
    @Getter private static boolean glyphChroma = false;
    @Setter private static boolean haltChroma = false;

    public static void checkIfGlyphIsChroma(BakedSheetGlyph.GlyphInstance glyphInstance) {
        TextColor color = glyphInstance.style().getColor();
        glyphChroma = color != null && DrawUtils.CHROMA_TEXT_COLOR.name.equals(color.name);
    }

    public static Style setChromaColorStyle(Style style, String text, char colorCode) {
        if (colorCode == ColorCode.CHROMA.getCode()) {
            if (Feature.CHROMA_MODE.getValue() == ChromaMode.FADE) {
                style.withColor(DrawUtils.CHROMA_TEXT_COLOR);
            } else {
                style.withColor(new TextColor(ManualChromaManager.getChromaColor(0, 0, 255), DrawUtils.CHROMA_TEXT_COLOR.name));
            }
        }
        return style;
    }

    public static Style forceChromaStyle(Style original) {
        if (haltChroma) {
            return original;
        }

        if (allTextChroma) {
            if (Feature.CHROMA_MODE.getValue() == ChromaMode.FADE) {
                return original.withColor(DrawUtils.CHROMA_TEXT_COLOR);
            } else {
                return original.withColor(new TextColor(ManualChromaManager.getChromaColor(0, 0, 255), DrawUtils.CHROMA_TEXT_COLOR.name));
            }
        }

        TextColor textColor = original.getColor();
        boolean chroma = textColor != null && DrawUtils.CHROMA_TEXT_COLOR.name.equals(textColor.name);

        if (chroma && Feature.CHROMA_MODE.getValue() == ChromaMode.ALL_SAME_COLOR) {
            return original.withColor(
                    new TextColor(ManualChromaManager.getChromaColor(0, 0, 255), DrawUtils.CHROMA_TEXT_COLOR.name)
            );
        }

        return original;
    }

}