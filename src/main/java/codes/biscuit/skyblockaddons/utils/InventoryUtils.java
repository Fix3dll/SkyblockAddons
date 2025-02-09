package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Island;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.core.ItemDiff;
import codes.biscuit.skyblockaddons.core.SlayerArmorProgress;
import codes.biscuit.skyblockaddons.core.ThunderBottle;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonTracker;
import codes.biscuit.skyblockaddons.core.scheduler.ScheduledTask;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO Fix for Hypixel localization

/**
 * Utility methods related to player inventories
 */
public class InventoryUtils {
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public static final HashSet<String> BAT_PERSON_SET_IDS = new HashSet<>(
            Arrays.asList("BAT_PERSON_BOOTS", "BAT_PERSON_LEGGINGS", "BAT_PERSON_CHESTPLATE", "BAT_PERSON_HELMET")
    );
    private static final Pattern SLAYER_ARMOR_STACK_PATTERN = Pattern.compile("Next Upgrade: \\+([0-9]+❈) \\(([0-9,]+)/([0-9,]+)\\)");
    private List<ItemStack> previousInventory;
    private final Multimap<String, ItemDiff> itemPickupLog = ArrayListMultimap.create();

    @Setter private boolean inventoryWarningShown;

    /**
     * Whether the player is wearing a Skeleton Helmet.
     */
    @Getter private boolean wearingSkeletonHelmet;

    @Getter private boolean usingToxicArrowPoison;

    @Getter private boolean usingTwilightArrowPoison;

    @Getter private final SlayerArmorProgress[] slayerArmorProgresses = new SlayerArmorProgress[4];

    @Getter @Setter private InventoryType inventoryType;
    @Getter private String inventoryKey;
    @Getter @Setter private int inventoryPageNum;
    @Getter private String inventorySubtype;
    @Getter private String inventoryMayorName;

    private ScheduledTask repeatWarningTask = null;
    private boolean inQuiverMode = false;

    /**
     * Copies an inventory into a List of copied ItemStacks
     * @param inventory Inventory to copy
     * @return List of copied ItemStacks
     */
    private List<ItemStack> copyInventory(ItemStack[] inventory) {
        List<ItemStack> copy = new ArrayList<>(inventory.length);
        for (ItemStack item : inventory) {
            copy.add(ItemStack.copyItemStack(item));
        }
        return copy;
    }

