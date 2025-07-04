package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.util.function.Supplier;

public class ButtonFeatureToggle extends ButtonFeature {

    private static final ResourceLocation TOGGLE_INSIDE_CIRCLE = SkyblockAddons.resourceLocation("gui/toggleinsidecircle.png");
    private static final ResourceLocation TOGGLE_BORDER = SkyblockAddons.resourceLocation("gui/toggleborder.png");
    private static final ResourceLocation TOGGLE_INSIDE_BACKGROUND = SkyblockAddons.resourceLocation("gui/toggleinsidebackground.png");

    private static final int CIRCLE_PADDING_LEFT = 5;
    private static final int ANIMATION_SLIDE_DISTANCE = 12;
    private static final int ANIMATION_SLIDE_TIME = 150;

    protected long animationButtonClicked = -1;
    protected Supplier<Boolean> isEnabled;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonFeatureToggle(double x, double y, Feature feature) {
        super((int) x, (int) y, Component.empty(), feature);
        this.width = 31;
        this.height = 15;
        this.isEnabled = feature::isEnabled;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = isHovered(mouseX, mouseY);

        int color = ARGB.color(30, 37, 46, 255);
        graphics.blit(RenderType::guiTextured, TOGGLE_BORDER, getX(), getY(), 0, 0, width, height, width, height, color);
        boolean enabled = isEnabled == null ? feature.isEnabled() : isEnabled.get();
        boolean remoteDisabled = feature.isRemoteDisabled();

        if (enabled) {
            color = ARGB.color(remoteDisabled ? 25 : 255, 36, 255, 98); // Green
        } else {
            color = ARGB.color(remoteDisabled ? 25 : 255, 222, 68, 76); // Red
        }
        graphics.blit(RenderType::guiTextured, TOGGLE_INSIDE_BACKGROUND, getX(), getY(), 0, 0, width, height, width, height, color);

        int startingX = getStartingPosition(enabled);
        int slideAnimationOffset = 0;

        if (animationButtonClicked != -1) {
            startingX = getStartingPosition(!enabled); // They toggled so start from the opposite side.

            int timeSinceOpen = (int) (System.currentTimeMillis() - animationButtonClicked);
            int animationTime = ANIMATION_SLIDE_TIME;
            if (timeSinceOpen > animationTime) {
                timeSinceOpen = animationTime;
            }

            slideAnimationOffset = ANIMATION_SLIDE_DISTANCE * timeSinceOpen/animationTime;
        }

        startingX += enabled ? slideAnimationOffset : -slideAnimationOffset;

        color = ARGB.white(1F);
        graphics.blit(RenderType::guiTextured, TOGGLE_INSIDE_CIRCLE, startingX, getY() + 3, 0, 0, 9, 9, 9, 9, color);
    }

    private int getStartingPosition(boolean enabled) {
        if (!enabled)  {
            return getX() + CIRCLE_PADDING_LEFT;
        } else {
            return getStartingPosition(false) + ANIMATION_SLIDE_DISTANCE;
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.feature != null && !this.feature.isRemoteDisabled()) {
            if (feature.isDisabled()) {
                feature.setEnabled(true);
                switch (feature) {
                    case DISCORD_RPC:
                        if (main.getUtils().isOnSkyblock()) {
                            main.getDiscordRPCManager().start();
                        }
                        break;
                    case ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT:
                        Feature.DISABLE_ENDERMAN_TELEPORTATION_EFFECT.setEnabled(true);
                        break;
                    case TURN_ALL_TEXTS_CHROMA:
                        feature.setChroma(true);
                        break;
                }
            } else {
                feature.setEnabled(false);
                switch (feature) {
                    case FULL_INVENTORY_WARNING:
                        main.getInventoryUtils().setInventoryWarningShown(false);
                        break;
                    case DISCORD_RPC:
                        main.getDiscordRPCManager().stop();
                        break;
                    case DISABLE_ENDERMAN_TELEPORTATION_EFFECT:
                        Feature.ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT.setEnabled(true);
                        break;
                    case TURN_ALL_TEXTS_CHROMA:
                        feature.setChroma(false);
                        break;
                }
            }
            if (feature == Feature.TURN_ALL_FEATURES_CHROMA) {
                boolean areAllFeaturesChroma = ColorUtils.areAllFeaturesChroma();

                for (Feature loopFeature : Feature.values()) {
                    if (loopFeature.isGuiFeature() && loopFeature.getFeatureGuiData().getDefaultColor() != null) {
                        loopFeature.setChroma(!areAllFeaturesChroma);
                    }
                }
            }
            this.animationButtonClicked = System.currentTimeMillis();
        }
    }

}