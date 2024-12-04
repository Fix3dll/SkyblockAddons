package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonFeatureToggle;
import net.minecraft.client.Minecraft;

public class ButtonSettingToggle extends ButtonFeatureToggle {

    public ButtonSettingToggle(double x, double y, String buttonText, Feature feature) {
        super(x, y, feature);
        displayString = buttonText;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        drawCenteredString(
                mc.fontRendererObj,
                displayString,
                xPosition + width / 2,
                yPosition - 10,
                main.getUtils().getDefaultBlue(255)
        );
    }
}
