package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.gui.screens.ColorSelectionGui;
import com.fix3dll.skyblockaddons.gui.screens.SettingsGui;
import com.fix3dll.skyblockaddons.gui.screens.SkyblockAddonsGui;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.function.Function;

public class ButtonOpenColorMenu extends ButtonFeature {

    private static final float WIDTH_LIMIT = SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10F;

    private final FeatureSetting setting;
    private final Function<Integer, Integer> boxColorSupplier;

    /**
     * Create a button that displays the color of whatever feature it is assigned to.
     */
    public ButtonOpenColorMenu(double x, double y, int width, int height, String buttonText, Feature feature) {
        super((int) x, (int) y, Component.literal(buttonText), feature);
        this.width = width;
        this.height = height;
        this.setting = null;
        this.boxColorSupplier = feature::getColor;
    }

    /**
     * Create a button for {@link Feature#ENCHANTMENT_LORE_PARSING}'s {@link FeatureSetting}'s or similar
     */
    public ButtonOpenColorMenu(double x, double y, int width, int height, String buttonText, FeatureSetting setting) {
        super((int) x, (int) y, Component.literal(buttonText), setting.getRelatedFeature());
        this.width = width;
        this.height = height;
        this.setting = setting;
        this.boxColorSupplier = (boxAlpha) -> {
            Object color = feature.get(setting);
            if (color instanceof ColorCode) {
                return ((ColorCode) color).getColor(boxAlpha);
            } else {
                return ARGB.color(boxAlpha, ((Number) color).intValue());
            }
        };
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = isHovered(mouseX, mouseY);
        int fontColor, boxAlpha;
        if (this.isHovered) {
            boxAlpha = 170;
            fontColor = ARGB.color(255, 255, 255, 160);
        } else {
            boxAlpha = 100;
            fontColor = ARGB.color(255, 224, 224, 224);
        }
        int boxColor = boxColorSupplier.apply(boxAlpha);
        int stringWidth = MC.font.width(getMessage());
        this.scale = stringWidth > WIDTH_LIMIT ? 1 / (stringWidth / WIDTH_LIMIT) : 1;

        drawButtonBoxAndText(graphics, boxColor, scale, fontColor);
    }

    /**
     * Code to perform the button toggles, openings of other gui's/pages, and language changes.
     */
    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        if (this.feature != null && MC.screen instanceof SettingsGui gui) {
            gui.setClosingGui(true);
            if (this.setting != null) {
                MC.setScreen(new ColorSelectionGui(setting, EnumUtils.GUIType.SETTINGS, gui.getLastTab(), gui.getLastPage()));
            } else {
                MC.setScreen(new ColorSelectionGui(feature, EnumUtils.GUIType.SETTINGS, gui.getLastTab(), gui.getLastPage()));
            }
        }
    }

}