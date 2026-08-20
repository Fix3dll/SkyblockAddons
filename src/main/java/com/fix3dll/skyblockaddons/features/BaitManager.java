package com.fix3dll.skyblockaddons.features;

import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;

import java.text.ParseException;
import java.util.List;

/**
 * Keeps track of bait in the Player's Inventory.
 */
public class BaitManager {

    /** The BaitListManager instance. */
    @Getter private static final BaitManager instance = new BaitManager();
    private static final int NUMBER_OF_BAITS = 28;
    private static final String BAIT_REMAINING_PREFIX = "Bait Remaining: ";

    public static final Object2IntMap<String> DUMMY_BAITS = Object2IntMap.ofEntries(
            Object2IntMap.entry("CARROT_BAIT", 1),
            Object2IntMap.entry("MINNOW_BAIT", 2),
            Object2IntMap.entry("WHALE_BAIT", 3)
    );

    /** A map of all baits in the inventory and their count */
    @Getter private final Object2IntOpenHashMap<String> baitsInInventory = new Object2IntOpenHashMap<>(NUMBER_OF_BAITS);

    @Getter private String selectedBaitId = null;
    @Getter private int selectedRemainingBaits = 0;
    @Getter @Setter private boolean fishingBagEnabled = true;

    /**
     * Re-count all baits in the inventory
     */
    public void refreshBaits(LocalPlayer player) {
        baitsInInventory.clear();

        NonNullList<ItemStack> nonEquipmentItems = player.getInventory().getNonEquipmentItems();
        ItemStack menuItem = nonEquipmentItems.get(8);

        boolean remainingBaitsFound = false;
        if (!menuItem.isEmpty() && fishingBagEnabled) {
            String skyblockID = ItemUtils.getSkyblockItemID(menuItem);

            if (isBait(skyblockID)) {
                int loreCount = getBaitCountFromLore(menuItem);

                if (loreCount > 0) {
                    selectedBaitId = skyblockID;
                    selectedRemainingBaits = loreCount;
                    remainingBaitsFound = true;
                }
            }
        }
        if (!remainingBaitsFound) {
            selectedBaitId = null;
            selectedRemainingBaits = 0;
        }

        for (int i = 0; i < nonEquipmentItems.size(); i++) {
            if (i == 8) {
                continue;
            }

            ItemStack itemStack = nonEquipmentItems.get(i);
            if (itemStack.isEmpty()) continue;

            String skyblockID = ItemUtils.getSkyblockItemID(itemStack);
            if (!isBait(skyblockID)) continue;

            baitsInInventory.addTo(skyblockID, itemStack.getCount());
        }
    }

    private int getBaitCountFromLore(ItemStack itemStack) {
        List<Component> loreList = ItemUtils.getItemLoreComponent(itemStack).reversed();

        for (Component component : loreList) {
            String line = component.getString();
            int index = line.indexOf(BAIT_REMAINING_PREFIX);

            if (index != -1) {
                String countStr = line.substring(index + BAIT_REMAINING_PREFIX.length());
                if (!countStr.isEmpty() && Character.isDigit(countStr.charAt(0))) {
                    try {
                        return TextUtils.NUMBER_FORMAT.parse(countStr).intValue();
                    } catch (ParseException ignored) {}
                }
                break;
            }
        }
        return 0;
    }

    private static boolean isBait(String skyblockID) {
        if (StringUtil.isNullOrEmpty(skyblockID)) return false;
        return skyblockID.endsWith("_BAIT")
                || skyblockID.startsWith("OBFUSCATED_FISH_1")
                || skyblockID.startsWith("OBFUSCATED_FISH_2");
    }

}