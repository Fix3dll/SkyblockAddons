package com.fix3dll.skyblockaddons.gui.screens;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Language;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureGuiData;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.discordrpc.DiscordStatus;
import com.fix3dll.skyblockaddons.features.dungeonmap.DungeonMapManager;
import com.fix3dll.skyblockaddons.features.enchants.EnchantLayout;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonArrow;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonCycling;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonInputFieldWrapper;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonLanguage;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonSlider;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonText;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonOpenColorMenu;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonSettingToggle;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonSolid;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

public class SettingsGui extends SkyblockAddonsScreen {

    /** Half the total pixel width of the settings box. Box spans [screenCenterX ± BOX_HALF_EXTENT]. */
    static final int BOX_HALF_EXTENT = 230;
    static final int BOX_TOTAL_WIDTH = BOX_HALF_EXTENT * 2;
    /** Top Y of the settings box, matching {@link #getRowHeight(double)} at row 1. */
    static final int BOX_Y = 95;

    /** Maximum fraction of the logical screen height the settings box may occupy. */
    static final float MAX_BOX_HEIGHT_RATIO = 0.65F;
    static final int SCROLL_SPEED = 16;
    static final int SCROLLBAR_WIDTH = 4;
    static final int SCROLLBAR_PADDING = 4;
    static final int MIN_THUMB_HEIGHT = 20;
    static final int SCROLLBAR_TRACK_COLOR = ARGB.color(160, 45, 47, 65);
    static final int SCROLLBAR_THUMB_COLOR = ARGB.color(220, 100, 105, 150);
    static final int BOX_BACKGROUND_COLOR = ARGB.color(230, 28, 29, 41);

    /**
     * Buttons that are excluded from scroll offset adjustments and drawn on top of
     * the scissored content region (e.g. navigation arrows, social links).
     *
     * <p>Uses {@link LinkedHashSet} instead of a list to preserve insertion
     * order for rendering while providing O(1) {@code contains()} for per-frame checks.</p>
     */
    final LinkedHashSet<Renderable> scrollIgnoredButtons = new LinkedHashSet<>();

    @Getter final Feature feature;
    @Getter final int lastPage;
    @Getter final EnumUtils.GuiTab lastTab;
    @Getter final EnumUtils.GUIType lastGUI;
    @Getter final int page;
    float row = 1;
    int column = 1;
    int displayCount;
    @Setter boolean closingGui;
    @Setter boolean reInit = false;

    double scrollValue;
    double previousScrollValue;
    /** Maximum downward scroll distance in pixels (0 when content fits without scrolling). */
    int maxScrollValue;
    double scrollY;

    /** Rendered height of the settings box, capped at {@link #MAX_BOX_HEIGHT_RATIO} of screen height. */
    int boxHeight;
    /** Whether content exceeds {@link #boxHeight} and a scrollbar should be shown. */
    boolean isScrollable;

    private boolean isDraggingScrollbar;
    private double dragStartMouseY;
    private double dragStartScrollValue;
    /** Cached thumb geometry for drag hit testing, updated each frame by {@link #drawScrollbar}. */
    private int cachedThumbY;
    private int cachedThumbHeight;
    private int cachedTrackX;
    private int cachedTrackHeight;

    private int lastEnchantX;
    private double lastEnchantY;

