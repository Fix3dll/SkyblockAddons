package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.FontHook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @WrapOperation(method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Style;getColor()Lnet/minecraft/network/chat/TextColor;"))
    private TextColor sba$forceWhiteTextColorForChroma(Style original, Operation<TextColor> operation) {
        return FontHook.forceWhiteTextColorForChroma(original.getColor());
    }

    @ModifyArg(method = "accept(ILnet/minecraft/network/chat/Style;Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;createGlyph(FFIILnet/minecraft/network/chat/Style;FF)Lnet/minecraft/client/gui/font/TextRenderable;"))
    private Style sba$forceChromaIfNecessary(Style style) {
        return FontHook.forceChromaStyleIfNecessary(style);
    }

}