package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.extensions.WakeParticleExtension;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Particle.class)
public class ParticleMixin {

    @Inject(method = "getLightColor", at = @At("HEAD"), cancellable = true)
    public void sba$getLightColor(float partialTick, CallbackInfoReturnable<Integer> cir) {
        Particle particle = (Particle)(Object)this;
        if (particle instanceof WakeParticleExtension wpe && wpe.sba$isBlankSprite()) {
            cir.setReturnValue(LightTexture.FULL_BRIGHT);
        }
    }

}