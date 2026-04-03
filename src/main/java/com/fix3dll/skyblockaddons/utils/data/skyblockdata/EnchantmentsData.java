package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.ItemType;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

@Getter
public class EnchantmentsData {

    /**
     * Placeholder shown in the missing enchants list when an item has no ultimate enchant.
     * Rendered with ultimate styling (bold) to signal that any one ultimate
     * can be applied, without listing all applicable ultimates individually.
     */
    public static final Enchant ULTIMATE_PLACEHOLDER = new Enchant.Ultimate();

    static {
        ULTIMATE_PLACEHOLDER.loreName = "Ultimate Enchant";
        ULTIMATE_PLACEHOLDER.nbtName = "";
    }

    @SerializedName("NORMAL")
    private HashMap<String, Enchant.Normal> normal = new HashMap<>();
    @SerializedName("ULTIMATE")
    private HashMap<String, Enchant.Ultimate> ultimate = new HashMap<>();
    @SerializedName("STACKING")
    private HashMap<String, Enchant.Stacking> stacking = new HashMap<>();
    /**
     * Groups of mutually exclusive enchants (conflict pools). At most one enchant from each group
     * may be applied to an item at a time. Values are NBT names as they appear in item data.
     */
    @SerializedName("ENCHANT_POOLS")
    private List<List<String>> enchantPools = new ArrayList<>();
    private transient ArrayList<Enchant> allEnchants = null;

    public Enchant getFromLore(String loreName) {
        loreName = loreName.toLowerCase(Locale.US);
        Enchant enchant = normal.get(loreName);
        if (enchant == null) {
            enchant = ultimate.get(loreName);
        }
        if (enchant == null) {
            enchant = stacking.get(loreName);
        }
        if (enchant == null) {
            enchant = new Enchant.Dummy(loreName);
        }
        return enchant;
    }

    public Enchant getFromNbtKey(String nbtKey) {
        if (nbtKey.startsWith("ultimate_")) {
            for (Enchant.Ultimate enchant : ultimate.values()) {
                if (enchant.nbtName.equals(nbtKey)) {
                    return enchant;
                }
            }
        } else {
            String constantTitle = switch (nbtKey) {
                case "PROSECUTE" -> "prosecute";
                case "aiming" -> "dragon tracer";
                case "turbo_coco" -> "turbo-cocoa";
                case "turbo_cactus" -> "turbo-cacti";
                default -> nbtKey.replace("_", " ");
            };
            if (normal.containsKey(constantTitle)) {
                return normal.get(constantTitle);
            } else if (stacking.containsKey(constantTitle)) {
                return stacking.get(constantTitle);
            }
        }
        return null;
    }

    public List<Enchant> getAllEnchants() {
        if (allEnchants == null) {
            allEnchants = new ArrayList<>(normal.size() + ultimate.size() + stacking.size());
            allEnchants.addAll(normal.values());
            allEnchants.addAll(ultimate.values());
            allEnchants.addAll(stacking.values());
        }
        return allEnchants;
    }

    @Getter @ToString
    public static class Enchant implements Comparable<Enchant> {
        /**
         * Orders enchants by type, then alphabetically within each group:
         * <ol>
         *   <li>Ultimates</li>
         *   <li>Stacking</li>
         *   <li>Normal</li>
         * </ol>
         */
        private static final Comparator<Enchant> ENCHANT_ORDER = Comparator
                .comparingInt((Enchant e) -> e.isUltimate() ? 0 : 1)
                .thenComparingInt(e -> e.isStacking() ? 0 : 1)
                .thenComparing(e -> e.loreName);

        @SerializedName("nbtName")
        String nbtName;
        @SerializedName("loreName")
        String loreName;
        @SerializedName("goodLevel")
        int goodLevel;
        @SerializedName("maxLevel")
        int maxLevel;
        /**
         * Item type (matching {@link ItemType}) to which this enchant can be applied,
         * e.g. {@code ["SWORD", "LONGSWORD", "GAUNTLET"]}. An empty list means no applicable types
         * are known and this enchant will never appear in the missing enchants list.
         */
        @SerializedName("appliedTo")
        List<ItemType> appliedTo = List.of();

        public boolean isNormal() {
            return this instanceof Normal;
        }

