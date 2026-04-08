package com.fix3dll.skyblockaddons.core;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.joml.Matrix3x2fStack;

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
    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE = Identifier.withDefaultNamespace("container/slot_highlight_front");

    private static Type currentType;

    private final ItemStackTemplate emptyStackTemplate;
    @Getter private ItemStack itemStack = ItemStack.EMPTY;
    @Getter private ItemStack emptyStack = ItemStack.EMPTY;
    private boolean isHovered = false;

    SkyblockEquipment(String... defaultName) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        List<Component> loreList = createListsForItemLore(defaultName);
        if (!loreList.isEmpty()) {
            builder.set(DataComponents.CUSTOM_NAME, loreList.getFirst());
            loreList.removeFirst();
        }
        builder.set(DataComponents.LORE, new ItemLore(loreList));
        this.emptyStackTemplate = new ItemStackTemplate(Items.LIGHT_GRAY_STAINED_GLASS_PANE, builder.build());
    }

    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            if (this.emptyStack == ItemStack.EMPTY) {
                this.emptyStack = emptyStackTemplate.create();
            }
            this.itemStack = this.emptyStack;
        } else {
            this.itemStack = itemStack;
        }
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int leftPos, int topPos) {
        if (this == PET && Feature.EQUIPMENTS_IN_INVENTORY.isDisabled(FeatureSetting.PET_PANEL)) return;
        if (this.itemStack == null) {
            if (this.emptyStack == ItemStack.EMPTY) {
                this.emptyStack = emptyStackTemplate.create();
            }
            this.itemStack = this.emptyStack;
        }

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
        graphics.item(this.itemStack, x, y, seed);
        graphics.itemDecorations(font, this.itemStack, x, y);
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
            player.connection.sendChat("/equipment");
        }
    }

    public boolean isEmpty() {
        return ItemStack.matches(itemStack, emptyStack);
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
                    equipments[i].setItemStack(equipments[i].getEmptyStack());
                }
            }
        } else {
            // Fill with empty stacks if cache is missing or empty
            for (SkyblockEquipment equipment : equipments) {
                equipment.setItemStack(equipment.getEmptyStack());
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