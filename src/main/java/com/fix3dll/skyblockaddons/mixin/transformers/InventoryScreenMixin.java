package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import com.fix3dll.skyblockaddons.features.slots.EquipmentSlots;
import com.fix3dll.skyblockaddons.mixin.hooks.GuiHook;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> {

    @Unique
    private static final Identifier sba$equipmentPanel = SkyblockAddons.identifier("equipmentpanel.png");
    @Unique
    private static final Identifier sba$petPanel = SkyblockAddons.identifier("petpanel.png");

    public InventoryScreenMixin(InventoryMenu menu, RecipeBookComponent<?> recipeBookComponent, Inventory playerInventory, Component title) {
        super(menu, recipeBookComponent, playerInventory, title);
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;render(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    public boolean sba$renderEffects(EffectsInInventory instance, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        return GuiHook.renderEffectsHud;
    }

    /**
     * {@link AbstractRecipeBookScreen} does not call {@code super.extractRenderState}, so the equivalent injection in
     * {@code AbstractContainerScreenMixin} never runs for this screen.
     */
    @Inject(method = "render", at = @At("RETURN"))
    public void sba$drawContainerPreviews(GuiGraphics graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        ContainerPreviewManager.drawContainerPreviews(graphics, this, mouseX, mouseY);
    }

    @Inject(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", shift = At.Shift.AFTER))
    public void sba$renderEqs(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (SkyblockEquipment.equipmentsInInventory()) {
            Feature feature = Feature.EQUIPMENTS_IN_INVENTORY;
            RenderPipeline pipeline = feature.isChroma() ? DrawUtils.CHROMA_TEXT : RenderPipelines.GUI_TEXTURED;
            int panelX = this.leftPos + EquipmentSlots.PANEL_X;
            graphics.blit(pipeline, sba$equipmentPanel, panelX, this.topPos, 0.0F, 0.0F, EquipmentSlots.PANEL_WIDTH, EquipmentSlots.PANEL_HEIGHT, 256, 256, feature.getColor());
            if (feature.isEnabled(FeatureSetting.PET_PANEL)) {
                graphics.blit(pipeline, sba$petPanel, panelX, this.topPos + EquipmentSlots.PET_PANEL_Y, 0.0F, 0.0F, EquipmentSlots.PANEL_WIDTH, EquipmentSlots.PET_PANEL_HEIGHT, 256, 256, feature.getColor());
            }
        }
    }

}