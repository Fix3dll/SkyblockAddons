package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.gson.GsonInitializable;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.Logger;

public class PetItem implements GsonInitializable {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    @SerializedName("displayName")
    @Getter private String displayName;
    @SerializedName("enchanted")
    private boolean enchanted;
    @SerializedName("material")
    private String material;
    @SerializedName("rarity")
    @Getter private SkyblockRarity rarity;
    @SerializedName("resolvableProfile")
    private JsonElement resolvableProfile;
    @SerializedName("itemModel")
    private String itemModel;

    @Getter private transient ItemStack itemStack;

    @Override
    public void gsonInit() {
        makeItemStack();
    }

    private void makeItemStack() {
        try {
            if (material != null) {
                Identifier itemModelId = itemModel == null ? null : Identifier.parse(itemModel);
                if (material.equals("skull_item")) {
                    itemStack = ItemUtils.createSkullItemStack(itemModelId, resolvableProfile, null, "PET_ITEM");
                } else {
                    Item item = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material));
                    if (item != Items.AIR) {
                        itemStack = item.asItem().getDefaultInstance();
                    } else {
                        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(material));
                        if (block != Blocks.AIR) {
                            itemStack = block.asItem().getDefaultInstance();
                        } else {
                            itemStack = Items.BARRIER.getDefaultInstance(); // Item not found
                        }
                    }

                    if (enchanted) {
                        itemStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    }
                }
            }
        } catch (Exception ex) {
            itemStack = Items.BARRIER.getDefaultInstance();
            LOGGER.error(
                    "An error occurred while making an ItemStack with ID {} and name {}.\n{}",
                    material, displayName, ex
            );
        }
    }
}
