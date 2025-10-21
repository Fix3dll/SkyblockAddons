package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.render.state.BlitAbsoluteRenderState;
import com.fix3dll.skyblockaddons.core.render.state.SbaTextRenderState;
import com.fix3dll.skyblockaddons.gui.screens.IslandWarpGui;
import com.fix3dll.skyblockaddons.listeners.RenderListener;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;

import java.awt.geom.Point2D;

public class IslandMarkerButton extends SkyblockAddonsButton {

    public static final int MAX_SELECT_RADIUS = 90;

    private static final ResourceLocation PORTAL_ICON = SkyblockAddons.resourceLocation("portal.png");

    @Getter private final IslandWarpGui.Marker marker;

    private float centerX;
    private float centerY;

    public IslandMarkerButton(IslandWarpGui.Marker marker) {
        super(0, 0, Component.literal(marker.getLabel()));
        this.marker = marker;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    public void drawButton(GuiGraphics graphics, float islandX, float islandY, float expansion, boolean hovered) {
        float width = 50 * expansion;
        float height = width * (100 / 81F); // Ratio is 81w : 100h

        float centerX = islandX + (marker.getX())*expansion;
        float centerY = islandY + (marker.getY())*expansion;

        this.centerX = centerX;
        this.centerY = centerY;

        float x = centerX - (width / 2);
        float y = centerY - (height / 2);

        int color = ARGB.white(hovered ? 1F : 0.6F);

        Matrix3x2fStack poseStack = graphics.pose();
        graphics.guiRenderState.submitGuiElement(
                new BlitAbsoluteRenderState(RenderPipelines.GUI_TEXTURED, RenderListener.textureSetup(PORTAL_ICON), graphics.pose(), x, y, 0, 0, width, height, width, height, color, graphics.scissorStack.peek())
        );

        if (hovered) {
            poseStack.pushMatrix();
            float textScale = 2.5F * expansion;
            poseStack.scale(textScale, textScale);
            graphics.guiRenderState.submitText(
                    new SbaTextRenderState(
                            getMessage().getVisualOrderText(),
                            graphics.pose(),
                            (x + (width / 2)) / textScale - MC.font.width(getMessage()) / 2F,
                            (y - 20) / textScale,
                            color,
                            0,
                            true,
                            graphics.scissorStack.peek()
                    )
            );
            poseStack.popMatrix();
        }
    }

    public double getDistance(double mouseX, double mouseY) {
        double distance = new Point2D.Double(mouseX, mouseY).distance(new Point2D.Double(this.centerX, this.centerY));
        return distance > MAX_SELECT_RADIUS ? -1 : distance;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

}