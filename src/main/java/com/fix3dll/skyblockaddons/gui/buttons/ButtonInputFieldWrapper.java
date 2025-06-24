package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.core.ColorCode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        editBox.setFocused(this.isMouseOver(mouseX, mouseY));
        return editBox.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (editBox.isFocused()) {
            boolean consumed = editBox.charTyped(codePoint, modifiers);
            textUpdated.onUpdate(editBox.getValue());
            return consumed;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editBox.isFocused()) {
            return editBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

}