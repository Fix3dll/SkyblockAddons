package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.extensions.EntityRenderStateExtension;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateExtension {

    @Unique
    private Entity sba$entity;

    @Override
    public Entity sba$getEntity() {
        return this.sba$entity;
    }

    @Override
    public void sba$setEntity(Entity entity) {
        this.sba$entity = entity;
    }

}