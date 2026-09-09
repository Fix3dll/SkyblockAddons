package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.gson.GsonInitializable;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

@NoArgsConstructor
public class PetItem implements GsonInitializable {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    @SerializedName("displayName")
    @Getter private String displayName = "§cNot Found!";
    @SerializedName("enchanted")
    private boolean enchanted;
    @SerializedName("material")
    private String material;
    // Some items not have a rarity in API like PET_ITEM_*_COMMON
    @SerializedName("rarity")
    @Getter private SkyblockRarity rarity = SkyblockRarity.COMMON;
    @SerializedName("resolvableProfile")
    private JsonElement resolvableProfile;
    @SerializedName("itemModel")
    private String itemModel;

    @Setter private String skyblockId;

    private transient ItemStackTemplate itemStackTemplate;
    private transient ItemStack itemStack;

    public PetItem(String displayName, boolean enchanted, String material, SkyblockRarity rarity, JsonElement resolvableProfile, String itemModel, String skyblockId) {
        this.displayName = displayName;
        this.enchanted = enchanted;
        this.material = material;
        this.rarity = rarity;
        this.resolvableProfile = resolvableProfile;
        this.itemModel = itemModel;
        this.skyblockId = skyblockId;
        makeItemStackTemplate();
    }

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

                    if (itemModelId != null) {
                        builder.set(DataComponents.ITEM_MODEL, itemModelId);
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

    /**
     * Applies {@code patch} to existing {@code itemStackTemplate}
     * @param patch {@link DataComponentPatch}
     * @return Validated {@link ItemStack} after the patch
     */
    public ItemStack applyPatch(DataComponentPatch patch) {
        if (itemStackTemplate != null) {
            return itemStack = itemStackTemplate.apply(patch);
        }
        return getItemStack();
    }

}