package codes.biscuit.skyblockaddons.features.enchants;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.RomanNumeralParser;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.EnchantmentsData;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnchantManager {

    // Catches successive [ENCHANT] [ROMAN NUMERALS OR DIGITS], as well as stacking enchants listing total stacked number
    private static final Pattern ENCHANTMENT_PATTERN = Pattern.compile("(?<enchant>[A-Za-z][A-Za-z -]+) (?<levelNumeral>[IVXLCDM]+)(?=, |$| [\\d,]+$)");
    private static final Pattern GREY_ENCHANT_PATTERN = Pattern.compile("^(Respiration|Aqua Affinity|Depth Strider|Efficiency).*");
    private static final String COMMA = ", ";
    @Setter private static EnchantmentsData enchants = new EnchantmentsData();

    private static final Cache loreCache = new Cache();

    /**
     * Parse through enchantments, update the item's nbt, and cache the result for future queries
     * @param loreList the current item lore (which may be processed by enchants)
     * @param item
     */
    public static void parseEnchants(List<String> loreList, ItemStack item) {
        NBTTagCompound enchantNBT = ItemUtils.getEnchantments(item);
        if (enchantNBT == null && SkyblockAddons.getInstance().getInventoryUtils().getInventoryType() != InventoryType.SUPERPAIRS) {
            return;
        }
        // Add caching tooltip so continuous hover isn't so much of a problem
        if (loreCache.isCached(loreList)) {
            loreList.clear();
            loreList.addAll(loreCache.getCachedAfter());
            return;
        }
        // Update the cache so we have something to which to compare later
        loreCache.updateBefore(loreList);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        int startEnchant = -1, endEnchant = -1, maxTooltipWidth = 0;
        int indexOfLastGreyEnchant = accountForAndRemoveGreyEnchants(loreList, item);
        for (int i = indexOfLastGreyEnchant == -1 ? 0 : indexOfLastGreyEnchant + 1; i < loreList.size(); i++) {
            String line = loreList.get(i);
            String stripedLine = TextUtils.stripColor(line);
            if (startEnchant == -1) {
                if (containsEnchantment(item, stripedLine)) {
                    startEnchant = i;
                }
            }
            // Assume enchants end with an empty line "break"
            else if (stripedLine.trim().isEmpty() && endEnchant == -1) {
                endEnchant = i - 1;
            }
            // Get max tooltip size, disregarding the enchants section
            if (startEnchant == -1 || endEnchant != -1) {
                maxTooltipWidth = Math.max(fontRenderer.getStringWidth(loreList.get(i)), maxTooltipWidth);
            }
        }
        if (enchantNBT == null && endEnchant == -1) {
            endEnchant = startEnchant;
        }
        if (endEnchant == -1) {
            loreCache.updateAfter(loreList);
            return;
        }
        // Figure out whether the item tooltip is gonna wrap, and if so, try to make our enchantments wrap
        maxTooltipWidth = correctTooltipWidth(maxTooltipWidth);

        boolean hasLore = false;
        TreeSet<FormattedEnchant> orderedEnchants = new TreeSet<>();
        FormattedEnchant lastEnchant = null;
        // Order all enchants
        for (int i = startEnchant; i <= endEnchant; i++) {
            String unformattedLine = TextUtils.stripColor(loreList.get(i));
            Matcher m = ENCHANTMENT_PATTERN.matcher(unformattedLine);
            boolean containsEnchant = false;
            while (m.find()) {
                // Pull out the enchantment and the enchantment level from lore
                EnchantmentsData.Enchant enchant = enchants.getFromLore(m.group("enchant"));
                int level = RomanNumeralParser.parseNumeral(m.group("levelNumeral"));
                if (enchant != null) {
                    // Get the original (input) formatting code of the enchantment, which may have been affected by other mods
                    String inputFormatEnchant = "null";
                    if (Feature.ENCHANTMENTS_HIGHLIGHT.isDisabled()) {
                        inputFormatEnchant = TextUtils.getFormattedString(loreList.get(i), m.group());
                    }
                    lastEnchant = new FormattedEnchant(enchant, level, inputFormatEnchant);
                    // Try to add the enchant to the list, otherwise find the same enchant that was already present in the list
                    if (!orderedEnchants.add(lastEnchant)) {
                        for (FormattedEnchant e : orderedEnchants) {
                            if (e.compareTo(lastEnchant) == 0) {
                                lastEnchant = e;
                                break;
                            }
                        }
                    }
                    containsEnchant = true;
                }
            }
            // Add any enchantment lore that might follow an enchant to the lore description
            if (!containsEnchant && lastEnchant != null) {
                lastEnchant.addLore(loreList.get(i));
                hasLore = true;
            }
        }
        int numEnchants = orderedEnchants.size();

        for (FormattedEnchant enchant : orderedEnchants) {

            maxTooltipWidth = Math.max(enchant.getRenderLength(), maxTooltipWidth);
        }

        if (orderedEnchants.isEmpty()) {
            loreCache.updateAfter(loreList);
            return;
        }
        // Remove enchantment lines
        loreList.subList(startEnchant, endEnchant + 1).clear();

        List<String> insertEnchants;
        EnchantListLayout layout = SkyblockAddons.getInstance().getConfigValues().getEnchantLayout();
        // Pack as many enchantments as we can into one line (while not overstuffing it)
        if (layout == EnchantListLayout.COMPRESS && numEnchants != 1) {
            insertEnchants = new ArrayList<>();

            // Get format for comma
            String comma = SkyblockAddons.getInstance().getConfigValues().getRestrictedColor(Feature.ENCHANTMENT_COMMA_COLOR) + COMMA;
            int commaLength = fontRenderer.getStringWidth(comma);

            // Process each line of enchants
            int sum = 0;
            StringBuilder builder = new StringBuilder(maxTooltipWidth);
            for (FormattedEnchant enchant : orderedEnchants) {
                // Check if there will be overflow on this line. This will never happen for a single enchant on a line
                if (sum + enchant.getRenderLength() > maxTooltipWidth) {
                    builder.delete(builder.length() - comma.length(), builder.length());
                    insertEnchants.add(builder.toString());
                    builder = new StringBuilder(maxTooltipWidth);
                    sum = 0;
                }
                // Add the enchant followed by a comma
                builder.append(enchant.getFormattedString()).append(comma);
                sum += enchant.getRenderLength() + commaLength;
            }
            // Flush any remaining enchants
            if (builder.length() >= comma.length()) {
                builder.delete(builder.length() - comma.length(), builder.length());
                insertEnchants.add(builder.toString());
            }
        }
        // Print 2 enchants per line, separated by a comma, with no enchant lore (typical hypixel behavior)
        else if (layout == EnchantListLayout.NORMAL && !hasLore) {
            insertEnchants = new ArrayList<>();

            String comma;
            if (Feature.ENCHANTMENTS_HIGHLIGHT.isEnabled()) {
                comma = SkyblockAddons.getInstance().getConfigValues().getRestrictedColor(Feature.ENCHANTMENT_COMMA_COLOR) + COMMA;
            } else {
                comma = COMMA;
            }
            // Process each line of enchants
            int i = 0;
            StringBuilder builder = new StringBuilder(maxTooltipWidth);
            for (FormattedEnchant enchant : orderedEnchants) {
                // Add the enchant
                builder.append(enchant.getFormattedString());
                // Add a comma for the first on the row, followed by a comma
                if (i % 2 == 0) {
                    builder.append(comma);
                }
                // Create a new line
                else {
                    insertEnchants.add(builder.toString());
                    builder = new StringBuilder(maxTooltipWidth);
                }
                i++;
            }
            // Flush any remaining enchants
            if (builder.length() >= comma.length()) {
                builder.delete(builder.length() - comma.length(), builder.length());
                insertEnchants.add(builder.toString());
            }
        }
        // Prints each enchantment out on a separate line. Also adds the lore if need be
        else {
            // Add each enchantment (one per line) + add enchant lore (if available)
            if (Feature.HIDE_ENCHANT_DESCRIPTION.isDisabled()) {
                insertEnchants = new ArrayList<>((hasLore ? 3 : 1) * numEnchants);
                for (FormattedEnchant enchant : orderedEnchants) {
                    // Add the enchant
                    insertEnchants.add(enchant.getFormattedString());
                    // Add the enchant lore (if any)
                    insertEnchants.addAll(enchant.getLore());
                }
            } else {
                // Add each enchantment (one per line) and ignore enchant lore
                insertEnchants = new ArrayList<>(numEnchants);
                for (FormattedEnchant enchant : orderedEnchants) {
                    // Add the enchant
                    insertEnchants.add(enchant.getFormattedString());
                }
            }
        }

        // Add all of the enchants to the lore
        loreList.addAll(startEnchant, insertEnchants);
        // Cache the result so we can use it again
        loreCache.updateAfter(loreList);
    }

    /**
     * Adds the progression to the next level to any of the stacking enchants
     *
     * @param loreList        the tooltip being built
     * @param extraAttributes the extra attributes tag of the item
     * @param insertAt        the position at which we should insert the tag
     * @return the index after the point at which we inserted new lines, or {@param insertAt} if we didn't insert anything.
     */
    public static int insertStackingEnchantProgress(List<String> loreList, NBTTagCompound extraAttributes, int insertAt) {
        if (extraAttributes == null || Feature.SHOW_STACKING_ENCHANT_PROGRESS.isDisabled()) {
            return insertAt;
        }
        for (EnchantmentsData.Enchant.Stacking enchant : enchants.getStacking().values()) {
            if (extraAttributes.hasKey(enchant.getNbtNum(), Constants.NBT.TAG_ANY_NUMERIC)) {
                long stackedEnchantNum = extraAttributes.getLong(enchant.getNbtNum());
                Long nextLevel = enchant.getStackLevel().higher(stackedEnchantNum);
                String statLabel = Translations.getMessage("enchants." + enchant.getStatLabel());
                ColorCode colorCode = SkyblockAddons.getInstance().getConfigValues().getRestrictedColor(Feature.SHOW_STACKING_ENCHANT_PROGRESS);
                StringBuilder b = new StringBuilder();
                b.append("§7").append(statLabel).append(": ").append(colorCode);
                if (nextLevel == null) {
                    // §7Expertise Kills: §a5000000000 §7(Maxed)
                    b.append(TextUtils.abbreviate(stackedEnchantNum)).append(" §7(").append(Translations.getMessage("enchants.maxed")).append(")");
                } else {
                    // §7Expertise Kills: §a500 §7/ 1k
                    String format = TextUtils.formatNumber(stackedEnchantNum);
                    b.append(format).append(" §7/ ").append(TextUtils.abbreviate(nextLevel));
                }
                loreList.add(insertAt++, b.toString());
            }
        }
        return insertAt;
    }

    /**
     * Helper method to determine whether we should skip this line in parsing the lore.
     * E.g. we want to skip "Breaking Power X" seen on pickaxes.
     * @param itemStack ItemStack
     * @param stripedLine the line of lore we are parsing
     * @return {@code true} if no enchants on the line are in the enchants table, {@code false} otherwise.
     */
    public static boolean containsEnchantment(ItemStack itemStack, String stripedLine) {
        NBTTagCompound enchantNBT = ItemUtils.getEnchantments(itemStack);
        NBTTagCompound attributesNBT = ItemUtils.getAttributes(itemStack);

        Matcher m = ENCHANTMENT_PATTERN.matcher(stripedLine);
        while (m.find()) {
            EnchantmentsData.Enchant enchant = enchants.getFromLore(m.group("enchant"));
            if (enchantNBT == null || enchantNBT.hasKey(enchant.getNbtName())) {
                if (attributesNBT == null || !attributesNBT.hasKey(enchant.getNbtName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Counts (and optionally removes) vanilla grey enchants that are added on the first 1-2 lines of lore.
     * Removal of the grey enchants is specified by the {@link Feature#HIDE_GREY_ENCHANTS} feature.
     *
     * @param tooltip the tooltip being built
     * @param item    to which the tooltip corresponds
     * @return an integer denoting the last index of a grey enchantment, or -1 if none were found.
     */
    private static int accountForAndRemoveGreyEnchants(List<String> tooltip, ItemStack item) {
        // No grey enchants will be added if there is no vanilla enchantments tag
        if (item.getEnchantmentTagList() == null || item.getEnchantmentTagList().tagCount() == 0) {
            return -1;
        }
        int lastGreyEnchant = -1;
        boolean removeGreyEnchants = Feature.HIDE_GREY_ENCHANTS.isEnabled();

        // Start at index 1 since index 0 is the title
        int total = 0;
        for (int i = 1; total < 1 + item.getEnchantmentTagList().tagCount() && i < tooltip.size(); total++) { // only a max of 2 gray enchants are possible
            String line = tooltip.get(i);
            if (GREY_ENCHANT_PATTERN.matcher(line).matches()) {
                lastGreyEnchant = i;

                if (removeGreyEnchants) {
                    tooltip.remove(i);
                }
            } else {
                i++;
            }
        }
        return removeGreyEnchants ? -1 : lastGreyEnchant;
    }

    private static int correctTooltipWidth(int maxTooltipWidth) {
        // Figure out whether the item tooltip is gonna wrap, and if so, try to make our enchantments wrap
        final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        final int mouseX = Mouse.getX() * scaledresolution.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
        int tooltipX = mouseX + 12;
        if (tooltipX + maxTooltipWidth + 4 > scaledresolution.getScaledWidth()) {
            tooltipX = mouseX - 16 - maxTooltipWidth;
            if (tooltipX < 4) {
                if (mouseX > scaledresolution.getScaledWidth() / 2) {
                    maxTooltipWidth = mouseX - 12 - 8;
                } else {
                    maxTooltipWidth = scaledresolution.getScaledWidth() - 16 - mouseX;
                }
            }
        }

        if (scaledresolution.getScaledWidth() > 0 && maxTooltipWidth > scaledresolution.getScaledWidth()) {
            maxTooltipWidth = scaledresolution.getScaledWidth();
        }
        return maxTooltipWidth;
    }

    public static void markCacheDirty() {
        loreCache.configChanged = true;
    }

    static class Cache {
        @Getter
        List<String> cachedAfter = new ArrayList<>();
        boolean configChanged;
        @Getter
        private List<String> cachedBefore = new ArrayList<>();

        public Cache() {
        }

        public void updateBefore(List<String> loreBeforeModifications) {
            cachedBefore = new ArrayList<>(loreBeforeModifications);
        }

        public void updateAfter(List<String> loreAfterModifications) {
            cachedAfter = new ArrayList<>(loreAfterModifications);
            configChanged = false;
        }

        public boolean isCached(List<String> loreBeforeModifications) {
            if (configChanged || loreBeforeModifications.size() != cachedBefore.size()) {
                return false;
            }
            for (int i = 0; i < loreBeforeModifications.size(); i++) {
                if (!loreBeforeModifications.get(i).equals(cachedBefore.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    static class FormattedEnchant implements Comparable<FormattedEnchant> {
        EnchantmentsData.Enchant enchant;
        int level;
        List<String> loreDescription;
        String inputFormattedString;


        public FormattedEnchant(EnchantmentsData.Enchant theEnchant, int theLevel, String theFormattedEnchant) {
            enchant = theEnchant;
            level = theLevel;
            inputFormattedString = theFormattedEnchant;
            loreDescription = new ArrayList<>();
        }

        public void addLore(String lineOfEnchantLore) {
            loreDescription.add(lineOfEnchantLore);
        }

        public List<String> getLore() {
            return loreDescription;
        }

        @Override
        public int compareTo(FormattedEnchant o) {
            return this.enchant.compareTo(o.enchant);
        }

        public int getRenderLength() {
            return Minecraft.getMinecraft().fontRendererObj.getStringWidth(getFormattedString());
        }

        public String getFormattedString() {
            StringBuilder b = new StringBuilder();
            if (Feature.ENCHANTMENTS_HIGHLIGHT.isEnabled()) {
                b.append(enchant.getFormattedName(level));
            } else {
                return inputFormattedString;
            }
            b.append(" ");
            if (Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS.isEnabled()) {
                b.append(level);
            } else {
                b.append(RomanNumeralParser.integerToRoman(level));
            }

            return b.toString();
        }
    }
}
