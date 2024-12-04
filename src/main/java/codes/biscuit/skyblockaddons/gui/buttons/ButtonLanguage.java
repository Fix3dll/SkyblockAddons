package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

public class ButtonLanguage extends SkyblockAddonsButton {
    private static final Logger logger = SkyblockAddons.getLogger();

    @Getter private final Language language;
    private final String languageName;
    private boolean flagResourceExceptionTriggered;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonLanguage(double x, double y, String buttonText, Language language) {
        super(0, (int)x, (int)y, buttonText);
        this.language = language;
        DataUtils.loadLocalizedStrings(language, false);
        this.languageName = Translations.getMessage("language");
        this.width = 140;
        this.height = 25;
        flagResourceExceptionTriggered = false;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            DrawUtils.drawRect(xPosition, yPosition, width, height,  ColorUtils.getDummySkyblockColor(28, 29, 41, 230), 4);

            GlStateManager.color(1,1,1,1F);
            try {
                mc.getTextureManager().bindTexture(language.getResourceLocation());
                DrawUtils.drawModalRectWithCustomSizedTexture(xPosition+width-32, yPosition, 0, 0, 30, 26, 30, 26, true);
            } catch (Exception ex) {
                if (!flagResourceExceptionTriggered) {
                    flagResourceExceptionTriggered = true;
                    logger.catching(ex);
                }
            }
            hovered = isHovered(mouseX, mouseY);
            drawString(
                    mc.fontRendererObj,
                    languageName,
                    xPosition + 5,
                    yPosition+10,
                    hovered ? new Color(255, 255, 160, 255).getRGB()
                            : main.getUtils().getDefaultBlue(255)
            );
        }
    }

}
