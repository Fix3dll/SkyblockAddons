package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum SkyblockEquipment {
    NECKLACE(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Necklace"),
    CLOAK(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Cloak"),
    BELT(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Belt"),
    GLOVES_BRACELET(ColorCode.GRAY + "Empty Equipment Slot", ColorCode.DARK_GRAY + "> Gloves", ColorCode.DARK_GRAY + "> Bracelet"),
    PET(ColorCode.GRAY + "Empty Pet Slot");

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    /** Slot placement, relative to the screen's {@code leftPos}/{@code topPos}. */
    private static final int SLOT_X = -15;
    private static final int SLOT_Y = 8;
    private static final int SLOT_SPACING = 18;
    private static final int PET_SLOT_Y_OFFSET = 4;

    private static Type currentType;

    private final ItemStackTemplate emptyStackTemplate;
    @Getter private ItemStack itemStack = ItemStack.EMPTY;
    private ItemStack emptyStack = ItemStack.EMPTY;

    SkyblockEquipment(String... defaultName) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        List<Component> loreList = createListsForItemLore(defaultName);
        if (!loreList.isEmpty()) {
            builder.set(DataComponents.CUSTOM_NAME, loreList.getFirst());
            loreList.removeFirst();
        }
        builder.set(DataComponents.LORE, new ItemLore(loreList));
        this.emptyStackTemplate = new ItemStackTemplate(Items.STAINED_GLASS_PANE.lightGray(), builder.build());
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack == null || itemStack.isEmpty() ? emptyStack() : itemStack;
    }

    public int getSlotX() {
        return SLOT_X;
    }

    public int getSlotY() {
        return SLOT_Y + this.ordinal() * SLOT_SPACING + (this == PET ? PET_SLOT_Y_OFFSET : 0);
    }

    /** Whether this slot takes part in rendering and hovering while the panel is shown. */
    public boolean isSlotActive() {
        return this != PET || Feature.EQUIPMENTS_IN_INVENTORY.isEnabled(FeatureSetting.PET_PANEL);
    }

    /** The stack to draw, falling back to the placeholder so the slot is never rendered as an empty one. */
    public ItemStack getDisplayStack() {
        return this.itemStack.isEmpty() ? emptyStack() : this.itemStack;
    }

    public void onClick(int button) {
        LocalPlayer player = MC.player;
        if (player == null || button != 0) return;

        // These are user commands rather than container interactions, so they are sent from the click directly
        if (this == PET) {
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
        return itemStack.isEmpty() || ItemStack.matches(itemStack, emptyStack());
    }

    private ItemStack emptyStack() {
        if (this.emptyStack == ItemStack.EMPTY) {
            this.emptyStack = emptyStackTemplate.create();
        }
        return this.emptyStack;
    }

    public static boolean equipmentsInInventory() {
        return main.getUtils().isOnSkyblock()
                && Feature.EQUIPMENTS_IN_INVENTORY.isEnabled()
                && MC.gui.screen() instanceof InventoryScreen;
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
                    equipments[i].setItemStack(ItemStack.EMPTY);
                }
            }
        } else {
            // Fill with empty stacks if cache is missing or empty
            for (SkyblockEquipment equipment : equipments) {
                equipment.setItemStack(ItemStack.EMPTY);
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