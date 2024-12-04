package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonFeature;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

@Getter
public class ButtonLocation extends ButtonFeature {

    // So we know the latest hovered feature (used for arrow key movement).
    @Getter private static Feature lastHoveredFeature = null;

    private final SkyblockAddons main = SkyblockAddons.getInstance();

    private float boxXOne;
    private float boxXTwo;
    private float boxYOne;
    private float boxYTwo;

    private float scale;

    /**
     * Create a button that allows you to change the location of a GUI element.
     */
    public ButtonLocation(Feature feature) {
        super(-1, 0, 0, null, feature);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        // If the feature is disabled, don't draw in the "Edit GUI Location" section
        if (feature.isDisabled())
            return;

        float scale = main.getConfigValues().getGuiScale(feature);
        main.getRenderListener().drawFeature(feature, scale,this);

        if (hovered) {
            lastHoveredFeature = feature;
        }
    }

    /**
     * This just updates the hovered status and draws the box around each feature. To avoid repetitive code.
     */
    public void checkHoveredAndDrawBox(float boxXOne, float boxXTwo, float boxYOne, float boxYTwo, float scale) {
        checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale, 1F, 1F);
    }

    public void checkHoveredAndDrawBox(float boxXOne, float boxXTwo, float boxYOne, float boxYTwo, float scale, float scaleX, float scaleY) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        float minecraftScale = sr.getScaleFactor();
        float floatMouseX = Mouse.getX() / minecraftScale;
        float floatMouseY = (Minecraft.getMinecraft().displayHeight - Mouse.getY()) / minecraftScale;

        hovered = floatMouseX >= boxXOne * scale * scaleX && floatMouseY >= boxYOne * scale * scaleY
                && floatMouseX < boxXTwo * scale * scaleX && floatMouseY < boxYTwo * scale * scaleY;
        int boxAlpha = hovered ? 120 : 70;
        int boxColor = ColorCode.GRAY.getColor(boxAlpha);
        DrawUtils.drawRectAbsolute(boxXOne, boxYOne, boxXTwo, boxYTwo, boxColor);

        this.boxXOne = boxXOne;
        this.boxXTwo = boxXTwo;
        this.boxYOne = boxYOne;
        this.boxYTwo = boxYTwo;
        this.scale = scale;
    }


    /**
     * Because the box changes with the scale, have to override this.
     */
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return this.enabled && this.visible && hovered;
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {}
}
