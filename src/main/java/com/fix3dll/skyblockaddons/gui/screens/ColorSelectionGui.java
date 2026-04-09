package com.fix3dll.skyblockaddons.gui.screens;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.gui.buttons.ButtonSlider;
import com.fix3dll.skyblockaddons.gui.elements.CheckBox;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A production-ready GUI for color selection featuring a dynamic HSB (Hue, Saturation, Brightness) picker.
 * Allows users to select colors via visual gradients, hexadecimal input, or toggle Chroma (Rainbow) mode.
 */
public class ColorSelectionGui extends SkyblockAddonsScreen {

    // Feature and setting references
    private final Feature feature;
    private final FeatureSetting setting;

    // Navigation state for returning to the previous menu
    private final EnumUtils.GUIType lastGUI;
    private final EnumUtils.GuiTab lastTab;
    private final int lastPage;

    // Dynamic data accessors mapped to either a Feature or a FeatureSetting
    private final Supplier<Boolean> isChroma;
    private final Supplier<Integer> color;
    private final Consumer<Boolean> setChroma;
    private final Consumer<Integer> setColor;

    // HSB state (Values range from 0.0 to 1.0)
    private float hue, saturation, brightness;

    // UI Coordinates and Dimensions
    private int pickerX, pickerY;
    private final int pickerSize = 120;
    private int hueX, hueY;
    private final int hueWidth = 15;

    // Interaction states
    private boolean isDraggingPicker, isDraggingHue;
    private EditBox hexColorField;
    private boolean hexColorFieldHovered;
    private CheckBox chromaCheckbox;

    /**
     * Creates a GUI to allow the user to select a color for a primary Feature.
     * @param feature  The primary feature that this color applies to.
     * @param lastGUI  The previous GUI screen type.
     * @param lastTab  The previous tab the user came from.
     * @param lastPage The previous page number.
     */
    public ColorSelectionGui(Feature feature, EnumUtils.GUIType lastGUI, EnumUtils.GuiTab lastTab, int lastPage) {
        super(Component.empty());
        this.feature = feature;
        this.setting = null;
        this.lastTab = lastTab;
        this.lastGUI = lastGUI;
        this.lastPage = lastPage;

        // Bind primary feature handlers
        this.isChroma = feature::isChroma;
        this.color = feature::getColor;
        this.setColor = feature::setColor;
        this.setChroma = feature::setChroma;

        // Initialize the HSB picker cursors based on the current color
        updateHSBFromColor(this.color.get());
    }

    /**
     * Creates a GUI to allow the user to select a color for a specific sub-setting of a feature.
     * @param setting  The specific sub-setting that this color applies to.
     * @param lastGUI  The previous GUI screen type.
     * @param lastTab  The previous tab the user came from.
     * @param lastPage The previous page number.
     * @throws IllegalArgumentException if the setting has no related feature or invalid value type.
     */
    public ColorSelectionGui(FeatureSetting setting, EnumUtils.GUIType lastGUI, EnumUtils.GuiTab lastTab, int lastPage) {
        super(Component.empty());
        this.feature = setting.getRelatedFeature();
        this.setting = setting;
        this.lastTab = lastTab;
        this.lastGUI = lastGUI;
        this.lastPage = lastPage;

        if (feature == null) {
            throw new IllegalArgumentException("Feature cannot be null for setting color selection.");
        }

        Object settingValue = feature.get(setting);
        if (!(settingValue instanceof ColorCode || settingValue instanceof Number)) {
            throw new IllegalArgumentException("Setting value must be a ColorCode or a Number instance.");
        }

        // Bind setting-specific handlers
        this.isChroma = () -> feature.getAsNumber(setting).intValue() == ColorCode.CHROMA.getColor();
        this.color = () -> feature.getAsNumber(setting).intValue();
        this.setColor = integer -> feature.set(setting, integer);
        this.setChroma = setChroma -> {
            if (setChroma) {
                feature.set(setting, ColorCode.CHROMA.getColor());
            } else {
                main.getConfigValuesManager().setSettingToDefault(setting);
            }
        };

        // Initialize the HSB picker cursors based on the current color
        updateHSBFromColor(this.color.get());
    }

