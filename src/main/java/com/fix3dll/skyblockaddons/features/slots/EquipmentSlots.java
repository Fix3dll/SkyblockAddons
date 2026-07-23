package com.fix3dll.skyblockaddons.features.slots;

import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 * The client-only equipment slots drawn beside the player inventory for {@link Feature#EQUIPMENTS_IN_INVENTORY}.
 * Slot coordinates are relative to the screen's {@code leftPos}/{@code topPos}, matching the matrix vanilla has
 * already translated by while it renders menu slots.
 */
public final class EquipmentSlots {

    /** Panel texture placement, relative to the screen's {@code leftPos}/{@code topPos}. */
    public static final int PANEL_X = -23;
    public static final int PANEL_WIDTH = 28;
    public static final int PANEL_HEIGHT = 86;
    public static final int PET_PANEL_Y = 83;
    public static final int PET_PANEL_HEIGHT = 25;

    private static final Identifier SLOT_HIGHLIGHT_BACK_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_back");
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");

    private static final Minecraft MC = Minecraft.getInstance();
    private static final SkyblockEquipment[] EQUIPMENTS = SkyblockEquipment.values();
    private static final Container CONTAINER = new EquipmentContainer();
    private static final List<EquipmentSlot> SLOTS = Arrays.stream(EQUIPMENTS).map(EquipmentSlot::new).toList();

    private static EquipmentSlot hoveredSlot;

    private EquipmentSlots() {}

    /**
     * The slot under the given position, or {@code null}. The stored hover proves the slot was actually drawn - vanilla
     * skips slot rendering entirely while a wide recipe book covers the screen - and the position is re-checked so a
     * hover left over from such a frame cannot dispatch a click.
     */
    public static EquipmentSlot hoveredSlotAt(int leftPos, int topPos, double mouseX, double mouseY) {
        EquipmentSlot slot = hoveredSlot;
        return slot != null && isHovering(slot, (int) mouseX - leftPos, (int) mouseY - topPos) ? slot : null;
    }

    /**
     * Draws the slots through vanilla's per-slot path, so anything hooking it decorates them as well, and resolves
     * the hover state, highlight and tooltip on our own.
     * <p>
     * These slots deliberately never become the screen's {@code hoveredSlot}. That field feeds vanilla paths which
     * build packets out of {@link net.minecraft.world.inventory.Slot#index} - container clicks and the bundle
     * {@code ItemSlotMouseAction} - and a client-only slot has no index to give them. Keeping hover local means the
     * set of vanilla methods that can observe these slots stays closed instead of having to be re-audited whenever
     * the game updates.
     * <p>
     * Coordinates are relative to {@code leftPos}/{@code topPos}, which is the space {@code extractContents} has
     * already translated the matrix into by the time slots are rendered.
     */
    public static void render(
            AbstractContainerScreen<?> screen,
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            int leftPos,
            int topPos
    ) {
        hoveredSlot = null;
        int slotMouseX = mouseX - leftPos;
        int slotMouseY = mouseY - topPos;

        for (EquipmentSlot slot : SLOTS) {
            if (slot.isActive() && isHovering(slot, slotMouseX, slotMouseY)) {
                hoveredSlot = slot;
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, slot.x - 4, slot.y - 4, 24, 24);
                break;
            }
        }

        for (EquipmentSlot slot : SLOTS) {
            if (slot.isActive()) {
                screen.renderSlot(graphics, slot, mouseX, mouseY);
            }
        }

        if (hoveredSlot != null) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, hoveredSlot.x - 4, hoveredSlot.y - 4, 24, 24);
            graphics.setTooltipForNextFrame(MC.font, hoveredSlot.getItem(), mouseX, mouseY);
        }
    }

    private static boolean isHovering(EquipmentSlot slot, int slotMouseX, int slotMouseY) {
        return slotMouseX >= slot.x - 1 && slotMouseX < slot.x + 17
                && slotMouseY >= slot.y - 1 && slotMouseY < slot.y + 17;
    }

    /**
     * Whether the given screen position is over the part of the equipment panel that sticks out of the inventory GUI.
     * Clicks there have to be consumed: vanilla would otherwise count them as clicks outside the container and drop
     * the carried item.
     */
    public static boolean isOverPanel(int leftPos, int topPos, double mouseX, double mouseY) {
        if (mouseX < leftPos + PANEL_X || mouseX >= leftPos) {
            return false;
        }

        double y = mouseY - topPos;
        if (y >= 0 && y < PANEL_HEIGHT) {
            return true;
        }
        return Feature.EQUIPMENTS_IN_INVENTORY.isEnabled(FeatureSetting.PET_PANEL)
                && y >= PET_PANEL_Y && y < PET_PANEL_Y + PET_PANEL_HEIGHT;
    }

    public static class EquipmentSlot extends VirtualSlot {

        @Getter private final SkyblockEquipment equipment;

        private EquipmentSlot(SkyblockEquipment equipment) {
            super(CONTAINER, equipment.ordinal(), equipment.getSlotX(), equipment.getSlotY());
            this.equipment = equipment;
        }

        @Override
        public boolean isActive() {
            return this.equipment.isSlotActive();
        }
    }

    /** Live view over {@link SkyblockEquipment}, so slot contents never need an explicit refresh. */
    private static class EquipmentContainer implements Container {

        @Override
        public int getContainerSize() {
            return EQUIPMENTS.length;
        }

        @Override
        public boolean isEmpty() {
            for (SkyblockEquipment equipment : EQUIPMENTS) {
                if (!equipment.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public @NonNull ItemStack getItem(int slot) {
            return slot >= 0 && slot < EQUIPMENTS.length ? EQUIPMENTS[slot].getDisplayStack() : ItemStack.EMPTY;
        }

        @Override
        public @NonNull ItemStack removeItem(int slot, int count) {
            return ItemStack.EMPTY;
        }

        @Override
        public @NonNull ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, @NonNull ItemStack itemStack) {}

        @Override
        public void setChanged() {}

        @Override
        public boolean stillValid(@NonNull Player player) {
            return false;
        }

        @Override
        public void clearContent() {}
    }

}