package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.gui.screens.ColorSelectionGui;
import codes.biscuit.skyblockaddons.gui.screens.SettingsGui;
import codes.biscuit.skyblockaddons.gui.screens.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;
import java.util.EnumSet;

public class ButtonOpenColorMenu extends ButtonFeature {

    private static final float WIDTH_LIMIT = SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10F;
    private static final EnumSet<Feature> ENCHANT_COLOR_FEATURES = EnumSet.of(Feature.ENCHANTMENT_PERFECT_COLOR,
            Feature.ENCHANTMENT_GREAT_COLOR,Feature.ENCHANTMENT_GOOD_COLOR, Feature.ENCHANTMENT_POOR_COLOR,
            Feature.ENCHANTMENT_COMMA_COLOR);

    /**
     * Create a button that displays the color of whatever feature it is assigned to.
     */
    public ButtonOpenColorMenu(double x, double y, int width, int height, String buttonText, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        this.hovered = isHovered(mouseX, mouseY);
        int fontColor, boxAlpha;
        if (this.hovered) {
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

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered && this.feature != null) {
            if (Minecraft.getMinecraft().currentScreen instanceof SettingsGui) {
                SettingsGui gui = (SettingsGui) Minecraft.getMinecraft().currentScreen;
                gui.setClosingGui(true);
                // Temp fix until feature re-write. Open a color selection panel specific to the color setting
                Feature toOpen = ENCHANT_COLOR_FEATURES.contains(this.feature) ? this.feature : gui.getFeature();
                mc.displayGuiScreen(new ColorSelectionGui(toOpen, EnumUtils.GUIType.SETTINGS, gui.getLastTab(), gui.getLastPage()));
            }
            return true;
        }
        return false;
    }
}
