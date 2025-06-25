package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.Language;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.gui.screens.SkyblockAddonsGui;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.util.function.Consumer;

public class FeatureBase extends ButtonFeature {

    private final Consumer<FeatureBase> consumer;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public FeatureBase(double x, double y, String buttonText, Feature feature) {
        this((int)x, (int)y, 140, 50, buttonText, feature, null);
    }

    public FeatureBase(double x, double y, String buttonText, Feature feature, Consumer<FeatureBase> consumer) {
        this((int)x, (int)y, 140, 50, buttonText, feature, consumer);
    }

    public FeatureBase(double x, double y, int width, int height, String buttonText, Feature feature) {
        this((int) x, (int) y, width, height, buttonText, feature, null);
    }

    public FeatureBase(double x, double y, int width, int height, String buttonText, Feature feature, Consumer<FeatureBase> consumer) {
        super((int) x, (int) y, Component.literal(buttonText), feature);
        this.width = width;
        this.height = height;
        this.consumer = consumer;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float alphaMultiplier = calculateAlphaMultiplier();
        int alpha = alphaMultiplier == 1F ? 255 : (int) (255 * alphaMultiplier);
        if (alpha < 4) alpha = 4;
        this.isHovered = isHovered(mouseX, mouseY);

        int color, fontColor;
        if (feature.isRemoteDisabled()) {
            color = ARGB.colorFromFloat(0.7F, 0.3F, 0.3F, 0.3F);
            fontColor = ARGB.color(255, 60, 60, 60);
        } else {
            color = ARGB.white(0.7F);
            fontColor = ColorUtils.getDefaultBlue(alpha);
        }

        graphics.drawSpecial(source -> DrawUtils.drawRoundedRect(graphics, source, getX(), getY(), width, height, 4, ARGB.color(230, 27, 29, 41)));
//            DrawUtils.drawRect(xPosition, yPosition, width, height, ColorUtils.getDummySkyblockColor(27, 29, 41, 230), 4);

        EnumUtils.FeatureCredit creditFeature = EnumUtils.FeatureCredit.fromFeature(feature);

        // Wrap the feature name into 2 lines.
        String[] wrappedString = main.getUtils().wrapSplitText(getMessage().getString(), 28);
        if (wrappedString.length > 2) { // If it makes more than 2 lines,
            StringBuilder lastLineString = new StringBuilder(); // combine all the last
            for (int i = 1; i < wrappedString.length; i++) { // lines and combine them
                lastLineString.append(wrappedString[i]); // back into the second line.
                if (i != wrappedString.length-1) {
                    lastLineString.append(" ");
                }
            }

            wrappedString = new String[]{wrappedString[0], lastLineString.toString()};
        }

        int textX = getX() + width / 2;
        int textY = getY();

        boolean multiline = wrappedString.length > 1;
        PoseStack poseStack = graphics.pose();

        for (int i = 0; i < wrappedString.length; i++) {
            String line = wrappedString[i];

            float scale = 1F;
            int stringWidth = MC.font.width(line);
            float widthLimit = SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10;
            if (feature == Feature.WARNING_TIME) {
                widthLimit = 90;
            }
            if (stringWidth > widthLimit) {
                scale = 1F / (stringWidth / widthLimit);
            }
            if (feature == Feature.GENERAL_SETTINGS) textY -= 5;

            poseStack.pushPose();
            poseStack.scale(scale, scale, 1);
            int offset = 9;
            if (creditFeature != null) offset -= 4;
            // If the scale is small gotta move it down a bit or else it's too mushed with the above line.
            offset += (10 - 10 * scale);
            graphics.drawCenteredString(MC.font, line, (int) (textX / scale), (int) ((textY / scale) + offset), fontColor);
            poseStack.popPose();

            // If it's not the last line, add to the Y.
            if (multiline && i == 0) {
                textY += 10;
            }
        }

        if (creditFeature != null) {
            // If its 2 lines the credits have to be smaller.
            float scale = multiline ? 0.6F : 0.8F;

            float creditsY = (textY / scale) + 23;
            if (multiline) {
                creditsY += 3; // Since its smaller the scale is wierd to move it down a tiny bit.
            }

            poseStack.pushPose();
            poseStack.scale(scale, scale, 1);
            graphics.drawCenteredString(MC.font, creditFeature.getAuthor(), (int) (textX / scale), (int) creditsY, fontColor);
            poseStack.popPose();
        }

        if (feature == Feature.LANGUAGE) {
            try {
                ResourceLocation langSprite = main.getUtils().isHalloween()
                        ? SkyblockAddons.resourceLocation("flags/halloween.png")
                        : ((Language) Feature.LANGUAGE.getValue()).getIdentifier();
                graphics.blit(RenderType::guiTextured, langSprite, (int) (getX() + width / 2F - 20), getY() + 20, 0, 0, 38, 30, 38, 30, -1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (feature == Feature.EDIT_LOCATIONS) {
            try {
                graphics.blit(RenderType::guiTextured, SkyblockAddons.resourceLocation("gui/move.png"), (int) (getX() + width / 2F - 12), getY() + 22, 0, 0, 25, 25, 25, 25, -1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (feature.isRemoteDisabled()) {
            graphics.drawCenteredString(
                    MC.font,
                    Translations.getMessage("messages.featureDisabled"),
                    textX,
                    textY + 6,
                    ColorUtils.getDefaultBlue(alpha)
            );
        }
    }

    public Pair<Integer, Integer> getCreditsCoords(EnumUtils.FeatureCredit credit) {
        String[] wrappedString = main.getUtils().wrapSplitText(getMessage().getString(), 28);
        boolean multiLine = wrappedString.length > 1;

        // If its 2 lines the credits have to be smaller.
        float scale = multiLine ? 0.6F : 0.8F;

        // If it's a smaller scale, you have to move it down more.
        int y = (int) ((getY() / scale) + (multiLine ? 30 : 21));

        if (multiLine) { // When there's multiple lines the second line is moved 10px down.
            y += 10;
        }

        int x = (int) ((getX() + width / 2F) / scale) - MC.font.width(credit.getAuthor()) / 2 - 17;
        return new Pair<>(x, y);
    }

    public boolean isMultilineButton() {
        String[] wrappedString = main.getUtils().wrapSplitText(getMessage().getString(), 28);
        return wrappedString.length > 1;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        switch (feature) {
            case LANGUAGE, EDIT_LOCATIONS, GENERAL_SETTINGS -> playButtonClickSound(soundManager);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered && this.consumer != null) {
            this.consumer.accept(this);
        }
        return switch (feature) {
            case LANGUAGE, EDIT_LOCATIONS, GENERAL_SETTINGS -> super.mouseClicked(mouseX, mouseY, button);
            default -> false;
        };
    }
}