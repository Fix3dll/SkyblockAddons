package com.fix3dll.skyblockaddons.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class RenderEvents {

    public static final Event<RenderLivingName> LIVING_NAME = EventFactory.createArrayBacked(RenderLivingName.class, callbacks -> (livingEntity, d, cir) -> {
        for (RenderLivingName callback : callbacks) {
            callback.shouldRenderLivingName(livingEntity, d, cir);
        }
    });

    public static final Event<SubmitEntityNameTag<EntityRenderState>> SUBMIT_ENTITY_NAME_TAG = EventFactory.createArrayBacked(SubmitEntityNameTag.class, callbacks -> (renderState, poseStack, nodeCollector, cameraRenderState, ci) -> {
        boolean canceled = false;

        for (SubmitEntityNameTag<EntityRenderState> callback : callbacks) {
            canceled = callback.onSubmitEntityNameTag(renderState, poseStack, nodeCollector, cameraRenderState, ci);
        }

        return canceled;
    });

    public static final Event<RenderLevelLast> LEVEL_LAST = EventFactory.createArrayBacked(RenderLevelLast.class, callbacks -> (source, poseStack) -> {
        for (RenderLevelLast callback : callbacks) {
            callback.onRenderLevelLast(source, poseStack);
        }
    });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface RenderLivingName {
        void shouldRenderLivingName(LivingEntity livingEntity, double d, CallbackInfoReturnable<Boolean> cir);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SubmitEntityNameTag<S extends EntityRenderState> {
        /**
         * @return true if name tag rendering will be canceled
         */
        boolean onSubmitEntityNameTag(S renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface RenderLevelLast {
        void onRenderLevelLast(MultiBufferSource.BufferSource source, PoseStack poseStack);
    }

}
