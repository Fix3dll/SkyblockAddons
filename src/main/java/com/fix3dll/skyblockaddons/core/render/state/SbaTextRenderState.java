package com.fix3dll.skyblockaddons.core.render.state;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.client.gui.render.state.ScreenArea;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public final class SbaTextRenderState extends GuiTextRenderState implements ScreenArea {

    private static final Minecraft MC = Minecraft.getInstance();

    public final Font font = MC.font;
    public final FormattedCharSequence text;
    public final Matrix3x2f pose;
    public final float floatX;
    public final float floatY;
    public final int color;
    public final int backgroundColor;
    public final boolean dropShadow;
    @Nullable
    public final ScreenRectangle scissor;
    @Nullable
    private Font.PreparedText preparedText;
    @Nullable
    private ScreenRectangle bounds;

    public SbaTextRenderState(
            FormattedCharSequence text, Matrix3x2f pose, float floatX, float floatY, int color, int backgroundColor, boolean dropShadow, @Nullable ScreenRectangle scissor
    ) {
        super(MC.font, text, new Matrix3x2f(pose), Math.round(floatX), Math.round(floatY), color, backgroundColor, dropShadow, scissor);
        this.text = text;
        this.pose = new Matrix3x2f(pose);
        this.floatX = floatX;
        this.floatY = floatY;
        this.color = color;
        this.backgroundColor = backgroundColor;
        this.dropShadow = dropShadow;
        this.scissor = scissor;
    }

    @Override
    public Font.@NotNull PreparedText ensurePrepared() {
        if (this.preparedText == null) {
            this.preparedText = this.font.prepareText(this.text, this.floatX, this.floatY, this.color, this.dropShadow, this.backgroundColor);
            ScreenRectangle screenRectangle = this.preparedText.bounds();
            if (screenRectangle != null) {
                screenRectangle = screenRectangle.transformMaxBounds(this.pose);
                this.bounds = this.scissor != null ? this.scissor.intersection(screenRectangle) : screenRectangle;
            }
        }

        return this.preparedText;
    }

    @Nullable
    @Override
    public ScreenRectangle bounds() {
        this.ensurePrepared();
        return this.bounds;
    }

}