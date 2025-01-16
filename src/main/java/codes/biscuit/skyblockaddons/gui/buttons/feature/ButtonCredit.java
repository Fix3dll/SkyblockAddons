package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.Desktop;
import java.net.URI;

public class ButtonCredit extends ButtonFeature {

    private static final ResourceLocation WEB = new ResourceLocation("skyblockaddons", "gui/web.png");

    @Getter private final EnumUtils.FeatureCredit credit;
    private final boolean smaller;

    public ButtonCredit(double x, double y, String buttonText, EnumUtils.FeatureCredit credit, Feature feature, boolean smaller) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.width = 12;
        this.height = 12;
        this.credit = credit;
        this.smaller = smaller;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        float alphaMultiplier = calculateAlphaMultiplier();

        float scale = smaller ? 0.6F : 0.8F;
        hovered = isHovered(mouseX, mouseY, scale);
        GlStateManager.enableBlend();

        GlStateManager.color(1,1,1,alphaMultiplier * (hovered ? 1F : 0.7F));
        if (feature.isRemoteDisabled()) {
            GlStateManager.color(0.3F,0.3F,0.3F,0.7F);
        }
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        mc.getTextureManager().bindTexture(WEB);
        drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0, 12, 12, 12, 12);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (hovered && !feature.isRemoteDisabled()) {
            try {
                Desktop.getDesktop().browse(new URI(credit.getUrl()));
                return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    @Override
    public void playPressSound(SoundHandler soundHandler) {
        if (!feature.isRemoteDisabled()) {
            super.playPressSound(soundHandler);
        }
    }
}
