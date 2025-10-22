package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackColor;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackInventoryManager;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import com.fix3dll.skyblockaddons.mixin.hooks.AbstractContainerScreenHook;
import com.fix3dll.skyblockaddons.mixin.hooks.ScreenHook;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {

    @Shadow @Nullable public Slot hoveredSlot;
    @Shadow protected int titleLabelX;
    @Shadow protected int titleLabelY;
    @Shadow @Final protected Component playerInventoryTitle;
    @Shadow protected int inventoryLabelX;
    @Shadow protected int inventoryLabelY;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/ResourceLocation;)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void sba$onRenderTooltip(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci, ItemStack itemStack) {
        if (ScreenHook.onRenderTooltip(itemStack, x, y)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderContents", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;"))
    public void sba$renderContentsLast(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AbstractContainerScreenHook.renderReforgeTooltip((AbstractContainerScreen<?>) (Object) this, graphics);
    }

    @Inject(method = "renderContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlightFront(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void sba$setLastSlot(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        SkyblockAddons.getInstance().getUtils().setLastHoveredSlot(-1);
    }

    @WrapWithCondition(method = "renderSlotHighlightFront", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    public boolean sba$renderSlotHighlightFront(GuiGraphics graphics, RenderPipeline pipeline, ResourceLocation sprite, int x, int y, int width, int height) {
        return AbstractContainerScreenHook.renderSlotHighlightFront(graphics, x, y, this.hoveredSlot);
    }

    @Inject(method = "renderSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", shift = At.Shift.AFTER))
    public void sba$renderSlots(GuiGraphics graphics, CallbackInfo ci, @Local Slot slot) {
        AbstractContainerScreenHook.renderSlot(graphics, slot);
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void sba$drawBackpacks(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        //noinspection unchecked
        AbstractContainerScreen<T> instance = (AbstractContainerScreen<T>) (Object) this;
        ContainerPreviewManager.drawContainerPreviews(graphics, instance, mouseX, mouseY);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void sba$keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (AbstractContainerScreenHook.keyPressed_reforgeFilter(event)) {
            cir.cancel();
        } else {
            AbstractContainerScreenHook.keyPressed(this.hoveredSlot, event.key(), cir);
        }
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    public void sba$slotClicked(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        //noinspection unchecked
        if (AbstractContainerScreenHook.onHandleMouseClick((AbstractContainerScreen<T>) (Object) this, slot, slotId, mouseButton, type)) {
            ci.cancel();
        }
    }

    @WrapMethod(method = "renderLabels")
    public void sba$renderLabels(GuiGraphics graphics, int mouseX, int mouseY, Operation<Void> original) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock() && Feature.SHOW_BACKPACK_PREVIEW.isEnabled(FeatureSetting.MAKE_INVENTORY_COLORED)) {
            BackpackColor backpackColor = BackpackInventoryManager.getBackpackColor().get(
                    main.getInventoryUtils().getInventoryPageNum()
            );
            if (backpackColor != null) {
                int color = backpackColor.getInventoryTextColor();
                graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, color, false);
                graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color, false);
                return;
            }
        }

        original.call(graphics, mouseX, mouseY);
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void sba$renderLast(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AbstractContainerScreenHook.renderLast(graphics, mouseX, mouseY, partialTick, this.leftPos, this.topPos);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void sba$initLast(CallbackInfo ci) {
        AbstractContainerScreenHook.initLast(this.leftPos, this.topPos);
    }

    @Inject(method = "mouseClicked", at= @At("HEAD"), cancellable = true)
    public void sba$mouseClicked(MouseButtonEvent event, boolean isDoubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (AbstractContainerScreenHook.mouseClicked(event, isDoubleClick)) {
            cir.cancel();
        }
        AbstractContainerScreenHook.keyPressed(this.hoveredSlot, event.input() - 100, cir);
        if (SkyblockEquipment.equipmentsInInventory() && Minecraft.getInstance().screen instanceof InventoryScreen) {
            for (SkyblockEquipment equipment : SkyblockEquipment.values()) {
                equipment.onClick(event.button());
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void sba$mouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (AbstractContainerScreenHook.mouseReleased()) {
            cir.cancel();
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    public void sba$mouseDragged(MouseButtonEvent event, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (AbstractContainerScreenHook.mouseDragged()) {
            cir.cancel();
        }
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        AbstractContainerScreenHook.charTyped_reforgeFilter(event);
        return super.charTyped(event);
    }

}