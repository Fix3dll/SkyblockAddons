package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
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
import java.util.UUID;

public class ButtonBanner extends SkyblockAddonsButton {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public static final int WIDTH = 130;
    public static final int HEIGHT = 95;

    private static ResourceLocation banner;
    private static NativeImage bannerImage;

    public static boolean bannerRegistered;

    public static Runnable REGISTER_BANNER = () -> {
        String bannerImageUrl = main.getOnlineData().getBannerImageURL();
        if (bannerImageUrl == null) {
            bannerRegistered = false;
            return;
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URI(bannerImageUrl).toURL();
            connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.addRequestProperty("User-Agent", Utils.USER_AGENT);

            banner = SkyblockAddons.resourceLocation("dynamic/" + UUID.randomUUID());
            bannerImage = NativeImage.read(connection.getInputStream());

            connection.disconnect();

            MC.execute(() -> {
                DynamicTexture dynamicTexture = new DynamicTexture(() -> banner.toString(), bannerImage);
                Minecraft.getInstance().getTextureManager().register(banner, dynamicTexture);
                bannerRegistered = true;
            });
        } catch (IOException ex) {
            LOGGER.warn("Couldn't grab main menu banner image from URL, falling back to local banner.", ex);
        } catch (URISyntaxException e) {
            LOGGER.error("Wrong banner image URL!", e);
        } finally {
            if (connection != null) connection.disconnect();
        }
    };

    /**
     * Create a button for toggling a feature on or off. This includes all the Features that have a proper ID.
     */
    public ButtonBanner(double x, double y) {
        super((int) x, (int) y, WIDTH, HEIGHT, Component.empty());

        if (!bannerRegistered) {
            bannerImage = null;
            banner = null;

            SkyblockAddons.runAsync(REGISTER_BANNER);
        }

        if (bannerImage != null) {
            int imageWidth = bannerImage.getWidth();
            int imageHeight = bannerImage.getHeight();
            this.scale = (float) WIDTH / imageWidth; // max width
            float scaledImageHeight = imageHeight * scale;

            if (scaledImageHeight < HEIGHT) {
               setY(getY() + (HEIGHT / 2 - (int) (scaledImageHeight / 2.0F)));
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (bannerRegistered) { // Could have not been loaded yet.
            int imageWidth = bannerImage.getWidth();
            int imageHeight = bannerImage.getHeight();

            float alphaMultiplier = calculateAlphaMultiplier();
            this.scale = (float) WIDTH / imageWidth; // max width
            this.isHovered = isMouseOver(mouseX, mouseY);
            int color = ARGB.white(alphaMultiplier * (this.isHovered ? 1F : 0.8F));

            Matrix3x2fStack poseStack = graphics.pose();
            poseStack.pushMatrix();
            poseStack.scale(scale);
            int x = Math.round(getX() / scale);
            int y = Math.round(getY() / scale);
            graphics.blit(RenderPipelines.GUI_TEXTURED, banner, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight, color);
            poseStack.popMatrix();
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + WIDTH &&
               mouseY >= getY() && mouseY < getY() + bannerImage.getHeight() * scale;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        String link = main.getOnlineData().getBannerLink();
        if (link != null && !link.isBlank()) {
            try {
                Util.getPlatform().openUri(link);
            } catch (Exception ignored) {}
        }
    }

}