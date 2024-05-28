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

import java.util.LinkedHashMap;
import java.util.Map;

public class FontRendererHook {

    private static final SkyblockColor CHROMA_COLOR = new SkyblockColor(0xFFFFFFFF).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA);
    private static final DrawStateFontRenderer DRAW_CHROMA = new DrawStateFontRenderer(CHROMA_COLOR);
    private static final SkyblockColor CHROMA_COLOR_SHADOW = new SkyblockColor(0xFF555555).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA);
    private static final DrawStateFontRenderer DRAW_CHROMA_SHADOW = new DrawStateFontRenderer(CHROMA_COLOR_SHADOW);
    private static final MaxSizeHashMap<String, Boolean> stringsWithChroma = new MaxSizeHashMap<>(1000);
    private static DrawStateFontRenderer currentDrawState = null;
    private static boolean modInitialized = false;
    private static final int CHROMA_FORMAT_INDEX = 22;
    private static final int WHITE_FORMAT_INDEX = 15;
    private static boolean turnAllText = false;

    public static void beforeRenderChar() {
        if (!shouldRenderChroma()) return;

        ConfigValues config = SkyblockAddons.getInstance().getConfigValues();
        if (currentDrawState.shouldManuallyRecolorFont() || (config.isEnabled(Feature.TURN_ALL_TEXTS_CHROMA)
                && config.getChromaMode() == EnumUtils.ChromaMode.ALL_SAME_COLOR)) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
            currentDrawState.bindAnimatedColor(fontRenderer.posX, fontRenderer.posY);
        }
    }

    public static void setupFeatureFont(Feature feature) {
        if (!modInitialized) return;

        ConfigValues config = SkyblockAddons.getInstance().getConfigValues();
        if (config.getChromaMode() == EnumUtils.ChromaMode.FADE && config.getChromaFeatures().contains(feature)) {
            DRAW_CHROMA.setupMulticolorFeature(config.getGuiScale(feature));
            DRAW_CHROMA_SHADOW.setupMulticolorFeature(config.getGuiScale(feature));
        }
    }

    public static void endFeatureFont() {
        DRAW_CHROMA.endMulticolorFeature();
        DRAW_CHROMA_SHADOW.endMulticolorFeature();
    }

    /**
     * Called in patcher code to stop patcher optimization and do vanilla render
     * @param s string to render
     * @return true to override
     */
    public static boolean shouldOverridePatcher(String s) {
        if (shouldRenderChroma()) {
            //return chromaStrings.get(s) == null || chromaStrings.get(s);
            if (stringsWithChroma.get(s) != null) {
                return stringsWithChroma.get(s);
            }
            // Check if there is a "§z" colorcode in the string and cache it
            boolean hasChroma = false;
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '§') {
                    i++;
                    if (i < s.length() && (s.charAt(i) == 'z' || s.charAt(i) == 'Z')) {
                        hasChroma = true;
                        break;
                    }
                }
            }
            stringsWithChroma.put(s, hasChroma);
            return hasChroma;
        } else {
            return false;
        }
    }

    /**
     * Called to save the current shader state
     */
    public static void beginRenderString(boolean shadow) {
        if (modInitialized && SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            currentDrawState = shadow ? DRAW_CHROMA_SHADOW : DRAW_CHROMA;
            currentDrawState.loadFeatureColorEnv();

            boolean allChroma = Feature.TURN_ALL_TEXTS_CHROMA.isEnabled();
            if (allChroma || currentDrawState.isActive()) {
                float rgb = shadow ? 0.2f : 1f;
                GlStateManager.color(rgb, rgb, rgb, ColorUtils.getAlpha());
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
    public static boolean toggleChromaOn(int formatIndex) {
        if (shouldRenderChroma() && formatIndex == CHROMA_FORMAT_INDEX) {
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

            if (turnAllText && !Feature.TURN_ALL_TEXTS_CHROMA.isEnabled()) {
                endFeatureFont();
                turnAllText = false;
            }
        }
    }

    public static int forceWhiteColor(int formatIndex) {
        if (!shouldRenderChroma()) return formatIndex;

        if (formatIndex <= WHITE_FORMAT_INDEX && (currentDrawState.isActive() || Feature.TURN_ALL_TEXTS_CHROMA.isEnabled())) {
            return WHITE_FORMAT_INDEX;
        }

        return formatIndex;
    }

    /**
     * HashMap with upper limit on storage size. Used to enforce the font renderer cache not getting too large over time
     */
    public static class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
        private final int maxSize;

        public MaxSizeHashMap(int maxSize) {
            super();
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
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