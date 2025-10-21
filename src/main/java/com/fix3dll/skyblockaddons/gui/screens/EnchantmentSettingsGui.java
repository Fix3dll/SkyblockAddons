package com.fix3dll.skyblockaddons.gui.screens;

import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.enchants.EnchantLayout;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonArrow;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonCycling;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonText;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonOpenColorMenu;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonSettingToggle;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Arrays;
import java.util.EnumSet;

public class EnchantmentSettingsGui extends SettingsGui {

    private static final EnumSet<FeatureSetting> ENCHANT_COLORING = EnumSet.of(FeatureSetting.HIGHLIGHT_ENCHANTMENTS,
            FeatureSetting.PERFECT_ENCHANT_COLOR, FeatureSetting.GREAT_ENCHANT_COLOR, FeatureSetting.GOOD_ENCHANT_COLOR,
            FeatureSetting.POOR_ENCHANT_COLOR, FeatureSetting.COMMA_ENCHANT_COLOR);
    private static final EnumSet<FeatureSetting> ORGANIZATION = EnumSet.of(FeatureSetting.ENCHANT_LAYOUT,
            FeatureSetting.HIDE_ENCHANTMENT_LORE, FeatureSetting.HIDE_GREY_ENCHANTS);

    private int maxPage;

    public EnchantmentSettingsGui(int page, int lastPage, EnumUtils.GuiTab lastTab) {
        super(Feature.ENCHANTMENT_LORE_PARSING, page, lastPage, lastTab, EnumUtils.GUIType.MAIN);
        maxPage = 1;
        if (feature.hasSettings()) {
            for (FeatureSetting setting : feature.getFeatureData().getSettings().keySet()) {
                if (!(ENCHANT_COLORING.contains(setting) || ORGANIZATION.contains(setting))) {
                    maxPage = 2;
                    break;
                }
            }
        } else {
            throw new IllegalStateException("Unexpected feature on EnchantmentSettingsGui: " + feature);
        }
    }

    @Override
    public void init() {
        row = 1;
        column = 1;
        clearWidgets();
        for (FeatureSetting setting : feature.getFeatureData().getSettings().keySet()) {
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
        row += .4F;
        addScrollIgnoredButton(new ButtonArrow(width / 2D - 15 - 150, height - 70, ButtonArrow.ArrowType.LEFT, page == 0));
        addScrollIgnoredButton(new ButtonArrow(width / 2D - 15 + 150, height - 70, ButtonArrow.ArrowType.RIGHT, page == maxPage));
        addSocials(this::addScrollIgnoredButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick); // Draw buttons.
    }

    private void addButton(FeatureSetting setting) {
        int halfWidth = width / 2;
        int boxWidth = 100;
        int x = halfWidth - (boxWidth / 2);
        double y = getRowHeightSetting(row);

        switch (setting) {
            case COLOR:
                addRenderableWidget(new ButtonOpenColorMenu(x, y, 100, 20, Translations.getMessage("settings.changeColor"), feature));
                break;

            case PERFECT_ENCHANT_COLOR:
            case GREAT_ENCHANT_COLOR:
            case GOOD_ENCHANT_COLOR:
            case POOR_ENCHANT_COLOR:
            case COMMA_ENCHANT_COLOR:
                // Temp hardcode until feature rewrite
                addRenderableWidget(new ButtonOpenColorMenu(x, y, 100, 20, setting.getMessage(), setting));
                break;

            case ENCHANT_LAYOUT:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                EnchantLayout currentStatus = (EnchantLayout) feature.getAsEnum(FeatureSetting.ENCHANT_LAYOUT);

                addRenderableWidget(new ButtonText(halfWidth, (int) y - 10, Translations.getMessage("enchantLayout.title"), true, 0xFFFFFFFF));
                addRenderableWidget(new ButtonCycling(x, (int) y, boxWidth, 20, Arrays.asList(EnchantLayout.values()), currentStatus.ordinal(), index -> {
                    final EnchantLayout enchantLayout = EnchantLayout.values()[index];
                    feature.set(setting, enchantLayout);
                    this.reInit = true;
                }));
                row += 0.4F;
                break;

            default:
                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                addRenderableWidget(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                break;
        }
        row++;
    }

}