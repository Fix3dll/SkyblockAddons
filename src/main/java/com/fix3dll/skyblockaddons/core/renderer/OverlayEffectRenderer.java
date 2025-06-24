package com.fix3dll.skyblockaddons.core.renderer;//package com.fix3dll.skyblockaddons.core.renderer;
//
//import com.fix3dll.skyblockaddons.SkyblockAddons;
//import com.fix3dll.skyblockaddons.core.feature.Feature;
//import com.fix3dll.skyblockaddons.mixin.hooks.EffectRendererHook;
//import com.fix3dll.skyblockaddons.utils.SkyblockColor;
//import net.minecraft.client.particle.Particle;
//
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;
//
///**
// * This class is made to extend the functionality of {@link net.minecraft.client.particle.ParticleManager} to integrate Skyblock Addons features and logic.
// * The class is meant to provide the foundation (super class) for individual features that want to integrate particle/effect rendering into their logic.
// * See TODO for an example implementation.
// * The end goal of this module is to embrace a feature-driven decentralized design, while providing easy access to particle/effect rendering.
// */
//public class OverlayEffectRenderer {
//
////    protected static DrawState3D DRAW_PARTICLE = new DrawState3D(new SkyblockColor(0xFFFFFFFF), 7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP, true, true);
//    private Set<Particle>[][] overlayParticles;
//    protected Feature feature = null;
//
//    @SuppressWarnings("unchecked")
//    private void initParticles() {
//        this.overlayParticles = new Set[4][2];
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 2; j++) {
//                overlayParticles[i][j] = new HashSet<>();
//            }
//        }
//    }
//
//    public OverlayEffectRenderer() {
//        initParticles();
//        EffectRendererHook.registerOverlay(this);
//    }
//
//    public void addParticle(Particle particle) {
//        if (particle == null) {
//            return;
//        }
//        int i = particle.getFXLayer();
//        int j = particle.getAlpha() != 1.0F ? 0 : 1;
//
//        if (overlayParticles[i][j].size() >= 100)
//        {
//            Iterator<Particle> itr = overlayParticles[i][j].iterator();
//            itr.next();
//            itr.remove();
//        }
//        overlayParticles[i][j].add(particle);
//    }
//
//    public void clearParticles() {
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 2; j++) {
//                overlayParticles[i][j].clear();
//            }
//        }
//    }
//
//    /**
//     * Whether we should render the overlay on this frame. Return {@code true} to render
//     */
//    public boolean shouldRenderOverlay() {
//        return SkyblockAddons.getInstance().getUtils().isOnSkyblock();
//    }
//
//
//    /**
//     * Setup the overlay render environment. Called once in {@link #renderOverlayParticles(EffectRendererHook.OverlayInfo)}, before all rendering occurs.
//     * Override this in a subclass to set up the render environment for a given feature. Defaults to using the previous color.
//     */
//    public void setupRenderEnvironment() {
//        if (feature != null) {
//            DRAW_PARTICLE.setColor(SkyblockAddons.getInstance().getConfigValues().getSkyblockColor(feature)).newColorEnv();
//        }
//    }
//
//
//    /**
//     * End the render environment. Called once in {@link #renderOverlayParticles(EffectRendererHook.OverlayInfo)}, after all rendering occurs.
//     */
//    public void endRenderEnvironment() {
//        DRAW_PARTICLE.endColorEnv();
//    }
//
//
//    /**
//     * Called directly before rendering an effect
//     * @param effect the effect about to be rendered
//     */
//    public void setupRenderEffect(EntityFX effect) {
//    }
//
//
//    /**
//     * Called directly after rendering an effect
//     * @param effect the effect that was just rendered
//     */
//    public void endRenderEffect(EntityFX effect) {
//    }
//
//
//    /**
//     * Main method to render particles
//     * @param info setup information used to render the particle overlay
//     */
//    public void renderOverlayParticles(EffectRendererHook.OverlayInfo info) {
//        if (!shouldRenderOverlay()) {
//            return;
//        }
//        float partialTicks = info.getPartialTicks();
//        float rotationX = info.getRotationX();
//        float rotationZ = info.getRotationZ();
//        float rotationYZ = info.getRotationYZ();
//        float rotationXY = info.getRotationXY();
//        float rotationXZ = info.getRotationXZ();
//        TextureManager renderer = info.getRenderer();
//        Entity entity = info.getRenderViewEntity();
//        WorldRenderer worldRenderer = info.getWorldRenderer();
//
//        ResourceLocation particleTextures = EffectRenderer.particleTextures;
//
//        setupRenderEnvironment();
//
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 2; j++) {
//                overlayParticles[i][j].removeIf(entityFX -> entityFX.isDead);
//                if (!overlayParticles[i][j].isEmpty()) {
//                    GlStateManager.depthMask(j == 1);
//                    if (i == 1) {
//                        renderer.bindTexture(TextureMap.locationBlocksTexture);
//                    } else {
//                        renderer.bindTexture(particleTextures);
//                    }
//
//                    GlStateManager.enableColorMaterial();
//                    for (EntityFX effect : overlayParticles[i][j]) {
//                        try {
//                            // Set up the outline color
//                            DRAW_PARTICLE.beginWorldRenderer().bindColor((float)effect.posX, (float)effect.posY, (float)effect.posZ);
//
//                            setupRenderEffect(effect);
//                            effect.renderParticle(worldRenderer, entity, partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
//                            endRenderEffect(effect);
//
//                            // Send vertices to the GPU
//                            DRAW_PARTICLE.draw();
//                        }
//                        catch (Throwable ex) {
//                            //logger.warn("Couldn't render outline for effect " + effect.toString() + ".");
//                            //logger.catching(ex); // Just move on to the next entity...
//                        }
//                    }
//
//                    GlStateManager.disableColorMaterial();
//                }
//            }
//        }
//        endRenderEnvironment();
//    }
//}
