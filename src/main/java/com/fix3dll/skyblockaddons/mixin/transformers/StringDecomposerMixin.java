package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.FontHook;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StringDecomposer.class)
public class StringDecomposerMixin {

    @ModifyVariable(method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/ChatFormatting;getByCode(C)Lnet/minecraft/ChatFormatting;"), ordinal = 2)
    private static Style sba$iterateFormatted(Style value, @Local(argsOnly = true) String text, @Local(ordinal = 0) char colorCode) {
        return FontHook.setChromaColorStyle(value, text, Character.toLowerCase(colorCode));
    }

}