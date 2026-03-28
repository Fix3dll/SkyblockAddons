package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.gson.GsonInitializable;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.Logger;

public class TexturedHead implements GsonInitializable {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    @SerializedName("profile")
    private JsonElement profile;
    @SerializedName("customName")
    private JsonElement customName;
    @SerializedName("skyblockId")
    private String skyblockId;

    @Getter
    private transient ItemStackTemplate itemStackTemplate;
    private transient ItemStack itemStack;

    @Override
    public void gsonInit() {
        makeItemStackTemplate();
    }

    private void makeItemStackTemplate() {
        try {
            itemStackTemplate = ItemUtils.createSkullTemplate(profile, customName, skyblockId);
        } catch (Exception ex) {
            itemStackTemplate = new ItemStackTemplate(Items.BARRIER);
            Object identifier = skyblockId == null ? customName : skyblockId;
            LOGGER.error(
                    "An error occurred while making an ItemStack '{}'.\n{}", identifier, ex
            );
        }
    }

    public ItemStack getItemStack() {
        if (itemStack == null) {
            itemStack = itemStackTemplate.create();
        }
        return itemStack;
    }

}