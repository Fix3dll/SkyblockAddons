package codes.biscuit.skyblockaddons.features.backpacks;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;

import java.util.List;

import static codes.biscuit.skyblockaddons.utils.data.skyblockdata.ContainerData.ContainerType;

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
