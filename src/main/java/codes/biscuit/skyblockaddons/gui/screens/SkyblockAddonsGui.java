package codes.biscuit.skyblockaddons.gui.screens;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.gui.SBAModGuiFactory;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonCredit;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonFeature;
import codes.biscuit.skyblockaddons.gui.buttons.feature.FeatureBase;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonSettings;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonFeatureToggle;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

public class SkyblockAddonsGui extends SkyblockAddonsScreen {

    private static final HashSet<Feature> FEATURE_SET = Sets.newHashSet(Feature.values());
    public static final int BUTTON_MAX_WIDTH = 140;

    private static String searchString;

    private GuiTextField featureSearchBar;
    @Getter private final EnumUtils.GuiTab tab;
    @Getter private int page;
    private int row = 1;
    private int collumn = 1;
    private int displayCount;
    boolean reInit = false;

    @Setter private boolean cancelClose;
    private GuiScreen parent = null;

    /** Boolean to draw the warning */
    private boolean showWarning = false;

    static {
        // all features except General Settings
        FEATURE_SET.removeAll(Feature.getGeneralTabFeatures());
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
        for (Feature feature : tab != EnumUtils.GuiTab.GENERAL_SETTINGS ? FEATURE_SET : Feature.getGeneralTabFeatures()) {
            // Ignore Edit GUI features
            if (Feature.getEditGuiFeatures().contains(feature)) {
                continue;
            }
            // Don't add disabled features yet
            if ((feature.isActualFeature() || tab == EnumUtils.GuiTab.GENERAL_SETTINGS) && !feature.isRemoteDisabled()) {
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
                if (feature.isRemoteDisabled() && matchesSearch(feature.getMessage())) {
                    features.add(feature); // add disabled features at the end
                }
            }
        }

        int skip = (page - 1) * displayCount;

        boolean max = page == 1;
        buttonList.add(new ButtonArrow(width / 2D - 15 - 50, height - 70, ButtonArrow.ArrowType.LEFT, max));
        max = features.size() - skip - displayCount <= 0;
        buttonList.add(new ButtonArrow(width / 2D - 15 + 50, height - 70, ButtonArrow.ArrowType.RIGHT, max));
        addSocials();

        for (Feature feature : features) {
            if (skip == 0) {
                switch (feature) {
                    case TEXT_STYLE:
                    case CHROMA_MODE:
                    case TURN_ALL_FEATURES_CHROMA:
                        addButton(feature, EnumUtils.ButtonType.CYCLING);
                        break;
                    case WARNING_TIME:
                        addButton(feature, EnumUtils.ButtonType.STEPPER);
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
        if (reInit) {
            reInit = false;
            cancelClose = true;
            mc.displayGuiScreen(this);
            cancelClose = false;
        }

        float alphaMultiplier = calculateAlphaMultiplier();
        // Alpha of the text will increase from 0 to 127 over 500ms.
        int alpha = (int) (255 * alphaMultiplier);

        GlStateManager.enableBlend();
        drawGradientBackground(alpha);

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
        GlStateManager.disableBlend();
    }

    /**
     * Code to perform the button toggles, openings of other guis/pages, and language changes.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonFeature) {
            Feature feature = ((ButtonFeature)abstractButton).getFeature();

            if (feature == Feature.LANGUAGE) {
                main.getUtils().setFadingIn(false);
                mc.displayGuiScreen(new SettingsGui(Feature.LANGUAGE,1, page,tab));

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

            }
        }

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
                Pair<Integer, Integer> coords = featureGui.getCreditsCoords(credit);
                buttonList.add(new ButtonCredit(coords.getLeft(), coords.getRight(), text, credit, feature, featureGui.isMultilineButton()));
            }

            if (!feature.getSettings().isEmpty()) {
                buttonList.add(new ButtonSettings(x + boxWidth - 33, y + boxHeight - 20, feature));
            }
            buttonList.add(new ButtonFeatureToggle(x + (boxWidth / 2F) - (31 / 2F), y+boxHeight-18, feature));

        } else if (buttonType == EnumUtils.ButtonType.CYCLING) {
            buttonList.add(new FeatureBase(x, y, text, feature));

            int bcX = x + 10;
            int bcY = (int) y + boxHeight - 23;
            int bcWidth = 120;
            int bcHeight = 15;
            switch (feature) {
                case TEXT_STYLE:
                    buttonList.add(new ButtonCycling(bcX , bcY, bcWidth, bcHeight,
                            Arrays.asList(EnumUtils.TextStyle.values()),
                            main.getConfigValues().getTextStyle().ordinal(),
                            index -> main.getConfigValues().setTextStyle(EnumUtils.TextStyle.values()[index])
                    ));
                    break;
                case CHROMA_MODE:
                    buttonList.add(new ButtonCycling(
                            bcX , bcY, bcWidth, bcHeight,
                            Arrays.asList(EnumUtils.ChromaMode.values()),
                            main.getConfigValues().getChromaMode().ordinal(),
                            index -> main.getConfigValues().setChromaMode(EnumUtils.ChromaMode.values()[index])
                    ));
                    break;
                case TURN_ALL_FEATURES_CHROMA:
                    final int currIdx = ColorUtils.areAllFeaturesChroma()
                            ? EnumUtils.AllFeaturesChroma.ENABLED.ordinal()
                            : EnumUtils.AllFeaturesChroma.DISABLED.ordinal();
                    buttonList.add(new ButtonCycling(bcX , bcY, bcWidth, bcHeight, Arrays.asList(EnumUtils.AllFeaturesChroma.values()), currIdx, index -> {
                        boolean areAllFeaturesChroma = ColorUtils.areAllFeaturesChroma();

                        for (Feature loopFeature : Feature.values()) {
                            if (loopFeature.getGuiFeatureData() != null && loopFeature.getGuiFeatureData().getDefaultColor() != null) {
                                if (!areAllFeaturesChroma) {
                                    main.getConfigValues().getChromaFeatures().add(loopFeature);
                                } else {
                                    main.getConfigValues().getChromaFeatures().remove(loopFeature);
                                }
                            }
                        }
                    }));
                    break;
            }

        } else if (buttonType == EnumUtils.ButtonType.STEPPER) {
            buttonList.add(new FeatureBase(x, y, text, feature));

            //noinspection SwitchStatementWithTooFewBranches
            switch (feature) {
                case WARNING_TIME:
                    int solidButtonX = x + (boxWidth / 2) - (ButtonStepper.SPACER + 25 + 15);
                    final int warningSeconds = main.getConfigValues().getWarningSeconds();
                    buttonList.add(new ButtonStepper(solidButtonX, y + boxHeight - 23, 50, 15,
                            warningSeconds+"s",
                            modifier -> {
                                switch (modifier) {
                                    case SUBTRACT:
                                        if (main.getConfigValues().getWarningSeconds() > 1) {
                                            main.getConfigValues().setWarningSeconds(warningSeconds - 1);
                                        }
                                        break;
                                    case ADD:
                                        if (main.getConfigValues().getWarningSeconds() < 99) {
                                            main.getConfigValues().setWarningSeconds(warningSeconds + 1);
                                        }
                                        break;
                                    default:
                                        return;
                                }
                                reInit = true;
                            }
                    ));
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
