package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.config.PersistentValuesManager;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.core.npc.NPCUtils;
import com.fix3dll.skyblockaddons.features.ItemDropChecker;
import com.fix3dll.skyblockaddons.features.PetManager.Pet;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import com.fix3dll.skyblockaddons.gui.screens.IslandWarpGui;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AbstractContainerScreenHook {
    
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    private static final ResourceLocation LOCK = SkyblockAddons.resourceLocation("lock.png");
    private static final int OVERLAY_RED = ColorCode.RED.getColor(127);
    /** (slotId, clickedButton) */
    @Getter @Setter private static Pair<Integer, Integer> lastClickedButtonOnPetsMenu = new Pair<>(-46, -1);
    protected static final int REFORGE_MENU_HEIGHT = 222 - 108 + 5 * 18;
    /** Strings for reforge filter */
    private static final String TYPE_TO_MATCH = Translations.getMessage("messages.reforges");
    private static final String TYPE_ENCHANTMENTS = Translations.getMessage("messages.typeEnchantmentsHere", TYPE_TO_MATCH);
    private static final String SEPARATE_MULTIPLE = Translations.getMessage("messages.separateMultiple");
    private static final String ENCHANTS_TO_INCLUDE = Translations.getMessage("messages.enchantsToMatch", TYPE_TO_MATCH);
    private static final String INCLUSION_EXAMPLE = Translations.getMessage("messages.reforgeInclusionExample");
    private static final String ENCHANTS_TO_EXCLUDE = Translations.getMessage("messages.enchantsToExclude", TYPE_TO_MATCH);
    private static final String EXCLUSION_EXAMPLE = Translations.getMessage("messages.reforgeExclusionExample");

    /** Reforge filter text field for reforges to match */
    protected static EditBox textFieldMatches = null;
    /** Reforge filter text field for reforges to exclude */
    protected static EditBox textFieldExclusions = null;

    protected static int reforgeFilterHeight;

    /** String dimensions for reforge filter */
    private static int maxStringWidth;
    private static int typeEnchantmentsHeight;
    private static int enchantsToIncludeHeight;
    private static int enchantsToExcludeHeight;

    @Getter private static IslandWarpGui islandWarpGui = null;

    /**
     * @return true for render default
     */
    public static boolean renderSlotHighlightFront(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            Slot hoveredSlot
    ) {
        if (ContainerPreviewManager.isFrozen()) {
            return false;
        }

        if (hoveredSlot != null && MC.player != null) {
            if (hoveredSlot.hasItem() && Feature.DISABLE_EMPTY_GLASS_PANES.isEnabled()
                    && main.getUtils().isBlankGlassPane(hoveredSlot.getItem())) {
                return false;
            }

            AbstractContainerMenu container = MC.player.containerMenu;
            int slotNum = hoveredSlot.index + main.getInventoryUtils().getSlotDifference(container);
            main.getUtils().setLastHoveredSlot(slotNum);
            if (main.getUtils().isOnSkyblock() && Feature.LOCK_SLOTS.isEnabled()
                    && main.getPersistentValuesManager().getLockedSlots().contains(slotNum)
                    && (slotNum >= 9 || container instanceof InventoryMenu && slotNum >= 5)) {
                graphics.fill(x + 4, y + 4, x + width - 4, y + height - 4, OVERLAY_RED);
                graphics.blit(RenderType::guiTexturedOverlay, LOCK, hoveredSlot.x, hoveredSlot.y, 0, 0, 16, 16, 256, 256, ARGB.white(0.4F));
                return false;
            }
        }
        return true;
    }

    public static void renderSlot(GuiGraphics graphics, Slot slot) {
        if (MC.player != null && slot != null && Feature.LOCK_SLOTS.isEnabled() && main.getUtils().isOnSkyblock()) {
            if (Feature.LOCK_SLOTS.isEnabled(FeatureSetting.DRAW_LOCK_ONLY_WHEN_HOVERED)) return;

            AbstractContainerMenu container = MC.player.containerMenu;
            int slotNum = slot.index + main.getInventoryUtils().getSlotDifference(container);
            if (main.getPersistentValuesManager().getLockedSlots().contains(slotNum)
                    && (slotNum >= 9 || container instanceof InventoryMenu && slotNum >= 5)) {
                graphics.blit(RenderType::guiTexturedOverlay, LOCK, slot.x, slot.y, 0, 0, 16, 16, 256, 256, ARGB.white(0.4F));
            }
        }
    }

    public static void keyPressed(Slot hoveredSlot, int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (main.getUtils().isOnSkyblock() && MC.player != null) {

            if (Feature.LOCK_SLOTS.isEnabled() && keyCode != 1 && keyCode != MC.options.keyInventory.key.getValue()) {
                int slot = main.getUtils().getLastHoveredSlot();
                boolean isHotkeying = false;
                if (MC.player.getInventory().getSelectedItem() == ItemStack.EMPTY && hoveredSlot != null) {
                    for (int i = 0; i < 9; ++i) {
                        if (keyCode == MC.options.keyHotbarSlots[i].key.getValue()) {
                            slot = i + 36; // They are hotkeying, the actual slot is the targeted one, +36 because
                            isHotkeying = true;
                        }
                    }
                }
                if (slot >= 9 || MC.player.containerMenu instanceof InventoryMenu && slot >= 5) {
                    PersistentValuesManager pvm = main.getPersistentValuesManager();
                    if (pvm.getLockedSlots().contains(slot)) {
                        if (SkyblockKeyBinding.LOCK_SLOT.getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1);
                            pvm.getLockedSlots().remove(slot);
                            pvm.saveValues();
                        } else if (isHotkeying || MC.options.keyDrop.key.getValue() == keyCode) {
                            // Only buttons that would cause an item to move/drop out of the slot will be canceled
                            main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
                            cir.cancel(); // slot is locked
                            return;
                        }
                    } else {
                        if (SkyblockKeyBinding.LOCK_SLOT.getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1);
                            pvm.getLockedSlots().add(slot);
                            pvm.saveValues();
                        }
                    }
                }
            }
            if (MC.options.keyDrop.key.getValue() == keyCode
                    && Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled()
                    && !main.getUtils().isInDungeon()
                    && !ItemDropChecker.canDropItem(hoveredSlot)) {
                cir.cancel();
            }
        }
    }

    /**
     * This method returns true to CANCEL the click in a GUI
     */
    public static boolean onHandleMouseClick(AbstractContainerScreen<?> screen, Slot slot, int slotId, int clickedButton, ClickType clickType) {
        if (MC.player != null && !main.getUtils().isOnSkyblock()) return false;

        if (Feature.REFORGE_FILTER.isEnabled() && !main.getUtils().getReforgeMatches().isEmpty()) {
            if (slot != null && slot.container != MC.player.getInventory() && slot.hasItem()) {
                InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

                NonNullList<Slot> slots = screen.getMenu().slots;
                Slot itemSlot = null;
                if (slot.index == 22 && inventoryType == InventoryType.BASIC_REFORGING) {
                    itemSlot = slots.get(13);
                } else if (inventoryType == InventoryType.HEX_REFORGING) {
                    ItemStack slotInStack = slot.getItem();
                    boolean reforgeStone = slotInStack.getItem().equals(Items.PLAYER_HEAD)
                            && slotInStack.getCustomName() != null
                            && !slotInStack.getCustomName().getString().contains("Page");

                    if (slot.index == 48 || reforgeStone)
                        itemSlot = slots.get(19);
                }

                if (itemSlot != null && itemSlot.hasItem()) {
                    ItemStack item = itemSlot.getItem();
                    if (item.getCustomName() != null) {
                        String reforge = ItemUtils.getReforge(item);
                        if (reforge != null) {
                            if (main.getUtils().enchantReforgeMatches(reforge)) {
                                main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1);
                                return true;
                            }
                        }
                    }
                }
            }
        }

        if (Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled() && !main.getUtils().isInDungeon()
                && NPCUtils.isSellMerchant(screen.getMenu().slots) && slot != null && slot.container instanceof Inventory
                && !ItemDropChecker.canDropItem(slot)) {
            return true;
        }

        // Saves clicks in Pets menu
        if (main.getInventoryUtils().getInventoryType() == InventoryType.PETS && screen.getMenu() instanceof ChestMenu chestMenu) {
            Container container = chestMenu.getContainer();

            ItemStack petBone = container.getItem(4);
            if (petBone != ItemStack.EMPTY && petBone.getItem() == Items.BONE && !Screen.hasShiftDown()) {
                lastClickedButtonOnPetsMenu = new Pair<>(slotId, clickedButton);
                if (slot != null) {
                    ItemStack slotItem = slot.getItem();
                    SkyblockEquipment equipment = SkyblockEquipment.PET;
                    Pet currentPet = main.getPetCacheManager().getCurrentPet();
                    UUID currentPetUuid = currentPet == null ? null : currentPet.getPetInfo().getUuid();

                    // Pet removed
                    if (Objects.equals(ItemUtils.getUuid(slotItem), currentPetUuid)) {
                        equipment.setItemStack(equipment.getEmptyStack());
                        SkyblockEquipment.saveEquipments();
                    } else if (slotItem.is(Items.PLAYER_HEAD)) {
                        equipment.setItemStack(slotItem.copy());
                        SkyblockEquipment.saveEquipments();
                    }
                }
            }
        }

        return main.getUtils().isOnSkyblock() && !main.getUtils().isInDungeon() && slot != null && slot.hasItem()
                && Feature.DISABLE_EMPTY_GLASS_PANES.isEnabled() && main.getUtils().isBlankGlassPane(slot.getItem())
                && (main.getInventoryUtils().getInventoryType() != InventoryType.ULTRASEQUENCER || main.getUtils().isGlassPaneColor(slot.getItem(), DyeColor.BLACK));
    }

    public static void onRenderLabels(AbstractContainerScreen<?> screen, GuiGraphics graphics) {
        if (!main.getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        if (Feature.SHOW_REFORGE_OVERLAY.isEnabled()) {
            NonNullList<Slot> slots = screen.getMenu().slots;

            if (slots.size() > 13) {
                InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

                Slot slot = inventoryType == InventoryType.HEX_REFORGING ? slots.get(19) : slots.get(13);

                if (slot.hasItem()) {
                    ItemStack item = slot.getItem();
                    if (item != ItemStack.EMPTY) {
                        String reforge;
                        if (inventoryType == InventoryType.BASIC_REFORGING || inventoryType == InventoryType.HEX_REFORGING) {
                            reforge = ItemUtils.getReforge(item);
                        } else {
                            reforge = null;
                        }

                        if (reforge != null) {
                            int color;
                            if (Feature.REFORGE_FILTER.isEnabled() &&
                                    !main.getUtils().getReforgeMatches().isEmpty() &&
                                    main.getUtils().enchantReforgeMatches(reforge)) {
                                color = ColorCode.RED.getColor();
                            } else {
                                color = ColorCode.YELLOW.getColor();
                            }

                            int x = slot.x;
                            int y = slot.y;

                            int stringWidth = MC.font.width(reforge);
                            float renderX = x - 28 - stringWidth / 2F;
                            int renderY = y + 22;

                            TooltipRenderUtil.renderTooltipBackground(graphics, (int) renderX, renderY, stringWidth, 7, 400, null);
                            graphics.drawSpecial(source -> {
                                graphics.pose().pushPose();
                                graphics.pose().translate(0, 0, 401);
                                MC.font.drawInBatch(
                                        reforge, renderX, renderY, color, true, graphics.pose().last().pose(), source, Font.DisplayMode.NORMAL, 0, 15728880
                                );
                                graphics.pose().popPose();
                            });
                        }
                    }
                }
            }
        }
    }

    /**
     * @return true if ContainerScreen rendering should be bypassed
     */
    public static boolean drawScreenIslands(AbstractContainerScreen<?> instance, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (MC.player == null || !main.getUtils().isOnSkyblock()) {
            return false; // don't draw any overlays outside SkyBlock
        }

        if (Feature.FANCY_WARP_MENU.isEnabled()) {
            Component titleComponent = instance.getTitle();
            if (titleComponent == null) return false;
            String title = titleComponent.getString();
            if (title.equals("Fast Travel")) {
                if (islandWarpGui == null) {
                    islandWarpGui = new IslandWarpGui();
                    islandWarpGui.init(MC, MC.getWindow().getGuiScaledWidth(), MC.getWindow().getGuiScaledHeight());
                }

                try {
                    islandWarpGui.render(graphics, mouseX, mouseY, partialTick);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }

                return true;
            } else {
                islandWarpGui = null;
            }
        } else {
            islandWarpGui = null;
        }

        return false;
    }

    public static void renderLast(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, int leftPos, int topPos) {
        if (!main.getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

        // Essences from TabListParser#parseSections
        if (Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY.isEnabled(FeatureSetting.SHOW_SALVAGE_ESSENCES_COUNTER)
                && inventoryType == InventoryType.SALVAGING
                && LocationUtils.isOn(Island.DUNGEON_HUB)) {
            int ySize = 222 - 108 + 6 * 18;
            float x = leftPos - 117 - 5;
            float y = topPos + ySize / 2F - 72 / 2F;

            main.getRenderListener().drawCollectedEssences(graphics, x, y, false, true);
        }

        if (Feature.REFORGE_FILTER.isEnabled()
                && (inventoryType == InventoryType.BASIC_REFORGING || inventoryType == InventoryType.HEX_REFORGING)
                && textFieldMatches != null) {

            int defaultBlue = ColorUtils.getDefaultBlue(255);
            int x = leftPos - 160;
            if (x < 0) {
                x = 20;
            }
            int y = topPos + REFORGE_MENU_HEIGHT / 2 - reforgeFilterHeight / 2;

            drawSplitString(graphics, TYPE_ENCHANTMENTS, x, y, maxStringWidth, defaultBlue);
            y = y + typeEnchantmentsHeight;
            drawSplitString(graphics, SEPARATE_MULTIPLE, x, y, maxStringWidth, defaultBlue);

            int placeholderTextX = textFieldMatches.getX() + 4;
            int placeholderTextY = textFieldMatches.getY() + (textFieldMatches.getHeight() - 8) / 2;

            y = textFieldMatches.getY() - enchantsToIncludeHeight - 1;
            drawSplitString(graphics, ENCHANTS_TO_INCLUDE, x, y, maxStringWidth, defaultBlue);

            textFieldMatches.render(graphics, mouseX, mouseY, partialTick);
            if (StringUtils.isEmpty(textFieldMatches.getValue())) {
                graphics.drawString(MC.font, MC.font.plainSubstrByWidth(INCLUSION_EXAMPLE, textFieldMatches.getWidth()), placeholderTextX, placeholderTextY, ColorCode.DARK_GRAY.getColor());
            }

            y = textFieldExclusions.getY() - enchantsToExcludeHeight - 1;
            drawSplitString(graphics, ENCHANTS_TO_EXCLUDE, x, y, maxStringWidth, defaultBlue);

            placeholderTextY = textFieldExclusions.getY() + (textFieldExclusions.getHeight() - 8) / 2;
            textFieldExclusions.render(graphics, mouseX, mouseY, partialTick);
            if (StringUtils.isEmpty(textFieldExclusions.getValue())) {
                graphics.drawString(
                        MC.font,
                        MC.font.plainSubstrByWidth(EXCLUSION_EXAMPLE, textFieldExclusions.getWidth()),
                        placeholderTextX,
                        placeholderTextY,
                        ColorCode.DARK_GRAY.getColor()
                );
            }
        }
    }

    public static void initLast(int leftPos, int topPos) {
        if (!main.getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

        if (inventoryType != null) {
            if (Feature.REFORGE_FILTER.isEnabled()
                    && (inventoryType == InventoryType.BASIC_REFORGING || inventoryType == InventoryType.HEX_REFORGING)) {
                int xPos = leftPos - 160;
                if (xPos<0) {
                    xPos = 20;
                }
                int yPos;
                int textFieldWidth = leftPos - 20 - xPos;
                int textFieldHeight = REFORGE_MENU_HEIGHT / 10;
                int textFieldSpacing = (int) (textFieldHeight * 1.5);

                // Calculate the height of the whole thing to center it vertically in relation to the chest UI.
                maxStringWidth = textFieldWidth + 5;
                typeEnchantmentsHeight = MC.font.wordWrapHeight(TYPE_ENCHANTMENTS, maxStringWidth);
                int separateEnchantmentsHeight = MC.font.wordWrapHeight(SEPARATE_MULTIPLE, maxStringWidth) + MC.font.lineHeight;
                enchantsToIncludeHeight = MC.font.wordWrapHeight(ENCHANTS_TO_INCLUDE, maxStringWidth);
                enchantsToExcludeHeight = MC.font.wordWrapHeight(ENCHANTS_TO_EXCLUDE, maxStringWidth);
                reforgeFilterHeight = typeEnchantmentsHeight + separateEnchantmentsHeight + enchantsToIncludeHeight +
                        2 * textFieldHeight + textFieldSpacing;

                yPos = topPos + REFORGE_MENU_HEIGHT / 2 - reforgeFilterHeight / 2;

                // Matches text field
                yPos = yPos + typeEnchantmentsHeight + separateEnchantmentsHeight + enchantsToIncludeHeight;
                textFieldMatches = new EditBox(MC.font, xPos, yPos, textFieldWidth, textFieldHeight, Component.empty());
                textFieldMatches.setMaxLength(500);
                List<String> reforgeMatches = main.getUtils().getReforgeMatches();
                StringBuilder reforgeBuilder = new StringBuilder();

                for (int i = 0; i < reforgeMatches.size(); i++) {
                    reforgeBuilder.append(reforgeMatches.get(i));
                    if (i < reforgeMatches.size() - 1) {
                        reforgeBuilder.append(',');
                    }
                }
                String text = reforgeBuilder.toString();
                if (!text.isEmpty()) {
                    textFieldMatches.setValue(text);
                }

                // Exclusions text field
                yPos = yPos + textFieldHeight + textFieldSpacing;
                textFieldExclusions = new EditBox(MC.font, xPos, yPos, textFieldWidth, textFieldHeight, Component.empty());
                textFieldExclusions.setMaxLength(500);
                List<String> reforgeExclusions = main.getUtils().getReforgeExclusions();
                reforgeBuilder = new StringBuilder();

                for (int i = 0; i < reforgeExclusions.size(); i++) {
                    reforgeBuilder.append(reforgeExclusions.get(i));
                    if (i < reforgeExclusions.size() - 1) {
                        reforgeBuilder.append(',');
                    }
                }
                text = reforgeBuilder.toString();
                if (!text.isEmpty()) {
                    textFieldExclusions.setValue(text);
                }
            }
        }
    }

    // return whether to continue (super.keyTyped(typedChar, keyCode);)
    public static boolean keyPressed_reforgeFilter(int keyCode, int scanCode, int modifiers) {
        if (main.getUtils().isOnSkyblock() && Feature.REFORGE_FILTER.isEnabled()) {
            InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

            if (inventoryType == InventoryType.BASIC_REFORGING || inventoryType == InventoryType.HEX_REFORGING) {
                if (keyCode != MC.options.keyInventory.key.getValue() ||
                        (!textFieldMatches.isFocused() && !textFieldExclusions.isFocused())) {
                    textFieldMatches.keyPressed(keyCode, scanCode, modifiers);
                    textFieldExclusions.keyPressed(keyCode, scanCode, modifiers);
                    return true;
                }
                textFieldMatches.keyPressed(keyCode, scanCode, modifiers);
                textFieldExclusions.keyPressed(keyCode, scanCode, modifiers);
            } else {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean charTyped_reforgeFilter(char codePoint, int modifiers) {
        if (main.getUtils().isOnSkyblock() && Feature.REFORGE_FILTER.isEnabled() && textFieldMatches != null) {
            InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

            if (inventoryType == InventoryType.BASIC_REFORGING || inventoryType == InventoryType.HEX_REFORGING) {
                textFieldMatches.charTyped(codePoint, modifiers);
                textFieldExclusions.charTyped(codePoint, modifiers);
                LinkedList<String> reforges = new LinkedList<>(Arrays.asList(textFieldMatches.getValue().split(",")));
                main.getUtils().setReforgeMatches(reforges);
                reforges = new LinkedList<>(Arrays.asList(textFieldExclusions.getValue().split(",")));
                main.getUtils().setReforgeExclusions(reforges);
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Handles mouse clicks for the Fancy Warp GUI and the Reforge Filter text fields.
     * @param mouseX x coordinate of the mouse pointer
     * @param mouseY y coordinate of the mouse pointer
     * @param button mouse button that was clicked
     * @return true if click should be bypassed
     */
    public static boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (islandWarpGui != null) { // TODO is it still necessary?
            islandWarpGui.mouseClicked(mouseX, mouseY, button);
            return true;
        }

        if (textFieldMatches != null) {
            textFieldMatches.mouseClicked(mouseX, mouseY, button);
            textFieldMatches.setFocused(textFieldMatches.isHovered());
            textFieldExclusions.mouseClicked(mouseX, mouseY, button);
            textFieldExclusions.setFocused(textFieldExclusions.isHovered());
        }

        return false;
    }

    public static boolean mouseReleased() {
        return islandWarpGui != null;
    }

    public static boolean mouseDragged() {
        return islandWarpGui != null;
    }

    private static void drawSplitString(GuiGraphics graphics, String text, int x, int y, int wrapWidth, int color) {
        List<FormattedCharSequence> lines = MC.font.split(Component.literal(text), wrapWidth);

        int lineY = y;
        for (FormattedCharSequence seq : lines) {
            graphics.drawString(MC.font, seq, x, lineY, color, false);   // dropShadow=false
            lineY += 9;
        }
    }
}