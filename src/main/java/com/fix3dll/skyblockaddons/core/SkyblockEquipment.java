package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public enum SkyblockEquipment {
    NECKLACE(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Necklace"),
    CLOAK(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Cloak"),
    BELT(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Belt"),
    GLOVES_BRACELET(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Gloves", ColorCode.DARK_GRAY + "> Bracelet"),
    PET(ColorCode.GRAY + "Empty Pet Slot");

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    @Getter @Setter private ItemStack itemStack;
    @Getter private final ItemStack emptyStack;
    private boolean isHovered = false;

    SkyblockEquipment(String... defaultName) {
        this.emptyStack = Items.LIGHT_GRAY_STAINED_GLASS_PANE.getDefaultInstance();
        List<Component> loreList = createListsForItemLore(defaultName);
        if (!loreList.isEmpty()) {
            this.emptyStack.set(DataComponents.CUSTOM_NAME, loreList.getFirst());
            loreList.removeFirst();
        }
        this.emptyStack.set(DataComponents.LORE, new ItemLore(loreList));
        this.itemStack = emptyStack;
    }

    public static void initialize(List<ItemStack> items) {
        if (items == null) return;

        int itemListSize = items.size();
        for (int i = 0; i < values().length; i++) {
            if (itemListSize > i) {
                values()[i].itemStack = items.get(i);
            }
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, int leftPos, int topPos) {
        int x = -15;
        int y = 8 + this.ordinal() * 18 + (this == PET ? 4 : 0);
        int seed = x + y * 176;
        Font font = MC.font;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate((float)leftPos, (float)topPos, 100.0F);
        graphics.renderItem(this.itemStack, x, y, seed);
        graphics.renderItemDecorations(font, this.itemStack, x, y);
        poseStack.popPose();

        int translatedMouseX = mouseX - leftPos;
        int translatedMouseY = mouseY - topPos;
        if (translatedMouseX >= x - 1 && translatedMouseX < x + 16 + 1
                && translatedMouseY >= y - 1 && translatedMouseY < y + 16 + 1) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 302);
            graphics.renderTooltip(font, this.itemStack, mouseX, mouseY);
            poseStack.popPose();
            isHovered = true;
        } else {
            isHovered = false;
        }
    }

    public void onClick(int button) {
        LocalPlayer player = MC.player;
        if (player == null || !isHovered || button != 0) return;

        if (this == PET) {
            player.connection.sendChat("/petsmenu");
        } else {
            player.connection.sendChat("/equipment");
        }
    }

    public static boolean equipmentsInInventory() {
        return main.getUtils().isOnSkyblock() && Feature.EQUIPMENTS_IN_INVENTORY.isEnabled();
    }

    /**
     * Save equipments to persistent values
     */
    public static void saveEquipments() {
        int length = values().length;
        ItemStack[] list = new ItemStack[length];
        for (int slotNumber = 0; slotNumber < length; slotNumber++) {
            ItemStack itemStack = values()[slotNumber].getItemStack();
            list[slotNumber] = itemStack == ItemStack.EMPTY ? values()[slotNumber].getEmptyStack() : itemStack;
        }

        main.getPersistentValuesManager().getPersistentValues().getEquipments().setStorage(
                ItemUtils.getCompressedNBT(list).getAsByteArray()
        );
        main.getPersistentValuesManager().saveValues();
    }

    private static List<Component> createListsForItemLore(String... strings) {
        if (strings == null || strings.length == 0) {
            throw new IllegalArgumentException("\"strings\" cannot be null or empty!");
        }

        List<Component> list = new ArrayList<>();
        for (String string : strings) {
            list.add(Component.literal(string));
        }
        return list;
    }

}