package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.core.atlas.Atlases;
import net.minecraft.client.resources.model.AtlasManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(AtlasManager.class)
public class AtlasManagerMixin {

    @Shadow @Final private static List<AtlasManager.AtlasConfig> KNOWN_ATLASES;

    static {
        KNOWN_ATLASES = new ArrayList<>(KNOWN_ATLASES);
        KNOWN_ATLASES.add(new AtlasManager.AtlasConfig(Atlases.PARTICLES_LOCATION, Atlases.PARTICLES, false));
        KNOWN_ATLASES = List.copyOf(KNOWN_ATLASES);
    }

}