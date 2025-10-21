package com.fix3dll.skyblockaddons.core.render.state;

import com.fix3dll.skyblockaddons.core.render.chroma.ManualChromaManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record ButtonColorBoxRenderState (
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        int left,
        int top,
        int right,
        int bottom,
        int alpha,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    public ButtonColorBoxRenderState {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }
        pose = new Matrix3x2f(pose);
    }
    public ButtonColorBoxRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            int left,
            int top,
            int right,
            int bottom,
            int alpha,
            @Nullable ScreenRectangle scissorArea
    ) {
        this(pipeline, textureSetup, pose, left, top, right, bottom, alpha, scissorArea, getBounds(left, top, right, bottom, pose, scissorArea));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        int colorLB = ManualChromaManager.getChromaColor(left, bottom, 1);
        int colorRB = ManualChromaManager.getChromaColor(right, bottom, 1);
        int colorLT = ManualChromaManager.getChromaColor(left, top, 1);
        int colorRT = ManualChromaManager.getChromaColor(right, top, 1);
        int colorMM = ManualChromaManager.getChromaColor(Math.floorDiv((right + left), 2), Math.floorDiv((top + bottom), 2), 1);
        
        // First triangle
        consumer.addVertexWith2DPose(this.pose, right, bottom).setColor(ARGB.color(alpha, colorRB));
        consumer.addVertexWith2DPose(this.pose, Math.floorDiv((right + left), 2), Math.floorDiv((top + bottom), 2)).setColor(ARGB.color(alpha, colorMM));
        consumer.addVertexWith2DPose(this.pose, left, top).setColor(ARGB.color(alpha, colorLT));
        consumer.addVertexWith2DPose(this.pose, left, bottom).setColor(ARGB.color(alpha, colorLB));
        // 2nd triangle
        consumer.addVertexWith2DPose(this.pose, right, bottom).setColor(ARGB.color(alpha, colorRB));
        consumer.addVertexWith2DPose(this.pose, right, top).setColor(ARGB.color(alpha, colorRT));
        consumer.addVertexWith2DPose(this.pose, left, top).setColor(ARGB.color(alpha, colorLT));
        consumer.addVertexWith2DPose(this.pose, Math.floorDiv((right + left), 2), Math.floorDiv((top + bottom), 2)).setColor(ARGB.color(alpha, colorMM));
    }

    @Nullable
    private static ScreenRectangle getBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle screenRectangle = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(screenRectangle) : screenRectangle;
    }

}