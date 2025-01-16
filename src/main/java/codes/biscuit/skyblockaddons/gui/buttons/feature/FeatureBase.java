package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.gui.screens.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;

public class FeatureBase extends ButtonFeature {

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public FeatureBase(double x, double y, String buttonText, Feature feature) {
        this((int)x, (int)y, 140, 50, buttonText, feature);
    }

    public FeatureBase(double x, double y, int width, int height, String buttonText, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.feature = feature;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            float alphaMultiplier = calculateAlphaMultiplier();
            int alpha = alphaMultiplier == 1F ? 255 : (int) (255 * alphaMultiplier);
            if (alpha < 4) alpha = 4;
            hovered = isHovered(mouseX, mouseY);

            int fontColor = ColorUtils.getDefaultBlue(alpha);
            if (feature.isRemoteDisabled()) {
                fontColor = new Color(60,60,60).getRGB();
            }
            GlStateManager.enableBlend();
            GlStateManager.color(1,1,1,0.7F);
            if (feature.isRemoteDisabled()) {
                GlStateManager.color(0.3F,0.3F,0.3F,0.7F);
            }
            DrawUtils.drawRect(xPosition, yPosition, width, height, ColorUtils.getDummySkyblockColor(27, 29, 41, 230), 4);

            EnumUtils.FeatureCredit creditFeature = EnumUtils.FeatureCredit.fromFeature(feature);

            // Wrap the feature name into 2 lines.
            String[] wrappedString = main.getUtils().wrapSplitText(displayString, 28);
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

            int textX = xPosition + width / 2;
            int textY = yPosition;

            boolean multiline = wrappedString.length > 1;

            for (int i = 0; i < wrappedString.length; i++) {
                String line = wrappedString[i];

                float scale = 1;
                int stringWidth = mc.fontRendererObj.getStringWidth(line);
                float widthLimit = SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10;
                if (feature == Feature.WARNING_TIME) {
                    widthLimit = 90;
                }
                if (stringWidth > widthLimit) {
                    scale = 1 / (stringWidth / widthLimit);
                }
                if (feature == Feature.GENERAL_SETTINGS) textY -= 5;

                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                int offset = 9;
                if (creditFeature != null) offset -= 4;
                offset += (10 - 10*scale); // If the scale is small gotta move it down a bit or else its too mushed with the above line.
                DrawUtils.drawCenteredText(line, (textX / scale), (textY / scale) + offset, fontColor);
                GlStateManager.popMatrix();

                // If its not the last line, add to the Y.
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

                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                DrawUtils.drawCenteredText(creditFeature.getAuthor(), (textX / scale), creditsY, fontColor);
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();
            }

            if (feature == Feature.LANGUAGE) {
                GlStateManager.color(1,1,1,1F);
                try {
                    Language currentLanguage = (Language) Feature.LANGUAGE.getValue();
                    mc.getTextureManager().bindTexture(currentLanguage.getResourceLocation());
                    if (main.getUtils().isHalloween()) {
                        mc.getTextureManager().bindTexture(new ResourceLocation("skyblockaddons", "flags/halloween.png"));
                    }
                    DrawUtils.drawModalRectWithCustomSizedTexture(xPosition + width / 2F - 20, yPosition + 20, 0, 0, 38, 30, 38, 30, true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (feature == Feature.EDIT_LOCATIONS) {
                GlStateManager.color(1,1,1,1F);
                try {
                    mc.getTextureManager().bindTexture(new ResourceLocation("skyblockaddons", "gui/move.png"));
                    DrawUtils.drawModalRectWithCustomSizedTexture(xPosition + width / 2F - 12, yPosition + 22, 0, 0, 25, 25, 25, 25, true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (feature.isRemoteDisabled()) {
                drawCenteredString(mc.fontRendererObj, Translations.getMessage("messages.featureDisabled"), textX, textY + 6 , ColorUtils.getDefaultBlue(alpha));
            }
        }
    }

    public Pair<Integer, Integer> getCreditsCoords(EnumUtils.FeatureCredit credit) {
        String[] wrappedString = main.getUtils().wrapSplitText(displayString, 28);
        boolean multiLine = wrappedString.length > 1;

        // If its 2 lines the credits have to be smaller.
        float scale = multiLine ? 0.6F : 0.8F;

        int y = (int)((yPosition/scale) + (multiLine ? 30 : 21)); // If its a smaller scale, you gotta move it down more.

        if (multiLine) { // When there's multiple lines the second line is moved 10px down.
            y += 10;
        }

        int x = (int)((xPosition+width/2F)/scale) - Minecraft.getMinecraft().fontRendererObj.getStringWidth(credit.getAuthor()) / 2 - 17;
        return new Pair<>(x, y);
    }

    public boolean isMultilineButton() {
        String[] wrappedString = main.getUtils().wrapSplitText(displayString, 28);
        return wrappedString.length > 1;
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (feature == Feature.LANGUAGE || feature == Feature.EDIT_LOCATIONS || feature == Feature.GENERAL_SETTINGS) {
            super.playPressSound(soundHandlerIn);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (feature == Feature.LANGUAGE || feature == Feature.EDIT_LOCATIONS || feature == Feature.GENERAL_SETTINGS) {
            return super.mousePressed(mc, mouseX, mouseY);
        }
        return false;
    }
}
