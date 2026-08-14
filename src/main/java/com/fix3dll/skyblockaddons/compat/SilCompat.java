package com.fix3dll.skyblockaddons.compat;

import com.fix3dll.skyblockaddons.mixin.hooks.ScreenHook;
import com.operationpotato.itemlist.api.ExclusionZoneManager;
import com.operationpotato.itemlist.api.Plugin;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class SilCompat implements Plugin {

    private static final List<Rect2i> EMPTY = List.of();

    public static final Rect2i DUNGEON_PROFIT_OVERLAY = new Rect2i(0, 0, 0, 0);

    @Override
    public void registerExclusionZones(@NonNull ExclusionZoneManager exclusionZoneManager) {
        exclusionZoneManager.addProvider(ContainerScreen.class, screen -> {
            if (ScreenHook.islandWarpGui != null) {
                return List.of(new Rect2i(0, 0, screen.width, screen.height));
            }
            if (!isDungeonProfitOverlayEmpty()) {
                return List.of(DUNGEON_PROFIT_OVERLAY);
            }
            return EMPTY;
        });
    }

    private boolean isDungeonProfitOverlayEmpty() {
        return DUNGEON_PROFIT_OVERLAY.getWidth() <= 0 || DUNGEON_PROFIT_OVERLAY.getHeight() <= 0;
    }

}