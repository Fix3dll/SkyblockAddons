package com.fix3dll.skyblockaddons.core.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record BlitAbsoluteRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        float x0,
        float y0,
        float x1,
        float y1,
        float u0,
        float u1,
        float v0,
        float v1,
        int color,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    public BlitAbsoluteRenderState {
        float x2 = x0 + u0;
        float y2 = y0 + u1;
        float minU = (x1 + 0.0F) / v0;
        float maxU = (x1 + u0) / v0;
        float minV = (y1 + 0.0F) / v1;
        float maxV = (y1 + u1) / v1;
        x1 = x2;
        y1 = y2;
        u0 = minU;
        u1 = maxU;
        v0 = minV;
        v1 = maxV;
        pose = new Matrix3x2f(pose);
    }
    public BlitAbsoluteRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x,
            float y,
            float uOffset,
            float vOffset,
            float uWidth,
            float vHeight,
            float textureWidth,
            float textureHeight,
            int color,
            @Nullable ScreenRectangle scissorArea
    ) {
        this(pipeline, textureSetup, pose, x, y, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight, color, scissorArea, getBounds(x, y, x + uWidth, y + vHeight, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0()).setUv(this.u0(), this.v0()).setColor(this.color());
        consumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1()).setUv(this.u0(), this.v1()).setColor(this.color());
        consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1()).setUv(this.u1(), this.v1()).setColor(this.color());
        consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0()).setUv(this.u1(), this.v0()).setColor(this.color());
    }

    @Nullable
    private static ScreenRectangle getBounds(float x0, float y0, float x1, float y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle screenRectangle = new ScreenRectangle((int) x0, (int) y0, (int) (x1 - x0), (int) (y1 - y0)).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }

}