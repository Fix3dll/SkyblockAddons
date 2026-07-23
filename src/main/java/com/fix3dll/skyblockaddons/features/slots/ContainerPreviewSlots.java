package com.fix3dll.skyblockaddons.features.slots;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The client-only slots a container preview is drawn with. Unlike the equipment slots these never become the screen's
 * hovered slot: a preview opens while a real container slot is hovered, and that slot has to keep its own tooltip and
 * click handling.
 */
public final class ContainerPreviewSlots {

    /** A preview is at most six rows of nine, so anything past this cannot be drawn on the chest texture anyway. */
    public static final int MAX_SLOTS = 54;

    private static final Container CONTAINER = new PreviewContainer();

    private static List<ItemStack> items = List.of();
    private static PreviewSlot[] slots = new PreviewSlot[0];
    private static int slotCols;
    private static int slotSpacing;

    private ContainerPreviewSlots() {}

    /**
     * Draws the preview contents through vanilla's per-slot path. Slot coordinates are relative to the given origin,
     * so the pooled slots survive the preview following the mouse and are only rebuilt when the grid itself changes.
     *
     * @param contents the preview items, which may contain {@code null} entries for empty cells
     */
    public static void render(
            AbstractContainerScreen<?> screen,
            GuiGraphics graphics,
            List<ItemStack> contents,
            int cols,
            int spacing,
            int originX,
            int originY,
            int mouseX,
            int mouseY
    ) {
        if (cols <= 0) return;

        items = contents;
        layout(cols, spacing);

        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate((float) originX, (float) originY);

        int size = Math.min(contents.size(), MAX_SLOTS);
        for (int i = 0; i < size; i++) {
            PreviewSlot slot = slots[i];
            if (slot.isActive()) {
                screen.renderSlot(graphics, slot, mouseX, mouseY);
            }
        }

        poseStack.popMatrix();
        items = List.of();
    }

    private static void layout(int cols, int spacing) {
        if (slots.length == MAX_SLOTS && slotCols == cols && slotSpacing == spacing) return;

        slots = new PreviewSlot[MAX_SLOTS];
        for (int i = 0; i < MAX_SLOTS; i++) {
            slots[i] = new PreviewSlot(i, (i % cols) * spacing, (i / cols) * spacing);
        }
        slotCols = cols;
        slotSpacing = spacing;
    }

    private static class PreviewSlot extends VirtualSlot {

        private PreviewSlot(int containerSlot, int x, int y) {
            super(CONTAINER, containerSlot, x, y);
        }

        @Override
        public boolean isActive() {
            return !this.getItem().isEmpty();
        }
    }

    /** Live view over the items of the preview currently being drawn. */
    private static class PreviewContainer implements Container {

        @Override
        public int getContainerSize() {
            return MAX_SLOTS;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack item : items) {
                if (item != null && !item.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public @NonNull ItemStack getItem(int slot) {
            if (slot < 0 || slot >= items.size()) {
                return ItemStack.EMPTY;
            }
            // Empty cells are decompressed as nulls, which a container is never allowed to return
            ItemStack item = items.get(slot);
            return item == null ? ItemStack.EMPTY : item;
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