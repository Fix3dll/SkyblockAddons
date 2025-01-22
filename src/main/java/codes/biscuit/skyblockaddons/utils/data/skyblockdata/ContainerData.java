package codes.biscuit.skyblockaddons.utils.data.skyblockdata;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

@Getter
public class ContainerData {

    public enum ContainerType {
        BACKPACK,
        NEW_YEARS_CAKE,
        PERSONAL_COMPACTOR,
        PERSONAL_DELETOR,
        BUILDERS_WAND,
        BUILDERS_RULER,
        BASKET_OF_SEEDS,
        NETHER_WART_POUCH
    }

    /**
     * The container type (see {@link ContainerType}).
     */
    @SerializedName("type")
    private ContainerType type;

    /**
     * The size of the container
     */
    @SerializedName("size")
    private int size;

    /**
     * The data tag where a compressed array of item stacks are stored.
     */
    @SerializedName("compressedItemStacksTag")
    private String compressedItemStacksTag;

    /**
     * Data tags where individual item stacks are stored.
     */
    @SerializedName("itemStackDataTags")
    private List<String> itemStackDataTags;

    /**
     * The ExtraAttributes NBT tag for retrieving backpack color
     */
    @SerializedName("colorTag")
    private String colorTag;

    /**
     * The container (item array) dimensions
     */
    @SerializedName("dimensions")
    private int[] dimensions = {6, 9};


    /* Functions that check the container type */

    public boolean isBackpack() {
        return type == ContainerType.BACKPACK;
    }

    public boolean isCakeBag() {
        return type == ContainerType.NEW_YEARS_CAKE;
    }

    public boolean isPersonalCompactor() {
        return type == ContainerType.PERSONAL_COMPACTOR;
    }

    public boolean isPersonalDeletor() {
        return type == ContainerType.PERSONAL_DELETOR;
    }

    public boolean isBuildersWand() {
        return type == ContainerType.BUILDERS_WAND;
    }

    public boolean isBuildersRuler() {
        return type == ContainerType.BUILDERS_RULER;
    }

    public boolean isBasketOfSeeds() {
        return type == ContainerType.BASKET_OF_SEEDS;
    }

    public boolean isNetherWartPouch() {
        return type == ContainerType.NETHER_WART_POUCH;
    }

    /**
     * @return the item capacity of the container, or a maximum of 54
     */
    public int getSize() {
        return Math.min(size, 54);
    }

    /**
     * @return the number of rows in the container, or a maximum of 6
     */
    public int getNumRows() {
        return dimensions.length == 2 ? Math.min(dimensions[0], 6) : 6;
    }

    /**
     * @return the number of columns in the container, or a maximum of 9
     */
    public int getNumCols() {
        return dimensions.length == 2 ? Math.min(dimensions[1], 9) : 9;
    }

}
