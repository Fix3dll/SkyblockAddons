package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.features.tablist.TabListParser;
import com.fix3dll.skyblockaddons.features.tablist.TabListRenderer;
import com.fix3dll.skyblockaddons.mixin.hooks.GuiHook;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
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

    @Inject(method = "renderTabList", at = @At("HEAD"), cancellable = true)
    public void sba$renderTabList(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (GuiHook.isOnSkyblock() && Feature.COMPACT_TAB_LIST.isEnabled()) {
            if (TabListParser.getRenderColumns() != null && this.minecraft.options.keyPlayerList.isDown()) {
                ci.cancel();
                TabListRenderer.render(guiGraphics);
            }
        }
    }

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    public void sba$renderHearts(GuiGraphics guiGraphics, Player player, int x, int y, int height, int offsetHeartIndex, float maxHealth, int currentHealth, int displayHealth, int absorptionAmount, boolean renderHighlight, CallbackInfo ci) {
        GuiHook.renderHearts = !GuiHook.isOnSkyblock() || GuiHook.isHideOnlyOutsideRiftEnabled();

        if (!GuiHook.renderHearts) {
            ci.cancel();
        }
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void sba$renderArmor(GuiGraphics guiGraphics, Player player, int y, int heartRows, int height, int x, CallbackInfo ci) {
        GuiHook.renderArmor = !GuiHook.isOnSkyblock() || Feature.HIDE_FOOD_ARMOR_BAR.isDisabled();

        if (!GuiHook.renderArmor) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    public void sba$renderFood(GuiGraphics guiGraphics, Player player, int y, int x, CallbackInfo ci) {
        GuiHook.renderFood = !GuiHook.isOnSkyblock() || Feature.HIDE_FOOD_ARMOR_BAR.isDisabled();

        if (!GuiHook.renderFood) {
            ci.cancel();
        }
    }

    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    public void sba$renderVehicleHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        GuiHook.renderVehicleHealth = !GuiHook.isOnSkyblock() || Feature.HIDE_PET_HEALTH_BAR.isDisabled();

        if (!GuiHook.renderVehicleHealth) {
            ci.cancel();
        }
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    public void sba$renderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        GuiHook.renderEffectsHud = !GuiHook.isOnSkyblock() || Feature.HIDE_EFFECTS_HUD.isDisabled();

        if (!GuiHook.renderEffectsHud) {
            ci.cancel();
        }
    }

}