package com.fix3dll.skyblockaddons.features.fishParticles;

import com.fix3dll.skyblockaddons.core.OverlayParticleEngine;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.ParticlesRenderState;

// TODO: change particle texture with blank texture for RGB
public class FishParticleOverlay extends OverlayParticleEngine {

    private boolean biggerWakeCache;
    private final Feature feature;

    public FishParticleOverlay() {
        super();
        feature = Feature.FISHING_PARTICLE_OVERLAY;
    }

    @Override
    public void tick() {
        if (feature.isDisabled()) return;
        super.tick();
    }

    @Override
    public void extract(ParticlesRenderState reusedState, Frustum frustum, Camera camera, float partialTick) {
        if (feature.isDisabled()) return;
        super.extract(reusedState, frustum, camera, partialTick);
    }

    /**
     * @return {@code true} iff the fishing particle overlay is enabled.
     */
    @Override
    public boolean shouldRenderOverlay() {
        return super.shouldRenderOverlay() && feature.isEnabled();
    }

    /**
     * Setup the render environment for a fish particle.
     * @param particle the fish particle to be rendered
     */
    @Override
    public void setupRenderEffect(Particle particle) {
        biggerWakeCache = feature.isEnabled(FeatureSetting.BIGGER_WAKE);
        if (biggerWakeCache) {
            particle.scale(2.0F);
        }
    }

    /**
     * End the render environment for a fish particle.
     * @param particle the particle that was just rendered
     */
    @Override
    public void endRenderEffect(Particle particle) {
        if (biggerWakeCache) {
            particle.scale(0.5F);
            particle.y -= .1;
            particle.yo -= .1;
        }
    }

}