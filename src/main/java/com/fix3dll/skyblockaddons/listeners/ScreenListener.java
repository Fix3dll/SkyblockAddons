package com.fix3dll.skyblockaddons.listeners;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.config.PetCacheManager;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.SkyblockEquipment;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.scheduler.ScheduledTask;
import com.fix3dll.skyblockaddons.events.ClientEvents;
import com.fix3dll.skyblockaddons.events.SkyblockAddonsEvents;
import com.fix3dll.skyblockaddons.features.PetManager;
import com.fix3dll.skyblockaddons.features.PetManager.Pet;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackColor;
import com.fix3dll.skyblockaddons.features.backpacks.BackpackInventoryManager;
import com.fix3dll.skyblockaddons.features.backpacks.ContainerPreviewManager;
import com.fix3dll.skyblockaddons.gui.screens.SkyblockAddonsScreen;
import com.fix3dll.skyblockaddons.mixin.hooks.AbstractContainerScreenHook;
import com.fix3dll.skyblockaddons.utils.DevUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.fix3dll.skyblockaddons.utils.data.requests.MayorRequest;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringUtil;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    private InventoryChangeListener inventoryChangeListener;
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
            addInventoryChangeListener(chestContainer);

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

        return;
    }

    private void beforeKeyPress(Screen screen, int key, int scancode, int modifiers) {
        if (main.getUtils().isOnSkyblock()) {
            ContainerPreviewManager.onContainerKeyTyped(key);
        }

        if (Feature.DEVELOPER_MODE.isEnabled() && key == SkyblockKeyBinding.DEVELOPER_COPY_NBT.getKeyCode()) {
            // Copy Item NBT, check if the player is in an inventory.
            if (MC.screen instanceof AbstractContainerScreen<?> containerScreen) {
                Slot currentSlot = containerScreen.hoveredSlot;
                if (currentSlot != null && currentSlot.hasItem() && MC.level != null) {
                    DevUtils.setCopyMode(DevUtils.CopyMode.ITEM);
                    // TODO add to DevUtils
                    if (currentSlot.getItem().has(DataComponents.PROFILE)) {
                        ResolvableProfile.CODEC.encodeStart(
                                JsonOps.INSTANCE, currentSlot.getItem().get(DataComponents.PROFILE)
                        ).result().ifPresent(LOGGER::info);
                    }
                    if (currentSlot.getItem().has(DataComponents.CUSTOM_NAME)) {
                        LOGGER.info(Component.Serializer.toJson(
                                currentSlot.getItem().getCustomName(), RegistryAccess.EMPTY
                        ));
                    }
                    DevUtils.copyNBTTagToClipboard(
                            currentSlot.getItem().save(MC.level.registryAccess()),
                            ColorCode.GREEN + "Item data was copied to clipboard!"
                    );
                }
            }
        }
    }

    public boolean onGuiOpen(Screen screen) {
        if (!main.getUtils().isOnSkyblock()) {
            return false;
        }

        Screen oldGuiScreen = MC.screen;

        // Closing a container
        if (screen == null) {
            boolean closed;

            if (oldGuiScreen instanceof ContainerScreen containerScreen) {
                InventoryType inventoryType = main.getInventoryUtils().getInventoryType();
                if (inventoryType == InventoryType.EQUIPMENT) {
                    // Set eqs after close eq menu
                    this.setEquipments(containerScreen.getMenu());
                } else if (inventoryType == InventoryType.SKYBLOCK_MENU && main.getUtils().isOnRift()) {
                    this.setRiftPet(containerScreen.getMenu());
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
            if (inventoryChangeListener != null) {
                removeInventoryChangeListener(listenedInventory);
            }

            ContainerPreviewManager.onContainerClose();
            setCurrentPet(containerScreen);
        }

        return false;
    }

    public void onInventoryLoadingDone() {
        Screen screen = MC.screen;
        InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

        if (listenedInventory != null) {
            removeInventoryChangeListener(listenedInventory);
            lastInventoryChangeMs = -1;
        }

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
                            DataUtils.loadOnlineData(new MayorRequest(mayorName));

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
                        DataUtils.loadOnlineData(new MayorRequest(mayorName));

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
                this.setEquipments(chestMenu);
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
                            PetCacheManager petCacheManager = main.getPetCacheManager();
                            Pet currentPet = petCacheManager.getCurrentPet();

                            if (selectedPet.contains("None")) {
                                petCacheManager.setCurrentPet(null);
                            } else if (currentPet != null && !currentPet.getDisplayName().endsWith(selectedPet)) {
                                Pet petToSet = null;
                                for (Pet pet : petCacheManager.getPetCache().getPetMap().values()) {
                                    if (pet.getDisplayName().endsWith(selectedPet)) {
                                        // If a similar pet is found, set the ‘petToSet’,
                                        // but continue searching for similarities.
                                        if (petToSet == null) {
                                            petToSet = pet;
                                        }
                                        // Otherwise, if there are more than one pet similarities, do not touch.
                                        else {
                                            petToSet = null;
                                            break;
                                        }
                                    }
                                }
                                if (petToSet != null) {
                                    petCacheManager.setCurrentPet(petToSet);
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    public boolean allowMouseClick(Screen screen, double mouseX, double mouseY, int button) {
        if (!main.getUtils().isOnSkyblock()) {
            return true;
        }

        // Ignore button up
//        if (!Mouse.getEventButtonState()) {
//            return;
//        }

        if (Feature.LOCK_SLOTS.isEnabled() && screen instanceof AbstractContainerScreen<?> containerScreen) {
            /*
            This prevents swapping items in/out of locked hotbar slots when using a hotbar key binding that is bound
            to a mouse button.
             */
            InputConstants.Key clickKey = InputConstants.Type.MOUSE.getOrCreate(button);
            KeyMapping[] hotbarKeys = MC.options.keyHotbarSlots;
            for (int i = 0; i < 9; i++) {
                if (!hotbarKeys[i].key.equals(clickKey)) continue;

                Slot slot = containerScreen.hoveredSlot;
                Slot hotbarSlot = containerScreen.getMenu().getSlot(containerScreen.getMenu().slots.size() - (9 - i));

                if (slot == null || hotbarSlot == null) {
                    return true;
                }

                if (main.getPersistentValuesManager().getLockedSlots().contains(i + 36)) {
                    if (!slot.hasItem() && !hotbarSlot.hasItem()) {
                        return true;
                    } else {
                        main.getUtils().playLoudSound(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5);
                        Utils.sendMessage(Feature.DROP_CONFIRMATION.getRestrictedColor() + Translations.getMessage("messages.slotLocked"));
                        return false;
                    }
                }
            }
            //TODO: Cover shift-clicking into locked slots
        }

        if (main.getUtils().isOnSkyblock()) {
            ContainerPreviewManager.onContainerKeyTyped(button);
        }

        return true;
    }

    /**
     * Called when a slot in the currently opened {@code GuiContainer} changes. Used to determine if all its items have been loaded.
     */
    void containerChanged(SimpleContainer inventory) {
        if (inventory.getItem(inventory.getContainerSize() - 1) != ItemStack.EMPTY) {
            SkyblockAddonsEvents.INVENTORY_LOADING_DONE.invoker().onInventoryLoadingDone();
        } else {
            lastInventoryChangeMs = System.currentTimeMillis();
        }
    }

    /**
     * Adds a change listener to a given inventory.
     * @param inventory the inventory to add the change listener to
     */
    private void addInventoryChangeListener(SimpleContainer inventory) {
        if (inventory == null) {
            throw new NullPointerException("Tried to add listener to null inventory.");
        }

        lastInventoryChangeMs = System.currentTimeMillis();
        inventoryChangeListener = new InventoryChangeListener(this);
        inventory.addListener(inventoryChangeListener);
        listenedInventory = inventory;
        inventoryChangeTimeCheckTask = main.getScheduler().scheduleTask(
                scheduledTask -> checkLastInventoryChangeTime(), 20, 5
        );
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
     * Removes {@link #inventoryChangeListener} from a given {@link SimpleContainer}.
     * @param inventory the {@code InventoryBasic} to remove the listener from
     */
    private void removeInventoryChangeListener(SimpleContainer inventory) {
        if (inventory == null) {
            throw new NullPointerException("Tried to remove listener from null inventory.");
        }

        if (inventoryChangeListener != null) {
            try {
                inventory.removeListener(inventoryChangeListener);
            } catch (NullPointerException e) {
                LOGGER.catching(e);
            }

            if (inventoryChangeTimeCheckTask != null) {
                if (!inventoryChangeTimeCheckTask.isCanceled()) {
                    inventoryChangeTimeCheckTask.cancel();
                }
            }

            inventoryChangeListener = null;
            listenedInventory = null;
            inventoryChangeTimeCheckTask = null;
        }
    }

    /**
     * Set current pet to last clicked pet while pets menu closing
     */
    private void setCurrentPet(ContainerScreen containerScreen) {
        boolean isClosedGuiPets = InventoryType.PETS.getInventoryPattern().matcher(
                containerScreen.getTitle().getString()
        ).matches();
        if (!isClosedGuiPets) return;

        PetCacheManager petCacheManager = main.getPetCacheManager();
        Int2ObjectOpenHashMap<Pet> petMap = petCacheManager.getPetCache().getPetMap();

        Pair<Integer, Integer> clickedButton = AbstractContainerScreenHook.getLastClickedButtonOnPetsMenu();
        if (clickedButton == null || clickedButton.getLeft() >= 54) return;

        int pageNum = main.getInventoryUtils().getInventoryPageNum();
        // If pageNum == 0, there is no page indicator in the title, there is only 1 pet page.
        int index = clickedButton.getLeft() + 45 * (pageNum == 0 ? 0 : pageNum -1);

        if (petMap.containsKey(index)) {
            Pet pet = petMap.get(index);
            if (pet.getPetInfo().isActive()) {
                petCacheManager.setCurrentPet(null);
            } else if (clickedButton.getRight() != 1 /*right click*/) {
                petCacheManager.setCurrentPet(pet);
            }
            // lastClickedButton has completed its task, time to clean up
            AbstractContainerScreenHook.setLastClickedButtonOnPetsMenu(null);
        }
    }

    private void setEquipments(ChestMenu chestMenu) {
        SkyblockEquipment.NECKLACE.setItemStack(chestMenu.getSlot(10).getItem());
        SkyblockEquipment.CLOAK.setItemStack(chestMenu.getSlot(19).getItem());
        SkyblockEquipment.BELT.setItemStack(chestMenu.getSlot(28).getItem());
        SkyblockEquipment.GLOVES_BRACELET.setItemStack(chestMenu.getSlot(37).getItem());

        ItemStack petItem = chestMenu.getSlot(47).getItem();

        if (petItem.is(Items.LIGHT_GRAY_STAINED_GLASS_PANE)) {
            // Be sure current pet is same on cache
            main.getPetCacheManager().setCurrentPet(null, false);
            SkyblockEquipment.PET.setItemStack(petItem);
        } else if (petItem.is(Items.PLAYER_HEAD)) {
            Pet newPet = PetManager.getInstance().getPetFromItemStack(petItem);
            Int2ObjectOpenHashMap<Pet> petMap = main.getPetCacheManager().getPetCache().getPetMap();

            if (newPet != null) {
                for (Int2ObjectMap.Entry<Pet> entry : petMap.int2ObjectEntrySet()) {
                    int entryKey = entry.getIntKey();
                    Pet entryValue = petMap.get(entryKey);

                    if (newPet.getPetInfo().getUniqueId().equals(entryValue.getPetInfo().getUniqueId())) {
                        petMap.put(entryKey, newPet);
                        main.getPetCacheManager().saveValues();
                        break;
                    }
                }
            }
            SkyblockEquipment.PET.setItemStack(petItem);
        }
        SkyblockEquipment.saveEquipments();
    }

    private void setRiftPet(ChestMenu chestMenu) {
        ItemStack riftPet = chestMenu.getSlot(30).getItem();
        if (riftPet.is(Items.PLAYER_HEAD)) {
            SkyblockEquipment.PET.setItemStack(riftPet);
            SkyblockEquipment.saveEquipments();
        }
    }

}