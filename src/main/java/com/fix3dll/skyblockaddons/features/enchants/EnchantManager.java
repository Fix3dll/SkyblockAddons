package com.fix3dll.skyblockaddons.features.enchants;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.RomanNumeralParser;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.EnchantmentsData;
import com.fix3dll.skyblockaddons.utils.objects.RegistrableEnum;
import com.mojang.blaze3d.platform.Window;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO Component <-> String overhaul, cleanup
public class EnchantManager {

    // Catches successive [ENCHANT] [ROMAN NUMERALS OR DIGITS], as well as stacking enchants listing total stacked number
    private static final Pattern ENCHANTMENT_PATTERN = Pattern.compile("(?<enchant>[A-Za-z][A-Za-z -]+) (?<levelNumeral>[IVXLCDM]+)(?=, |$| [\\d,]+$)");
    private static final Pattern GREY_ENCHANT_PATTERN = Pattern.compile("^(Respiration|Aqua Affinity|Depth Strider|Efficiency).*");
    private static final String COMMA = ", ";
    private static final Cache LORE_CACHE = new Cache();
    @Setter private static EnchantmentsData enchants = new EnchantmentsData();

    /**
     * Parse through enchantments, update the item's nbt, and cache the result for future queries
     * @param loreList the current item lore (which may be processed by enchants)
     * @param item item
     */
    public static void parseEnchants(List<Component> loreList, ItemStack item) {
        Map<String, Integer> enchantments = ItemUtils.getEnchantments(item);
        if (enchantments.isEmpty() && SkyblockAddons.getInstance().getInventoryUtils().getInventoryType() != InventoryType.SUPERPAIRS) {
            return;
        }

        // Add caching tooltip so continuous hover isn't so much of a problem
        if (LORE_CACHE.isCached(loreList)) {
            loreList.clear();
            loreList.addAll(LORE_CACHE.getCachedAfter());
            return;
        }

        // Update the cache so we have something to which to compare later
        LORE_CACHE.updateBefore(loreList);

        Feature feature = Feature.ENCHANTMENT_LORE_PARSING;
        Font font = Minecraft.getInstance().font;
        int startEnchant = -1, endEnchant = -1, maxTooltipWidth = 0;
        int indexOfLastGreyEnchant = accountForAndRemoveGreyEnchants(loreList, item);
        for (int i = indexOfLastGreyEnchant == -1 ? 0 : indexOfLastGreyEnchant + 1; i < loreList.size(); i++) {
            Component line = loreList.get(i);
            String strippedLine = TextUtils.stripColor(line.getString());
            if (startEnchant == -1) {
                if (containsEnchantment(item, strippedLine)) {
                    startEnchant = i;
                }
            }
            // Assume enchants end with an empty line "break"
            else if (strippedLine.trim().isEmpty() && endEnchant == -1) {
                endEnchant = i - 1;
            }
            // Get max tooltip size, disregarding the enchants section
            if (startEnchant == -1 || endEnchant != -1) {
                maxTooltipWidth = Math.max(font.width(loreList.get(i)), maxTooltipWidth);
            }
        }
        if (enchantments.isEmpty() && endEnchant == -1) {
            endEnchant = startEnchant;
        }
        if (endEnchant == -1) {
            LORE_CACHE.updateAfter(loreList);
            return;
        }
        // Figure out whether the item tooltip is gonna wrap, and if so, try to make our enchantments wrap
        maxTooltipWidth = correctTooltipWidth(maxTooltipWidth);

        boolean hasLore = false;
        TreeSet<FormattedEnchant> orderedEnchants = new TreeSet<>();
        FormattedEnchant lastEnchant = null;
        // Order all enchants
        for (int i = startEnchant; i <= endEnchant; i++) {
            String unformattedLine = TextUtils.stripColor(loreList.get(i).getString());
            Matcher m = ENCHANTMENT_PATTERN.matcher(unformattedLine);
            boolean containsEnchant = false;
            while (m.find()) {
                // Pull out the enchantment and the enchantment level from lore
                EnchantmentsData.Enchant enchant = enchants.getFromLore(m.group("enchant"));
                int level = RomanNumeralParser.parseNumeral(m.group("levelNumeral"));
                if (enchant != null) {
                    // Get the original (input) formatting code of the enchantment, which may have been affected by other mods
                    String inputFormatEnchant = "null";
                    if (feature.isDisabled(FeatureSetting.HIGHLIGHT_ENCHANTMENTS)) {
                        inputFormatEnchant = TextUtils.getFormattedString(loreList.get(i).getString(), m.group());
                    }
                    lastEnchant = new FormattedEnchant(enchant, level, inputFormatEnchant);
                    // Try to add enchant to the list, otherwise find the same enchant that was already present in the list
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
            LORE_CACHE.updateAfter(loreList);
            return;
        }
        // Remove enchantment lines
        loreList.subList(startEnchant, endEnchant + 1).clear();

        List<Component> insertEnchants;
        RegistrableEnum layout = feature.getAsEnum(FeatureSetting.ENCHANT_LAYOUT);
        // Pack as many enchantments as we can into one line (while not overstuffing it)
        if (layout == EnchantLayout.COMPRESS && numEnchants != 1) {
            insertEnchants = new ArrayList<>();

            // Get format for comma
            String comma = feature.getAsEnum(FeatureSetting.COMMA_ENCHANT_COLOR) + COMMA;
            int commaLength = font.width(comma);

            // Process each line of enchants
            int sum = 0;
            StringBuilder builder = new StringBuilder(maxTooltipWidth);
            for (FormattedEnchant enchant : orderedEnchants) {
                // Check if there will be overflow on this line. This will never happen for a single enchant on a line
                if (sum + enchant.getRenderLength() > maxTooltipWidth) {
                    builder.delete(builder.length() - comma.length(), builder.length());
                    insertEnchants.add(CREATE_STYLED_COMPONENT.apply(builder.toString()));
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
                insertEnchants.add(CREATE_STYLED_COMPONENT.apply(builder.toString()));
            }
        }
        // Print 2 enchants per line, separated by a comma, with no enchant lore (typical hypixel behavior)
        else if (layout == EnchantLayout.NORMAL && !hasLore) {
            insertEnchants = new ArrayList<>();

            String comma;
            if (feature.isEnabled(FeatureSetting.HIGHLIGHT_ENCHANTMENTS)) {
                comma = feature.getAsEnum(FeatureSetting.COMMA_ENCHANT_COLOR) + COMMA;
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
                    insertEnchants.add(CREATE_STYLED_COMPONENT.apply(builder.toString()));
                    builder = new StringBuilder(maxTooltipWidth);
                }
                i++;
            }
            // Flush any remaining enchants
            if (builder.length() >= comma.length()) {
                builder.delete(builder.length() - comma.length(), builder.length());
                insertEnchants.add(CREATE_STYLED_COMPONENT.apply(builder.toString()));
            }
        }
        // Prints each enchantment out on a separate line. Also adds the lore if need be
        else {
            // Add each enchantment (one per line) + add enchant lore (if available)
            if (feature.isDisabled(FeatureSetting.HIDE_ENCHANTMENT_LORE)) {
                insertEnchants = new ArrayList<>((hasLore ? 3 : 1) * numEnchants);
                for (FormattedEnchant enchant : orderedEnchants) {
                    // Add enchant
                    insertEnchants.add(CREATE_STYLED_COMPONENT.apply(enchant.getFormattedString()));
                    // Add the enchant lore (if any)
                    insertEnchants.addAll(enchant.getLore());
                }
            } else {
                // Add each enchantment (one per line) and ignore enchant lore
                insertEnchants = new ArrayList<>(numEnchants);
                for (FormattedEnchant enchant : orderedEnchants) {
                    // Add enchant
                    insertEnchants.add(CREATE_STYLED_COMPONENT.apply(enchant.getFormattedString()));
                }
            }
        }

        // Add all of the enchants to the lore
        loreList.addAll(startEnchant, insertEnchants);
        // Cache the result so we can use it again
        LORE_CACHE.updateAfter(loreList);
    }

    /**
     * Adds the progression to the next level to any of the stacking enchants
     * @param loreList        the tooltip being built
     * @param extraAttributes the extra attributes tag of the item
     * @param insertAt        the position at which we should insert the tag
     * @return the index after the point at which we inserted new lines, or {@param insertAt} if we didn't insert anything.
     */
    public static int insertStackingEnchantProgress(List<Component> loreList, CompoundTag extraAttributes, int insertAt) {
        if (extraAttributes == null || Feature.SHOW_STACKING_ENCHANT_PROGRESS.isDisabled()) {
            return insertAt;
        }
        for (EnchantmentsData.Enchant.Stacking enchant : enchants.getStacking().values()) {
            if (extraAttributes.contains(enchant.getNbtNum())) {
                long stackedEnchantNum = extraAttributes.getLongOr(enchant.getNbtNum(), 0L);
                Long nextLevel = enchant.getStackLevel().higher(stackedEnchantNum);
                String statLabel = Translations.getMessage("enchants." + enchant.getStatLabel());
                ColorCode colorCode = Feature.SHOW_STACKING_ENCHANT_PROGRESS.getRestrictedColor();
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
                loreList.add(insertAt++, Component.literal(b.toString()));
            }
        }
        return insertAt;
    }

    /**
     * Helper method to determine whether we should skip this line in parsing the lore.
     * E.g. we want to skip "Breaking Power X" seen on pickaxes.
     * @param itemStack ItemStack to check contains enchantments
     * @param strippedLine the stripped line of lore we are parsing
     * @return {@code true} if no enchants on the line are in the enchants table, {@code false} otherwise.
     */
    public static boolean containsEnchantment(ItemStack itemStack, String strippedLine) {
        Map<String, Integer> enchantments = ItemUtils.getEnchantments(itemStack);
        Map<String, Integer> attributes = ItemUtils.getAttributes(itemStack);

        Matcher m = ENCHANTMENT_PATTERN.matcher(strippedLine);
        while (m.find()) {
            EnchantmentsData.Enchant enchant = enchants.getFromLore(m.group("enchant"));
            if (enchantments == null || enchantments.containsKey(enchant.getNbtName())) {
                if (attributes == null || !attributes.containsKey(enchant.getNbtName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Counts (and optionally removes) vanilla grey enchants that are added on the first 1-2 lines of lore.
     * Removal of the grey enchants is specified by the {@link FeatureSetting#HIDE_GREY_ENCHANTS} feature.
     * @param tooltip the tooltip being built
     * @param item    to which the tooltip corresponds
     * @return an integer denoting the last index of a grey enchantment, or -1 if none were found.
     */
    private static int accountForAndRemoveGreyEnchants(List<Component> tooltip, ItemStack item) {
        // No grey enchants will be added if there is no vanilla enchantments tag
        if (item.getEnchantments() == ItemEnchantments.EMPTY || item.getEnchantments().isEmpty()) {
            return -1;
        }
        int lastGreyEnchant = -1;
        boolean removeGreyEnchants = Feature.ENCHANTMENT_LORE_PARSING.isEnabled(FeatureSetting.HIDE_GREY_ENCHANTS);

        // Start at index 1 since index 0 is the title
        int total = 0;
        for (int i = 1; total < 1 + item.getEnchantments().size() && i < tooltip.size(); total++) { // only a max of 2 gray enchants are possible
            String line = tooltip.get(i).getString();
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
        final Window window = Minecraft.getInstance().getWindow();
        final int mouseX = (int) Minecraft.getInstance().mouseHandler.xpos();
        int tooltipX = mouseX + 12;
        if (tooltipX + maxTooltipWidth + 4 > window.getGuiScaledWidth()) {
            tooltipX = mouseX - 16 - maxTooltipWidth;
            if (tooltipX < 4) {
                if (mouseX > window.getGuiScaledWidth() / 2) {
                    maxTooltipWidth = mouseX - 12 - 8;
                } else {
                    maxTooltipWidth = window.getGuiScaledWidth() - 16 - mouseX;
                }
            }
        }

        if (window.getGuiScaledWidth() > 0 && maxTooltipWidth > window.getGuiScaledWidth()) {
            maxTooltipWidth = window.getGuiScaledWidth();
        }
        return maxTooltipWidth;
    }

    private static final Pattern ENCHANT_SPLITTER = Pattern.compile("§.(?:§.)?[^§]*");

    public static void markCacheDirty() {
        LORE_CACHE.configChanged = true;
    }

    public static Function<String, MutableComponent> CREATE_STYLED_COMPONENT = enchantString -> {
        MutableComponent component = Component.empty();
        Matcher m = ENCHANT_SPLITTER.matcher(enchantString);

        while (m.find()) {
            String part = m.group();
            if (part.contains(ColorCode.CHROMA.toString())) {
                component.append(Component.literal(part).withStyle(
                        Style.EMPTY.withColor(DrawUtils.CHROMA_TEXT_COLOR)
                ));
            } else {
                component.append(part);
            }
        }

        return component.getSiblings().isEmpty() ? Component.literal(enchantString) : component;
    };

    static class Cache {
        @Getter ArrayList<Component> cachedAfter = new ArrayList<>();
        boolean configChanged;
        @Getter private ArrayList<Component> cachedBefore = new ArrayList<>();

        public Cache() {
        }

        public void updateBefore(List<Component> loreBeforeModifications) {
            cachedBefore = new ArrayList<>(loreBeforeModifications);
        }

        public void updateAfter(List<Component> loreAfterModifications) {
            cachedAfter = new ArrayList<>(loreAfterModifications);
            configChanged = false;
        }

        public boolean isCached(List<Component> loreBeforeModifications) {
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
        List<Component> loreDescription;
        String inputFormattedString;


        public FormattedEnchant(EnchantmentsData.Enchant theEnchant, int theLevel, String theFormattedEnchant) {
            enchant = theEnchant;
            level = theLevel;
            inputFormattedString = theFormattedEnchant;
            loreDescription = new ArrayList<>();
        }

        public void addLore(Component lineOfEnchantLore) {
            loreDescription.add(lineOfEnchantLore);
        }

        public List<Component> getLore() {
            return loreDescription;
        }

        @Override
        public int compareTo(FormattedEnchant o) {
            return this.enchant.compareTo(o.enchant);
        }

        public int getRenderLength() {
            return Minecraft.getInstance().font.width(getFormattedString());
        }

        public String getFormattedString() {
            StringBuilder b = new StringBuilder();
            if (Feature.ENCHANTMENT_LORE_PARSING.isEnabled(FeatureSetting.HIGHLIGHT_ENCHANTMENTS)) {
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