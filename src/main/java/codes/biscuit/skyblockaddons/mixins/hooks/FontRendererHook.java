package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;
import codes.biscuit.skyblockaddons.utils.draw.DrawStateFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public class FontRendererHook {

    private static final SkyblockColor CHROMA_COLOR = new SkyblockColor(0xFFFFFFFF).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA);
    private static final DrawStateFontRenderer DRAW_CHROMA = new DrawStateFontRenderer(CHROMA_COLOR);
    private static final SkyblockColor CHROMA_COLOR_SHADOW = new SkyblockColor(0xFF555555).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA);
    private static final DrawStateFontRenderer DRAW_CHROMA_SHADOW = new DrawStateFontRenderer(CHROMA_COLOR_SHADOW);
    private static DrawStateFontRenderer currentDrawState = null;
    private static boolean modInitialized = false;
    private static final int CHROMA_FORMAT_INDEX = 22;
    private static final int WHITE_FORMAT_INDEX = 15;
    private static boolean turnAllText = false;
    private static Feature fontFeature = null;

    public static void beforeRenderChar() {
        if (!shouldRenderChroma()) return;

        if (currentDrawState.shouldManuallyRecolorFont() || (Feature.TURN_ALL_TEXTS_CHROMA.isEnabled()
                && SkyblockAddons.getInstance().getConfigValues().getChromaMode() == EnumUtils.ChromaMode.ALL_SAME_COLOR)) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
            currentDrawState.bindAnimatedColor(fontRenderer.posX, fontRenderer.posY);
        }
    }

    public static void setupFeatureFont(Feature feature) {
        if (!modInitialized) return;

        ConfigValues config = SkyblockAddons.getInstance().getConfigValues();
        if (config.getChromaMode() == EnumUtils.ChromaMode.FADE && config.getChromaFeatures().contains(feature)) {
            fontFeature = feature;
        }
    }

    public static void endFeatureFont() {
        fontFeature = null;
    }

    /**
     * Called to save the current shader state
     */
    public static void beginRenderString(boolean shadow) {
        if (modInitialized && SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            boolean allChroma = Feature.TURN_ALL_TEXTS_CHROMA.isEnabled();
            ConfigValues config = SkyblockAddons.getInstance().getConfigValues();

            if (allChroma || fontFeature != null) {
                if (fontFeature != null) {
                    DRAW_CHROMA.setupMulticolorFeature(config.getGuiScale(fontFeature));
                    DRAW_CHROMA_SHADOW.setupMulticolorFeature(config.getGuiScale(fontFeature));
                }

                currentDrawState = shadow ? DRAW_CHROMA_SHADOW : DRAW_CHROMA;
                currentDrawState.loadFeatureColorEnv();
            } else {
                DRAW_CHROMA.endMulticolorFeature();
                DRAW_CHROMA_SHADOW.endMulticolorFeature();
            }

            if (allChroma || (currentDrawState != null && currentDrawState.isUsingShader())) {
                // There is no need to force the white color if there is no fading
                if (config.getChromaMode() == EnumUtils.ChromaMode.FADE) {
                    float rgb = shadow ? 0.2f : 1f;
                    GlStateManager.color(rgb, rgb, rgb, ColorUtils.getAlpha());
                }
                if (allChroma) {
                    setupFeatureFont(Feature.TURN_ALL_TEXTS_CHROMA);
                    turnAllText = true;
                }
            }
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

            if (turnAllText && !Feature.TURN_ALL_TEXTS_CHROMA.isEnabled()) {
                endFeatureFont();
                turnAllText = false;
            }
        }
    }

    public static int forceWhiteColor(int formatIndex) {
        if (shouldRenderChroma() && formatIndex <= WHITE_FORMAT_INDEX
                && (currentDrawState.isUsingShader() || Feature.TURN_ALL_TEXTS_CHROMA.isEnabled())) {
            return WHITE_FORMAT_INDEX;
        }

        return formatIndex;
    }

    /**
     * Called by {@link SkyblockAddons#postInit(FMLPostInitializationEvent)}
     * Fixes NPE caused by Splash Screen, calling FontRendererHook before SBA is loaded.
     */
    public static void onModInitialized() {
        modInitialized = true;
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