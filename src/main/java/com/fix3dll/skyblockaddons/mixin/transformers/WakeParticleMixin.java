package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.extensions.WakeParticleExtension;
import com.fix3dll.skyblockaddons.mixin.hooks.WakeParticleHook;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.particle.WakeParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WakeParticle.class)
public class WakeParticleMixin implements WakeParticleExtension {

    @Unique private boolean sba$blankSprite = false;

    @Override
    public void sba$setBlankSprite(boolean shouldBlank) {
        this.sba$blankSprite = shouldBlank;
    }

    @Override
    public boolean sba$isBlankSprite() {
        return this.sba$blankSprite;
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/WakeParticle;setSprite(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    public TextureAtlasSprite sba$tick_setSprite(TextureAtlasSprite original, @Local int i) {
        if (!sba$blankSprite) return original;

        TextureAtlasSprite blankSprite = WakeParticleHook.getBlankSprite(i);
        return blankSprite == null ? original :  blankSprite;
    }

}