package com.fix3dll.skyblockaddons.gui.screens;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.Language;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.discordrpc.DiscordStatus;
import com.fix3dll.skyblockaddons.features.dungeonmap.DungeonMapManager;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonArrow;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonCycling;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonInputFieldWrapper;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonLanguage;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonSettingToggle;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonSlider;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonText;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonOpenColorMenu;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.Arrays;
import java.util.Map;

public class SettingsGui extends SkyblockAddonsScreen {

    final ObjectArrayList<Renderable> scrollIgnoredButtons = new ObjectArrayList<>();
    @Getter final Feature feature;
    @Getter final int lastPage;
    @Getter final EnumUtils.GuiTab lastTab;
    @Getter final EnumUtils.GUIType lastGUI;
    @Getter int page;
    float row = 1;
    int column = 1;
    int displayCount;
    @Setter boolean closingGui;
    @Setter boolean reInit = false;

    double scrollValue;
    int maxScrollValue;
    double scrollY;

    /**
     * The main gui, opened with /sba.
     */
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
        scrollValue = 0;
        maxScrollValue = MC.getWindow().getScreenHeight() / 2;
        row = 1;
        column = 1;
        clearWidgets();
        if (feature == Feature.LANGUAGE) {
            Language currentLanguage = (Language) feature.getFeatureData().getValue();

            displayCount = findDisplayCount();
            // Add the buttons for each page.
            int skip = (page - 1) * displayCount;

            boolean max = page == 1;
            addScrollIgnoredButton(new ButtonArrow(width / 2 - 15 - 50, height - 70, ButtonArrow.ArrowType.LEFT, max));
            max = Language.values().length - skip - displayCount <= 0;
            addScrollIgnoredButton(new ButtonArrow(width / 2 - 15 + 50, height - 70, ButtonArrow.ArrowType.RIGHT, max));

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
        } else {
            if (feature.hasSettings()) {
                for (Map.Entry<FeatureSetting, Object> entry : feature.getFeatureData().getSettings().entrySet()) {
                    addButton(entry.getKey(), entry.getValue());
                }
            }
            addUniversalButton();
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

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (reInit) {
            reInit = false;
            init();
        }
        double scroll = this.getMouseScrollY() * 16;
        if (Math.abs(scrollValue + scroll) <= maxScrollValue) {
            scrollValue += scroll;
        } else {
            scroll = 0;
        }

        float alphaMultiplier = calculateAlphaMultiplier();
        // Alpha of the text will increase from 0 to 127 over 500ms.
        int alpha = (int) (255 * alphaMultiplier);
        drawGradientBackground(graphics, alpha);

        if (alpha < 4) alpha = 4; // Text under 4 alpha appear 100% transparent for some reason o.O
        int defaultBlue = ColorUtils.getDefaultBlue(alpha * 2);
        drawDefaultTitleText(graphics, this, alpha * 2);

        boolean scissorEnabled = false;
        if (feature != Feature.LANGUAGE) {
            int halfWidth = width / 2;
            int boxWidth = 140;
            int x = halfWidth - 90 - boxWidth;
            int width = halfWidth + 90 + boxWidth;
            width -= x;
            float numberOfRow = row - 1;
            int height = (int) (getRowHeightSetting(numberOfRow) - 70);
            int y = (int) getRowHeight(1);
            this.maxScrollValue = height - 35; // - 35 because we don't want it to be completely invisible
            DrawUtils.drawRoundedRect(graphics, x, y, width, height, 4, ARGB.color(230, 28, 29, 41));
            // Scroll ability with scissor
            graphics.enableScissor(x, y, x + width, y + height);
            scissorEnabled = true;
            drawScaledString(graphics, this, Translations.getMessage("settings.settings"), (int) (110 + scrollValue), defaultBlue, 1.5F, 0);
            final double finalScroll = scroll;
            renderables.forEach(guiButton -> {
                AbstractWidget abstractWidget = (AbstractWidget) guiButton;
                if (!scrollIgnoredButtons.contains(guiButton)) {
                    abstractWidget.setY(abstractWidget.getY() + (int) finalScroll);
                }
            });
        }
        this.drawSettingsScreen(graphics, mouseX, mouseY, partialTick); // Draw buttons.
        if (scissorEnabled) graphics.disableScissor();
        scrollIgnoredButtons.forEach(renderable -> renderable.render(graphics, mouseX, mouseY, partialTick));
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

                boxWidth = 31;
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
                //noinspection ConstantConditions
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
                        feature.getAsEnum(FeatureSetting.PET_ITEM_STYLE).ordinal(),
                        index -> feature.set(setting, EnumUtils.PetItemStyle.values()[index])
                ));
                row += .1F;
                break;

            case TREVOR_SHOW_QUEST_COOLDOWN:
                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                row += .1F;
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("settings.trevorTheTrapper.showQuestCooldownDescription"), true, ColorCode.GRAY.getColor()));
                row += .4F;
                break;

            case TREVOR_HIGHLIGHT_TRACKED_ENTITY:
                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                row += .1F;
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("messages.entityOutlinesRequirement"), true, ColorCode.GRAY.getColor()));
                row += .4F;
                break;

            case CLASS_COLORED_TEAMMATE:
                boxWidth = 31; // Default size and stuff.
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

            default:
                if (setting.isUniversal()) return; // see addUniversalButton()

                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                break;
        }
        row++;
    }

    private void addUniversalButton() {
        if (feature.isGuiFeature()) {
            final int halfWidth = width / 2;
            if (feature.couldBeXAllignment()) {
                double x = halfWidth - 15.5D; // - half button width
                double y = getRowHeightSetting(row);
                FeatureSetting xAllignment = FeatureSetting.X_ALLIGNMENT;
                xAllignment.setUniversalFeature(feature);
                addRenderableWidget(new ButtonSettingToggle(x, y, xAllignment.getMessage(), xAllignment));
                row++;
            }
            if (feature.getFeatureGuiData().getDefaultColor() != null) {
                double x = halfWidth - 50; // - half button width
                double y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonOpenColorMenu(x, y - 10, 100, 20, Translations.getMessage("settings.changeColor"), feature));
                row++;
            }
        }
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
    private void updateButtonInputFields(double mouseX, double mouseY, int button) {
        for (GuiEventListener guiEventListener : this.children()) {
            if (guiEventListener instanceof ButtonInputFieldWrapper bif && bif.mouseClicked(mouseX, mouseY, button)) {
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean consumed =  super.mouseClicked(mouseX, mouseY, button);
        updateButtonInputFields(mouseX, mouseY, button);
        return consumed;
    }

}