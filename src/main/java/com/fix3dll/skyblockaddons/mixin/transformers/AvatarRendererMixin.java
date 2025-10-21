package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.mixin.hooks.LivingEntityRendererHook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    public void sba$onRenderNameTag(AvatarRenderState avatarRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (SkyblockAddons.getInstance().getDungeonManager().onRenderNameTag(avatarRenderState, poseStack, submitNodeCollector, cameraRenderState, ci)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "isEntityUpsideDown(Lnet/minecraft/world/entity/Avatar;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Avatar;isModelPartShown(Lnet/minecraft/world/entity/player/PlayerModelPart;)Z"))
    public boolean sba$isModelPartShown(Avatar instance, PlayerModelPart part, Operation<Boolean> original) {
        return LivingEntityRendererHook.isCoolPerson || instance.isModelPartShown(part);
    }

}