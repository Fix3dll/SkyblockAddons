package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix3x2fStack;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class ButtonBanner extends SkyblockAddonsButton {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final int WIDTH = 130;

    private static ResourceLocation banner;
    private static NativeImage bannerImage;

    private static boolean grabbedBanner;

    /**
     * Create a button for toggling a feature on or off. This includes all the Features that have a proper ID.
     */
    public ButtonBanner(double x, double y) {
        super((int) x, (int) y, Component.empty());

        if (!grabbedBanner) {
            grabbedBanner = true;
            bannerImage = null;
            banner = null;

            SkyblockAddons.runAsync(() -> {
                try {
                    URL url = new URI(main.getOnlineData().getBannerImageURL()).toURL();
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setReadTimeout(5000);
                    connection.addRequestProperty("User-Agent", Utils.USER_AGENT);

                    bannerImage = NativeImage.read(connection.getInputStream());

                    connection.disconnect();

                    this.width = bannerImage.getWidth();
                    this.height = bannerImage.getHeight();
                } catch (IOException ex) {
                    LOGGER.warn("Couldn't grab main menu banner image from URL, falling back to local banner.", ex);
                } catch (URISyntaxException e) {
                    LOGGER.error("Wrong banner image URL!", e);
                }
            });
        }

        setX(getX() - WIDTH / 2);

        if (bannerImage != null) {
            this.width = bannerImage.getWidth();
            this.height = bannerImage.getHeight();
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // This means it was just loaded from the URL above.
        if (bannerImage != null && banner == null) {
//            banner = MC.getTextureManager().registerDynamicTexture("banner", new NativeImageBackedTexture(bannerImage)); FIXME
        }

        if (banner != null) { // Could have not been loaded yet.
            float alphaMultiplier = calculateAlphaMultiplier();
            int color = ARGB.white(alphaMultiplier * (this.isHovered ? 1F : 0.8F));
            this.scale = (float) WIDTH / bannerImage.getWidth(); // max width
            this.isHovered = mouseX >= getX() && mouseX < getX() + WIDTH
                    && mouseY >= getY() && mouseY < getY() + bannerImage.getHeight() * scale;

            Matrix3x2fStack poseStack = graphics.pose();
            poseStack.pushMatrix();
            poseStack.scale(scale, scale);
            int x = Math.round(getX() / scale);
            int y = Math.round(getY() / scale);
            graphics.blit(RenderPipelines.GUI_TEXTURED, banner, x, y, 0, 0, width, height, width, height, color);
//            drawModalRectWithCustomSizedTexture(Math.round(xPosition / scale), Math.round(xPosition / scale), 0, 0, width, height, width, height);
            poseStack.popMatrix();
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        if (this.isHovered) {
            String link = main.getOnlineData().getBannerLink();
            if (link != null && !link.isEmpty()) {
                try {
                    Util.getPlatform().openUri(link);
                } catch (Exception ignored) {}
            }
        }
    }
}