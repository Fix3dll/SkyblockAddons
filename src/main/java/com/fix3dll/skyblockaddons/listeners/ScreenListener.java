package com.fix3dll.skyblockaddons.listeners;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.config.PetCacheManager;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.PetInfo;
import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.scheduler.ScheduledTask;
import com.fix3dll.skyblockaddons.events.ClientEvents;
import com.fix3dll.skyblockaddons.events.SkyblockAddonsEvents;
import com.fix3dll.skyblockaddons.features.PetManager;
import com.fix3dll.skyblockaddons.features.PetManager.Pet;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackColor;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackInventoryManager;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import com.fix3dll.skyblockaddons.features.dungeons.DungeonProfitOverlay;
import com.fix3dll.skyblockaddons.gui.screens.SkyblockAddonsScreen;
import com.fix3dll.skyblockaddons.mixin.hooks.AbstractContainerScreenHook;
import com.fix3dll.skyblockaddons.utils.DevUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.fix3dll.skyblockaddons.utils.data.requests.ElectionRequest;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringUtil;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This listener listens for events that happen while a {@link Screen} is open.
 */
public class ScreenListener {

    private final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private SimpleContainer listenedInventory;
    private ScheduledTask inventoryChangeTimeCheckTask;

    /** Time in milliseconds of the last time a {@code GuiContainer} was closed */
    @Getter private long lastContainerCloseMs = -1;

    /** Time in milliseconds of the last time a backpack was opened, used by {@link Feature#BACKPACK_OPENING_SOUND}. */
    @Getter private long lastBackpackOpenMs = -1;

    /** Time in milliseconds of the last time an item in the currently open {@code GuiContainer} changed */
    private long lastInventoryChangeMs = -1;

    public ScreenListener() {
        SkyblockAddonsEvents.INVENTORY_LOADING_DONE.register(this::onInventoryLoadingDone);
        ClientEvents.BEFORE_SET_SCREEN.register(this::onGuiOpen);
        ScreenEvents.BEFORE_INIT.register(this::beforeScreenInit);
        ScreenEvents.AFTER_INIT.register(this::onAfterInitScreen);
    }

    public void beforeScreenInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        ScreenKeyboardEvents.beforeKeyPress(screen).register(this::beforeKeyPress);
        ScreenMouseEvents.allowMouseClick(screen).register(this::allowMouseClick);

        if (!main.getUtils().isOnSkyblock()) {
            return;
        }

