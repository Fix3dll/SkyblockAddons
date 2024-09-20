package codes.biscuit.skyblockaddons.features.backpacks;

import lombok.Getter;

import java.util.HashMap;

/**
 * Class with information of backpacks
 */
public class BackpackInventoryManager {

    /**
     * Backpack slot and color pairs
     */
    @Getter
    private static final HashMap<Integer, BackpackColor> backpackColor = new HashMap<>();

}
