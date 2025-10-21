package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.render.state.FillAbsoluteRenderState;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

@Getter
public class ButtonLocation extends ButtonFeature {

    // So we know the latest hovered feature (used for arrow key movement).
    @Getter private static Feature lastHoveredFeature = null;

    private float boxXOne;
    private float boxXTwo;
    private float boxYOne;
    private float boxYTwo;

    private float scale;
    private float scaleX;
    private float scaleY;

    /**
     * Create a button that allows you to change the location of a GUI element.
     */
    public ButtonLocation(Feature feature) {
        super(0, 0, Component.empty(), feature);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // If the feature is disabled, don't draw in the "Edit GUI Location" section
        if (feature.isDisabled()) {
            return;
        }

        float scale = feature.getGuiScale();
        main.getRenderListener().drawFeature(graphics, feature, scale,this);

        if (this.isHovered) {
            lastHoveredFeature = feature;
        }
    }

    /**
     * This just updates the hovered status and draws the box around each feature. To avoid repetitive code.
     */
    public void checkHoveredAndDrawBox(GuiGraphics graphics, float boxXOne, float boxXTwo, float boxYOne, float boxYTwo, float scale) {
        checkHoveredAndDrawBox(graphics, boxXOne, boxXTwo, boxYOne, boxYTwo, scale, 1F, 1F);
    }

    public void checkHoveredAndDrawBox(GuiGraphics graphics, float boxXOne, float boxXTwo, float boxYOne, float boxYTwo, float scale, float scaleX, float scaleY) {
        double doubleMouseX = (MC.mouseHandler.xpos() * (double)MC.getWindow().getGuiScaledWidth() / (double)MC.getWindow().getWidth());
        double doubleMouseY = (MC.mouseHandler.ypos() * (double)MC.getWindow().getGuiScaledHeight() / (double)MC.getWindow().getHeight());

        this.boxXOne = boxXOne;
        this.boxXTwo = boxXTwo;
        this.boxYOne = boxYOne;
        this.boxYTwo = boxYTwo;
        this.scale = scale;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.isHovered = isMouseOver(doubleMouseX, doubleMouseY);
        int boxAlpha = this.isHovered ? 120 : 70;
        int boxColor = ColorCode.GRAY.getColor(boxAlpha);
        graphics.guiRenderState.submitGuiElement(
                new FillAbsoluteRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), graphics.pose(), boxXOne, boxYOne, boxXTwo, boxYTwo, boxColor, graphics.scissorStack.peek())
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return this.active && this.visible && this.isHovered && this.isValidClickButton(event.buttonInfo());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible
                && mouseX >= boxXOne * scale * scaleX && mouseY >= boxYOne * scale * scaleY
                && mouseX < boxXTwo * scale * scaleX && mouseY < boxYTwo * scale * scaleY;
    }
}