package com.fix3dll.skyblockaddons.features.backpacks;

import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ContainerData.ContainerType;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Getter
public class ContainerPreview {

    @Setter private int x;
    @Setter private int y;
    private final int numRows;
    private final int numCols;
    private final List<ItemStack> items;
    private final String name;
    private final ContainerType containerType;
    private final boolean active;

    private final BackpackColor backpackColor;

    public ContainerPreview(List<ItemStack> items, String name, BackpackColor backpackColor, int rows, int cols, ContainerType containerType) {
        this(items, name, backpackColor, rows, cols, containerType, false);
    }

    public ContainerPreview(List<ItemStack> items, String name, BackpackColor backpackColor, int rows, int cols, ContainerType containerType, boolean active) {
        this.items = items;
        this.name = name;
        this.backpackColor = backpackColor;
        this.numRows = Math.min(rows, 6);
        this.numCols = Math.min(cols, 9);
        this.containerType = containerType;
        this.active = active;
    }
}
