package com.fix3dll.skyblockaddons.utils;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.ItemType;
import com.fix3dll.skyblockaddons.core.PetInfo;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.core.SkyblockRune;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackColor;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.CompactorItem;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ContainerData;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.TexturedHead;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for Skyblock Items
 */
public class ItemUtils {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final Minecraft MC = Minecraft.getInstance();
    /**
     * This expression matches the line with a Skyblock item's rarity and item type that's at the end of its lore.
     * <p><i>Recombobulated Special items have exception for rarity pattern.<i/></p>
     */
    private static final Pattern ITEM_TYPE_AND_RARITY_PATTERN = Pattern.compile("§l(?<rarity>[A-Z]+(?: SPECIAL)?) ?(?<type>[A-Z ]+)?(?:§[0-9a-f]§l§ka)?(?:§8\\(ID (?<id>[A-Z]+\\d+)\\))?$");
    private static final Pattern BACKPACK_SLOT_PATTERN = Pattern.compile("Backpack Slot (?<slot>\\d+)");
    @Getter @Setter private static Object2ObjectOpenHashMap<String, CompactorItem> compactorItems;
    @Setter private static Object2ObjectOpenHashMap<String, ContainerData> containers;
    @Setter private static Object2ObjectOpenHashMap<String, TexturedHead> texturedHeads;

    public static @NonNull ItemStack getTexturedHead(String identifier) {
        if (texturedHeads != null) {
            TexturedHead texturedHead = texturedHeads.get(identifier);
            return texturedHead == null ? Items.BARRIER.getDefaultInstance() : texturedHead.getItemStack();
        }

        return Items.BARRIER.getDefaultInstance();
    }

    /**
     * Returns the rarity of a given Skyblock item. The rarity is read from the item's lore.
     * The item must not be {@code null}.
     *
     * @param item the Skyblock item to check, can't be {@code null}
     * @return the rarity of the item if a valid rarity is found, or {@code null} if item is {@code null} or no valid rarity is found
     */
    public static SkyblockRarity getRarity(ItemStack item) {
        if (item == null) {
            throw new NullPointerException("The item cannot be null!");
        }

        return getRarity(getItemLore(item));
    }

    /**
     * Returns the item type of given Skyblock item.
     * The item must not be {@code null}.
     * @param item the Skyblock item to check, can't be {@code null}
     * @return the item type of the item or {@code null} if no item type was found
     */
    public static ItemType getItemType(ItemStack item) {
        if (item == null) {
            throw new NullPointerException("The item cannot be null!");
        }

        return getType(getItemLore(item));
    }

    /**
     * Returns the itemstack that this personal compactor skyblock ID represents. Note that
     * a personal compactor skyblock ID is not the same as an item's regular skyblock id!
     *
     * @param personalCompactorSkyblockID The personal compactor skyblock ID (ex. ENCHANTED_ACACIA_LOG)
     * @return The itemstack that this personal compactor skyblock ID represents
     */
    public static ItemStack getPersonalCompactorItemStack(String personalCompactorSkyblockID) {
        CompactorItem compactorItem = compactorItems.get(personalCompactorSkyblockID);
        if (compactorItem != null) {
            return compactorItem.getItemStack();
        } else {
            ItemStack unknown = ItemUtils.createItemStack(
                    Items.BARRIER, ColorCode.GOLD + personalCompactorSkyblockID, personalCompactorSkyblockID, false
            );
            ItemUtils.setItemLore(
                    unknown,
                    Collections.singletonList(
                            Component.literal("SBA cannot found this item!").withColor(ColorCode.RED.getColor())
                    )
            );
            return unknown;
        }
    }

    /**
     * Returns data about the container that is passed in.
     *
     * @param skyblockID The skyblock ID of the container
     * @return A {@link ContainerData} object containing info about the container in general
     */
    public static ContainerData getContainerData(String skyblockID) {
        return containers.get(skyblockID);
    }

    /**
     * Returns the {@code ExtraAttributes} compound tag from the item's NBT data. The item must not be {@code null}.
     *
     * @param item the item to get the tag from
     * @return the item's {@code ExtraAttributes} compound tag or {@code null} if the item doesn't have one
     */
    public static CompoundTag getExtraAttributes(ItemStack item) {
        if (item == null) {
            throw new NullPointerException("The item cannot be null!");
        }
        CustomData customData = item.get(DataComponents.CUSTOM_DATA);
        return customData == null ? null : customData.copyTag();
    }

