package com.fix3dll.skyblockaddons.features.healingcircle;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.MathUtils;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Getter;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Set;

import static com.fix3dll.skyblockaddons.core.feature.FeatureSetting.HEALING_CIRCLE_OPACITY;

public class HealingCircleManager {

    public static final RenderPipeline HEALING_CIRCLE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
                    .withLocation("pipeline/healing_circle")
                    .withVertexShader("core/position_color")
                    .withFragmentShader("core/position_color")
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withCull(false)
                    .withDepthWrite(true)
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                    .build()
    );

    private static final RenderType HEALING_CIRCLE = RenderType.create(
            "healing_circle",
            256,
            true,
            true,
            HEALING_CIRCLE_PIPELINE,
            RenderType.CompositeState.builder()
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setOutputState(RenderType.TRANSLUCENT_TARGET)
                    .createCompositeState(false)
    );

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    @Getter private static final Set<HealingCircle> healingCircles = Sets.newConcurrentHashSet();

    public static void addHealingCircleParticle(HealingCircleParticle healingCircleParticle) {
        HealingCircle nearbyHealingCircle = null;
        for (HealingCircle healingCircle : healingCircles) {
            if (healingCircle.hasCachedCenterPoint()) {
                Point2D.Double circleCenter = healingCircle.getCircleCenter();
                if (healingCircleParticle.getPoint().distance(circleCenter.getX(), circleCenter.getY()) < (HealingCircle.getDiameter() + 2) / 2F) {
                    nearbyHealingCircle = healingCircle;
                    break;
                }
            } else {
                if (healingCircleParticle.getPoint().distance(healingCircle.getAverageX(), healingCircle.getAverageZ()) < HealingCircle.getDiameter() + 2) {
                    nearbyHealingCircle = healingCircle;
                    break;
                }
            }
        }

        if (nearbyHealingCircle != null) {
            nearbyHealingCircle.addPoint(healingCircleParticle);
        } else {
            healingCircles.add(new HealingCircle(healingCircleParticle));
        }
    }

    public static void renderHealingCircleOverlays(WorldRenderContext worldRenderContext) {
        Feature feature = Feature.SHOW_HEALING_CIRCLE_WALL;
        if (main.getUtils().isOnSkyblock() && feature.isEnabled()) {

            Iterator<HealingCircle> healingCircleIterator = healingCircles.iterator();
            while (healingCircleIterator.hasNext()) {
                HealingCircle healingCircle = healingCircleIterator.next();

                healingCircle.removeOldParticles();
                if (System.currentTimeMillis() - healingCircle.getCreation() > 1000 && healingCircle.getParticlesPerSecond() < 10) {
                    healingCircleIterator.remove();
                    continue;
                }

                Point2D.Double circleCenter = healingCircle.getCircleCenter();
                if (circleCenter != null && !Double.isNaN(circleCenter.getX()) && !Double.isNaN(circleCenter.getY())) {
                    PoseStack poseStack = worldRenderContext.matrixStack();
                    MultiBufferSource source = worldRenderContext.consumers();
                    if (poseStack == null || source == null) continue;
//                    poseStack.pushPose();
//                    GL11.glNormal3f(0.0F, 1.0F, 0.0F);

//                    GlStateManager.disableLighting();
//                    GlStateManager.depthMask(false);
//                    GlStateManager.enableDepth();
//                    GlStateManager.enableBlend();
//                    GlStateManager.depthFunc(GL11.GL_LEQUAL);
//                    GlStateManager.disableCull();
//                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//                    GlStateManager.enableAlpha();
//                    GlStateManager.disableTexture2D();

                    boolean chroma = feature.isChroma();
                    int color = feature.getColor(
                            ColorUtils.getAlphaIntFromFloat(
                                    MathUtils.clamp(
                                            feature.getAsNumber(HEALING_CIRCLE_OPACITY).floatValue(), 0, 1
                                    )
                            )
                    );
                    DrawUtils.drawCylinder(poseStack, source.getBuffer(HEALING_CIRCLE), circleCenter.getX(), 0, circleCenter.getY(), HealingCircle.getRadius(), 255, ColorUtils.getDummySkyblockColor(color, chroma));

//                    GlStateManager.enableCull();
//                    GlStateManager.enableTexture2D();
//                    GlStateManager.enableDepth();
//                    GlStateManager.depthMask(true);
//                    GlStateManager.enableLighting();
//                    GlStateManager.disableBlend();
//                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//                    poseStack.popPose();
                }
            }
        }
    }
}