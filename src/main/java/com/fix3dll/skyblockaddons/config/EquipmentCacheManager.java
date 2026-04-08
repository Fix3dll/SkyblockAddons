package com.fix3dll.skyblockaddons.config;

import com.fix3dll.skyblockaddons.features.backpacks.CompressedStorage;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentCacheManager extends AbstractPersistentDataManager<EquipmentCacheManager.EquipmentCache> {

    @Getter @Setter
    public static class EquipmentCache {
        /**
         * Key = Equipment level key (e.g., "main", "rift"), Value = {@link CompressedStorage}
         */
        private Map<String, CompressedStorage> equipmentStorage = new HashMap<>();
    }

    /**
     * Initializes the equipment cache manager.
     * @param mainConfigDir The root configuration directory.
     */
    public EquipmentCacheManager(File mainConfigDir) {
        super(mainConfigDir, "equipmentCache.json", EquipmentCache.class);
    }

    @Override
    protected EquipmentCache createDefault() {
        return new EquipmentCache();
    }

    /**
     * Loads and decompresses the equipment list for the specified level.
     * @param levelKey The unique identifier for the equipment level.
     * @return A list of decompressed ItemStacks, or null if the cache is empty or invalid.
     */
    public List<ItemStack> loadDecompressedEquipments(String levelKey) {
        if (levelKey == null || levelKey.isEmpty()) {
            return null;
        }

        CompressedStorage compressedStorage = data.getEquipmentStorage().get(levelKey);

        if (compressedStorage != null) {
            byte[] storageBytes = compressedStorage.getStorage();
            if (storageBytes != null) {
                return ContainerPreviewManager.decompressItems(storageBytes);
            }
        }

        return null;
    }

    /**
     * Compresses and saves the provided equipment items to the cache.
     * Utilizes array comparison to prevent unnecessary disk I/O operations.
     * @param levelKey The unique identifier for the equipment level.
     * @param items    The array of ItemStacks to be cached.
     */
    public void saveEquipments(String levelKey, ItemStack[] items) {
        if (levelKey == null || levelKey.isEmpty() || items == null) {
            return;
        }

        byte[] newCompressedBytes = ItemUtils.getCompressedNBT(items).getAsByteArray();
        CompressedStorage oldStorage = data.getEquipmentStorage().get(levelKey);

        // Early return to prevent unnecessary I/O if the items have not changed
        if (oldStorage != null && Arrays.equals(oldStorage.getStorage(), newCompressedBytes)) {
            return;
        }

        data.getEquipmentStorage().put(levelKey, new CompressedStorage(newCompressedBytes));
        saveValues();
    }

}