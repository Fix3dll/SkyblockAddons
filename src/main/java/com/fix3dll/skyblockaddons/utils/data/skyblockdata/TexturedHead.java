package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.gson.GsonInitializable;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor
public class TexturedHead implements GsonInitializable {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    @SerializedName("profile")
    private JsonElement profile;
    @SerializedName("customName")
    private JsonElement customName;
    @SerializedName("skyblockId")
    private String skyblockId;
    @SerializedName("itemModel")
    private String itemModel;

    @Getter
    private transient ItemStackTemplate itemStackTemplate;
    private transient ItemStack itemStack;
    /**
     * Tracks whether this instance has already been reconciled against the remote API.
     * Prevents redundant lookups and object allocations on subsequent render passes.
     */
    @Getter private transient volatile boolean apiChecked = false;

    public TexturedHead(ItemsData.@NonNull Item item) {
        skyblockId = item.getId();
        itemModel = item.getItemModel();

        ItemsData.Skin skin = item.getSkin();
        GameProfile gameProfile = skin != null ? skin.getGameProfile() : null;

        if (gameProfile != null) {
            itemStackTemplate = new ItemStackTemplate(Items.PLAYER_HEAD);
            CompoundTag extraAttributes = new CompoundTag();
            extraAttributes.putString("id", item.getId());
            itemStack = itemStackTemplate.apply(DataComponentPatch.builder()
                    .set(DataComponents.PROFILE, ResolvableProfile.createResolved(gameProfile))
                    .set(DataComponents.CUSTOM_DATA, CustomData.of(extraAttributes))
                    .set(DataComponents.CUSTOM_NAME, createCustomName(item))
                    .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, item.isGlowing())
                    .build());
            this.apiChecked = true;
        } else if (this.itemModel != null) {
            Identifier modelId = Identifier.tryParse(this.itemModel);

            if (modelId != null) {
                itemStackTemplate = new ItemStackTemplate(Items.PAPER);
                CompoundTag extraAttributes = new CompoundTag();
                extraAttributes.putString("id", item.getId());
                itemStack = itemStackTemplate.apply(DataComponentPatch.builder()
                        .set(DataComponents.ITEM_MODEL, modelId)
                        .set(DataComponents.CUSTOM_DATA, CustomData.of(extraAttributes))
                        .set(DataComponents.CUSTOM_NAME, createCustomName(item))
                        .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, item.isGlowing())
                        .build());
                this.apiChecked = true;
            }
        }
    }

    @Override
    public void gsonInit() {
        makeItemStackTemplate();
    }

    private void makeItemStackTemplate() {
        try {
            Identifier itemModelId = itemModel == null ? null : Identifier.parse(itemModel);
            itemStackTemplate = ItemUtils.createSkullTemplate(itemModelId, profile, customName, skyblockId);
        } catch (Exception ex) {
            itemStackTemplate = new ItemStackTemplate(Items.BARRIER);
            Object identifier = skyblockId == null ? customName : skyblockId;
            LOGGER.error("An error occurred while making an ItemStack '{}'.\n{}", identifier, ex);
        }
    }

    private MutableComponent createCustomName(ItemsData.@NonNull Item item) {
        String name = item.getName();
        SkyblockRarity rarity = item.getTier();
        MutableComponent customNameComponent = name == null ? Component.empty() : Component.literal(name);

        if (rarity != null) {
            ColorCode colorCode = rarity.getColorCode();
            if (colorCode != null) {
                customNameComponent.withColor(colorCode.getColor());
            }
        }

        return customNameComponent;
    }

    public @Nullable ItemStack getItemStack() {
        if (this.itemStack == null && this.itemStackTemplate != null) {
            this.itemStack = this.itemStackTemplate.create();
        }
        return this.itemStack;
    }

    public TexturedHead setApiChecked(boolean value) {
        this.apiChecked = value;
        return this;
    }

}