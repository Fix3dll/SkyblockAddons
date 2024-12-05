package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonCredit;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonFeature;
import codes.biscuit.skyblockaddons.gui.buttons.feature.FeatureBase;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonSettings;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonFeatureToggle;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.objects.IntPair;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

public class SkyblockAddonsGui extends GuiScreen {

    public static final ResourceLocation LOGO = new ResourceLocation("skyblockaddons", "logo.png");
    public static final ResourceLocation LOGO_GLOW = new ResourceLocation("skyblockaddons", "logoglow.png");
    private static final String FORMATTED_VERSION = "v" + SkyblockAddons.VERSION
            .replace("+" + SkyblockAddons.BUILD_NUMBER, "")
            .replace("beta", "b") + " unofficial";

    public static final int BUTTON_MAX_WIDTH = 140;

    private static String searchString;

    private GuiTextField featureSearchBar;
    private final EnumUtils.GuiTab tab;
    private final SkyblockAddons main = SkyblockAddons.getInstance();
    private int page;
    private int row = 1;
    private int collumn = 1;
    private int displayCount;

    private final long timeOpened = System.currentTimeMillis();

    private boolean cancelClose;
    private GuiScreen parent = null;

    /**
     * Boolean to draw the warning
     */
    private boolean showWarning = false;
    private static final HashSet<Feature> featureSet = Sets.newHashSet(Feature.values());

    static {
        // all features except General Settings
        featureSet.removeAll(Feature.getGeneralTabFeatures());
    }

    /**
     * For {@link SBAModGuiFactory}
     */
    public SkyblockAddonsGui(GuiScreen parent) {
        this.parent = parent;
        this.tab = EnumUtils.GuiTab.MAIN;
        this.page = 1;
    }

