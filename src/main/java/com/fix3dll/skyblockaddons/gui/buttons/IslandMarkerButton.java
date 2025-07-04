package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.gui.screens.IslandWarpGui;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

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

        PoseStack poseStack = graphics.pose();
        graphics.drawSpecial(source -> DrawUtils.blitAbsolute(graphics.pose(), source, PORTAL_ICON, x, y, 0, 0, width, height, width, height, color));

        if (hovered) {
            poseStack.pushPose();
            float textScale = 2.5F * expansion;
            poseStack.scale(textScale, textScale, 1);
            graphics.drawSpecial(source -> MC.font.drawInBatch(
                    getMessage(),
                    (x + (width / 2)) / textScale - MC.font.width(getMessage()) / 2F,
                    (y - 20) / textScale,
                    color,
                    true,
                    graphics.pose().last().pose(),
                    source,
                    Font.DisplayMode.NORMAL,
                    0,
                    15728880
            ));
            poseStack.popPose();
        }
    }

    public double getDistance(double mouseX, double mouseY) {
        double distance = new Point2D.Double(mouseX, mouseY).distance(new Point2D.Double(this.centerX, this.centerY));
        return distance > MAX_SELECT_RADIUS ? -1 : distance;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

}