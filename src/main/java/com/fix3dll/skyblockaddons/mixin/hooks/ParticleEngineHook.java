package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.features.healingcircle.HealingCircleManager;
import com.fix3dll.skyblockaddons.features.healingcircle.HealingCircleParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.player.LocalPlayer;

public class ParticleEngineHook {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

//    private static final Set<OverlayEffectRenderer> effectRenderers = new HashSet<>();

    public static void onAddParticle(Particle particle) {
        LocalPlayer player = MC.player;

        if (main.getUtils().isOnSkyblock()) {
            if (main.getUtils().isInDungeon() && Feature.SHOW_HEALING_CIRCLE_WALL.isEnabled()
                    && particle instanceof SuspendedTownParticle && particle.y % 1 == 0.0) {
                HealingCircleManager.addHealingCircleParticle(new HealingCircleParticle(particle.x, particle.z));
            }
//            else if (player != null && player.fishing != null && Feature.FISHING_PARTICLE_OVERLAY.isEnabled()
//                    && particle instanceof WakeParticle wakeParticle) {
//                FishParticleManager.onFishWakeSpawn(wakeParticle);
//            }
        }
    }

//    @Getter
//    public static class OverlayInfo {
//        private final float rotationX;
//        private final float rotationZ;
//        private final float rotationYZ;
//        private final float rotationXY;
//        private final float rotationXZ;
//        private final float partialTicks;
//        private final TextureManager renderer;
//        private final WorldRenderer worldRenderer;
//        private final Entity renderViewEntity;
//
//
//        public OverlayInfo (float thePartialTicks) {
//            rotationX = ActiveRenderInfo.getRotationX();
//            rotationZ = ActiveRenderInfo.getRotationZ();
//            rotationYZ = ActiveRenderInfo.getRotationYZ();
//            rotationXY = ActiveRenderInfo.getRotationXY();
//            rotationXZ = ActiveRenderInfo.getRotationXZ();
//            partialTicks = thePartialTicks;
//            renderer = Minecraft.getMinecraft().effectRenderer.renderer;
//            worldRenderer = Tessellator.getInstance().getWorldRenderer();
//            renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
//        }
//
//    }
//
//    /**
//     * Called every frame directly after particle rendering to overlay modified particles to the screen.
//     * @param partialTicks a float in [0, 1) indicating the progress to the next tick
//     */
//    public static void renderParticleOverlays(float partialTicks) {
//        OverlayInfo info = new OverlayInfo(partialTicks);
//
//        for (OverlayEffectRenderer renderer : effectRenderers) {
//            renderer.renderOverlayParticles(info);
//        }
//    }
//
//
//    /**
//     * Called from {@link OverlayEffectRenderer} during object initialization to render the registered particles every frame.
//     * @param renderer the attached renderer
//     */
//    public static void registerOverlay(OverlayEffectRenderer renderer) {
//        effectRenderers.add(renderer);
//    }
}