    /**
     * The main gui, opened with /sba.
     */
    public SkyblockAddonsGui(int page, EnumUtils.GuiTab tab) {
        this.tab = tab;
        this.page = page;
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        if (parent != null && Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.handleKeyboardInput();
    }

    @Override
    public void initGui() {
        row = 1;
        collumn = 1;
        displayCount = findDisplayCount();
        addLanguageButton();
        addEditLocationsButton();
        addFeaturedBanner();
        addGeneralSettingsButton();

        if (featureSearchBar == null) {
            featureSearchBar = new GuiTextField(2, this.fontRendererObj, width / 2 - 220, 69, 120, 15);
            featureSearchBar.setMaxStringLength(500);
            featureSearchBar.setFocused(true);

            if (searchString != null) {
                featureSearchBar.setText(searchString);
            }
        } else {
            featureSearchBar.xPosition = width / 2 - 220;
        }

        // Add the buttons for each page.
        TreeSet<Feature> features = new TreeSet<>(Comparator.comparing(Feature::ordinal).reversed());
        for (Feature feature : tab != EnumUtils.GuiTab.GENERAL_SETTINGS ? featureSet : Feature.getGeneralTabFeatures()) {
            // Ignore Edit GUI features
            if (Feature.getEditGuiFeatures().contains(feature)) {
                continue;
            }
            // Don't add disabled features yet
            if ((feature.isActualFeature() || tab == EnumUtils.GuiTab.GENERAL_SETTINGS) && !main.getConfigValues().isRemoteDisabled(feature)) {
                if (matchesSearch(feature.getMessage())) { // Matches search.
                    features.add(feature);
                } else { // If a sub-setting matches the search show it up in the results as well.
                    for (EnumUtils.FeatureSetting setting : feature.getSettings()) {
                        try {
                            if (matchesSearch(setting.getMessage())) {
                                features.add(feature);
                            }
                        } catch (Exception ignored) {} // Hit a message that probably needs variables to fill in, just skip it.
                    }
                }
            }
        }

        if (tab != EnumUtils.GuiTab.GENERAL_SETTINGS) {
            for (Feature feature : Feature.values()) {
                if (Feature.getEditGuiFeatures().contains(feature)) {
                    continue;
                }
                if (main.getConfigValues().isRemoteDisabled(feature) && matchesSearch(feature.getMessage())) {
                    features.add(feature); // add disabled features at the end
                }
            }
        }

        int skip = (page - 1) * displayCount;

        boolean max = page == 1;
        buttonList.add(new ButtonArrow(width / 2D - 15 - 50, height - 70, ButtonArrow.ArrowType.LEFT, max));
        max = features.size() - skip - displayCount <= 0;
        buttonList.add(new ButtonArrow(width / 2D - 15 + 50, height - 70, ButtonArrow.ArrowType.RIGHT, max));

        //buttonList.add(new ButtonSocial(width / 2 + 175, 30, EnumUtils.Social.DISCORD));
        buttonList.add(new ButtonSocial(width / 2D + 125, 30, EnumUtils.Social.MODRINTH));
        buttonList.add(new ButtonSocial(width / 2D + 150, 30, EnumUtils.Social.GITHUB));
        buttonList.add(new ButtonSocial(width / 2D + 175, 30, EnumUtils.Social.BUYMEACOFFEE));

        for (Feature feature : features) {
            if (skip == 0) {
                switch (feature) {
                    case TEXT_STYLE:
                    case WARNING_TIME:
                    case CHROMA_MODE:
                    case TURN_ALL_FEATURES_CHROMA:
                        addButton(feature, EnumUtils.ButtonType.SOLID);
                        break;
                    case CHROMA_SPEED:
                    case CHROMA_SIZE:
                    case CHROMA_SATURATION:
                    case CHROMA_BRIGHTNESS:
                        addButton(feature, EnumUtils.ButtonType.CHROMA_SLIDER);
                        break;
                    default:
                        addButton(feature, EnumUtils.ButtonType.TOGGLE);
                }
            } else {
                skip--;
            }
        }
        Keyboard.enableRepeatEvents(true);
    }

    private boolean matchesSearch(String textToSearch) {
        String searchBarText = featureSearchBar.getText();
        if (searchBarText == null || searchBarText.isEmpty()) return true;

        String[] searchTerms = searchBarText.toLowerCase().split(" ");
        textToSearch = textToSearch.toLowerCase();

        for (String searchTerm : searchTerms) {
            if (!textToSearch.contains(searchTerm)) {
                return false;
            }
        }

        return true;
    }

    private int findDisplayCount() {
        int maxX = new ScaledResolution(mc).getScaledHeight()-70-50;
        int displayCount = 0;
        for (int row = 1; row < 99; row++) {
            if (getRowHeight(row) < maxX) {
                displayCount+=3;
            } else {
                return displayCount;
            }
        }
        return displayCount;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long timeSinceOpen = System.currentTimeMillis() - timeOpened;
        float alphaMultiplier = 0.5F; // This all calculates the alpha for the fade-in effect.
        if (main.getUtils().isFadingIn()) {
            int fadeMilis = 500;
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = (float) timeSinceOpen / (fadeMilis * 2);
            }
        }
        int alpha = (int)(255*alphaMultiplier); // Alpha of the text will increase from 0 to 127 over 500ms.

        int startColor = new Color(0,0, 0, (int)(alpha*0.5)).getRGB();
        int endColor = new Color(0,0, 0, alpha).getRGB();
        drawGradientRect(0, 0, width, height, startColor, endColor);
        GlStateManager.enableBlend();

        if (alpha < 4) alpha = 4; // Text under 4 alpha appear 100% transparent for some reason o.O

        drawDefaultTitleText(this, alpha*2);

        featureSearchBar.drawTextBox();
        if (StringUtils.isEmpty(featureSearchBar.getText())) {
            mc.fontRendererObj.drawString(
                    Translations.getMessage("messages.searchFeatures"), 
                    featureSearchBar.xPosition+4, 
                    featureSearchBar.yPosition+3,
                    ColorCode.DARK_GRAY.getColor()
            );
        }

        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.

        // Warning for trying to open "Edit GUI Locations" menu from outside
        if (showWarning) {
            String warning = Translations.getMessage("settings.editLocationFromOutsideWarning");
            drawHoveringText(
                    Collections.singletonList(warning),
                    width / 2 - mc.fontRendererObj.getStringWidth(warning) / 2,
                    height / 2
            );
        }
    }

