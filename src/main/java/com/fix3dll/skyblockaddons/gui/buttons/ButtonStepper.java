package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.function.Consumer;

public class ButtonStepper extends SkyblockAddonsButton {

    private static final float WIDTH_LIMIT = 90.0F;
    public static final int SPACER = 5;

    private final Consumer<Modifier> callback;

    private boolean hitMaximum = false;

    /**
     * Create a button for adding or subtracting a number. The width of the modifier is the same as the height and
     * the spacing is 5 pixels. </br>
     * When creating a ButtonStepper, remember that the starting X of the text box will be x + (height + SPACER)
     */
    public ButtonStepper(double x, double y, int width, int height, String displayString, Consumer<Modifier> callback) {
        super((int) x, (int) y, width, height, Component.literal(displayString));
        if (callback == null) {
            throw new IllegalArgumentException("ButtonStepper's callback cannot be null!");
        }
        this.callback = callback;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int boxColor, fontColor, boxAlpha;
        this.isHovered = isMouseOver(mouseX, mouseY);
        for (Modifier modifier : Modifier.values()) {
            hitMaximum = hitMaximum(modifier, mouseX, mouseY);
            boolean isHovered = modifier == Modifier.SUBTRACT ? isOverSubtractButton(mouseX, mouseY) : isOverAddButton(mouseX, mouseY);

            if (isHovered && !hitMaximum) {
                fontColor = ARGB.white(1F);
                boxAlpha = 170;
            } else {
                fontColor = ARGB.color(255, 255, 255, 160);
                boxAlpha = 100;
            }

            if (hitMaximum) {
                boxColor = ColorCode.GRAY.getColor(boxAlpha);
                fontColor = ARGB.color(255, 255, 255, 160);
            } else {
                if (modifier == Modifier.SUBTRACT) {
                    boxColor = ColorCode.RED.getColor(boxAlpha);
                } else {
                    boxColor = ColorCode.GREEN.getColor(boxAlpha);
                }
            }

            int startX;
            if (modifier == Modifier.SUBTRACT) {
                startX = getX();
            } else {
                startX = getX() + width - height;
            }
            //noinspection SuspiciousNameCombination
            drawButtonBoxAndText(graphics, modifier.message, startX, getY(), height, height, boxColor, 1F, fontColor);
        }

        boxColor = main.getUtils().getDefaultColor(100);
        int stringWidth = MC.font.width(getMessage());
        int textBoxWidth = width - 2 * (SPACER + height);
        float scale = stringWidth > WIDTH_LIMIT ? 1F / (stringWidth / WIDTH_LIMIT) : 1F;
        drawButtonBoxAndText(graphics, getMessage(), getX() + height + SPACER, getY(), textBoxWidth, height, boxColor, scale, ColorCode.WHITE.getColor());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isOverSubtractButton(mouseX, mouseY)) {
                callback.accept(Modifier.SUBTRACT);
                if (!hitMaximum(Modifier.SUBTRACT, mouseX, mouseY)) {
                    this.playDownSound(MC.getSoundManager());
                }
                return true;
            } else if (isOverAddButton(mouseX, mouseY)) {
                callback.accept(Modifier.ADD);
                if (!hitMaximum(Modifier.ADD, mouseX, mouseY)) {
                    this.playDownSound(MC.getSoundManager());
                }
                return true;
            }
        }
        return false;
    }

    private boolean hitMaximum(Modifier modifier, double mouseX, double mouseY) {
        int warningSeconds = Feature.WARNING_TIME.numberValue().intValue();
        return switch (modifier) {
            case SUBTRACT -> isOverSubtractButton(mouseX, mouseY) && warningSeconds == 1;
            case ADD -> isOverAddButton(mouseX, mouseY) && warningSeconds == 99;
        };
    }

    /**
     * @return Whether the given mouse position is hovering over the left arrow button
     */
    private boolean isOverSubtractButton(double mouseX, double mouseY) {
        return mouseX >= getX()
                && mouseX < getX() + height
                && mouseY >= getY()
                && mouseY < getY() + height;
    }

    /**
     * @return Whether the given mouse position is hovering over the right arrow button
     */
    private boolean isOverAddButton(double mouseX, double mouseY) {
        return mouseX >= getX() + width - height
                && mouseX < getX() + width
                && mouseY >= getY()
                && mouseY < getY() + height;
    }

    public enum Modifier {
        SUBTRACT("-"),
        ADD("+");

        private final Component message;

        Modifier(String message) {
            this.message = Component.literal(message);
        }
    }
}