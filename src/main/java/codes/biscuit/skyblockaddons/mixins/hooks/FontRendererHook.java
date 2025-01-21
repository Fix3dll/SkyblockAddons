package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils.ChromaMode;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;
import codes.biscuit.skyblockaddons.utils.draw.DrawStateFontRenderer;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public class FontRendererHook {

    private static final SkyblockColor CHROMA_COLOR = new SkyblockColor(0xFFFFFFFF).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA);
    private static final DrawStateFontRenderer DRAW_CHROMA = new DrawStateFontRenderer(CHROMA_COLOR);
    private static final SkyblockColor CHROMA_COLOR_SHADOW = new SkyblockColor(0xFF555555).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA);
    private static final DrawStateFontRenderer DRAW_CHROMA_SHADOW = new DrawStateFontRenderer(CHROMA_COLOR_SHADOW);
    private static final int CHROMA_FORMAT_INDEX = 22;
    private static final int WHITE_FORMAT_INDEX = 15;

    private static FontRenderer fontRenderer;
    private static DrawStateFontRenderer currentDrawState = null;
    private static boolean modInitialized = false;
    private static Feature fadeFontFeature = null;
    @Setter private static boolean turnAllTextChroma = false;
    @Setter private static boolean haltManualColor = false;

    public static void beforeRenderChar() {
        if (!shouldRenderChroma() || haltManualColor) return;

        if (currentDrawState.shouldManuallyRecolorFont() || (turnAllTextChroma && fadeFontFeature == null)) {
            currentDrawState.bindAnimatedColor(fontRenderer.posX, fontRenderer.posY);
        }
    }

    public static void setupFeatureFont(Feature feature) {
        if (!modInitialized || (!feature.isChroma() && !turnAllTextChroma)) return;

        if (Feature.CHROMA_MODE.getValue() == ChromaMode.FADE) {
            fadeFontFeature = feature;
        }
    }

    public static void endFeatureFont() {
        fadeFontFeature = null;
    }

    /**
     * Called to save the current shader state
     */
    public static void beginRenderString(boolean shadow) {
        if (!modInitialized || !SkyblockAddons.getInstance().getUtils().isOnSkyblock()) return;

        if (turnAllTextChroma && fadeFontFeature == null && Feature.CHROMA_MODE.getValue() == ChromaMode.FADE) {
            fadeFontFeature = Feature.TURN_ALL_TEXTS_CHROMA;
        }

        if (fadeFontFeature != null) {
            DRAW_CHROMA.setupMulticolorFeature(fadeFontFeature.getGuiScale());
            DRAW_CHROMA_SHADOW.setupMulticolorFeature(fadeFontFeature.getGuiScale());

            // There is no need to force the white color if there is no fading
            float rgb = shadow ? 0.2f : 1f;
            GlStateManager.color(rgb, rgb, rgb, ColorUtils.getAlpha());

            currentDrawState = shadow ? DRAW_CHROMA_SHADOW : DRAW_CHROMA;
            currentDrawState.loadFeatureColorEnv();
        } else {
            DRAW_CHROMA.endMulticolorFeature();
            DRAW_CHROMA_SHADOW.endMulticolorFeature();
        }
    }

    /**
     * Called to restore the saved chroma state
     */
    public static void restoreChromaState() {
        if (shouldRenderChroma()) {
            currentDrawState.restoreColorEnv();
        }
    }

    /**
     * Called to turn chroma on
     */
    public static boolean toggleChromaOn(int formatIndex, boolean shadow) {
        if (!modInitialized || !SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            return false;
        }

        if (formatIndex == CHROMA_FORMAT_INDEX) {
            if (currentDrawState == null) {
                currentDrawState = shadow ? DRAW_CHROMA_SHADOW : DRAW_CHROMA;
                currentDrawState.loadFeatureColorEnv();
            }
            currentDrawState.newColorEnv().bindActualColor();
            return true;
        }

        return false;
    }

    /**
     * Called to turn chroma off after the full string has been rendered (before returning)
     */
    public static void endRenderString() {
        if (shouldRenderChroma()) {
            currentDrawState.endColorEnv();
            currentDrawState = null;
        }
    }

    public static int forceWhiteColor(int formatIndex) {
        if (shouldRenderChroma() && formatIndex <= WHITE_FORMAT_INDEX && (currentDrawState.isUsingShader() || turnAllTextChroma)) {
            return WHITE_FORMAT_INDEX;
        }

        return formatIndex;
    }

    /**
     * Called by {@link SkyblockAddons#postInit(FMLPostInitializationEvent)}
     * Fixes NPE caused by Splash Screen, calling FontRendererHook before SBA is loaded.
     */
    public static void onModInitialized() {
        fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        modInitialized = true;
        if (Feature.TURN_ALL_TEXTS_CHROMA.isEnabled()) {
            turnAllTextChroma = true;
        }
    }

    /**
     * Returns whether the methods for rendering chroma text should be run. They should be run only while the mod is
     * fully initialized and the player is playing Skyblock.
     *
     * @return {@code true} when the mod is fully initialized and the player is in Skyblock, {@code false} otherwise
     */
    public static boolean shouldRenderChroma() {
        if (!modInitialized) return false;

        return SkyblockAddons.getInstance().getUtils().isOnSkyblock() && currentDrawState != null;
    }
}