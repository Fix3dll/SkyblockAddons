package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.gui.screens.SettingsGui;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ButtonSettingToggle extends ButtonFeatureToggle {

    private final FeatureSetting setting;

    public ButtonSettingToggle(double x, double y, String buttonText, FeatureSetting setting) {
        super(x, y, setting.isUniversal() ? setting.getUniversalFeature() : setting.getRelatedFeature());
        setMessage(Component.literal(buttonText));
        this.setting = setting;
        this.isEnabled = () -> feature.isEnabled(setting);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(
                MC.font,
                getMessage(),
                getX() + width / 2,
                getY() - 10,
                ColorUtils.getDefaultBlue(255)
        );
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.isHovered && this.feature != null) {
            if (feature.isDisabled(setting)) {
                feature.set(setting, true);
            } else {
                feature.set(setting, false);
            }
            if (setting == FeatureSetting.CLASS_COLORED_TEAMMATE && MC.screen instanceof SettingsGui settingsGui) {
                settingsGui.setReInit(true);
            }
            this.animationButtonClicked = System.currentTimeMillis();
        }
    }
}