package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeSet;

@Getter
public class EnchantmentsData {
    @SerializedName("NORMAL")
    private HashMap<String, Enchant.Normal> normal = new HashMap<>();
    @SerializedName("ULTIMATE")
    private HashMap<String, Enchant.Ultimate> ultimate = new HashMap<>();
    @SerializedName("STACKING")
    private HashMap<String, Enchant.Stacking> stacking = new HashMap<>();

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
            String constantTitle = nbtKey.replace("_", " ");
            if (normal.containsKey(constantTitle)) {
                return normal.get(constantTitle);
            } else if (stacking.containsKey(constantTitle)) {
                return stacking.get(constantTitle);
            }
        }
        return null;
    }

    @Getter
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

        String nbtName;
        String loreName;
        int goodLevel;
        int maxLevel;

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
            FeatureSetting setting;
            if (level >= maxLevel)       setting = FeatureSetting.PERFECT_ENCHANT_COLOR;
            else if (level > goodLevel)  setting = FeatureSetting.GREAT_ENCHANT_COLOR;
            else if (level == goodLevel) setting = FeatureSetting.GOOD_ENCHANT_COLOR;
            else                         setting = FeatureSetting.POOR_ENCHANT_COLOR;
            int color = feature.getAsNumber(setting).intValue();
            if (color == ColorCode.CHROMA.getColor()) {
                return Style.EMPTY.withColor(DrawUtils.CHROMA_TEXT_COLOR);
            } else {
                return Style.EMPTY.withColor(color);
            }
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
                nbtName = name.toLowerCase(Locale.US).replaceAll(" ", "_");
            }

            @Override
            public Style getStyle(int level) {
                return DUMMY_STYLE;
            }
        }
    }

}
