package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Display order is ordinal. The capacity of full bottles in enum is 0.
 */
@Getter
public enum ThunderBottle {
    THUNDER_IN_A_BOTTLE_EMPTY(null, 0, 50000),
    STORM_IN_A_BOTTLE_EMPTY(null, 0, 500000),
    HURRICANE_IN_A_BOTTLE_EMPTY(null, 0, 5000000),
    THUNDER_IN_A_BOTTLE(null),
    STORM_IN_A_BOTTLE(null),
    HURRICANE_IN_A_BOTTLE(null);

    @Setter private ItemStack itemStack;
    @Setter private int charge;
    private final int capacity;

    ThunderBottle(ItemStack itemStack) {
        this(itemStack, 0, 0);
    }

    ThunderBottle(ItemStack itemStack, int charge, int capacity) {
        this.itemStack = itemStack;
        this.charge = charge;
        this.capacity = capacity;
    }

    public static void updateThunderBottles(ItemStack[] inventory) {
        if (ArrayUtils.isEmpty(inventory)) return;

        boolean foundFullThunderBottle = false;
        int displayOrder = Integer.MAX_VALUE;

        for (ItemStack itemStack : inventory) {
            if (itemStack == null || itemStack.getItem() != Items.skull) continue;

            String itemID = ItemUtils.getSkyblockItemID(itemStack);
            for (ThunderBottle bottle : values()) {
                // If there is multiple empty bottle, Hypixel applies "first-come, first-served" according to inv index
                if (bottle.name().equals(itemID) && bottle.itemStack == null) {
                    bottle.itemStack = itemStack;

                    if (bottle.isFull()) {
                        // Full bottles
                        foundFullThunderBottle = true;
                    } else {
                        // Empty bottles
                        bottle.charge = ItemUtils.getThunderCharge(itemStack);
                    }

                    // Display order
                    if (bottle.ordinal() < displayOrder) {
                        displayOrder = bottle.ordinal();
                    }
                }
            }
        }

        for (ThunderBottle bottle : values()) {
            if (bottle.ordinal() > displayOrder || displayOrder == Integer.MAX_VALUE) {
                bottle.itemStack = null;
                bottle.charge = 0;
            } else if (!foundFullThunderBottle && bottle.isFull()) {
                bottle.itemStack = null;
            }
        }

    }

    /**
     * Returns first {@link ThunderBottle} with non-empty {@link ItemStack} in ordinal order.
     * {@link ThunderBottle} enum sorted by display priority.
     * @return {@link ThunderBottle} enum
     */
    public static ThunderBottle getDisplayBottle() {
        for (ThunderBottle bottle : values()) {
            if (bottle.itemStack != null) {
                return bottle;
            }
        }
        return null;
    }

    public boolean isFull() {
        return this.capacity == 0;
    }

}