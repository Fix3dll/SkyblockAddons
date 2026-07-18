package com.fix3dll.skyblockaddons.utils;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.ItemType;
import com.fix3dll.skyblockaddons.core.PetInfo;
import com.fix3dll.skyblockaddons.core.Regex;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.core.SkyblockRune;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackColor;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.CompactorItem;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ContainerData;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.TexturedHead;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringUtil;
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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;

/**
 * Utility methods for Skyblock Items
 */
public class ItemUtils {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    @Getter @Setter private static Map<String, CompactorItem> compactorItems;
    @Setter private static Map<String, ContainerData> containers;
    @Setter private static Map<String, TexturedHead> texturedHeads;

    public static @NonNull ItemStack getTexturedHead(String identifier) {
        if (texturedHeads != null) {
            TexturedHead texturedHead = texturedHeads.get(identifier);
            return texturedHead == null ? Items.BARRIER.getDefaultInstance() : texturedHead.getItemStack();
        }

        return Items.BARRIER.getDefaultInstance();
    }

    /**
     * Parses the item classification (rarity, type, dungeon status) from the given Skyblock item.
     * Processes the lore list in a single pass for optimized performance.
     * @param item the Skyblock item to check, cannot be {@code null}
     * @return an {@link ItemClassification} instance containing the parsed data, or {@code null} if the item is empty or invalid
     * @since 2.2.4
     */
    public static ItemClassification getItemClassification(ItemStack item) {
        if (item == null) {
            throw new NullPointerException("The item cannot be null!");
        } else if (item == ItemStack.EMPTY) {
            return null;
        }

        return getClassificationFromLore(getItemLoreComponent(item));
    }

    /**
     * Parses the item classification from the given lore components.
     * This method is split up from the method that takes the {@code ItemStack} instance for easier unit testing.
     * @param lore the {@code List<Component>} containing the item's lore
     * @return an {@link ItemClassification} instance, or {@code null} if no valid classification line is found
     * @since 2.2.4
     */
    private static ItemClassification getClassificationFromLore(List<Component> lore) {
        if (lore == null || lore.isEmpty()) {
            return null;
        }

        // Start from the end since the target line is usually the last line or one of the last.
        for (int i = lore.size() - 1; i >= 0; i--) {
            String loreLine = lore.get(i).getString();
            if (loreLine.isBlank()) continue;

            Matcher matcher = Regex.ITEM_TYPE_AND_RARITY_PATTERN.matcher(loreLine);
            if (matcher.find()) {
                String rarityStr = matcher.group("rarity");
                if (rarityStr == null || rarityStr.isBlank()) continue;

                SkyblockRarity parsedRarity = null;
                for (SkyblockRarity itemRarity : SkyblockRarity.values()) {
                    if (itemRarity.getLoreName().equals(rarityStr)) {
                        parsedRarity = itemRarity;
                        break;
                    }
                }

                // If a valid rarity is matched, we consider this the correct classification line.
                if (parsedRarity != null) {
                    String typeStr = matcher.group("type");
                    ItemType parsedType = null;

                    if (typeStr != null && !typeStr.isBlank()) {
                        for (ItemType itemType : ItemType.values()) {
                            if (itemType.getLoreName().startsWith(typeStr)) {
                                parsedType = itemType;
                                break;
                            }
                        }
                    }

                    String dungeonStr = matcher.group("dungeon");
                    boolean isDungeon = dungeonStr != null && !dungeonStr.isBlank();

                    String idStr = matcher.group("id");
                    return new ItemClassification(parsedRarity, parsedType, isDungeon, idStr);
                }
            }
        }

        return null;
    }

