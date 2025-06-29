package com.fix3dll.skyblockaddons.gui.buttons;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.Language;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.gui.screens.SettingsGui;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.apache.logging.log4j.Logger;

public class ButtonLanguage extends SkyblockAddonsButton {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    @Getter private final Language language;
    private final String languageName;
    private boolean flagResourceExceptionTriggered;

    /**
     * Create a button for toggling a feature on or off. This includes all the Features that have a proper ID.
     */
    public ButtonLanguage(double x, double y, String buttonText, Language language) {
        super((int) x, (int) y, Component.literal(buttonText));
        this.language = language;
        DataUtils.loadLocalizedStrings(language, false);
        this.languageName = Translations.getMessage("language");
        this.width = 140;
        this.height = 25;
        flagResourceExceptionTriggered = false;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = isHovered(mouseX, mouseY);

        DrawUtils.drawRoundedRect(graphics, getX(), getY(), width, height, 4, ARGB.color(230, 28, 29, 41));
//            DrawUtils.drawRect(xPosition, yPosition, width, height,  ColorUtils.getDummySkyblockColor(28, 29, 41, 230), 4);

        int color = ARGB.white(1F);
        try {
            graphics.blit(RenderType::guiTextured, language.getIdentifier(), getX() + width - 32, getY(), 0, 0, 30, 26, 30, 26, color);
//                DrawUtils.drawModalRectWithCustomSizedTexture(xPosition+width-32, yPosition, 0, 0, 30, 26, 30, 26, true);
        } catch (Exception ex) {
            if (!flagResourceExceptionTriggered) {
                flagResourceExceptionTriggered = true;
                LOGGER.catching(ex);
            }
        }
        color = this.isHovered ? ARGB.color(255, 255, 255, 160) : ColorUtils.getDefaultBlue(255);
        graphics.drawString(MC.font, languageName, getX() + 5, getY() + 10, color, true);
//            drawString(
//                    mc.fontRendererObj,
//                    languageName,
//                    xPosition + 5,
//                    yPosition+10,
//                    hovered ? new Color(255, 255, 160, 255).getRGB()
//                            : main.getUtils().getDefaultBlue(255)
//            );
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.isHovered) {
            if (MC.screen instanceof SettingsGui gui) {
                DataUtils.loadLocalizedStrings(this.language, true);
                gui.setClosingGui(true);
                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, gui.getLastPage(), gui.getLastTab());
            }
        }
    }
}