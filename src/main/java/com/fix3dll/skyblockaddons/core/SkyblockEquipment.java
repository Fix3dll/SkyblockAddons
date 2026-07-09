package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");
    private static final ItemStack NULL =  Items.BARRIER.getDefaultInstance();

    static {
        NULL.set(DataComponents.CUSTOM_NAME, Component.literal("ERROR").withColor(ColorCode.RED.getColor()));
        NULL.set(DataComponents.LORE, new ItemLore(Collections.singletonList(
                Component.literal("You have to update equipments and pets from '/petsmenu' and '/equipment'!")
                        .withColor(ColorCode.GRAY.getColor())
        )));
    }

    private static Type currentType;

    @Getter private ItemStack itemStack;
    private final ItemStack emptyStack;
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

    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            this.itemStack = emptyStack;
        } else {
            this.itemStack = itemStack;
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

        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate((float)leftPos, (float)topPos);
        graphics.renderItem(this.itemStack, x, y, seed);
        graphics.renderItemDecorations(font, this.itemStack, x, y);
        if (this.isHovered) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, x - 4, y - 4, 24, 24);
        poseStack.popMatrix();

        if (this.isHovered) {
            graphics.nextStratum();
            graphics.setTooltipForNextFrame(font, this.itemStack, mouseX, mouseY);
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
            if (main.getUtils().isOnRift()) {
                player.connection.sendChat("/stats");
            } else {
                player.connection.sendChat("/equipment");
            }
        }
    }

    public boolean isEmpty() {
        return ItemStack.matches(itemStack, emptyStack) || ItemStack.isSameItem(itemStack, NULL);
    }

    public static boolean equipmentsInInventory() {
        return main.getUtils().isOnSkyblock() && Feature.EQUIPMENTS_IN_INVENTORY.isEnabled();
    }

    /**
     * Loads equipments from the cache manager and applies them to the current slots.
     * @param equipmentType The environment type (MAIN or RIFT) to load equipments for.
     */
    public static void loadEquipments(Type equipmentType) {
        if (equipmentType != null && currentType != equipmentType) {
            currentType = equipmentType;
        } else {
            return;
        }

        List<ItemStack> list = main.getEquipmentCacheManager().loadDecompressedEquipments(currentType.getLevelKey());
        SkyblockEquipment[] equipments = values();

        if (list != null) {
            int listSize = list.size();
            // Fill any remaining equipment slots with empty stacks
            // if the cached list is shorter than the current equipment count.
            for (int i = 0; i < equipments.length; i++) {
                if (i < listSize) {
                    equipments[i].setItemStack(list.get(i));
                } else {
                    equipments[i].setItemStack(equipments[i].emptyStack);
                }
            }
        } else {
            // Fill with empty stacks if cache is missing or empty
            for (SkyblockEquipment equipment : equipments) {
                equipment.setItemStack(equipment.emptyStack);
            }
        }
    }

    /**
     * Collects current equipments and sends them to the cache manager for saving.
     */
    public static void saveEquipments() {
        if (currentType == null) {
            currentType = main.getUtils().isOnRift() ? Type.RIFT : Type.MAIN;
        }

        ItemStack[] currentItems = Arrays.stream(values())
                .map(SkyblockEquipment::getItemStack)
                .toArray(ItemStack[]::new);

        main.getEquipmentCacheManager().saveEquipments(currentType.getLevelKey(), currentItems);
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