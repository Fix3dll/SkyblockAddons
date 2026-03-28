package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.gson.GsonInitializable;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.Setter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.Logger;

/**
 * For storing any skyblock item. Very much a work in progress, with other things (like pets perhaps) on the way
 * Another potential addition is prevented placing enchanted items (blacklist + whitelist), item cooldown amounts, etc.
 */
public class CompactorItem implements GsonInitializable {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    @SerializedName("material")
    private String material;
    @SerializedName("displayName")
    private JsonElement displayName;
    @SerializedName("enchanted")
    private boolean enchanted;
    @SerializedName("resolvableProfile")
    private JsonElement resolvableProfile;

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
                if (material.equals("skull_item")) {
                    itemStackTemplate = ItemUtils.createSkullTemplate(resolvableProfile, displayName, null);
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

                    if (displayName != null) {
                        MutableComponent component = TextUtils.componentFromJson(displayName);
                        if (component != null) {
                            builder.set(DataComponents.CUSTOM_NAME, component);
                        }
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