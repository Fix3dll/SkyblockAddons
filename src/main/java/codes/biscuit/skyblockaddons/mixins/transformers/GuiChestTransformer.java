package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.mixins.hooks.GuiChestHook;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(GuiChest.class)
public abstract class GuiChestTransformer extends GuiContainer {
    @Shadow public IInventory lowerChestInventory;

    public GuiChestTransformer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Redirect(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"))
    protected void drawGuiContainerBackgroundLayer(float colorRed, float colorGreen, float colorBlue, float colorAlpha) {
        GuiChestHook.color(1.0F, 1.0F, 1.0F, 1.0F, this.lowerChestInventory);
    }

    @Inject(method = "drawGuiContainerForegroundLayer", at = @At("HEAD"))
    private void renderChestForegroundLayer(int mouseX, int mouseY, CallbackInfo ci) {
        GuiChestHook.onRenderChestForegroundLayer((GuiChest) (Object) this);
    }

    @Redirect(method = "drawGuiContainerForegroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I"))
    private int GuiChestHook_drawString(FontRenderer instance, String text, int x, int y, int color) {
        return GuiChestHook.drawString(instance, text, x, y, color);
    }

    @Override
    public void updateScreen() {
        GuiChestHook.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ReturnValue<?> returnValue = new ReturnValue<>();
        GuiChestHook.drawScreenIslands(mouseX, mouseY, returnValue);
        if (returnValue.isCancelled()) {
            return;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        GuiChestHook.drawScreen(this.guiLeft, this.guiTop);
    }

    @Override
    public void initGui() {
        super.initGui();
        GuiChestHook.initGui(this.lowerChestInventory, this.guiLeft, this.guiTop, this.fontRendererObj);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (GuiChestHook.keyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
       }
    }

     @Override
     public void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
         ReturnValue<?> returnValue = new ReturnValue<>();
         GuiChestHook.handleMouseClick(slotIn, this.inventorySlots, lowerChestInventory, returnValue);
         if (returnValue.isCancelled()) {
             return;
         }
         super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
     }

     @Override
     public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
         ReturnValue<?> returnValue = new ReturnValue<>();
         GuiChestHook.mouseClicked(mouseX, mouseY, mouseButton, returnValue);
         if (returnValue.isCancelled()) {
             return;
         }
         super.mouseClicked(mouseX, mouseY, mouseButton);
     }

     @Override
     public void mouseReleased(int mouseX, int mouseY, int state) {
         ReturnValue<?> returnValue = new ReturnValue<>();
         GuiChestHook.mouseReleased(returnValue);
         if (returnValue.isCancelled()) {
             return;
         }
         super.mouseReleased(mouseX, mouseY, state);
     }

     @Override
     public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
         ReturnValue<?> returnValue = new ReturnValue<>();
         GuiChestHook.mouseClickMove(returnValue);
         if (returnValue.isCancelled()) {
             return;
         }
         super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
     }

}
