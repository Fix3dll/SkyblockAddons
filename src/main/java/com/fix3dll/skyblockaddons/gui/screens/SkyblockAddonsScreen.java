package com.fix3dll.skyblockaddons.gui.screens;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonSocial;
import com.fix3dll.skyblockaddons.gui.buttons.SkyblockAddonsButton;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class SkyblockAddonsScreen extends Screen {

    protected static final Minecraft MC = Minecraft.getInstance();
    protected static final SkyblockAddons main = SkyblockAddons.getInstance();

    public static final ResourceLocation LOGO = SkyblockAddons.resourceLocation("logo.png");
    public static final ResourceLocation LOGO_GLOW = SkyblockAddons.resourceLocation("logoglow.png");
    private static final String FORMATTED_VERSION = "v" + SkyblockAddons.METADATA.getVersion().toString()
            .replaceAll("\\+\\d+(?:\\.\\d+)?", "") // BUILD NUMBER
            .replace("alpha", "a")
            .replace("beta", "b") + " reborn";
    private static final int FADE_MILLIS = 500;
    private static final int TITLE_ANIMATION_MILLIS = 4000;

    // Used to calculate the transparency when fading in.
    final long timeOpened = System.currentTimeMillis();

    boolean firstDraw = true;

    protected SkyblockAddonsScreen(Component title) {
        super(title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (firstDraw) {
            sortButtonList();
            firstDraw = false;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    }

    /** Returns the last event listener that intersects with the mouse coordinates. */
    @Override
    public @NotNull Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        for (GuiEventListener guiEventListener : this.children().reversed()) {
            if (guiEventListener instanceof AbstractWidget widget && widget.isHovered()) {
                return Optional.of(guiEventListener);
            }
        }

        return Optional.empty();
    }

    public float calculateAlphaMultiplier() {
        if (main.getUtils().isFadingIn()) {
            long timeSinceOpen = System.currentTimeMillis() - timeOpened;
            if (timeSinceOpen <= FADE_MILLIS) {
                return (float) timeSinceOpen / (FADE_MILLIS * 2);
            }
        }
        return 0.5F;
    }

    protected void drawGradientBackground(GuiGraphics graphics, int alpha) {
        drawGradientBackground(graphics, (int) (alpha * 0.5), alpha);
    }

    protected void drawGradientBackground(GuiGraphics graphics, int startAlpha, int endAlpha) {
        int startColor = ARGB.color(startAlpha, 0, 0, 0);
        int endColor = ARGB.color(endAlpha, 0, 0, 0);
        graphics.fillGradient(0, 0, MC.getWindow().getWidth(), MC.getWindow().getHeight(), startColor, endColor);
    }

    /**
     * Draws the default text at the top at bottoms of the GUI.
     * @param screen The screen to draw the text on.
     */
    protected void drawDefaultTitleText(GuiGraphics graphics, Screen screen, int alpha) {
        int defaultBlue = ColorUtils.getDefaultBlue(alpha);

        int height = 85;
        int width = height*2;
        Window window = MC.getWindow();

        graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO, (int) (window.getGuiScaledWidth() / 2F - width / 2F), 5, 0, 0, width, height, width, height);

        float glowAlpha;
        glowAlpha = System.currentTimeMillis() % TITLE_ANIMATION_MILLIS;
        if (glowAlpha > TITLE_ANIMATION_MILLIS / 2F) {
            glowAlpha = (TITLE_ANIMATION_MILLIS - glowAlpha) / (TITLE_ANIMATION_MILLIS / 2F);
        } else {
            glowAlpha = glowAlpha / (TITLE_ANIMATION_MILLIS / 2F);
        }

        int color = ARGB.white(glowAlpha);
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOGO_GLOW, (int) (window.getGuiScaledWidth() / 2F - width / 2F), 5, 0, 0, width, height, width, height, color);

        drawScaledString(graphics, screen, FORMATTED_VERSION, 55, defaultBlue, 1.3F, 170 - MC.font.width(FORMATTED_VERSION), false);
    }

    /**
     * Sorts buttons by {@link SkyblockAddonsButton#priority}. They are sorted so that the ones with higher priority are
     * displayed last and the ones with lower priority are displayed first.
     */
    protected void sortButtonList() {
        renderables.sort((b1, b2) -> {
            boolean b1IsCustom = b1 instanceof SkyblockAddonsButton;
            boolean b2IsCustom = b2 instanceof SkyblockAddonsButton;

            if (b1IsCustom && b2IsCustom) {
                return Integer.compare(((SkyblockAddonsButton) b1).priority, ((SkyblockAddonsButton) b2).priority);
            }

            if (b1IsCustom) return -1;
            if (b2IsCustom) return 1;

            return 0;
        });
    }

    static void drawScaledString(GuiGraphics graphics, Screen guiScreen, String text, int y, int color, float scale, int xOffset) {
        drawScaledString(graphics, guiScreen, text, y, color, scale, xOffset, true);
    }

    /**
     * Draws a centered string at the middle of the screen on the x axis, with a specified scale and location.
     *
     * @param text The text to draw.
     * @param y The y level to draw the text/
     * @param color The text color.
     * @param scale The scale to draw the text.
     * @param xOffset The offset from the center x that the text should be drawn at.
     */
    static void drawScaledString(GuiGraphics graphics, Screen screen, String text, int y, int color, float scale, int xOffset, boolean centered) {
        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.scale(scale);
        if (centered) {
            graphics.drawCenteredString(
                    MC.font,
                    text,
                    Math.round((float) screen.width / 2 / scale) + xOffset,
                    Math.round((float) y / scale),
                    color
            );
        } else {
            graphics.drawString(
                    MC.font,
                    text,
                    Math.round((float) screen.width / 2 / scale) + xOffset,
                    Math.round((float) y / scale),
                    color,
                    true
            );
        }
        poseStack.popMatrix();
    }

    public void addSocials(Consumer<AbstractWidget> adder) {
        List<AbstractWidget> socials = List.of(
//                new ButtonSocial(width / 2 + 175, 30, EnumUtils.Social.DISCORD),
                new ButtonSocial(width / 2D + 125, 30, EnumUtils.Social.MODRINTH),
                new ButtonSocial(width / 2D + 150, 30, EnumUtils.Social.GITHUB),
                new ButtonSocial(width / 2D + 175, 30, EnumUtils.Social.BUYMEACOFFEE)
        );

        for (AbstractWidget social : socials) {
            adder.accept(social);
        }
    }

}