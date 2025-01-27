package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.gui.screens.EnchantmentSettingsGui;
import codes.biscuit.skyblockaddons.gui.screens.SettingsGui;
import codes.biscuit.skyblockaddons.gui.screens.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonArrow extends SkyblockAddonsButton {

    @Getter private final ArrowType arrowType;
    private final boolean max;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonArrow(double x, double y, ArrowType arrowType, boolean max) {
        super(0, (int)x, (int)y, null);
        this.width = 30;
        this.height = 30;
        this.arrowType = arrowType;
        this.max = max;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            float alphaMultiplier = calculateAlphaMultiplier();
            hovered = isHovered(mouseX, mouseY);
            // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
            // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
            GlStateManager.enableBlend();
            mc.getTextureManager().bindTexture(arrowType.resourceLocation);
            if (max) {
                GlStateManager.color(0.5F, 0.5F, 0.5F, alphaMultiplier * 0.5F);
            } else {
                GlStateManager.color(1F, 1F, 1F, hovered ? 1F : alphaMultiplier * 0.7F);
            }
            drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height);
            GlStateManager.disableBlend();
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered && !this.max) {
            main.getUtils().setFadingIn(false);

            if (mc.currentScreen instanceof SkyblockAddonsGui) {
                SkyblockAddonsGui gui = (SkyblockAddonsGui) mc.currentScreen;
                if (gui.getTab() == EnumUtils.GuiTab.GENERAL_SETTINGS) gui.setCancelClose(true);

                int page = gui.getPage() + (arrowType == ArrowType.LEFT ? -1 : +1);
                mc.displayGuiScreen(new SkyblockAddonsGui(page, gui.getTab()));

                if (gui.getTab() == EnumUtils.GuiTab.GENERAL_SETTINGS) gui.setCancelClose(false);
                return true;

            } else if (mc.currentScreen instanceof EnchantmentSettingsGui) {
                EnchantmentSettingsGui gui = (EnchantmentSettingsGui) mc.currentScreen;
                gui.setClosingGui(true);

                int page = gui.getPage() + (arrowType == ArrowType.LEFT ? -1 : +1);
                mc.displayGuiScreen(new EnchantmentSettingsGui(page, gui.getLastPage(), gui.getLastTab()));
                return true;
            } else if (mc.currentScreen instanceof SettingsGui) {
                SettingsGui gui = (SettingsGui) mc.currentScreen;
                gui.setClosingGui(true);

                int page = gui.getPage() + (arrowType == ArrowType.LEFT ? -1 : +1);
                mc.displayGuiScreen(new SettingsGui(gui.getFeature(), page, gui.getLastPage(), gui.getLastTab(), gui.getLastGUI()));
                return true;

            }
        }
        return false;
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (!max) {
            super.playPressSound(soundHandlerIn);
        }
    }

    public enum ArrowType {
        LEFT("gui/arrowleft.png"),
        RIGHT("gui/arrowright.png");

        final ResourceLocation resourceLocation;

        ArrowType(String path) {
            this.resourceLocation = new ResourceLocation("skyblockaddons", path);
        }
    }
}
