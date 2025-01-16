package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Island;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.features.ItemDropChecker;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.core.npc.NPCUtils;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackColor;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackInventoryManager;
import codes.biscuit.skyblockaddons.gui.screens.IslandWarpGui;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//TODO Fix for Hypixel localization
public class GuiChestHook {
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final FontRenderer fontRenderer = mc.fontRendererObj;

    /** Strings for reforge filter */
    private static final String TYPE_TO_MATCH = Translations.getMessage("messages.reforges");
    private static final String TYPE_ENCHANTMENTS = Translations.getMessage("messages.typeEnchantmentsHere", TYPE_TO_MATCH);
    private static final String SEPARATE_MULTIPLE = Translations.getMessage("messages.separateMultiple");
    private static final String ENCHANTS_TO_INCLUDE = Translations.getMessage("messages.enchantsToMatch", TYPE_TO_MATCH);
    private static final String INCLUSION_EXAMPLE = Translations.getMessage("messages.reforgeInclusionExample");
    private static final String ENCHANTS_TO_EXCLUDE = Translations.getMessage("messages.enchantsToExclude", TYPE_TO_MATCH);
    private static final String EXCLUSION_EXAMPLE = Translations.getMessage("messages.reforgeExclusionExample");

    private static final int REFORGE_MENU_HEIGHT = 222 - 108 + 5 * 18;

    @Getter
    private static IslandWarpGui islandWarpGui = null;

    /** Reforge filter text field for reforges to match */
    private static GuiTextField textFieldMatches = null;
    /** Reforge filter text field for reforges to exclude */
    private static GuiTextField textFieldExclusions = null;

    private static int reforgeFilterHeight;

    /** String dimensions for reforge filter */
    private static int maxStringWidth;
    private static int typeEnchantmentsHeight;
    private static int enchantsToIncludeHeight;
    private static int enchantsToExcludeHeight;

    public static void updateScreen() {
        if (textFieldMatches != null && textFieldExclusions != null) {
            textFieldMatches.updateCursorCounter();
            textFieldExclusions.updateCursorCounter();
        }
    }

    public static void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        islandWarpGui = null;

