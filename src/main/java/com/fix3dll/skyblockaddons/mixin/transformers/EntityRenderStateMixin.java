package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.mixin.extensions.EntityRenderStateExtension;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateExtension {

    @Unique
    private int sba$entityId;

    @Override
    public int sba$getEntityId() {
        return this.sba$entityId;
    }

    @Override
    public void sba$setEntityId(int id) {
        this.sba$entityId = id;
    }

}