    /**
     * Returns the itemstack that this personal compactor skyblock ID represents. Note that
     * a personal compactor skyblock ID is not the same as an item's regular skyblock id!
     * @param personalCompactorSkyblockID The personal compactor skyblock ID (ex. ENCHANTED_ACACIA_LOG)
     * @return The itemstack that this personal compactor skyblock ID represents
     */
    public static ItemStack getPersonalCompactorItemStack(String personalCompactorSkyblockID) {
        CompactorItem compactorItem = null;
        if (!StringUtil.isNullOrEmpty(personalCompactorSkyblockID)) {
            compactorItem = compactorItems.get(personalCompactorSkyblockID);
        }
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
     * @param skyblockID The skyblock ID of the container
     * @return A {@link ContainerData} object containing info about the container in general
     */
    public static ContainerData getContainerData(String skyblockID) {
        if (StringUtil.isNullOrEmpty(skyblockID)) return null;
        return containers.get(skyblockID);
    }

    /**
     * Returns the {@code ExtraAttributes} compound tag from the item's NBT data. The item must not be {@code null}.
     * @param item the item to get the tag from
     * @return the item's {@code ExtraAttributes} compound tag or {@code null} if the item doesn't have one
     */
    public static CompoundTag getExtraAttributes(ItemStack item) {
        if (item == null) {
            throw new NullPointerException("The item cannot be null!");
        } else if (item == ItemStack.EMPTY) {
            return null;
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
        if (itemStack == null || itemStack == ItemStack.EMPTY) return false;
        if (itemStack.is(ItemTags.PICKAXES)) return true;

        CompoundTag extraAttributes = getExtraAttributes(itemStack);
        if (extraAttributes == null) return false;

        return isDrill(extraAttributes) || "GEMSTONE_GAUNTLET".equals(getSkyblockItemID(extraAttributes));
    }

    /**
     * Checks if the given {@code ItemStack} is a drill.
     * It works by checking for the presence of the {@code drill_fuel} NBT tag, which only drills have.
     * @param itemStack the item to check
     * @return {@code true} if this item is a drill, {@code false} otherwise
     */
    public static boolean isDrill(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        } else if (itemStack == ItemStack.EMPTY) {
            return false;
        }
        return isDrill(getExtraAttributes(itemStack));
    }

    /**
     * Checks if the given {@code ItemStack} is a drill.
     * It works by checking for the presence of the {@code drill_fuel} NBT tag, which only drills have.
     * @param extraAttributes the compound to check
     * @return {@code true} if this item is a drill, {@code false} otherwise
     */
    public static boolean isDrill(CompoundTag extraAttributes) {
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
     * <br>See also {@link Regex#BACKPACK_SLOT_PATTERN}
     * @param itemStack Backpack {@link ItemStack}
     * @return returns the slot number as integer else 0
     */
    public static int getBackpackSlot(ItemStack itemStack) {
        if (itemStack == null) return 0;
        else if (itemStack == ItemStack.EMPTY) return 0;

        Matcher matcher = Regex.BACKPACK_SLOT_PATTERN.matcher(itemStack.getDisplayName().getString());
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

    public static List<Component> getItemLoreComponent(ItemStack itemStack) {
        if (itemStack == null) {
            throw new NullPointerException("Cannot get lore from null item!");
        } else if (itemStack == ItemStack.EMPTY) {
            return Collections.emptyList();
        }

        ItemLore itemLore = itemStack.get(DataComponents.LORE);
        return itemLore != null ? itemLore.lines() : Collections.emptyList();
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
        } else if (itemStack == ItemStack.EMPTY) {
            return Collections.emptyList();
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
        } else if (itemStack == ItemStack.EMPTY) {
            return false;
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
        ItemStack stack = createItemStack(Items.ENCHANTED_BOOK, name, null, false);


        CompoundTag ea = new CompoundTag();
        ea.putString("id", "ENCHANTED_BOOK");

        if (enchantName != null) {
            CompoundTag enchantments = new CompoundTag();
            enchantments.putInt(enchantName, enchantLevel);
            ea.put("enchantments", enchantments);
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(ea));

        return stack;
    }

    public static ItemStack createSkullItemStack(Identifier itemModel, JsonElement profile, JsonElement customName, String skyblockId) {
        ResolvableProfile resolvableProfile = null;
        if (profile != null) {
            resolvableProfile = ResolvableProfile.CODEC.parse(JsonOps.INSTANCE, profile).result().get();
        }

        MutableComponent component = null;
        if (customName != null) {
            component = TextUtils.componentFromJson(customName);
        }

        return createSkullItemStack(itemModel, resolvableProfile, component, skyblockId);
    }

    public static ItemStack createSkullItemStack(Identifier itemModel, ResolvableProfile profile, Component customName, String skyblockId) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

        if (itemModel != null) {
            stack.set(DataComponents.ITEM_MODEL, itemModel);
        }

        if (profile != null) {
            stack.set(DataComponents.PROFILE, profile);
        }

        if (customName != null) {
            stack.set(DataComponents.CUSTOM_NAME, customName);
        }

        if (skyblockId != null) {
            ItemUtils.setItemStackSkyblockID(stack, skyblockId);
        }

        return stack;
    }

    public static void setItemStackSkyblockID(ItemStack itemStack, String skyblockID) {
        CompoundTag ea = getExtraAttributes(itemStack);
        if (ea == null) {
            ea = new CompoundTag();
        }
        ea.putString("id", skyblockID);
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(ea));
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
        CompoundTag ea = getExtraAttributes(itemStack);
        return ea == null ? null : ea.getString("uuid").map(UUID::fromString).orElse(null);
    }

    public static boolean isQuiverArrow(ItemStack itemStack) {
        CompoundTag ea = getExtraAttributes(itemStack);
        return ea != null && ea.getBooleanOr("quiver_arrow", false);
    }

    /**
     * Constructs a {@link GameProfile} from raw Mojang skin properties.
     *
     * <p>The {@link UUID} is derived deterministically from {@code value} via
     * {@link UUID#nameUUIDFromBytes} so that repeated calls with the same texture
     * produce the same UUID, preserving Minecraft's skin cache behaviour.
     * @param value     Base64-encoded texture payload
     * @param signature Mojang RSA signature for {@code value}, may be {@code null}
     * @return a {@link GameProfile} with the {@code "textures"} property populated
     * @since 2.2.3
     */
    public static GameProfile createGameProfile(String value, String signature) {
        PropertyMap propertyMap = new PropertyMap(ImmutableMultimap.of());
        propertyMap.put("textures", new Property("textures", value, signature));

        UUID uuid = UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));

        return new GameProfile(uuid, "", propertyMap);
    }

