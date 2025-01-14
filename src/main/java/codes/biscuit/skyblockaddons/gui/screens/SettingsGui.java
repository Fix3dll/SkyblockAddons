package codes.biscuit.skyblockaddons.gui.screens;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.features.discordrpc.DiscordStatus;
import codes.biscuit.skyblockaddons.features.dungeonmap.DungeonMapManager;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonOpenColorMenu;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonSettingToggle;
import codes.biscuit.skyblockaddons.utils.*;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SettingsGui extends SkyblockAddonsScreen {

    @Getter final Feature feature;
    @Getter final int lastPage;
    @Getter final EnumUtils.GuiTab lastTab;
    @Getter int page;
    final List<EnumUtils.FeatureSetting> settings;
    float row = 1;
    int column = 1;
    int displayCount;
    @Setter boolean closingGui;
    boolean reInit = false;

    /**
     * The main gui, opened with /sba.
     */
    public SettingsGui(Feature feature, int page, int lastPage, EnumUtils.GuiTab lastTab) {
        this.feature = feature;
        this.page = page;
        this.lastPage = lastPage;
        this.lastTab = lastTab;
        this.settings = feature != null ? feature.getSettings() : Collections.emptyList();
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        row = 1;
        column = 1;
        buttonList.clear();
        if (feature == Feature.LANGUAGE) {
            Language currentLanguage = Language.getFromPath(main.getConfigValues().getLanguage().getPath());

            displayCount = findDisplayCount();
            // Add the buttons for each page.
            int skip = (page - 1) * displayCount;

            boolean max = page == 1;
            buttonList.add(new ButtonArrow(width / 2 - 15 - 50, height - 70, ButtonArrow.ArrowType.LEFT, max));
            max = Language.values().length - skip - displayCount <= 0;
            buttonList.add(new ButtonArrow(width / 2 - 15 + 50, height - 70, ButtonArrow.ArrowType.RIGHT, max));

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

            main.getConfigValues().setLanguage(currentLanguage);
            DataUtils.loadLocalizedStrings(false);
        } else {
            for (EnumUtils.FeatureSetting setting : settings) {
                addButton(setting);
            }
        }
        addSocials();
    }

    private int findDisplayCount() {
        int maxX = new ScaledResolution(mc).getScaledHeight() - 70 - 25;
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

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (reInit) {
            reInit = false;
            initGui();
        }

        float alphaMultiplier = calculateAlphaMultiplier();
        // Alpha of the text will increase from 0 to 127 over 500ms.
        int alpha = (int) (255 * alphaMultiplier);
        GlStateManager.enableBlend();
        drawGradientBackground(alpha);

        if (alpha < 4) alpha = 4; // Text under 4 alpha appear 100% transparent for some reason o.O
        int defaultBlue = ColorUtils.getDefaultBlue(alpha * 2);
        drawDefaultTitleText(this, alpha * 2);

        if (feature != Feature.LANGUAGE) {
            int halfWidth = width / 2;
            int boxWidth = 140;
            int x = halfWidth - 90 - boxWidth;
            int width = halfWidth + 90 + boxWidth;
            width -= x;
            float numSettings = settings.size();
            if (settings.contains(EnumUtils.FeatureSetting.DISCORD_RP_STATE)) {
                if (main.getConfigValues().getDiscordStatus() == DiscordStatus.CUSTOM) numSettings++;
                if (main.getConfigValues().getDiscordStatus() == DiscordStatus.AUTO_STATUS) {
                    numSettings++;
                    if (main.getConfigValues().getDiscordAutoDefault() == DiscordStatus.CUSTOM) {
                        numSettings++;
                    }
                }
                numSettings += 0.4f;
            }
            if (settings.contains(EnumUtils.FeatureSetting.DISCORD_RP_DETAILS)) {
                if (main.getConfigValues().getDiscordDetails() == DiscordStatus.CUSTOM) numSettings++;
                if (main.getConfigValues().getDiscordDetails() == DiscordStatus.AUTO_STATUS) {
                    numSettings++;
                    if (main.getConfigValues().getDiscordAutoDefault() == DiscordStatus.CUSTOM) {
                        numSettings++;
                    }
                }
                numSettings += 0.4f;
            }
            int height = (int) (getRowHeightSetting(numSettings) - 50);
            int y = (int) getRowHeight(1);
            if (!(this instanceof EnchantmentSettingsGui)) {
                DrawUtils.drawRect(x, y, width, height, ColorUtils.getDummySkyblockColor(28, 29, 41, 230), 4);
            }
            drawScaledString(this, Translations.getMessage("settings.settings"), 110, defaultBlue, 1.5, 0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
        GlStateManager.disableBlend();
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
        buttonList.add(new ButtonLanguage(x, y, text, language));
        column++;
        if (column > 3) {
            column = 1;
            row++;
        }
        displayCount--;
    }

    private void addButton(EnumUtils.FeatureSetting setting) {
        int halfWidth = width / 2;
        int boxWidth = 100;
        int x = halfWidth - (boxWidth / 2);
        double y = getRowHeightSetting(row);
        Feature settingFeature = null;
        switch (setting) {
            case COLOR:
                buttonList.add(new ButtonOpenColorMenu(x, y, 100, 20, Translations.getMessage("settings.changeColor"), feature));
                break;

            case REPEATING:
                boxWidth = 31;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);

                if (feature == Feature.FULL_INVENTORY_WARNING) {
                    settingFeature = Feature.REPEAT_FULL_INVENTORY_WARNING;
                } else if (feature == Feature.BOSS_APPROACH_ALERT) {
                    settingFeature = Feature.REPEAT_SLAYER_BOSS_WARNING;
                }
                buttonList.add(new ButtonSettingToggle(x, y, Translations.getMessage("settings.repeating"), settingFeature));
                break;

            case ENABLED_IN_OTHER_GAMES:
                boxWidth = 31;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);

                if (feature == Feature.DARK_AUCTION_TIMER) {
                    settingFeature = Feature.SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES;
                } else if  (feature == Feature.FARM_EVENT_TIMER) {
                    settingFeature = Feature.SHOW_FARM_EVENT_TIMER_IN_OTHER_GAMES;
                } else if (feature == Feature.DROP_CONFIRMATION) {
                    settingFeature = Feature.DOUBLE_DROP_IN_OTHER_GAMES;
                } else if (feature == Feature.OUTBID_ALERT_SOUND) {
                    settingFeature = Feature.OUTBID_ALERT_SOUND_IN_OTHER_GAMES;
                }
                buttonList.add(new ButtonSettingToggle(x, y, Translations.getMessage("settings.showInOtherGames"), settingFeature));
                break;

            case BACKPACK_STYLE:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                buttonList.add(new ButtonText(halfWidth, (int) y - 10, Translations.getMessage("settings.backpackStyle"), true, 0xFFFFFFFF));
                buttonList.add(new ButtonCycling(x, (int) y, 140, 20,
                        Arrays.asList(EnumUtils.BackpackStyle.values()),
                        main.getConfigValues().getBackpackStyle().ordinal(),
                        index -> main.getConfigValues().setBackpackStyle(EnumUtils.BackpackStyle.values()[index])
                ));
                break;

            case DEPLOYABLE_DISPLAY_STYLE:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                buttonList.add(new ButtonText(halfWidth, (int) y - 10, setting.getMessage(), true, 0xFFFFFFFF));
                buttonList.add(new ButtonCycling(x, (int) y, 140, 20,
                        Arrays.asList(EnumUtils.DeployableDisplayStyle.values()),
                        main.getConfigValues().getDeployableDisplayStyle().ordinal(),
                        index -> {
                            main.getConfigValues().setDeployableDisplayStyle(EnumUtils.DeployableDisplayStyle.values()[index]);
                            reInit = true;
                        }
                ));
                break;

            case EXPAND_DEPLOYABLE_STATUS:
                boxWidth = 31;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);

                if (main.getConfigValues().getDeployableDisplayStyle().equals(EnumUtils.DeployableDisplayStyle.DETAILED)) {
                    settingFeature = Feature.EXPAND_DEPLOYABLE_STATUS;
                    buttonList.add(new ButtonSettingToggle(x, y, Translations.getMessage("settings.expandDeployableStatus"), settingFeature));
                }
                break;

            case DISCORD_RP_DETAILS: case DISCORD_RP_STATE:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                DiscordStatus currentStatus;
                if (setting == EnumUtils.FeatureSetting.DISCORD_RP_STATE) {
                    currentStatus = main.getConfigValues().getDiscordStatus();
                } else {
                    currentStatus = main.getConfigValues().getDiscordDetails();
                }

                buttonList.add(new ButtonText(halfWidth, (int) y - 10, setting == EnumUtils.FeatureSetting.DISCORD_RP_DETAILS ? Translations.getMessage("messages.firstStatus") :
                        Translations.getMessage("messages.secondStatus"), true, 0xFFFFFFFF));
                buttonList.add(new ButtonCycling(x, (int) y, boxWidth, 20, Arrays.asList(DiscordStatus.values()), currentStatus.ordinal(), index -> {
                    final DiscordStatus selectedStatus = DiscordStatus.values()[index];
                    if (setting == EnumUtils.FeatureSetting.DISCORD_RP_STATE) {
                        main.getDiscordRPCManager().setStateLine(selectedStatus);
                        main.getConfigValues().setDiscordStatus(selectedStatus);
                    } else {
                        main.getDiscordRPCManager().setDetailsLine(selectedStatus);
                        main.getConfigValues().setDiscordDetails(selectedStatus);
                    }
                    SettingsGui.this.reInit = true;
                }));

                if (currentStatus == DiscordStatus.AUTO_STATUS) {
                    row++;
                    row += 0.4F;
                    x = halfWidth - (boxWidth / 2);
                    y = getRowHeightSetting(row);

                    buttonList.add(new ButtonText(halfWidth, (int) y - 10, Translations.getMessage("messages.fallbackStatus"), true, 0xFFFFFFFF));
                    currentStatus = main.getConfigValues().getDiscordAutoDefault();
                    buttonList.add(new ButtonCycling(x, (int) y, boxWidth, 20, Arrays.asList(DiscordStatus.values()), currentStatus.ordinal(), index -> {
                        final DiscordStatus selectedStatus = DiscordStatus.values()[index];
                        main.getConfigValues().setDiscordAutoDefault(selectedStatus);
                        SettingsGui.this.reInit = true;
                    }));
                }

                if (currentStatus == DiscordStatus.CUSTOM) {
                    row++;
                    halfWidth = width / 2;
                    boxWidth = 200;
                    x = halfWidth - (boxWidth / 2);
                    y = getRowHeightSetting(row);

                    EnumUtils.DiscordStatusEntry discordStatusEntry = EnumUtils.DiscordStatusEntry.DETAILS;
                    if (setting == EnumUtils.FeatureSetting.DISCORD_RP_STATE) {
                        discordStatusEntry = EnumUtils.DiscordStatusEntry.STATE;
                    }
                    final EnumUtils.DiscordStatusEntry finalDiscordStatusEntry = discordStatusEntry;
                    ButtonInputFieldWrapper inputField = new ButtonInputFieldWrapper(x, (int) y, 200, 20, main.getConfigValues().getCustomStatus(discordStatusEntry),
                            null, 100, false, updatedValue -> main.getConfigValues().setCustomStatus(finalDiscordStatusEntry, updatedValue));
                    buttonList.add(inputField);
                }
                row += 0.4F;
                break;

            case MAP_ZOOM:
                // For clarity
                //noinspection ConstantConditions
                boxWidth = 100; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                buttonList.add(
                        new ButtonSlider(x, y, 100, 20,
                                DungeonMapManager.getMapZoom(),
                                DungeonMapManager.MIN_ZOOM,
                                DungeonMapManager.MAX_ZOOM,
                                0.05F,
                                DungeonMapManager::setMapZoom
                        ).setPrefix("Map Zoom: ")
                );
                break;

            case COLOUR_BY_RARITY:
                boxWidth = 31;
                x = halfWidth - boxWidth / 2;
                y = this.getRowHeightSetting(this.row);
                switch (feature) {
                    case SHOW_BASE_STAT_BOOST_PERCENTAGE:
                        settingFeature = Feature.BASE_STAT_BOOST_COLOR_BY_RARITY;
                        break;
                    case REVENANT_SLAYER_TRACKER:
                        settingFeature = Feature.REVENANT_COLOR_BY_RARITY;
                        break;
                    case TARANTULA_SLAYER_TRACKER:
                        settingFeature = Feature.TARANTULA_COLOR_BY_RARITY;
                        break;
                    case SVEN_SLAYER_TRACKER:
                        settingFeature = Feature.SVEN_COLOR_BY_RARITY;
                        break;
                    case VOIDGLOOM_SLAYER_TRACKER:
                        settingFeature = Feature.VOIDGLOOM_COLOR_BY_RARITY;
                        break;
                    case INFERNO_SLAYER_TRACKER:
                        settingFeature = Feature.INFERNO_COLOR_BY_RARITY;
                        break;
                    case RIFTSTALKER_SLAYER_TRACKER:
                        settingFeature = Feature.RIFTSTALKER_COLOR_BY_RARITY;
                        break;
                    case DRAGON_STATS_TRACKER:
                        settingFeature = Feature.DRAGON_STATS_TRACKER_COLOR_BY_RARITY;
                        break;
                }
                buttonList.add(new ButtonSettingToggle(x, y, Translations.getMessage("settings.colorByRarity"), settingFeature));
                break;

            case TEXT_MODE:
                boxWidth = 31;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);

                if (feature == Feature.REVENANT_SLAYER_TRACKER) {
                    settingFeature = Feature.REVENANT_TEXT_MODE;

                } else if (feature == Feature.TARANTULA_SLAYER_TRACKER) {
                    settingFeature = Feature.TARANTULA_TEXT_MODE;

                } else if (feature == Feature.SVEN_SLAYER_TRACKER) {
                    settingFeature = Feature.SVEN_TEXT_MODE;

                } else if (feature == Feature.VOIDGLOOM_SLAYER_TRACKER) {
                    settingFeature = Feature.VOIDGLOOM_TEXT_MODE;

                } else if (feature == Feature.INFERNO_SLAYER_TRACKER) {
                    settingFeature = Feature.INFERNO_TEXT_MODE;

                } else if (feature == Feature.RIFTSTALKER_SLAYER_TRACKER) {
                    settingFeature = Feature.RIFTSTALKER_TEXT_MODE;

                } else if (feature == Feature.DRAGON_STATS_TRACKER_TEXT_MODE) {
                    settingFeature = Feature.DRAGON_STATS_TRACKER_TEXT_MODE;
                }
                buttonList.add(new ButtonSettingToggle(x, y, Translations.getMessage("settings.textMode"), settingFeature));
                break;

            case ZEALOT_SPAWN_AREAS_ONLY:
                boxWidth = 31;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);

                if (feature == Feature.ZEALOT_COUNTER) {
                    settingFeature = Feature.ZEALOT_COUNTER_ZEALOT_SPAWN_AREAS_ONLY;
                } else if (feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
                    settingFeature = Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE_ZEALOT_SPAWN_AREAS_ONLY;
                } else if (feature == Feature.SHOW_TOTAL_ZEALOT_COUNT) {
                    settingFeature = Feature.SHOW_TOTAL_ZEALOT_COUNT_ZEALOT_SPAWN_AREAS_ONLY;
                } else if (feature == Feature.SHOW_SUMMONING_EYE_COUNT) {
                    settingFeature = Feature.SHOW_SUMMONING_EYE_COUNT_ZEALOT_SPAWN_AREAS_ONLY;
                }
                buttonList.add(new ButtonSettingToggle(x, y, setting.getMessage(), settingFeature));
                break;

            case DISABLE_SPIRIT_SCEPTRE_MESSAGES:
                boxWidth = 31;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonSettingToggle(x, y, setting.getMessage(), Feature.DISABLE_SPIRIT_SCEPTRE_MESSAGES));
                break;

            case HEALING_CIRCLE_OPACITY:
                boxWidth = 150;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                buttonList.add(
                        new ButtonSlider(
                                x, y, boxWidth, 20,
                                main.getConfigValues().getHealingCircleOpacity().getValue(), 0, 1, 0.01F,
                                updatedValue -> main.getConfigValues().getHealingCircleOpacity().setValue(updatedValue)
                        ).setPrefix("Healing Circle Opacity: ")
                );

            case TREVOR_SHOW_QUEST_COOLDOWN:
                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonSettingToggle(x, y, setting.getMessage(), setting.getFeatureEquivalent()));
                row += .1F;
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("settings.trevorTheTrapper.showQuestCooldownDescription"), true, ColorCode.GRAY.getColor()));
                row += .4F;
                break;

            case TREVOR_HIGHLIGHT_TRACKED_ENTITY:
                if (feature != Feature.ENTITY_OUTLINES) return;
                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonSettingToggle(x, y, setting.getMessage(), setting.getFeatureEquivalent()));
                row += .4F;
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("messages.entityOutlinesRequirement"), true, ColorCode.GRAY.getColor()));
                row += .4F;
                break;

            case PET_ITEM_STYLE:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                buttonList.add(new ButtonText(halfWidth, (int) y - 10, setting.getMessage(), true, 0xFFFFFFFF));
                buttonList.add(new ButtonCycling(x, (int) y, 140, 20,
                        Arrays.asList(EnumUtils.PetItemStyle.values()),
                        main.getConfigValues().getPetItemStyle().ordinal(),
                        index -> main.getConfigValues().setPetItemStyle(EnumUtils.PetItemStyle.values()[index])
                ));
                break;

            case CLASS_COLORED_TEAMMATE:
                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonSettingToggle(x, y, setting.getMessage(), setting.getFeatureEquivalent()));
                row += .4F;
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("messages.classColoredTeammateRequirement"), true, ColorCode.GRAY.getColor()));
                row += .4F;
                break;

            default:
                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonSettingToggle(x, y, setting.getMessage(), setting.getFeatureEquivalent()));
                break;
        }
        row++;
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

    @Override
    public void onGuiClosed() {
        if (!closingGui) {
            closingGui = true;
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, lastPage, lastTab);
        }
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        ButtonInputFieldWrapper.callKeyTyped(buttonList, typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ButtonInputFieldWrapper.callUpdateScreen(buttonList);
    }
}
