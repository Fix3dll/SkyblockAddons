package com.fix3dll.skyblockaddons.config;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.PetInfo;
import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.fix3dll.skyblockaddons.features.PetManager;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;

public class PetCacheManager extends AbstractPersistentDataManager<PetCacheManager.PetCache> {

    @JsonAdapter(PetCache.Deserializer.class)
    public static class PetCache {
        @Setter @Getter private int currentPetIdx = -1;

        /**
         * Mapping between slot index and pet data.
         * Key = index + 45 * (pageNum - 1), Value = {@link PetManager.Pet}
         * @see PetInfo
         */
        @Getter private final Int2ObjectOpenHashMap<PetManager.Pet> petMap = new Int2ObjectOpenHashMap<>(512);

        public static class Deserializer implements JsonDeserializer<PetCache> {
            @Override
            public PetCache deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                Logger logger = SkyblockAddons.getLogger();
                PetCache cache = new PetCache();

                if (!json.isJsonObject()) {
                    logger.warn("petCache.json is corrupted or not a JSON object! Defaulting to empty cache.");
                    return cache;
                }

                JsonObject obj = json.getAsJsonObject();

                JsonElement currentPetIdxElem = obj.get("currentPetIdx");
                if (currentPetIdxElem != null && currentPetIdxElem.isJsonPrimitive()) {
                    try {
                        cache.setCurrentPetIdx(currentPetIdxElem.getAsInt());
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid currentPetIdx value in JSON. Defaulting to -1.");
                    }
                }

                JsonElement petMapElem = obj.get("petMap");
                if (petMapElem != null && petMapElem.isJsonObject()) {
                    JsonObject mapObj = petMapElem.getAsJsonObject();

                    for (Map.Entry<String, JsonElement> entry : mapObj.entrySet()) {
                        int key;
                        try {
                            key = Integer.parseInt(entry.getKey());
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid integer key found in petMap: {}. Skipping.", entry.getKey());
                            continue;
                        }

                        try {
                            PetManager.Pet pet = context.deserialize(entry.getValue(), PetManager.Pet.class);
                            if (pet != null) {
                                cache.getPetMap().put(key, pet);
                            }
                        } catch (Exception e) {
                            logger.error("Failed to deserialize pet at index " + key + ". Skipping this pet to prevent data loss.", e);
                        }
                    }
                }
                return cache;
            }
        }
    }

    public PetCacheManager(File mainConfigDir) {
        super(mainConfigDir, "petCache.json", PetCache.class);
    }

    @Override
    protected PetCache createDefault() {
        return new PetCache();
    }

    public PetManager.Pet getCurrentPet() {
        if (data.getCurrentPetIdx() == -1) return null;
        return data.getPetMap().get(data.getCurrentPetIdx());
    }

    public int getCurrentPetIndex() {
        return data.getCurrentPetIdx();
    }

    public void setCurrentPetIndex(int idx) {
        setCurrentPetIndex(idx, true);
    }

    public void setCurrentPetIndex(int idx, boolean updateEquipment) {
        if (data.getCurrentPetIdx() != idx) {
            data.setCurrentPetIdx(idx);
            saveValues();
        }

        if (updateEquipment) {
            SkyblockEquipment eq = SkyblockEquipment.PET;

            if (idx == -1 && !eq.isEmpty()) {
                eq.setItemStack(null);
                SkyblockEquipment.saveEquipments();
            } else if (idx != -1) {
                PetManager.Pet currentPet = getCurrentPet();
                if (currentPet != null) {
                    ItemStack currentPetItemStack = currentPet.getItemStack();
                    if (currentPetItemStack != null && !ItemStack.isSameItemSameComponents(currentPetItemStack, eq.getItemStack())) {
                        eq.setItemStack(currentPetItemStack);
                        SkyblockEquipment.saveEquipments();
                    }
                }
            }
        }
    }

    public PetManager.Pet getPet(int index) {
        return data.getPetMap().get(index);
    }

    public void putPet(int index, PetManager.Pet pet) {
        data.getPetMap().put(index, pet);
    }

    /**
     * Removes the pet at the given index from the cache.
     * @param index The slot index, computed as {@code index + 45 * (pageNum - 1)}.
     * @return {@code true} if a pet was present and removed, {@code false} otherwise.
     */
    public boolean removePet(int index) {
        return data.getPetMap().remove(index) != null;
    }

}