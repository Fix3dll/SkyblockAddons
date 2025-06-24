package com.fix3dll.skyblockaddons.gui.screens;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonArrow;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonBanner;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonCycling;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonNewTag;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonSlider;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonStepper;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonCredit;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonFeatureToggle;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonSettings;
import com.fix3dll.skyblockaddons.gui.buttons.feature.FeatureBase;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils.AutoUpdateMode;
import com.fix3dll.skyblockaddons.utils.EnumUtils.ChromaMode;
import com.fix3dll.skyblockaddons.utils.EnumUtils.TextStyle;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class SkyblockAddonsGui extends SkyblockAddonsScreen {

    private static final HashSet<Feature> FEATURE_SET = Sets.newHashSet(Feature.values());
    public static final int BUTTON_MAX_WIDTH = 140;

    private static String searchString;

    private EditBox featureSearchBar;
    @Getter private final EnumUtils.GuiTab tab;
    @Getter private int page;
    private int row = 1;
    private int column = 1;
    private int displayCount;
    boolean reInit = false;

    @Setter private boolean cancelClose;
    private Screen parent = null;

    /**
     * Boolean to draw the warning
     */
    private boolean showWarning = false;

    static {
        // all features except General Settings
        FEATURE_SET.removeAll(Feature.getGeneralTabFeatures());
    }

    /**
     * The main gui, opened with /sba.
     */
    public SkyblockAddonsGui(int page, EnumUtils.GuiTab tab) {
        super(Component.empty());
        this.tab = tab;
        this.page = page;
    }

    @Override
    public void init() {
        row = 1;
        column = 1;
        displayCount = findDisplayCount();
        addLanguageButton();
        addEditLocationsButton();
        addFeaturedBanner();
        addGeneralSettingsButton();

        if (featureSearchBar == null) {
            featureSearchBar = new EditBox(MC.font, width / 2 - 220, 69, 120, 15, Component.empty());
            featureSearchBar.setMaxLength(500);
            featureSearchBar.setHint(
                    Component.literal(Translations.getMessage("messages.searchFeatures")).withColor(ColorCode.DARK_GRAY.getColor())
            );

            if (searchString != null) {
                featureSearchBar.setValue(searchString);
            }
        } else {
            featureSearchBar.setX(width / 2 - 220);
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
                    TreeMap<FeatureSetting, Object> settings = feature.getFeatureData().getSettings();
                    if (settings == null) continue;

                    for (FeatureSetting setting : settings.keySet()) {
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
        addRenderableWidget(new ButtonArrow(width / 2D - 15 - 50, height - 70, ButtonArrow.ArrowType.LEFT, max));
        max = features.size() - skip - displayCount <= 0;
        addRenderableWidget(new ButtonArrow(width / 2D - 15 + 50, height - 70, ButtonArrow.ArrowType.RIGHT, max));
        addSocials(this::addRenderableWidget);

        for (Feature feature : features) {
            if (skip == 0) {
                switch (feature) {
                    case GENERAL_SETTINGS:
                    case EDIT_LOCATIONS:
                    case LANGUAGE:
                        continue;
                    case TEXT_STYLE:
                    case CHROMA_MODE:
                    case AUTO_UPDATE:
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
    }

    private boolean matchesSearch(String textToSearch) {
        String searchBarText = featureSearchBar.getValue();
        if (searchBarText.isEmpty()) return true;

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
        int maxX = MC.getWindow().getGuiScaledHeight()-70-50;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (reInit) {
            reInit = false;
            cancelClose = true;
            MC.setScreen(this);
            cancelClose = false;
        }

        float alphaMultiplier = calculateAlphaMultiplier();
        // Alpha of the text will increase from 0 to 127 over 500ms.
        int alpha = (int) (255 * alphaMultiplier);

        drawGradientBackground(graphics, alpha);

        if (alpha < 4) alpha = 4; // Text under 4 alpha appear 100% transparent for some reason o.O
        drawDefaultTitleText(graphics, this, alpha * 2);

        featureSearchBar.render(graphics, mouseX, mouseY, partialTick);

        super.render(graphics, mouseX, mouseY, partialTick); // Draw buttons.

        // Warning for trying to open "Edit GUI Locations" menu from outside
        if (showWarning) {
            String warning = Translations.getMessage("settings.editLocationFromOutsideWarning");
            graphics.renderTooltip(
                    MC.font,
                    Component.literal(warning),
                    width / 2 - MC.font.width(warning) / 2,
                    height / 2
            );
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
        if (column == 1) {
            x = halfWidth-90-boxWidth;
        } else if (column == 2) {
            x = halfWidth-(boxWidth/2);
        } else if (column == 3) {
            x = halfWidth+90;
        }
        double y = getRowHeight(row);

        if (buttonType == EnumUtils.ButtonType.TOGGLE) {
            FeatureBase featureGui = new FeatureBase(x, y, text, feature);
            addRenderableWidget(featureGui);

            EnumUtils.FeatureCredit credit = EnumUtils.FeatureCredit.fromFeature(feature);
            if (credit != null) {
                Pair<Integer, Integer> coords = featureGui.getCreditsCoords(credit);
                addRenderableWidget(new ButtonCredit(coords.getLeft(), coords.getRight(), text, credit, feature, featureGui.isMultilineButton()));
            }

            if (feature.hasSettings() || feature.isColorFeature() || feature.couldBeXAllignment()) {
                addRenderableWidget(new ButtonSettings(x + boxWidth - 33, y + boxHeight - 20, text, feature));
            }
            addRenderableWidget(new ButtonFeatureToggle(x + (boxWidth / 2F) - (31 / 2F), y + boxHeight - 18, feature));

        } else if (buttonType == EnumUtils.ButtonType.CYCLING) {
            addRenderableWidget(new FeatureBase(x, y, text, feature));

            int bcX = x + 10;
            int bcY = (int) y + boxHeight - 23;
            int bcWidth = 120;
            int bcHeight = 15;
            switch (feature) {
                case TEXT_STYLE -> addRenderableWidget(new ButtonCycling(
                        bcX , bcY, bcWidth, bcHeight,
                        Arrays.asList(TextStyle.values()),
                        ((TextStyle) Feature.TEXT_STYLE.getValue()).ordinal(),
                        index -> Feature.TEXT_STYLE.setValue(TextStyle.values()[index])
                ));
                case CHROMA_MODE -> addRenderableWidget(new ButtonCycling(
                        bcX , bcY, bcWidth, bcHeight,
                        Arrays.asList(ChromaMode.values()),
                        ((ChromaMode) Feature.CHROMA_MODE.getValue()).ordinal(),
                        index -> Feature.CHROMA_MODE.setValue(ChromaMode.values()[index])
                ));
                case AUTO_UPDATE -> addRenderableWidget(new ButtonCycling(
                        bcX , bcY, bcWidth, bcHeight,
                        Arrays.asList(AutoUpdateMode.values()),
                        ((AutoUpdateMode) Feature.AUTO_UPDATE.getValue()).ordinal(),
                        index -> Feature.AUTO_UPDATE.setValue(AutoUpdateMode.values()[index])
                ));
            }

        } else if (buttonType == EnumUtils.ButtonType.STEPPER) {
            addRenderableWidget(new FeatureBase(x, y, text, feature));

            //noinspection SwitchStatementWithTooFewBranches
            switch (feature) {
                case WARNING_TIME -> {
                    int solidButtonX = x + (boxWidth / 2) - 45;
                    final int warningSeconds = Feature.WARNING_TIME.numberValue().intValue();
                    addRenderableWidget(new ButtonStepper(solidButtonX, y + boxHeight - 23, 90, 15,
                            warningSeconds + "s",
                            modifier -> {
                                int seconds = Feature.WARNING_TIME.numberValue().intValue();
                                switch (modifier) {
                                    case SUBTRACT -> {
                                        if (seconds > 1) {
                                            Feature.WARNING_TIME.setValue(seconds - 1);
                                        }
                                    }
                                    case ADD -> {
                                        if (seconds < 99) {
                                            Feature.WARNING_TIME.setValue(seconds + 1);
                                        }
                                    }
                                    default -> {
                                        return;
                                    }
                                }
                                reInit = true;
                            }
                    ));
                }
            }

        } else if (buttonType == EnumUtils.ButtonType.CHROMA_SLIDER) {
            addRenderableWidget(new FeatureBase(x, y, text, feature));
            switch (feature) {
                case CHROMA_SPEED -> addRenderableWidget(new ButtonSlider(
                        x + 35, y + boxHeight - 23, 70, 15,
                        feature.numberValue().floatValue(),
                        0.5F, 20, 0.5F, feature::setValue
                ));
                case CHROMA_SIZE -> addRenderableWidget(new ButtonSlider(
                        x + 35, y + boxHeight - 23, 70, 15,
                        feature.numberValue().floatValue(),
                        1, 100, 1, feature::setValue
                    ));
                case CHROMA_BRIGHTNESS, CHROMA_SATURATION -> addRenderableWidget(new ButtonSlider(
                        x + 35, y + boxHeight - 23, 70, 15,
                        feature.numberValue().floatValue(),
                        0, 1, 0.01F, feature::setValue
                ));
            }
        }

        if (feature.isNew()) {
            addRenderableOnly(new ButtonNewTag(x + boxWidth - 15, (int) y + boxHeight - 10));
        }

        column++;
        if (column > 3) {
            column = 1;
            row++;
        }
        displayCount--;
    }

    private void addLanguageButton() {
        int halfWidth = width / 2;
        int boxWidth = 140;
        int boxHeight = 50;
        int x = halfWidth + 90;
        double y = getRowHeight(displayCount / 3D + 1);
        addRenderableWidget(new FeatureBase(
                x,
                y,
                boxWidth,
                boxHeight,
                Translations.getMessage("languageText") + Feature.LANGUAGE.getMessage(),
                Feature.LANGUAGE,
                button -> {
                    main.getUtils().setFadingIn(false);
                    MC.setScreen(new SettingsGui(Feature.LANGUAGE,1, page, tab, EnumUtils.GUIType.MAIN));
                })
        );
    }

    private void addEditLocationsButton() {
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 50;
        int x = halfWidth-90-boxWidth;
        double y = getRowHeight(displayCount/3d+1);
        addRenderableWidget(new FeatureBase(x, y, boxWidth, boxHeight, Feature.EDIT_LOCATIONS.getMessage(), Feature.EDIT_LOCATIONS, button -> {
            // If player tries to open "Edit GUI Locations" from outside
            if (MC.player == null) {
                showWarning = true;
                main.getScheduler().scheduleTask(scheduledTask -> showWarning = false, 60);
            } else {
                main.getUtils().setFadingIn(false);
                MC.setScreen(new LocationEditGui(page, tab));
            }
        }));
    }

    private void addGeneralSettingsButton() {
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 15;
        int x = halfWidth+90;
        double y = getRowHeight(1)-25;
        addRenderableWidget(new FeatureBase(x, y, boxWidth, boxHeight, Translations.getMessage("settings.tab.generalSettings"), Feature.GENERAL_SETTINGS, button -> {
            searchString = "";
            featureSearchBar.setValue(searchString);

            if (tab == EnumUtils.GuiTab.GENERAL_SETTINGS) {
                main.getUtils().setFadingIn(false);
                MC.setScreen(new SkyblockAddonsGui(1, EnumUtils.GuiTab.MAIN));
            } else {
                main.getUtils().setFadingIn(false);
                MC.setScreen(new SkyblockAddonsGui(1, EnumUtils.GuiTab.GENERAL_SETTINGS));
            }
        }));
    }

    private void addFeaturedBanner() {
        if (main.getOnlineData().getBannerImageURL() != null) {
            int halfWidth = width / 2;
            addRenderableWidget(new ButtonBanner(halfWidth - 170, 15));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (parent != null) {
            long handle = MC.getWindow().getWindow();
            if (InputConstants.isKeyDown(handle, keyCode) && keyCode == InputConstants.KEY_ESCAPE) {
                MC.setScreen(parent);
                return true;
            }
        }
        if (featureSearchBar.isFocused()) {
            featureSearchBar.keyPressed(keyCode, scanCode, modifiers);
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                searchString = featureSearchBar.getValue();

                main.getUtils().setFadingIn(false);
                clearWidgets();

                page = 1;
                init();
            }
        } else {
            featureSearchBar.setFocused(true);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (featureSearchBar.isFocused()) {
            featureSearchBar.charTyped(codePoint, modifiers);
            searchString = featureSearchBar.getValue();

            main.getUtils().setFadingIn(false);
            clearWidgets();

            page = 1;
            init();
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        featureSearchBar.mouseClicked(mouseX, mouseY, button);
        featureSearchBar.setFocused(featureSearchBar.isHovered());

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // Each row is spaced 0.08 apart, starting at 0.17.
    private double getRowHeight(double row) {
        row--;
        return 95 + (row * 60); // height * (0.18 + (row * 0.08));
    }

    /**
     * Save the config when exiting.
     */
    @Override
    public void removed() {
        if (!cancelClose) {
            if (tab == EnumUtils.GuiTab.GENERAL_SETTINGS) {
                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
            }
            main.getConfigValuesManager().saveConfig();
        }
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        super.resize(mc, width, height);
        main.getUtils().setFadingIn(false);
    }

}