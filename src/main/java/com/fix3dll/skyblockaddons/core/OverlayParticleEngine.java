package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.mixin.hooks.ParticleEngineHook;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.util.profiling.Profiler;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

public class OverlayParticleEngine {

    private static final List<ParticleRenderType> RENDER_ORDER = List.of(
            ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
    );

    private final Map<ParticleRenderType, Queue<Particle>> particles = Maps.newIdentityHashMap();
    private final Queue<TrackingEmitter> trackingEmitters = Queues.newArrayDeque();
    private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
    private final Object2IntOpenHashMap<ParticleGroup> trackedParticleCounts = new Object2IntOpenHashMap<>();

    public OverlayParticleEngine() {
        ParticleEngineHook.registerOverlay(this);
    }

    public void tick() {
        this.particles.forEach((particleRenderType, queue) -> {
            Profiler.get().push(particleRenderType.toString());
            this.tickParticleList(queue);
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
                this.particles.computeIfAbsent(particle.getRenderType(), particleRenderType -> EvictingQueue.create(16384)).add(particle);
            }
        }
    }

    private void tickParticleList(Collection<Particle> particles) {
        if (!particles.isEmpty()) {
            Iterator<Particle> iterator = particles.iterator();

            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                this.tickParticle(particle);
                if (!particle.isAlive()) {
                    particle.getParticleGroup().ifPresent(particleGroup -> this.updateCount(particleGroup, -1));
                    iterator.remove();
                }
            }
        }
    }

    protected void tickParticle(Particle particle) {
        try {
            particle.tick();
        } catch (Throwable var5) {
            CrashReport crashReport = CrashReport.forThrowable(var5, "Ticking Particle");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being ticked");
            crashReportCategory.setDetail("Particle", particle::toString);
            crashReportCategory.setDetail("Particle Type", particle.getRenderType()::toString);
            throw new ReportedException(crashReport);
        }
    }

    public void addParticle(Particle effect) {
        Optional<ParticleGroup> optional = effect.getParticleGroup();
        if (optional.isPresent()) {
            if (this.hasSpaceInParticleLimit(optional.get())) {
                this.particlesToAdd.add(effect);
                this.updateCount(optional.get(), 1);
            }
        } else {
            this.particlesToAdd.add(effect);
        }
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

    private boolean hasSpaceInParticleLimit(ParticleGroup group) {
        return this.trackedParticleCounts.getInt(group) < group.getLimit();
    }

    private void updateCount(ParticleGroup group, int count) {
        this.trackedParticleCounts.addTo(group, count);
    }

    public void render(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource) {
        for (ParticleRenderType particleRenderType : RENDER_ORDER) {
            Queue<Particle> queue = this.particles.get(particleRenderType);
            if (queue != null && !queue.isEmpty()) {
                renderParticleType(camera, partialTick, bufferSource, particleRenderType, queue);
            }
        }

        Queue<Particle> queue2 = this.particles.get(ParticleRenderType.CUSTOM);
        if (queue2 != null && !queue2.isEmpty()) {
            renderCustomParticles(camera, partialTick, bufferSource, queue2);
        }
    }

    private void renderParticleType(
            Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleType, Queue<Particle> particles
    ) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(Objects.requireNonNull(particleType.renderType()));

        for (Particle particle : particles) {
            try {
                this.setupRenderEffect(particle);
                particle.render(vertexConsumer, camera, partialTick);
                this.endRenderEffect(particle);
            } catch (Throwable var11) {
                CrashReport crashReport = CrashReport.forThrowable(var11, "Rendering SBA Particle");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being rendered");
                crashReportCategory.setDetail("Particle", particle::toString);
                crashReportCategory.setDetail("Particle Type", particleType::toString);
                throw new ReportedException(crashReport);
            }
        }
    }

    private void renderCustomParticles(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource, Queue<Particle> particles) {
        PoseStack poseStack = new PoseStack();

        for (Particle particle : particles) {
            try {
                this.setupRenderEffect(particle);
                particle.renderCustom(poseStack, bufferSource, camera, partialTick);
                this.endRenderEffect(particle);
            } catch (Throwable var10) {
                CrashReport crashReport = CrashReport.forThrowable(var10, "Rendering SBA Particle");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Particle being rendered");
                crashReportCategory.setDetail("Particle", particle::toString);
                crashReportCategory.setDetail("Particle Type", "Custom");
                throw new ReportedException(crashReport);
            }
        }
    }

}