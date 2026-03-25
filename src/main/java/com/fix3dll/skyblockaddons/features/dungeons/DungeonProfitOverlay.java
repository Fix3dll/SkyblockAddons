package com.fix3dll.skyblockaddons.features.dungeons;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.compat.ReiCompat;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.enchants.EnchantManager;
import com.fix3dll.skyblockaddons.listeners.PlayerListener;
import com.fix3dll.skyblockaddons.listeners.PlayerListener.ResolvedItemId;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils.TextStyle;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ItemsData;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and renders the dungeon chest profit overlay.
 * <p>
 * Supports two distinct inventory contexts:
 * <ul>
 * <li><b>Croesus menu</b> ({@link InventoryType#CROSEUS_CHEST_MENU}) - identifies chest slots by
 * {@link ChestType}, parses each chest's {@code "Contents"} lore, and renders a profit-sorted list.
 * Hovering a row shows a per-item breakdown tooltip styled identically to the chest-inside view.</li>
 * <li><b>Chest inside</b> ({@link InventoryType#CATACOMBS_CHEST}, {@link InventoryType#KUUDRA_CHEST}) - resolves items
 * in the opened chest container via Bazaar and Lowest BIN lookups, then renders total value, profit/loss,
 * and a per-item price breakdown.</li>
 * </ul>
 * <p>
 * Call {@link #parseContainer} when the inventory opens, {@link #clear} when it closes,
 * and {@link #render} each render frame.
 */
public class DungeonProfitOverlay {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private static final ResourceLocation DUNGEON_PROFIT_LOCATION = SkyblockAddons.resourceLocation("dungeon_profit");

    private static final Pattern COIN_COST_PATTERN = Pattern.compile("([\\d,]+) Coins");

    private static final int PADDING     = 10;
    private static final int LINE_HEIGHT = 13;

    private static int lastLowestBinIdentity = 0;
    private static long lastBazaarIdentity   = 0;

    private static PreparedOverlay cachedOverlay = null;

    /**
     * Clears all cached overlay data and resets the REI exclusion zone.
     * Should be called when the dungeon chest inventory is closed.
     */
    public static void clear() {
        cachedOverlay = null;
        setDungeonProfitOverlayBounds(0, 0, 0, 0);
    }

    /**
     * Returns {@code true} if pre-computed overlay data is available for rendering.
     */
    public static boolean hasData() {
        return cachedOverlay != null;
    }

    /**
     * Parses {@code container} according to {@code inventoryType}, caches the result, and pre-computes all layout data
     * needed for rendering. Unrecognized inventory types clear the REI exclusion zone.
     * @param container     the container whose items are to be parsed
     * @param inventoryType determines which parsing strategy to apply
     */
    public static void parseContainer(SimpleContainer container, InventoryType inventoryType) {
        if (Feature.DUNGEON_PROFIT_OVERLAY.isDisabled()) return;

        Font font = MC.font;
        if (inventoryType == InventoryType.CROSEUS_CHEST_MENU) {
            List<CroesusEntry> entries = parseCroesus(container);
            cachedOverlay = buildCroesusOverlay(font, entries);
        } else if (inventoryType == InventoryType.CATACOMBS_CHEST || inventoryType == InventoryType.KUUDRA_CHEST) {
            ChestInsideData data = parseChestInside(container);
            cachedOverlay = buildChestInsideOverlay(font, data);
        } else {
            cachedOverlay = null;
            setDungeonProfitOverlayBounds(0, 0, 0, 0);
        }
    }

    /**
     * Iterates the Croesus menu container, resolving each {@link Items#PLAYER_HEAD} slot to a {@link ChestType} and
     * computing its profit from parsed lore. The resulting list is sorted by descending profit.
     * @param container the Croesus chest overview container
     * @return an immutable, profit-sorted list of {@link CroesusEntry}
     */
    private static List<CroesusEntry> parseCroesus(SimpleContainer container) {
        List<CroesusEntry> entries = new ArrayList<>();
        boolean isKismetUsed = false;

        for (ItemStack stack : container.getItems().reversed()) {
            if (!isKismetUsed && stack.getItem() == Items.REDSTONE_TORCH) {
                for (Component loreLine : ItemUtils.getItemLoreComponent(stack).reversed()) {
                    isKismetUsed = loreLine.visit((style, text) -> {
                        if (style.isStrikethrough() && text.equals("Kismet Feather")) {
                            return Optional.of(true); // Target found, interrupt the traversal
                        }
                        return Optional.empty(); // Target not found, continue traversal
                    }, Style.EMPTY).orElse(false);
                    if (isKismetUsed) break;
                }
            }

            if (stack.getItem() != Items.PLAYER_HEAD) continue;

            Component customName = stack.getCustomName();
            if (customName == null) continue;

            ChestType chestType = ChestType.getByName(customName.getString());
            if (chestType == null) continue;

            ParsedChestLore parsed = parseChestLore(ItemUtils.getItemLoreComponent(stack), isKismetUsed);
            if (parsed == null) continue;

            double totalValue = parsed.items().stream()
                    .mapToDouble(ci -> priceLookup(ci.skyblockId()) * ci.amount()).sum();
            double profit = totalValue - parsed.costToOpen();
            entries.add(new CroesusEntry(chestType, profit, totalValue, parsed));
        }

        entries.sort(Comparator.comparingDouble(CroesusEntry::profit).reversed());
        return List.copyOf(entries);
    }

    /**
     * Parses an opened dungeon chest container into a priced item list.
     * <p>
     * The {@link Items#CHEST} slot whose custom name equals {@code "Open Reward Chest"} provides the opening cost
     * and a fallback item list via its {@code "Contents"} lore. Actual container stacks are the primary source;
     * lore items not accounted for by container stacks are appended as a fallback.
     * <p>
     * Price resolution order per container stack:
     * <ol>
     * <li>Lowest BIN via {@link PlayerListener#resolveLowestBinItemId} → {@link #lookupBinPrice}</li>
     * <li>Bazaar via {@link PlayerListener#resolveBazaarItemId} → {@link #lookupBazaarPrice}</li>
     * </ol>
     * Items with a resolved price of {@code 0} or no listing are silently skipped.
     * @param container the opened dungeon chest container
     * @return {@link ChestInsideData} containing priced items, total value, and opening costs
     */
    private static ChestInsideData parseChestInside(SimpleContainer container) {
        List<ChestItem> loreItems = List.of();
        double costToOpen = 0.0D;
        boolean requiresChestKey = false;
        KuudraKey requiredKuudraKey = null;
        boolean isKismetUsed = false;

        List<ItemStack> containerStacks = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize() - 1; i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty())
                continue;

            if (stack.getItem() == Items.FEATHER && !isKismetUsed) {
                for (Component component : ItemUtils.getItemLoreComponent(stack).reversed()) {
                    if (component.getString().equals("You already rerolled a chest!")) {
                        isKismetUsed = true;
                        break;
                    }
                }
            }

            if (Utils.isGlassPane(stack) || stack.getItem() == Items.BARRIER) continue;

            if (stack.getItem() == Items.CHEST) {
                if (loreItems.isEmpty()) {
                    Component name = stack.getCustomName();
                    if (name != null && "Open Reward Chest".equals(name.getString())) {
                        ParsedChestLore parsed = parseChestLore(ItemUtils.getItemLoreComponent(stack), isKismetUsed);
                        if (parsed != null) {
                            loreItems = parsed.items();
                            costToOpen = parsed.costToOpen();
                            requiresChestKey = parsed.requiresChestKey();
                            requiredKuudraKey = parsed.requiredKuudraKey();
                            isKismetUsed = parsed.kismetUsed();
                        }
                    }
                }
                continue;
            }

            containerStacks.add(stack);
        }

        List<PricedItem> result = new ArrayList<>();
        Set<String> coveredIds = new HashSet<>();

        // Primary: resolve actual container stacks
        for (ItemStack stack : containerStacks) {
            String itemId = ItemUtils.getSkyblockItemID(stack);

            // 1. Lowest BIN path
            ResolvedItemId binResolved = PlayerListener.resolveLowestBinItemId(itemId, stack);
            if (binResolved != null && !coveredIds.contains(binResolved.apiItemId())) {
                double price = lookupBinPrice(binResolved);
                if (price > 0) {
                    result.add(new PricedItem(binResolved.apiItemId(), stack.getCount(), stack.getHoverName(), price));
                    coveredIds.add(binResolved.apiItemId());
                    continue;
                }
            }

            // 2. Bazaar path
            int[] countOut = {0};
            String bazaarId = PlayerListener.resolveBazaarItemId(itemId, stack, main.getItemsData(), countOut);
            if (bazaarId != null && !coveredIds.contains(bazaarId)) {
                double price = lookupBazaarPrice(bazaarId);
                if (price > 0) {
                    int amount = countOut[0] != 0 ? countOut[0] : stack.getCount();
                    Component displayName;
                    if (bazaarId.startsWith("ENCHANTMENT_")) {
                        List<Component> lore = ItemUtils.getItemLoreComponent(stack);
                        displayName = lore.size() > 1 ? lore.getFirst() : stack.getHoverName();
                    } else {
                        displayName = stack.getHoverName();
                    }
                    result.add(new PricedItem(bazaarId, amount, displayName, price));
                    coveredIds.add(bazaarId);
                }
            }
        }

        // Fallback: lore items not accounted for by container stacks
        if (loreItems.size() > containerStacks.size()) {
            for (ChestItem item : loreItems) {
                if (coveredIds.contains(item.skyblockId())) continue;

                double price = priceLookup(item.skyblockId());
                if (price <= 0) continue;

                result.add(new PricedItem(item.skyblockId(), item.amount(), item.displayName(), price));
                coveredIds.add(item.skyblockId());
            }
        }

        double totalValue = result.stream().mapToDouble(p -> p.price() * p.amount()).sum();
        return new ChestInsideData(
                List.copyOf(result), totalValue, costToOpen, requiredKuudraKey, requiresChestKey, isKismetUsed
        );
    }

    /**
     * Parses a chest item's lore into its {@code "Contents"} item list and coin opening cost.
     * <p>
     * Parsing begins after the mandatory {@code "Contents"} header and stops after the {@code "Cost"} section is
     * resolved. Lines are classified as:
     * <ul>
     * <li><b>Enchanted Book</b> - text between {@code "Enchanted Book ("} and {@code ")"} is matched against
     * {@link EnchantManager#ENCHANTMENT_PATTERN} and resolved via {@link PlayerListener#enchantedBookResolution}.</li>
     * <li><b>Essence</b> - matched against {@link PlayerListener#ESSENCE_NAME_PATTERN},
     * producing e.g. {@code ESSENCE_WITHER}.</li>
     * <li><b>Default</b> - plain name looked up in {@link ItemsData#getByName()},
     * falling back to an uppercased underscore-separated ID.</li>
     * </ul>
     * @param lore       raw lore component list from the chest item stack
     * @param kismetUsed {@code true} if a Kismet Feather was used to reroll the chest
     * @return parsed result, or {@code null} if the lore structure is invalid or no opening cost could be determined
     */
    public static ParsedChestLore parseChestLore(List<Component> lore, boolean kismetUsed) {
        if (lore.size() < 2) return null;
        if (!"Contents".equals(lore.getFirst().getString())) return null;

        List<ChestItem> items = new ArrayList<>();
        KuudraKey requiredKuudraKey = null;
        double costToOpen = kismetUsed && Feature.DUNGEON_PROFIT_OVERLAY.isEnabled(FeatureSetting.SHOW_KISMET_FEATHER_LOSS)
                ? priceLookup("KISMET_FEATHER")
                : -1;
        boolean inCost = false;
        boolean requiresChestKey = false;

        for (Component line : lore) {
            String s = line.getString();
            switch (s) {
                case "", "Contents", "Can't open another chest!", "Click to open!" -> {
                    continue;
                }
                case "Already opened!" -> {
                    costToOpen = -1;
                    continue;
                }
                case "Cost" -> {
                    inCost = true;
                    continue;
                }
            }

            if (inCost) {
                if ("FREE".equals(s)) {
                    costToOpen = 0;
                } else if (s.contains("Dungeon Chest Key")) {
                    requiresChestKey = true;
                    costToOpen += priceLookup("DUNGEON_CHEST_KEY");
                } else if ((requiredKuudraKey = KuudraKey.getByName(s)) != null) {
                    costToOpen += requiredKuudraKey.maxPrice();
                } else {
                    Matcher m = COIN_COST_PATTERN.matcher(s);
                    if (m.find()) {
                        try {
                            costToOpen += Integer.parseInt(m.group(1).replace(",", ""));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                continue;
            }

            ChestItem item = parseContentLine(s, line);
            if (item != null) items.add(item);
            else LOGGER.debug("Unrecognized chest lore line: {}", s);
        }

        if (costToOpen == -1.0D) return null;
        return new ParsedChestLore(List.copyOf(items), costToOpen, requiredKuudraKey, requiresChestKey, kismetUsed);
    }

    /**
     * Attempts to resolve a single {@code "Contents"} lore line into a {@link ChestItem}.
     * @param plain plain-text content of the line (formatting codes stripped)
     * @param line  original component, preserved as the display name
     * @return resolved item, or {@code null} if the line could not be identified
     */
    private static ChestItem parseContentLine(String plain, Component line) {
        // Enchanted Book
        if (plain.startsWith("Enchanted Book (") && plain.endsWith(")")) {
            String content = plain.substring("Enchanted Book (".length(), plain.length() - 1);
            Matcher m = EnchantManager.ENCHANTMENT_PATTERN.matcher(content);
            if (!m.find()) return null;

            String apiId = PlayerListener.enchantedBookResolution(null, m);
            if (apiId == null) return null;

            return new ChestItem(apiId, 1, line);
        }

        // Essence
        int[] countOutParam = {1};
        String essenceResolution = PlayerListener.essenceResolution(countOutParam, plain);
        if (essenceResolution != null) {
            return new ChestItem(essenceResolution, countOutParam[0], line);
        }

        String attributeShardResolution = PlayerListener.attributeShardResolution(countOutParam, plain);
        if (attributeShardResolution != null) {
            return new ChestItem(attributeShardResolution, countOutParam[0], line);
        }

        plain = plain.replace("✪", "").trim();

        // Generic item - name-to-ID lookup with uppercase fallback
        ItemsData.Item dataItem = main.getItemsData().getByName().get(plain);
        if (dataItem != null) return new ChestItem(dataItem.getId(), 1, line);

        String fallbackId = plain.toUpperCase(Locale.ENGLISH).replace(" ", "_");
        if (fallbackId.isEmpty() || "DUNGEON_CHEST_KEY".equals(fallbackId)) return null;

        return new ChestItem(fallbackId, 1, line);
    }

    /**
     * Looks up the best available price for {@code skyblockId}. Tries Bazaar instant-sell first, then Lowest BIN.
     * Both lookups use the ID as-is - callers are responsible for passing a correctly resolved ID for the respective
     * system.
     * @return best available price, or {@code 0} if neither source has a listing
     */
    private static double priceLookup(String skyblockId) {
        double price = lookupBazaarPrice(skyblockId);
        if (price > 0)
            return price;
        return lookupBinPrice(new ResolvedItemId(skyblockId, null));
    }

    /**
     * Returns the Bazaar instant-sell price for {@code skyblockId}, or {@code 0} if unavailable.
     */
    private static double lookupBazaarPrice(String skyblockId) {
        var bazaarData = main.getBazaarData();
        if (bazaarData == null) return 0;

        var product = bazaarData.getProducts().get(skyblockId);
        if (product == null) return 0;

        double price = product.getInstaSellPrice();
        return price > 0 ? price : 0;
    }

    /**
     * Returns the Lowest BIN price for {@code resolved}, falling back to the base item ID
     * if the specific variant has no listing. Returns {@code 0} if unavailable.
     */
    private static double lookupBinPrice(ResolvedItemId resolved) {
        Object2DoubleMap<String> binData = main.getLowestBinData();
        if (binData == null) return 0;

        double price = PlayerListener.lookupWithFallback(binData, resolved);
        return price > 0 ? price : 0;
    }

    /**
     * Builds a {@link PreparedOverlay} for the Croesus context. Each entry produces one
     * display row and one pre-computed tooltip line list for hover rendering.
     * @param font    font used for width measurement
     * @param entries profit-sorted Croesus entry list
     * @return pre-computed overlay ready for rendering
     */
    private static PreparedOverlay buildCroesusOverlay(Font font, List<CroesusEntry> entries) {
        List<OverlayRow> rows = new ArrayList<>();
        for (CroesusEntry entry : entries) {
            rows.add(new PairRow(entry.type().getDisplayComponent(), profitComponent(entry.profit())));
        }
        rows = List.copyOf(rows);
        int maxWidth = computeMaxWidth(font, rows);

        List<List<Component>> tooltips = new ArrayList<>();
        for (CroesusEntry entry : entries) {
            List<OverlayRow> tooltipRows = buildInsideStyleRows(
                    entry.totalValue(), entry.profit(), itemRowsFromChestItems(entry.lore())
            );
            int tooltipMax = computeMaxWidth(font, tooltipRows);
            tooltips.add(flattenToTooltipLines(font, tooltipRows, tooltipMax));
        }
        return new PreparedOverlay(rows, maxWidth, entries, List.copyOf(tooltips));
    }

    /**
     * Builds a {@link PreparedOverlay} for the chest-inside context.
     * @param font font used for width measurement
     * @param data aggregated chest data
     * @return pre-computed overlay ready for rendering
     */
    private static PreparedOverlay buildChestInsideOverlay(Font font, ChestInsideData data) {
        List<OverlayRow> rows = buildInsideStyleRows(
                data.totalValue(), data.profit(), itemRowsFromPricedItems(data)
        );
        int maxWidth = computeMaxWidth(font, rows);
        return new PreparedOverlay(rows, maxWidth, null, null);
    }

    /**
     * Builds the shared inside-style row list: two header rows (total value and profit/loss),
     * a blank separator,and one row per item.
     * @param totalValue gross item value for the value header
     * @param profit     net profit for the profit/loss header
     * @param itemRows   list of overlay rows (usually {@link PairRow}) to append after the separator
     * @return immutable row list
     */
    private static List<OverlayRow> buildInsideStyleRows(double totalValue, double profit,
                                                         List<OverlayRow> itemRows) {
        List<OverlayRow> rows = new ArrayList<>();
        rows.add(new PairRow(
                Component.literal(Translations.getMessage("tooltip.valueBin")).withColor(ColorCode.YELLOW.getColor()),
                Component.literal(TextUtils.formatCoin(totalValue, 0) + " coins").withColor(ColorCode.GOLD.getColor())
        ));
        rows.add(new PairRow(
                Component.literal(Translations.getMessage("tooltip.profitLoss")).withColor(ColorCode.YELLOW.getColor()),
                profitComponent(profit)
        ));
        rows.add(new SeparatorRow());
        rows.addAll(itemRows);
        return List.copyOf(rows);
    }

    /**
     * Converts a {@link ChestItem} list into label-value pairs for use in
     * {@link #buildInsideStyleRows}.
     */
    private static List<OverlayRow> itemRowsFromChestItems(ParsedChestLore lore) {
        List<OverlayRow> rows = new ArrayList<>();
        for (ChestItem item : lore.items()) {
            double price = priceLookup(item.skyblockId()) * item.amount();
            rows.add(new PairRow(
                    item.displayName(),
                    Component.literal(TextUtils.formatNumber(price) + " coins").withColor(ColorCode.DARK_GREEN.getColor())
            ));
        }
        appendChestKeyRowIfNeeded(rows, lore.requiresChestKey());
        appendKuudraKeyRowIfNeeded(rows, lore.requiredKuudraKey());
        appendKismetRowIfNeeded(rows, lore.kismetUsed());
        return rows;
    }

    /**
     * Converts a {@link PricedItem} list into label-value pairs for use in {@link #buildInsideStyleRows}.
     */
    private static List<OverlayRow> itemRowsFromPricedItems(ChestInsideData data) {
        List<OverlayRow> rows = new ArrayList<>();
        for (PricedItem item : data.items()) {
            rows.add(new PairRow(
                    item.displayName(),
                    Component.literal(TextUtils.formatNumber(item.price() * item.amount()) + " coins").withColor(ColorCode.DARK_GREEN.getColor())
            ));
        }
        appendChestKeyRowIfNeeded(rows, data.requiresChestKey());
        appendKuudraKeyRowIfNeeded(rows, data.requiredKuudraKey());
        appendKismetRowIfNeeded(rows, data.kismetUsed());
        return rows;
    }

    /**
     * Appends a Dungeon Chest Key cost row in red to {@code rows} if {@code requiresChestKey}
     * is {@code true} and a BIN price is available.
     */
    private static void appendChestKeyRowIfNeeded(List<OverlayRow> rows, boolean requiresChestKey) {
        if (!requiresChestKey) return;
        double keyPrice = priceLookup("DUNGEON_CHEST_KEY");
        rows.add(new PairRow(
                Component.literal("Dungeon Chest Key").withColor(ColorCode.BLUE.getColor()),
                Component.literal(TextUtils.formatNumber(keyPrice) + " coins").withColor(ColorCode.RED.getColor())
        ));
    }

    /**
     * Appends a Kuudra Key cost row in red to {@code rows} if {@code requiredKuudraKey}
     * is not {@code null} and a price is available.
     * @param rows              the list of {@link OverlayRow} representing the UI rows
     * @param requiredKuudraKey the required Kuudra Key tier, or {@code null} if not applicable
     */
    private static void appendKuudraKeyRowIfNeeded(List<OverlayRow> rows, KuudraKey requiredKuudraKey) {
        if (requiredKuudraKey == null) return;
        rows.add(new PairRow(
                requiredKuudraKey.getDisplayComponent(),
                Component.literal(TextUtils.formatNumber(requiredKuudraKey.maxPrice()) + " coins").withColor(ColorCode.RED.getColor())
        ));
    }

    /**
     * Appends a Kismet Feather cost row in red to {@code rows} if {@code kismetUsed}
     * is {@code true} and a price is available.
     * @param rows       the list of {@link OverlayRow} representing the UI rows
     * @param kismetUsed {@code true} if a Kismet Feather was used
     */
    private static void appendKismetRowIfNeeded(List<OverlayRow> rows, boolean kismetUsed) {
        if (Feature.DUNGEON_PROFIT_OVERLAY.isDisabled(FeatureSetting.SHOW_KISMET_FEATHER_LOSS)) return;
        if (!kismetUsed) return;
        double kismetPrice = priceLookup("KISMET_FEATHER");
        rows.add(new PairRow(
                Component.literal("Kismet Feather").withColor(ColorCode.BLUE.getColor()),
                Component.literal(TextUtils.formatNumber(kismetPrice) + " coins").withColor(ColorCode.RED.getColor())
        ));
    }

    /**
     * Returns the pixel width of the widest pair row, computed with a minimum two-space gap between label and value.
     * @param font font used for measurement
     * @param rows row list to scan; {@link SeparatorRow} instances are skipped
     * @return maximum pair row width in pixels, or {@code 0} if no pair rows exist
     */
    private static int computeMaxWidth(Font font, List<OverlayRow> rows) {
        int minGap = font.width("  ");
        return rows.stream()
                .filter(r -> r instanceof PairRow)
                .map(r -> (PairRow) r)
                .mapToInt(p -> p.labelWidth() + minGap + p.valueWidth())
                .max().orElse(0);
    }

    /**
     * Flattens a row list into a {@link List} of {@link Component} suitable for
     * {@link GuiGraphics#setTooltipForNextFrame}. {@link PairRow} entries are joined with space padding;
     * {@link SeparatorRow} entries become empty components.
     * @param font     font used for space-padding calculation
     * @param rows     row list to flatten
     * @param maxWidth target pixel width for right-alignment padding
     * @return immutable list of tooltip-ready components
     */
    private static List<Component> flattenToTooltipLines(Font font, List<OverlayRow> rows, int maxWidth) {
        int unitSpace = font.width("\u200A");
        List<Component> lines = new ArrayList<>();
        for (OverlayRow r : rows) {
            if (r instanceof PairRow(Component label, Component value, int labelWidth, int valueWidth)) {
                double gap = maxWidth - labelWidth - valueWidth;
                int spaces = (int) Math.ceil(gap / unitSpace);
                lines.add(Component.empty().append(label)
                        .append(Component.literal("\u200A".repeat(spaces)))
                        .append(value));
            } else {
                lines.add(Component.empty());
            }
        }
        return List.copyOf(lines);
    }

    /**
     * Renders the profit overlay to the right of the container GUI using the pre-computed {@link PreparedOverlay}.
     * Pair rows are drawn with label flush-left and value flush-right; separator rows advance the cursor without drawing.
     * For the Croesus context, hovering a row triggers a pre-computed tooltip via
     * {@link GuiGraphics#setTooltipForNextFrame}. Updates the REI exclusion zone.
     * No-op if the feature is disabled or no overlay data is cached.
     * @param screen    the currently open screen
     * @param graphics  the current render context
     * @param mouseX    current mouse x in GUI coordinates
     * @param mouseY    current mouse y in GUI coordinates
     * @param tickDelta partial tick for animations (currently unused)
     */
    public static void render(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
        if (Feature.DUNGEON_PROFIT_OVERLAY.isDisabled()) return;
        if (!(screen instanceof ContainerScreen cs)) return;

        PreparedOverlay overlay = cachedOverlay;
        if (overlay == null) return;

        int lowestBinIdentity = System.identityHashCode(main.getLowestBinData());
        long bazaarIdentity   = main.getBazaarData().getLastUpdated();
        boolean isDataSame    = (lastLowestBinIdentity == lowestBinIdentity)
                             && (lastBazaarIdentity    == bazaarIdentity);

        if (!isDataSame && cs.getMenu() instanceof ChestMenu cm) {
            parseContainer((SimpleContainer) cm.getContainer(), main.getInventoryUtils().getInventoryType());
        }

        lastLowestBinIdentity = lowestBinIdentity;
        lastBazaarIdentity    = bazaarIdentity;

        int x = (cs.width + cs.imageWidth) / 2 + 4;
        int y = (cs.height - cs.imageHeight) / 2;
        int maxWidth = overlay.maxWidth();
        int rowSpacing = LINE_HEIGHT - 2;
        int bgWidth = maxWidth + PADDING * 2;
        int height = PADDING + rowSpacing * overlay.rows().size() + PADDING;
        int textX = x + PADDING;
        int textY = y + PADDING + 2;

        Feature feature = Feature.DUNGEON_PROFIT_OVERLAY;
        RenderPipeline pipeline = feature.isChroma() ? DrawUtils.CHROMA_TEXT : RenderPipelines.GUI_TEXTURED;
        graphics.blitSprite(pipeline, DUNGEON_PROFIT_LOCATION, x, y, bgWidth, height, feature.getColor());

        TextStyle previousStyle = (TextStyle) Feature.TEXT_STYLE.getValue();
        Feature.TEXT_STYLE.setValue(TextStyle.STYLE_TWO);

        for (OverlayRow row : overlay.rows()) {
            if (row instanceof PairRow(Component label, Component value, int labelWidth, int valueWidth)) {
                DrawUtils.drawText(graphics, label, textX, textY, -1, true);
                DrawUtils.drawText(graphics, value, textX + maxWidth - valueWidth, textY, -1, true);
            }
            textY += rowSpacing;
        }

        Feature.TEXT_STYLE.setValue(previousStyle);
        setDungeonProfitOverlayBounds(x, y, bgWidth, height);

        if (overlay.entries() != null && overlay.tooltips() != null) {
            List<CroesusEntry> entries = overlay.entries();
            int startY = y + PADDING + 2;
            if (mouseX >= textX && mouseX <= textX + maxWidth && mouseY >= startY) {
                int hoverIndex = (mouseY - startY) / rowSpacing;
                if (hoverIndex >= 0 && hoverIndex < entries.size()) {
                    List<List<Component>> tooltips = overlay.tooltips();
                    graphics.setTooltipForNextFrame(MC.font, tooltips.get(hoverIndex), Optional.empty(), mouseX, mouseY);
                }
            }
        }
    }

    /**
     * Returns a colored {@link MutableComponent} representing {@code profit}.
     * Positive values are rendered green with a {@code +} prefix; negative values red.
     * The coin suffix is always appended.
     */
    private static MutableComponent profitComponent(double profit) {
        String text = (profit >= 0 ? "+" : "-") + TextUtils.formatCoin(Math.abs(profit), 0) + " coins";
        return Component.literal(text)
                .withColor(profit >= 0 ? ColorCode.DARK_GREEN.getColor() : ColorCode.RED.getColor());
    }

    /**
     * Updates the REI button exclusion zone to cover the overlay area.
     * No-op if REI is not loaded.
     */
    private static void setDungeonProfitOverlayBounds(int x, int y, int width, int height) {
        if (!main.isReiLoaded()) return;
        ReiCompat.DUNGEON_PROFIT_OVERLAY.setBounds(x, y, width, height);
    }

    /**
     * A single resolved Croesus chest entry with computed profit.
     * @param type       chest rarity tier
     * @param profit     net profit after subtracting the opening cost
     * @param totalValue gross item value before deducting cost
     * @param lore       parsed lore used to build the hover tooltip
     */
    public record CroesusEntry(ChestType type, double profit, double totalValue, ParsedChestLore lore) {
    }

    /**
     * Aggregated data for an opened dungeon chest container.
     * @param items             immutable list of priced items resolved from the container
     * @param totalValue        gross sum of all item prices
     * @param costToOpen        coin cost parsed from lore; {@code 0} if free or not found
     * @param requiredKuudraKey the required Kuudra key to open this chest, or {@code null}
     * @param requiresChestKey  {@code true} if a Dungeon Chest Key was required to open this chest
     * @param kismetUsed        {@code true} if a Kismet Feather was used to reroll the chest
     */
    public record ChestInsideData(List<PricedItem> items, double totalValue, double costToOpen,
                                  KuudraKey requiredKuudraKey, boolean requiresChestKey, boolean kismetUsed) {
        /**
         * Calculates the net profit.
         * Subtracts the chest opening cost and, if applicable, the current market price of a Kismet Feather.
         * @return the calculated net profit
         */
        public double profit() {
            double net = totalValue - costToOpen;
            if (kismetUsed && Feature.DUNGEON_PROFIT_OVERLAY.isEnabled(FeatureSetting.SHOW_KISMET_FEATHER_LOSS)) {
                net -= priceLookup("KISMET_FEATHER");
            }
            return net;
        }
    }

    /**
     * A resolved and priced item from a dungeon chest container slot.
     * @param skyblockId  Hypixel SkyBlock or Bazaar item ID
     * @param amount      stack or parsed quantity
     * @param displayName display component shown in the overlay
     * @param price       unit price in coins; {@code 0} if the item could not be priced
     */
    public record PricedItem(String skyblockId, int amount, Component displayName, double price) {
    }

    /**
     * @param items             immutable list of parsed chest items
     * @param costToOpen        coin cost to open the chest; {@code 0} if free
     * @param requiredKuudraKey the required Kuudra key to open this chest, or {@code null}
     * @param requiresChestKey  {@code true} if a Dungeon Chest Key is required to open this chest
     * @param kismetUsed        {@code true} if a Kismet Feather was used to reroll the chest
     */
    public record ParsedChestLore(List<ChestItem> items, double costToOpen, KuudraKey requiredKuudraKey,
                                  boolean requiresChestKey, boolean kismetUsed) {
    }

    /**
     * A single item parsed from a chest lore {@code "Contents"} section, prior to price resolution.
     * @param skyblockId  resolved Hypixel SkyBlock item ID
     * @param amount      quantity indicated by the lore line
     * @param displayName original lore component preserved for display
     */
    public record ChestItem(String skyblockId, int amount, Component displayName) {
    }

    /**
     * Pre-computed overlay data built once at parse time and consumed every render frame.
     * Eliminates per-frame layout calculations from the render thread.
     * @param rows     display rows; {@link PairRow} or {@link SeparatorRow}
     * @param maxWidth pixel width of the widest pair row, used for background sizing and right-alignment
     * @param entries  Croesus entry list for per-row hover detection; {@code null} for the chest-inside context
     * @param tooltips pre-computed tooltip line lists, parallel to {@code entries};
     *                 {@code null} for the chest-inside context
     */
    private record PreparedOverlay(
            List<OverlayRow> rows,
            int maxWidth,
            List<CroesusEntry> entries,
            List<List<Component>> tooltips) {
    }

    /**
     * Identifies the rarity tier of a Croesus dungeon chest.
     * Each tier is associated with a colored {@link #getDisplayComponent()} component.
     */
    @Getter
    enum ChestType {
        Wood(ColorCode.WHITE.getColor()),
        Gold(ColorCode.GOLD.getColor()),
        Diamond(ColorCode.AQUA.getColor()),
        Emerald(ColorCode.GREEN.getColor()),
        Obsidian(ColorCode.DARK_PURPLE.getColor()),
        Bedrock(ColorCode.DARK_GRAY.getColor()),
        Free_Chest(ColorCode.WHITE.getColor()),
        Paid_Chest(ColorCode.GOLD.getColor());

        private final Component displayComponent;
        private final String displayName;

        ChestType(int color) {
            this.displayComponent = Component.literal(this.name().replace("_", " ")).withColor(color);
            this.displayName = this.name().replace("_", " ");
        }

        /**
         * Resolves a {@link ChestType} from its display name string, treating underscores as spaces.
         * @param name the plain display name to match
         * @return the matching {@link ChestType}, or {@code null} if not found
         */
        static ChestType getByName(String name) {
            if (name == null || name.isBlank()) return null;
            for (ChestType type : values()) {
                if (type.displayName.equals(name))
                    return type;
            }
            return null;
        }
    }

    /**
     * Identifies the required Kuudra Key tier for opening Kuudra chests.
     * Each tier is associated with a specific item ID and a colored display component.
     */
    @Getter
    enum KuudraKey {
        Kuudra_Key("KUUDRA_TIER_KEY", ColorCode.BLUE.getColor()),
        Hot_Kuudra_Key("KUUDRA_HOT_TIER_KEY", ColorCode.DARK_PURPLE.getColor()),
        Burning_Kuudra_Key("KUUDRA_BURNING_TIER_KEY", ColorCode.DARK_PURPLE.getColor()),
        Fiery_Kuudra_Key("KUUDRA_FIERY_TIER_KEY", ColorCode.DARK_PURPLE.getColor()),
        Infernal_Kuudra_Key("KUUDRA_INFERNAL_TIER_KEY", ColorCode.GOLD.getColor());

        private final String skyblockId;
        private final String displayName;
        private final Component displayComponent;

        KuudraKey(String skyblockId, int color) {
            this.skyblockId = skyblockId;
            this.displayName = this.name().replace("_", " ");
            this.displayComponent = Component.literal(this.displayName).withColor(color)
                    .append(Component.literal(" (Max Price)").withColor(ColorCode.DARK_GRAY.getColor()));
        }

        static KuudraKey getByName(String name) {
            if (name == null || name.isBlank()) return null;
            for (KuudraKey key : values()) {
                if (key.displayName.equals(name)) return key;
            }
            return null;
        }

        /**
         * Calculates the estimated maximum price to craft this tier of Kuudra Key.
         * The calculation is based on the current market prices of its base materials.
         * @return the calculated maximum crafting cost in coins
         */
        // TODO add to remote data instead of hardcode
        double maxPrice() {
            double netherPriceCost = priceLookup("CORRUPTED_NETHER_STAR") * 2;
            double ers = priceLookup("ENCHANTED_RED_SAND");
            double em = priceLookup("ENCHANTED_MYCELIUM");
            double resourcePriceCap = Math.max(ers, em);
            return switch (this) {
                case Kuudra_Key -> 200_000D + resourcePriceCap * 2 + netherPriceCost;
                case Hot_Kuudra_Key -> 400_000D + resourcePriceCap * 6 + netherPriceCost;
                case Burning_Kuudra_Key -> 750_000D + resourcePriceCap * 20 + netherPriceCost;
                case Fiery_Kuudra_Key -> 1_500_000D + resourcePriceCap * 60 + netherPriceCost;
                case Infernal_Kuudra_Key -> 3_000_000D + resourcePriceCap * 120 + netherPriceCost;
            };
        }
    }

    /**
     * Represents a row in the overlay.
     */
    public sealed interface OverlayRow permits PairRow, SeparatorRow {}

    /**
     * A row displaying a label and a value.
     * Pre-computes and caches string widths during parsing for zero-overhead render frames.
     */
    public record PairRow(Component label, Component value, int labelWidth, int valueWidth) implements OverlayRow {
        public PairRow(Component label, Component value) {
            this(label, value, MC.font.width(label), MC.font.width(value));
        }
    }

    /**
     * An empty separator row.
     */
    public record SeparatorRow() implements OverlayRow {}

}