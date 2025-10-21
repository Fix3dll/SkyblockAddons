package com.fix3dll.skyblockaddons.features;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.PetInfo;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.features.backpacks.CompressedStorage;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.PetItem;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetManager {
    //private static final Pattern SELECTED_PET_PATTERN = Pattern.compile("(?:§.)*Selected pet: §(?<rarity>\\w)(?<pet>[\\w ]+)");
    private static final Pattern PET_LEVEL_PATTERN = Pattern.compile("(§7\\[Lvl )(?<level>\\d+)(] )(§8\\[§.)?(?<cosmeticLevel>\\d+)?(.*)");
    private static final Pattern FAVORITE_PATTERN = Pattern.compile("(?i)(§r)?§e⭐ ");

    /** The PetManager instance.*/
    @Getter private static final PetManager instance = new PetManager();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    @Setter private static Map<String, PetItem> petItems;

    /**
     * Inspired by NEU
     * @author Fix3dll
     */
    public void checkCurrentPet(Minecraft mc) {
        if (!main.getUtils().isOnSkyblock()) return;

        boolean petsUpdated = false;
        if (main.getInventoryUtils().getInventoryType() == InventoryType.PETS && mc.screen instanceof ContainerScreen containerScreen) {
            NonNullList<ItemStack> inventory = containerScreen.getMenu().getItems();

            // Pets menu size not lower than 54 slot
            if (inventory.size() < 54) return;
//            ItemStack petMenuBone = lower.getStackInSlot(4);
//            List<String> lore = ItemUtils.getItemLore(petMenuBone);
//
//            for (String line : lore) {
//                if (line.contains("Selected pet:")) {
//                    Matcher m = SELECTED_PET_PATTERN.matcher(line);
//                    if (m.find()) {
//                        String petName = m.group("pet");
//                        PetInfo.PetRarity.getRarity(m.group("rarity"));
//                    }
//                }
//            }

            int pageNum = main.getInventoryUtils().getInventoryPageNum();
            // Ignore first and last row
            for (int i = 10; i < inventory.size() - 10; i++) {
                ItemStack item = inventory.get(i);
                if (item != ItemStack.EMPTY && item.getCustomName() != null) {

                    Pet pet = getPetFromItemStack(item);
                    if (pet == null) continue;

                    // For unique index slot: add 45 slot = 5 row for each page except first page
                    // If pageNum == 0, there is no page indicator in the title, there is only 1 pet page.
                    int sbaPetIndex = i + 45 * (pageNum == 0 ? 0 : pageNum - 1);

                    Pet oldPet = main.getPetCacheManager().getPet(sbaPetIndex);

                    if (oldPet == null || oldPet.getItemStack() == null || !oldPet.displayName.equals(pet.displayName) || !oldPet.petInfo.equals(pet.petInfo)) {
                        main.getPetCacheManager().putPet(sbaPetIndex, pet);
                        petsUpdated = true;
                    }
                }
            }
        }
        if (petsUpdated) {
            main.getPetCacheManager().saveValues();
        }
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

        for (Pet pet : main.getPetCacheManager().getPetCache().getPetMap().values()) {
            if (TextUtils.stripPetName(pet.displayName).equals(petName)
                    && pet.petLevel == level
                    && pet.petInfo.getPetRarity() == rarity) {
                main.getPetCacheManager().setCurrentPet(pet);
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

        for (Map.Entry<Integer, Pet> petEntry : main.getPetCacheManager().getPetCache().getPetMap().int2ObjectEntrySet()) {
            int index = petEntry.getKey();
            Pet pet = petEntry.getValue();

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
                    main.getPetCacheManager().saveValues();
                    if (isCurrentPet) {
                        main.getPetCacheManager().setCurrentPet(pet);
                    }
                }
            }
        }
    }

    public void updatePetItem(String rarityColor, String petItem) {
        String petItemId = getPetIdFromDisplayName("§" + rarityColor + petItem);
        if (petItemId == null) return;
        Pet currentPet = main.getPetCacheManager().getCurrentPet();

        for (Map.Entry<Integer, Pet> petEntry : main.getPetCacheManager().getPetCache().getPetMap().int2ObjectEntrySet()) {
            int index = petEntry.getKey();
            Pet pet = petEntry.getValue();

            if (pet.petInfo.getUniqueId() == currentPet.petInfo.getUniqueId()) {
                pet.petInfo.setHeldItemId(petItemId);
                main.getPetCacheManager().putPet(index, pet);
                main.getPetCacheManager().setCurrentPet(pet);
            }
        }
    }

    /**
     * Parses the petInfo in the pet's ExtraAttributes to JsonObject after than converts to {@link Pet}
     * @param itemStack The pet ItemStack
     * @return {@link Pet}
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
        if (petInfo != null) {
            Pet oldPet = main.getPetCacheManager().getCurrentPet();
            Pet newPet = new Pet(itemStack, displayName, petLevel, petInfo);

            if (petInfo.isActive()) {
                if (oldPet == null || oldPet.getItemStack() == null || !oldPet.displayName.equals(displayName) || !oldPet.petInfo.equals(petInfo)) {
                    main.getPetCacheManager().setCurrentPet(newPet);
                }
            }

            return newPet;
        }
        return null;
    }

    public ItemStack getPetItemFromId(String petItemId) {
        PetItem petItem = petItems.get(petItemId);
        if (petItem != null) {
            return petItem.getItemStack();
        } else {
            return new ItemStack(Blocks.STONE.asItem());
        }
    }

    public String getPetItemDisplayNameFromId(String petItemId) {
        PetItem petItem = petItems.get(petItemId);
        if (petItem != null) {
            return petItem.getDisplayName();
        } else {
            return "§cNot Found!";
        }
    }

    public SkyblockRarity getPetItemRarityFromId(String petItemId) {
        PetItem petItem = petItems.get(petItemId);
        if (petItem != null) {
            return petItem.getRarity();
        } else {
            return SkyblockRarity.ADMIN;
        }
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
    @Getter
    public static class Pet {
        private String displayName;
        private int petLevel;
        private PetInfo petInfo;
        private CompressedStorage compressedStorage = new CompressedStorage(); // compressed ItemStack
        @Expose(serialize = false, deserialize = false)
        private ItemStack itemStack;

        public Pet(ItemStack stack, String displayName, int petLevel, PetInfo petInfo) {
            this.displayName = displayName;
            this.petLevel = petLevel;
            this.petInfo = petInfo;
            this.compressedStorage.setStorage(ItemUtils.getCompressedNBT(new ItemStack[]{stack}).getAsByteArray());
            this.itemStack = stack;
        }

        public ItemStack getItemStack() {
            if (this.itemStack == null && this.compressedStorage != null) {
                List<ItemStack> list = ContainerPreviewManager.decompressItems(this.compressedStorage.getStorage());
                if (!list.isEmpty()) {
                    this.itemStack = list.getFirst();
                }
            }
            return this.itemStack;
        }
    }

}