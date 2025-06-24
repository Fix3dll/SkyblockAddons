package com.fix3dll.skyblockaddons.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class RenderEvents {

    public static final Event<RenderLivingName> LIVING_NAME = EventFactory.createArrayBacked(RenderLivingName.class, callbacks -> (livingEntity, d, cir) -> {
        for (RenderLivingName callback : callbacks) {
            callback.shouldRenderLivingName(livingEntity, d, cir);
        }
    });

    public static final Event<RenderLevelLast> LEVEL_LAST = EventFactory.createArrayBacked(RenderLevelLast.class, callbacks -> (deltaTracker) -> {
        for (RenderLevelLast callback : callbacks) {
            callback.onRenderLevelLast(deltaTracker);
        }
    });

    public static final Event<RenderEntityNameTag<EntityRenderState>> RENDER_ENTITY_NAME_TAG = EventFactory.createArrayBacked(RenderEntityNameTag.class, callbacks -> (renderState,  displayName,  poseStack,  bufferSource, packedLight) -> {
        boolean canceled = false;

        for (RenderEntityNameTag<EntityRenderState> callback : callbacks) {
            canceled = callback.onRenderEntityNameTag(renderState,  displayName,  poseStack,  bufferSource, packedLight);
        }

        return canceled;
    });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface RenderLivingName {
        void shouldRenderLivingName(LivingEntity livingEntity, double d, CallbackInfoReturnable<Boolean> cir);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface RenderLevelLast {
        void onRenderLevelLast(DeltaTracker deltaTracker);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface RenderEntityNameTag<S extends EntityRenderState> {
        /**
         * @return true if name tag rendering will be canceled
         */
        boolean onRenderEntityNameTag(S renderState, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight);
    }

}
