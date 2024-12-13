package codes.biscuit.skyblockaddons.gui.screens;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSocial;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;

public class SkyblockAddonsScreen extends GuiScreen {

    protected static final SkyblockAddons main = SkyblockAddons.getInstance();

    public static final ResourceLocation LOGO = new ResourceLocation("skyblockaddons", "logo.png");
    public static final ResourceLocation LOGO_GLOW = new ResourceLocation("skyblockaddons", "logoglow.png");
    private static final String FORMATTED_VERSION = "v" + SkyblockAddons.VERSION
            .replace("+" + SkyblockAddons.BUILD_NUMBER, "")
            .replace("beta", "b") + " unofficial";
    private static final int FADE_MILLIS = 500;
    private static final int TITLE_ANIMATION_MILLIS = 4000;

    // Used to calculate the transparency when fading in.
    final long timeOpened = System.currentTimeMillis();

    public float calculateAlphaMultiplier() {
        if (main.getUtils().isFadingIn()) {
            long timeSinceOpen = System.currentTimeMillis() - timeOpened;
            if (timeSinceOpen <= FADE_MILLIS) {
                return (float) timeSinceOpen / (FADE_MILLIS * 2);
            }
        }
        return 0.5F;
    }

    protected void drawGradientBackground(int alpha) {
        drawGradientBackground((int) (alpha * 0.5), alpha);
    }

    protected void drawGradientBackground(int startAlpha, int endAlpha) {
        int startColor = new Color(0,0, 0, startAlpha).getRGB();
        int endColor = new Color(0,0, 0, endAlpha).getRGB();
        drawGradientRect(0, 0, width, height, startColor, endColor);
    }

    /**
     * Draws the default text at the top at bottoms of the GUI.
     * @param gui The gui to draw the text on.
     */
    protected void drawDefaultTitleText(GuiScreen gui, int alpha) {
        int defaultBlue = SkyblockAddons.getInstance().getUtils().getDefaultBlue(alpha);

        int height = 85;
        int width = height*2;
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        SkyblockAddons.getInstance().getUtils().enableStandardGLOptions();
        textureManager.bindTexture(LOGO);
        DrawUtils.drawModalRectWithCustomSizedTexture(scaledResolution.getScaledWidth()/2F-width/2F, 5, 0, 0, width, height, width, height, true);

        float glowAlpha;
        glowAlpha = System.currentTimeMillis() % TITLE_ANIMATION_MILLIS;
        if (glowAlpha > TITLE_ANIMATION_MILLIS / 2F) {
            glowAlpha = (TITLE_ANIMATION_MILLIS - glowAlpha) / (TITLE_ANIMATION_MILLIS / 2F);
        } else {
            glowAlpha = glowAlpha / (TITLE_ANIMATION_MILLIS / 2F);
        }

        GlStateManager.color(1,1,1, glowAlpha);
        textureManager.bindTexture(LOGO_GLOW);
        DrawUtils.drawModalRectWithCustomSizedTexture(scaledResolution.getScaledWidth()/2F-width/2F, 5, 0, 0, width, height, width, height, true);

        GlStateManager.color(1,1,1, 1);
        drawScaledString(gui, FORMATTED_VERSION, 55, defaultBlue, 1.3, 170 - Minecraft.getMinecraft().fontRendererObj.getStringWidth(FORMATTED_VERSION), false);

        SkyblockAddons.getInstance().getUtils().restoreGLOptions();
    }

    static void drawScaledString(GuiScreen guiScreen, String text, int y, int color, double scale, int xOffset) {
        drawScaledString(guiScreen, text, y, color, scale, xOffset, true);
    }

    /**
     * Draws a centered string at the middle of the screen on the x axis, with a specified scale and location.
     *
     * @param text The text to draw.
     * @param y The y level to draw the text/
     * @param color The text color.
     * @param scale The scale to draw the text.
     * @param xOffset The offset from the center x that the text should be drawn at.
     */
    static void drawScaledString(GuiScreen guiScreen, String text, int y, int color, double scale, int xOffset, boolean centered) {
        GlStateManager.enableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        if (centered) {
            DrawUtils.drawCenteredText(
                    text,
                    Math.round((float) guiScreen.width / 2 / scale) + xOffset,
                    Math.round((float) y / scale),
                    color
            );
        } else {
            Minecraft.getMinecraft().fontRendererObj.drawString(
                    text,
                    Math.round((float) guiScreen.width / 2 / scale) + xOffset,
                    Math.round((float) y / scale),
                    color,
                    true
            );
        }
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }

    public void addSocials() {
        //buttonList.add(new ButtonSocial(width / 2 + 175, 30, EnumUtils.Social.DISCORD));
        buttonList.add(new ButtonSocial(width / 2D + 125, 30, EnumUtils.Social.MODRINTH));
        buttonList.add(new ButtonSocial(width / 2D + 150, 30, EnumUtils.Social.GITHUB));
        buttonList.add(new ButtonSocial(width / 2D + 175, 30, EnumUtils.Social.BUYMEACOFFEE));
    }

}
