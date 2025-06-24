package com.fix3dll.skyblockaddons.mixin.transformers;

import com.fix3dll.skyblockaddons.SkyblockAddons;
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
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
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

import java.util.function.Function;

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

    @Inject(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/ResourceLocation;)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void sba$onRenderTooltip(GuiGraphics graphics, int x, int y, CallbackInfo ci, ItemStack itemStack) {
        if (ScreenHook.onRenderTooltip(itemStack, x, y)) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlightFront(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void sba$setLastSlot(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        SkyblockAddons.getInstance().getUtils().setLastHoveredSlot(-1);
    }

    @WrapWithCondition(method = "renderSlotHighlightFront", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Ljava/util/function/Function;Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    public boolean sba$renderSlotHighlightFront(GuiGraphics graphics, Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation sprite, int x, int y, int width, int height) {
        return !AbstractContainerScreenHook.renderSlotHighlightFront(graphics, x, y, width, height, this.hoveredSlot);
    }

    @Inject(method = "renderSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V", shift = At.Shift.AFTER))
    public void sba$renderSlots(GuiGraphics graphics, CallbackInfo ci, @Local Slot slot, @Share("acs") LocalRef<AbstractContainerScreen<?>> acsRef) {
        acsRef.set((AbstractContainerScreen<?>) (Object) this);
        AbstractContainerScreenHook.renderSlot(graphics, slot);
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void sba$drawBackpacks(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        //noinspection unchecked
        ContainerPreviewManager.drawContainerPreviews(graphics, (AbstractContainerScreen<T>) (Object) this, mouseX, mouseY);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void sba$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!AbstractContainerScreenHook.keyPressed_reforgeFilter(keyCode, scanCode, modifiers)) {
            cir.cancel();
        } else {
            AbstractContainerScreenHook.keyPressed(this.hoveredSlot, keyCode, cir);
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
    public void sba$renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, Operation<Void> original, @Share("acs") LocalRef<AbstractContainerScreen<?>> acsRef) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        AbstractContainerScreenHook.onRenderLabels(acsRef.get(), guiGraphics);

        if (main.getUtils().isOnSkyblock() && Feature.SHOW_BACKPACK_PREVIEW.isEnabled(FeatureSetting.MAKE_INVENTORY_COLORED)) {
            BackpackColor backpackColor = BackpackInventoryManager.getBackpackColor().get(
                    main.getInventoryUtils().getInventoryPageNum()
            );
            if (backpackColor != null) {
                int color = backpackColor.getInventoryTextColor();
                guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, color, false);
                guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color, false);
            }
        } else {
            original.call(guiGraphics, mouseX, mouseY);
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void sba$renderHead(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        //noinspection unchecked
        if (AbstractContainerScreenHook.drawScreenIslands((AbstractContainerScreen<T>) (Object) this, guiGraphics, mouseX, mouseY, partialTick)) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void sba$renderLast(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AbstractContainerScreenHook.renderLast(guiGraphics, mouseX, mouseY, partialTick, this.leftPos, this.topPos);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void sba$initLast(CallbackInfo ci) {
        AbstractContainerScreenHook.initLast(this.leftPos, this.topPos);
    }

    @Inject(method = "mouseClicked", at= @At("HEAD"), cancellable = true)
    public void sba$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (AbstractContainerScreenHook.mouseClicked(mouseX, mouseY, button)) {
            cir.cancel();
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void sba$mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (AbstractContainerScreenHook.mouseReleased()) {
            cir.cancel();
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    public void sba$mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if (AbstractContainerScreenHook.mouseDragged()) {
            cir.cancel();
        }
    }

    // TODO move to screen mixin, dont use overwrite
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (AbstractContainerScreenHook.charTyped_reforgeFilter(codePoint, modifiers)) {
            return super.charTyped(codePoint, modifiers);
        }
        return false;
    }
}