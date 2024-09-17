package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreviewManager;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiContainerHook {

    private static final ResourceLocation LOCK = new ResourceLocation("skyblockaddons", "lock.png");
    private static final int OVERLAY_RED = ColorCode.RED.getColor(127);
    /** (slotId, clickedButton) */
    @Getter @Setter private static Pair<Integer, Integer> lastClickedButtonOnPetsMenu = new Pair<>(-46, -1);

    public static void keyTyped(int keyCode) {
        ContainerPreviewManager.onContainerKeyTyped(keyCode);
    }

    public static void drawBackpacks(GuiContainer guiContainer, int mouseX, int mouseY) {
        ContainerPreviewManager.drawContainerPreviews(guiContainer, mouseX, mouseY);
    }

    public static void setLastSlot() {
        SkyblockAddons.getInstance().getUtils().setLastHoveredSlot(-1);
    }

    public static void drawGradientRect(GuiContainer guiContainer, int left, int top, int right, int bottom, int startColor, int endColor, Slot theSlot) {
        if (ContainerPreviewManager.isFrozen()) {
            return;
        }

        if (theSlot != null) {
            SkyblockAddons main = SkyblockAddons.getInstance();
            if (theSlot.getHasStack() && main.getConfigValues().isEnabled(Feature.DISABLE_EMPTY_GLASS_PANES)
                    && main.getUtils().isEmptyGlassPane(theSlot.getStack())) {
                return;
            }

            Container container = Minecraft.getMinecraft().thePlayer.openContainer;
            int slotNum = theSlot.slotNumber + main.getInventoryUtils().getSlotDifference(container);
            main.getUtils().setLastHoveredSlot(slotNum);
            if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.LOCK_SLOTS)
                    && main.getConfigValues().getLockedSlots().contains(slotNum)
                    && (slotNum >= 9 || container instanceof ContainerPlayer && slotNum >= 5)) {
                guiContainer.drawGradientRect(left, top, right, bottom, OVERLAY_RED, OVERLAY_RED);
                return;
            }
        }
        guiContainer.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    public static void drawSlot(GuiContainer guiContainer, Slot slot) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        Container container = mc.thePlayer.openContainer;

        if (slot != null) {
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && main.getUtils().isOnSkyblock()) {
                int slotNum = slot.slotNumber + main.getInventoryUtils().getSlotDifference(container);
                if (main.getConfigValues().getLockedSlots().contains(slotNum)
                        && (slotNum >= 9 || container instanceof ContainerPlayer && slotNum >= 5)) {
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.color(1,1,1,0.4F);
                    GlStateManager.enableBlend();
                    mc.getTextureManager().bindTexture(LOCK);
                    mc.ingameGUI.drawTexturedModalRect(slot.xDisplayPosition, slot.yDisplayPosition, 0, 0, 16, 16);
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                }
            }
        }
    }

    public static void keyTyped(GuiContainer guiContainer, int keyCode, Slot theSlot, ReturnValue<?> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        if (main.getUtils().isOnSkyblock()) {
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && (keyCode != 1 && keyCode != mc.gameSettings.keyBindInventory.getKeyCode())) {
                int slot = main.getUtils().getLastHoveredSlot();
                boolean isHotkeying = false;
                if (mc.thePlayer.inventory.getItemStack() == null && theSlot != null) {
                    for (int i = 0; i < 9; ++i) {
                        if (keyCode == mc.gameSettings.keyBindsHotbar[i].getKeyCode()) {
                            slot = i + 36; // They are hotkeying, the actual slot is the targeted one, +36 because
                            isHotkeying = true;
                        }
                    }
                }
                if (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5) {
                    if (main.getConfigValues().getLockedSlots().contains(slot)) {
                        if (main.getLockSlotKey().getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound("random.orb", 1);
                            main.getConfigValues().getLockedSlots().remove(slot);
                            main.getConfigValues().saveConfig();
                        } else if (isHotkeying || mc.gameSettings.keyBindDrop.getKeyCode() == keyCode) {
                            // Only buttons that would cause an item to move/drop out of the slot will be canceled
                            returnValue.cancel(); // slot is locked
                            main.getUtils().playLoudSound("note.bass", 0.5);
                            return;
                        }
                    } else {
                        if (main.getLockSlotKey().getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound("random.orb", 0.1);
                            main.getConfigValues().getLockedSlots().add(slot);
                            main.getConfigValues().saveConfig();
                        }
                    }
                }
            }
            if (mc.gameSettings.keyBindDrop.getKeyCode() == keyCode && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && !main.getUtils().isInDungeon()) {
                if (!main.getUtils().getItemDropChecker().canDropItem(theSlot)) returnValue.cancel();
            }
        }
    }

    /**
     * This method returns true to CANCEL the click in a GUI (lol I get confused)
     */
    public static boolean onHandleMouseClick(Slot slot, int slotId, int clickedButton, int clickType) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        // Saves clicks in Pets menu
        if (main.getInventoryUtils().getInventoryType() == InventoryType.PETS && currentScreen instanceof GuiChest) {
            GuiChest chest = (GuiChest) currentScreen;
            ContainerChest container = (ContainerChest) chest.inventorySlots;
            IInventory lower = container.getLowerChestInventory();

            ItemStack petBone = lower.getStackInSlot(4);
            if (petBone != null && petBone.getItem() == Items.bone) {
                lastClickedButtonOnPetsMenu = new Pair<>(slotId, clickedButton);
            }
        }

        return main.getUtils().isOnSkyblock() && !main.getUtils().isInDungeon() && slot != null && slot.getHasStack()
                && main.getConfigValues().isEnabled(Feature.DISABLE_EMPTY_GLASS_PANES) && main.getUtils().isEmptyGlassPane(slot.getStack())
                && (main.getInventoryUtils().getInventoryType() != InventoryType.ULTRASEQUENCER || main.getUtils().isGlassPaneColor(slot.getStack(), EnumDyeColor.BLACK));
    }
}
