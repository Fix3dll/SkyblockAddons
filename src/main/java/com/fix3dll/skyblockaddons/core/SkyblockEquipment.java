package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.backpacks.CompressedStorage;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum SkyblockEquipment {
    NECKLACE(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Necklace"),
    CLOAK(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Cloak"),
    BELT(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Belt"),
    GLOVES_BRACELET(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Gloves", ColorCode.DARK_GRAY + "> Bracelet"),
    PET(ColorCode.GRAY + "Empty Pet Slot");

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();
    private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_front");
    private static final ItemStack NULL =  Items.BARRIER.getDefaultInstance();

    static {
        NULL.set(DataComponents.CUSTOM_NAME, Component.literal("ERROR").withColor(ColorCode.RED.getColor()));
        NULL.set(DataComponents.LORE, new ItemLore(Collections.singletonList(
                Component.literal("You have to update equipments and pets from '/petsmenu' and '/equipment'!")
                        .withColor(ColorCode.GRAY.getColor())
        )));
    }

    private static Type currentType;

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

    public static void loadEquipments(Type equipmentType) {
        if (equipmentType != null && currentType != equipmentType) {
            currentType = equipmentType;
        } else {
            return;
        }

        CompressedStorage compressedStorage = main.getPersistentValuesManager().getCompressedEquipments(currentType.getLevelKey());
        if (compressedStorage != null) {
            List <ItemStack> list = ContainerPreviewManager.decompressItems(compressedStorage.getStorage());
            if (list != null) {
                int listSize = list.size();
                // If there is another null while the list is decompressed, fill it too.
                for (int i = 0; i < values().length; i++) {
                    if (listSize > i) {
                        values()[i].itemStack = list.get(i);
                    } else {
                        values()[i].itemStack = values()[i].getEmptyStack();
                    }
                }
            } else {
                // Fill with empty stacks if not decompressed
                for (SkyblockEquipment equipment : values()) {
                    equipment.itemStack = equipment.emptyStack;
                }
            }
        } else {
            // Fill with empty stacks if there is no cache
            for (SkyblockEquipment equipment : values()) {
                equipment.itemStack = equipment.emptyStack;
            }
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, int leftPos, int topPos) {
        if (this == PET && Feature.EQUIPMENTS_IN_INVENTORY.isDisabled(FeatureSetting.PET_PANEL)) return;
        if (this.itemStack == null) this.itemStack = NULL;

        int x = -15;
        int y = 8 + this.ordinal() * 18 + (this == PET ? 4 : 0);
        int seed = x + y * 176;
        Font font = MC.font;

        int translatedMouseX = mouseX - leftPos;
        int translatedMouseY = mouseY - topPos;
        this.isHovered = translatedMouseX >= x - 1 && translatedMouseX < x + 16 + 1
                      && translatedMouseY >= y - 1 && translatedMouseY < y + 16 + 1;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate((float)leftPos, (float)topPos, 100.0F);
        graphics.renderItem(this.itemStack, x, y, seed);
        graphics.renderItemDecorations(font, this.itemStack, x, y);
        if (this.isHovered) graphics.blitSprite(RenderType::guiTexturedOverlay, SLOT_HIGHLIGHT_FRONT_SPRITE, x - 4, y - 4, 24, 24);
        poseStack.popPose();


        if (this.isHovered) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 302);
            graphics.renderTooltip(font, this.itemStack, mouseX, mouseY);
            poseStack.popPose();
        }
    }

    public void onClick(int button) {
        LocalPlayer player = MC.player;
        if (player == null || !isHovered || button != 0) return;

        if (this == PET && Feature.EQUIPMENTS_IN_INVENTORY.isEnabled(FeatureSetting.PET_PANEL)) {
            if (main.getUtils().isOnRift()) {
                player.connection.sendChat("/sbmenu");
            } else {
                player.connection.sendChat("/petsmenu");
            }
        } else {
            player.connection.sendChat("/equipment");
        }
    }

    public boolean isEmpty() {
        return ItemStack.matches(itemStack, emptyStack) || ItemStack.isSameItem(itemStack, NULL);
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
            ItemStack itemStack = values()[slotNumber].itemStack;
            list[slotNumber] = itemStack == ItemStack.EMPTY ? values()[slotNumber].getEmptyStack() : itemStack;
        }

        main.getPersistentValuesManager().getPersistentValues().getEquipmentCache().put(
                main.getUtils().isOnRift() ? Type.RIFT.getLevelKey() : Type.MAIN.getLevelKey(),
                new CompressedStorage(ItemUtils.getCompressedNBT(list).getAsByteArray())
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

    @AllArgsConstructor @Getter
    public enum Type {
        MAIN("main"),
        RIFT("rift");

        public final String levelKey;
    }

}