package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.gui.screens.ColorSelectionGui;
import codes.biscuit.skyblockaddons.gui.screens.SettingsGui;
import codes.biscuit.skyblockaddons.gui.screens.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;
import java.util.function.Function;

public class ButtonOpenColorMenu extends ButtonFeature {

    private static final float WIDTH_LIMIT = SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10F;

    private final FeatureSetting setting;
    private final Function<Integer, Integer> boxColorSupplier;

    /**
     * Create a button that displays the color of whatever feature it is assigned to.
     */
    public ButtonOpenColorMenu(double x, double y, int width, int height, String buttonText, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.width = width;
        this.height = height;
        this.setting = null;
        this.boxColorSupplier = feature::getColor;
    }

    /**
     * Create a button for {@link Feature#ENCHANTMENT_LORE_PARSING}'s {@link FeatureSetting}'s or similar
     */
    public ButtonOpenColorMenu(double x, double y, int width, int height, String buttonText, FeatureSetting setting) {
        super(0, (int)x, (int)y, buttonText, setting.getRelatedFeature());
        this.width = width;
        this.height = height;
        this.setting = setting;
        this.boxColorSupplier = (boxAlpha) -> {
            Object color = feature.get(setting);
            if (color instanceof ColorCode) {
                return ((ColorCode) color).getColor(boxAlpha);
            } else {
                return ColorUtils.setColorAlpha(((Number) color).intValue(), boxAlpha);
            }
        };
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
        int boxColor = boxColorSupplier.apply(boxAlpha);
        // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
        GlStateManager.enableBlend();
        int stringWidth = mc.fontRendererObj.getStringWidth(displayString);
        float scale = stringWidth > WIDTH_LIMIT ? 1 / (stringWidth / WIDTH_LIMIT) : 1;
        drawButtonBoxAndText(boxColor, scale, fontColor);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered) {
            if (mc.currentScreen instanceof SettingsGui) {
                SettingsGui gui = (SettingsGui) mc.currentScreen;
                gui.setClosingGui(true);
                if (this.setting != null) {
                    mc.displayGuiScreen(new ColorSelectionGui(setting, EnumUtils.GUIType.SETTINGS, gui.getLastTab(), gui.getLastPage()));
                } else {
                    mc.displayGuiScreen(new ColorSelectionGui(feature, EnumUtils.GUIType.SETTINGS, gui.getLastTab(), gui.getLastPage()));
                }
            }
            return true;
        }
        return false;
    }
}
