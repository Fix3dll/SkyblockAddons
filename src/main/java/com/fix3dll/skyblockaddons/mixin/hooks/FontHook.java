package com.fix3dll.skyblockaddons.mixin.hooks;

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

    private static final TextColor textColor = new TextColor(0xFFFFFF, "chroma");
    private static final TextColor textColorOffWhite = new TextColor(0xFFFFFE, "chroma");
    @Getter @Setter private static boolean allTextChroma = false;
    @Getter private static boolean glyphChroma = false;
    @Setter private static boolean haltChroma = false;

    public static void checkIfGlyphIsChroma(BakedSheetGlyph.GlyphInstance glyphInstance) {
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