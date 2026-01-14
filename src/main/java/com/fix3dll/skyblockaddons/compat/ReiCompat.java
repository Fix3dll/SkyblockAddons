package com.fix3dll.skyblockaddons.compat;

import com.fix3dll.skyblockaddons.mixin.hooks.ScreenHook;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;

import java.util.Collections;

public class ReiCompat implements REIClientPlugin {

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(
                ContainerScreen.class,
                screen -> {
                    if (ScreenHook.islandWarpGui != null) {
                        return Collections.singleton(new Rectangle(0, 0, screen.width, screen.height));
                    }
                    return Collections.emptyList();
                }
        );
    }

}