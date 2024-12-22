package codes.biscuit.skyblockaddons.utils.data.skyblockdata;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.SkyblockRarity;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.gson.GsonInitializable;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
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
    @SerializedName("texture")
    private String texture;
    @SerializedName("skullId")
    private String skullId;

    @Getter private transient ItemStack itemStack;

    @Override
    public void gsonInit() {
        makeItemStack();
    }

    private void makeItemStack() {
        try {
            if (material != null) {
                if (material.equals("skull_item")) {
                    itemStack = ItemUtils.createSkullItemStack(null, null, skullId, texture);
                } else {
                    String itemId;
                    int meta = 0;
                    if (material.contains(":")) {
                        int index = material.indexOf(":");
                        meta = Integer.parseInt(material.substring(index + 1));
                        itemId = material.substring(0, index);
                    } else {
                        itemId = material;
                    }

                    Item item = Item.getByNameOrId(itemId);
                    if (item == null) {
                        item = Item.getItemFromBlock(Block.getBlockFromName(itemId));
                    }

                    if (item != null) {
                        itemStack = meta > 0 ? new ItemStack(item, 1, meta) : new ItemStack(item);
                        if (enchanted) {
                            itemStack.setTagInfo("ench", new NBTTagList());
                        }
                    } else {
                        // Item not found
                        itemStack = new ItemStack(Item.getItemFromBlock(Blocks.stone));
                    }
                }
            }
        } catch (Exception ex) {
            itemStack = new ItemStack(Item.getItemFromBlock(Blocks.stone));
            LOGGER.error(
                    "An error occurred while making an item stack with ID {} and name {}.", material, displayName, ex
            );
        }
    }
}
