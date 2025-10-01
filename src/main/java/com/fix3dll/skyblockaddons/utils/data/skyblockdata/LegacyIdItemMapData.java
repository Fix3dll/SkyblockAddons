package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class LegacyIdItemMapData {

    private static final Map<String, Item> LEGACY_ID_ITEM_MAP;
    private static final String JSON_FILE_NAME = "legacyIdItemMap.json";

    static {
        LEGACY_ID_ITEM_MAP = loadAndConvertMap();
    }

    private static Map<String, Item> loadAndConvertMap() {
        Gson gson = SkyblockAddons.getGson();
        HashMap<String, Item> tempMap = new HashMap<>();

        try (InputStream is = LegacyIdItemMapData.class.getClassLoader().getResourceAsStream(JSON_FILE_NAME);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(is), StandardCharsets.UTF_8)) {

            Map<String, String> idMapping = gson.fromJson(inputStreamReader, new TypeToken<Map<String, String>>() {}.getType());

            for (Map.Entry<String, String> entry : idMapping.entrySet()) {
                String legacyId = entry.getKey();
                ResourceLocation modernId = ResourceLocation.withDefaultNamespace(entry.getValue());

                BuiltInRegistries.ITEM.get(modernId).ifPresentOrElse(
                        modernItem -> tempMap.put(legacyId, modernItem.value()),
                        () -> tempMap.put(legacyId, Items.BARRIER)
                );
            }
        } catch (Exception ex) {
            DataUtils.handleLocalFileReadException(JSON_FILE_NAME, ex);
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(tempMap);
    }

    public static ItemStack getItemStack(String legacyId) {
        return getItemStack(legacyId, 1);
    }

    public static ItemStack getItemStack(String legacyId, int count) {
        Item item = LEGACY_ID_ITEM_MAP.getOrDefault(legacyId, Items.BARRIER);
        return new ItemStack(item, count);
    }

}