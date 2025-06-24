package com.fix3dll.skyblockaddons.listeners;

import lombok.NonNull;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;

/**
 * This listener is used for {@link net.minecraft.world.ContainerListener}. Its
 * {@code onInventoryChanged} method is called when an item in the {@link SimpleContainer} it is listening
 * to changes.
 */
public class InventoryChangeListener implements ContainerListener {
    private final ScreenListener GUI_SCREEN_LISTENER;

    /**
     * Creates a new {@code InventoryChangeListener} with the given {@code GuiScreenListener} reference.
     *
     * @param guiScreenListener the {@code GuiScreenListener} reference
     */
    public InventoryChangeListener(@NonNull ScreenListener guiScreenListener) {
        GUI_SCREEN_LISTENER = guiScreenListener;
    }

    /**
     * This is called when an item in the {@code InventoryBasic} being listened to changes.
     *
     * @param inventory the {@code InventoryBasic} after the change
     */
    @Override
    public void containerChanged(Container inventory) {
        GUI_SCREEN_LISTENER.containerChanged((SimpleContainer) inventory);
    }
}