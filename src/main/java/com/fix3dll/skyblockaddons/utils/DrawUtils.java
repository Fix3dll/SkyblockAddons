package com.fix3dll.skyblockaddons.utils;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.chroma.ChromaRenderType;
import com.fix3dll.skyblockaddons.core.chroma.ManualChromaManager;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.EnumUtils.ChromaMode;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.regex.Pattern;

public class DrawUtils {

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-F]");
    private static final double HALF_PI = Math.PI / 2D;
    private static final RenderPipeline CHROMA_STANDART_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_SNIPPET)
            .withLocation(SkyblockAddons.resourceLocation("sba_chroma_standard"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withVertexShader(SkyblockAddons.resourceLocation("chroma_standard"))
            .withFragmentShader(SkyblockAddons.resourceLocation("chroma_standard"))
            .withUniform("chromaSize", UniformType.FLOAT)
            .withUniform("timeOffset", UniformType.FLOAT)
            .withUniform("saturation", UniformType.FLOAT)
            .build();
    public static final RenderType CHROMA_STANDARD = new ChromaRenderType(
            "sba_chroma_standard",
            RenderType.SMALL_BUFFER_SIZE,
            false,
            false,
            CHROMA_STANDART_PIPELINE,
            RenderType.CompositeState.builder().createCompositeState(false)
    );

//    private static final RenderType ROUNDED_RECTANGLE_LAYER = RenderType.create(
//            "rounded_rectangle",
////            DefaultVertexFormat.POSITION_COLOR,
////            VertexFormat.Mode.TRIANGLE_FAN,
//            1536,
//            false,
//            true,
//            RenderType.CompositeState.builder()
//                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
//                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
//                    .setTextureState(RenderStateShard.NO_TEXTURE)
//                    .setCullState(RenderStateShard.NO_CULL)
//                    .createCompositeState(false)
//    );


    public static void drawRoundedRect(GuiGraphics graphics, MultiBufferSource source, int x, int y, int width, int height, int radius, int color) {
        radius = Math.min(radius, Math.min(width, height) / 2);

        graphics.fill(x + radius, y, x + width - radius, y + height, color); // Main vertical rectangle
        graphics.fill(x, y + radius, x + radius, y + height - radius, color); // Left rectangle
        graphics.fill(x + width - radius, y + radius, x + width, y + height - radius, color); // Right rectangle

        drawCorner(graphics, source, x + radius, y + radius, radius, RoundedRectCorner.TOP_LEFT, color);
        drawCorner(graphics, source, x + width - radius, y + radius, radius, RoundedRectCorner.TOP_RIGHT, color);
        drawCorner(graphics, source, x + width - radius, y + height - radius, radius, RoundedRectCorner.BOTTOM_LEFT, color);
        drawCorner(graphics, source, x + radius, y + height - radius, radius, RoundedRectCorner.BOTTOM_RIGHT, color);
    }

    private static void drawCorner(GuiGraphics graphics, MultiBufferSource source, float x, float y, float radius, RoundedRectCorner corner, int color) {
        VertexConsumer vertexConsumer = source.getBuffer(RenderType.debugTriangleFan()); // 1.21.5
        Matrix4f matrix4f = graphics.pose().last().pose();

        int segments = 64;
        double angleStep = HALF_PI / (float) segments;
        double startAngle = corner.startAngle;

        float prevX = x + (float) Math.cos(startAngle) * radius;
        float prevY = y + (float) Math.sin(startAngle) * radius;

        for (int segment = 0; segment <= segments; segment++) {
            double angle = startAngle - angleStep * segment;

            float newX = x + (float) (Math.cos(angle) * radius);
            float newY = y + (float) (Math.sin(angle) * radius);

            vertexConsumer.addVertex(matrix4f, newX, newY, 0.0F).setColor(color);
        }

        // Draw final empty triangle
        vertexConsumer.addVertex(matrix4f, x, y, 0.0F).setColor(color);
        vertexConsumer.addVertex(matrix4f, prevX, prevY, 0.0F).setColor(color);
    }

    // FIXME there are spikes sometimes
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
        final int packed = LightTexture.pack(15, 15);
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

    /**
     * Fills an absolute rectangle with the specified color and z-level using the given coordinates as the boundaries.
     * @param minX the minimum x-coordinate of the rectangle.
     * @param minY the minimum y-coordinate of the rectangle.
     * @param maxX the maximum x-coordinate of the rectangle.
     * @param maxY the maximum y-coordinate of the rectangle.
     * @param color the color to fill the rectangle with.
     */
    public static void fillAbsolute(GuiGraphics graphics, MultiBufferSource source, float minX, float minY, float maxX, float maxY, int color) {
        Matrix4f matrix4f = graphics.pose().last().pose();
        if (minX < maxX) {
            float i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            float i = minY;
            minY = maxY;
            maxY = i;
        }

        VertexConsumer vertexConsumer = source.getBuffer(RenderType.gui());
        vertexConsumer.addVertex(matrix4f, minX, minY, 0).setColor(color);
        vertexConsumer.addVertex(matrix4f, minX, maxY, 0).setColor(color);
        vertexConsumer.addVertex(matrix4f, maxX, maxY, 0).setColor(color);
        vertexConsumer.addVertex(matrix4f, maxX, minY, 0).setColor(color);
    }

    public static void drawCenteredText(GuiGraphics graphics, MultiBufferSource source, String text, float x, float y, int color) {
        drawText(graphics, text, x - Minecraft.getInstance().font.width(text) / 2F, y, color);
    }

    /**
     * Draws an absolute text with the specified parameters. Use black color while drawing legacy formatting texts.
     * @param text the text.
     * @param x the x-coordinate of the text.
     * @param y the y-coordinate of the text.
     * @param color the color to fill the text with.
     */
    public static void drawText(GuiGraphics graphics, @NonNull String text, float x, float y, int color) {
        if (text.isEmpty()) return;

        Component component;
        if (Feature.CHROMA_MODE.getValue() == ChromaMode.FADE
                && color == ManualChromaManager.getChromaColor(0, 0, ARGB.alpha(color))) {
            component = Component.literal(text).withStyle(style ->
               style.withColor(new TextColor(ColorCode.CHROMA.getColor(), "chroma"))
            );
        } else {
            component = Component.literal(text);
        }

        Font font = Minecraft.getInstance().font;
        if (Feature.TEXT_STYLE.getValue() == EnumUtils.TextStyle.STYLE_TWO) {
            // FIXME alpha on legacy format
            int colorAlpha = Math.max(ARGB.alpha(color), 4);
            int colorBlack = ARGB.color(colorAlpha, 0, 0, 0);
            String blackedText = "§r" + COLOR_CODE_PATTERN.matcher(text).replaceAll("§r");
            graphics.drawSpecial(source -> font.drawInBatch(blackedText, x + 1, y, colorBlack, false, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880));
            graphics.drawSpecial(source -> font.drawInBatch(blackedText, x - 1, y, colorBlack, false, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880));
            graphics.drawSpecial(source -> font.drawInBatch(blackedText, x, y + 1, colorBlack, false, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880));
            graphics.drawSpecial(source -> font.drawInBatch(blackedText, x, y - 1, colorBlack, false, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880));
            graphics.drawSpecial(source -> font.drawInBatch(component, x, y, color, false, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880));
        } else {
            graphics.drawSpecial(source -> font.drawInBatch(component, x, y , color, true, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880));
        }
    }
    public static void blitAbsolute(
            PoseStack poseStack,
            MultiBufferSource source,
            ResourceLocation atlasLocation,
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
        blitAbsolute(poseStack, source, null, atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight, textureWidth, textureHeight, color);
    }

    public static void blitAbsolute(
            PoseStack poseStack,
            MultiBufferSource source,
            RenderType renderType,
            ResourceLocation atlasLocation,
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

        RenderType options = renderType == null ? RenderType.guiTextured(atlasLocation) : renderType;
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = source.getBuffer(options);
        vertexConsumer.addVertex(matrix4f, x, y, 0.0F).setUv(minU, minV).setColor(color);
        vertexConsumer.addVertex(matrix4f, x, y2, 0.0F).setUv(minU, maxV).setColor(color);
        vertexConsumer.addVertex(matrix4f, x2, y2, 0.0F).setUv(maxU, maxV).setColor(color);
        vertexConsumer.addVertex(matrix4f, x2, y, 0.0F).setUv(maxU, minV).setColor(color);
    }

    private enum RoundedRectCorner {
        TOP_LEFT(-HALF_PI),
        TOP_RIGHT(0),
        BOTTOM_LEFT(HALF_PI),
        BOTTOM_RIGHT(Math.PI);

        private final double startAngle;

        RoundedRectCorner(double startAngle) {
            this.startAngle = startAngle;
        }
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