    /**
     * Code to perform the button toggles, openings of other guis/pages, and language changes.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonFeature) {
            Feature feature = ((ButtonFeature)abstractButton).getFeature();

            if (abstractButton instanceof ButtonSettings) {
                main.getUtils().setFadingIn(false);
                if (((ButtonSettings) abstractButton).feature == Feature.ENCHANTMENT_LORE_PARSING) {
                    mc.displayGuiScreen(new EnchantmentSettingsGui(feature, 0, page, tab, feature.getSettings()));
                } else {
                    mc.displayGuiScreen(new SettingsGui(feature, 1, page, tab, feature.getSettings()));
                }
                return;
            }

            if (feature == Feature.LANGUAGE) {
                main.getUtils().setFadingIn(false);
                mc.displayGuiScreen(new SettingsGui(Feature.LANGUAGE,1, page,tab, null));

            } else if (feature == Feature.EDIT_LOCATIONS) {
                // If player tries to open "Edit GUI Locations" from outside
                if (mc.thePlayer == null) {
                    showWarning = true;
                    main.getScheduler().scheduleTask(scheduledTask -> showWarning = false, 60);
                } else {
                    main.getUtils().setFadingIn(false);
                    mc.displayGuiScreen(new LocationEditGui(page, tab));
                }

            } else if (feature == Feature.GENERAL_SETTINGS) {
                searchString = "";
                featureSearchBar.setText(searchString);

                if (tab == EnumUtils.GuiTab.GENERAL_SETTINGS) {
                    main.getUtils().setFadingIn(false);
                    mc.displayGuiScreen(new SkyblockAddonsGui(1, EnumUtils.GuiTab.MAIN));
                } else {
                    main.getUtils().setFadingIn(false);
                    mc.displayGuiScreen(new SkyblockAddonsGui(1, EnumUtils.GuiTab.GENERAL_SETTINGS));
                }

            } else if (abstractButton instanceof ButtonFeatureToggle) {
                if (main.getConfigValues().isRemoteDisabled(feature)) return;
                if (feature.isDisabled()) {
                    feature.setEnabled(true);
                    switch (feature) {
                        case DISCORD_RPC:
                            if (main.getUtils().isOnSkyblock()) {
                                main.getDiscordRPCManager().start();
                            }
                            break;
                        case ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT:
                            Feature.DISABLE_ENDERMAN_TELEPORTATION_EFFECT.setEnabled(true);
                            break;
                        case TURN_ALL_TEXTS_CHROMA:
                            main.getConfigValues().getChromaFeatures().add(feature);
                            break;
                    }
                } else {
                    feature.setEnabled(false);
                    switch (feature) {
                        // Reset the vanilla bars when disabling these two features.
                        case HIDE_FOOD_ARMOR_BAR:
                            // The food gets automatically enabled, no need to include it.
                            GuiIngameForge.renderArmor = true;
                            break;
                        case HIDE_HEALTH_BAR:
                            GuiIngameForge.renderHealth = true;
                            break;
                        case FULL_INVENTORY_WARNING:
                            main.getInventoryUtils().setInventoryWarningShown(false);
                            break;
                        case DISCORD_RPC:
                            main.getDiscordRPCManager().stop();
                            break;
                        case DISABLE_ENDERMAN_TELEPORTATION_EFFECT:
                            Feature.ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT.setEnabled(true);
                            break;
                        case TURN_ALL_TEXTS_CHROMA:
                            main.getConfigValues().getChromaFeatures().remove(feature);
                            break;
                    }
                }
                ((ButtonFeatureToggle)abstractButton).onClick();

            } else if (abstractButton instanceof ButtonSolid) {
                if (feature == Feature.TEXT_STYLE) {
                    main.getConfigValues().setTextStyle(main.getConfigValues().getTextStyle().getNextType());
                    cancelClose = true;
                    mc.displayGuiScreen(new SkyblockAddonsGui(page, tab));
                    cancelClose = false;
                } else if (feature == Feature.CHROMA_MODE) {
                    main.getConfigValues().setChromaMode(main.getConfigValues().getChromaMode().getNextType());
                    cancelClose = true;
                    mc.displayGuiScreen(new SkyblockAddonsGui(page, tab));
                    cancelClose = false;
                } else if (feature == Feature.TURN_ALL_FEATURES_CHROMA) {
                    boolean enable = false;

                    for (Feature loopFeature : Feature.values()) {
                        if (loopFeature.getGuiFeatureData() != null && loopFeature.getGuiFeatureData().getDefaultColor() != null) {
                            if (!main.getConfigValues().getChromaFeatures().contains(loopFeature)) {
                                enable = true;
                                break;
                            }
                        }
                    }

                    for (Feature loopFeature : Feature.values()) {
                        if (loopFeature.getGuiFeatureData() != null && loopFeature.getGuiFeatureData().getDefaultColor() != null) {
                            if (enable) {
                                main.getConfigValues().getChromaFeatures().add(loopFeature);
                            } else {
                                main.getConfigValues().getChromaFeatures().remove(loopFeature);
                            }
                        }
                    }
                }

            } else if (abstractButton instanceof ButtonModify) {
                if (feature == Feature.ADD) {
                    if (main.getConfigValues().getWarningSeconds() < 99) {
                        main.getConfigValues().setWarningSeconds(main.getConfigValues().getWarningSeconds() + 1);
                    }
                } else {
                    if (main.getConfigValues().getWarningSeconds() > 1) {
                        main.getConfigValues().setWarningSeconds(main.getConfigValues().getWarningSeconds() - 1);
                    }
                }

            } else if (abstractButton instanceof ButtonCredit) {
                if (main.getConfigValues().isRemoteDisabled(feature)) return;
                EnumUtils.FeatureCredit credit = ((ButtonCredit)abstractButton).getCredit();
                try {
                    Desktop.getDesktop().browse(new URI(credit.getUrl()));
                } catch (Exception ignored) {}
            }

        } else if (abstractButton instanceof ButtonArrow) {
            ButtonArrow arrow = (ButtonArrow)abstractButton;
            if (arrow.isNotMax()) {
                main.getUtils().setFadingIn(false);
                if (tab == EnumUtils.GuiTab.GENERAL_SETTINGS) cancelClose = true;
                if (arrow.getArrowType() == ButtonArrow.ArrowType.RIGHT) {
                    mc.displayGuiScreen(new SkyblockAddonsGui(++page, tab));
                } else {
                    mc.displayGuiScreen(new SkyblockAddonsGui(--page, tab));
                }
                if (tab == EnumUtils.GuiTab.GENERAL_SETTINGS) cancelClose = false;
            }

        } else if (abstractButton instanceof ButtonSwitchTab) {
            ButtonSwitchTab tab = (ButtonSwitchTab)abstractButton;
            if (tab.getTab() != this.tab) {
                main.getUtils().setFadingIn(false);
                mc.displayGuiScreen(new SkyblockAddonsGui(1, tab.getTab()));
            }

        } else if (abstractButton instanceof ButtonSocial) {
            EnumUtils.Social social = ((ButtonSocial)abstractButton).getSocial();
            try {
                Desktop.getDesktop().browse(social.getUrl());
            } catch (Exception ignored) {}

        } else if (abstractButton instanceof ButtonBanner) {
            try {
                Desktop.getDesktop().browse(new URI(main.getOnlineData().getBannerLink()));
            } catch (Exception ignored) {}
        }
    }

    /**
     * Draws the default text at the top at bottoms of the GUI.
     * @param gui The gui to draw the text on.
     */
    static void drawDefaultTitleText(GuiScreen gui, int alpha) {
        int defaultBlue = SkyblockAddons.getInstance().getUtils().getDefaultBlue(alpha);

        int height = 85;
        int width = height*2;
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        SkyblockAddons.getInstance().getUtils().enableStandardGLOptions();
        textureManager.bindTexture(LOGO);
        DrawUtils.drawModalRectWithCustomSizedTexture(scaledResolution.getScaledWidth()/2F-width/2F, 5, 0, 0, width, height, width, height, true);

        int animationMillis = 4000;
        float glowAlpha;
        glowAlpha = System.currentTimeMillis()%animationMillis;
        if (glowAlpha > animationMillis/2F) {
            glowAlpha = (animationMillis-glowAlpha)/(animationMillis/2F);
        } else {
            glowAlpha = glowAlpha/(animationMillis/2F);
        }

        GlStateManager.color(1,1,1, glowAlpha);
        textureManager.bindTexture(LOGO_GLOW);
        DrawUtils.drawModalRectWithCustomSizedTexture(scaledResolution.getScaledWidth()/2F-width/2F, 5, 0, 0, width, height, width, height, true);

        GlStateManager.color(1,1,1, 1);
        drawScaledString(gui, FORMATTED_VERSION, 55, defaultBlue, 1.3, 170 - Minecraft.getMinecraft().fontRendererObj.getStringWidth(FORMATTED_VERSION), false);

        SkyblockAddons.getInstance().getUtils().restoreGLOptions();
    }

