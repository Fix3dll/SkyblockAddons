package com.fix3dll.skyblockaddons.features.healingcircle;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.MathUtils;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Set;

import static com.fix3dll.skyblockaddons.core.feature.FeatureSetting.HEALING_CIRCLE_OPACITY;

public class HealingCircleManager {

    public static final RenderPipeline HEALING_CIRCLE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder()
                    .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                    .withLocation("sba_healing_circle")
                    .withVertexShader("core/position_color")
                    .withFragmentShader("core/position_color")
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false)
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .build()
    );

    private static final RenderType HEALING_CIRCLE = RenderType.create(
            "sba_healing_circle",
            RenderSetup.builder(HEALING_CIRCLE_PIPELINE)
                    .affectsCrumbling()
                    .sortOnUpload()
                    .createRenderSetup()
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

    public static void renderHealingCircleOverlays(LevelRenderContext endMain) {
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

                    int color = feature.getColor(
                            ColorUtils.getAlphaIntFromFloat(
                                    MathUtils.clamp(
                                            feature.getAsNumber(HEALING_CIRCLE_OPACITY).floatValue(), 0, 1
                                    )
                            )
                    );
                    endMain.submitNodeCollector().submitCustomGeometry(
                            endMain.poseStack(),
                            HEALING_CIRCLE,
                            (pose, buffer) -> DrawUtils.drawCylinder(
                                    pose,
                                    buffer,
                                    circleCenter.getX(),
                                    0,
                                    circleCenter.getY(),
                                    HealingCircle.getRadius(),
                                    255,
                                    ColorUtils.getDummySkyblockColor(color, feature.isChroma())
                            )
                    );
                }
            }
        }
    }

}