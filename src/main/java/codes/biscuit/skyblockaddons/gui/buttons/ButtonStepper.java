package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.Color;
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
        super(0, (int)x, (int)y, displayString);
        if (callback == null) {
            throw new IllegalArgumentException("ButtonStepper's callback cannot be null!");
        }
        this.width = width;
        this.height = height;
        this.callback = callback;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        GlStateManager.enableBlend();
        int boxColor, fontColor, boxAlpha;
        this.hovered = isHovered(mouseX, mouseY);
        for (Modifier modifier : Modifier.values()) {
            hitMaximum = hitMaximum(modifier, mouseX, mouseY);
            boolean hovered = modifier == Modifier.SUBTRACT ? isOverSubtractButton(mouseX, mouseY) : isOverAddButton(mouseX, mouseY);

            if (hovered && !hitMaximum) {
                fontColor = ColorCode.WHITE.getColor();
                boxAlpha = 170;
            } else {
                fontColor = new Color(255, 255, 160, 255).getRGB();
                boxAlpha = 100;
            }

            if (hitMaximum) {
                boxColor = ColorCode.GRAY.getColor(boxAlpha);
                fontColor = new Color(255, 255, 160, 255).getRGB();
            } else {
                if (modifier == Modifier.SUBTRACT) {
                    boxColor = ColorCode.RED.getColor(boxAlpha);
                } else {
                    boxColor = ColorCode.GREEN.getColor(boxAlpha);
                }
            }

            int startX;
            if (modifier == Modifier.SUBTRACT) {
                startX = xPosition;
            } else {
                startX = xPosition + width - height;
            }
            //noinspection SuspiciousNameCombination
            drawButtonBoxAndText(modifier.displayString, startX, yPosition, height, height, boxColor, 1F, fontColor);
        }

        boxColor = main.getUtils().getDefaultColor(100);
        int stringWidth = mc.fontRendererObj.getStringWidth(displayString);
        int textBoxWidth = width - 2 * (SPACER + height);
        float scale = stringWidth > WIDTH_LIMIT ? 1F / (stringWidth / WIDTH_LIMIT) : 1F;
        drawButtonBoxAndText(displayString, xPosition + height + SPACER, yPosition, textBoxWidth, height, boxColor, scale, ColorCode.WHITE.getColor());
        GlStateManager.disableBlend();
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (isOverSubtractButton(mouseX, mouseY)) {
            callback.accept(Modifier.SUBTRACT);
            return true;
        } else if (isOverAddButton(mouseX, mouseY)) {
            callback.accept(Modifier.ADD);
            return true;
        }
        return false;
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (!hitMaximum) {
            super.playPressSound(soundHandlerIn);
        }
    }

    private boolean hitMaximum(Modifier modifier, int mouseX, int mouseY) {
        int warningSeconds = Feature.WARNING_TIME.numberValue().intValue();
        switch (modifier) {
            case SUBTRACT:
                return isOverSubtractButton(mouseX, mouseY) && warningSeconds == 1;
            case ADD:
                return isOverAddButton(mouseX, mouseY) && warningSeconds == 99;
            default:
                return false;
        }
    }

    /**
     * @return Whether the given mouse position is hovering over the left arrow button
     */
    private boolean isOverSubtractButton(int mouseX, int mouseY) {
        return mouseX >= xPosition
                && mouseX < xPosition + height
                && mouseY >= yPosition
                && mouseY < yPosition + height;
    }

    /**
     * @return Whether the given mouse position is hovering over the right arrow button
     */
    private boolean isOverAddButton(int mouseX, int mouseY) {
        return mouseX >= xPosition + width - height
                && mouseX < xPosition + width
                && mouseY >= yPosition
                && mouseY < yPosition + height;
    }

    public enum Modifier {
        SUBTRACT("-"),
        ADD("+");

        private final String displayString;

        Modifier(String displayString) {
            this.displayString = displayString;
        }
    }
}