package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.events.ClientEvents;
import com.fix3dll.skyblockaddons.mixin.hooks.ClientLevelHook;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "addEntity", at = @At("HEAD"))
    public void sba$onWorldJoin(Entity entity, CallbackInfo ci) {
        ClientEvents.ENTITY_JOIN_WORLD.invoker().onEntityJoinWorld(entity, ci);
    }

    @Inject(method = "removeEntity", at = @At("HEAD"))
    public void sba$removeEntity(int entityId, Entity.RemovalReason reason, CallbackInfo ci) {
        ClientLevelHook.onRemoveEntity(entityId);
    }

    @Inject(method = "setServerVerifiedBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z"))
    public void sba$setServerVerifiedBlockState(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        ClientLevelHook.blockUpdated(pos, state);
    }

}