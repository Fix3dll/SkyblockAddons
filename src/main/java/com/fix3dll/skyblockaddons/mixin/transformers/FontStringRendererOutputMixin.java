package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.FontHook;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Font.StringRenderOutput.class)
public class FontStringRendererOutputMixin {

    @Inject(method = "renderCharacters", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph$GlyphInstance;glyph()Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;"))
    private void sba$checkIfGlyphIsChroma(CallbackInfo ci, @Local BakedGlyph.GlyphInstance drawnGlyph) {
        FontHook.checkIfGlyphIsChroma(drawnGlyph);
    }

    @ModifyVariable(method = "accept", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/chat/Style;getColor()Lnet/minecraft/network/chat/TextColor;"))
    private TextColor sba$forceWhiteTextColorForChroma(TextColor color) {
        return FontHook.forceWhiteTextColorForChroma(color);
    }

    @ModifyArg(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph$GlyphInstance;<init>(FFIILnet/minecraft/client/gui/font/glyphs/BakedGlyph;Lnet/minecraft/network/chat/Style;FF)V"))
    private Style sba$forceChromaIfNecessary(Style style) {
        return FontHook.forceChromaStyleIfNecessary(style);
    }

}