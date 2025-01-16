package codes.biscuit.skyblockaddons.mixins.transformers;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackColor;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackInventoryManager;
import codes.biscuit.skyblockaddons.utils.objects.ReturnValue;
import codes.biscuit.skyblockaddons.mixins.hooks.GuiChestHook;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.io.IOException;
import java.util.List;

@Mixin(value = GuiChest.class, priority = 100)
public abstract class GuiChestMixin extends GuiContainer {
    @Shadow public IInventory lowerChestInventory;
    @Shadow private IInventory upperChestInventory;

    public GuiChestMixin(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @ModifyArgs(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"))
    protected void sba$drawGuiContainerBackgroundColor(Args args) {
        List<Float> rgba = GuiChestHook.color(this.lowerChestInventory);
        for (int i = 0; i < rgba.size(); i++) {
            args.set(i, rgba.get(i));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        GuiChestHook.onRenderChestForegroundLayer((GuiChest) (Object) this);

        int color = 4210752; // vanilla color
        if (main.getUtils().isOnSkyblock() && Feature.SHOW_BACKPACK_PREVIEW.isEnabled(FeatureSetting.MAKE_INVENTORY_COLORED)) {
            BackpackColor backpackColor = BackpackInventoryManager.getBackpackColor().get(
                    main.getInventoryUtils().getInventoryPageNum()
            );
            if (backpackColor != null) {
                color = backpackColor.getInventoryTextColor();
            }
        }
        this.fontRendererObj.drawString(this.lowerChestInventory.getDisplayName().getUnformattedText(), 8, 6, color);
        this.fontRendererObj.drawString(this.upperChestInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, color);
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
