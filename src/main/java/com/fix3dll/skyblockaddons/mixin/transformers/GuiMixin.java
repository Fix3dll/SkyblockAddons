package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.features.tablist.TabListParser;
import com.fix3dll.skyblockaddons.features.tablist.TabListRenderer;
import com.fix3dll.skyblockaddons.mixin.hooks.FontHook;
import com.fix3dll.skyblockaddons.mixin.hooks.GuiHook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractHotbarAndDecorations(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER))
    public void sba$onRenderHud_renderListener(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        SkyblockAddons.getInstance().getRenderListener().onRenderHud(guiGraphics, deltaTracker);
    }

    @Inject(method = "extractTabList", at = @At("HEAD"), cancellable = true)
    public void sba$renderTabList(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (GuiHook.isOnSkyblock() && Feature.COMPACT_TAB_LIST.isEnabled()) {
            if (TabListParser.getRenderColumns() != null && this.minecraft.options.keyPlayerList.isDown()) {
                ci.cancel();
                TabListRenderer.render(guiGraphics);
            }
        }
    }

    @Inject(method = "extractHearts", at = @At("HEAD"), cancellable = true)
    public void sba$renderHearts(GuiGraphicsExtractor guiGraphics, Player player, int x, int y, int height, int offsetHeartIndex, float maxHealth, int currentHealth, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci) {
        GuiHook.renderHearts = !GuiHook.isOnSkyblock() || GuiHook.isHideOnlyOutsideRiftEnabled();

        if (!GuiHook.renderHearts) {
            ci.cancel();
        }
    }

    @Inject(method = "extractArmor", at = @At("HEAD"), cancellable = true)
    private static void sba$renderArmor(GuiGraphicsExtractor guiGraphics, Player player, int y, int heartRows, int height, int x, CallbackInfo ci) {
        GuiHook.renderArmor = !GuiHook.isOnSkyblock() || Feature.HIDE_FOOD_ARMOR_BAR.isDisabled();

        if (!GuiHook.renderArmor) {
            ci.cancel();
        }
    }

    @Inject(method = "extractFood", at = @At("HEAD"), cancellable = true)
    public void sba$renderFood(GuiGraphicsExtractor guiGraphics, Player player, int y, int x, CallbackInfo ci) {
        GuiHook.renderFood = !GuiHook.isOnSkyblock() || Feature.HIDE_FOOD_ARMOR_BAR.isDisabled();

        if (!GuiHook.renderFood) {
            ci.cancel();
        }
    }

    @Inject(method = "extractVehicleHealth", at = @At("HEAD"), cancellable = true)
    public void sba$renderVehicleHealth(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        GuiHook.renderVehicleHealth = !GuiHook.isOnSkyblock() || Feature.HIDE_PET_HEALTH_BAR.isDisabled();

        if (!GuiHook.renderVehicleHealth) {
            ci.cancel();
        }
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    public void sba$renderEffects(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        GuiHook.renderEffectsHud = !GuiHook.isOnSkyblock() || Feature.HIDE_EFFECTS_HUD.isDisabled();

        if (!GuiHook.renderEffectsHud) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
    public void sba$expBarLevelChromaFix(GuiGraphicsExtractor graphics, Font font, int level, Operation<Void> original) {
        FontHook.setHaltChroma(true);
        original.call(graphics, font, level);
        FontHook.setHaltChroma(false);
    }

}