    /**
     * Compares previously recorded Inventory state with current Inventory state to determine changes and
     * stores them in {@link #itemPickupLog}
     * @param currentInventory Current Inventory state
     */
    public void calculateInventoryDifference(ItemStack[] currentInventory) {
        List<ItemStack> newInventory = copyInventory(currentInventory);

        if (previousInventory != null) {
            DiffHashMap previousInventoryMap = new DiffHashMap();
            DiffHashMap newInventoryMap = new DiffHashMap();

            for (int i = 0; i < newInventory.size(); i++) {
                if (i == 8) { // Skip the SkyBlock Menu slot altogether (which includes the Quiver Arrow now)
                    continue;
                }

                ItemStack previousItem = null;
                ItemStack newItem = null;

                try {
                    previousItem = previousInventory.get(i);
                    newItem = newInventory.get(i);

                    if (previousItem != null) {
                        previousInventoryMap.updateWithItem(previousItem);
                    }

                    if (newItem != null) {
                        if (newItem.getDisplayName().contains(" " + ColorCode.DARK_GRAY + "x")) {
                            String newName = newItem.getDisplayName().substring(0, newItem.getDisplayName().lastIndexOf(" "));
                            newItem.setStackDisplayName(newName); // This is a workaround for merchants, it adds x64 or whatever to the end of the name.
                        }
                        newInventoryMap.updateWithItem(newItem);
                    }
                } catch (RuntimeException exception) {
                    CrashReport crashReport = CrashReport.makeCrashReport(exception, "Comparing current inventory to previous inventory");
                    CrashReportCategory inventoryDetails = crashReport.makeCategory("Inventory Details");
                    inventoryDetails.addCrashSection("Previous", "Size: " + previousInventory.size());
                    inventoryDetails.addCrashSection("New", "Size: " + newInventory.size());
                    CrashReportCategory itemDetails = crashReport.makeCategory("Item Details");
                    itemDetails.addCrashSection("Previous Item", "Item: " + (previousItem != null ? previousItem.toString() : "null") + "\n"
                            + "Display Name: " + (previousItem != null ? previousItem.getDisplayName() : "null") + "\n"
                            + "Index: " + i + "\n"
                            + "Map Value: " + (previousItem != null ? (previousInventoryMap.get(previousItem.getDisplayName()) != null ? previousInventoryMap.get(previousItem.getDisplayName()).toString() : "null") : "null"));
                    itemDetails.addCrashSection("New Item", "Item: " + (newItem != null ? newItem.toString() : "null") + "\n"
                            + "Display Name: " + (newItem != null ? newItem.getDisplayName() : "null") + "\n"
                            + "Index: " + i + "\n"
                            + "Map Value: " + (newItem != null ? (previousInventoryMap.get(newItem.getDisplayName()) != null ? previousInventoryMap.get(newItem.getDisplayName()).toString() : "null") : "null"));
                    throw new ReportedException(crashReport);
                }
            }

            List<ItemDiff> inventoryDifference = new LinkedList<>();
            Set<String> keySet = new HashSet<>(previousInventoryMap.keySet());
            keySet.addAll(newInventoryMap.keySet());

            keySet.forEach(key -> {
                int previousAmount = 0;
                if (previousInventoryMap.containsKey(key)) {
                    previousAmount = previousInventoryMap.get(key).getLeft();
                }

                int newAmount = 0;
                if (newInventoryMap.containsKey(key)) {
                    newAmount = newInventoryMap.get(key).getLeft();
                }

                int diff = newAmount - previousAmount;
                if (diff != 0) { // Get the NBT tag from whichever map the name exists in
                    inventoryDifference.add(
                            new ItemDiff(key, diff, newInventoryMap.getOrDefault(key, previousInventoryMap.get(key)).getRight())
                    );
                }
            });

            if (Feature.DRAGON_STATS_TRACKER.isEnabled()) {
                DragonTracker.getInstance().checkInventoryDifferenceForDrops(inventoryDifference);
            }

            // Add changes to already logged changes of the same item, so it will increase/decrease the amount
            // instead of displaying the same item twice
            if (Feature.ITEM_PICKUP_LOG.isEnabled()) {
                for (ItemDiff diff : inventoryDifference) {
                    Collection<ItemDiff> itemDiffs = itemPickupLog.get(diff.getDisplayName());
                    if (itemDiffs.size() <= 0) {
                        itemPickupLog.put(diff.getDisplayName(), diff);

                    } else {
                        boolean added = false;
                        for (ItemDiff loopDiff : itemDiffs) {
                            if ((diff.getAmount() < 0 && loopDiff.getAmount() < 0) || (diff.getAmount() > 0 && loopDiff.getAmount() > 0)) {
                                loopDiff.add(diff.getAmount());
                                added = true;
                            }
                        }
                        if (!added) {
                            itemPickupLog.put(diff.getDisplayName(), diff);
                        }
                    }
                }
            }
        }

        previousInventory = newInventory;
    }

    /**
     * Resets the previously stored Inventory state
     */
    public void resetPreviousInventory() {
        previousInventory = null;
    }

    /**
     * Removes items in the pickup log that have been there for longer than {@link ItemDiff#LIFESPAN}
     */
    public void cleanUpPickupLog() {
        itemPickupLog.entries().removeIf(entry -> entry.getValue().getLifetime() > ItemDiff.LIFESPAN);
    }

