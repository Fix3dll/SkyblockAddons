package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackColor;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackInventoryManager;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import com.fix3dll.skyblockaddons.features.slots.EquipmentSlots;
import com.fix3dll.skyblockaddons.features.slots.EquipmentSlots.EquipmentSlot;
import com.fix3dll.skyblockaddons.features.slots.VirtualSlot;
import com.fix3dll.skyblockaddons.mixin.hooks.AbstractContainerScreenHook;
import com.fix3dll.skyblockaddons.mixin.hooks.ScreenHook;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
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

    @Inject(method = "extractTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void sba$onRenderTooltip(GuiGraphicsExtractor guiGraphics, int x, int y, CallbackInfo ci, ItemStack itemStack) {
        if (ScreenHook.onRenderTooltip(itemStack, x, y)) {
            ci.cancel();
        }
    }

    @Inject(method = "extractContents", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;popMatrix()Lorg/joml/Matrix3x2fStack;"))
    public void sba$renderContentsLast(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AbstractContainerScreenHook.renderReforgeTooltip((AbstractContainerScreen<?>) (Object) this, graphics);
    }

    @Inject(method = "extractContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;extractSlotHighlightFront(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    public void sba$setLastSlot(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        SkyblockAddons.getInstance().getUtils().setLastHoveredSlot(-1);
    }

    @WrapWithCondition(method = "extractSlotHighlightFront", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    public boolean sba$renderSlotHighlightFront(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
        return AbstractContainerScreenHook.renderSlotHighlightFront(graphics, x, y, this.hoveredSlot);
    }

    @Inject(method = "extractSlot", at = @At("RETURN"))
    public void sba$renderSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        AbstractContainerScreenHook.renderSlot(graphics, slot);
    }

    @Inject(method = "extractSlots", at = @At("TAIL"))
    public void sba$extractEquipmentSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (SkyblockEquipment.equipmentsInInventory()) {
            //noinspection unchecked
            AbstractContainerScreen<T> instance = (AbstractContainerScreen<T>) (Object) this;
            EquipmentSlots.render(instance, graphics, mouseX, mouseY, this.leftPos, this.topPos);
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    public void sba$drawBackpacks(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
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
    public void sba$slotClicked(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        // Virtual slots are kept out of hoveredSlot, so they cannot get here. This is the packet boundary, so it
        // stays as a backstop against any future path that would hand one to the container input handler.
        if (slot instanceof VirtualSlot) {
            ci.cancel();
            return;
        }

        //noinspection unchecked
        if (AbstractContainerScreenHook.onHandleMouseClick((AbstractContainerScreen<T>) (Object) this, slot, slotId, buttonNum, containerInput)) {
            ci.cancel();
        }
    }

    @WrapMethod(method = "extractLabels")
    public void sba$renderLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY, Operation<Void> original) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock() && Feature.SHOW_BACKPACK_PREVIEW.isEnabled(FeatureSetting.MAKE_INVENTORY_COLORED)) {
            BackpackColor backpackColor = BackpackInventoryManager.getBackpackColor().get(
                    main.getInventoryUtils().getInventoryPageNum()
            );
            if (backpackColor != null) {
                int color = backpackColor.getInventoryTextColor();
                graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, color, false);
                graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color, false);
                return;
            }
        }

        original.call(graphics, mouseX, mouseY);
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    public void sba$renderLast(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
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

        if (SkyblockEquipment.equipmentsInInventory()) {
            EquipmentSlot equipmentSlot = EquipmentSlots.hoveredSlotAt(this.leftPos, this.topPos, event.x(), event.y());
            if (equipmentSlot != null) {
                equipmentSlot.getEquipment().onClick(event.button());
                cir.setReturnValue(true);
            } else if (EquipmentSlots.isOverPanel(this.leftPos, this.topPos, event.x(), event.y())) {
                // The panel sticks out of the GUI rect, so an unconsumed click there would drop the carried item
                cir.setReturnValue(true);
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
    public boolean charTyped(@NonNull CharacterEvent event) {
        AbstractContainerScreenHook.charTyped_reforgeFilter(event);
        return super.charTyped(event);
    }

}