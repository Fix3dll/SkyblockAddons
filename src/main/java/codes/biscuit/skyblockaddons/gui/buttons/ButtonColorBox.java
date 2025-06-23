package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.core.chroma.MulticolorShaderManager;
import codes.biscuit.skyblockaddons.core.chroma.ManualChromaManager;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenShader;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils.ChromaMode;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * This button is for when you are choosing one of the 16 color codes.
 */
@Getter
public class ButtonColorBox extends SkyblockAddonsButton {

    public static final int WIDTH = 40;
    public static final int HEIGHT = 20;

    private final ColorCode color;

    public ButtonColorBox(int x, int y, ColorCode color) {
        super(0, x, y, null);
        this.width = WIDTH;
        this.height = HEIGHT;
        this.color = color;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        hovered = isHovered(mouseX, mouseY);
        drawColorRect(xPosition, yPosition, xPosition + width, yPosition + height, hovered ? color.getColor() : color.getColor(127));
    }

    public static void drawColorRect(int left, int top, int right, int bottom, int color) {
        boolean isChromaColor = color == ColorCode.CHROMA.getColor(ColorUtils.getAlpha(color));

        GlStateManager.enableBlend();
        if (isChromaColor) {
            if (MulticolorShaderManager.getInstance().shouldUseChromaShaders() && Feature.CHROMA_MODE.getValue() == ChromaMode.FADE) {
                ShaderManager.getInstance().enableShader(ChromaScreenShader.class);
                drawRect(left, top, right, bottom, color);
                ShaderManager.getInstance().disableShader();
            } else {
                drawChromaRect(left, top, right, bottom, ColorUtils.getAlpha(color));
            }
        } else {
            drawRect(left, top, right, bottom, color);
        }
        GlStateManager.disableBlend();
    }

    public static void drawChromaRect(int left, int top, int right, int bottom, int alpha) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int colorLB = ManualChromaManager.getChromaColor(left, bottom, 1);
        int colorRB = ManualChromaManager.getChromaColor(right, bottom, 1);
        int colorLT = ManualChromaManager.getChromaColor(left, top, 1);
        int colorRT = ManualChromaManager.getChromaColor(right, top, 1);
        int colorMM = ManualChromaManager.getChromaColor(Math.floorDiv((right+left), 2), Math.floorDiv((top+bottom), 2), 1);
        // First triangle
        worldrenderer.pos(right, bottom, 0.0D).color(ColorUtils.getRed(colorRB), ColorUtils.getGreen(colorRB), ColorUtils.getBlue(colorRB), alpha).endVertex();
        worldrenderer.pos(Math.floorDiv((right+left), 2), Math.floorDiv((top+bottom), 2), 0.0D).color(ColorUtils.getRed(colorMM), ColorUtils.getGreen(colorMM), ColorUtils.getBlue(colorMM), alpha).endVertex();
        worldrenderer.pos(left, top, 0.0D).color(ColorUtils.getRed(colorLT), ColorUtils.getGreen(colorLT), ColorUtils.getBlue(colorLT), alpha).endVertex();
        worldrenderer.pos(left, bottom, 0.0D).color(ColorUtils.getRed(colorLB), ColorUtils.getGreen(colorLB), ColorUtils.getBlue(colorLB), alpha).endVertex();
        // 2nd triangle
        worldrenderer.pos(right, bottom, 0.0D).color(ColorUtils.getRed(colorRB), ColorUtils.getGreen(colorRB), ColorUtils.getBlue(colorRB), alpha).endVertex();
        worldrenderer.pos(right, top, 0.0D).color(ColorUtils.getRed(colorRT), ColorUtils.getGreen(colorRT), ColorUtils.getBlue(colorRT), alpha).endVertex();
        worldrenderer.pos(left, top, 0.0D).color(ColorUtils.getRed(colorLT), ColorUtils.getGreen(colorLT), ColorUtils.getBlue(colorLT), alpha).endVertex();
        worldrenderer.pos(Math.floorDiv((right+left), 2), Math.floorDiv((top+bottom), 2), 0.0D).color(ColorUtils.getRed(colorMM), ColorUtils.getGreen(colorMM), ColorUtils.getBlue(colorMM), alpha).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
