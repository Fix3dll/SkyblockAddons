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

public class EnchantManager {

    private static final Minecraft MC = Minecraft.getInstance();

    // Catches successive [ENCHANT] [ROMAN NUMERALS OR DIGITS], as well as stacking enchants listing total stacked number
    private static final Pattern ENCHANTMENT_PATTERN = Pattern.compile("(?<enchant>[A-Za-z][A-Za-z -]+) (?<levelNumeral>[IVXLCDM]+)(?=, |$| [\\d,]+$)");
    private static final Pattern GREY_ENCHANT_PATTERN = Pattern.compile("^(Respiration|Aqua Affinity|Depth Strider|Efficiency).*");
    private static final String COMMA = ", ";
    private static final Cache LORE_CACHE = new Cache();
    /**
     * Cached {@code [startEnchant, endEnchant]} inclusive index range of the enchantment section within
     * the tooltip after the last successful parse. Updated on every cache miss, set to {@code null} when
     * no enchantment section is found.
     */
    private static int[] INDEX_CACHE = null;
    @Setter private static EnchantmentsData enchants = new EnchantmentsData();

    /**
     * Parses through enchantments, reformats them according to current feature settings, and caches the
     * result for future calls. On a cache hit the lore list is replaced with the cached result directly.
     * <p>
     * Enchantments are sorted by type (ultimates → stacking → normal) then alphabetically within each
     * group, and laid out according to {@link FeatureSetting#ENCHANT_LAYOUT}.
     * @param loreList the current item lore; contents are cleared and replaced in-place on a cache hit,
     *                 or reordered and reformatted on a cache miss
     * @param item     the item whose enchantments are being parsed
     * @return a two-element array {@code [startIndex, endIndex]} denoting the inclusive index range of
     *         the enchantment section in the modified {@code loreList}, or {@code null} if no enchantment
     *         section was found or the item has no enchantments
     */
    public static int[] parseEnchants(List<Component> loreList, ItemStack item) {
        Map<String, Integer> enchantments = ItemUtils.getEnchantments(item);
        if (enchantments.isEmpty() && SkyblockAddons.getInstance().getInventoryUtils().getInventoryType() != InventoryType.SUPERPAIRS) {
            return null;
        }

        // Return the cached result if the lore has not changed since the last parse
        if (LORE_CACHE.isCached(loreList)) {
            loreList.clear();
            loreList.addAll(LORE_CACHE.getCachedAfter());
            return INDEX_CACHE;
        }

        // Snapshot lore before modifications so we can detect changes on future calls
        LORE_CACHE.updateBefore(loreList);

        Feature feature = Feature.ENCHANTMENT_LORE_PARSING;
        int startEnchant = -1, endEnchant = -1, maxTooltipWidth = 0;
        int indexOfLastGreyEnchant = accountForAndRemoveGreyEnchants(loreList, item);
        Map<String, Integer> attributes = ItemUtils.getAttributes(item);
        for (int i = indexOfLastGreyEnchant == -1 ? 0 : indexOfLastGreyEnchant + 1; i < loreList.size(); i++) {
            Component line = loreList.get(i);
            String strippedLine = TextUtils.stripColor(line.getString());
            if (startEnchant == -1) {
                if (containsEnchantment(enchantments, attributes, strippedLine)) {
                    startEnchant = i;
                }
            }
            // Assume enchants end with an empty line "break"
            else if (strippedLine.isBlank() && endEnchant == -1) {
                endEnchant = i - 1;
            }
            // Track max tooltip width outside the enchants section, used later for line wrapping decisions
            if (startEnchant == -1 || endEnchant != -1) {
                maxTooltipWidth = Math.max(MC.font.width(line), maxTooltipWidth);
            }
        }
        if (enchantments.isEmpty() && endEnchant == -1) {
            endEnchant = startEnchant;
        }
        if (endEnchant == -1) {
            LORE_CACHE.updateAfter(loreList);
            INDEX_CACHE = null;
            return INDEX_CACHE;
        }
        INDEX_CACHE = new int[] {startEnchant, endEnchant};

        // Figure out whether the item tooltip is gonna wrap, and if so, try to make our enchantments wrap
        maxTooltipWidth = correctTooltipWidth(maxTooltipWidth);

        // Get format for comma
        boolean highlightEnchantments = feature.isEnabled(FeatureSetting.HIGHLIGHT_ENCHANTMENTS);
        boolean commaFormatted = false;
        MutableComponent comma = highlightEnchantments
                ? Component.literal(COMMA).withColor(feature.getAsNumber(FeatureSetting.COMMA_ENCHANT_COLOR).intValue())
                : Component.literal(COMMA);

        boolean hasLore = false;
        TreeSet<FormattedEnchant> orderedEnchants = new TreeSet<>();
        FormattedEnchant lastEnchant = null;
        // Order all enchants
        for (int i = startEnchant; i <= endEnchant; i++) {
            Component originalLine = loreList.get(i);
            String unformattedLine = TextUtils.stripColor(originalLine.getString());
            Matcher m = ENCHANTMENT_PATTERN.matcher(unformattedLine);
            boolean containsEnchant = false;
            int counter = 0;
            while (m.find()) {
                // Pull out the enchantment and the enchantment level from lore
                EnchantmentsData.Enchant enchant = enchants.getFromLore(m.group("enchant"));
                int level = RomanNumeralParser.parseNumeral(m.group("levelNumeral"));
                if (enchant != null) {
                    List<Component> lineSiblings = originalLine.getSiblings();
                    Component enchantSibling = lineSiblings.size() > counter ? lineSiblings.get(counter) : null;

                    // Inherit comma style from the first normal enchant sibling when not highlighting
                    if (!commaFormatted && enchantSibling != null && !highlightEnchantments && enchant.isNormal()) {
                        commaFormatted = true;
                        comma.withStyle(enchantSibling.getStyle());
                    }
                    lastEnchant = new FormattedEnchant(enchant, level, enchantSibling);
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
                    counter++;
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
            return INDEX_CACHE;
        }
        // Remove enchantment lines
        loreList.subList(startEnchant, endEnchant + 1).clear();

        List<Component> insertEnchants;
        int commaLength = MC.font.width(COMMA);
        RegistrableEnum layout = feature.getAsEnum(FeatureSetting.ENCHANT_LAYOUT);
        // Pack as many enchantments as we can into one line (while not overstuffing it)
        if (layout == EnchantLayout.COMPRESS && numEnchants != 1) {
            insertEnchants = new ArrayList<>();

            // Process each line of enchants
            int sum = 0;
            MutableComponent loreLine = Component.empty();
            for (FormattedEnchant enchant : orderedEnchants) {
                // Check if there will be overflow on this line. This will never happen for a single enchant on a line
                if (sum + enchant.getRenderLength() > maxTooltipWidth) {
                    loreLine.getSiblings().removeLast();
                    insertEnchants.add(loreLine);
                    loreLine = Component.empty();
                    sum = 0;
                }
                // Add to enchant followed by a comma
                loreLine.append(enchant.getFormattedComponent()).append(comma);
                sum += enchant.getRenderLength() + commaLength ;
            }
            // Flush any remaining enchants
            if (MC.font.width(loreLine) >= commaLength ) {
                loreLine.getSiblings().removeLast();
                insertEnchants.add(loreLine);
            }
        }
        // Print 2 enchants per line, separated by a comma, with no enchant lore (typical hypixel behavior)
        else if (layout == EnchantLayout.NORMAL && !hasLore) {
            insertEnchants = new ArrayList<>();

            // Process each line of enchants
            int i = 0;
            MutableComponent loreLine = Component.empty();
            for (FormattedEnchant enchant : orderedEnchants) {
                // Add enchant
                loreLine.append(enchant.getFormattedComponent());
                // Add a comma for the first on the row, followed by a comma
                if (i % 2 == 0) {
                    loreLine.append(comma);
                }
                // Create a new line
                else {
                    insertEnchants.add(loreLine);
                    loreLine = Component.empty();
                }
                i++;
            }
            // Flush any remaining enchants
            if (MC.font.width(loreLine) >= commaLength ) {
                loreLine.getSiblings().removeLast();
                insertEnchants.add(loreLine);
            }
        }
        // Prints each enchantment out on a separate line. Also adds the lore if need be
        else {
            // Add each enchantment (one per line) + add enchant lore (if available)
            if (feature.isDisabled(FeatureSetting.HIDE_ENCHANTMENT_LORE)) {
                insertEnchants = new ArrayList<>((hasLore ? 3 : 1) * numEnchants);
                for (FormattedEnchant enchant : orderedEnchants) {
                    // Add enchant
                    insertEnchants.add(enchant.getFormattedComponent());
                    // Add the enchant lore (if any)
                    insertEnchants.addAll(enchant.getLore());
                }
            } else {
                // Add each enchantment (one per line) and ignore enchant lore
                insertEnchants = new ArrayList<>(numEnchants);
                for (FormattedEnchant enchant : orderedEnchants) {
                    // Add enchant
                    insertEnchants.add(enchant.getFormattedComponent());
                }
            }
        }

        // Add all of the enchants to the lore
        loreList.addAll(startEnchant, insertEnchants);
        // Cache the result so we can use it again
        LORE_CACHE.updateAfter(loreList);
        return INDEX_CACHE;
    }

    /**
     * Appends stacking enchant progress lines to {@code loreListCache} for each stacking enchant
     * whose NBT counter is present on the item.
     * @param loreListCache   the tooltip cache being built; components are appended in-place
     * @param extraAttributes the {@code ExtraAttributes} NBT compound tag of the item,
     *                        or {@code null} if not present (method returns immediately)
     */
    public static void insertStackingEnchantProgress(List<Component> loreListCache, CompoundTag extraAttributes) {
        if (extraAttributes == null || Feature.SHOW_STACKING_ENCHANT_PROGRESS.isDisabled()) {
            return;
        }
        for (EnchantmentsData.Enchant.Stacking enchant : enchants.getStacking().values()) {
            if (!extraAttributes.contains(enchant.getNbtNum())) continue;

            long stackedEnchantNum = extraAttributes.getLongOr(enchant.getNbtNum(), 0L);
            Long nextLevel = enchant.getStackLevel().higher(stackedEnchantNum);
            String statLabel = Translations.getMessage("enchants." + enchant.getStatLabel());
            ColorCode colorCode = Feature.SHOW_STACKING_ENCHANT_PROGRESS.getRestrictedColor();
            if (colorCode == null) colorCode = ColorCode.WHITE;
            MutableComponent component = Component.literal(statLabel + ": ").withColor(ColorCode.GRAY.getColor());

            if (nextLevel == null) {
                // §7Expertise Kills: §a5000000000 §7(Maxed)
                component.append(Component.literal(TextUtils.abbreviate(stackedEnchantNum)).withColor(colorCode.getColor())
                        .append(Component.literal(" (" + Translations.getMessage("enchants.maxed") + ")").withColor(ColorCode.GRAY.getColor())));
            } else {
                // §7Expertise Kills: §a500 §7/ 1k
                String format = TextUtils.formatNumber(stackedEnchantNum);
                component.append(Component.literal(format).withColor(colorCode.getColor())
                        .append(Component.literal(" / ").withColor(ColorCode.GRAY.getColor()))
                        .append(TextUtils.abbreviate(nextLevel)).withColor(colorCode.getColor()));
            }

            loreListCache.add(component);
        }
    }

    /**
     * Determines whether the given lore line contains an enchantment that is present on the item.
     * Used to skip lines like "Breaking Power X" on pickaxes, which pattern-match as enchants
     * but are not in the enchantments table or belong only to the item's attributes.
     * @param enchantments pre-fetched enchantment map for the item, keyed by NBT name
     * @param attributes   pre-fetched attribute map for the item, keyed by NBT name; any token that
     *                     resolves to an enchant also present as an attribute is excluded to avoid
     *                     false positives on attribute-named lines
     * @param strippedLine the color-stripped lore line to check
     * @return {@code true} if at least one token on the line matches a known enchant on this item,
     *         {@code false} otherwise
     */
    public static boolean containsEnchantment(Map<String, Integer> enchantments, Map<String, Integer> attributes, String strippedLine) {
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
     * Counts (and optionally removes) vanilla grey enchants prepended to the first 1-2 lore lines.
     * Removal is controlled by {@link FeatureSetting#HIDE_GREY_ENCHANTS}.
     * @param tooltip the tooltip being built
     * @param item    the item to which the tooltip corresponds
     * @return the last index of a grey enchantment line, or {@code -1} if none were found
     *         or if all grey enchants were removed
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

    /**
     * Adjusts {@code maxTooltipWidth} to account for tooltip overflow at the screen edges, mirroring
     * Minecraft's own tooltip positioning logic. When the tooltip is shifted left due to right-edge
     * overflow, this ensures enchantment line wrapping matches the adjusted width rather than the
     * original measured width.
     * @param maxTooltipWidth the pixel width of the widest non-enchant tooltip line
     * @return the corrected max width to use for enchantment line wrapping
     */
    private static int correctTooltipWidth(int maxTooltipWidth) {
        // Figure out whether the item tooltip is gonna wrap, and if so, try to make our enchantments wrap
        final Window window = MC.getWindow();
        final int mouseX = (int) MC.mouseHandler.xpos();
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
    /**
     * Reusable {@link Matcher} for {@link #ENCHANT_SPLITTER}. Reset via {@link Matcher#reset(CharSequence)}
     * before each use to avoid allocating a new instance per enchant string on the render thread.
     * Must only be accessed from the render thread.
     */
    private static final Matcher ENCHANT_SPLITTER_MATCHER = ENCHANT_SPLITTER.matcher("");

    /**
     * Signals that the lore cache is stale due to a config change. The next call to
     * {@link #parseEnchants} will perform a full re-parse regardless of whether the lore has changed.
     */
    public static void markCacheDirty() {
        LORE_CACHE.configChanged = true;
    }

    /**
     * Converts a legacy {@code §}-formatted enchant string into a {@link MutableComponent} tree,
     * replacing any chroma color codes with {@link DrawUtils#CHROMA_TEXT_COLOR}.
     * Falls back to a plain literal if the string contains no {@code §} segments.
     */
    public static Function<String, MutableComponent> CREATE_STYLED_COMPONENT = enchantString -> {
        MutableComponent component = Component.empty();
        Matcher m = ENCHANT_SPLITTER_MATCHER.reset(enchantString);

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

    /**
     * Caches the item lore before and after enchant parsing so that repeat tooltip renders for the
     * same item can skip the full parse. The cache is invalidated either when the pre-parse lore
     * differs from the stored snapshot, or when {@link EnchantManager#markCacheDirty()} is called
     * (e.g. on config changes that affect enchant formatting).
     */
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

        /**
         * Returns {@code true} if {@code loreBeforeModifications} matches the snapshot taken at the
         * last {@link #updateBefore} call and no config change has been signalled via
         * {@link EnchantManager#markCacheDirty()}.
         * @param loreBeforeModifications the unmodified lore list to compare against the cached snapshot
         * @return {@code true} if the cache is valid and the stored post-parse result can be reused,
         * {@code false} if a full re-parse is needed
         */
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

    /**
     * Holds a single enchantment extracted from lore during a parse cycle, along with its computed
     * display state. Instances are created fresh on every cache miss and discarded when the lore cache
     * is invalidated, so cached fields never hold stale data across parse cycles.
     */
    static class FormattedEnchant implements Comparable<FormattedEnchant> {
        EnchantmentsData.Enchant enchant;
        int level;
        List<Component> loreDescription;
        /**
         * The original {@link Component} sibling from the source lore line that corresponds to this
         * enchant entry. Used to inherit the input formatting style when
         * {@link FeatureSetting#HIGHLIGHT_ENCHANTMENTS} is disabled, preserving styling applied by
         * other mods. {@code null} if no matching sibling could be located.
         */
        Component originalSibling;
        /**
         * Cached result of {@link #getFormattedComponent()}. Populated on first call and reused within
         * the same parse cycle. {@link FormattedEnchant} instances are recreated each time the lore
         * cache is invalidated, so this never holds stale data across parse cycles.
         */
        private MutableComponent cachedFormattedComponent = null;
        /**
         * Cached result of {@link #getRenderLength()}. {@code -1} indicates not yet computed.
         * Always derived from {@link #cachedFormattedComponent}, so it is consistent with the
         * formatted output.
         */
        private int cachedRenderLength = -1;

        public FormattedEnchant(EnchantmentsData.Enchant enchant, int level, Component originalSibling) {
            this.enchant = enchant;
            this.level = level;
            this.originalSibling = originalSibling;
            this.loreDescription = new ArrayList<>();
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

        /**
         * Returns the pixel width of this enchant's formatted component as measured by
         * {@link net.minecraft.client.gui.Font}. Result is cached after the first call.
         */
        public int getRenderLength() {
            if (cachedRenderLength == -1) {
                cachedRenderLength = MC.font.width(getFormattedComponent());
            }
            return cachedRenderLength;
        }

        /**
         * Builds and returns the formatted {@link Component} for this enchant, including its level.
         * <p>
         * When {@link FeatureSetting#HIGHLIGHT_ENCHANTMENTS} is disabled, the component is built from
         * the unformatted enchant name and inherits the style of {@link #originalSibling} if available,
         * preserving formatting applied by other mods.
         * When highlighting is enabled, {@link EnchantmentsData.Enchant#getFormattedName(int)} supplies
         * the color style and the level string is appended with the same style.
         * <p>
         * Result is cached after the first call.
         */
        public Component getFormattedComponent() {
            if (cachedFormattedComponent != null) return cachedFormattedComponent;

            String level = Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS.isEnabled()
                    ? Integer.toString(this.level)
                    : RomanNumeralParser.integerToRoman(this.level);

            if (Feature.ENCHANTMENT_LORE_PARSING.isDisabled(FeatureSetting.HIGHLIGHT_ENCHANTMENTS)) {
                cachedFormattedComponent = Component.literal(enchant.getUnformattedName() + " " + level);

                if (originalSibling != null) {
                    return cachedFormattedComponent.withStyle(originalSibling.getStyle());
                }
                return cachedFormattedComponent;
            }

            MutableComponent component = enchant.getFormattedName(this.level);
            component.append(Component.literal(" " + level).withStyle(component.getStyle()));
            cachedFormattedComponent = component;
            return cachedFormattedComponent;
        }
    }

}