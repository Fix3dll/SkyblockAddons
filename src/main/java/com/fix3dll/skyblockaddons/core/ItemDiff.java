package com.fix3dll.skyblockaddons.core;

import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.world.item.ItemStack;

@Getter
public class ItemDiff {

    /**
     * How long items in the log should be displayed before they are removed in ms
     */
    public static final long LIFESPAN = 5000;
    private final String displayName;
    private final ItemStack itemStack;
    private int amount;
    @Getter(AccessLevel.NONE) private long timestamp;

    /**
     * @param displayName The item's display name.
     * @param amount The changed amount.
     * @param itemStack The {@link ItemStack} of the first item detected
     */
    public ItemDiff(String displayName, int amount, ItemStack itemStack) {
        this.displayName = displayName;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.itemStack = itemStack;
    }

    /**
     * Update the changed amount of the item.
     * @param amount Amount to be added
     */
    public void add(int amount) {
        this.amount += amount;
        if (this.amount == 0) {
            this.timestamp -= LIFESPAN;
        } else {
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * @return Amount of time in ms since the ItemDiff was created.
     */
    public long getLifetime() {
        return System.currentTimeMillis() - timestamp;
    }
}