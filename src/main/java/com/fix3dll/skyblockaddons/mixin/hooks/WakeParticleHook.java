package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;

import java.util.List;

public class WakeParticleHook {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final List<TextureAtlasSprite> sprites;

    static {
        TextureAtlas vanillaAtlas = MC.getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES);
        sprites = List.of(
                vanillaAtlas.getSprite(SkyblockAddons.identifier("blank_splash/0")),
                vanillaAtlas.getSprite(SkyblockAddons.identifier("blank_splash/1")),
                vanillaAtlas.getSprite(SkyblockAddons.identifier("blank_splash/2")),
                vanillaAtlas.getSprite(SkyblockAddons.identifier("blank_splash/3"))
        );
    }

    public static TextureAtlasSprite getBlankSprite(int i) {
        LocalPlayer localPlayer = MC.player;
        if (localPlayer == null || localPlayer.fishing == null || Feature.COLORED_FISHING_PARTICLES.isDisabled()) {
            return null;
        }

        int age = i % 4;
        return sprites.get(age * (sprites.size() - 1) / 4/*lifetime*/);
    }

}