    /**
     * @since 2.3.1
     */
    public static Component getEnchantedBookName(ItemStack itemStack) {
        if (itemStack == null || itemStack == ItemStack.EMPTY) {
            return null;
        }
        List<Component> lore = getItemLoreComponent(itemStack);
        if (lore == null || lore.isEmpty()) {
            return itemStack.getCustomName();
        }
        for (Component component : lore) {
            String line = component.getString();
            if (StringUtil.isBlank(line)) continue;

            Matcher m = Regex.ENCHANTMENT_PATTERN.matcher(line);
            if (m.find()) {
                return component;
            }
        }
        return itemStack.getCustomName();
    }

    /**
     * Represents the fundamental classification of a Skyblock item extracted from its lore.
     * Encapsulates the item's rarity, categorical type, dungeon variant status, and optional ID.
     * @param rarity    the rarity of the item, or {@code null} if not found
     * @param type      the type of the item, or {@code null} if not found
     * @param isDungeon {@code true} if the item is a dungeon variant, {@code false} otherwise
     * @param id        the item ID if present in the lore line (e.g. {@code R36}), or {@code null}
     * @since 2.2.4
     */
    public record ItemClassification(SkyblockRarity rarity, ItemType type, boolean isDungeon, String id) {

        /**
         * Checks if the item has a valid parsed type.
         * @return {@code true} if the item type is present and is not considered a default/other type.
         */
        public boolean hasValidType() {
            return type != null && type != ItemType.OTHER;
        }
    }

}