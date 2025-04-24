package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ButtonBanner extends SkyblockAddonsButton {

    private static final Logger logger = SkyblockAddons.getLogger();
    private static final int WIDTH = 130;

    private static ResourceLocation banner;
    private static BufferedImage bannerImage;

    private static boolean grabbedBanner;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonBanner(double x, double y) {
        super(0, (int)x, (int)y, "");

        if (!grabbedBanner) {
            grabbedBanner = true;
            bannerImage = null;
            banner = null;

            SkyblockAddons.runAsync(() -> {
                try {
                    URL url = new URL(main.getOnlineData().getBannerImageURL());
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setReadTimeout(5000);
                    connection.setRequestProperty("User-Agent", Utils.USER_AGENT);

                    bannerImage = TextureUtil.readBufferedImage(connection.getInputStream());

                    connection.disconnect();

                    this.width = bannerImage.getWidth();
                    this.height = bannerImage.getHeight();
                } catch (IOException ex) {
                    logger.warn("Couldn't grab main menu banner image from URL, falling back to local banner.", ex);
                }
            });
        }

        xPosition -= WIDTH/2;

        if (bannerImage != null) {
            this.width = bannerImage.getWidth();
            this.height = bannerImage.getHeight();
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (bannerImage != null && banner == null) { // This means it was just loaded from the URL above.
            banner = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("banner", new DynamicTexture(bannerImage));
        }

        if (banner != null) { // Could have not been loaded yet.
            float alphaMultiplier = calculateAlphaMultiplier();
            float scale = (float) WIDTH / bannerImage.getWidth(); // max width
            hovered = mouseX >= xPosition && mouseX < xPosition + WIDTH
                    && mouseY >= yPosition && mouseY < yPosition + bannerImage.getHeight() * scale;

            GlStateManager.enableBlend();
            GlStateManager.color(1F, 1F, 1F, alphaMultiplier * (hovered ? 1F : 0.8F));
            mc.getTextureManager().bindTexture(banner);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            drawModalRectWithCustomSizedTexture(
                    Math.round(xPosition / scale),
                    Math.round(yPosition / scale),
                    0, 0,
                    width, height,
                    width, height
            );
            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (hovered) {
            try {
                Desktop.getDesktop().browse(new URI(main.getOnlineData().getBannerLink()));
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }
}