    public SettingsGui(Feature feature, int page, int lastPage, EnumUtils.GuiTab lastTab, EnumUtils.GUIType lastGUI) {
        super(Component.empty());
        this.feature = feature;
        this.page = page;
        this.lastPage = lastPage;
        this.lastTab = lastTab;
        this.lastGUI = lastGUI;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void init() {
        super.init();

        scrollValue = 0;
        previousScrollValue = 0;
        isDraggingScrollbar = false;
        row = 1;
        column = 1;
        clearWidgets();
        if (feature == Feature.LANGUAGE) {
            Language currentLanguage = (Language) feature.getFeatureData().getValue();

            displayCount = findDisplayCount();
            // Add the buttons for each page.
            int skip = (page - 1) * displayCount;

            addScrollIgnoredButton(new ButtonArrow(width / 2 - 15 - 50, height - 70, ButtonArrow.ArrowType.LEFT, page == 1));
            addScrollIgnoredButton(new ButtonArrow(width / 2 - 15 + 50, height - 70, ButtonArrow.ArrowType.RIGHT, Language.values().length - skip - displayCount <= 0));

            for (Language language : Language.values()) {
                if (skip == 0) {
                    if (language == Language.ENGLISH) continue;
                    if (language == Language.CHINESE_TRADITIONAL) {
                        addLanguageButton(Language.ENGLISH);
                    }
                    addLanguageButton(language);
                } else {
                    skip--;
                }
            }

            feature.setValue(currentLanguage);
            DataUtils.loadLocalizedStrings(false);

            boxHeight = 0;
            isScrollable = false;
            maxScrollValue = 0;
        } else {
            if (feature.hasSettings()) {
                for (Map.Entry<FeatureSetting, Object> entry : feature.getFeatureData().getSettings().entrySet()) {
                    addButton(entry.getKey(), entry.getValue());
                }
            }
            addUniversalButton();
            computeScrollGeometry();
            addImportExportButton();
        }
        addSocials(this::addScrollIgnoredButton);
    }

    private int findDisplayCount() {
        int maxX = MC.getWindow().getGuiScaledHeight() - 70 - 25;
        int displayCount = 0;
        for (int row = 1; row < 99; row++) {
            if (getRowHeight(row) < maxX) {
                displayCount += 3;
            } else {
                return displayCount;
            }
        }
        return displayCount;
    }

    /**
     * Recomputes {@link #boxHeight}, {@link #isScrollable}, and {@link #maxScrollValue}
     * based on the current content height and screen size. Must be called after all
     * setting buttons have been added so that {@link #row} reflects the final layout.
     */
    protected void computeScrollGeometry() {
        int fullContentHeight = (int) getRowHeightSetting(row) - BOX_Y;
        int maxBoxHeight = (int) (this.height * MAX_BOX_HEIGHT_RATIO);
        boxHeight = Math.min(fullContentHeight, maxBoxHeight);
        isScrollable = fullContentHeight > boxHeight;
        maxScrollValue = isScrollable ? fullContentHeight - boxHeight : 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (reInit) {
            reInit = false;
            init();
        }

        double scroll = this.getMouseScrollY() * SCROLL_SPEED;
        double newScrollValue = scrollValue + scroll;
        if (newScrollValue >= -maxScrollValue && newScrollValue <= 0) {
            scrollValue = newScrollValue;
        }

        float alphaMultiplier = calculateAlphaMultiplier();
        // Alpha of the text will increase from 0 to 127 over 500ms.
        int alpha = (int) (255 * alphaMultiplier);
        drawGradientBackground(graphics, alpha);

        if (alpha < 4) alpha = 4; // Text under 4 alpha appear 100% transparent for some reason o.O
        int defaultBlue = ColorUtils.getDefaultBlue(alpha * 2);
        drawDefaultTitleText(graphics, mouseX, mouseY, partialTick, this, alpha * 2);

        boolean scissorEnabled = false;
        if (feature != Feature.LANGUAGE) {
            int boxX = width / 2 - BOX_HALF_EXTENT;

            DrawUtils.drawRoundedRect(graphics, boxX, BOX_Y, BOX_TOTAL_WIDTH, boxHeight, 4, BOX_BACKGROUND_COLOR);

            // Scroll ability with scissor
            graphics.enableScissor(boxX, BOX_Y, boxX + BOX_TOTAL_WIDTH, BOX_Y + boxHeight);
            scissorEnabled = true;

            drawScaledString(graphics, this, Translations.getMessage("settings.settings"), (int) (110 + scrollValue), defaultBlue, 1.5F, 0);

            final int scrollDelta = (int) scrollValue - (int) previousScrollValue;
            previousScrollValue = scrollValue;
            renderables.forEach(renderable -> {
                AbstractWidget widget = (AbstractWidget) renderable;
                if (!scrollIgnoredButtons.contains(renderable)) {
                    widget.setY(widget.getY() + scrollDelta);
                }
            });
        }

        this.drawSettingsScreen(graphics, mouseX, mouseY, partialTick); // Draw buttons.

        if (scissorEnabled) {
            graphics.disableScissor();
            if (isScrollable) {
                drawScrollbar(graphics);
            }
        }

        scrollIgnoredButtons.forEach(renderable -> renderable.render(graphics, mouseX, mouseY, partialTick));
    }

    /**
     * Draws the scrollbar track and thumb outside the scissor region so they are never clipped.
     * Also caches the computed thumb bounds into {@link #cachedTrackX}, {@link #cachedTrackHeight},
     * {@link #cachedThumbY}, and {@link #cachedThumbHeight} for use in mouse hit-testing.
     */
    private void drawScrollbar(GuiGraphics graphics) {
        int boxX = width / 2 - BOX_HALF_EXTENT;
        int trackX = boxX + BOX_TOTAL_WIDTH - SCROLLBAR_WIDTH - SCROLLBAR_PADDING;
        int trackY = BOX_Y + SCROLLBAR_PADDING;
        int trackHeight = boxHeight - SCROLLBAR_PADDING * 2;

        DrawUtils.drawRoundedRect(graphics, trackX, trackY, SCROLLBAR_WIDTH, trackHeight, SCROLLBAR_WIDTH / 2, SCROLLBAR_TRACK_COLOR);

        int fullContentHeight = boxHeight + maxScrollValue;
        int thumbHeight = Math.max(MIN_THUMB_HEIGHT, (int) ((float) boxHeight / fullContentHeight * trackHeight));
        float scrollRatio = (float) (-scrollValue) / maxScrollValue;
        int thumbY = trackY + Math.round(scrollRatio * (trackHeight - thumbHeight));

        cachedTrackX = trackX;
        cachedTrackHeight = trackHeight;
        cachedThumbY = thumbY;
        cachedThumbHeight = thumbHeight;

        DrawUtils.drawRoundedRect(graphics, trackX, thumbY, SCROLLBAR_WIDTH, thumbHeight, SCROLLBAR_WIDTH / 2, SCROLLBAR_THUMB_COLOR);
    }

    @Override
    protected void clearWidgets() {
        this.scrollIgnoredButtons.clear();
        super.clearWidgets();
    }

    private void addLanguageButton(Language language) {
        if (displayCount == 0) return;
        String text = feature.getMessage();
        int halfWidth = width / 2;
        int boxWidth = 140;
        int x = 0;
        if (column == 1) {
            x = halfWidth - 90 - boxWidth;
        } else if (column == 2) {
            x = halfWidth - (boxWidth / 2);
        } else if (column == 3) {
            x = halfWidth + 90;
        }
        double y = getRowHeight(row);
        addRenderableWidget(new ButtonLanguage(x, y, text, language));
        column++;
        if (column > 3) {
            column = 1;
            row++;
        }
        displayCount--;
    }

    private void addButton(FeatureSetting setting, Object settingValue) {
        final int halfWidth = width / 2;
        int boxWidth;
        int x;
        double y = getRowHeightSetting(row);
        switch (setting) {
            // These are for holding values.
            case DISCORD_RP_AUTO_MODE: case DISCORD_RP_CUSTOM_DETAILS: case DISCORD_RP_CUSTOM_STATE:
                return;

            case DISCORD_RP_DETAILS: case DISCORD_RP_STATE:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                DiscordStatus currentStatus = (DiscordStatus) settingValue;

                addRenderableWidget(new ButtonText(halfWidth, (int) y - 10, setting.getMessage(), true, 0xFFFFFFFF));
                addRenderableWidget(new ButtonCycling(x, (int) y, boxWidth, 20, Arrays.asList(DiscordStatus.values()), currentStatus.ordinal(), index -> {
                    final DiscordStatus selectedStatus = DiscordStatus.values()[index];
                    if (setting == FeatureSetting.DISCORD_RP_STATE) {
                        main.getDiscordRPCManager().setStateLine(selectedStatus);
                    } else {
                        main.getDiscordRPCManager().setDetailsLine(selectedStatus);
                    }
                    feature.set(setting, selectedStatus);
                    reInit = true;
                }));

                if (currentStatus == DiscordStatus.AUTO_STATUS) {
                    row++;
                    row += 0.4F;
                    x = halfWidth - (boxWidth / 2);
                    y = getRowHeightSetting(row);

                    addRenderableWidget(new ButtonText(halfWidth, (int) y - 10, Translations.getMessage("messages.fallbackStatus"), true, 0xFFFFFFFF));
                    currentStatus = (DiscordStatus) feature.get(FeatureSetting.DISCORD_RP_AUTO_MODE);
                    addRenderableWidget(new ButtonCycling(x, (int) y, boxWidth, 20, Arrays.asList(DiscordStatus.values()), currentStatus.ordinal(), index -> {
                        final DiscordStatus selectedStatus = DiscordStatus.values()[index];
                        feature.set(FeatureSetting.DISCORD_RP_AUTO_MODE, selectedStatus);
                        reInit = true;
                    }));
                }

                if (currentStatus == DiscordStatus.CUSTOM) {
                    row++;
                    boxWidth = 200;
                    x = halfWidth - (boxWidth / 2);
                    y = getRowHeightSetting(row);

                    FeatureSetting customLine = setting == FeatureSetting.DISCORD_RP_DETAILS
                            ? FeatureSetting.DISCORD_RP_CUSTOM_DETAILS
                            : FeatureSetting.DISCORD_RP_CUSTOM_STATE;

                    addRenderableWidget(new ButtonInputFieldWrapper(
                            x, (int) y, 200, 20,
                            Feature.DISCORD_RPC.getAsString(customLine),
                            null, 100, false,
                            updatedValue -> Feature.DISCORD_RPC.set(customLine, updatedValue)
                    ));
                }
                row += 0.4F;
                break;

            case EXPAND_DEPLOYABLE_STATUS:
                if (feature.get(FeatureSetting.DEPLOYABLE_DISPLAY_STYLE) != EnumUtils.DeployableDisplayStyle.DETAILED) {
                    return;
                }

                boxWidth = ButtonSettingToggle.WIDTH;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonSettingToggle(x, y, Translations.getMessage("settings.expandDeployableStatus"), setting));
                break;

            case DEPLOYABLE_DISPLAY_STYLE:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                addRenderableWidget(new ButtonText(halfWidth, (int) y - 10, setting.getMessage(), true, 0xFFFFFFFF));
                addRenderableWidget(new ButtonCycling(x, (int) y, 140, 20,
                        Arrays.asList(EnumUtils.DeployableDisplayStyle.values()),
                        feature.getAsEnum(setting).ordinal(),
                        index -> {
                            EnumUtils.DeployableDisplayStyle style = EnumUtils.DeployableDisplayStyle.values()[index];
                            feature.set(setting, style);
                            reInit = true;
                        }
                ));
                row += .1F;
                break;

            case BACKPACK_STYLE:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                addRenderableWidget(new ButtonText(halfWidth, (int) y - 10, Translations.getMessage("settings.backpackStyle"), true, 0xFFFFFFFF));
                addRenderableWidget(new ButtonCycling(x, (int) y, 140, 20,
                        Arrays.asList(EnumUtils.BackpackStyle.values()),
                        feature.getAsEnum(setting).ordinal(),
                        index -> feature.set(setting, EnumUtils.BackpackStyle.values()[index])
                ));
                row += .1F;
                break;

            case DUNGEON_MAP_ZOOM:
                // For clarity
                boxWidth = 100; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(
                        new ButtonSlider(x, y, 100, 20,
                                DungeonMapManager.getMapZoom(),
                                DungeonMapManager.MIN_ZOOM,
                                DungeonMapManager.MAX_ZOOM,
                                0.05F,
                                DungeonMapManager::setMapZoom
                        ).setPrefix("Map Zoom: ")
                );
                row += .1F;
                break;

            case HEALING_CIRCLE_OPACITY:
                boxWidth = 150;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(
                        new ButtonSlider(
                                x, y, boxWidth, 20,
                                feature.getAsNumber(setting).floatValue(), 0, 1, 0.01F,
                                updatedValue -> feature.set(setting, updatedValue)
                        ).setPrefix("Healing Circle Opacity: ")
                );
                row += .1F;
                break;

            case PET_ITEM_STYLE:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                addRenderableWidget(new ButtonText(halfWidth, (int) y - 10, setting.getMessage(), true, 0xFFFFFFFF));
                addRenderableWidget(new ButtonCycling(x, (int) y, 140, 20,
                        Arrays.asList(EnumUtils.PetItemStyle.values()),
                        feature.getAsEnum(setting).ordinal(),
                        index -> feature.set(setting, EnumUtils.PetItemStyle.values()[index])
                ));
                row += .1F;
                break;

            case TREVOR_SHOW_QUEST_COOLDOWN:
                boxWidth = ButtonSettingToggle.WIDTH;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                row += .1F;
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("settings.trevorTheTrapper.showQuestCooldownDescription"), true, ColorCode.GRAY.getColor()));
                row += .4F;
                break;