    static void drawScaledString(GuiScreen guiScreen, String text, int y, int color, double scale, int xOffset) {
        drawScaledString(guiScreen, text, y, color, scale, xOffset, true);
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
    static void drawScaledString(GuiScreen guiScreen, String text, int y, int color, double scale, int xOffset, boolean centered) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        if (centered) {
            DrawUtils.drawCenteredText(text, Math.round((float) guiScreen.width / 2 / scale) + xOffset,
                    Math.round((float) y / scale), color);
        } else {
            Minecraft.getMinecraft().fontRendererObj.drawString(text, Math.round((float) guiScreen.width / 2 / scale) + xOffset,
                    Math.round((float) y / scale), color, true);
        }
        GlStateManager.popMatrix();
    }

    /**
     * Adds a button, limiting its width and setting the correct position.
     */
    private void addButton(Feature feature, EnumUtils.ButtonType buttonType) {
        if (displayCount == 0) return;
        String text = feature.getMessage();
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 50;

        int x = 0;
        if (collumn == 1) {
            x = halfWidth-90-boxWidth;
        } else if (collumn == 2) {
            x = halfWidth-(boxWidth/2);
        } else if (collumn == 3) {
            x = halfWidth+90;
        }
        double y = getRowHeight(row);

        if (buttonType == EnumUtils.ButtonType.TOGGLE) {
            FeatureBase featureGui = new FeatureBase(x, y, text, feature);
            buttonList.add(featureGui);

            EnumUtils.FeatureCredit credit = EnumUtils.FeatureCredit.fromFeature(feature);
            if (credit != null) {
                IntPair coords = featureGui.getCreditsCoords(credit);
                buttonList.add(new ButtonCredit(coords.getX(), coords.getY(), text, credit, feature, featureGui.isMultilineButton()));
            }

            if (!feature.getSettings().isEmpty()) {
                buttonList.add(new ButtonSettings(x + boxWidth - 33, y + boxHeight - 20, text, feature));
            }
            buttonList.add(new ButtonFeatureToggle(x+40, y+boxHeight-18, feature));

        } else if (buttonType == EnumUtils.ButtonType.SOLID) {
            buttonList.add(new FeatureBase(x, y, text, feature));

            switch (feature) {
                case TEXT_STYLE:
                case CHROMA_MODE:
                case TURN_ALL_FEATURES_CHROMA:
                    buttonList.add(new ButtonSolid(x+10, y + boxHeight - 23, 120, 15, "", feature));
                    break;
                case WARNING_TIME:
                    int solidButtonX = x+(boxWidth/2)-17;
                    buttonList.add(new ButtonModify(solidButtonX-20, y + boxHeight - 23, 15, 15, "+",Feature.ADD));
                    buttonList.add(new ButtonSolid(solidButtonX, y + boxHeight - 23, 35, 15, "", feature));
                    buttonList.add(new ButtonModify(solidButtonX+35+5, y + boxHeight - 23, 15, 15,"-", Feature.SUBTRACT));
                    break;
            }

        } else if (buttonType == EnumUtils.ButtonType.CHROMA_SLIDER) {
            buttonList.add(new FeatureBase(x, y, text, feature));

            if (feature == Feature.CHROMA_SPEED) {
                buttonList.add(new ButtonSlider(x + 35, y + boxHeight - 23, 70, 15, main.getConfigValues().getChromaSpeed().floatValue(),
                        0.5F, 20, 0.5F, value -> main.getConfigValues().getChromaSpeed().setValue(value)));

            } else if (feature == Feature.CHROMA_SIZE) {
                buttonList.add(new ButtonSlider(x + 35, y + boxHeight - 23, 70, 15, main.getConfigValues().getChromaSize().floatValue(),
                        1, 100, 1, value -> main.getConfigValues().getChromaSize().setValue(value)));

            } else if (feature == Feature.CHROMA_BRIGHTNESS) {
                buttonList.add(new ButtonSlider(x + 35, y + boxHeight - 23, 70, 15, main.getConfigValues().getChromaBrightness().floatValue(),
                        0, 1, 0.01F, value -> main.getConfigValues().getChromaBrightness().setValue(value)));

            } else if (feature == Feature.CHROMA_SATURATION) {
                buttonList.add(new ButtonSlider(x + 35, y + boxHeight - 23, 70, 15, main.getConfigValues().getChromaSaturation().floatValue(),
                        0, 1, 0.01F, value -> main.getConfigValues().getChromaSaturation().setValue(value)));
            }
        }

        if (feature.isNew()) {
            buttonList.add(new ButtonNewTag(x+boxWidth-15, (int)y+boxHeight-10));
        }

        collumn++;
        if (collumn > 3) {
            collumn = 1;
            row++;
        }
        displayCount--;
    }

