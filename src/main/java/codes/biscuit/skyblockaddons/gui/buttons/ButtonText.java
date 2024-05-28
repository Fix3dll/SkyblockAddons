package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.chroma.ManualChromaManager;
import codes.biscuit.skyblockaddons.core.chroma.MulticolorShaderManager;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenShader;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.renderer.GlStateManager;

class ButtonText extends ButtonFeature {

    /**
     * Create a button that displays text.
     */
    ButtonText(int buttonId, int x, int y, String buttonText, Feature feature) {
        super(buttonId, x, y, buttonText, feature);
    }

    void drawButtonBoxAndText(int boxColor, int alpha, float scale, int fontColor) {
        boolean isChroma = boxColor == ManualChromaManager.getChromaColor(0, 0, alpha);
        if (isChroma) {
            if (MulticolorShaderManager.getInstance().shouldUseChromaShaders()
                    && SkyblockAddons.getInstance().getConfigValues().getChromaMode() == EnumUtils.ChromaMode.FADE) {
                ShaderManager.getInstance().enableShader(ChromaScreenShader.class);
                drawRect(xPosition, yPosition, xPosition + width, yPosition + height, boxColor);
                ShaderManager.getInstance().disableShader();
            } else {
                ButtonColorBox.drawChromaRect(xPosition, yPosition, xPosition + width, yPosition + height, boxColor);
            }
        } else {
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, boxColor);
        }
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        //noinspection IntegerDivisionInFloatingPointContext
        DrawUtils.drawCenteredText(
                displayString,
                ((xPosition + width / 2) / scale),
                ((yPosition + (height - (8 * scale)) / 2) / scale),
                fontColor
        );
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
