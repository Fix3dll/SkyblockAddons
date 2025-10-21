package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.mixin.hooks.ParticleEngineHook;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ElderGuardianParticleGroup;
import net.minecraft.client.particle.ItemPickupParticleGroup;
import net.minecraft.client.particle.NoRenderParticleGroup;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.QuadParticleGroup;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.ParticlesRenderState;
import net.minecraft.core.particles.ParticleLimit;
import net.minecraft.util.profiling.Profiler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

public class OverlayParticleEngine extends ParticleEngine {

    private static final Minecraft MC = Minecraft.getInstance();

    public static final List<ParticleRenderType> RENDER_ORDER = List.of(
            ParticleRenderType.SINGLE_QUADS, ParticleRenderType.ITEM_PICKUP, ParticleRenderType.ELDER_GUARDIANS
    );

    private final Map<ParticleRenderType, ParticleGroup<?>> particles = Maps.newIdentityHashMap();
    private final Queue<TrackingEmitter> trackingEmitters = Queues.newArrayDeque();
    private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
    private final Object2IntOpenHashMap<ParticleLimit> trackedParticleCounts = new Object2IntOpenHashMap<>();

    public OverlayParticleEngine() {
        super(MC.level, MC.particleResources);
        ParticleEngineHook.registerOverlay(this);
    }

    public void tick() {
        this.particles.forEach((particleRenderType, particleGroup) -> {
            Profiler.get().push(particleRenderType.name());
            particleGroup.tickParticles();
            Profiler.get().pop();
        });
        if (!this.trackingEmitters.isEmpty()) {
            List<TrackingEmitter> list = Lists.newArrayList();

            for (TrackingEmitter trackingEmitter : this.trackingEmitters) {
                trackingEmitter.tick();
                if (!trackingEmitter.isAlive()) {
                    list.add(trackingEmitter);
                }
            }

            this.trackingEmitters.removeAll(list);
        }

        Particle particle;
        if (!this.particlesToAdd.isEmpty()) {
            while ((particle = this.particlesToAdd.poll()) != null) {
                this.particles.computeIfAbsent(particle.getGroup(), this::createParticleGroup).add(particle);
            }
        }
    }

    private ParticleGroup<?> createParticleGroup(ParticleRenderType renderType) {
        if (renderType == ParticleRenderType.ITEM_PICKUP) {
            return new ItemPickupParticleGroup(this);
        } else if (renderType == ParticleRenderType.ELDER_GUARDIANS) {
            return new ElderGuardianParticleGroup(this);
        } else {
            return renderType == ParticleRenderType.NO_RENDER
                    ? new NoRenderParticleGroup(this)
                    : new QuadParticleGroup(this, renderType);
        }
    }

    protected void updateCount(ParticleLimit limit, int count) {
        this.trackedParticleCounts.addTo(limit, count);
    }

    public void add(Particle effect) {
        Optional<ParticleLimit> optional = effect.getParticleLimit();
        if (optional.isPresent()) {
            if (this.hasSpaceInParticleLimit(optional.get())) {
                this.particlesToAdd.add(effect);
                this.updateCount(optional.get(), 1);
            }
        } else {
            this.particlesToAdd.add(effect);
        }
    }

    private boolean hasSpaceInParticleLimit(ParticleLimit limit) {
        return this.trackedParticleCounts.getInt(limit) < limit.limit();
    }

    public void clearParticles() {
        this.particles.clear();
        this.particlesToAdd.clear();
        this.trackingEmitters.clear();
        this.trackedParticleCounts.clear();
    }

    /**
     * Whether we should render the overlay on this frame. Return {@code true} to render
     */
    public boolean shouldRenderOverlay() {
        return SkyblockAddons.getInstance().getUtils().isOnSkyblock();
    }

    /**
     * Called directly before rendering an effect
     * @param effect the effect about to be rendered
     */
    public void setupRenderEffect(Particle effect) {
    }

    /**
     * Called directly after rendering an effect
     * @param effect the effect that was just rendered
     */
    public void endRenderEffect(Particle effect) {
    }

    public void extract(ParticlesRenderState reusedState, Frustum frustum, Camera camera, float partialTick) {
        for (ParticleRenderType particleRenderType : RENDER_ORDER) {
            ParticleGroup<?> particleGroup = this.particles.get(particleRenderType);
            if (particleGroup != null && !particleGroup.isEmpty()) {
                reusedState.add(particleGroup.extractRenderState(frustum, camera, partialTick));
            }
        }
    }

}