    private void addLanguageButton() {
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 50;
        int x = halfWidth+90;
        double y = getRowHeight(displayCount/3d+1);
        buttonList.add(new FeatureBase(x, y, boxWidth, boxHeight, Translations.getMessage("languageText")+Feature.LANGUAGE.getMessage(), Feature.LANGUAGE));
    }

    private void addEditLocationsButton() {
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 50;
        int x = halfWidth-90-boxWidth;
        double y = getRowHeight(displayCount/3d+1);
        buttonList.add(new FeatureBase(x, y, boxWidth, boxHeight, Feature.EDIT_LOCATIONS.getMessage(), Feature.EDIT_LOCATIONS));
    }

    private void addGeneralSettingsButton() {
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 15;
        int x = halfWidth+90;
        double y = getRowHeight(1)-25;
        buttonList.add(new FeatureBase(x, y, boxWidth, boxHeight, Translations.getMessage("settings.tab.generalSettings"), Feature.GENERAL_SETTINGS));
    }


    private void addFeaturedBanner() {
        if (main.getOnlineData().getBannerImageURL() != null) {
            int halfWidth = width / 2;
            buttonList.add(new ButtonBanner(halfWidth - 170, 15));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (featureSearchBar.isFocused()) {
            featureSearchBar.textboxKeyTyped(typedChar, keyCode);
            searchString = featureSearchBar.getText();

            main.getUtils().setFadingIn(false);
            buttonList.clear();

            page = 1;
            initGui();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        featureSearchBar.mouseClicked(mouseX, mouseY, mouseButton);
    }

    // Each row is spaced 0.08 apart, starting at 0.17.
    private double getRowHeight(double row) {
        row--;
        return 95+(row*60); //height*(0.18+(row*0.08));
    }

    /**
     * Save the config when exiting.
     */
    @Override
    public void onGuiClosed() {
        if (!cancelClose) {
            if (tab == EnumUtils.GuiTab.GENERAL_SETTINGS) {
                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
            }
            main.getConfigValues().saveConfig();
            Keyboard.enableRepeatEvents(false);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        featureSearchBar.updateCursorCounter();
    }

    @Override
    public void onResize(Minecraft mcIn, int w, int h) {
        super.onResize(mcIn, w, h);
        main.getUtils().setFadingIn(false);
    }
}