        if (screen instanceof AbstractContainerScreen<?> containerScreen && containerScreen.getMenu() instanceof ChestMenu chestMenu) {
            Component title = containerScreen.getTitle();
            InventoryType inventoryType = main.getInventoryUtils().updateInventoryType(title);
            SimpleContainer chestContainer = (SimpleContainer) chestMenu.getContainer();

            lastInventoryChangeMs = System.currentTimeMillis();
            listenedInventory = chestContainer;
            if (inventoryChangeTimeCheckTask == null) {
                inventoryChangeTimeCheckTask = main.getScheduler().scheduleTask (_ -> checkLastInventoryChangeTime(), 20, 5);
            }

            // Backpack opening sound
            if (Feature.BACKPACK_OPENING_SOUND.isEnabled()) {
                if (title.getString().contains("Backpack")) {
                    lastBackpackOpenMs = System.currentTimeMillis();

                    if (ThreadLocalRandom.current().nextInt(0, 2) == 0) {
                        Utils.playSound(SoundEvents.HORSE_ARMOR.value(), 0.5F, 1F);
                    } else {
                        Utils.playSound(SoundEvents.HORSE_SADDLE.value(), 0.5F, 1F);
                    }
                }
            }

            if (Feature.SHOW_BACKPACK_PREVIEW.isEnabled()) {
                if (inventoryType == InventoryType.STORAGE_BACKPACK || inventoryType == InventoryType.ENDER_CHEST) {
                    ContainerPreviewManager.onContainerOpen(chestContainer);
                }
            }
        }
    }

    private void onAfterInitScreen(Minecraft minecraft, Screen screen, int width, int height) {
        ScreenEvents.afterBackground(screen).register(this::onAfterRenderScreenBg);
    }

    private void onAfterRenderScreenBg(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
        DungeonProfitOverlay.render(screen, graphics, mouseX, mouseY, tickDelta);
    }

    private void beforeKeyPress(Screen screen, KeyEvent event) {
        if (main.getUtils().isOnSkyblock()) {
            ContainerPreviewManager.onContainerKeyTyped(event.key());
        }

        if (Feature.DEVELOPER_MODE.isEnabled() && event.key() == SkyblockKeyBinding.DEVELOPER_COPY_NBT.getKeyCode()) {
            // Copy Item NBT, check if the player is in an inventory.
            if (MC.gui.screen() instanceof AbstractContainerScreen<?> containerScreen) {
                Slot currentSlot = containerScreen.hoveredSlot;
                if (currentSlot != null && currentSlot.hasItem() && MC.level != null) {
                    DevUtils.setCopyMode(DevUtils.CopyMode.ITEM);
                    // TODO add to DevUtils
                    ItemStack currentSlotItem = currentSlot.getItem();
                    ResolvableProfile profile = currentSlotItem.get(DataComponents.PROFILE);
                    if (profile != null) {
                        ResolvableProfile.CODEC.encodeStart(JsonOps.INSTANCE, profile).result().ifPresent(LOGGER::info);
                    }
                    Identifier itemModel = currentSlotItem.get(DataComponents.ITEM_MODEL);
                    if (itemModel != null) {
                        LOGGER.info("\"itemModel\": \"{}\"", itemModel);
                    }
                    Component customName = currentSlotItem.getCustomName();
                    if (customName != null) {
                        LOGGER.info(TextUtils.componentToJson(customName));
                    }
                    DevUtils.copyNBTTagToClipboard(
                            ItemUtils.encodeItemStack(currentSlotItem),
                            Component.literal("Item data was copied to clipboard!")
                                     .withColor(ColorCode.GREEN.getColor())
                    );
                }
            }
        }
    }

    /**
     * @return {@code true} to cancel the screen opening
     */
    public boolean onGuiOpen(Screen screen) {
        if (!main.getUtils().isOnSkyblock()) {
            return false;
        }

        Screen oldGuiScreen = MC.gui.screen();

        // Closing a container
        if (screen == null) {
            boolean closed;

            if (oldGuiScreen instanceof ContainerScreen containerScreen) {
                switch (main.getInventoryUtils().getInventoryType()) {
                    // Set eqs after close eq menu
                    case EQUIPMENT -> {
                        ChestMenu chestMenu = containerScreen.getMenu();
                        if (chestMenu.slots.size() > 47) {
                            this.setEquipmentsWithSlotIdx(containerScreen.getMenu(), 10, 19, 28, 37, 47);
                        }
                    }
                    case SKYBLOCK_MENU -> {
                        if (main.getUtils().isOnRift()) {
                            this.setRiftPet(containerScreen.getMenu());
                        }
                    }
                    case PETS -> {
                        PetManager petManager = PetManager.getInstance();
                        if (petManager.isCacheDirty()) {
                            main.getPetCacheManager().saveValues();
                            main.getPersistentValuesManager().saveValues();
                            petManager.setCacheDirty(false);
                        }
                    }
                    case EQUIPMENT_SETS -> this.setEquipmentsInSetsMenu(containerScreen.getMenu());
                    case LOADOUTS -> {
                        ChestMenu chestMenu = containerScreen.getMenu();
                        if (chestMenu.slots.size() > 37) {
                            this.setEquipmentsWithSlotIdx(containerScreen.getMenu(), 10, 19, 28, 37, 21);
                        }
                    }
                    case null, default -> {}
                }
                closed = true;
            } else {
                // Set lastContainerCloseMs on SkyblockAddonsScreen for shouldResetMouse()
                closed = oldGuiScreen instanceof SkyblockAddonsScreen;
            }

            if (closed) {
                lastContainerCloseMs = System.currentTimeMillis();
                main.getInventoryUtils().setInventoryType(null);
            }
        }

        // Closing or switching to a different GuiChest
        if (oldGuiScreen instanceof ContainerScreen containerScreen) {
            if (listenedInventory != null) {
                if (inventoryChangeTimeCheckTask != null && !inventoryChangeTimeCheckTask.isCanceled()) {
                    inventoryChangeTimeCheckTask.cancel();
                    inventoryChangeTimeCheckTask = null;
                }
                listenedInventory = null;
            }

            ContainerPreviewManager.onContainerClose();
            setCurrentPet(containerScreen);
            DungeonProfitOverlay.clear();
        }

        return false;
    }

    public void onInventoryLoadingDone() {
        Screen screen = MC.gui.screen();
        InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

        // Inventory loading is complete. Cancel the monitoring task and reset tracking state.
        if (inventoryChangeTimeCheckTask != null && !inventoryChangeTimeCheckTask.isCanceled()) {
            inventoryChangeTimeCheckTask.cancel();
            inventoryChangeTimeCheckTask = null;
        }
        listenedInventory = null;
        lastInventoryChangeMs = -1;

        if (screen instanceof AbstractContainerScreen<?> containerScreen && containerScreen.getMenu() instanceof ChestMenu chestMenu) {
            SimpleContainer chestContainer = (SimpleContainer) chestMenu.getContainer();

            // Save backpack colors
            if (inventoryType == InventoryType.STORAGE) {
                for (int i = 0; i < chestContainer.getContainerSize(); i++) {
                    ItemStack item = chestContainer.getItem(i);
                    if (item == ItemStack.EMPTY || item.getItem() != Items.PLAYER_HEAD) continue;

                    BackpackColor backpackColor = ItemUtils.getBackpackColor(item);
                    if (backpackColor != null) {
                        int slot = ItemUtils.getBackpackSlot(item);
                        if (slot != 0) {
                            BackpackInventoryManager.getBackpackColor().put(slot, backpackColor);
                        }
                    }
                }
            } else if (inventoryType == InventoryType.CALENDAR) {
                for (int i = 0; i < chestContainer.getContainerSize(); i++) {
                    ItemStack item = chestContainer.getItem(i);
                    if (item == ItemStack.EMPTY || item.getItem() != Items.PLAYER_HEAD) continue;

                    Component name = item.getCustomName();
                    if (name != null && name.getString().contains("Mayor ")) {
                        String mayorName = name.getString();
                        mayorName = mayorName.substring(mayorName.indexOf(' ') + 1);

                        if (!mayorName.equals(main.getUtils().getMayor())) {
                            // Update new mayor data from API
                            DataUtils.loadOnlineData(new ElectionRequest(mayorName));

                            main.getUtils().setMayor(mayorName);
                            LOGGER.info("Mayor changed to {}", mayorName);
                        }

                        if (mayorName.contains("Jerry")) {
                            main.getMayorJerryData().parseMayorJerryPerkpocalypse(item);
                        }

                        break;
                    }
                }
            } else if (inventoryType == InventoryType.MAYOR) {
                String mayorName = main.getInventoryUtils().getInventoryMayorName();

                if (!StringUtil.isNullOrEmpty(mayorName)) {
                    if (!mayorName.equals(main.getUtils().getMayor())) {
                        // Update new mayor data from API
                        DataUtils.loadOnlineData(new ElectionRequest(mayorName));

                        main.getUtils().setMayor(mayorName);
                        LOGGER.info("Mayor changed to {}", mayorName);
                    }

                    if (mayorName.contains("Jerry")) {
                        for (int i = 0; i < chestContainer.getContainerSize(); i++) {
                            ItemStack item = chestContainer.getItem(i);
                            if (item == ItemStack.EMPTY || item.getItem() != Items.PLAYER_HEAD) continue;

                            Component customName = item.getCustomName();
                            if (customName != null && customName.getString().contains("Mayor")) {
                                main.getMayorJerryData().parseMayorJerryPerkpocalypse(item);
                            }
                        }
                    }
                }
            } else if (inventoryType == InventoryType.EQUIPMENT) {
                if (chestMenu.slots.size() > 47) {
                    this.setEquipmentsWithSlotIdx(chestMenu, 10, 19, 28, 37, 47);
                }
            } else if (inventoryType == InventoryType.EQUIPMENT_SETS) {
                this.setEquipmentsInSetsMenu(chestMenu);
            } else if (inventoryType == InventoryType.LOADOUTS) {
                if (chestMenu.slots.size() > 37) {
                    this.setEquipmentsWithSlotIdx(chestMenu, 10, 19, 28, 37, 21);
                }
            } else if (inventoryType == InventoryType.SKYBLOCK_MENU) {
                if (main.getUtils().isOnRift()) {
                    this.setRiftPet(chestMenu);
                }
            } else if (inventoryType == InventoryType.PETS) {
                ItemStack petMenuBone = chestMenu.getSlot(4).getItem();

                if (petMenuBone.getItem() == Items.BONE) {
                    ItemLore itemLore = petMenuBone.get(DataComponents.LORE);

                    if (itemLore != null) {
                        String selectedPet = null;
                        for (Component line : itemLore.lines()) {
                            String lineString = line.getString();
                            if (lineString.contains("Selected pet:")) {
                                int colonIndex = lineString.indexOf(':');
                                if (colonIndex != -1) {
                                    selectedPet = lineString.substring(colonIndex + 2); // +1 space
                                }
                                break;
                            }
                        }

                        if (selectedPet != null) {
                            PetCacheManager pcm = main.getPetCacheManager();

                            if (selectedPet.contains("None")) {
                                pcm.setCurrentPetIndex(-1);
                            } else if (!isCurrentPetValid(selectedPet)) {
                                int petIdxToSet = Integer.MIN_VALUE;
                                var iterator = pcm.getData().getPetMap().int2ObjectEntrySet().fastIterator();
                                while (iterator.hasNext()) {
                                    Int2ObjectMap.Entry<Pet> entry = iterator.next();
                                    int idx = entry.getIntKey();
                                    Pet pet = entry.getValue();

                                    String resolvedBoneName = resolveAncientGoldenDragonException(pet, selectedPet);
                                    String strippedPetName = TextUtils.stripColor(pet.getDisplayName());

                                    if (strippedPetName.endsWith(resolvedBoneName)) {
                                        // If a similar pet is found, set the ‘petIdxToSet’,
                                        // but continue searching for similarities.
                                        if (petIdxToSet == Integer.MIN_VALUE) {
                                            petIdxToSet = idx;
                                        }
                                        // Otherwise, if there are more than one pet similarities, do not touch.
                                        else {
                                            petIdxToSet = Integer.MIN_VALUE;
                                            break;
                                        }
                                    }
                                }
                                if (petIdxToSet != Integer.MIN_VALUE) {
                                    pcm.setCurrentPetIndex(petIdxToSet);
                                }
                            }
                        }
                    }
                }
                PetManager.getInstance().setUpdatePetCache(true);
            } else if (inventoryType == InventoryType.KUUDRA_CHEST
                    || inventoryType == InventoryType.CATACOMBS_CHEST
                    || inventoryType == InventoryType.CROSEUS_CHEST_MENU) {
                DungeonProfitOverlay.parseContainer(chestContainer, inventoryType);
            }

        }
    }

    public boolean allowMouseClick(Screen screen, MouseButtonEvent event) {
        if (!main.getUtils().isOnSkyblock()) {
            return true;
        }

        ContainerPreviewManager.onContainerKeyTyped(event.button());

        return true;
    }

    /**
     * Called when a slot in the currently opened {@code GuiContainer} changes. Used to determine if all its items have been loaded.
     */
    public void containerChanged(SimpleContainer inventory) {
        if (listenedInventory == null) return;

        if (inventory.getItem(inventory.getContainerSize() - 1) != ItemStack.EMPTY) {
            SkyblockAddonsEvents.INVENTORY_LOADING_DONE.invoker().onInventoryLoadingDone();
        } else {
            lastInventoryChangeMs = System.currentTimeMillis();
        }
    }



    /**
     * Checks whether it has been more than one second since the last inventory change, which indicates inventory
     * loading is most likely finished. Could trigger incorrectly with a lag spike.
     */
    private void checkLastInventoryChangeTime() {
        if (listenedInventory != null) {
            if (lastInventoryChangeMs > -1 && System.currentTimeMillis() - lastInventoryChangeMs > 1000) {
                SkyblockAddonsEvents.INVENTORY_LOADING_DONE.invoker().onInventoryLoadingDone();
            }
        }
    }

    /**
     * Set current pet to last clicked pet while pets menu closing
     */
    private void setCurrentPet(ContainerScreen containerScreen) {
        boolean isClosedGuiPets = InventoryType.PETS.matcher(
                containerScreen.getTitle().getString()
        ).matches();
        if (!isClosedGuiPets) return;

        PetCacheManager petCacheManager = main.getPetCacheManager();
        Int2ObjectOpenHashMap<Pet> petMap = petCacheManager.getData().getPetMap();

        Pair<Integer, Integer> clickedButton = AbstractContainerScreenHook.consumePetsMenuLastClick();
        if (clickedButton == null || clickedButton.getLeft() >= 54) return;

        int pageNum = main.getInventoryUtils().getInventoryPageNum();
        // If pageNum == 0, there is no page indicator in the title, there is only 1 pet page.
        int index = clickedButton.getLeft() + 45 * (pageNum == 0 ? 0 : pageNum -1);

        Pet pet = petMap.get(index);
        if (pet != null) {
            if (pet.getPetInfo().isActive()) {
                petCacheManager.setCurrentPetIndex(-1);
            } else {
                if (clickedButton.getRight() != 1 /*1==right click*/) {
                    petCacheManager.setCurrentPetIndex(index);
                }
            }
        }
    }

    private void setEquipmentsInSetsMenu(ChestMenu chestMenu) {
        if (chestMenu.slots.size() < 44) return;

        for (int i = 36; i <= 44; i++) {
            Slot slot = chestMenu.slots.get(i);

            if (slot.getItem().is(Items.DYE.lime())) {
                setEquipmentsWithSlotIdx(chestMenu, i - 36, i - 27, i -18, i-9, -1);
                break;
            }
        }
    }

    private void setEquipmentsWithSlotIdx(ChestMenu chestMenu,
                                          int necklaceIdx,
                                          int cloakIdx,
                                          int beltIdx,
                                          int glovesBraceletIdx,
                                          int petIdx) {
        ItemStack necklace = chestMenu.getSlot(necklaceIdx).getItem();
        ItemStack cloak = chestMenu.getSlot(cloakIdx).getItem();
        ItemStack belt = chestMenu.getSlot(beltIdx).getItem();
        ItemStack glovesBracelet = chestMenu.getSlot(glovesBraceletIdx).getItem();
        SkyblockEquipment.NECKLACE.setItemStack(Utils.isGlassPane(necklace) ? null : necklace);
        SkyblockEquipment.CLOAK.setItemStack(Utils.isGlassPane(cloak) ? null : cloak);
        SkyblockEquipment.BELT.setItemStack(Utils.isGlassPane(belt) ? null : belt);
        SkyblockEquipment.GLOVES_BRACELET.setItemStack(Utils.isGlassPane(glovesBracelet) ? null : glovesBracelet);
        if (petIdx != -1) {
            parsePetItemAndSave(chestMenu.getSlot(petIdx).getItem());
        }
        SkyblockEquipment.saveEquipments();
    }

    private void parsePetItemAndSave(ItemStack petItem) {
        if (petItem == null) return;
        PetCacheManager pcm = main.getPetCacheManager();

        if (Utils.isGlassPane(petItem, DyeColor.LIGHT_GRAY)) {
            // Be sure current pet is same on cache
            pcm.setCurrentPetIndex(-1, false);
            SkyblockEquipment.PET.setItemStack(petItem);
        } else if (petItem.is(Items.PLAYER_HEAD)) {
            petItem.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data
                    .update(compoundTag -> compoundTag.remove("timestamp"))
            );
            ItemStack itemCopy = petItem.copy();
            Pet newPet = PetManager.getInstance().getPetFromItemStack(itemCopy);
            Int2ObjectOpenHashMap<Pet> petMap = pcm.getData().getPetMap();

            if (newPet != null) {
                var iterator = petMap.int2ObjectEntrySet().fastIterator();
                while (iterator.hasNext()) {
                    Int2ObjectMap.Entry<Pet> entry = iterator.next();
                    int entryKey = entry.getIntKey();
                    Pet entryValue = entry.getValue();

                    if (newPet.getPetInfo().getUniqueId().equals(entryValue.getPetInfo().getUniqueId())) {
                        newPet.compressItem();
                        petMap.put(entryKey, newPet);
                        pcm.setCurrentPetIndex(entryKey, false);

                        ItemStack oldPetItem = entryValue.getItemStack();
                        if (oldPetItem == null) {
                            pcm.saveValues();
                        } else {
                            oldPetItem.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data
                                    .update(compoundTag -> compoundTag.remove("timestamp"))
                            );
                            if (!ItemStack.matches(oldPetItem, petItem)) {
                                pcm.saveValues();
                            }
                        }
                        break;
                    }
                }
            }
            SkyblockEquipment.PET.setItemStack(itemCopy);
        }
    }

    private void setRiftPet(ChestMenu chestMenu) {
        ItemStack riftPet = chestMenu.getSlot(30).getItem();
        if (riftPet.is(Items.PLAYER_HEAD)) {
            SkyblockEquipment.PET.setItemStack(riftPet);
            SkyblockEquipment.saveEquipments();
        }
    }

    private boolean isCurrentPetValid(String selectedPetBone) {
        Pet currentPet = main.getPetCacheManager().getCurrentPet();

        if (currentPet == null) {
            return selectedPetBone.contains("None");
        } else {
            String selectedPet = resolveAncientGoldenDragonException(currentPet, selectedPetBone);
            return TextUtils.stripColor(currentPet.getDisplayName()).endsWith(selectedPet);
        }
    }

    private String resolveAncientGoldenDragonException(Pet pet, String selectedPetBone) {
        PetInfo currentPetInfo = pet.getPetInfo();

        if (currentPetInfo != null) {
            String currentSkin = currentPetInfo.getSkin();

            // "§7[Lvl 200] §8[§64§4✦§8] §6Golden Dragon" and "Selected pet: Golden Dragon ✦"
            if ("GOLDEN_DRAGON_ANCIENT".equals(currentSkin)) {
                return selectedPetBone.replace(" ✦", "");
            }
        }

        return selectedPetBone;
    }

}