package com.fix3dll.skyblockaddons.compat;

import com.fix3dll.skyblockaddons.mixin.hooks.ScreenHook;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;

import java.util.List;

public class ReiCompat implements REIClientPlugin {

    private static final List<Rectangle> EMPTY = List.of();

    public static final Rectangle DUNGEON_PROFIT_OVERLAY = new Rectangle();

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(
                ContainerScreen.class,
                screen -> {
                    if (ScreenHook.islandWarpGui != null) {
                        return List.of(new Rectangle(0, 0, screen.width, screen.height));
                    }
                    if (!DUNGEON_PROFIT_OVERLAY.isEmpty()) {
                        return List.of(DUNGEON_PROFIT_OVERLAY);
                    }
                    return EMPTY;
                }
        );
    }

}