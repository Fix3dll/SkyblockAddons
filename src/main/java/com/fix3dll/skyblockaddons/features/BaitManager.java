package com.fix3dll.skyblockaddons.features;

import com.fix3dll.skyblockaddons.utils.ItemUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of bait in the Player's Inventory.
 */
public class BaitManager {

    /**
     * The BaitListManager instance.
     */
    @Getter private static final BaitManager instance = new BaitManager();
    private static final int NUMBER_OF_BAITS = 27;

    public static final Map<ItemStack, Integer> DUMMY_BAITS = new HashMap<>();

    static {
        DUMMY_BAITS.put(ItemUtils.getTexturedHead("CARROT_BAIT"), 1);
        DUMMY_BAITS.put(ItemUtils.getTexturedHead("MINNOW_BAIT"), 2);
        DUMMY_BAITS.put(ItemUtils.getTexturedHead("WHALE_BAIT"), 3);
    }

    /**
     * A map of all baits in the inventory and their count
     */
    @Getter private final Object2ObjectOpenHashMap<ItemStack, Integer> baitsInInventory = new Object2ObjectOpenHashMap<>(NUMBER_OF_BAITS);

    /**
     * Re-count all baits in the inventory
     */
    public void refreshBaits(LocalPlayer player) {
        baitsInInventory.clear();

        for (ItemStack itemStack : player.getInventory().getNonEquipmentItems()) {
            String skyblockID = ItemUtils.getSkyblockItemID(itemStack);
            ItemStack bait = ItemUtils.getTexturedHead(skyblockID);

            if (bait.getItem() == Items.STONE) continue;

            baitsInInventory.put(bait, baitsInInventory.getOrDefault(bait, 0) + itemStack.getCount());
        }
    }
}