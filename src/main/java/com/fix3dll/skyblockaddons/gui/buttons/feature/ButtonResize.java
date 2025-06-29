package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

@Getter
public class ButtonResize extends ButtonFeature {

    private static final int SIZE = 2;

    private final Corner corner;
    public float resizeX;
    public float resizeY;

    public ButtonResize(float resizeX, float resizeY, Feature feature, Corner corner) {
        super(0, 0, Component.empty(), feature);
        this.corner = corner;
        this.resizeX = resizeX;
        this.resizeY = resizeY;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.scale = feature.getGuiScale();
        this.isHovered = isMouseOver(mouseX, mouseY);
        int color = ARGB.white(this.isHovered ? 1F : 0.25F);

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1);
        DrawUtils.fillAbsolute(graphics, resizeX - SIZE, resizeY - SIZE, resizeX + SIZE, resizeY + SIZE, color);
        poseStack.popPose();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible
                && mouseX >= (resizeX - SIZE) * scale && mouseY >= (resizeY - SIZE) * scale
                && mouseX < (resizeX + SIZE) * scale && mouseY < (resizeY + SIZE) * scale;
    }

    public enum Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }
}
