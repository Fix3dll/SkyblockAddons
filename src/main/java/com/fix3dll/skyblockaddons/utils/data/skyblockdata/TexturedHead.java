package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.gson.GsonInitializable;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.minecraft.world.item.ItemStack;
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

    @Getter private transient ItemStack itemStack;

    @Override
    public void gsonInit() {
        makeItemStack();
    }

    private void makeItemStack() {
        try {
            itemStack = ItemUtils.createSkullItemStack(profile, customName, skyblockId);
        } catch (Exception ex) {
            itemStack = Items.BARRIER.getDefaultInstance();
            LOGGER.error(
                    "An error occurred while making an ItemStack with ID {}.\n{}", skyblockId, ex
            );
        }
    }
}