    /**
     * Returns the {@code enchantments} compound tag from the itemStack's data.
     * @param itemStack the itemStack to get the tag from
     * @return {@code enchantments} tag as map or {@code null} if the itemStack doesn't have one
     */
    public static Map<String, Integer> getEnchantments(ItemStack itemStack) {
        CompoundTag extraAttributes = getExtraAttributes(itemStack);
        return extraAttributes == null ? Collections.emptyMap() : getEnchantments(extraAttributes);
    }

    /**
     * Returns the {@code enchantments} compound tag from the extraAttributes tag.
     * @param extraAttributes the extraAttributes tag
     * @return {@code enchantments} tag as map or {@code null} if the extraAttributes doesn't have one
     */
    public static Map<String, Integer> getEnchantments(CompoundTag extraAttributes) {
        if (extraAttributes == null) return Collections.emptyMap();

        HashMap<String, Integer> enchantmentsMap = new HashMap<>();
        extraAttributes.getCompound("enchantments").ifPresent(enchantments -> enchantments
                .entrySet().forEach(entry ->
                        enchantmentsMap.put(entry.getKey(), entry.getValue().asInt().orElse(0))
            )
        );

        return enchantmentsMap;
    }

    /**
     * Returns the {@code attributes} compound tag from the itemStack's data.
     * @param itemStack the itemStack to get the tag from
     * @return {@code attributes} tag as map or {@code null} if the itemStack doesn't have one
     */
    public static Map<String, Integer> getAttributes(ItemStack itemStack) {
        CompoundTag extraAttributes = getExtraAttributes(itemStack);
        return extraAttributes == null ? Collections.emptyMap() : getAttributes(extraAttributes);
    }

    /**
     * Returns the {@code attributes} compound tag from the extraAttributes tag.
     * @param extraAttributes the extraAttributes tag
     * @return {@code attributes} tag as map or {@code null} if the extraAttributes doesn't have one
     */
    public static Map<String, Integer> getAttributes(CompoundTag extraAttributes) {
        if (extraAttributes == null) return Collections.emptyMap();

        HashMap<String, Integer> attributesMap = new HashMap<>();
        extraAttributes.getCompound("attributes").ifPresent(attributes -> attributes
                .entrySet().forEach(entry ->
                        attributesMap.put(entry.getKey(), entry.getValue().asInt().orElse(0))
                )
        );

        return attributesMap;
    }

    /**
     * @param itemStack the item to check
     * @return The Skyblock reforge of a given itemstack
     */
    public static String getReforge(ItemStack itemStack) {
        CompoundTag extraAttributes = getExtraAttributes(itemStack);
        if (extraAttributes == null) return null;

        Optional<String> modifier = extraAttributes.getString("modifier");

        if (modifier.isPresent()) {
            String reforge = WordUtils.capitalizeFully(modifier.get());

            reforge = reforge.replace("_sword", ""); //fixes reforges like "Odd_sword"
            reforge = reforge.replace("_bow", "");
            reforge = reforge.replace("Warped", "Hyper"); // exception

            return reforge;
        } else {
            return null;
        }
    }

