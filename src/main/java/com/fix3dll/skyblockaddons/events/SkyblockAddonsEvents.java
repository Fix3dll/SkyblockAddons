package com.fix3dll.skyblockaddons.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class SkyblockAddonsEvents {

    public static final Event<InventoryLoading> INVENTORY_LOADING_DONE = EventFactory.createArrayBacked(InventoryLoading.class, callbacks -> () -> {
        for (InventoryLoading callback : callbacks) {
            callback.onInventoryLoadingDone();
        }
    });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface InventoryLoading {
        void onInventoryLoadingDone();
    }
}
