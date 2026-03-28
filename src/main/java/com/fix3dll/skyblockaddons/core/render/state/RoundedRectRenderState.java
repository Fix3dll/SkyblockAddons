package com.fix3dll.skyblockaddons.core.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;

import static net.minecraft.util.Mth.HALF_PI;

public record RoundedRectRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x,
        float y,
        float width,
        float height,
        float radius,
        int color,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {

    public RoundedRectRenderState {
        pose = new Matrix3x2f(pose);
    }

    public RoundedRectRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x,
            float y,
            float width,
            float height,
            float radius,
            int color,
            @Nullable ScreenRectangle scissorArea
    ) {
        this(pipeline, textureSetup, pose, x, y, width, height, radius, color, scissorArea,
                getBounds(Math.round(x), Math.round(y), Math.round(x + width), Math.round(y + height), pose, scissorArea));
    }

    @Override
    public void buildVertices(@NonNull VertexConsumer consumer) {
        // 1. Draw the 3 central filling rectangles (cross shape)
        addQuad(consumer, x + radius, y, x + width - radius, y + height);           // Center vertical
        addQuad(consumer, x, y + radius, x + radius, y + height - radius);          // Left horizontal
        addQuad(consumer, x + width - radius, y + radius, x + width, y + height - radius); // Right horizontal

        // 2. Draw the 4 corners using pie slices converted into QUADS
        drawCorner(consumer, x + radius, y + radius, -HALF_PI);                     // TOP_LEFT
        drawCorner(consumer, x + width - radius, y + radius, 0);           // TOP_RIGHT
        drawCorner(consumer, x + radius, y + height - radius, Math.PI);             // BOTTOM_LEFT
        drawCorner(consumer, x + width - radius, y + height - radius, HALF_PI);     // BOTTOM_RIGHT
    }

    /**
     * Appends 4 vertices to draw a standard rectangle (Quad).
     */
    private void addQuad(VertexConsumer consumer, float minX, float minY, float maxX, float maxY) {
        consumer.addVertexWith2DPose(pose, minX, minY).setColor(color);
        consumer.addVertexWith2DPose(pose, minX, maxY).setColor(color);
        consumer.addVertexWith2DPose(pose, maxX, maxY).setColor(color);
        consumer.addVertexWith2DPose(pose, maxX, minY).setColor(color);
    }

    /**
     * Draws a radial corner.
     * Since we are using QUADS, each "slice" of the pie is defined by 4 vertices:
     * the center, two points on the arc, and a duplicate of the center point to satisfy the Quad requirement.
     */
    private void drawCorner(VertexConsumer consumer, float centerX, float centerY, double startAngle) {
        int segments = 16;
        double angleStep = HALF_PI / (float) segments;

        for (int i = 0; i < segments; i++) {
            double angle1 = startAngle - angleStep * i;
            double angle2 = startAngle - angleStep * (i + 1);

            float p1X = centerX + (float) (Math.cos(angle1) * radius);
            float p1Y = centerY + (float) (Math.sin(angle1) * radius);

            float p2X = centerX + (float) (Math.cos(angle2) * radius);
            float p2Y = centerY + (float) (Math.sin(angle2) * radius);

            // Emit 4 vertices for a Quad. The first and last are the same center point,
            // creating a triangle that fits the QUAD pipeline without bleeding.
            consumer.addVertexWith2DPose(pose, centerX, centerY).setColor(color);
            consumer.addVertexWith2DPose(pose, p1X, p1Y).setColor(color);
            consumer.addVertexWith2DPose(pose, p2X, p2Y).setColor(color);
            consumer.addVertexWith2DPose(pose, centerX, centerY).setColor(color);
        }
    }

    @Nullable
    private static ScreenRectangle getBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle screenRectangle = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }

}