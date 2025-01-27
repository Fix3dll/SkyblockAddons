package codes.biscuit.skyblockaddons.gui.screens;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.features.enchants.EnchantLayout;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonArrow;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonCycling;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonText;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonOpenColorMenu;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonSettingToggle;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

import static codes.biscuit.skyblockaddons.core.Translations.getMessage;

public class EnchantmentSettingsGui extends SettingsGui {

    private static final EnumSet<FeatureSetting> ENCHANT_COLORING = EnumSet.of(
            FeatureSetting.HIGHLIGHT_ENCHANTMENTS, FeatureSetting.PERFECT_ENCHANT_COLOR,
            FeatureSetting.GREAT_ENCHANT_COLOR, FeatureSetting.GOOD_ENCHANT_COLOR,
            FeatureSetting.POOR_ENCHANT_COLOR, FeatureSetting.COMMA_ENCHANT_COLOR
    );
    private static final EnumSet<FeatureSetting> ORGANIZATION = EnumSet.of(
            FeatureSetting.ENCHANT_LAYOUT, FeatureSetting.HIDE_ENCHANTMENT_LORE, FeatureSetting.HIDE_GREY_ENCHANTS
    );

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
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        scrollValue = 0;
        maxScrollValue = mc.displayHeight / 2;
        row = 1;
        column = 1;
        buttonList.clear();
        scrollIgnoredButtons.clear();
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
        ArrayList<GuiButton> socials = new ArrayList<>();
        addSocials(socials);
        socials.forEach(this::addScrollIgnoredButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
    }

    private void addButton(FeatureSetting setting) {
        int halfWidth = width / 2;
        int boxWidth = 100;
        int x = halfWidth - (boxWidth / 2);
        double y = getRowHeightSetting(row);

        switch (setting) {
            case PERFECT_ENCHANT_COLOR:
            case GREAT_ENCHANT_COLOR:
            case GOOD_ENCHANT_COLOR:
            case POOR_ENCHANT_COLOR:
            case COMMA_ENCHANT_COLOR:
                buttonList.add(new ButtonOpenColorMenu(x, y, 100, 20, setting.getMessage(), setting));
                break;

            case ENCHANT_LAYOUT:
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                EnchantLayout currentStatus = (EnchantLayout) feature.getAsEnum(FeatureSetting.ENCHANT_LAYOUT);

                buttonList.add(new ButtonText(halfWidth, (int) y - 10, getMessage("enchantLayout.title"), true, 0xFFFFFFFF));
                buttonList.add(new ButtonCycling(x, (int) y, boxWidth, 20, Arrays.asList(EnchantLayout.values()), currentStatus.ordinal(), index -> {
                    final EnchantLayout enchantLayout = EnchantLayout.values()[index];
                    feature.set(setting, enchantLayout);
                    this.reInit = true;
                }));
                row += .4F;
                break;

            default:
                boxWidth = 31; // Default size and stuff.
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);
                buttonList.add(new ButtonSettingToggle(x, y, setting.getMessage(), setting));
                break;
        }
        row++;
    }

}