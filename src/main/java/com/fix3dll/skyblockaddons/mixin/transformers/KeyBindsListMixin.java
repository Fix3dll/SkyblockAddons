package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.Translations;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(KeyBindsList.class)
public class KeyBindsListMixin {

    // Fixes translation issues on keybindings
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 1))
    public MutableComponent sba$setKeyTranslation(String key) {
        if (key.contains(SkyblockAddons.MOD_ID)) {
            for (SkyblockKeyBinding binding : SkyblockKeyBinding.values()) {
                if (Objects.equals(binding.getKeyBinding().getName(), key)) {
                    return Component.literal(Translations.getMessage(binding.getTranslationKey()));
                }
            }
        }
        return Component.translatable(key);
    }

}