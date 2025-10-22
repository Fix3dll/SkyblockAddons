package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.FontHook;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Chroma related codes adapted from SkyHanni under LGPL-2.1 license
 * @link <a href="https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE">github.com/hannibal002/SkyHanni/blob/beta/LICENSE</a>
 * @author hannibal2
 */
@Mixin(Font.PreparedTextBuilder.class)
public class FontPreparedTextBuilderMixin {

    @Inject(method = "visit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font$GlyphVisitor;acceptGlyph(Lnet/minecraft/client/gui/font/TextRenderable;)V"))
    private void sba$checkIfGlyphIsChroma(CallbackInfo ci, @Local TextRenderable textRenderable) {
        if (textRenderable instanceof BakedSheetGlyph.GlyphInstance glyphInstance) {
            FontHook.checkIfGlyphIsChroma(glyphInstance);
        }
    }

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z", at = @At("HEAD"))
    private void sba$forceWhiteTextColorForChroma(int positionInCurrentSequence, Style style, BakedGlyph glyph, CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) LocalRef<Style> styleLocalRef) {
        styleLocalRef.set(FontHook.forceChromaStyle(styleLocalRef.get()));
    }

}