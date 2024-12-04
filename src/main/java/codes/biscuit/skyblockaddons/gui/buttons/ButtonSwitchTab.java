package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class ButtonSwitchTab extends SkyblockAddonsButton {

    private final EnumUtils.GuiTab currentTab;
    @Getter private final EnumUtils.GuiTab tab;

    // Used to calculate the transparency when fading in.
    private final long timeOpened = System.currentTimeMillis();

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonSwitchTab(double x, double y, int width, int height, String buttonText,
                           EnumUtils.GuiTab tab, EnumUtils.GuiTab currentTab) {
        super(0, (int)x, (int)y, width, height, buttonText);
        this.width = width;
        this.height = height;
        this.currentTab = currentTab;
        this.tab = tab;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            float alphaMultiplier = getAlphaMultiplier(timeOpened);
            hovered = isHovered(mouseX, mouseY);
            if (currentTab == tab) hovered = false;
            if (alphaMultiplier < 0.1) alphaMultiplier = 0.1F;
            int boxColor = main.getUtils().getDefaultBlue((int)(alphaMultiplier*50));
            int fontColor = main.getUtils().getDefaultBlue(
                    (int) (alphaMultiplier * (currentTab != tab ? 255 : 127))
            );
            if (hovered) {
                fontColor = new Color(255, 255, 160, (int)(alphaMultiplier*255)).getRGB();
            }
            drawRect(xPosition, yPosition, xPosition+width, yPosition+height, boxColor);
            float scale = 1.4F;
            float scaleMultiplier = 1/scale;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            GlStateManager.enableBlend();
            drawCenteredString(
                    mc.fontRendererObj,
                    displayString,
                    (int) ((xPosition + (float) width / 2) * scaleMultiplier),
                    (int) ((yPosition + (this.height - (8 / scaleMultiplier)) / 2) * scaleMultiplier),
                    fontColor
            );
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (currentTab != tab) super.playPressSound(soundHandlerIn);
    }

}