    /**
     * Checks if the given item is a material meant to be used in a crafting recipe. Dragon fragments are an example
     * since they are used to make dragon armor.
     *
     * @param itemStack the item to check
     * @return {@code true} if this item is a material, {@code false} otherwise
     */
    public static boolean isMaterialForRecipe(ItemStack itemStack) {
        List<String> lore = ItemUtils.getItemLore(itemStack);
        for (String loreLine : lore) {
            if ("Right-click to view recipes!".equals(TextUtils.stripColor(loreLine))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given item is a mining tool (pickaxe or drill).
     *
     * @param itemStack the item to check
     * @return {@code true} if this item is a pickaxe/drill, {@code false} otherwise
     */
    public static boolean isMiningTool(ItemStack itemStack) {
        // TEST 1.21.5
        return itemStack.is(ItemTags.PICKAXES) || isDrill(itemStack);
    }

    /**
     * Checks if the given {@code ItemStack} is a drill. It works by checking for the presence of the {@code drill_fuel} NBT tag,
     * which only drills have.
     *
     * @param itemStack the item to check
     * @return {@code true} if this item is a drill, {@code false} otherwise
     */
    public static boolean isDrill(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        CompoundTag extraAttributes = getExtraAttributes(itemStack);

        return extraAttributes != null && extraAttributes.contains("drill_fuel");
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock item
     * @param itemStack the Skyblock item to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock item
     */
    public static String getSkyblockItemID(ItemStack itemStack) {
        CompoundTag extraAttributes = getExtraAttributes(itemStack);
        return getSkyblockItemID(extraAttributes);
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock item
     * @param extraAttributes the CustomData to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock NBT
     */
    public static String getSkyblockItemID(CompoundTag extraAttributes) {
        return extraAttributes == null ? null : extraAttributes.getString("id").orElse(null);
    }

    /**
     * Gets the color of the backpack in the given {@code ItemStack}
     * @param stack the {@code ItemStack} containing the backpack
     * @return The color of the backpack; or {@code WHITE} if there is no color
     */
    public static BackpackColor getBackpackColor(ItemStack stack) {
        CompoundTag extraAttributes = getExtraAttributes(stack);

        if (extraAttributes != null) {
            Optional<String> backpack_color = extraAttributes.getString("backpack_color");

            if (backpack_color.isPresent()) {
                try {
                    return BackpackColor.valueOf(backpack_color.get());
                } catch (IllegalArgumentException ignored) {}
            }
        }

        return BackpackColor.DEFAULT;
    }

    /**
     * Gets slot number from the {@link ItemStack} on the slot.
     * <br>See also {@link ItemUtils#BACKPACK_SLOT_PATTERN}
     * @param itemStack Backpack {@link ItemStack}
     * @return returns the slot number as integer else 0
     */
    public static int getBackpackSlot(ItemStack itemStack) {
        if (itemStack == null) return 0;

        Matcher matcher = BACKPACK_SLOT_PATTERN.matcher(itemStack.getDisplayName().getString());
        if (matcher.find()) {
            return Integer.parseInt(matcher.group("slot"));
        }

        return 0;
    }

    /**
     * Returns a {@link SkyblockRune} from the ExtraAttributes Skyblock data
     * This can ge retrieved from a rune itself or an infused item
     *
     * @param extraAttributes the Skyblock Data to check
     * @return A {@link SkyblockRune} or {@code null} if it doesn't have it
     */
    public static SkyblockRune getRuneData(CompoundTag extraAttributes) {
        if (extraAttributes != null) {
            HashMap<String, Integer> runesMap = new HashMap<>();
            extraAttributes.getCompound("runes").ifPresent(runes -> runes
                    .entrySet().forEach(entry ->
                            runesMap.put(entry.getKey(), entry.getValue().asInt().orElse(0))
                    )
            );
            return new SkyblockRune(runesMap);
        }

        return null;
    }

    /**
     * Returns a {@link PetInfo} from the {@link ItemStack}
     * @param itemStack the {@link ItemStack} to check
     * @return A {@link PetInfo} or {@code null} if it isn't a pet
     */
    public static PetInfo getPetInfo(ItemStack itemStack) {
        CompoundTag extraAttributes = getExtraAttributes(itemStack);
        return getPetInfo(extraAttributes);
    }


    /**
     * Returns a {@link PetInfo} from the ExtraAttributes Skyblock data
     * @param extraAttributes the Skyblock Data to check
     * @return A {@link PetInfo} or {@code null} if it isn't a pet
     */
    public static PetInfo getPetInfo(CompoundTag extraAttributes) {
        if (extraAttributes == null) return null;
        return extraAttributes.getString("petInfo")
                .map(str -> SkyblockAddons.getGson().fromJson(str, PetInfo.class))
                .orElse(null);

    }

    /**
     * Returns a string list containing the NBT lore of an {@code ItemStack}, or
     * an empty list if this item doesn't have a lore tag.
     * The itemStack argument must not be {@code null}. The returned lore list is unmodifiable since it has been
     * converted from an {@code NBTTagList}.
     *
     * @param itemStack the ItemStack to get the lore from
     * @return the lore of an ItemStack as a string list
     */
    public static List<String> getItemLore(ItemStack itemStack) {
        if (itemStack == null) {
            throw new NullPointerException("Cannot get lore from null item!");
        }

        ItemLore itemLore = itemStack.get(DataComponents.LORE);
        List<Component> lore;
        if (itemLore != null) {
            lore = itemLore.lines();
        } else {
            return Collections.emptyList();
        }

        return lore.stream().map(component -> TextUtils.getFormattedText(component, true)).toList();
    }

    /**
     * Sets the lore text of a given {@code ItemStack}.
     * @param itemStack the {@code ItemStack} to set the lore for
     * @param lore the new lore
     */
    public static void setItemLore(ItemStack itemStack, List<Component> lore) {
        itemStack.set(DataComponents.LORE, new ItemLore(lore));
    }

    /**
     * Check if the given {@code ItemStack} is an item shown in a menu as a preview or placeholder
     * (e.g. items in the recipe book).
     *
     * @param itemStack the {@code ItemStack} to check
     * @return {@code true} if {@code itemStack} is an item shown in a menu as a preview or placeholder, {@code false} otherwise
     */
    public static boolean isMenuItem(ItemStack itemStack) {
        if (itemStack == null) {
            throw new NullPointerException("Item stack cannot be null!");
        }
        CompoundTag extraAttributes = getExtraAttributes(itemStack);

        // If this item stack is a menu item, it won't have this key.
        return extraAttributes != null && !extraAttributes.contains("uuid");
    }

    /**
     * Creates a new {@code ItemStack} instance with the given item and a fake enchantment to enable the enchanted "glint"
     * effect if {@code enchanted} is true. This method should be used when you want to create a bare-bones {@code ItemStack}
     * to render as part of a GUI.
     *
     * @param item the {@code Item} the created {@code ItemStack} should be
     * @param enchanted the item has the enchanted "glint" effect enabled if {@code true}, disabled if {@code false}
     * @return a new {@code ItemStack} instance with the given item and a fake enchantment if applicable
     */
    public static ItemStack createItemStack(Item item, boolean enchanted) {
        return createItemStack(item, null, null, enchanted);
    }

    public static ItemStack createItemStack(Item item, String name, String skyblockID, boolean enchanted) {
        ItemStack stack = item.getDefaultInstance();

        if (name != null) {
            stack.set(DataComponents.ITEM_NAME, Component.literal(name));
        }

        if (enchanted) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        if (skyblockID != null) {
            setItemStackSkyblockID(stack, skyblockID);
        }

        return stack;
    }

    public static ItemStack createEnchantedBook(SkyblockRarity rarity, String enchantName, int enchantLevel) {
        String name = rarity == null ? "Enchanted Book" : rarity.getColorCode() + "Enchanted Book";
        ItemStack stack = createItemStack(Items.ENCHANTED_BOOK, name, "ENCHANTED_BOOK", false);

        CompoundTag enchantments = new CompoundTag();
        enchantments.putInt(enchantName, enchantLevel);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(enchantments));

        return stack;
    }

    public static ItemStack createSkullItemStack(@NonNull JsonElement profile, JsonElement customName, String skyblockId) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

        ResolvableProfile.CODEC.parse(JsonOps.INSTANCE, profile).result().ifPresent(
                resolvableProfile -> stack.set(DataComponents.PROFILE, resolvableProfile)
        );

        MutableComponent component = null;
        if (customName != null) {
            component = TextUtils.componentFromJson(customName);
        }
        if (component != null) {
            stack.set(DataComponents.CUSTOM_NAME, component);
        }

        if (skyblockId != null) {
            ItemUtils.setItemStackSkyblockID(stack, skyblockId);
        }

        return stack;
    }

    public static void setItemStackSkyblockID(ItemStack itemStack, String skyblockID) {
        CompoundTag ea = getExtraAttributes(itemStack);
        CustomData customData;
        if (ea != null) {
            ea.putString("id", skyblockID);
            customData = CustomData.of(ea);
        } else {
            customData = CustomData.of(new CompoundTag());
        }

        itemStack.set(DataComponents.CUSTOM_DATA, customData);
    }

    /**
     * Given a skull ItemStack, returns the skull owner ID, or null if it doesn't exist.
     */
    public static String getSkullOwnerID(ItemStack skull) {
        if (skull == null) {
            return null;
        }

        ResolvableProfile profile = skull.get(DataComponents.PROFILE);

        if (profile != null) {
            return profile.partialProfile().id().toString();
        }

        return null;
    }

    /**
     * Given a skull ItemStack, returns the texture, or null if it doesn't exist.
     */
    public static String getSkullTexture(ItemStack skull) {
        if (skull == null) {
            return null;
        }

        ResolvableProfile profile = skull.get(DataComponents.PROFILE);

        if (profile != null) {
            GameProfile gameProfile = profile.partialProfile();
            Iterator<Property> textures = gameProfile.properties().get("textures").iterator();

            if (textures.hasNext()) {
                return textures.next().value();
            }
        }

        return null;
    }

    public static ByteArrayTag getCompressedNBT(ItemStack[] items) {
        // Add each item's nbt to a tag list
        ListTag list = new ListTag();
        for (ItemStack item : items) {
            if (item == null || item == ItemStack.EMPTY) {
                list.add(new CompoundTag()/*ItemStack.EMPTY.save(world.registryAccess())*/);
            } else {
                Tag listTag = encodeItemStack(item);
                list.add(listTag);
            }
        }

        // Append standard "i" tag for compression
        CompoundTag nbt = new CompoundTag();
        nbt.put("i", list);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (stream) {
            NbtIo.writeCompressed(nbt, stream);
        } catch (IOException e) {
            return null;
        }
        return new ByteArrayTag(stream.toByteArray());
    }

    public static Tag encodeItemStack(ItemStack itemStack) {
        RegistryAccess registryAccess = Utils.registryAccess();

        return ItemStack.CODEC.encodeStart(
                registryAccess.createSerializationContext(NbtOps.INSTANCE), itemStack
        ).getOrThrow();
    }

    public static Optional<ItemStack> parseTag(Tag tag) {
        RegistryAccess registryAccess = Utils.registryAccess();

        return ItemStack.CODEC.parse(
                registryAccess.createSerializationContext(NbtOps.INSTANCE), tag
        ).resultOrPartial(string -> LOGGER.error("Tried to load invalid item: '{}'", string));
    }

    /**
     * Returns the integer thunder charge amount of Thunder Bottle
     * @param bottle Empty Thunder Bottle ItemStack
     * @return thunder charge amount
     */
    public static int getThunderCharge(ItemStack bottle) {
        CompoundTag ea = getExtraAttributes(bottle);
        return ea == null ? 0 : ea.getIntOr("thunder_charge", 0);
    }

    public static UUID getUuid(ItemStack itemStack) {
        CompoundTag extraAttributes = getExtraAttributes(itemStack);
        return extraAttributes == null ? null : extraAttributes.getString("uuid").map(UUID::fromString).orElse(null);
    }

    public static boolean isQuiverArrow(ItemStack itemStack) {
        CompoundTag ea = getExtraAttributes(itemStack);
        return ea != null && ea.getBooleanOr("quiver_arrow", false);
    }

    /**
     * Returns the rarity of a Skyblock item given its lore. This method takes the item's lore as a string list as input.
     * This method is split up from the method that takes the {@code ItemStack} instance for easier unit testing.
     *
     * @param lore the {@code List<String>} containing the item's lore
     * @return the rarity of the item if a valid rarity is found, or {@code null} if item is {@code null} or no valid rarity is found
     */
    private static SkyblockRarity getRarity(List<String> lore) {
        // Start from the end since the rarity is usually the last line or one of the last.
        for (int i = lore.size() - 1; i >= 0 ; i--) {
            String currentLine = lore.get(i);

            Matcher rarityMatcher = ITEM_TYPE_AND_RARITY_PATTERN.matcher(currentLine);
            if (rarityMatcher.find()) {
                String rarity = rarityMatcher.group("rarity");

                for (SkyblockRarity itemRarity : SkyblockRarity.values()) {
                    // Use a "startsWith" check here because "VERY SPECIAL" has two words and only "VERY" is matched.
                    if (itemRarity.getLoreName().startsWith(rarity)) {
                        return itemRarity;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns the item type of a Skyblock item given its lore. This method takes the item's lore as a string list as input.
     * This method is split up from the method that takes the {@code ItemStack} instance for easier unit testing.
     *
     * @param lore the {@code List<String>} containing the item's lore
     * @return the rarity of the item if a valid rarity is found, or {@code null} if item is {@code null} or no valid rarity is found
     */
    private static ItemType getType(List<String> lore) {
        // Start from the end since the rarity is usually the last line or one of the last.
        for (int i = lore.size() - 1; i >= 0; i--) {
            String currentLine = lore.get(i);

            Matcher itemTypeMatcher = ITEM_TYPE_AND_RARITY_PATTERN.matcher(currentLine);
            if (itemTypeMatcher.find()) {
                String type = itemTypeMatcher.group("type");

                if (type != null) {
                    type = type.trim();
                    for (ItemType itemType : ItemType.values()) {
                        if (itemType.getLoreName().startsWith(type)) {
                            return itemType;
                        }
                    }
                }
            }
        }

        return null;
    }

}