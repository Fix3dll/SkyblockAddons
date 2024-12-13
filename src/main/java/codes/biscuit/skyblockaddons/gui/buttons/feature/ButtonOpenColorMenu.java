package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.gui.screens.SkyblockAddonsGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;

public class ButtonOpenColorMenu extends ButtonFeature {

    private static final float WIDTH_LIMIT = SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10F;

    /**
     * Create a button that displays the color of whatever feature it is assigned to.
     */
    public ButtonOpenColorMenu(double x, double y, int width, int height, String buttonText, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.width = width;
        this.height = height;
        this.feature = feature;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        hovered = isHovered(mouseX, mouseY);
        int fontColor, boxAlpha;
        if (hovered) {
            boxAlpha = 170;
            fontColor = new Color(255, 255, 160, 255).getRGB();
        } else {
            boxAlpha = 100;
            fontColor = new Color(224, 224, 224, 255).getRGB();
        }
        int boxColor = SkyblockAddons.getInstance().getConfigValues().getColor(feature, boxAlpha);
        // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
        GlStateManager.enableBlend();
        int stringWidth = mc.fontRendererObj.getStringWidth(displayString);
        float scale = stringWidth > WIDTH_LIMIT ? 1 / (stringWidth / WIDTH_LIMIT) : 1;
        drawButtonBoxAndText(boxColor, boxAlpha, scale, fontColor);
    }
}
