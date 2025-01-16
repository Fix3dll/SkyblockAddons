package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.gui.screens.EnchantmentSettingsGui;
import codes.biscuit.skyblockaddons.gui.screens.SettingsGui;
import codes.biscuit.skyblockaddons.gui.screens.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonSettings extends ButtonFeature {

    private static final ResourceLocation GEAR = new ResourceLocation("skyblockaddons", "gui/gear.png");

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonSettings(double x, double y, Feature feature) {
        super(0, (int)x, (int)y, "", feature);
        this.feature = feature;
        this.width = 15;
        this.height = 15;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            float alphaMultiplier = calculateAlphaMultiplier();
            hovered = isHovered(mouseX, mouseY);
            // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
            // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
            GlStateManager.color(1, 1, 1, hovered ? 1 : alphaMultiplier * 0.7F);
            mc.getTextureManager().bindTexture(GEAR);
            DrawUtils.drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0, width, height, width, height, true);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered && mc.currentScreen instanceof SkyblockAddonsGui) {
            SkyblockAddonsGui gui = (SkyblockAddonsGui) mc.currentScreen;
            main.getUtils().setFadingIn(false);
            if (this.feature == Feature.ENCHANTMENT_LORE_PARSING) {
                mc.displayGuiScreen(new EnchantmentSettingsGui(0, gui.getPage(), gui.getTab()));
            } else {
                mc.displayGuiScreen(new SettingsGui(feature, 1, gui.getPage(), gui.getTab()));
            }
            return true;
        }
        return false;
    }
}
