package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.gui.buttons.SkyblockAddonsButton;
import lombok.Getter;

@Getter
public class ButtonFeature extends SkyblockAddonsButton {

    // The feature that this button moves.
    public Feature feature;

    /**
     * Create a button that is assigned a feature (to toggle/change color etc.).
     */
    public ButtonFeature(int buttonId, int x, int y, String buttonText, Feature feature) {
        super(buttonId, x, y, buttonText);
        if (feature == null) {
            throw new IllegalArgumentException("ButtonFeature's feature cannot be null!");
        }
        this.feature = feature;
    }

}