        public boolean isUltimate() {
            return this instanceof Ultimate;
        }

        public boolean isStacking() {
            return this instanceof Stacking;
        }

        public MutableComponent getFormattedName(int level) {
            return Component.literal(loreName).withStyle(getStyle(level));
        }

        public String getUnformattedName() {
            return loreName;
        }

        public Style getStyle(int level) {
            Feature feature = Feature.ENCHANTMENT_LORE_PARSING;
            FeatureSetting colorSetting, boldSetting, italicSetting, underlinedSetting, strikethroughSetting;
            if (level >= maxLevel) {
                colorSetting         = FeatureSetting.PERFECT_ENCHANT_COLOR;
                boldSetting          = FeatureSetting.PERFECT_ENCHANT_BOLD;
                italicSetting        = FeatureSetting.PERFECT_ENCHANT_ITALIC;
                underlinedSetting    = FeatureSetting.PERFECT_ENCHANT_UNDERLINED;
                strikethroughSetting = FeatureSetting.PERFECT_ENCHANT_STRIKETHROUGH;
            } else if (level > goodLevel) {
                colorSetting         = FeatureSetting.GREAT_ENCHANT_COLOR;
                boldSetting          = FeatureSetting.GREAT_ENCHANT_BOLD;
                italicSetting        = FeatureSetting.GREAT_ENCHANT_ITALIC;
                underlinedSetting    = FeatureSetting.GREAT_ENCHANT_UNDERLINED;
                strikethroughSetting = FeatureSetting.GREAT_ENCHANT_STRIKETHROUGH;
            } else if (level == goodLevel) {
                colorSetting         = FeatureSetting.GOOD_ENCHANT_COLOR;
                boldSetting          = FeatureSetting.GOOD_ENCHANT_BOLD;
                italicSetting        = FeatureSetting.GOOD_ENCHANT_ITALIC;
                underlinedSetting    = FeatureSetting.GOOD_ENCHANT_UNDERLINED;
                strikethroughSetting = FeatureSetting.GOOD_ENCHANT_STRIKETHROUGH;
            } else {
                colorSetting         = FeatureSetting.POOR_ENCHANT_COLOR;
                boldSetting          = FeatureSetting.POOR_ENCHANT_BOLD;
                italicSetting        = FeatureSetting.POOR_ENCHANT_ITALIC;
                underlinedSetting    = FeatureSetting.POOR_ENCHANT_UNDERLINED;
                strikethroughSetting = FeatureSetting.POOR_ENCHANT_STRIKETHROUGH;
            }
            Style baseStyle = Style.EMPTY
                    .withBold(feature.isEnabled(boldSetting))
                    .withItalic(feature.isEnabled(italicSetting))
                    .withUnderlined(feature.isEnabled(underlinedSetting))
                    .withStrikethrough(feature.isEnabled(strikethroughSetting));

            int colorCode = feature.getAsNumber(colorSetting).intValue();
            return colorCode == ColorCode.CHROMA.getColor()
                    ? baseStyle.withColor(DrawUtils.CHROMA_TEXT_COLOR)
                    : baseStyle.withColor(colorCode);
        }

        @Override
        public int compareTo(@NonNull Enchant o) {
            return ENCHANT_ORDER.compare(this, o);
        }

        public static class Normal extends Enchant {
        }

        public static class Ultimate extends Enchant {
            private static final Style ULTIMATE_STYLE = Style.EMPTY
                    .withColor(ColorCode.LIGHT_PURPLE.getColor())
                    .withBold(true);

            @Override
            public Style getStyle(int level) {
                return ULTIMATE_STYLE;
            }
        }

        @Getter
        public static class Stacking extends Enchant {
            String nbtNum;
            String statLabel;
            TreeSet<Long> stackLevel;
        }

        public static class Dummy extends Enchant {
            private static final Style DUMMY_STYLE = Style.EMPTY.withColor(ColorCode.DARK_RED.getColor());

            public Dummy(String name) {
                loreName = name;
                nbtName = name.toLowerCase(Locale.US).replace(" ", "_");
            }

            @Override
            public Style getStyle(int level) {
                return DUMMY_STYLE;
            }
        }
    }

}