package com.fix3dll.skyblockaddons.core.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import static net.minecraft.util.Mth.HALF_PI;

public record CornerRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x,
        float y,
        float radius,
        RoundedRectCorner corner,
        int color,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    public CornerRenderState {
        pose = new Matrix3x2f(pose);
    }
    public CornerRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x,
            float y,
            float radius,
            RoundedRectCorner corner,
            int color,
            @Nullable ScreenRectangle scissorArea
    ) {
        this(pipeline, textureSetup, pose, x, y, radius, corner, color, scissorArea, getBounds(Math.round(x), Math.round(y), Math.round(x + radius), Math.round(y + radius), pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        int segments = 64;
        double angleStep = HALF_PI / (float) segments;
        double startAngle = corner.startAngle;

        float prevX = x + (float) Math.cos(startAngle) * this.radius;
        float prevY = y + (float) Math.sin(startAngle) * radius;

        for (int segment = 0; segment <= segments; segment++) {
            double angle = startAngle - angleStep * segment;

            float newX = x + (float) (Math.cos(angle) * radius);
            float newY = y + (float) (Math.sin(angle) * radius);

            consumer.addVertexWith2DPose(pose, newX, newY).setColor(color);
        }

        // Draw final empty triangle
        consumer.addVertexWith2DPose(pose, x, y).setColor(color);
        consumer.addVertexWith2DPose(pose, prevX, prevY).setColor(color);
    }

    @Nullable
    private static ScreenRectangle getBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle screenRectangle = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }

    @AllArgsConstructor
    public enum RoundedRectCorner {
        TOP_LEFT(-HALF_PI),
        TOP_RIGHT(0),
        BOTTOM_LEFT(HALF_PI),
        BOTTOM_RIGHT(Math.PI);

        private final double startAngle;
    }

}