    /**
     * Checks if the players inventory is full and displays an alarm if so. Slot 8 is the Skyblock menu/quiver feather
     * slot. It's ignored so shooting with a full inventory doesn't spam the full inventory warning.
     * @param mc Minecraft instance
     * @param p Player to check
     */
    public void checkIfInventoryIsFull(Minecraft mc, EntityPlayerSP p) {
        Feature feature = Feature.FULL_INVENTORY_WARNING;

        if (main.getUtils().isOnSkyblock() && feature.isEnabled()) {
            for (int i = 0; i < p.inventory.mainInventory.length; i++) {
                // If we find an empty slot that isn't slot 8, remove any queued warnings and stop checking.
                ItemStack idxItem = p.inventory.mainInventory[i];
                if (idxItem == null && i != 8) {
                    if (inventoryWarningShown) {
                        if (repeatWarningTask != null) {
                            repeatWarningTask.cancel();
                            repeatWarningTask = null;
                        }
                    }
                    inventoryWarningShown = false;
                    return;
                } else if (idxItem != null && i == 8 && idxItem.getItem() == Items.feather) {
                    inQuiverMode = true;
                    return;
                }
            }
            inQuiverMode = false;

            // If we make it here, the inventory is full. Show the warning.
            if (mc.currentScreen == null && main.getPlayerListener().didntRecentlyJoinWorld()) {
                if (!inventoryWarningShown) {
                    showFullInventoryWarning();
                    inventoryWarningShown = true;
                }
                // Schedule a repeat if needed.
                if (feature.isEnabled(FeatureSetting.REPEATING_FULL_INVENTORY_WARNING) && repeatWarningTask == null) {
                    repeatWarningTask = main.getScheduler().scheduleTask(
                            scheduledTask -> {
                                // Stop the task if the setting is disabled or the player is not in Skyblock
                                if (feature.isDisabled(FeatureSetting.REPEATING_FULL_INVENTORY_WARNING)
                                        || mc.theWorld == null || mc.thePlayer == null || !main.getUtils().isOnSkyblock()) {
                                    scheduledTask.cancel();
                                    repeatWarningTask = null;
                                    return;
                                }
                                if (inQuiverMode) {
                                    return;
                                }
                                showFullInventoryWarning();
                            },
                            10 * 20,
                            10 * 20,
                            true,
                            false
                    );
                }
            }
        }
    }

    /**
     * Shows the full inventory warning.
     */
    public void showFullInventoryWarning() {
        main.getUtils().playLoudSound("random.orb", 0.5);
        main.getRenderListener().setTitleFeature(Feature.FULL_INVENTORY_WARNING);
    }

    /**
     * Checks if the player is wearing a Skeleton Helmet and updates {@link #wearingSkeletonHelmet} accordingly
     *
     * @param p Player to check
     */
    public void checkIfWearingSkeletonHelmet(EntityPlayerSP p) {
        if (Feature.SKELETON_BAR.isEnabled()) {
            ItemStack item = p.getCurrentArmor(3);
            if (item != null && "SKELETON_HELMET".equals(ItemUtils.getSkyblockItemID(item))) {
                wearingSkeletonHelmet = true;
                return;
            }
            wearingSkeletonHelmet = false;
        }
    }

    /**
     * Determines if the player is using Toxic Arrow Poison by detecting if it is present in their inventory.
     *
     * @param p the player to check
     */
    public void checkIfUsingArrowPoison(EntityPlayerSP p) {
        if (Feature.TURN_BOW_COLOR_WHEN_USING_ARROW_POISON.isEnabled()) {
            for (ItemStack item : p.inventory.mainInventory) {
                if (item != null) {
                    String itemID = ItemUtils.getSkyblockItemID(item);
                    if ("TOXIC_ARROW_POISON".equals(itemID)) {
                        this.usingToxicArrowPoison = true;
                        this.usingTwilightArrowPoison = false;
                        return;
                    } else if ("TWILIGHT_ARROW_POISON".equals(itemID)) {
                        this.usingToxicArrowPoison = false;
                        this.usingTwilightArrowPoison = true;
                        return;
                    }
                }
            }
            this.usingToxicArrowPoison = false;
            this.usingTwilightArrowPoison = false;
        }
    }

    /**
     * The difference between a slot number in any given {@link Container} and what that number would be in a {@link ContainerPlayer}.
     */
    public int getSlotDifference(Container container) {
        if (container instanceof ContainerChest) return 9-((ContainerChest)container).getLowerChestInventory().getSizeInventory();
        else if (container instanceof ContainerHopper) return 4;
        else if (container instanceof ContainerFurnace) return 6;
        else if (container instanceof ContainerBeacon) return 8;
        else return 0;
    }

