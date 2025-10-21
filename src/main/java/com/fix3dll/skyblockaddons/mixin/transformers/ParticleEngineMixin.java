package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.hooks.ParticleEngineHook;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.ParticlesRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(method = "add", at = @At("HEAD"))
    private void sba$addEffect(Particle effect, CallbackInfo ci) {
        ParticleEngineHook.onAddParticle(effect);
    }

    @Inject(method = "extract", at = @At("RETURN"))
    public void sba$extract(ParticlesRenderState reusedState, Frustum frustum, Camera camera, float partialTick, CallbackInfo ci) {
        ParticleEngineHook.extractParticleOverlays(reusedState, frustum, camera, partialTick);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void sba$tick(CallbackInfo ci) {
        ParticleEngineHook.tickParticleOverlays();
    }

}