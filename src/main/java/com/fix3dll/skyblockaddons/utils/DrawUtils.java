package com.fix3dll.skyblockaddons.utils;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.render.chroma.ChromaRenderType;
import com.fix3dll.skyblockaddons.core.render.chroma.ManualChromaManager;
import com.fix3dll.skyblockaddons.core.render.state.FillAbsoluteRenderState;
import com.fix3dll.skyblockaddons.core.render.state.RoundedRectRenderState;
import com.fix3dll.skyblockaddons.core.render.state.SbaTextRenderState;
import com.fix3dll.skyblockaddons.mixin.extensions.StyleExtension;
import com.fix3dll.skyblockaddons.mixin.hooks.FontHook;
import com.fix3dll.skyblockaddons.utils.EnumUtils.ChromaMode;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.TriState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.function.Function;

public class DrawUtils {

    public static final RenderPipeline CHROMA_STANDARD = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                    .withLocation(SkyblockAddons.resourceLocation("sba_chroma_standard"))
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                    .withVertexShader(SkyblockAddons.resourceLocation("chroma_standard"))
                    .withFragmentShader(SkyblockAddons.resourceLocation("chroma_standard"))
                    .withUniform("ChromaUniforms", UniformType.UNIFORM_BUFFER)
                    .withDepthWrite(true)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .build()
    );
    public static final RenderPipeline CHROMA_TEXT = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                    .withLocation(SkyblockAddons.resourceLocation("sba_chroma_text"))
                    .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withVertexShader(SkyblockAddons.resourceLocation("chroma_textured"))
                    .withFragmentShader(SkyblockAddons.resourceLocation("chroma_textured"))
                    .withUniform("ChromaUniforms", UniformType.UNIFORM_BUFFER)
                    .withSampler("Sampler0")
                    .withDepthWrite(true)
                    .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                    .build()
    );
    private static final Function<ResourceLocation, RenderType> CHROMA_TEXTURED = Util.memoize(
            (texture -> new ChromaRenderType(
                    "sba_chroma_textured",
                    RenderType.TRANSIENT_BUFFER_SIZE,
                    false,
                    false,
                    CHROMA_TEXT,
                    RenderType.CompositeState.builder()
                            .setTextureState(new RenderStateShard.TextureStateShard(texture, TriState.FALSE.toBoolean(false)))
                            .createCompositeState(false)
            ))
    );
    public static final TextColor CHROMA_TEXT_COLOR = new TextColor(ColorCode.CHROMA.getColor(), "chroma");

    public static RenderType getChromaTextured(ResourceLocation resourceLocation) {
        return CHROMA_TEXTURED.apply(resourceLocation);
    }

    public static void drawRoundedRect(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
        radius = Math.min(radius, Math.min(width, height) / 2);

        // RenderPipelines.GUI expects QUADS
        graphics.guiRenderState.submitGuiElement(new RoundedRectRenderState(
                RenderPipelines.GUI, // Standard 2D solid color quad pipeline
                TextureSetup.noTexture(),
                graphics.pose(),
                x, y, width, height, radius, color,
                graphics.scissorStack.peek()
        ));
    }

    public static void drawCylinder(PoseStack poseStack,
                                    VertexConsumer vc,
                                    double x, double y, double z,
                                    float radius,
                                    float height,
                                    SkyblockColor color) {

        // Move into eye‑space
        final Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(x - cam.x, y - cam.y, z - cam.z);
        Matrix4f pose = poseStack.last().pose();

        // Full‑bright lightmap
        final int packed = LightTexture.FULL_BRIGHT;
        int lu = packed & 0xFFFF;
        int lv = packed >>> 16;

        boolean multi = color.drawMulticolorManually();

        int rI = 255, gI = 255, bI = 255, aI = 255;  // defaults for shader / white
        if (!multi) {
            int argb = color.getColor();
            rI = ARGB.red(argb);
            gI = ARGB.green(argb);
            bI = ARGB.blue(argb);
            aI = ARGB.alpha(argb);
        }

        // Back‑to‑front heading relative to camera
        double startAngle = Math.atan2(cam.z - z, cam.x - x) + Math.PI;

        final int    SEG = 64;
        final double STEP = Math.PI * 2.0 / SEG;

        for (int seg = 0; seg < SEG / 2; seg++) {
            // positive offset
            addQuad(vc, pose, startAngle + seg * STEP, startAngle + (seg + 1) * STEP,
                    radius, height, color, multi, rI, gI, bI, aI, lu, lv);
            // negative mirror offset
            addQuad(vc, pose, startAngle - seg * STEP, startAngle - (seg + 1) * STEP,
                    radius, height, color, multi, rI, gI, bI, aI, lu, lv);
        }

        poseStack.popPose();
    }

    public static void renderOutlineAbsolute(GuiGraphics graphics, RenderPipeline renderPipeline, TextureSetup textureSetup, float x, float y, float width, float height, int thickness, int color) {
        graphics.guiRenderState.submitGuiElement(
                new FillAbsoluteRenderState(renderPipeline, textureSetup, graphics.pose(), x - thickness, y, x, y + height, color, graphics.scissorStack.peek())
        );
        graphics.guiRenderState.submitGuiElement(
                new FillAbsoluteRenderState(renderPipeline, textureSetup, graphics.pose(), x - thickness, y - thickness, x + width + thickness, y, color, graphics.scissorStack.peek())
        );
        graphics.guiRenderState.submitGuiElement(
                new FillAbsoluteRenderState(renderPipeline, textureSetup, graphics.pose(), x + width, y, x + width + thickness, y + height, color, graphics.scissorStack.peek())
        );
        graphics.guiRenderState.submitGuiElement(
                new FillAbsoluteRenderState(renderPipeline, textureSetup, graphics.pose(), x - thickness, y + height, x - thickness + width + thickness * 2, y + height + thickness, color, graphics.scissorStack.peek())
        );
    }

    public static void drawCenteredText(GuiGraphics graphics, Component text, float x, float y, int color) {
        drawText(graphics, text, x - Minecraft.getInstance().font.width(text) / 2F, y, color);
    }

    /**
     * Draws absolute text at the given position with chroma detection enabled.
     * @param graphics the current {@link GuiGraphics} context
     * @param text     the component to render; no-op if {@code null}
     * @param x        the x-coordinate
     * @param y        the y-coordinate
     * @param color    the ARGB fill color, also used to detect chroma
     * @see #drawText(GuiGraphics, Component, float, float, int, boolean)
     */
    public static void drawText(GuiGraphics graphics, Component text, float x, float y, int color) {
        drawText(graphics, text, x, y, color, false);
    }

    /**
     * Renders absolute text at the specified coordinates with support for custom styles and chroma effects.
     * <p>
     * This method handles two primary rendering modes:
     * <ul>
     * <li><b>Style Two:</b> Renders a 4-way black outline (±offset) followed by the main text pass.
     * The outline pass strips specific colors to allow uniform blending under chroma effects.</li>
     * <li><b>Default Style:</b> Renders the text with a standard shadow.</li>
     * </ul>
     * Chroma effects are applied per-character via {@link #resolveChromaSequence} unless explicitly
     * disabled by the component's style or the {@code chromaDisabled} override.
     * @param graphics       the current {@link GuiGraphics} rendering context
     * @param text           the {@link Component} to be rendered; ignored if {@code null}
     * @param x              the x-coordinate for text placement
     * @param y              the y-coordinate for text placement
     * @param color          the primary ARGB color used for the text fill and chroma detection
     * @param chromaDisabled if {@code true}, bypasses all chroma logic regardless of global settings
     */
    public static void drawText(GuiGraphics graphics, Component text, float x, float y, int color, boolean chromaDisabled) {
        if (text == null) return;

        boolean isChroma = !chromaDisabled && color == ManualChromaManager.getChromaColor(0, 0, ARGB.alpha(color));
        boolean styleTwo = Feature.TEXT_STYLE.getValue() == EnumUtils.TextStyle.STYLE_TWO;
        FormattedCharSequence originalFcs = text.getVisualOrderText();
        FormattedCharSequence finalFcs = resolveChromaSequence(originalFcs, isChroma);

        if (styleTwo) {
            FormattedCharSequence colorlessFcs = sink -> finalFcs.accept(
                    (index, style, codePoint) -> sink.accept(index, style.withColor((TextColor) null), codePoint)
            );
            int colorAlpha = Math.max(ARGB.alpha(color), 4);
            int colorBlack = ARGB.color(colorAlpha, 0, 0, 0);
            float offset = Minecraft.getInstance().options.forceUnicodeFont().get() ? 0.5F : 1.0F;
            FontHook.setHaltChroma(true);
            graphics.guiRenderState.submitText(
                    new SbaTextRenderState(colorlessFcs, graphics.pose(), x + offset, y, colorBlack, 0, false, graphics.scissorStack.peek())
            );
            graphics.guiRenderState.submitText(
                    new SbaTextRenderState(colorlessFcs, graphics.pose(), x - offset, y, colorBlack, 0, false, graphics.scissorStack.peek())
            );
            graphics.guiRenderState.submitText(
                    new SbaTextRenderState(colorlessFcs, graphics.pose(), x, y + offset, colorBlack, 0, false, graphics.scissorStack.peek())
            );
            graphics.guiRenderState.submitText(
                    new SbaTextRenderState(colorlessFcs, graphics.pose(), x, y - offset, colorBlack, 0, false, graphics.scissorStack.peek())
            );
            FontHook.setHaltChroma(false);
            graphics.guiRenderState.submitText(
                    new SbaTextRenderState(finalFcs, graphics.pose(), x, y, color, 0, false, graphics.scissorStack.peek())
            );
        } else {
            graphics.guiRenderState.submitText(
                    new SbaTextRenderState(finalFcs, graphics.pose(), x, y, color, 0, true, graphics.scissorStack.peek())
            );
        }
    }

    /**
     * Processes the sequence and applies chroma fade only where the style allows it.
     */
    public static FormattedCharSequence resolveChromaSequence(FormattedCharSequence original, boolean isChroma) {
        if (isChroma && Feature.CHROMA_MODE.getValue() == ChromaMode.FADE) {
            return sink -> original.accept((index, style, codePoint) -> {
                boolean isChromaDisabled = ((StyleExtension) (Object) style).sba$isChromaDisabled();
                Style resolvedStyle = isChromaDisabled ? style : style.withColor(DrawUtils.CHROMA_TEXT_COLOR);
                return sink.accept(index, resolvedStyle, codePoint);
            });
        }
        return original;
    }

    public static void blitAbsolute(
            PoseStack.Pose pose,
            VertexConsumer vertexConsumer,
            float x,
            float y,
            float uOffset,
            float vOffset,
            float uWidth,
            float vHeight,
            float textureWidth,
            float textureHeight,
            int color
    ) {
        float x2 = x + uWidth;
        float y2 = y + vHeight;
        float minU = (uOffset + 0.0F) / textureWidth;
        float maxU = (uOffset + uWidth) / textureWidth;
        float minV = (vOffset + 0.0F) / textureHeight;
        float maxV = (vOffset + vHeight) / textureHeight;

        vertexConsumer.addVertex(pose, x, y, 0.0F).setUv(minU, minV).setColor(color);
        vertexConsumer.addVertex(pose, x, y2, 0.0F).setUv(minU, maxV).setColor(color);
        vertexConsumer.addVertex(pose, x2, y2, 0.0F).setUv(maxU, maxV).setColor(color);
        vertexConsumer.addVertex(pose, x2, y, 0.0F).setUv(maxU, minV).setColor(color);
    }

    private static void addQuad(VertexConsumer vc,
                                Matrix4f pose,
                                double ang0, double ang1,
                                float radius, float h,
                                SkyblockColor clr, boolean multi,
                                int r, int g, int b, int a,
                                int lu, int lv) {

        float x0 = (float) (radius * Math.cos(ang0));
        float z0 = (float) (radius * Math.sin(ang0));
        float x1 = (float) (radius * Math.cos(ang1));
        float z1 = (float) (radius * Math.sin(ang1));

        // top edge – fade alpha→0
        push(vc, pose, x0, h, z0, clr, multi, r, g, b, 0,  lu, lv);
        push(vc, pose, x1, h, z1, clr, multi, r, g, b, 0,  lu, lv);

        // bottom edge – full alpha
        push(vc, pose, x1, 0, z1, clr, multi, r, g, b, a, lu, lv);
        push(vc, pose, x0, 0, z0, clr, multi, r, g, b, a, lu, lv);
    }

    private static void push(VertexConsumer vc,
                             Matrix4f pose,
                             float x, float y, float z,
                             SkyblockColor clr, boolean multi,
                             int r, int g, int b, int a,
                             int lu, int lv) {

        if (multi) {                                 // per-vertex chroma
            int dyn = clr.getColorAtPosition(x, y, z);
            r = ARGB.red(dyn);
            g = ARGB.green(dyn);
            b = ARGB.blue(dyn);
            a = ARGB.alpha(dyn);
        }

        // POSITION_COLOR pipeline expects colour here
        vc.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setUv2(lu, lv)
                .setNormal(0, 1, 0);
    }

}