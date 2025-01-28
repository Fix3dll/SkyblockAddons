package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.gui.screens.SettingsGui;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.GuiIngameForge;

public class ButtonSettingToggle extends ButtonFeatureToggle {

    private final FeatureSetting setting;

    public ButtonSettingToggle(double x, double y, String buttonText, FeatureSetting setting) {
        super(x, y, setting.isUniversal() ? setting.getUniversalFeature() : setting.getRelatedFeature());
        this.displayString = buttonText;
        this.setting = setting;
        this.isEnabled = () -> feature.isEnabled(setting);
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
            if (feature.isDisabled(setting)) {
                feature.set(setting, true);
            } else {
                feature.set(setting, false);

                if (feature == Feature.HIDE_FOOD_ARMOR_BAR) { // Reset the vanilla bars when disabling these two features.
                    GuiIngameForge.renderArmor = true; // The food gets automatically enabled, no need to include it.
                } else if (feature == Feature.HIDE_HEALTH_BAR) {
                    GuiIngameForge.renderHealth = true;
                }
            }

            if (setting == FeatureSetting.CLASS_COLORED_TEAMMATE) {
                ((SettingsGui) mc.currentScreen).setReInit(true);
            }
            this.animationButtonClicked = System.currentTimeMillis();
            return true;
        }
        return false;
    }

}
