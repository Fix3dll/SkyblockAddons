package com.fix3dll.skyblockaddons.core.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record FillAbsoluteRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float minX,
        float minY,
        float maxX,
        float maxY,
        int color,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    public FillAbsoluteRenderState {
        if (minX < maxX) {
            float i = minX;
            minX = maxX;
            maxX = i;
        }
        if (minY < maxY) {
            float i = minY;
            minY = maxY;
            maxY = i;
        }
        pose = new Matrix3x2f(pose);
    }
    public FillAbsoluteRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float minX,
            float minY,
            float maxX,
            float maxY,
            int color,
            @Nullable ScreenRectangle scissorArea
    ) {
        this(pipeline, textureSetup, pose, minX, minY, maxX, maxY, color, scissorArea, getBounds(Math.round(minX), Math.round(minY), Math.round(maxX), Math.round(maxY), pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(this.pose(), this.minX, this.minY).setColor(this.color);
        consumer.addVertexWith2DPose(this.pose(), this.minX, this.maxY).setColor(this.color);
        consumer.addVertexWith2DPose(this.pose(), this.maxX, this.maxY).setColor(this.color);
        consumer.addVertexWith2DPose(this.pose(), this.maxX, this.minY).setColor(this.color);
    }

    @Nullable
    private static ScreenRectangle getBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle screenRectangle = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }

}