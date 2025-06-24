package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

@Getter
public class ButtonCredit extends ButtonFeature {

    private static final ResourceLocation WEB = ResourceLocation.fromNamespaceAndPath(SkyblockAddons.MOD_ID, "gui/web.png");

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

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.scale(scale, scale, 1);
        graphics.blit(RenderType::guiTextured, WEB, getX(), getY(), 0, 0, 12, 12, 12, 12, color);
        poseStack.popPose();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.isHovered && !feature.isRemoteDisabled()) {
            try {
                Util.getPlatform().openUri(credit.getUrl());
            } catch (Exception ignored) {}
        }
    }

}