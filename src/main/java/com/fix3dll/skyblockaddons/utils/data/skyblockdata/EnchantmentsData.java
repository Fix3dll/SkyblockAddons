package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

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
            String constantTitle = nbtKey.replaceAll("_", " ");
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

        public String getFormattedName(int level) {
            return getFormat(level) + loreName;
        }

        public String getUnformattedName() {
            return loreName;
        }

        public String getFormat(int level) {
            Feature feature = Feature.ENCHANTMENT_LORE_PARSING;
            if (level >= maxLevel) {
                return feature.getAsEnum(FeatureSetting.PERFECT_ENCHANT_COLOR).toString();
            }
            if (level > goodLevel) {
                return feature.getAsEnum(FeatureSetting.GREAT_ENCHANT_COLOR).toString();
            }
            if (level == goodLevel) {
                return feature.getAsEnum(FeatureSetting.GOOD_ENCHANT_COLOR).toString();
            }
            return feature.getAsEnum(FeatureSetting.POOR_ENCHANT_COLOR).toString();
        }

        /**
         * Orders enchants by type in the following way:
         * 1) Ultimates (alphabetically)
         * 2) Stacking (alphabetically)
         * 3) Normal (alphabetically)
         */
        @Override
        public int compareTo(Enchant o) {
            if (this.isUltimate() == o.isUltimate()) {
                if (this.isStacking() == o.isStacking()) {
                    return this.loreName.compareTo(o.loreName);
                }
                return this.isStacking() ? -1 : 1;
            }
            return this.isUltimate() ? -1 : 1;
        }

        public static class Normal extends Enchant {
        }

        public static class Ultimate extends Enchant {
            @Override
            public String getFormat(int level) {
                return "§d§l";
            }
        }

        @Getter
        public static class Stacking extends Enchant {
            String nbtNum;
            String statLabel;
            TreeSet<Long> stackLevel;
        }

        public static class Dummy extends Enchant {

            public Dummy(String name) {
                loreName = name;
                nbtName = name.toLowerCase(Locale.US).replaceAll(" ", "_");
            }

            @Override
            public String getFormat(int level) {
                return ColorCode.DARK_RED.toString();
            }
        }
    }

}
