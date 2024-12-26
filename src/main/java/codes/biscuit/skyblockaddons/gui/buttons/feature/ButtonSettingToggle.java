package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.GuiIngameForge;

public class ButtonSettingToggle extends ButtonFeatureToggle {

    public ButtonSettingToggle(double x, double y, String buttonText, Feature feature) {
        super(x, y, feature);
        this.displayString = buttonText;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        drawCenteredString(
                mc.fontRendererObj,
                this.displayString,
                xPosition + width / 2,
                yPosition - 10,
                ColorUtils.getDefaultBlue(255)
        );
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered && this.feature != null && !this.feature.isRemoteDisabled()) {
            if (feature.isDisabled()) {
                feature.setEnabled(true);
            } else {
                feature.setEnabled(false);

                if (feature == Feature.HIDE_FOOD_ARMOR_BAR) { // Reset the vanilla bars when disabling these two features.
                    GuiIngameForge.renderArmor = true; // The food gets automatically enabled, no need to include it.
                } else if (feature == Feature.HIDE_HEALTH_BAR) {
                    GuiIngameForge.renderHealth = true;
                }
            }
            onClick();
            return true;
        }
        return false;
    }

}
