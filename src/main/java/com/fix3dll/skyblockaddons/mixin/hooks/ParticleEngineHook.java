package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.OverlayParticleEngine;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.features.fishParticles.FishParticleManager;
import com.fix3dll.skyblockaddons.features.healingcircle.HealingCircleManager;
import com.fix3dll.skyblockaddons.features.healingcircle.HealingCircleParticle;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.particle.WakeParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.HashSet;

public class ParticleEngineHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    private static final HashSet<OverlayParticleEngine> effectRenderers = new HashSet<>();

    public static void onAddParticle(Particle particle) {
        LocalPlayer player = MC.player;

        if (player != null && main.getUtils().isOnSkyblock()) {
            if (main.getUtils().isInDungeon() && Feature.SHOW_HEALING_CIRCLE_WALL.isEnabled()
                    && particle instanceof SuspendedTownParticle && particle.y % 1 == 0.0) {
                HealingCircleManager.addHealingCircleParticle(new HealingCircleParticle(particle.x, particle.z));
            } else if (player.fishing != null && Feature.FISHING_PARTICLE_OVERLAY.isEnabled()
                    && particle instanceof WakeParticle wakeParticle) {
                FishParticleManager.onFishWakeSpawn(wakeParticle);
            }
        }
    }

    /**
     * Called every frame directly after particle rendering to overlay modified particles to the screen.
     */
    public static void tickParticleOverlays() {
        for (OverlayParticleEngine renderer : effectRenderers) {
            renderer.tick();
        }
    }

    /**
     * Called every frame directly after particle rendering to overlay modified particles to the screen.
     */
    public static void renderParticleOverlays(Camera camera, float partialTick, MultiBufferSource.BufferSource bufferSource) {
        for (OverlayParticleEngine renderer : effectRenderers) {
            renderer.render(camera, partialTick, bufferSource);
        }
    }

    /**
     * Called from {@link OverlayParticleEngine} during object initialization to render the registered particles every frame.
     * @param renderer the attached renderer
     */
    public static void registerOverlay(OverlayParticleEngine renderer) {
        effectRenderers.add(renderer);
    }

}