package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.PersistentValuesManager;
import codes.biscuit.skyblockaddons.features.ItemDropChecker;
import codes.biscuit.skyblockaddons.core.SkyblockKeyBinding;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import codes.biscuit.skyblockaddons.core.feature.Feature;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class GuiContainerHook {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final ResourceLocation LOCK = new ResourceLocation("skyblockaddons", "lock.png");
    private static final int OVERLAY_RED = ColorCode.RED.getColor(127);
    /** (slotId, clickedButton) */
    @Getter @Setter private static Pair<Integer, Integer> lastClickedButtonOnPetsMenu = new Pair<>(-46, -1);

    public static void drawGradientRect(GuiContainer guiContainer, int left, int top, int right, int bottom, int startColor, int endColor, Slot hoveredSlot) {
        if (ContainerPreviewManager.isFrozen()) {
            return;
        }

        if (hoveredSlot != null) {
            SkyblockAddons main = SkyblockAddons.getInstance();
            if (hoveredSlot.getHasStack() && Feature.DISABLE_EMPTY_GLASS_PANES.isEnabled()
                    && main.getUtils().isEmptyGlassPane(hoveredSlot.getStack())) {
                return;
            }

            Container container = Minecraft.getMinecraft().thePlayer.openContainer;
            int slotNum = hoveredSlot.slotNumber + main.getInventoryUtils().getSlotDifference(container);
            main.getUtils().setLastHoveredSlot(slotNum);
            if (main.getUtils().isOnSkyblock() && Feature.LOCK_SLOTS.isEnabled()
                    && main.getPersistentValuesManager().getLockedSlots().contains(slotNum)
                    && (slotNum >= 9 || container instanceof ContainerPlayer && slotNum >= 5)) {
                guiContainer.drawGradientRect(left, top, right, bottom, OVERLAY_RED, OVERLAY_RED);
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.color(1,1,1,0.4F);
                GlStateManager.enableBlend();
                MC.getTextureManager().bindTexture(LOCK);
                MC.ingameGUI.drawTexturedModalRect(hoveredSlot.xDisplayPosition, hoveredSlot.yDisplayPosition, 0, 0, 16, 16);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                return;
            }
        }
        guiContainer.drawGradientRect(left, top, right, bottom, startColor, endColor); // default behaviour
    }

    public static void keyTyped(Slot theSlot, int keyCode, CallbackInfo ci) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();

        if (main.getUtils().isOnSkyblock() && mc.thePlayer != null) {
            ContainerPreviewManager.onContainerKeyTyped(keyCode);

            if (Feature.LOCK_SLOTS.isEnabled() && (keyCode != 1 && keyCode != mc.gameSettings.keyBindInventory.getKeyCode())) {
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
                    PersistentValuesManager pvm = main.getPersistentValuesManager();
                    if (pvm.getLockedSlots().contains(slot)) {
                        if (SkyblockKeyBinding.LOCK_SLOT.getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound("random.orb", 1);
                            pvm.getLockedSlots().remove(slot);
                            pvm.saveValues();
                        } else if (isHotkeying || mc.gameSettings.keyBindDrop.getKeyCode() == keyCode) {
                            // Only buttons that would cause an item to move/drop out of the slot will be canceled
                            ci.cancel(); // slot is locked
                            main.getUtils().playLoudSound("note.bass", 0.5);
                            return;
                        }
                    } else {
                        if (SkyblockKeyBinding.LOCK_SLOT.getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound("random.orb", 0.1);
                            pvm.getLockedSlots().add(slot);
                            pvm.saveValues();
                        }
                    }
                }
            }
            if (mc.gameSettings.keyBindDrop.getKeyCode() == keyCode
                    && Feature.STOP_DROPPING_SELLING_RARE_ITEMS.isEnabled()
                    && !main.getUtils().isInDungeon()
                    && !ItemDropChecker.canDropItem(theSlot)) {
                ci.cancel();
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
                && Feature.DISABLE_EMPTY_GLASS_PANES.isEnabled() && main.getUtils().isEmptyGlassPane(slot.getStack())
                && (main.getInventoryUtils().getInventoryType() != InventoryType.ULTRASEQUENCER || main.getUtils().isGlassPaneColor(slot.getStack(), EnumDyeColor.BLACK));
    }
}
