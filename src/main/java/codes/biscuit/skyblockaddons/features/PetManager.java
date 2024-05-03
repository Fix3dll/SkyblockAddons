package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.core.Rarity;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.pojo.PetInfo;
import codes.biscuit.skyblockaddons.utils.skyblockdata.PetItem;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetManager {
    //private static final Pattern SELECTED_PET_PATTERN = Pattern.compile("(?:§.)*Selected pet: §(?<rarity>\\w)(?<pet>[\\w ]+)");
    private static final Pattern PET_LEVEL_PATTERN = Pattern.compile("(§7\\[Lvl )(?<level>\\d+)(].*)");

    /** The PetManager instance.*/
    @Getter private static final PetManager instance = new PetManager();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    @Setter
    private static Map<String, PetItem> petItems;

    /**
     * Inspired by NEU
     * @author Fix3dll
     */
    public void checkCurrentPet(Minecraft mc) {
        if (!main.getUtils().isOnSkyblock()) return;

        boolean petsUpdated = false;
        if (main.getInventoryUtils().getInventoryType() == InventoryType.PETS && mc.currentScreen instanceof GuiChest) {
            GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
            ContainerChest container = (ContainerChest) chest.inventorySlots;
            IInventory lower = container.getLowerChestInventory();

            // Pets menu size not lower than 54 slot
            if (lower.getSizeInventory() < 54) return;
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
            for (int i = 10; i < lower.getSizeInventory() - 10; i++) {
                ItemStack item = lower.getStackInSlot(i);
                if (item != null && item.hasDisplayName()) {

                    Pet pet = getPetFromItemStack(item);
                    if (pet == null) continue;

                    // For unique index slot: add 45 slot = 5 row for each page except first page
                    int sbaPetIndex = i + 45 * (pageNum - 1);

                    Pet oldPet = main.getPetCacheManager().getPet(sbaPetIndex);

                    if (oldPet == null || !oldPet.displayName.equals(pet.displayName) && !oldPet.petInfo.equals(pet.petInfo)) {
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
     * When Autopet messages came to chat it will trigger {@link codes.biscuit.skyblockaddons.listeners.PlayerListener#AUTOPET_PATTERN}
     * We will get groups from that Pattern, and we will set current pet from these groups values.
     * @param levelString level string
     * @param rarityColor rarity color string
     * @param petName petName string
     * @author Fix3dll
     */
    public void findCurrentPetFromAutopet(String levelString, String rarityColor, String petName) {
        int level = Integer.parseInt(levelString);
        ColorCode color = ColorCode.getByChar(rarityColor.charAt(0));
        Rarity rarity = Rarity.getByColorCode(color);

        for (Pet pet : main.getPetCacheManager().getPetCache().getPetMap().values()) {
            if (pet.displayName.contains(petName) && pet.petLevel == level && pet.petInfo.getPetRarity() == rarity) {
                main.getPetCacheManager().setCurrentPet(pet);
            }
        }
    }

    /**
     * When levelled up messages came to chat it will trigger {@link codes.biscuit.skyblockaddons.listeners.PlayerListener#PET_LEVELED_UP_PATTERN}
     * We will get groups from that Pattern, and we will update petCache and set current pet from these groups values.
     * @param newLevelString level string
     * @param rarityColor rarity color string
     * @param petName petName string
     * @author Fix3dll
     */
    public void updateAndSetCurrentLevelledPet(String newLevelString, String rarityColor, String petName) {
        int newLevel = Integer.parseInt(newLevelString);
        ColorCode color = ColorCode.getByChar(rarityColor.charAt(0));
        Rarity rarity = Rarity.getByColorCode(color);

        for (Map.Entry<Integer, Pet> petEntry : main.getPetCacheManager().getPetCache().getPetMap().entrySet()) {
            int index = petEntry.getKey();
            Pet pet = petEntry.getValue();

            if (pet.displayName.contains(petName) && pet.petLevel == newLevel - 1 && pet.petInfo.getPetRarity() == rarity) {
                pet.petLevel = newLevel;
                Matcher m = PET_LEVEL_PATTERN.matcher(pet.displayName);
                if (m.matches()) {
                    pet.displayName = m.group(1) + newLevelString + m.group(3);
                }
                main.getPetCacheManager().putPet(index, pet);
                main.getPetCacheManager().setCurrentPet(pet);
            }
        }
    }

    /**
     * Parses the petInfo in the pet's ExtraAttributes to JsonObject after than converts to {@link Pet}
     * @param stack The pet ItemStack
     * @return {@link Pet}
     * @see codes.biscuit.skyblockaddons.utils.pojo.PetInfo
     * @author Fix3dll
     */
    private Pet getPetFromItemStack(ItemStack stack) {
        String displayName = stack.getDisplayName();

        int petLevel = TextUtils.getPetLevelFromDisplayName(displayName);
        if (petLevel == -1) return null;

        NBTTagCompound ea = ItemUtils.getExtraAttributes(stack);
        if (ea != null && ea.hasKey("petInfo")) {
            JsonParser jsonParser = new JsonParser();
            JsonObject petInfoJson = jsonParser.parse(ea.getString("petInfo")).getAsJsonObject();
            PetInfo petInfo = SkyblockAddons.getGson().fromJson(petInfoJson, PetInfo.class);

            Pet oldPet = main.getPetCacheManager().getCurrentPet();
            Pet newPet = new Pet(stack, displayName, petLevel, petInfo);

            if (petInfo.isActive()) {
                if (oldPet == null || !oldPet.displayName.equals(displayName) || !oldPet.petInfo.equals(petInfo)) {
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
            return new ItemStack(Item.getItemFromBlock(Blocks.stone));
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

    public Rarity getPetItemRarityFromId(String petItemId) {
        PetItem petItem = petItems.get(petItemId);
        if (petItem != null) {
            return petItem.getRarity();
        } else {
            return Rarity.ADMIN;
        }
    }

    @SuppressWarnings("FieldMayBeFinal")
    @Getter
    public static class Pet {
        private String displayName;
        private int petLevel;
        private PetInfo petInfo;
        // for create skull
        private String skullId;
        private String textureURL;

        public Pet(ItemStack stack, String displayName, int petLevel, PetInfo petInfo) {
            this.displayName = displayName;
            this.petLevel = petLevel;
            this.petInfo = petInfo;
            this.skullId = ItemUtils.getSkullOwnerID(stack);
            this.textureURL = TextUtils.decodeSkinTexture(ItemUtils.getSkullTexture(stack), true);
        }
    }
}
