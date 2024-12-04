package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.core.Feature;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class ButtonSocial extends SkyblockAddonsButton {

    @Getter private final EnumUtils.Social social;

    // Used to calculate the transparency when fading in.
    private final long timeOpened = System.currentTimeMillis();

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonSocial(double x, double y, EnumUtils.Social social) {
        super(0, (int)x, (int)y, "");
        this.width = 20;
        this.height = 20;
        this.social = social;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        float alphaMultiplier = getAlphaMultiplier(timeOpened);
        hovered = isHovered(mouseX, mouseY);
        GlStateManager.enableBlend();
        GlStateManager.color(1,1,1,alphaMultiplier * (hovered ? 1F : 0.7F));
        mc.getTextureManager().bindTexture(social.getResourceLocation());
        DrawUtils.drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0, width, height, width, height, true);
    }

}
