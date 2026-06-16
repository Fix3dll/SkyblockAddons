package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.gui.screens.SettingsGui;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class ButtonSettingToggle extends ButtonFeatureToggle {

    private final FeatureSetting setting;

    public ButtonSettingToggle(double x, double y, String buttonText, FeatureSetting setting) {
        super(x, y, setting.isUniversal() ? setting.getUniversalFeature() : setting.getRelatedFeature());
        setMessage(Component.literal(buttonText));
        this.setting = setting;
        this.isEnabled = () -> feature.isEnabled(setting);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(
                MC.font,
                getMessage(),
                getX() + width / 2,
                getY() - 10,
                ColorUtils.getDefaultBlue(255)
        );
    }

    @Override
    public void onClick(@NonNull MouseButtonEvent event, boolean isDoubleClick) {
        if (this.isHovered && this.feature != null) {
            if (feature.isDisabled(setting)) {
                feature.set(setting, true);
            } else {
                feature.set(setting, false);
            }
            if (setting == FeatureSetting.CLASS_COLORED_TEAMMATE && MC.gui.screen() instanceof SettingsGui settingsGui) {
                settingsGui.setReInit(true);
            }
            this.animationButtonClicked = System.currentTimeMillis();
        }
    }

}