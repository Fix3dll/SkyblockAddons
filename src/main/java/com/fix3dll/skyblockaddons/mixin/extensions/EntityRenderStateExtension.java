package com.fix3dll.skyblockaddons.mixin.extensions;

import net.minecraft.world.entity.Entity;

public interface EntityRenderStateExtension {

    Entity sba$getEntity();

    void sba$setEntity(Entity entity);

}