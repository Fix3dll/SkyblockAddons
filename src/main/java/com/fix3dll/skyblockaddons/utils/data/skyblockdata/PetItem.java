package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.gson.GsonInitializable;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
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

    @Setter private String skyblockId;

    private transient ItemStackTemplate itemStackTemplate;
    private transient ItemStack itemStack;

    @Override
    public void gsonInit() {
        makeItemStackTemplate();
    }

    private void makeItemStackTemplate() {
        try {
            if (material != null) {
                Identifier itemModelId = itemModel == null ? null : Identifier.parse(itemModel);
                if (material.equals("skull_item")) {
                    itemStackTemplate = ItemUtils.createSkullTemplate(itemModelId, resolvableProfile, null, "PET_ITEM");
                } else {
                    Item item = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(material));
                    if (item == Items.AIR) {
                        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(material));
                        item = block != Blocks.AIR ? block.asItem() : Items.BARRIER;
                    }

                    DataComponentPatch.Builder builder = DataComponentPatch.builder();

                    if (enchanted) {
                        builder.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                    }

                    itemStackTemplate = new ItemStackTemplate(item, builder.build());
                }
            }
        } catch (Exception ex) {
            itemStackTemplate = new ItemStackTemplate(Items.BARRIER);
            LOGGER.error(
                    "An error occurred while making an ItemStackTemplate with ID {} and name {}.\n{}",
                    material, displayName, ex
            );
        }
    }

    /**
     * Lazily creates and returns the ItemStack from the template.
     * @return The instantiated ItemStack.
     */
    public ItemStack getItemStack() {
        if (itemStack == null) {
            itemStack = itemStackTemplate != null ? itemStackTemplate.create() : Items.BARRIER.getDefaultInstance();
            if (skyblockId != null) ItemUtils.setItemStackSkyblockID(itemStack, skyblockId);
        }
        return itemStack;
    }

}