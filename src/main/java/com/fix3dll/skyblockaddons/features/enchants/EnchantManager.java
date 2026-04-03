package com.fix3dll.skyblockaddons.features.enchants;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.ItemType;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils.ItemClassification;
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
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnchantManager {

    private static final Minecraft MC = Minecraft.getInstance();

    // Catches successive [ENCHANT] [ROMAN NUMERALS OR DIGITS], as well as stacking enchants listing total stacked number
    public static final Pattern ENCHANTMENT_PATTERN = Pattern.compile("(?<enchant>[A-Za-z][A-Za-z -]+) (?<levelNumeral>[IVXLCDM]+)(?=, |$| [\\d,]+$)");
    private static final String COMMA = ", ";
    private static final Cache LORE_CACHE = new Cache();
    /**
     * Cached {@code [startEnchant, endEnchant]} inclusive index range of the enchantment section
     * within the tooltip as it appears <em>after</em> parsing and reformatting. The end index
     * therefore reflects the actual number of formatted lines inserted, not the original lore line
     * count. Updated on every cache miss; set to {@code null} when no enchantment section is found
     * or the item has no enchantments.
     */
    private static int[] INDEX_CACHE = null;
    @Getter @Setter private static EnchantmentsData enchants = new EnchantmentsData();

    /**
     * Parses through enchantments, reformats them according to current feature settings, and caches
     * the result for future calls. On a cache hit the lore list is replaced with the cached result
     * directly.
     * <p>
     * The cache check and snapshot precede the no-enchantment early-out so that switching to an
     * un-enchanted item always commits a clean cache entry, preventing stale data from the previous
     * item from leaking through to {@link #getCachedMissingEnchants()}.
     * <p>
     * Enchantments are sorted by type (ultimates → stacking → normal) then alphabetically within
     * each group, and laid out according to {@link FeatureSetting#ENCHANT_LAYOUT}.
     * @param loreList the current item lore; contents are cleared and replaced in-place on a cache
     *                 hit, or reordered and reformatted on a cache miss
     * @param item     the item whose enchantments are being parsed
     * @return a two-element array {@code [startIndex, endIndex]} denoting the inclusive index range
     *         of the enchantment section in the <em>modified</em> {@code loreList} (post-format),
     *         or {@code null} if no enchantment section was found or the item has no enchantments
     */
    public static int[] parseEnchants(List<Component> loreList, ItemStack item) {
        if (LORE_CACHE.isCached(loreList, item)) {
            loreList.clear();
            loreList.addAll(LORE_CACHE.getCachedAfter());
            return INDEX_CACHE;
        }

        LORE_CACHE.updateBefore(loreList, item);

        Map<String, Integer> enchantments = ItemUtils.getEnchantments(item);
        if (enchantments.isEmpty() && SkyblockAddons.getInstance().getInventoryUtils().getInventoryType() != InventoryType.SUPERPAIRS) {
            LORE_CACHE.updateAfter(loreList, Set.of(), item);
            return INDEX_CACHE = null;
        }

        Feature feature = Feature.ENCHANTMENT_LORE_PARSING;
        int startEnchant = -1, endEnchant = -1, maxTooltipWidth = 0;
        Map<String, Integer> attributes = ItemUtils.getAttributes(item);
        for (int i = 0; i < loreList.size(); i++) {
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
            LORE_CACHE.updateAfter(loreList, Set.of(), item);
            return INDEX_CACHE = null;
        }

        // Figure out whether the item tooltip is gonna wrap, and if so, try to make our enchantments wrap
        maxTooltipWidth = correctTooltipWidth(maxTooltipWidth);

        MutableComponent comma = Component.literal(COMMA);
        if (feature.isEnabled(FeatureSetting.HIGHLIGHT_ENCHANTMENTS)
                && feature.isDisabled(FeatureSetting.DEFAULT_COMMA_STYLE)) {
            comma.withStyle(style -> style
                    .withColor(feature.getAsNumber(FeatureSetting.COMMA_ENCHANT_COLOR).intValue())
                    .withBold(feature.isEnabled(FeatureSetting.COMMA_ENCHANT_BOLD))
                    .withItalic(feature.isEnabled(FeatureSetting.COMMA_ENCHANT_ITALIC))
                    .withUnderlined(feature.isEnabled(FeatureSetting.COMMA_ENCHANT_UNDERLINED))
                    .withStrikethrough(feature.isEnabled(FeatureSetting.COMMA_ENCHANT_STRIKETHROUGH))
            );
        }

        int maxEnchantsPerLine = 0;
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
//                    extra:[{color: "light_purple",text: "",bold: 1b},
//                           {color: "light_purple", text: "Ultimate Wise V, ",bold: 1b},...
                    // FIXME Hypixel needs to fix these shitty Components
//                    List<Component> lineSiblings = originalLine.getSiblings();
//                    Component enchantSibling = lineSiblings.size() > counter ? lineSiblings.get(counter) : null;

                    lastEnchant = new FormattedEnchant(enchant, level, findSibling(originalLine, enchant));
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
            maxEnchantsPerLine = Math.max(maxEnchantsPerLine, counter);
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
            INDEX_CACHE = new int[] {startEnchant, startEnchant};
            LORE_CACHE.updateAfter(loreList, orderedEnchants, item);
            return INDEX_CACHE;
        }
        // Remove enchantment lines
        loreList.subList(startEnchant, endEnchant + 1).clear();

        List<Component> insertEnchants;
        boolean isCommaDefault = comma.getStyle().isEmpty();
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
                Component formattedEnchantComponent = enchant.getFormattedComponent();
                loreLine.append(formattedEnchantComponent).append(
                        isCommaDefault ? comma.copy().withStyle(formattedEnchantComponent.getStyle()) : comma
                );
                sum += enchant.getRenderLength() + commaLength;
            }
            // Flush any remaining enchants
            if (MC.font.width(loreLine) >= commaLength) {
                loreLine.getSiblings().removeLast();
                insertEnchants.add(loreLine);
            }
        }
        // Print maxEnchantsPerLine enchants per line, separated by a comma, with no enchant lore (typical hypixel behavior)
        else if (layout == EnchantLayout.NORMAL && !hasLore) {
            insertEnchants = new ArrayList<>();

            int i = 0;
            MutableComponent loreLine = Component.empty();
            for (FormattedEnchant enchant : orderedEnchants) {
                Component formattedEnchantComponent = enchant.getFormattedComponent();
                loreLine.append(formattedEnchantComponent);
                if (i % maxEnchantsPerLine < maxEnchantsPerLine - 1) {
                    loreLine.append(
                            isCommaDefault ? comma.copy().withStyle(formattedEnchantComponent.getStyle()) : comma
                    );
                } else {
                    insertEnchants.add(loreLine);
                    loreLine = Component.empty();
                }
                i++;
            }
            // Flush any remaining enchants
            if (MC.font.width(loreLine) >= commaLength) {
                loreLine.getSiblings().removeLast();
                insertEnchants.add(loreLine);
            }
        }
        // Prints each enchantment out on a separate line. Also adds the lore if need be
        else {
            if (feature.isDisabled(FeatureSetting.HIDE_ENCHANTMENT_LORE)) {
                // Add each enchantment (one per line) + add enchant lore (if available)
                insertEnchants = new ArrayList<>((hasLore ? 3 : 1) * numEnchants);
                for (FormattedEnchant enchant : orderedEnchants) {
                    insertEnchants.add(enchant.getFormattedComponent());
                    insertEnchants.addAll(enchant.getLore());
                }
            } else {
                // Add each enchantment (one per line) and ignore enchant lore
                insertEnchants = new ArrayList<>(numEnchants);
                for (FormattedEnchant enchant : orderedEnchants) {
                    insertEnchants.add(enchant.getFormattedComponent());
                }
            }
        }

        // Add all of the enchants to the lore
        loreList.addAll(startEnchant, insertEnchants);
        // Index range reflects the post-format lines actually inserted into loreList
        INDEX_CACHE = new int[] {startEnchant, startEnchant + insertEnchants.size() - 1};
        // Cache the result so we can use it again
        LORE_CACHE.updateAfter(loreList, orderedEnchants, item);
        return INDEX_CACHE;
    }

    public static Set<FormattedEnchant> getCachedFormattedEnchants() {
        return LORE_CACHE.formattedEnchants;
    }

    /**
     * Returns the set of enchants applicable to the last parsed item's type that are not present
     * on it, computed lazily on first access after each cache miss. Returns an empty set when the
     * item type is unknown or no enchants are missing.
     */
    public static Set<EnchantmentsData.Enchant> getCachedMissingEnchants() {
        return LORE_CACHE.getMissingEnchants();
    }

    /**
     * Returns the missing enchants for the last parsed item as ready-to-insert tooltip
     * {@link Component}s, computed lazily on first access after each cache miss. Enchant names
     * are laid out {@value Cache#MISSING_ENCHANTS_PER_LINE} per line, separated by commas.
     * Returns an empty list when the item type is unknown or no enchants are missing.
     */
    public static List<Component> getCachedMissingEnchantsComponent() {
        return LORE_CACHE.getMissingEnchantsComponent();
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

    private static Component findSibling(Component originalLine, EnchantmentsData.Enchant enchant) {
        List<Component> siblings = originalLine.getSiblings();
        if (siblings.isEmpty()) return null;

        for (Component sibling : siblings) {
            if (sibling.getString().contains(enchant.getLoreName())) {
                return sibling;
            }
        }

        return null;
    }

    /**
     * Signals that the lore cache is stale due to a config change. The next call to
     * {@link #parseEnchants} will perform a full re-parse regardless of whether the lore has changed.
     */
    public static void markCacheDirty() {
        LORE_CACHE.configChanged = true;
    }

    /**
     * Caches the parsed enchant output so that repeat tooltip renders for the same item skip
     * re-parsing entirely. The cache is keyed on {@link ItemStack} identity plus a snapshot of
     * the pre-parse lore list; it is invalidated when either changes or when
     * {@link EnchantManager#markCacheDirty()} is called (e.g. on config changes).
     * <p>
     * Component equality is reference-based, so the element loop in {@link #isCached}
     * uses {@code !=} for a correct, allocation-free comparison.
     */
    static class Cache {
        static final int MISSING_ENCHANTS_PER_LINE = 3;

        private ArrayList<Component> cachedBefore = new ArrayList<>();
        private ArrayList<String> cachedBeforeStrings = new ArrayList<>();
        @Getter ArrayList<Component> cachedAfter = new ArrayList<>();
        boolean configChanged = false;
        Set<FormattedEnchant> formattedEnchants = Collections.emptySet();
        /**
         * Lazily computed set of enchants applicable to this item's type that are absent from the
         * item. {@code null} until first accessed via {@link #getMissingEnchants()} after a cache
         * miss, then stored for the lifetime of this cache entry.
         */
        @Nullable private Set<EnchantmentsData.Enchant> missingEnchants;
        /**
         * Lazily computed list of ready-to-insert tooltip lines built from {@link #missingEnchants}.
         * {@code null} until first accessed via {@link #getMissingEnchantsComponent()} after a cache
         * miss, then stored for the lifetime of this cache entry.
         */
        @Nullable private List<Component> missingEnchantsComponent;
        private ItemStack itemStack;

        public Cache() {
        }

        /**
         * Snapshots the pre-parse lore list and resets all per-item state. Called once per cache
         * miss before any modifications are made to {@code loreBeforeModifications}.
         */
        public void updateBefore(List<Component> loreBeforeModifications, ItemStack itemStack) {
            cachedBefore = new ArrayList<>(loreBeforeModifications);
            this.cachedBeforeStrings = new ArrayList<>(loreBeforeModifications.size());
            for (Component line : loreBeforeModifications) {
                this.cachedBeforeStrings.add(line.getString());
            }
            this.itemStack = itemStack;
            formattedEnchants = Collections.emptySet();
            missingEnchants = null;
            missingEnchantsComponent = null;
        }

        /**
         * Stores the post-parse output. Also called for no-enchant items (with an empty enchant set)
         * to ensure the cache reflects the current item and both {@link #getMissingEnchants()} and
         * {@link #getMissingEnchantsComponent()} return empty results rather than stale data.
         */
        public void updateAfter(List<Component> loreAfterModifications, Set<FormattedEnchant> enchants, ItemStack itemStack) {
            cachedAfter = new ArrayList<>(loreAfterModifications);
            formattedEnchants = enchants;
            this.itemStack = itemStack;
            missingEnchants = null;
            missingEnchantsComponent = null;
            configChanged = false;
        }

        /**
         * Returns the set of enchants applicable to this item's type that are absent from the item.
         * Computed once on first access after each cache miss by diffing {@link #formattedEnchants}
         * against the full enchant list, then stored as an unmodifiable set for subsequent calls.
         * <p>
         * An enchant is excluded from the result if:
         * <ul>
         *   <li>it is already present on the item,</li>
         *   <li>its {@code appliedTo} list does not include this item's classification type,</li>
         *   <li>another member of its conflict pool is already present on the item, or</li>
         *   <li>the item already carries an ultimate enchant (only one ultimate may be applied
         *       per item).</li>
         * </ul>
         * If no ultimate is present and exactly one ultimate is applicable, that enchant is added
         * directly. If multiple ultimates are applicable, {@link EnchantmentsData#ULTIMATE_PLACEHOLDER}
         * is added instead to signal that any one ultimate can be chosen.
         */
        public Set<EnchantmentsData.Enchant> getMissingEnchants() {
            if (missingEnchants != null) return missingEnchants;

            if (itemStack == null) {
                return missingEnchants = Collections.emptySet();
            }

            ItemClassification itemClassification = ItemUtils.getItemClassification(itemStack);
            if (itemClassification == null) {
                return missingEnchants = Collections.emptySet();
            } else if (itemClassification.isDungeon() && itemClassification.type() == ItemType.PICKAXE) {
                // Ignore Dungeonbreaker
                return missingEnchants = Collections.emptySet();
            }

            // Build present-NBT-name set from the already-parsed formattedEnchants
            HashSet<String> presentNbtNames = new HashSet<>(formattedEnchants.size());
            boolean hasUltimate = false;
            for (FormattedEnchant fe : formattedEnchants) {
                String nbtName = fe.getEnchant().getNbtName();
                presentNbtNames.add(nbtName);
                if (!hasUltimate && nbtName.startsWith("ultimate_")) {
                    // If any ultimate enchant is present, no other ultimate can be applied
                    hasUltimate = true;
                }
            }

            // Any pool that has at least one present member blocks all other members from appearing as missing
            HashSet<String> blockedByPool = new HashSet<>();
            for (List<String> pool : enchants.getEnchantPools()) {
                for (String nbtName : pool) {
                    if (presentNbtNames.contains(nbtName)) {
                        blockedByPool.addAll(pool);
                        break;
                    }
                }
            }

            HashSet<EnchantmentsData.Enchant> result = new HashSet<>();
            int applicableUltimate = 0;
            EnchantmentsData.Enchant singleUltimate = null;
            for (EnchantmentsData.Enchant enchant : enchants.getAllEnchants()) {
                if (enchant instanceof EnchantmentsData.Enchant.Dummy) continue;
                List<ItemType> appliedTo = enchant.getAppliedTo();
                if (appliedTo.isEmpty() || !appliedTo.contains(itemClassification.type())) continue;
                if (presentNbtNames.contains(enchant.getNbtName())) continue;
                if (blockedByPool.contains(enchant.getNbtName())) continue;
                // Skip all ultimates - a placeholder is added below if none are present
                if (enchant.isUltimate()) {
                    applicableUltimate++;
                    singleUltimate = enchant;
                    continue;
                }
                result.add(enchant);
            }
            if (!hasUltimate && applicableUltimate > 0) {
                result.add(applicableUltimate == 1 ? singleUltimate : EnchantmentsData.ULTIMATE_PLACEHOLDER);
            }

            return missingEnchants = Collections.unmodifiableSet(result);
        }

        /**
         * Returns the missing enchants as ready-to-insert tooltip {@link Component}s, computed
         * lazily on first access after each cache miss. Delegates to {@link #getMissingEnchants()}
         * for the underlying set, then lays out enchant names {@value MISSING_ENCHANTS_PER_LINE}
         * per line, separated by commas. If an ultimate enchant entry is present (either a real
         * ultimate or {@link EnchantmentsData#ULTIMATE_PLACEHOLDER}), it is rendered first on its
         * own slot in bold. Returns an empty list when there are no missing enchants.
         */
        public List<Component> getMissingEnchantsComponent() {
            if (missingEnchantsComponent != null) return missingEnchantsComponent;

            Set<EnchantmentsData.Enchant> missing = getMissingEnchants();
            if (missing.isEmpty()) {
                return missingEnchantsComponent = Collections.emptyList();
            }

            int grayColor = ColorCode.GRAY.getColor();
            int commaLength = MC.font.width(COMMA);
            MutableComponent comma = Component.literal(COMMA).withColor(grayColor);

            int i = 0;
            List<Component> lines = new ArrayList<>();
            MutableComponent loreLine = Component.empty().withColor(grayColor);

            // Render the ultimate entry first (bold), if present
            for (EnchantmentsData.Enchant enchant : missing) {
                if (enchant.isUltimate()) {
                    loreLine.append(Component.literal(enchant.getUnformattedName())
                            .withStyle(Style.EMPTY.withColor(grayColor).withBold(true))).append(COMMA);
                    i++;
                    break;
                }
            }

            for (EnchantmentsData.Enchant enchant : missing) {
                if (enchant.isUltimate()) continue;
                loreLine.append(enchant.getUnformattedName());
                if (i % MISSING_ENCHANTS_PER_LINE < MISSING_ENCHANTS_PER_LINE - 1) {
                    loreLine.append(comma);
                } else {
                    lines.add(loreLine);
                    loreLine = Component.empty().withColor(grayColor);
                }
                i++;
            }
            // Flush any remaining enchants
            if (MC.font.width(loreLine) >= commaLength) {
                loreLine.getSiblings().removeLast();
                lines.add(loreLine);
            }

            return missingEnchantsComponent = Collections.unmodifiableList(lines);
        }

        /**
         * Returns {@code true} if the cache entry is valid for the given lore list and item.
         * <p>
         * Validates the item identity, lore size, and line contents. It uses a fast reference check
         * followed by a pre-calculated string comparison to maintain performance and handle edge cases.
         * @param loreList  the unmodified lore list passed to {@link EnchantManager#parseEnchants}
         * @param itemStack the item being rendered
         * @return {@code true} if the stored post-parse result can be reused, {@code false} otherwise
         */
        public boolean isCached(List<Component> loreList, ItemStack itemStack) {
            if (configChanged || itemStack != this.itemStack || loreList.size() != cachedBefore.size()) {
                return false;
            }
            for (int i = 0; i < loreList.size(); i++) {
                Component currentLine = loreList.get(i);
                Component cachedLine = cachedBefore.get(i);
                if (currentLine == cachedLine) {
                    continue;
                }
                if (!loreList.get(i).getString().equals(cachedBeforeStrings.get(i))) {
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
    @Getter
    public static class FormattedEnchant implements Comparable<FormattedEnchant> {
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

        public FormattedEnchant(EnchantmentsData.Enchant enchant, int level, @Nullable Component originalSibling) {
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