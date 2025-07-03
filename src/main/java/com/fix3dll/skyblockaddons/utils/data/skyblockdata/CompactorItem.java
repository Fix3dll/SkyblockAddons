package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.gson.GsonInitializable;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.Logger;

/**
 * For storing any skyblock item. Very much a work in progress, with other things (like pets perhaps) on the way
 * Another potential addition is prevent placing enchanted items (blacklist + whitelist), item cooldown amounts, etc.
 */
public class CompactorItem implements GsonInitializable {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final HolderLookup.Provider LOOKUP = VanillaRegistries.createLookup();

    @SerializedName("material")
    private String material;
    @SerializedName("displayName")
    private JsonElement displayName;
    @SerializedName("enchanted")
    private boolean enchanted;
    @SerializedName("resolvableProfile")
    private JsonElement resolvableProfile;

    @Getter private transient ItemStack itemStack;

    @Override
    public void gsonInit() {
        makeItemStack();
    }

    private void makeItemStack() {
        try {
            if (material != null) {
                if (material.equals("skull_item")) {
                    itemStack = ItemUtils.createSkullItemStack(resolvableProfile, displayName, null);
                } else {
                    Item item = BuiltInRegistries.ITEM.getValue(ResourceLocation.withDefaultNamespace(material));
                    if (item != Items.AIR) {
                        itemStack = item.asItem().getDefaultInstance();
                    } else {
                        Block block = BuiltInRegistries.BLOCK.getValue(ResourceLocation.withDefaultNamespace(material));
                        if (block != Blocks.AIR) {
                            itemStack = block.asItem().getDefaultInstance();
                        } else {
                            itemStack = Items.BARRIER.getDefaultInstance(); // Item not found
                        }
                    }

                    if (enchanted) {
                        itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    }
                    MutableComponent component = Component.Serializer.fromJson(displayName, LOOKUP);
                    if (component != null) {
                        itemStack.set(DataComponents.CUSTOM_NAME, component);
                    }
                }
            }
        } catch (Exception ex) {
            itemStack = ItemUtils.createItemStack(Blocks.BARRIER.asItem(), "ERROR", "ERROR", false);
            LOGGER.error(
                    "An error occurred while making an ItemStack with ID {} and name {}.\n{}",
                    material, displayName, ex
            );
        }
    }
}
