package com.fix3dll.skyblockaddons.gui.screens;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonColorBox;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonSlider;
import com.fix3dll.skyblockaddons.gui.elements.CheckBox;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils.ChromaMode;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorSelectionGui extends SkyblockAddonsScreen {

    private static final ResourceLocation COLOR_PICKER = ResourceLocation.fromNamespaceAndPath(SkyblockAddons.MOD_ID, "gui/colorpicker.png");
    private static NativeImage COLOR_PICKER_IMAGE = loadColorPicker();

    // The feature that this color is for.
    private final Feature feature;
    private final FeatureSetting setting;

    // Previous pages for when they return.
    private final EnumUtils.GUIType lastGUI;
    private final EnumUtils.GuiTab lastTab;
    private final int lastPage;

    private final boolean isRestricted;
    private final Supplier<Boolean> isChroma;
    private final Supplier<Integer> color;
    private final Consumer<Boolean> setChroma;
    private final Consumer<Integer> setColor;


    private int imageX;
    private int imageY;

    private EditBox hexColorField;
    private CheckBox chromaCheckbox;

    /**
     * Creates a gui to allow you to select a color for a specific feature.
     *
     * @param feature The feature that this color is for.
     * @param lastTab The previous tab that you came from.
     * @param lastPage The previous page.
     */
    public ColorSelectionGui(Feature feature, EnumUtils.GUIType lastGUI, EnumUtils.GuiTab lastTab, int lastPage) {
        super(Component.empty());
        this.feature = feature;
        this.setting = null;
        this.lastTab = lastTab;
        this.lastGUI = lastGUI;
        this.lastPage = lastPage;
        this.isRestricted = feature.isGuiFeature() && feature.getFeatureGuiData().isColorsRestricted();
        this.isChroma = feature::isChroma;
        this.color = feature::getColor;
        this.setColor = feature::setColor;
        this.setChroma = feature::setChroma;
    }

    /**
     * Creates a gui to allow you to select a color for a specific setting.
     * @param setting The setting that this color is for.
     * @param lastGUI The previous GUI that you came from.
     * @param lastTab The previous tab that you came from.
     * @param lastPage The previous page.
     * @exception NullPointerException if setting doesn't have related Feature
     */
    public ColorSelectionGui(FeatureSetting setting, EnumUtils.GUIType lastGUI, EnumUtils.GuiTab lastTab, int lastPage) {
        super(Component.empty());
        this.feature = setting.getRelatedFeature();
        this.setting = setting;
        this.lastTab = lastTab;
        this.lastGUI = lastGUI;
        this.lastPage = lastPage;
        if (feature == null) {
            throw new IllegalArgumentException("Feature cannot be null");
        }
        Object settingValue = feature.get(setting);
        if (!(settingValue instanceof ColorCode || settingValue instanceof Number)) {
            throw new IllegalArgumentException("Setting value is not a ColorCode or a Number");
        }
        this.isRestricted = settingValue instanceof ColorCode;
        this.isChroma= () -> {
            if (this.isRestricted) {
                return feature.get(setting) == ColorCode.CHROMA;
            } else {
                return feature.getAsNumber(setting).intValue() == ColorCode.CHROMA.getColor();
            }
        };
        this.color = () -> {
            if (this.isRestricted) {
                return ((ColorCode) feature.get(setting)).getColor();
            } else {
                return feature.getAsNumber(setting).intValue();
            }
        };
        this.setColor = integer -> {
            if (this.isRestricted) {
                ColorCode colorCode = ColorCode.getByARGB(integer);
                if (colorCode != null) {
                    feature.set(setting, colorCode);
                }
            } else {
                feature.set(setting, integer);
            }
        };
        this.setChroma = setChroma -> {
            if (setChroma) {
                if (this.isRestricted) {
                    feature.set(setting, ColorCode.CHROMA);
                } else {
                    feature.set(setting, ColorCode.CHROMA.getColor());
                }
            } else {
                main.getConfigValuesManager().setSettingToDefault(setting);
            }
        };
    }

    @Override
    public void init() {
        if (COLOR_PICKER_IMAGE == null) {
            COLOR_PICKER_IMAGE = loadColorPicker();
        }

        boolean isChroma = this.isChroma.get();

        if (chromaCheckbox == null) {
            chromaCheckbox = new CheckBox(width / 2 + 88, 170, 12, Translations.getMessage("messages.chroma"), false);
            chromaCheckbox.setValue(isChroma);
            chromaCheckbox.setOnToggleListener(value -> {
                setChroma.accept(value);
                this.removeChromaSliders();
                if (value) {
                    this.addChromaSliders();
                }
            });
        }

        if (hexColorField == null) {
            hexColorField = new EditBox(MC.font, width / 2 + 110 - 50, 220, 100, 15, Component.empty());
            hexColorField.setMaxLength(7);
        }

        // Set the current color in the text box after creating it.
        setTextBoxHex(color.get());

        // This creates the 16 buttons for all the color codes.
        if (isRestricted) {
            int collumn = 1;
            int x = width / 2 - 160;
            int y = 120;

            for (ColorCode colorCode : ColorCode.values()) {
                if (!colorCode.isColor()) continue;

                addRenderableWidget(new ButtonColorBox(x, y, colorCode));

                if (collumn < 6) { // 6 buttons per row.
                    collumn++; // Go to the next collumn once the 6 are over.
                    x += ButtonColorBox.WIDTH + 15; // 15 spacing.
                } else {
                    y += ButtonColorBox.HEIGHT + 20; // Go to next row.
                    collumn = 1; // Reset the collumn.
                    x = width / 2 - 160; // Reset the x vlue.
                }
            }
        }

        if (isChroma && !isRestricted) {
            addChromaSliders();
        }
        addSocials(this::addRenderableWidget);
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw background and default text.
        drawGradientBackground(graphics, 128, 192);
        drawDefaultTitleText(graphics, this, 255);

        int defaultBlue = ColorUtils.getDefaultBlue(1);

        if (feature.isGuiFeature() || setting != null) {
            if (isRestricted) {
                drawScaledString(
                        graphics,
                        this,
                        Translations.getMessage("messages.chooseAColor"),
                        90,
                        defaultBlue,
                        1.5F,
                        0
                );
            } else {
                boolean isChroma = this.isChroma.get();
                int pickerWidth = COLOR_PICKER_IMAGE.getWidth();
                int pickerHeight = COLOR_PICKER_IMAGE.getHeight();

                imageX = width / 2 - 200;
                imageY = 90;

                // Fade out color picker if chroma enabled
                int color;
                if (isChroma) {
                    color = ARGB.colorFromFloat(0.7F, 0.5F, 0.5F, 0.5F);
                } else {
                    color = ARGB.white(1F);
                }

                graphics.blit(RenderType::guiTextured, COLOR_PICKER, imageX, imageY, 0, 0, pickerWidth, pickerHeight, pickerWidth, pickerHeight, color);

                drawScaledString(graphics, this, Translations.getMessage("messages.selectedColor"), 120, defaultBlue, 1.5F, 75);

                int currentColor = this.color.get();
                if (setting == null && feature.isChroma() && Feature.CHROMA_MODE.getValue() == ChromaMode.FADE) {
                    currentColor = ColorCode.CHROMA.getColor(); // alpha is default on here
                }
                ButtonColorBox.drawColorRect(graphics, width / 2 + 90, 140, width / 2 + 130, 160, currentColor);

                if (chromaCheckbox != null) chromaCheckbox.draw(graphics);

                if (!isChroma) { // Disabled cause chroma is enabled
                    drawScaledString(graphics, this, Translations.getMessage("messages.setHexColor"), 200, defaultBlue, 1.5F, 75);
                    hexColorField.renderWidget(graphics, mouseX, mouseY, partialTick);
                }

                if (isChroma) {
                    drawScaledString(graphics, this, Translations.getMessage("settings.chromaSpeed"), 170 + 25, defaultBlue, 1F, 110);
                    drawScaledString(graphics, this, Translations.getMessage("settings.chromaFadeWidth"), 170 + 35 + 25, defaultBlue, 1F, 110);
                }
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isRestricted && !this.isChroma.get()) {
            int xPixel = (int) mouseX - imageX;
            int yPixel = (int) mouseY - imageY;

            // If the mouse is over the color picker.
            if (xPixel > 0 && xPixel < COLOR_PICKER_IMAGE.getWidth()
                    && yPixel > 0 && yPixel < COLOR_PICKER_IMAGE.getHeight()) {

                // Get the color of the clicked pixel.
                int selectedColor = COLOR_PICKER_IMAGE.getPixel(xPixel, yPixel);

                // Choose this color.
                if (ARGB.alpha(selectedColor) == 255) {
                    setColor.accept(selectedColor);
                    setTextBoxHex(selectedColor);
                    Utils.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.25, 1);
                }
            }

            hexColorField.mouseClicked(mouseX, mouseY, button);
            hexColorField.setFocused(hexColorField.isHovered());
        }

        if (chromaCheckbox != null) chromaCheckbox.onMouseClick((int) mouseX, (int) mouseY, button);

        Optional<GuiEventListener> optional = this.getChildAt(mouseX, mouseY);
        if (optional.isEmpty()) {
            return false;
        } else {
            GuiEventListener guiEventListener = (GuiEventListener) optional.get();

            if (guiEventListener instanceof ButtonColorBox colorBox) {
                setChroma.accept(colorBox.getColor() == ColorCode.CHROMA);
                setColor.accept(colorBox.getColor().getColor());
                MC.setScreen(null);
            } else if (guiEventListener.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(guiEventListener);
                if (button == 0) {
                    this.setDragging(true);
                }
            }

            return true;
        }
    }

    private void setTextBoxHex(int color) {
        hexColorField.setValue(
                String.format(
                        "#%02x%02x%02x",
                        ARGB.red(color),
                        ARGB.green(color),
                        ARGB.blue(color)
                )
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (hexColorField.isFocused()) {
            hexColorField.keyPressed(keyCode, scanCode, modifiers);
        } else {
            hexColorField.setFocused(true);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (hexColorField.isFocused()) {
            hexColorField.charTyped(codePoint, modifiers);
            String text = hexColorField.getValue();
            if (text.startsWith("#")) { // Get rid of the #.
                text = text.substring(1);
            }

            if (text.length() == 6) {
                int typedColor;
                try {
                    // Try to read the hex value and put it in an integer.
                    typedColor = (0xFF << 24) | Integer.parseInt(text, 16);
                } catch (NumberFormatException ex) {
                    // This just means it wasn't in the format of a hex number that's fine!
                    ex.printStackTrace();
                    return false;
                }
                setColor.accept(typedColor);
            }
        } else {
            hexColorField.setFocused(true);
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void removed() {
        main.getRenderListener().setGuiToOpen(lastGUI, lastPage, lastTab, feature);
    }

    private void removeChromaSliders() {
        children().removeIf(guiEventListener -> guiEventListener instanceof ButtonSlider);
        renderables.removeIf(renderable -> renderable instanceof ButtonSlider);
    }

    private void addChromaSliders() {
        addRenderableWidget(new ButtonSlider((double) width / 2 + 76,
                170 + 35,
                70,
                15,
                Feature.CHROMA_SPEED.numberValue().floatValue(),
                0.5F,
                20,
                0.5F,
                Feature.CHROMA_SPEED::setValue
        ));
        addRenderableWidget(new ButtonSlider(
                (double) width / 2 + 76,
                170 + 35+ 35,
                70,
                15,
                Feature.CHROMA_SIZE.numberValue().floatValue(),
                1,
                100,
                1,
                Feature.CHROMA_SIZE::setValue
        ));
    }

    private static NativeImage loadColorPicker() {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(COLOR_PICKER);
        if (resource.isPresent()) {
            try {
                return COLOR_PICKER_IMAGE = NativeImage.read(resource.get().open());
            } catch (IOException e) {
                SkyblockAddons.getLogger().catching(e);
            }
        }
        return null;
    }
}