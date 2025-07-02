package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.mixin.hooks.FontHook;
import com.fix3dll.skyblockaddons.mixin.hooks.GuiHook;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> {

    @Unique
    private static final ResourceLocation sba$equipmentPanel = SkyblockAddons.resourceLocation("equipmentpanel.png");
    @Unique
    private static final ResourceLocation sba$petPanel = SkyblockAddons.resourceLocation("petpanel.png");

    public InventoryScreenMixin(InventoryMenu menu, RecipeBookComponent<?> recipeBookComponent, Inventory playerInventory, Component title) {
        super(menu, recipeBookComponent, playerInventory, title);
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
    public boolean sba$renderEffects(EffectsInInventory instance, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        return GuiHook.renderEffectsHud;
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V", shift = At.Shift.AFTER))
    public void sba$renderEqs(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (SkyblockEquipment.equipmentsInInventory()) {
            Feature feature = Feature.EQUIPMENTS_IN_INVENTORY;
            Function<ResourceLocation, RenderType> renderType = resourceLocation -> feature.isChroma()
                    ? FontHook.getChromaTextured(resourceLocation)
                    : RenderType.guiTextured(resourceLocation);
            guiGraphics.blit(renderType, sba$equipmentPanel, this.leftPos - 23, this.topPos, 0.0F, 0.0F, 28, 86, 256, 256, feature.getColor());
            if (feature.isEnabled(FeatureSetting.PET_PANEL)) {
                guiGraphics.blit(renderType, sba$petPanel, this.leftPos - 23, this.topPos + 83, 0.0F, 0.0F, 28, 25, 256, 256, feature.getColor());
            }
        }
    }

}