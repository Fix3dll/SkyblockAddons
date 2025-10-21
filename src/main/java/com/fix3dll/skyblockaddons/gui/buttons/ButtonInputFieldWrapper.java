package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.core.ColorCode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

public class ButtonInputFieldWrapper extends SkyblockAddonsButton {

    private final EditBox editBox;
    private final UpdateCallback<String> textUpdated;

    public ButtonInputFieldWrapper(int x, int y, int w, int h, String buttonText, String placeholderText, int maxLength,
                                   boolean focused, UpdateCallback<String> textUpdated) {
        super(x, y, Component.literal(buttonText));
        this.textUpdated = textUpdated;

        editBox = new EditBox(MC.font, x, y, w, h, Component.empty());
        editBox.setMaxLength(maxLength);
        editBox.setFocused(focused);
        editBox.setValue(buttonText);
        if (!StringUtil.isNullOrEmpty(placeholderText)) {
            editBox.setHint(Component.literal(placeholderText).withColor(ColorCode.DARK_GRAY.getColor()));
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        editBox.renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        editBox.setFocused(this.isMouseOver(event.x(), event.y()));
        return editBox.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (editBox.isFocused()) {
            boolean consumed = editBox.charTyped(event);
            textUpdated.onUpdate(editBox.getValue());
            return consumed;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (editBox.isFocused()) {
            return editBox.keyPressed(event);
        }
        return false;
    }

}