    @Override
    public void init() {
        super.init();

        // Picker Positioning (Left Side)
        this.pickerX = width / 2 - 200;
        this.pickerY = 120;
        this.hueX = pickerX + pickerSize + 15;
        this.hueY = pickerY;

        boolean isChroma = this.isChroma.get();

        // Initialize Chroma Checkbox
        if (chromaCheckbox == null) {
            chromaCheckbox = new CheckBox(width / 2 + 88, 170, 12, Translations.getMessage("messages.chroma"), false);
            chromaCheckbox.setValue(isChroma);
            chromaCheckbox.setOnToggleListener(value -> {
                setChroma.accept(value);
                this.removeChromaSliders();
                if (value) this.addChromaSliders();
            });
        } else {
            chromaCheckbox.setX(width / 2 + 88);
        }

        // Initialize Hexadecimal Input Field
        if (hexColorField == null) {
            hexColorField = new EditBox(MC.font, width / 2 + 110 - 50, 220, 100, 15, Component.empty());
            hexColorField.setMaxLength(7); // Max length 7 to accommodate the '#' and 6 hex digits
        } else {
            hexColorField.setX(width / 2 + 110 - 50);
        }

        setTextBoxHex(color.get());
        if (isChroma) addChromaSliders();
        addSocials(this::addRenderableWidget);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw background and default UI components
        drawGradientBackground(graphics, 128, 192);
        drawDefaultTitleText(graphics, mouseX, mouseY, partialTick, this, 255);

        int defaultBlue = ColorUtils.getDefaultBlue(255);
        boolean isChromaVal = this.isChroma.get();

        // 1. RENDER DYNAMIC COLOR PICKER (Left Side)
        drawHSBPicker(graphics, isChromaVal);

        // 2. RENDER SELECTED COLOR PREVIEW AND CONTROLS (Right Side)
        drawScaledString(graphics, this, Translations.getMessage("messages.selectedColor"), 120, defaultBlue, 1.5F, 75);

        int currentColor = isChromaVal ? ColorCode.CHROMA.getColor() : this.color.get();
        DrawUtils.drawColorRect(graphics, width / 2 + 90, 140, width / 2 + 130, 160, currentColor);

        if (chromaCheckbox != null) chromaCheckbox.draw(graphics);

        if (isChromaVal) {
            // Render Chroma-specific sliders and labels
            drawScaledString(graphics, this, Translations.getMessage("settings.chromaSpeed"), 170 + 25, defaultBlue, 1F, 110);
            drawScaledString(graphics, this, Translations.getMessage("settings.chromaFadeWidth"), 170 + 35 + 25, defaultBlue, 1F, 110);
        } else {
            // Render Hex field and its label
            drawScaledString(graphics, this, Translations.getMessage("messages.setHexColor"), 200, defaultBlue, 1.5F, 75);
            hexColorField.renderWidget(graphics, mouseX, mouseY, partialTick);

            // Check hover state for proper text field focusing
            hexColorFieldHovered = mouseX >= hexColorField.getX() && mouseX < hexColorField.getX() + hexColorField.getWidth()
                    && mouseY >= hexColorField.getY() && mouseY < hexColorField.getY() + hexColorField.getHeight();
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * Handles the rendering of the Hue slider and Saturation/Brightness square using procedural gradients.
     * @param graphics The GUI graphics extractor instance.
     * @param disabled Whether the picker should be visually dimmed (e.g., when Chroma is active).
     */
    private void drawHSBPicker(GuiGraphics graphics, boolean disabled) {
        int alpha = disabled ? 100 : 255;

        // Draw Hue Strip (Vertical spectrum)
        for (int i = 0; i < pickerSize; i++) {
            float f = (float) i / pickerSize;
            int c = Color.HSBtoRGB(f, 1f, 1f);
            graphics.fill(hueX, hueY + i, hueX + hueWidth, hueY + i + 1, ARGB.color(alpha, ARGB.red(c), ARGB.green(c), ARGB.blue(c)));
        }

        // Draw Saturation Base (Horizontal: White to pure Hue color)
        // Since standard fillGradient is vertical, we draw horizontal saturation pixel-column by pixel-column
        for (int i = 0; i < pickerSize; i++) {
            float s = (float) i / pickerSize;
            int columnColor = Color.HSBtoRGB(hue, s, 1f);
            graphics.fill(pickerX + i, pickerY, pickerX + i + 1, pickerY + pickerSize, ARGB.color(alpha, ARGB.red(columnColor), ARGB.green(columnColor), ARGB.blue(columnColor)));
        }

        // Draw Brightness Overlay (Vertical: Transparent to Black)
        graphics.fillGradient(pickerX, pickerY, pickerX + pickerSize, pickerY + pickerSize,
                ARGB.color(0, 0, 0, 0), ARGB.color(alpha, 0, 0, 0));

        // Draw interaction cursors
        if (!disabled) {
            // Hue cursor (Horizontal White Outline)
            int hueCursorY = hueY + (int) (hue * pickerSize);
            graphics.submitOutline(hueX - 2, hueCursorY - 1, hueWidth + 4, 3, ColorCode.WHITE.getColor());

            // Main picker cursor (Hollow square)
            int cursorX = pickerX + (int)(saturation * pickerSize);
            int cursorY = pickerY + (int)((1 - brightness) * pickerSize);
            graphics.submitOutline(cursorX - 2, cursorY - 2, 4, 4, ColorCode.WHITE.getColor());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        double mx = event.x();
        double my = event.y();

        if (!this.isChroma.get()) {
            // Detect click on the Hue Bar
            if (mx >= hueX && mx <= hueX + hueWidth && my >= hueY && my <= hueY + pickerSize) {
                isDraggingHue = true;
                updateFromMouse(mx, my);
                return true;
            }
            // Detect click on the Saturation/Brightness Square
            if (mx >= pickerX && mx <= pickerX + pickerSize && my >= pickerY && my <= pickerY + pickerSize) {
                isDraggingPicker = true;
                updateFromMouse(mx, my);
                return true;
            }

            // Pass click event to the hex field
            hexColorField.mouseClicked(event, isDoubleClick);
            hexColorField.setFocused(hexColorFieldHovered);
        }

        if (chromaCheckbox != null) chromaCheckbox.onMouseClick(event, isDoubleClick);
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        // Reset dragging states upon mouse release
        isDraggingHue = false;
        isDraggingPicker = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        // Handle smooth color updating while dragging
        if (isDraggingHue || isDraggingPicker) {
            updateFromMouse(event.x(), event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    /**
     * Synchronizes the internal HSB values based on mouse coordinates and updates the underlying feature color.
     */
    private void updateFromMouse(double mx, double my) {
        if (isDraggingHue) {
            hue = (float) Math.clamp((my - hueY) / pickerSize, 0, 1);
        } else if (isDraggingPicker) {
            saturation = (float) Math.clamp((mx - pickerX) / pickerSize, 0, 1);
            brightness = 1.0F - (float) Math.clamp((my - pickerY) / pickerSize, 0, 1);
        }

        int newColor = Color.HSBtoRGB(hue, saturation, brightness);
        // Ensure Alpha channel is forced to 255 (fully opaque)
        setColor.accept(ARGB.color(255, ARGB.red(newColor), ARGB.green(newColor), ARGB.blue(newColor)));
        setTextBoxHex(newColor);
    }

    /**
     * Synchronizes the internal HSB variables from a given ARGB integer.
     */
    private void updateHSBFromColor(int color) {
        float[] hsb = Color.RGBtoHSB(ARGB.red(color), ARGB.green(color), ARGB.blue(color), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    /**
     * Formats and sets the current ARGB value into the hexadecimal input field.
     */
    private void setTextBoxHex(int color) {
        hexColorField.setValue(String.format("#%02x%02x%02x", ARGB.red(color), ARGB.green(color), ARGB.blue(color)));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (hexColorField.isFocused()) {
            boolean handled = hexColorField.keyPressed(event);
            enforceHexFormat();
            if (handled) {
                parseColor();
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (hexColorField.isFocused()) {
            boolean handled = hexColorField.charTyped(event);
            enforceHexFormat();
            if (handled) {
                parseColor();
                return true;
            }
        }
        return super.charTyped(event);
    }

    /**
     * Enforces strict hex formatting: exactly one '#' prefix and up to 6 valid hex characters.
     */
    private void enforceHexFormat() {
        String currentText = hexColorField.getValue();

        StringBuilder builder = new StringBuilder("#");
        for (int i = 0; i < currentText.length(); i++) {
            char c = currentText.charAt(i);
            if (isHexChar(c)) {
                builder.append(c);
                // Stop appending once we have 6 valid hex chars (1 prefix + 6 chars = 7 total)
                if (builder.length() == 7) break;
            }
        }

        String expectedText = builder.toString();

        if (!currentText.equals(expectedText)) {
            hexColorField.setValue(expectedText);

            if (expectedText.equals("#")) {
                hexColorField.setCursorPosition(1);
                hexColorField.setHighlightPos(1);
            }
        }
    }

    private boolean isHexChar(char c) {
        return switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                 'a', 'b', 'c', 'd', 'e', 'f',
                 'A', 'B', 'C', 'D', 'E', 'F' -> true;
            default -> false;
        };
    }

    /**
     * Attempts to parse the hex string from the edit box, update the internal color state,
     * and reposition the HSB cursors dynamically.
     */
    private void parseColor() {
        String text = hexColorField.getValue().replace("#", "");
        if (text.length() == 6) {
            try {
                int typedColor = Integer.parseInt(text, 16);
                int finalColor = ARGB.color(255, (typedColor >> 16) & 0xFF, (typedColor >> 8) & 0xFF, typedColor & 0xFF);
                setColor.accept(finalColor);
                updateHSBFromColor(finalColor);
            } catch (NumberFormatException ignored) {
                // Silently ignore invalid inputs until a valid hex code is completed
            }
        }
    }

    /**
     * Removes the Chroma configuration sliders from the UI.
     */
    private void removeChromaSliders() {
        children().removeIf(guiEventListener -> guiEventListener instanceof ButtonSlider);
        renderables.removeIf(renderable -> renderable instanceof ButtonSlider);
    }

    /**
     * Injects the Chroma configuration sliders into the UI underneath the checkbox.
     */
    private void addChromaSliders() {
        addRenderableWidget(new ButtonSlider(
                (double) width / 2 + 76, 170 + 35, 70, 15,
                Feature.CHROMA_SPEED.numberValue().floatValue(), 0.5F, 20, 0.5F,
                Feature.CHROMA_SPEED::setValue
        ));
        addRenderableWidget(new ButtonSlider(
                (double) width / 2 + 76, 170 + 70, 70, 15,
                Feature.CHROMA_SIZE.numberValue().floatValue(), 1, 100, 1,
                Feature.CHROMA_SIZE::setValue
        ));
    }

    @Override
    public void removed() {
        // Return the user safely to the prior configuration page
        main.getRenderListener().setGuiToOpen(lastGUI, lastPage, lastTab, feature);
    }

}