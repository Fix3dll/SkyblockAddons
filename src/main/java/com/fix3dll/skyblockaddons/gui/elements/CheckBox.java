package com.fix3dll.skyblockaddons.gui.elements;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.render.state.BlitAbsoluteRenderState;
import com.fix3dll.skyblockaddons.listeners.RenderListener;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.joml.Matrix3x2fStack;

public class CheckBox {
    private static final ResourceLocation ICONS = SkyblockAddons.resourceLocation("gui/checkbox.png");

    @FunctionalInterface
    public interface OnToggleListener {
        void onToggle(boolean value);
    }

    /**
     * Size of the CheckBox icon
     */
    private static final int ICON_SIZE = 16;

    private final float scale;

    private final int x;
    private final int y;
    private final String text;
    private final int textWidth;
    private final int size;
    @Getter @Setter private boolean value;
    /**
     * -- SETTER --</br>
     *  Attaches a listener that gets notified whenever the CheckBox is toggled
     */
    @Setter private OnToggleListener onToggleListener;

    /**
     * @param x x position
     * @param y y position
     * @param size Desired size (height) to scale to
     * @param text Displayed text
     * @param value Default value
     */
    public CheckBox(int x, int y, int size, String text, boolean value) {
        this(x, y, size, text);
        this.value = value;
    }

    /**
     * @param x x position
     * @param y y position
     * @param size Desired size (height) to scale to
     * @param text Displayed text
     */
    CheckBox(int x, int y, int size, String text) {
        this.x = x;
        this.y = y;
        this.scale = (float) size / (float) ICON_SIZE;
        this.text = text;
        this.textWidth = Minecraft.getInstance().font.width(text);
        this.size = size;
    }

    public void draw(GuiGraphics graphics) {
        int scaledX = Math.round(x / scale);
        int scaledY = Math.round(y / scale);

        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.scale(scale, scale);

        int color = value ? ColorCode.WHITE.getColor() : ColorCode.GRAY.getColor();
        DrawUtils.drawText(graphics, text, scaledX + Math.round(size * 1.5f / scale), scaledY + (size / 2f), color);

        float uOffset = value ? 16 : 0;
        graphics.guiRenderState.submitGuiElement(
                new BlitAbsoluteRenderState(RenderPipelines.GUI_TEXTURED, RenderListener.textureSetup(ICONS), graphics.pose(), scaledX, scaledY, uOffset, 0, 16, 16, 32, 16, -1, graphics.scissorStack.peek())
        );
        poseStack.popMatrix();
    }

    public void onMouseClick(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0
                && event.x() > this.x && event.x() < this.x + this.size + this.textWidth
                && event.y() > this.y && event.y() < this.y + this.size) {
            value = !value;
            Utils.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.25, 1);
            if (onToggleListener != null) {
                onToggleListener.onToggle(value);
            }

            Utils.blockNextClick = true;
        }
    }

}