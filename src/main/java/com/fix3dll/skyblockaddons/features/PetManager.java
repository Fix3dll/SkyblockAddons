package com.fix3dll.skyblockaddons.features;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.PetInfo;
import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.features.backpacks.CompressedStorage;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.PetItem;
import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetManager {

    private static final Pattern PET_LEVEL_PATTERN = Pattern.compile("(§7\\[Lvl )(?<level>\\d+)(] )(§8\\[§.)?(?<cosmeticLevel>\\d+)?(.*)");
    private static final Pattern FAVORITE_PATTERN = Pattern.compile("(?i)(§r)?§e⭐ ");

    /** The PetManager instance.*/
    @Getter private static final PetManager instance = new PetManager();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    @Setter private static Map<String, PetItem> petItems;

    @Getter @Setter private volatile boolean cacheDirty = false;
    @Setter private volatile boolean updatePetCache = false;
    private int previousPage = -1;

    /**
     * Inspired by NEU
     * @author Fix3dll
     */
    public void checkCurrentPet(Minecraft mc) {
        if (!main.getUtils().isOnSkyblock()) return;

        InventoryType inventoryType = main.getInventoryUtils().getInventoryType();
        if (inventoryType == InventoryType.PETS && mc.screen instanceof ContainerScreen containerScreen) {
            int page = main.getInventoryUtils().getInventoryPageNum();
            if (page != previousPage) {
                updatePetCache = true;
            }
            if (!updatePetCache) return;
            NonNullList<ItemStack> inventory = containerScreen.getMenu().getItems();

            // Pets menu size not lower than 54 slot
            if (inventory.size() < 54) return;

            previousPage = page;
            // For unique index slot: add 45 slot = 5 row for each page except first page
            // If page == 0, there is no page indicator in the title, there is only 1 pet page.
            int pageOffset = (45 * (page == 0 ? 0 : page - 1));

            // Ignore first and last row
            for (int i = 10; i < 54 - 10; i++) {
                ItemStack item = inventory.get(i);
                if (item == ItemStack.EMPTY || !item.is(Items.PLAYER_HEAD)) continue;

                ItemStack itemCopy = item.copy();
                Pet newPet = getPetFromItemStack(itemCopy);
                if (newPet == null) continue;

                int sbaPetIndex = i + pageOffset;
                Pet oldPet = main.getPetCacheManager().getPet(sbaPetIndex);

                if (oldPet != null && oldPet.equals(newPet)) {
                    if (newPet.petInfo.isActive() && syncActivePetEquipment(sbaPetIndex, itemCopy)) {
                        cacheDirty = true;
                    }
                    continue;
                }

                newPet.compressItem();

                if (newPet.petInfo.isActive()) {
                    syncActivePetEquipment(sbaPetIndex, itemCopy);
                }

                main.getPetCacheManager().putPet(sbaPetIndex, newPet);
                cacheDirty = true;
            }
            updatePetCache = false;
        } else {
            updatePetCache = true;
            previousPage = -1;
        }
    }

    /**
     * Registers the given index as the current pet and synchronizes its ItemStack with
     * {@link SkyblockEquipment#PET} if the stack has changed.
     * @return {@code true} if the equipment stack was updated
     */
    private boolean syncActivePetEquipment(int sbaPetIndex, ItemStack itemCopy) {
        main.getPetCacheManager().getData().setCurrentPetIdx(sbaPetIndex);
        if (!ItemStack.matches(itemCopy, SkyblockEquipment.PET.getItemStack())) {
            SkyblockEquipment.PET.setItemStack(itemCopy);
            return true;
        }
        return false;
    }

    /**
     * When Autopet messages came to chat it will trigger {@code PlayerListener.AUTOPET_PATTERN}
     * We will get groups from that Pattern, and we will set current pet from these groups values.
     * @param levelString level string
     * @param rarityColor rarity color string
     * @param petName petName string
     */
    public void findCurrentPetFromAutopet(String levelString, String rarityColor, String petName) {
        int level = Integer.parseInt(levelString);
        ColorCode color = ColorCode.getByChar(rarityColor.charAt(0));
        SkyblockRarity rarity = SkyblockRarity.getByColorCode(color);

        var iterator = main.getPetCacheManager().getData().getPetMap().int2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Int2ObjectMap.Entry<Pet> entry = iterator.next();
            int index = entry.getIntKey();
            Pet pet = entry.getValue();

            if (TextUtils.stripPetName(pet.displayName).equals(petName)
                    && pet.petLevel == level
                    && pet.petInfo.getPetRarity() == rarity) {
                main.getPetCacheManager().setCurrentPetIndex(index);
            }
        }
    }

    /**
     * When levelled up messages came to chat it will trigger {@code PlayerListener.PET_LEVELED_UP_PATTERN}
     * We will get groups from that Pattern, and we will update petCache and set current pet from these groups values.
     * @param newLevel pet's new level as an integer
     * @param rarityColor rarity color string
     * @param petName petName string
     */
    public void updateAndSetCurrentLevelledPet(int newLevel, String rarityColor, String petName) {
        ColorCode color = ColorCode.getByChar(rarityColor.charAt(0));
        SkyblockRarity rarity = SkyblockRarity.getByColorCode(color);
        Pet currentPet = main.getPetCacheManager().getCurrentPet();

        var iterator = main.getPetCacheManager().getData().getPetMap().int2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Int2ObjectMap.Entry<Pet> entry = iterator.next();
            int index = entry.getIntKey();
            Pet pet = entry.getValue();

            if (TextUtils.stripPetName(pet.displayName).equals(petName) && pet.petInfo.getPetRarity() == rarity) {
                Matcher m = PET_LEVEL_PATTERN.matcher(pet.displayName);
                if (m.matches()) {
                    boolean isCurrentPet = currentPet != null && currentPet.petInfo.getUniqueId() == pet.petInfo.getUniqueId();
                    String cosmeticLevelGroup = m.group("cosmeticLevel");

                    if (pet.petLevel < newLevel) {
                        if (cosmeticLevelGroup != null) {
                            int cosmeticLevel = newLevel - pet.petLevel;
                            pet.displayName = m.group(1) + m.group(2) + m.group(3) + m.group(4) + cosmeticLevel + m.group(6);
                        } else {
                            pet.petLevel = newLevel;
                            pet.displayName = m.group(1) + newLevel + m.group(3) + m.group(6);
                        }
                    } else {
                        continue;
                    }

                    main.getPetCacheManager().putPet(index, pet);
                    if (isCurrentPet) {
                        main.getPetCacheManager().setCurrentPetIndex(index);
                    } else {
                        main.getPetCacheManager().saveValues();
                    }
                }
            }
        }
    }

    public void updatePetItem(String rarityColor, String petItem) {
        String petItemId = getPetIdFromDisplayName("§" + rarityColor + petItem);
        if (petItemId == null) return;
        Pet currentPet = main.getPetCacheManager().getCurrentPet();
        if (currentPet == null) return;

        var iterator = main.getPetCacheManager().getData().getPetMap().int2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Int2ObjectMap.Entry<Pet> entry = iterator.next();
            int index = entry.getIntKey();
            Pet pet = entry.getValue();

            if (pet.petInfo.getUniqueId() == currentPet.petInfo.getUniqueId()) {
                pet.petInfo.setHeldItemId(petItemId);
                main.getPetCacheManager().putPet(index, pet);
                main.getPetCacheManager().setCurrentPetIndex(index);
            }
        }
    }

    /**
     * Parses the petInfo in the pet's ExtraAttributes to JsonObject after than converts to {@link Pet}
     * @param itemStack The pet ItemStack
     * @see PetInfo
     * @author Fix3dll
     */
    public Pet getPetFromItemStack(ItemStack itemStack) {
        String displayName;
        if (itemStack.getCustomName() != null) {
            displayName = FAVORITE_PATTERN.matcher(
                    TextUtils.getFormattedText(itemStack.getCustomName(), true)
            ).replaceAll("");
        } else {
            return null;
        }

        int petLevel = TextUtils.getPetLevelFromDisplayName(displayName);
        if (petLevel == -1) return null;

        PetInfo petInfo = ItemUtils.getPetInfo(itemStack);
        return petInfo != null ? new Pet(itemStack, displayName, petLevel, petInfo) : null;
    }

    public ItemStack getPetItemFromId(String petItemId) {
        PetItem petItem = getPetItemById(petItemId);
        return petItem != null ? petItem.getItemStack(): Blocks.STONE.asItem().getDefaultInstance();
    }

    public String getPetItemDisplayNameFromId(String petItemId) {
        PetItem petItem = getPetItemById(petItemId);
        return petItem != null ? petItem.getDisplayName() : "§cNot Found!";
    }

    public SkyblockRarity getPetItemRarityFromId(String petItemId) {
        PetItem petItem = getPetItemById(petItemId);
        return petItem != null ? petItem.getRarity() : SkyblockRarity.ADMIN;
    }

    public PetItem getPetItemById(String petItemId) {
        if (StringUtil.isNullOrEmpty(petItemId)) return null;
        return petItems.get(petItemId);
    }

    public String getPetIdFromDisplayName(String petItemDisplayName) {
        for (Map.Entry<String, PetItem> petItem : petItems.entrySet()) {
            if (petItem.getValue().getDisplayName().equals(petItemDisplayName)) {
                return petItem.getKey();
            }
        }
        return null;
    }

    @SuppressWarnings("FieldMayBeFinal")
    @Getter @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class Pet {
        @EqualsAndHashCode.Include private String displayName;
        @EqualsAndHashCode.Include private int petLevel;
        @EqualsAndHashCode.Include private PetInfo petInfo;
        private CompressedStorage compressedStorage = new CompressedStorage(); // compressed ItemStack
        @Expose(serialize = false, deserialize = false)
        private transient ItemStack itemStack;

        public Pet(ItemStack stack, String displayName, int petLevel, PetInfo petInfo) {
            this.displayName = displayName;
            this.petLevel = petLevel;
            this.petInfo = petInfo;
            this.itemStack = stack;
        }

        /**
         * Serializes {@link #itemStack} into {@link #compressedStorage} for persistent storage.
         * Must be called before the pet is persisted, as {@link #itemStack} is transient and
         * will not survive serialization. Has no effect if the storage is already populated.
         */
        public void compressItem() {
            byte[] storage = this.compressedStorage.getStorage();
            if (storage == null || storage.length == 0) {
                this.compressedStorage.setStorage(ItemUtils.getCompressedNBT(new ItemStack[]{this.itemStack}).getAsByteArray());
            }
        }

        /**
         * Returns the pet's {@link ItemStack}, deserializing it from {@link #compressedStorage}
         * if the transient field is absent, such as after the pet is loaded from the pet cache.
         * Returns {@code null} if both {@link #itemStack} is absent and {@link #compressedStorage} is empty.
         */
        public ItemStack getItemStack() {
            if (this.itemStack == null && this.compressedStorage.getStorage().length != 0) {
                List<ItemStack> list = ContainerPreviewManager.decompressItems(this.compressedStorage.getStorage());
                if (!list.isEmpty()) {
                    this.itemStack = list.getFirst();
                }
            }
            return this.itemStack;
        }
    }

}