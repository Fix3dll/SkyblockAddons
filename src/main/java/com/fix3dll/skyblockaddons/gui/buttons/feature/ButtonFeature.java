package com.fix3dll.skyblockaddons.gui.buttons.feature;

import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.gui.buttons.SkyblockAddonsButton;
import lombok.Getter;
import net.minecraft.network.chat.Component;

@Getter
public abstract class ButtonFeature extends SkyblockAddonsButton {

    // The feature that this button moves.
    public Feature feature;

    /**
     * Create a button that is assigned a feature (to toggle/change color etc.).
     */
    public ButtonFeature(int x, int y, Component buttonText, Feature feature) {
        super(x, y, buttonText);
        this.feature = feature;
    }

}