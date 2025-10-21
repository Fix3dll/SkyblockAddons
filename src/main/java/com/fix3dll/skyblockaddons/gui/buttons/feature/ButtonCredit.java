package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;

@Getter
public class ButtonCredit extends ButtonFeature {

    private static final ResourceLocation WEB = SkyblockAddons.resourceLocation("gui/web.png");

    private final EnumUtils.FeatureCredit credit;

    public ButtonCredit(double x, double y, String buttonText, EnumUtils.FeatureCredit credit, Feature feature, boolean smaller) {
        super((int) x, (int) y, Component.literal(buttonText), feature);
        this.width = 12;
        this.height = 12;
        this.credit = credit;
        this.scale = smaller ? 0.6F : 0.8F;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.isHovered = isHovered(mouseX, mouseY);
        float alphaMultiplier = calculateAlphaMultiplier();
        int color = ARGB.white(alphaMultiplier * (this.isHovered ? 1F : 0.7F));
        if (feature.isRemoteDisabled()) {
            color = ARGB.colorFromFloat(0.7F, 0.3F, 0.3F, 0.3F);
        }

        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.scale(scale, scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, WEB, getX(), getY(), 0, 0, 12, 12, 12, 12, color);
        poseStack.popMatrix();
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        if (!feature.isRemoteDisabled()) {
            try {
                Util.getPlatform().openUri(credit.getUrl());
            } catch (Exception ignored) {}
        }
    }

}