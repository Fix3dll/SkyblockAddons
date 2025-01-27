package codes.biscuit.skyblockaddons.gui.screens;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.features.discordrpc.DiscordStatus;
import codes.biscuit.skyblockaddons.features.dungeonmap.DungeonMapManager;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonOpenColorMenu;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonSettingToggle;
import codes.biscuit.skyblockaddons.utils.*;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class SettingsGui extends SkyblockAddonsScreen {

    final ArrayList<GuiButton> scrollIgnoredButtons = new ArrayList<>();
    @Getter final Feature feature;
    @Getter final int lastPage;
    @Getter final EnumUtils.GuiTab lastTab;
    @Getter int page;
    float row = 1;
    int column = 1;
    int displayCount;
    @Setter boolean closingGui;
    boolean reInit = false;

    int scrollValue;
    int maxScrollValue;

    /**
     * The main gui, opened with /sba.
     */
    public SettingsGui(Feature feature, int page, int lastPage, EnumUtils.GuiTab lastTab) {
        this.feature = feature;
        this.page = page;
        this.lastPage = lastPage;
        this.lastTab = lastTab;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        scrollValue = 0;
        maxScrollValue = mc.displayHeight / 2;
        row = 1;
        column = 1;
        buttonList.clear();
        scrollIgnoredButtons.clear();
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
        addSocials(scrollIgnoredButtons);
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
        int scroll = Mouse.getDWheel() / 10;
        if (Math.abs(scrollValue + scroll) <= maxScrollValue) {
            scrollValue += scroll;
        } else {
            scroll = 0;
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
            float numberOfRow = row - 1;
            int height = (int) (getRowHeightSetting(numberOfRow) - 70);
            int y = (int) getRowHeight(1);
            this.maxScrollValue = height - 35; // - 35 because we don't want it to be completely invisible
            DrawUtils.drawRect(x, y, width, height, ColorUtils.getDummySkyblockColor(28, 29, 41, 230), 4);
            // Scroll ability with scissor
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            ScaledResolution sr = new ScaledResolution(mc);
            int scaleFactor = sr.getScaleFactor();
            GL11.glScissor(
                    x * scaleFactor,
                    (sr.getScaledHeight() - (y + height)) * scaleFactor,
                    width * scaleFactor,
                    height * scaleFactor
            );
            drawScaledString(this, Translations.getMessage("settings.settings"), 110 + scrollValue, defaultBlue, 1.5, 0);
            final int finalScroll = scroll;
            buttonList.forEach(guiButton -> {
                if (!scrollIgnoredButtons.contains(guiButton)) {
                    guiButton.yPosition += finalScroll;
                }
            });
        }
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        scrollIgnoredButtons.forEach(guiButton -> guiButton.drawButton(mc, mouseX, mouseY));
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

                buttonList.add(new ButtonText(halfWidth, (int) y - 10, setting.getMessage(), true, 0xFFFFFFFF));
                buttonList.add(new ButtonCycling(x, (int) y, boxWidth, 20, Arrays.asList(DiscordStatus.values()), currentStatus.ordinal(), index -> {
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

                    buttonList.add(new ButtonText(halfWidth, (int) y - 10, Translations.getMessage("messages.fallbackStatus"), true, 0xFFFFFFFF));
                    currentStatus = (DiscordStatus) feature.get(FeatureSetting.DISCORD_RP_AUTO_MODE);
                    buttonList.add(new ButtonCycling(x, (int) y, boxWidth, 20, Arrays.asList(DiscordStatus.values()), currentStatus.ordinal(), index -> {
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

                    buttonList.add(new ButtonInputFieldWrapper(
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
                buttonList.add(new ButtonSettingToggle(x, y, Translations.getMessage("settings.expandDeployableStatus"), setting));
                break;

            case DEPLOYABLE_DISPLAY_STYLE:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                buttonList.add(new ButtonText(halfWidth, (int) y - 10, setting.getMessage(), true, 0xFFFFFFFF));
                buttonList.add(new ButtonCycling(x, (int) y, 140, 20,
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
                buttonList.add(new ButtonText(halfWidth, (int) y - 10, Translations.getMessage("settings.backpackStyle"), true, 0xFFFFFFFF));
                buttonList.add(new ButtonCycling(x, (int) y, 140, 20,
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
                buttonList.add(
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
                buttonList.add(
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
                buttonList.add(new ButtonText(halfWidth, (int) y - 10, setting.getMessage(), true, 0xFFFFFFFF));
                buttonList.add(new ButtonCycling(x, (int) y, 140, 20,
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
                buttonList.add(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                row += .1F;
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("settings.trevorTheTrapper.showQuestCooldownDescription"), true, ColorCode.GRAY.getColor()));
                row += .4F;
                break;

            case TREVOR_HIGHLIGHT_TRACKED_ENTITY:
                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                row += .1F;
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("messages.entityOutlinesRequirement"), true, ColorCode.GRAY.getColor()));
                row += .4F;
                break;

            case CLASS_COLORED_TEAMMATE:
                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                row += .1F;
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonText(halfWidth, (int) y + 15, Translations.getMessage("messages.classColoredTeammateRequirement"), true, ColorCode.GRAY.getColor()));
                row += .4F;
                break;

            default:
                if (setting.isUniversal()) return; // see addUniversalButton()

                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
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
                buttonList.add(new ButtonSettingToggle(x, y, xAllignment.getMessage(), xAllignment));
                row++;
            }
            if (feature.getFeatureGuiData().getDefaultColor() != null) {
                double x = halfWidth - 50; // - half button width
                double y = getRowHeightSetting(row);
                buttonList.add(new ButtonOpenColorMenu(x, y - 10, 100, 20, Translations.getMessage("settings.changeColor"), feature));
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

    protected void addScrollIgnoredButton(GuiButton button) {
        scrollIgnoredButtons.add(button);
        buttonList.add(button);
    }

    @Override
    public void onGuiClosed() {
        if (!closingGui) {
            closingGui = true;
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, lastPage, lastTab);
        } else {
            // Clear universal feature
            FeatureSetting.X_ALLIGNMENT.setUniversalFeature(null);
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