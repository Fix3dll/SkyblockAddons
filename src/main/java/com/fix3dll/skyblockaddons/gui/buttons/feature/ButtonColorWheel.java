package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.gui.screens.ColorSelectionGui;
import com.fix3dll.skyblockaddons.gui.screens.LocationEditGui;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class ButtonColorWheel extends ButtonFeature {

    private static final ResourceLocation COLOR_WHEEL = ResourceLocation.fromNamespaceAndPath(SkyblockAddons.MOD_ID, "gui/colorwheel.png");
    public static final int SIZE = 10;

    public float colorWheelX;
    public float colorWheelY;
    
    public ButtonColorWheel(float colorWheelX, float colorWheelY, Feature feature) {
        super(0, 0, Component.empty(), feature);
        this.width = SIZE;
        this.height = SIZE;

        this.colorWheelX = colorWheelX;
        this.colorWheelY = colorWheelY;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.scale = feature.getGuiScale();
        this.isHovered = isMouseOver(mouseX, mouseY);
        int color = ARGB.white(this.isHovered ? 1F : 0.5F);

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1F);
        graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, COLOR_WHEEL, colorWheelX, colorWheelY, 0, 0, 10, 10, 10, 10, color));poseStack.popPose();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isHovered(colorWheelX, colorWheelY, mouseX, mouseY, scale);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.isHovered) {
            if (MC.screen instanceof LocationEditGui gui) {
                gui.setClosing(true);
                MC.setScreen(new ColorSelectionGui(feature, EnumUtils.GUIType.EDIT_LOCATIONS, gui.getLastTab(), gui.getLastPage()));
            }
        }
    }
}