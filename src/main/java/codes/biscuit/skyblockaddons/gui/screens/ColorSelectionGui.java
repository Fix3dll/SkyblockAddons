package codes.biscuit.skyblockaddons.gui.screens;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonColorBox;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSlider;
import codes.biscuit.skyblockaddons.gui.elements.CheckBox;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ColorSelectionGui extends SkyblockAddonsScreen {

    private static final ResourceLocation COLOR_PICKER = new ResourceLocation("skyblockaddons", "gui/colorpicker.png");
    private static BufferedImage COLOR_PICKER_IMAGE;

    // The feature that this color is for.
    private final Feature feature;

    // Previous pages for when they return.
    private final EnumUtils.GUIType lastGUI;
    private final EnumUtils.GuiTab lastTab;
    private final int lastPage;

    private int imageX;
    private int imageY;

    private GuiTextField hexColorField;

    private CheckBox chromaCheckbox;

    static {
        try {
            COLOR_PICKER_IMAGE = TextureUtil.readBufferedImage(
                    Minecraft.getMinecraft().getResourceManager().getResource(COLOR_PICKER).getInputStream()
            );
        } catch (IOException e) {
            SkyblockAddons.getLogger().catching(e);
        }
    }

    /**
     * Creates a gui to allow you to select a color for a specific feature.
     *
     * @param feature The feature that this color is for.
     * @param lastTab The previous tab that you came from.
     * @param lastPage The previous page.
     */
    public ColorSelectionGui(Feature feature, EnumUtils.GUIType lastGUI, EnumUtils.GuiTab lastTab, int lastPage) {
        this.feature = feature;
        this.lastTab = lastTab;
        this.lastGUI = lastGUI;
        this.lastPage = lastPage;
    }

    @Override
    public void initGui() {
        chromaCheckbox = new CheckBox(width / 2 + 88, 170, 12, Translations.getMessage("messages.chroma"), false);
        chromaCheckbox.setValue(feature.isInChromaFeatures());
        chromaCheckbox.setOnToggleListener(value -> {
            main.getConfigValues().setChroma(feature, value);
            ColorSelectionGui.this.removeChromaSliders();
            if (value) {
                ColorSelectionGui.this.addChromaSliders();
            }
        });

        hexColorField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, width/2+110-50, 220, 100, 15);
        hexColorField.setMaxStringLength(7);
        hexColorField.setFocused(true);

        // Set the current color in the text box after creating it.
        setTextBoxHex(main.getConfigValues().getColor(feature));

        boolean restrictedColor = feature.getGuiFeatureData() != null && feature.getGuiFeatureData().isColorsRestricted();

        // This creates the 16 buttons for all the color codes.
        if (restrictedColor) {
            int collumn = 1;
            int x = width / 2 - 160;
            int y = 120;

            for (ColorCode colorCode : ColorCode.values()) {
                if (!colorCode.isColor()) continue;

                buttonList.add(new ButtonColorBox(x, y, colorCode));

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

        if (feature.isInChromaFeatures() && !restrictedColor) {
            addChromaSliders();
        }

        addSocials();

        Keyboard.enableRepeatEvents(true);

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableBlend();

        // Draw background and default text.
        drawGradientBackground(128, 192);
        drawDefaultTitleText(this, 255);

        int defaultBlue = ColorUtils.getDefaultBlue(1);

        if (feature.getGuiFeatureData() != null) {
            if (feature.getGuiFeatureData().isColorsRestricted()) {
                SkyblockAddonsGui.drawScaledString(
                        this,
                        Translations.getMessage("messages.chooseAColor"),
                        90,
                        defaultBlue,
                        1.5,
                        0
                );
            } else {
                int pickerWidth = COLOR_PICKER_IMAGE.getWidth();
                int pickerHeight = COLOR_PICKER_IMAGE.getHeight();

                imageX = width / 2 - 200;
                imageY = 90;

                // Fade out color picker if chroma enabled
                if (feature.isInChromaFeatures()) {
                    GlStateManager.color(0.5F, 0.5F, 0.5F, 0.7F);
                    GlStateManager.enableBlend();
                } else {
                    GlStateManager.color(1, 1, 1, 1);
                }

                // Draw the color picker with no scaling so the size is the exact same.
                mc.getTextureManager().bindTexture(COLOR_PICKER);
                Gui.drawModalRectWithCustomSizedTexture(imageX, imageY, 0, 0, pickerWidth, pickerHeight, pickerWidth, pickerHeight);

                SkyblockAddonsGui.drawScaledString(this, Translations.getMessage("messages.selectedColor"), 120, defaultBlue, 1.5, 75);
                drawRect(width / 2 + 90, 140, width / 2 + 130, 160, main.getConfigValues().getColor(feature));

                if (chromaCheckbox != null) chromaCheckbox.draw();

                if (!feature.isInChromaFeatures()) { // Disabled cause chroma is enabled
                    SkyblockAddonsGui.drawScaledString(this, Translations.getMessage("messages.setHexColor"), 200, defaultBlue, 1.5, 75);
                    hexColorField.drawTextBox();
                }

                if (feature.isInChromaFeatures()) {
                    SkyblockAddonsGui.drawScaledString(this, Translations.getMessage("settings.chromaSpeed"), 170 + 25, defaultBlue, 1, 110);
                    SkyblockAddonsGui.drawScaledString(this, Translations.getMessage("settings.chromaFadeWidth"), 170 + 35 + 25, defaultBlue, 1, 110);
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.disableBlend();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (feature.getGuiFeatureData() != null && !feature.getGuiFeatureData().isColorsRestricted() && !feature.isInChromaFeatures()) {
            int xPixel = mouseX - imageX;
            int yPixel = mouseY - imageY;

            // If the mouse is over the color picker.
            if (xPixel > 0 && xPixel < COLOR_PICKER_IMAGE.getWidth() &&
                    yPixel > 0 && yPixel < COLOR_PICKER_IMAGE.getHeight()) {

                // Get the color of the clicked pixel.
                int selectedColor = COLOR_PICKER_IMAGE.getRGB(xPixel, yPixel);

                // Choose this color.
                if (ColorUtils.getAlpha(selectedColor) == 255) {
                    main.getConfigValues().setColor(feature, selectedColor);
                    setTextBoxHex(selectedColor);

                    main.getUtils().playSound("gui.button.press", 0.25, 1);
                }
            }

            hexColorField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (chromaCheckbox != null) chromaCheckbox.onMouseClick(mouseX, mouseY, mouseButton);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void setTextBoxHex(int color) {
        hexColorField.setText(
                String.format(
                        "#%02x%02x%02x",
                        ColorUtils.getRed(color),
                        ColorUtils.getGreen(color),
                        ColorUtils.getBlue(color)
                )
        );
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (hexColorField.isFocused()) {
            hexColorField.textboxKeyTyped(typedChar, keyCode);

            String text = hexColorField.getText();
            if (text.startsWith("#")) { // Get rid of the #.
                text = text.substring(1);
            }

            if (text.length() == 6) {
                int typedColor;
                try {
                    typedColor = Integer.parseInt(text, 16); // Try to read the hex value and put it in an integer.
                } catch (NumberFormatException ex) {
                    ex.printStackTrace(); // This just means it wasn't in the format of a hex number- that's fine!
                    return;
                }

                main.getConfigValues().setColor(feature, typedColor);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof ButtonColorBox) {
            ButtonColorBox colorBox = (ButtonColorBox)button;
            main.getConfigValues().setChroma(feature, colorBox.getColor() == ColorCode.CHROMA);
            main.getConfigValues().setColor(feature, colorBox.getColor().getColor());
            this.mc.displayGuiScreen(null);
        }

        super.actionPerformed(button);
    }

    @Override
    public void updateScreen() {
        hexColorField.updateCursorCounter();

        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        // Hardcode until feature refactor...
        if (feature == Feature.ENCHANTMENT_PERFECT_COLOR || feature == Feature.ENCHANTMENT_GREAT_COLOR ||
                feature == Feature.ENCHANTMENT_GOOD_COLOR || feature == Feature.ENCHANTMENT_POOR_COLOR ||
                feature == Feature.ENCHANTMENT_COMMA_COLOR) {
            main.getRenderListener().setGuiToOpen(lastGUI, lastPage, lastTab, Feature.ENCHANTMENT_LORE_PARSING);
        } else {
            main.getRenderListener().setGuiToOpen(lastGUI, lastPage, lastTab, feature);
        }
    }

    private void removeChromaSliders() {
        this.buttonList.removeIf(button -> button instanceof ButtonSlider);
    }

    private void addChromaSliders() {
        buttonList.add(new ButtonSlider((double) width / 2 + 76,
                170 + 35,
                70,
                15,
                main.getConfigValues().getChromaSpeed().floatValue(),
                0.5F,
                20,
                0.5F,
                updatedValue -> main.getConfigValues().getChromaSpeed().setValue(updatedValue)
        ));
        buttonList.add(new ButtonSlider(
                (double) width / 2 + 76,
                170 + 35+ 35,
                70,
                15,
                main.getConfigValues().getChromaSize().floatValue(),
                1,
                100,
                1,
                updatedValue -> main.getConfigValues().getChromaSize().setValue(updatedValue)
        ));
    }
}