            case TREVOR_HIGHLIGHT_TRACKED_ENTITY:
                boxWidth = ButtonSettingToggle.WIDTH;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                row += .1F;
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("messages.entityOutlinesRequirement"), true, ColorCode.GRAY.getColor()));
                row += .4F;
                break;

            case CLASS_COLORED_TEAMMATE:
                boxWidth = ButtonSettingToggle.WIDTH;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                row += .1F;
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("messages.classColoredTeammateRequirement"), true, ColorCode.GRAY.getColor()));
                if (feature.isDisabled(setting)) row += .4F;
                break;

            case HEALER_COLOR:
            case MAGE_COLOR:
            case BERSERK_COLOR:
            case ARCHER_COLOR:
            case TANK_COLOR:
                if (feature.isDisabled(FeatureSetting.CLASS_COLORED_TEAMMATE)) return;
                boxWidth = 100;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonOpenColorMenu(x, y, 100, 20, setting.getMessage(), setting));
                if (setting == FeatureSetting.TANK_COLOR) row += 0.4F; // Last spacing
                break;

            case ULTIMATE_ENCHANT_COLOR:
            case PERFECT_ENCHANT_COLOR:
            case GREAT_ENCHANT_COLOR:
            case GOOD_ENCHANT_COLOR:
            case POOR_ENCHANT_COLOR:
            case COMMA_ENCHANT_COLOR:
                boxWidth = 100;
                x = lastEnchantX = halfWidth - (boxWidth / 2) - (ButtonSettingToggle.WIDTH * 4 / 2) - 45;
                y = lastEnchantY = getRowHeightSetting(row);
                addRenderableWidget(new ButtonOpenColorMenu(x, y, 100, 20, setting.getMessage(), setting));
                row--;
                break;

            case ULTIMATE_ENCHANT_BOLD:
            case PERFECT_ENCHANT_BOLD:
            case GREAT_ENCHANT_BOLD:
            case GOOD_ENCHANT_BOLD:
            case POOR_ENCHANT_BOLD:
            case COMMA_ENCHANT_BOLD:
                lastEnchantX += 100 + 10;
                addRenderableWidget(new ButtonSettingToggle(lastEnchantX, lastEnchantY + 2.5, setting.getMessage(), setting));
                row--;
                break;
            case ULTIMATE_ENCHANT_ITALIC:
            case PERFECT_ENCHANT_ITALIC:
            case GREAT_ENCHANT_ITALIC:
            case GOOD_ENCHANT_ITALIC:
            case POOR_ENCHANT_ITALIC:
            case COMMA_ENCHANT_ITALIC:
            case ULTIMATE_ENCHANT_UNDERLINED:
            case PERFECT_ENCHANT_UNDERLINED:
            case GREAT_ENCHANT_UNDERLINED:
            case GOOD_ENCHANT_UNDERLINED:
            case POOR_ENCHANT_UNDERLINED:
            case COMMA_ENCHANT_UNDERLINED:
                lastEnchantX -= 20;
            case ULTIMATE_ENCHANT_STRIKETHROUGH:
            case PERFECT_ENCHANT_STRIKETHROUGH:
            case GREAT_ENCHANT_STRIKETHROUGH:
            case GOOD_ENCHANT_STRIKETHROUGH:
            case POOR_ENCHANT_STRIKETHROUGH:
            case COMMA_ENCHANT_STRIKETHROUGH:
                lastEnchantX += ButtonSettingToggle.WIDTH + 40;
                addRenderableWidget(new ButtonSettingToggle(lastEnchantX, lastEnchantY + 2.5, setting.getMessage(), setting));
                if (!setting.name().endsWith("_STRIKETHROUGH")) row--; // Except last one
                break;

            case ENCHANT_LAYOUT:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                addRenderableWidget(new ButtonText(halfWidth, (int) y - 10, Translations.getMessage("enchantLayout.title"), true, 0xFFFFFFFF));
                addRenderableWidget(new ButtonCycling(x, (int) y, boxWidth, 20,
                        Arrays.asList(EnchantLayout.values()),
                        feature.getAsEnum(setting).ordinal(),
                        index -> feature.set(setting, EnchantLayout.values()[index])
                ));
                row += 0.5F;
                break;

            case BAZAAR_PRICES_UPDATE_INTERVAL:
                boxWidth = 150;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonText(halfWidth, (int) y - 10, Translations.getMessage("settings.itemPricesInTooltip.bazaarUpdateInterval"), true, ColorCode.GRAY.getColor()));
                row += .1F;
                y = getRowHeightSetting(row);
                addRenderableWidget(
                        new ButtonSlider(
                                x, y, boxWidth, 20,
                                feature.getAsNumber(setting).floatValue(), 20.0F, 120.0F, 1.0F,
                                updatedValue -> feature.set(setting, updatedValue)
                        ).setSuffix(" seconds")
                );
                row += .1F;
                break;

            case LOWEST_BIN_PRICES_UPDATE_INTERVAL:
                boxWidth = 150;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonText(halfWidth, (int) y - 10, Translations.getMessage("settings.itemPricesInTooltip.lowestBinUpdateInterval"), true, ColorCode.GRAY.getColor()));
                row += .1F;
                y = getRowHeightSetting(row);
                addRenderableWidget(
                        new ButtonSlider(
                                x, y, boxWidth, 20,
                                feature.getAsNumber(setting).floatValue(), 60.0F, 300.0F, 1.0F,
                                updatedValue -> feature.set(setting, updatedValue)
                        ).setSuffix(" seconds")
                );
                row += .1F;
                break;

            case LBIN_AVERAGES_TYPE:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonText(halfWidth, (int) y - 10, setting.getMessage(), true, ColorCode.GRAY.getColor()));
                addRenderableWidget(new ButtonCycling(x, (int) y, 140, 20,
                        Arrays.asList(EnumUtils.LBinAveragesType.values()),
                        feature.getAsEnum(setting).ordinal(),
                        index -> feature.set(setting, EnumUtils.LBinAveragesType.values()[index])
                ));
                row += .4F;
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("settings.itemPricesInTooltip.lbinAveragesWarning"), true, ColorCode.GRAY.getColor()));
                row += .2F;
                break;

            case MINIMUM_RARITY_FOR_CONFIRMATION:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonText(halfWidth, (int) y - 10, setting.getMessage(), true, ColorCode.GRAY.getColor()));
                addRenderableWidget(new ButtonCycling(x, (int) y, 140, 20,
                        Arrays.asList(SkyblockRarity.values()),
                        feature.getAsEnum(setting).ordinal(),
                        index -> feature.set(setting, SkyblockRarity.values()[index])
                ));
                row += .4F;
                break;

            default:
                if (setting.isUniversal()) return; // see addUniversalButton()

                boxWidth = ButtonSettingToggle.WIDTH; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                break;
        }
        row++;
    }

    private void addUniversalButton() {
        FeatureGuiData featureGuiData = feature.getFeatureGuiData();
        if (featureGuiData != null) {
            final int halfWidth = width / 2;
            if (feature.couldBeXAllignment()) {
                double x = halfWidth - 15.5D; // - half button width
                double y = getRowHeightSetting(row);
                FeatureSetting xAllignment = FeatureSetting.X_ALLIGNMENT;
                xAllignment.setUniversalFeature(feature);
                addRenderableWidget(new ButtonSettingToggle(x, y, xAllignment.getMessage(), xAllignment));
                row++;
            }
            if (featureGuiData.getDefaultColor() != null) {
                double x = halfWidth - 50; // - half button width
                double y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonOpenColorMenu(x, y - 10, 100, 20, Translations.getMessage("settings.changeColor"), feature));
                row++;
            }
        }
    }

    private void addImportExportButton() {
        // buttonWidth = 100, buttonHeight = 20
        double x = width / 2D;
        double y = BOX_Y + boxHeight + 5; // 5 padding

        addScrollIgnoredButton(new ButtonSolid(
                x - 110, y, 100, 20, ButtonSolid.DEFAULT_BOX_COLOR,
                Translations.getMessage("settings.configExportButton"),
                () -> main.getConfigValuesManager().exportFeatureDataToClipboard(feature),
                "descriptions.exportConfig"
        ));
        addScrollIgnoredButton(new ButtonSolid(
                x + 10, y, 100, 20, ButtonSolid.DEFAULT_BOX_COLOR,
                Translations.getMessage("settings.configImportButton"),
                () -> main.getConfigValuesManager().importFeatureDataFromClipboard(),
                "descriptions.importConfig"
        ));
    }

    // Each row is spaced 0.08 apart, starting at 0.17.
    protected double getRowHeight(double row) {
        row--;
        return 95 + (row * 30); //height*(0.18+(row*0.08));
    }

    protected double getRowHeightSetting(double row) {
        row--;
        return 140 + (row * 35); //height*(0.18+(row*0.08));
    }

    protected void addScrollIgnoredButton(AbstractWidget button) {
        scrollIgnoredButtons.add(button);
        addRenderableWidget(button);
    }

    protected void drawSettingsScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.firstDraw) {
            sortButtonList();
            this.firstDraw = false;
        }

        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        for (Renderable renderable : this.renderables) {
            if (!scrollIgnoredButtons.contains(renderable)) {
                renderable.render(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    private double getMouseScrollY() {
        double result = scrollY;
        scrollY = 0;
        return result;
    }

    // Call mouseClicked in each case to update the focus of the ButtonInputFieldWrappers
    private void updateButtonInputFields(MouseButtonEvent event, boolean isDoubleClick) {
        for (GuiEventListener guiEventListener : this.children()) {
            if (guiEventListener instanceof ButtonInputFieldWrapper bif && bif.mouseClicked(event, isDoubleClick)) {
                bif.playDownSound(MC.getSoundManager());
            }
        }
    }

    @Override
    public void removed() {
        if (!closingGui) {
            closingGui = true;
            main.getRenderListener().setGuiToOpen(lastGUI, lastPage, lastTab);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scrollY = scrollY;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (isScrollable && event.button() == 0) {
            double mx = event.x();
            double my = event.y();
            if (mx >= cachedTrackX && mx <= cachedTrackX + SCROLLBAR_WIDTH
                    && my >= cachedThumbY && my <= cachedThumbY + cachedThumbHeight) {
                isDraggingScrollbar = true;
                dragStartMouseY = my;
                dragStartScrollValue = scrollValue;
                return true;
            }
        }
        boolean consumed = super.mouseClicked(event, isDoubleClick);
        updateButtonInputFields(event, isDoubleClick);
        return consumed;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (isDraggingScrollbar && event.button() == 0 && isScrollable) {
            double mouseDelta = event.y() - dragStartMouseY;
            double scrollablePx = cachedTrackHeight - cachedThumbHeight;
            if (scrollablePx > 0) {
                double newScrollValue = dragStartScrollValue - (mouseDelta / scrollablePx) * maxScrollValue;
                scrollValue = Math.max(-maxScrollValue, Math.min(0, newScrollValue));
            }
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (isDraggingScrollbar && event.button() == 0) {
            isDraggingScrollbar = false;
        }
        return super.mouseReleased(event);
    }

}