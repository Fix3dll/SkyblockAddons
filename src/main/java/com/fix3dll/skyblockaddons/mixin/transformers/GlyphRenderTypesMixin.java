package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.FontHook;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlyphRenderTypes.class)
public class GlyphRenderTypesMixin {

    @Unique
    private ResourceLocation sba$identifier;

    @ModifyReturnValue(method = { "createForColorTexture", "createForIntensityTexture" }, at = @At("RETURN"))
    private static GlyphRenderTypes sba$createForColorTextureMethods(GlyphRenderTypes original, @Local(argsOnly = true) ResourceLocation id) {
        ((GlyphRenderTypesMixin) (Object) original).sba$identifier = id;
        return original;
    }

    @Inject(method = "select", at = @At("HEAD"), cancellable = true)
    public void sba$select(Font.DisplayMode displayMode, CallbackInfoReturnable<RenderType> cir) {
        if (FontHook.isGlyphChroma()) {
            cir.setReturnValue(FontHook.getChromaTextured(sba$identifier));
        }
    }

}