        if (Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY.isEnabled(FeatureSetting.SHOW_SALVAGE_ESSENCES_COUNTER)) {
            InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

            if (inventoryType == InventoryType.SALVAGING) {
                main.getDungeonManager().getSalvagedEssences().clear();
            }
        }
    }

    public static void drawScreenIslands(int mouseX, int mouseY, ReturnValue<?> returnValue) {
        if (!SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        Container playerContainer = mc.thePlayer.openContainer;
        if (playerContainer instanceof ContainerChest && Feature.FANCY_WARP_MENU.isEnabled()) {
            IInventory chestInventory = ((ContainerChest) playerContainer).getLowerChestInventory();
            if (chestInventory.hasCustomName()) {
                String chestName = chestInventory.getDisplayName().getUnformattedText();
                if (chestName.equals("Fast Travel")) {
                    if (islandWarpGui == null) {
                        islandWarpGui = new IslandWarpGui();
                        ScaledResolution scaledresolution = new ScaledResolution(mc);
                        int i = scaledresolution.getScaledWidth();
                        int j = scaledresolution.getScaledHeight();
                        islandWarpGui.setWorldAndResolution(mc, i, j);
                    }

                    try {
                        islandWarpGui.drawScreen(mouseX, mouseY, 0);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }

                    returnValue.cancel();
                } else {
                    islandWarpGui = null;
                }
            } else {
                islandWarpGui = null;
            }
        } else {
            islandWarpGui = null;
        }
    }

    public static void drawScreen(int guiLeft, int guiTop) {
        if (!SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        InventoryType inventoryType = SkyblockAddons.getInstance().getInventoryUtils().getInventoryType();

        // Essences from TabListParser#parseSections
        if (Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY.isEnabled(FeatureSetting.SHOW_SALVAGE_ESSENCES_COUNTER)
                && inventoryType == InventoryType.SALVAGING
                && main.getUtils().getMap() == Island.DUNGEON_HUB) {
            int ySize = 222 - 108 + 6 * 18;
            float x = guiLeft - 117 - 5;
            float y = guiTop + ySize / 2F - 72 / 2F;

            SkyblockAddons.getInstance().getRenderListener().drawCollectedEssences(x, y, false, true);
        }

        if (Feature.REFORGE_FILTER.isEnabled()
                && (inventoryType == InventoryType.BASIC_REFORGING || inventoryType == InventoryType.HEX_REFORGING)
                && textFieldMatches != null) {

            int defaultBlue = ColorUtils.getDefaultBlue(255);
            int x = guiLeft - 160;
            if (x < 0) {
                x = 20;
            }
            int y = guiTop + REFORGE_MENU_HEIGHT / 2 - reforgeFilterHeight / 2;

            GlStateManager.color(1F, 1F, 1F);
            fontRenderer.drawSplitString(TYPE_ENCHANTMENTS, x, y, maxStringWidth, defaultBlue);
            y = y + typeEnchantmentsHeight;
            fontRenderer.drawSplitString(SEPARATE_MULTIPLE, x, y, maxStringWidth, defaultBlue);

            int placeholderTextX = textFieldMatches.xPosition + 4;
            int placeholderTextY = textFieldMatches.yPosition + (textFieldMatches.height - 8) / 2;

            y = textFieldMatches.yPosition - enchantsToIncludeHeight - 1;
            fontRenderer.drawSplitString(ENCHANTS_TO_INCLUDE, x, y, maxStringWidth, defaultBlue);

            textFieldMatches.drawTextBox();
            if (StringUtils.isEmpty(textFieldMatches.getText())) {
                fontRenderer.drawString(fontRenderer.trimStringToWidth(INCLUSION_EXAMPLE, textFieldMatches.width), placeholderTextX, placeholderTextY, ColorCode.DARK_GRAY.getColor());
            }

            y = textFieldExclusions.yPosition - enchantsToExcludeHeight - 1;
            fontRenderer.drawSplitString(ENCHANTS_TO_EXCLUDE, x, y, maxStringWidth, defaultBlue);

            placeholderTextY = textFieldExclusions.yPosition + (textFieldExclusions.height - 8) / 2;
            textFieldExclusions.drawTextBox();
            if (StringUtils.isEmpty(textFieldExclusions.getText())) {
                fontRenderer.drawString(
                        fontRenderer.trimStringToWidth(EXCLUSION_EXAMPLE, textFieldExclusions.width),
                        placeholderTextX,
                        placeholderTextY,
                        ColorCode.DARK_GRAY.getColor()
                );
            }
        }
    }

    public static void initGui(IInventory lowerChestInventory, int guiLeft, int guiTop, FontRenderer fontRendererObj) {
        if (!main.getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        InventoryType inventoryType = SkyblockAddons.getInstance().getInventoryUtils().getInventoryType();

        if (inventoryType != null) {
            if (Feature.REFORGE_FILTER.isEnabled()
                    && (inventoryType == InventoryType.BASIC_REFORGING || inventoryType == InventoryType.HEX_REFORGING)) {
                int xPos = guiLeft - 160;
                if (xPos<0) {
                    xPos = 20;
                }
                int yPos;
                int textFieldWidth = guiLeft - 20 - xPos;
                int textFieldHeight = REFORGE_MENU_HEIGHT / 10;
                int textFieldSpacing = (int) (textFieldHeight * 1.5);

                // Calculate the height of the whole thing to center it vertically in relation to the chest UI.
                maxStringWidth = textFieldWidth + 5;
                typeEnchantmentsHeight = fontRenderer.splitStringWidth(TYPE_ENCHANTMENTS, maxStringWidth);
                int separateEnchantmentsHeight = fontRenderer.splitStringWidth(SEPARATE_MULTIPLE, maxStringWidth) + fontRendererObj.FONT_HEIGHT;
                enchantsToIncludeHeight = fontRenderer.splitStringWidth(ENCHANTS_TO_INCLUDE, maxStringWidth);
                enchantsToExcludeHeight = fontRenderer.splitStringWidth(ENCHANTS_TO_EXCLUDE, maxStringWidth);
                reforgeFilterHeight = typeEnchantmentsHeight + separateEnchantmentsHeight + enchantsToIncludeHeight +
                        2 * textFieldHeight + textFieldSpacing;

                yPos = guiTop + REFORGE_MENU_HEIGHT / 2 - reforgeFilterHeight / 2;

                // Matches text field
                yPos = yPos + typeEnchantmentsHeight + separateEnchantmentsHeight + enchantsToIncludeHeight;
                textFieldMatches = new GuiTextField(2, fontRendererObj, xPos, yPos, textFieldWidth, textFieldHeight);
                textFieldMatches.setMaxStringLength(500);
                List<String> reforgeMatches = SkyblockAddons.getInstance().getUtils().getReforgeMatches();
                StringBuilder reforgeBuilder = new StringBuilder();

                for (int i = 0; i < reforgeMatches.size(); i++) {
                    reforgeBuilder.append(reforgeMatches.get(i));
                    if (i < reforgeMatches.size() - 1) {
                        reforgeBuilder.append(',');
                    }
                }
                String text = reforgeBuilder.toString();
                if (!text.isEmpty()) {
                    textFieldMatches.setText(text);
                }

                // Exclusions text field
                yPos = yPos + textFieldHeight + textFieldSpacing;
                textFieldExclusions = new GuiTextField(2, fontRendererObj, xPos, yPos, textFieldWidth, textFieldHeight);
                textFieldExclusions.setMaxStringLength(500);
                List<String> reforgeExclusions = SkyblockAddons.getInstance().getUtils().getReforgeExclusions();
                reforgeBuilder = new StringBuilder();

                for (int i = 0; i < reforgeExclusions.size(); i++) {
                    reforgeBuilder.append(reforgeExclusions.get(i));
                    if (i < reforgeExclusions.size() - 1) {
                        reforgeBuilder.append(',');
                    }
                }
                text = reforgeBuilder.toString();
                if (!text.isEmpty()) {
                    textFieldExclusions.setText(text);
                }

                Keyboard.enableRepeatEvents(true);
            }
        }
    }

    public static boolean keyTyped(char typedChar, int keyCode) { // return whether to continue (super.keyTyped(typedChar, keyCode);)
        if (main.getUtils().isOnSkyblock() && Feature.REFORGE_FILTER.isEnabled()) {
            InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

            if (inventoryType == InventoryType.BASIC_REFORGING || inventoryType == InventoryType.HEX_REFORGING) {
                if (keyCode != mc.gameSettings.keyBindInventory.getKeyCode() ||
                        (!textFieldMatches.isFocused() && !textFieldExclusions.isFocused())) {
                    processTextFields(typedChar, keyCode);
                    return true;
                }
                processTextFields(typedChar, keyCode);
            } else {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    private static void processTextFields(char typedChar, int keyCode) {
        if (Feature.REFORGE_FILTER.isEnabled() && textFieldMatches != null) {
            textFieldMatches.textboxKeyTyped(typedChar, keyCode);
            textFieldExclusions.textboxKeyTyped(typedChar, keyCode);
            List<String> reforges = new LinkedList<>(Arrays.asList(textFieldMatches.getText().split(",")));
            main.getUtils().setReforgeMatches(reforges);
            reforges = new LinkedList<>(Arrays.asList(textFieldExclusions.getText().split(",")));
            main.getUtils().setReforgeExclusions(reforges);
        }
    }

    public static void handleMouseClick(Slot slotIn, Container slots, IInventory lowerChestInventory, ReturnValue<?> returnValue) {
        if (main.getUtils().isOnSkyblock()) {
            if (Feature.REFORGE_FILTER.isEnabled() && !main.getUtils().getReforgeMatches().isEmpty()) {
                if (slotIn != null && !slotIn.inventory.equals(mc.thePlayer.inventory) && slotIn.getHasStack()) {
                    InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

                    Slot itemSlot = null;
                    if (slotIn.getSlotIndex() == 22 && inventoryType == InventoryType.BASIC_REFORGING) {
                        itemSlot = slots.getSlot(13);
                    } else if (inventoryType == InventoryType.HEX_REFORGING) {
                        ItemStack slotInStack = slotIn.getStack();
                        boolean reforgeStone = slotInStack.getItem().equals(Items.skull) && !slotInStack.getDisplayName().contains("Page");

                        if (slotIn.getSlotIndex() == 48 || reforgeStone)
                            itemSlot = slots.getSlot(19);
                    }

                    if (itemSlot != null && itemSlot.getHasStack()) {
                        ItemStack item = itemSlot.getStack();
                        if (item.hasDisplayName()) {
                            String reforge = ItemUtils.getReforge(item);
                            if (reforge != null) {
                                if (main.getUtils().enchantReforgeMatches(reforge)) {
                                    main.getUtils().playLoudSound("random.orb", 0.1);
                                    returnValue.cancel();
                                }
                            }
                        }
                    }
                }
            }

            if (Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled() && !main.getUtils().isInDungeon()
                    && NPCUtils.isSellMerchant(lowerChestInventory)
                    && slotIn != null && slotIn.inventory instanceof InventoryPlayer
                    && !ItemDropChecker.canDropItem(slotIn)) {
                    returnValue.cancel();
            }
        }
    }

    /**
     * Handles mouse clicks for the Fancy Warp GUI and the Reforge Filter text fields.
     *
     * @param mouseX x coordinate of the mouse pointer
     * @param mouseY y coordinate of the mouse pointer
     * @param mouseButton mouse button that was clicked
     */
    public static void mouseClicked(int mouseX, int mouseY, int mouseButton, ReturnValue<?> returnValue) throws IOException {
        if (islandWarpGui != null) {
            islandWarpGui.mouseClicked(mouseX, mouseY, mouseButton);
            returnValue.cancel();
            return;
        }

        if (textFieldMatches != null) {
            textFieldMatches.mouseClicked(mouseX, mouseY, mouseButton);
            textFieldExclusions.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public static List<Float> color(IInventory lowerChestInventory) {
        if (!main.getUtils().isOnSkyblock()) {
            return Collections.emptyList();
        }

        if (Feature.SHOW_BACKPACK_PREVIEW.isEnabled(FeatureSetting.MAKE_INVENTORY_COLORED)) {
            if (main.getInventoryUtils().getInventoryType() == InventoryType.STORAGE_BACKPACK) {
                int pageNum = main.getInventoryUtils().getInventoryPageNum();
                if (BackpackInventoryManager.getBackpackColor().containsKey(pageNum)) {
                    BackpackColor color = BackpackInventoryManager.getBackpackColor().get(pageNum);
                    return Arrays.asList(color.getR(), color.getG(), color.getB(), 1.0F);
                }
            }
        }
        return Collections.emptyList();
    }

    public static void mouseReleased(ReturnValue<?> returnValue) {
        if (islandWarpGui != null) {
            returnValue.cancel();
        }
    }

    public static void mouseClickMove(ReturnValue<?> returnValue) {
        if (islandWarpGui != null) {
            returnValue.cancel();
        }
    }

    public static void onRenderChestForegroundLayer(GuiChest guiChest) {
        if (!SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        if (Feature.SHOW_REFORGE_OVERLAY.isEnabled()) {
            if (guiChest.inventorySlots.inventorySlots.size() > 13) {
                InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

                Slot slot = inventoryType == InventoryType.HEX_REFORGING
                        ? guiChest.inventorySlots.inventorySlots.get(19)
                        : guiChest.inventorySlots.inventorySlots.get(13);

                if (slot != null) {
                    ItemStack item = slot.getStack();
                    if (item != null) {
                        String reforge = null;
                        if (inventoryType == InventoryType.BASIC_REFORGING || inventoryType == InventoryType.HEX_REFORGING) {
                            reforge = ItemUtils.getReforge(item);
                        }

                        if (reforge != null) {
                            int color = ColorCode.YELLOW.getColor();
                            if (Feature.REFORGE_FILTER.isEnabled() &&
                                    !main.getUtils().getReforgeMatches().isEmpty() &&
                                    main.getUtils().enchantReforgeMatches(reforge)) {
                                color = ColorCode.RED.getColor();
                            }

                            int x = slot.xDisplayPosition;
                            int y = slot.yDisplayPosition;

                            int stringWidth = mc.fontRendererObj.getStringWidth(reforge);
                            float renderX = x - 28 - stringWidth / 2F;
                            int renderY = y + 22;

                            GlStateManager.disableDepth();
                            drawTooltipBackground(renderX, renderY, stringWidth);
                            mc.fontRendererObj.drawString(reforge, renderX, renderY, color, true);
                            GlStateManager.enableDepth();
                        }
                    }
                }
            }
        }
    }

    private static void drawTooltipBackground(float x, float y, float width) {
        int l = -267386864;
        DrawUtils.drawRectAbsolute(x - 3, y - 4, x + width + 3, y - 3, l);
        DrawUtils.drawRectAbsolute(x - 3, y + 8 + 3, x + width + 3, y + 8 + 4, l);
        DrawUtils.drawRectAbsolute(x - 3, y - 3, x + width + 3, y + 8 + 3, l);
        DrawUtils.drawRectAbsolute(x - 4, y - 3, x - 3, y + 8 + 3, l);
        DrawUtils.drawRectAbsolute(x + width + 3, y - 3, x + width + 4, y + 8 + 3, l);

        int borderColor = 1347420415;
        DrawUtils.drawRectAbsolute(x - 3, y - 3 + 1, x - 3 + 1, y + 8 + 3 - 1, borderColor);
        DrawUtils.drawRectAbsolute(x + width + 2, y - 3 + 1, x + width + 3, y + 8 + 3 - 1, borderColor);
        DrawUtils.drawRectAbsolute(x - 3, y - 3, x + width + 3, y - 3 + 1, borderColor);
        DrawUtils.drawRectAbsolute(x - 3, y + 8 + 2, x + width + 3, y + 8 + 3, borderColor);
    }
}
