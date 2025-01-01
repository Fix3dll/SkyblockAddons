package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.gui.screens.ColorSelectionGui;
import codes.biscuit.skyblockaddons.gui.screens.LocationEditGui;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonColorWheel extends ButtonFeature {

    private static final ResourceLocation COLOR_WHEEL = new ResourceLocation("skyblockaddons", "gui/colorwheel.png");
    public static final int SIZE = 10;

    public float x;
    public float y;
    
    public ButtonColorWheel(float x, float y, Feature feature) {
        super(0, 0, 0, "", feature);
        width = SIZE;
        height = SIZE;

        this.x = x;
        this.y = y;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        float scale = SkyblockAddons.getInstance().getConfigValues().getGuiScale(feature);
        this.hovered = isHovered(x, y, mouseX, mouseY, scale);
        GlStateManager.enableBlend();
        GlStateManager.color(1,1,1, hovered ? 1 : 0.5F);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale,scale,1);
        mc.getTextureManager().bindTexture(COLOR_WHEEL);
        DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 10, 10, 10, 10, true);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered) {
            if (mc.currentScreen instanceof LocationEditGui) {
                LocationEditGui gui = (LocationEditGui) mc.currentScreen;
                gui.setClosing(true);
                mc.displayGuiScreen(new ColorSelectionGui(feature, EnumUtils.GUIType.EDIT_LOCATIONS, gui.getLastTab(), gui.getLastPage()));
                return true;
            }
        }
        return false;
    }
}
