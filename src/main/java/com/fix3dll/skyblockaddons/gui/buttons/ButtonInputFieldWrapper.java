package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.core.ColorCode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

import java.util.List;

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
        editBox.mouseClicked(mouseX, mouseY, button);
        editBox.setFocused(this.isHovered);
        this.playDownSound(MC.getSoundManager());

        return editBox.isFocused();
    }

    protected void keyTyped(char typedChar, int keyCode) {
        if (editBox.isFocused()) {
            editBox.keyPressed(typedChar, keyCode, /* FIXME */ 0);
        }
        textUpdated.onUpdate(editBox.getValue());
    }

    public void updateScreen() {
        editBox.moveCursorToEnd(false);
    }

    public static void callKeyTyped(List<? extends GuiEventListener> buttonList, char typedChar, int keyCode) {
        for (GuiEventListener button : buttonList) {
            if (button instanceof ButtonInputFieldWrapper) {
                ((ButtonInputFieldWrapper) button).keyTyped(typedChar, keyCode);
            }
        }
    }

    public static void callUpdateScreen(List<? extends GuiEventListener> buttonList) {
        for (GuiEventListener button : buttonList) {
            if (button instanceof ButtonInputFieldWrapper) {
                ((ButtonInputFieldWrapper) button).updateScreen();
            }
        }
    }

}