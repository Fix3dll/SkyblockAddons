package codes.biscuit.skyblockaddons.gui.screens;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.enchants.EnchantListLayout;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonFeature;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonOpenColorMenu;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils.FeatureSetting;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static codes.biscuit.skyblockaddons.core.Translations.getMessage;

public class EnchantmentSettingsGui extends SettingsGui {

    private final EnumSet<FeatureSetting> ENCHANT_COLORING = EnumSet.of(FeatureSetting.HIGHLIGHT_ENCHANTMENTS,
            FeatureSetting.PERFECT_ENCHANT_COLOR, FeatureSetting.GREAT_ENCHANT_COLOR, FeatureSetting.GOOD_ENCHANT_COLOR,
            FeatureSetting.POOR_ENCHANT_COLOR, FeatureSetting.COMMA_ENCHANT_COLOR);
    private final EnumSet<FeatureSetting> ORGANIZATION = EnumSet.of(FeatureSetting.ENCHANT_LAYOUT,
            FeatureSetting.HIDE_ENCHANTMENT_LORE, FeatureSetting.HIDE_GREY_ENCHANTS);
    private final EnumSet<Feature> ENCHANT_COLOR_FEATURES = EnumSet.of(Feature.ENCHANTMENT_PERFECT_COLOR,
            Feature.ENCHANTMENT_GREAT_COLOR,Feature.ENCHANTMENT_GOOD_COLOR, Feature.ENCHANTMENT_POOR_COLOR,
            Feature.ENCHANTMENT_COMMA_COLOR);


    private int maxPage;

    public EnchantmentSettingsGui(Feature feature, int page, int lastPage, EnumUtils.GuiTab lastTab, List<FeatureSetting> settings) {
        super(feature, page, lastPage, lastTab, settings);
        maxPage = 1;
        for (FeatureSetting setting : settings) {
            if (!(ENCHANT_COLORING.contains(setting) || ORGANIZATION.contains(setting))) {
                maxPage = 2;
                break;
            }
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        row = 1;
        column = 1;
        buttonList.clear();
        for (FeatureSetting setting : settings) {
            switch (page) {
                case 0:
                    if (ORGANIZATION.contains(setting)) {
                        addButton(setting);
                    }
                    break;

                case 1:
                    if (ENCHANT_COLORING.contains(setting)) {
                        addButton(setting);
                    }
                    break;

                case 2:
                    if (!(ENCHANT_COLORING.contains(setting) || ORGANIZATION.contains(setting))) {
                        addButton(setting);
                    }
                    break;
            }
        }
        buttonList.add(new ButtonArrow(width / 2D - 15 - 150, height - 70, ButtonArrow.ArrowType.LEFT, page == 0));
        buttonList.add(new ButtonArrow(width / 2D - 15 + 150, height - 70, ButtonArrow.ArrowType.RIGHT, page == maxPage));
        addSocials();
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
        int defaultBlue = main.getUtils().getDefaultBlue(alpha * 2);
        drawDefaultTitleText(this, alpha * 2);

        if (feature != Feature.LANGUAGE) {
            int halfWidth = width / 2;
            int boxWidth = 140;
            int x = halfWidth - 90 - boxWidth;
            int width = halfWidth + 90 + boxWidth;
            width -= x;
            float numSettings;
            if (page == 0) {
                numSettings = ORGANIZATION.size();
            } else if (page == 1) {
                numSettings = ENCHANT_COLORING.size();
            } else {
                numSettings = Math.max(settings.size() - ORGANIZATION.size() - ENCHANT_COLORING.size(), 1);
            }
            int height = (int) (getRowHeightSetting(numSettings) - 50);
            int y = (int) getRowHeight(1);
            DrawUtils.drawRect(x, y, width, height, ColorUtils.getDummySkyblockColor(28, 29, 41, 230), 4);

            SkyblockAddonsGui.drawScaledString(this, getMessage("settings.settings"), 110, defaultBlue, 1.5, 0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
        GlStateManager.disableBlend();
    }

    /**
     * Code to perform the button toggles, openings of other gui's/pages, and language changes.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonOpenColorMenu) {
            closingGui = true;
            // Temp fix until feature re-write. Open a color selection panel specific to the color setting
            Feature f = ((ButtonOpenColorMenu) abstractButton).feature;
            if (ENCHANT_COLOR_FEATURES.contains(f)) {
                mc.displayGuiScreen(new ColorSelectionGui(f, EnumUtils.GUIType.SETTINGS, lastTab, page));
            } else {
                mc.displayGuiScreen(new ColorSelectionGui(feature, EnumUtils.GUIType.SETTINGS, lastTab, lastPage));
            }

        } else if (abstractButton instanceof ButtonSettingToggle) {
            ButtonFeature button = (ButtonFeature) abstractButton;
            Feature feature = button.getFeature();
            if (feature == null) return;
            feature.setEnabled(feature.isDisabled());
            ((ButtonSettingToggle)abstractButton).onClick();

        } else if (abstractButton instanceof ButtonArrow) {
            ButtonArrow arrow = (ButtonArrow) abstractButton;
            if (arrow.isNotMax()) {
                main.getUtils().setFadingIn(false);
                if (arrow.getArrowType() == ButtonArrow.ArrowType.RIGHT) {
                    closingGui = true;
                    mc.displayGuiScreen(new EnchantmentSettingsGui(feature, ++page, lastPage, lastTab, settings));
                } else {
                    closingGui = true;
                    mc.displayGuiScreen(new EnchantmentSettingsGui(feature, --page, lastPage, lastTab, settings));
                }
            }
        }
    }

    private void addButton(FeatureSetting setting) {
        int halfWidth = width / 2;
        int boxWidth = 100;
        int x = halfWidth - (boxWidth / 2);
        double y = getRowHeightSetting(row);

        switch (setting) {
            case COLOR:
                buttonList.add(new ButtonOpenColorMenu(x, y, 100, 20, getMessage("settings.changeColor"), feature));
                break;

            case PERFECT_ENCHANT_COLOR:
            case GREAT_ENCHANT_COLOR:
            case GOOD_ENCHANT_COLOR:
            case POOR_ENCHANT_COLOR:
            case COMMA_ENCHANT_COLOR:
                // Temp hardcode until feature rewrite
                buttonList.add(new ButtonOpenColorMenu(x, y, 100, 20, setting.getMessage(), setting.getFeatureEquivalent()));
                break;

            case ENCHANT_LAYOUT:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                EnchantListLayout currentStatus = main.getConfigValues().getEnchantLayout();

                buttonList.add(new ButtonText(halfWidth, (int) y - 10, getMessage("enchantLayout.title"), true, 0xFFFFFFFF));
                buttonList.add(new ButtonCycling(x, (int) y, boxWidth, 20, Arrays.asList(EnchantListLayout.values()), currentStatus.ordinal(), index -> {
                    final EnchantListLayout enchantLayout = EnchantListLayout.values()[index];
                    main.getConfigValues().setEnchantLayout(enchantLayout);
                    reInit = true;
                }));
                row += 0.4F;
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
    private double getRowHeight(double row) {
        row--;
        return 95 + (row * 30); //height*(0.18+(row*0.08));
    }

    private double getRowHeightSetting(double row) {
        row--;
        return 140 + (row * 35); //height*(0.18+(row*0.08));
    }

    @Override
    public void onGuiClosed() {
        if (!closingGui) {
            returnToGui();
        }
        Keyboard.enableRepeatEvents(false);
    }

    private void returnToGui() {
        closingGui = true;
        main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, lastPage, lastTab);
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
