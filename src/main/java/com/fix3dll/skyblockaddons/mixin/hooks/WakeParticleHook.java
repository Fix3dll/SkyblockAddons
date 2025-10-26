package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.atlas.Atlases;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.List;

/**
 * @see com.fix3dll.skyblockaddons.mixin.transformers.AtlasManagerMixin
 */
public class WakeParticleHook {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final List<TextureAtlasSprite> sprites;

    static {
        TextureAtlas sbaAtlas = MC.getAtlasManager().getAtlasOrThrow(Atlases.PARTICLES);
        sprites = List.of(
                sbaAtlas.getSprite(SkyblockAddons.resourceLocation("blank_splash/0")),
                sbaAtlas.getSprite(SkyblockAddons.resourceLocation("blank_splash/1")),
                sbaAtlas.getSprite(SkyblockAddons.resourceLocation("blank_splash/2")),
                sbaAtlas.getSprite(SkyblockAddons.resourceLocation("blank_splash/3"))
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