    /**
     * Checks if the player has the Thunder Bottle and updates accordingly
     * @param p EntityPlayerSP
     */
    public void checkIfThunderBottle(EntityPlayerSP p) {
        if (Feature.THUNDER_BOTTLE_DISPLAY.isEnabled()) {
            ThunderBottle displayBottle = ThunderBottle.getDisplayBottle();

            // Check if display bottle still exist in inventory, if not clear current ThunderBottle
            if (displayBottle != null && p.inventory.mainInventory[displayBottle.getSlot()] == null) {
                displayBottle.setItemStack(null);
                displayBottle.setSlot(-1);
                displayBottle.setCharge(0);
            }

            ThunderBottle.updateThunderBottles(p.inventory.mainInventory);
        }
    }

    /**
     * Checks if the player is wearing any Revenant or Tarantula armor.
     * If the armor is detected, the armor's levelling progress is retrieved to be displayed on the HUD.
     *
     * @param p the player to check
     */
    public void checkIfWearingSlayerArmor(EntityPlayerSP p) {
        if (Feature.SLAYER_ARMOR_PROGRESS.isDisabled()) return;

        for (int i = 3; i >= 0; i--) {
            ItemStack itemStack = p.inventory.armorInventory[i];
            String itemID = itemStack != null ? ItemUtils.getSkyblockItemID(itemStack) : null;

            if (itemID != null && (itemID.startsWith("REVENANT") || itemID.startsWith("TARANTULA") ||
                    itemID.startsWith("FINAL_DESTINATION") || itemID.startsWith("REAPER"))) {
                String percent = null;
                String defence = null;
                List<String> lore = ItemUtils.getItemLore(itemStack);
                for (String loreLine : lore) {
                    Matcher matcher = SLAYER_ARMOR_STACK_PATTERN.matcher(TextUtils.stripColor(loreLine));
                    if (matcher.matches()) { // Example: line§5§o§7Next Upgrade: §a+240❈ §8(§a14,418§7/§c15,000§8)
                        try {
                            float percentage = Float.parseFloat(matcher.group(2).replace(",", "")) /
                                    Integer.parseInt(matcher.group(3).replace(",", "")) * 100;
                            BigDecimal bigDecimal = new BigDecimal(percentage).setScale(0, RoundingMode.HALF_UP);
                            percent = bigDecimal.toString();
                            defence = ColorCode.GREEN + matcher.group(1);
                            break;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                if (percent != null && defence != null) {
                    SlayerArmorProgress currentProgress = slayerArmorProgresses[i];

                    if (currentProgress == null || itemStack != currentProgress.getItemStack()) {
                        // The item has changed or didn't exist. Create new object.
                        slayerArmorProgresses[i] = new SlayerArmorProgress(itemStack, percent, defence);
                    } else {
                        // The item has remained the same. Just update the stats.
                        currentProgress.setPercent(percent);
                        currentProgress.setDefence(defence);
                    }
                }
            } else {
                slayerArmorProgresses[i] = null;
            }
        }
    }

    /**
     * Returns true if the player is wearing a full armor set with IDs contained in the given set
     *
     * @param player the player
     * @param armorSetIds the given set of armor IDs
     * @return {@code true} iff all player armor contained in given set, {@code false} otherwise.
     */
    public static boolean isWearingFullSet(EntityPlayer player, Set<String> armorSetIds) {
        boolean flag = true;
        ItemStack[] armorInventory = player.inventory.armorInventory;
        for (int i = 0; i < 4; i++) {
            String itemID = ItemUtils.getSkyblockItemID(armorInventory[i]);
            if (itemID == null || !armorSetIds.contains(itemID)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    /**
     * @return Log of recent Inventory changes
     */
    public Collection<ItemDiff> getItemPickupLog() {
        return itemPickupLog.values();
    }

    /**
     * Detects, stores, and returns the Skyblock inventory type of the given {@code GuiChest}. The inventory type is the
     * kind of menu the player has open, like a crafting table, or an enchanting table for example. If no known inventory
     * type is detected, {@code null} will be stored.
     *
     * @return an {@link InventoryType} enum constant representing the current Skyblock inventory type
     */
    public InventoryType updateInventoryType(GuiChest guiChest) {
        // Get the open chest and test if it's the same one that we've seen before
        IInventory inventory = guiChest.lowerChestInventory;
        if (inventory.getDisplayName() == null) {
            return inventoryType = null;
        }
        String chestName = TextUtils.stripColor(inventory.getDisplayName().getUnformattedText());

        // Initialize inventory to null and get the open chest name
        inventoryType = null;

        // Find an inventory match if possible
        for (InventoryType inventoryTypeItr : InventoryType.values()) {
            Matcher m = inventoryTypeItr.getInventoryPattern().matcher(chestName);
            if (m.matches()) {
                if (m.groupCount() > 0) {
                    if (inventoryTypeItr == InventoryType.MAYOR) {
                        try {
                            inventoryMayorName = m.group("mayor").trim();
                            inventoryType = InventoryType.MAYOR;
                            break; // early break
                        } catch (NullPointerException | IllegalStateException | IllegalArgumentException e) {
                            LOGGER.warn("Could not detect mayor in Mayor Menu");
                            inventoryMayorName = null;
                        }
                    }

                    try {
                        inventoryPageNum = Integer.parseInt(m.group("page"));
                    } catch (Exception e) {
                        inventoryPageNum = 0;
                    }
                    try {
                        inventorySubtype = m.group("type");
                    } catch (Exception e) {
                        inventorySubtype = null;
                    }
                } else {
                    inventoryPageNum = 0;
                    inventorySubtype = null;
                }
                inventoryType = inventoryTypeItr;
                break;
            }
        }
        inventoryKey = getInventoryKey(inventoryType, inventoryPageNum);
        return inventoryType;
    }

    private String getInventoryKey(InventoryType inventoryType, int inventoryPageNum) {
        if (inventoryType == null) {
            return null;
        }
        return inventoryType.getInventoryName() + inventoryPageNum;
    }

    /**
     * Custom HashMap for handle inventory differences
     * </br>Key: Display Name, Value: Diff size and ItemStack pair
     */
    private static class DiffHashMap extends HashMap<String, Pair<Integer, ItemStack>> {

        public void updateWithItem(ItemStack itemStack) {
            String skyblockId = ItemUtils.getSkyblockItemID(itemStack);

            String displayName = itemStack.getDisplayName();
            // Exceptions
            if ("ENCHANTED_BOOK".equals(skyblockId) || "ATTRIBUTE_SHARD".equals(skyblockId)) {
                List<String> lore = ItemUtils.getItemLore(itemStack);
                if (!lore.isEmpty()) {
                    displayName = lore.get(0);
                }
            } else if (itemStack.getItem() == Items.dye) {
                if (main.getUtils().isInDungeon() && StringUtils.isBlank(displayName)) {
                    // Ignore Archer's ghost abilities cooldown
                    return;
                } else if (LocationUtils.isOn(Island.KUUDRA) && (displayName.contains("You will be revived in")
                        || displayName.contains("Purchasable revive is on cooldown!"))) {
                    // Ignore revive counter and Purchase Revive cooldown items in Kuudra
                    return;
                }
            } else if (ItemUtils.isQuiverArrow(itemStack)) {
                // Ignore quiver arrow
                return;
            } else if ("INFINITE_SUPERBOOM_TNT".equals(skyblockId) || "LESSER_ORB_OF_HEALING".equals(skyblockId)) {
                // TODO add this to data repository
                // Ignore Infinityboom TNT and Lesser Orb of Healing
                return;
            }

            int amount;
            if (this.containsKey(displayName)) {
                amount = this.get(displayName).getLeft() + itemStack.stackSize;
            } else {
                amount = itemStack.stackSize;
            }

            this.put(displayName, new Pair<>(amount, itemStack));
        }
    }

}