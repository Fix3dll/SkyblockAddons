package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.util.StringUtil;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Top-level response from {@code GET /v2/resources/skyblock/items}.
 *
 * <pre>
 * ItemsData
 * ├── success      : boolean
 * ├── lastUpdated  : long  (Unix epoch ms)
 * └── items        : List&lt;Item&gt;
 *     └── Item
 *         ├── id              : String         (e.g. "ARACK")
 *         ├── name            : String
 *         ├── material        : String         (Minecraft material)
 *         ├── durability      : int            (optional)
 *         ├── tier            : String         (COMMON … SPECIAL)
 *         ├── category        : String         (optional)
 *         ├── description     : String         (optional)
 *         ├── color           : String         (optional, R,G,B)
 *         ├── itemModel       : String         (optional)
 *         ├── npcSellPrice    : double         (optional)
 *         ├── unstackable     : boolean        (optional)
 *         ├── raritySalvageable : boolean      (optional)
 *         ├── soulbound       : String         (optional, e.g. "COOP")
 *         ├── stats           : Map&lt;String, Double&gt;  (optional)
 *         ├── skin            : Skin           (optional, SKULL_ITEM texture)
 *         ├── salvages        : List&lt;Cost&gt;     (optional)
 *         ├── dungeonItemConversionCost : Cost (optional)
 *         ├── upgradeCosts    : List&lt;List&lt;Cost&gt;&gt; (optional)
 *         ├── requirements    : List&lt;Requirement&gt; (optional)
 *         └── museumData      : MuseumData     (optional)
 *             ├── Skin
 *             │   ├── value     : String
 *             │   └── signature : String
 *             ├── Cost
 *             │   ├── type        : String  (ESSENCE | ITEM | COINS)
 *             │   ├── essenceType : String  (optional)
 *             │   ├── itemId      : String  (optional)
 *             │   └── amount      : int
 *             ├── Requirement
 *             │   ├── type           : String  (SLAYER | SKILL | COLLECTION …)
 *             │   ├── slayerBossType : String  (optional)
 *             │   └── level          : int
 *             └── MuseumData
 *                 ├── donationXp          : int
 *                 ├── type                : String
 *                 ├── gameStage           : String
 *                 ├── mappedItemIds       : List&lt;String&gt;
 *                 └── armorSetDonationXp  : Map&lt;String, Integer&gt; (optional)
 * </pre>
 */
@Getter
@JsonAdapter(ItemsData.Deserializer.class)
public class ItemsData {

    @SerializedName("success")
    private boolean success = false;
    @SerializedName("lastUpdated")
    private long lastUpdated = 0L;
    /* Instead of it we are using 'itemMap'
    @SerializedName("items")
    private List<Item> items = List.of();
    */
    @Getter
    private Map<String, Item> itemMap = Map.of();

    static class Deserializer implements JsonDeserializer<ItemsData> {
        @Override
        public ItemsData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            JsonObject obj = json.getAsJsonObject();
            ItemsData data = new ItemsData();
            data.success     = obj.get("success").getAsBoolean();
            data.lastUpdated = obj.get("lastUpdated").getAsLong();

            Map<String, Item> map = new HashMap<>();
            for (JsonElement el : obj.getAsJsonArray("items")) {
                Item item = ctx.deserialize(el, Item.class);
                if (!StringUtil.isNullOrEmpty(item.getId())) {
                    map.put(item.getId(), item);
                }
            }
            data.itemMap = Collections.unmodifiableMap(map);
            return data;
        }
    }

    /** A single SkyBlock item definition. All optional fields may be {@code null}. */
    @Getter
    public static class Item {

        /** Unique SkyBlock item ID, e.g. {@code "ARACK"} or {@code "INK_SACK:3"}. */
        @SerializedName("id")
        private String id;

        /** Display name, e.g. {@code "Arack"}. */
        @SerializedName("name")
        private String name;

        /** Minecraft base material, e.g. {@code "IRON_SWORD"} or {@code "SKULL_ITEM"}. */
        @SerializedName("material")
        private String material;

        /** Minecraft item durability/damage value. Primarily used for {@code SKULL_ITEM} (always 3). */
        @SerializedName("durability")
        private int durability;

        /** Rarity tier: {@code COMMON}, {@code UNCOMMON}, {@code RARE}, {@code EPIC},
         *  {@code LEGENDARY}, {@code MYTHIC}, {@code DIVINE}, {@code SPECIAL}. */
        @SerializedName("tier")
        private SkyblockRarity tier;

        /** Item category, e.g. {@code "SWORD"}, {@code "HELMET"}, {@code "REFORGE_STONE"}. */
        @SerializedName("category")
        private String category;

        /** Lore description. May contain {@code %%} formatting codes. */
        @SerializedName("description")
        private String description;

        /** Leather armor dye color in {@code "R,G,B"} format, e.g. {@code "139,0,0"}. */
        @SerializedName("color")
        private String colorString;

        @Expose(serialize = false, deserialize = false)
        @Getter(AccessLevel.NONE)
        private Integer colorInteger;

        /** Overrides the default Minecraft item model, e.g. {@code "minecraft:bamboo"}. */
        @SerializedName("item_model")
        private String itemModel;

        /** NPC sell price in coins. {@code 0} if not sold to NPCs. */
        @SerializedName("npc_sell_price")
        private double npcSellPrice;

        /** If {@code true}, the item cannot be stacked. */
        @SerializedName("unstackable")
        private boolean unstackable;

        /** If {@code true}, the item can be salvaged to a lower rarity. */
        @SerializedName("rarity_salvageable")
        private boolean raritySalvageable;

        /** Soulbound type, e.g. {@code "COOP"} or {@code "SOLO"}. {@code null} if not soulbound. */
        @SerializedName("soulbound")
        private String soulbound;

        /** Base item stats, e.g. {@code {"DAMAGE": 69, "HEALTH": 2000}}. Keys are stat IDs. */
        @SerializedName("stats")
        private Map<String, Double> stats;

        /** Skull texture data. Present only when {@code material} is {@code "SKULL_ITEM"}. */
        @SerializedName("skin")
        private Skin skin;

        /** Essence or item costs returned when salvaging this item. */
        @SerializedName("salvages")
        private List<Cost> salvages = Collections.emptyList();

        /** Cost to convert this item into its dungeon variant. */
        @SerializedName("dungeon_item_conversion_cost")
        private Cost dungeonItemConversionCost;

        /**
         * Upgrade cost tiers. Each inner list is the cost for one upgrade step.
         * e.g. {@code upgradeCosts.get(0)} is the cost for the first star upgrade.
         */
        @SerializedName("upgrade_costs")
        private List<List<Cost>> upgradeCosts = Collections.emptyList();

        /** Requirements to use or equip this item. */
        @SerializedName("requirements")
        private List<Requirement> requirements = Collections.emptyList();

        /** Museum donation metadata. */
        @SerializedName("museum_data")
        private MuseumData museumData;

        public @Nullable Integer getColorInteger() {
            if (colorInteger == null) {
                colorInteger = ColorUtils.parseColorToRgb(colorString);
            }
            return colorInteger;
        }

    }

    /** Base64-encoded Minecraft skull texture payload for {@code SKULL_ITEM} materials. */
    @Getter
    public static class Skin {

        /** Base64-encoded JSON containing the skin texture URL. */
        @SerializedName("value")
        private String value;

        /** Mojang signature for the skin value. */
        @SerializedName("signature")
        private String signature;

        /**
         * Returns a {@link GameProfile} with the skin texture applied, suitable for use
         * as a {@code PLAYER_HEAD} / skull item profile. Computed once and cached.
         */
        @Expose(serialize = false, deserialize = false)
        @Getter(AccessLevel.NONE)
        private GameProfile gameProfile;

        public GameProfile getGameProfile() {
            if (gameProfile == null) {
                gameProfile = ItemUtils.createGameProfile(value, signature);
            }
            return gameProfile;
        }

    }

    /**
     * A single cost entry used in salvage, upgrade, and dungeon conversion costs.
     * <p>The {@code type} field determines which optional fields are present:
     * <ul>
     *   <li>{@code ESSENCE} — {@link #essenceType} is set, e.g. {@code "SPIDER"}</li>
     *   <li>{@code ITEM}    — {@link #itemId} is set, e.g. {@code "ENCHANTED_LAPIS"}</li>
     *   <li>{@code COINS}   — only {@link #amount} is relevant</li>
     * </ul>
     */
    @Getter
    public static class Cost {

        /** Cost type: {@code ESSENCE}, {@code ITEM}, or {@code COINS}. */
        @SerializedName("type")
        private String type;

        /** Essence type when {@link #type} is {@code ESSENCE}, e.g. {@code "SPIDER"}. */
        @SerializedName("essence_type")
        private String essenceType;

        /** Item ID when {@link #type} is {@code ITEM}, e.g. {@code "ENCHANTED_LAPIS"}. */
        @SerializedName("item_id")
        private String itemId;

        /** Quantity required. */
        @SerializedName("amount")
        private int amount;
    }

    /** A prerequisite that must be met before the item can be used or equipped. */
    @Getter
    public static class Requirement {

        /** Requirement type, e.g. {@code "SLAYER"}, {@code "SKILL"}, {@code "COLLECTION"}. */
        @SerializedName("type")
        private String type;

        /** Slayer boss identifier when {@link #type} is {@code SLAYER}, e.g. {@code "spider"}. */
        @SerializedName("slayer_boss_type")
        private String slayerBossType;

        /** Required level or tier. */
        @SerializedName("level")
        private int level;
    }

    /** Museum donation metadata for this item. */
    @Getter
    public static class MuseumData {

        /** XP awarded when donating this item to the museum. */
        @SerializedName("donation_xp")
        private int donationXp;

        /** Museum section type, e.g. {@code "WEAPONS"}, {@code "ARMOR_SETS"}, {@code "RARITIES"}. */
        @SerializedName("type")
        private String type;

        /**
         * Player progression stage required, e.g. {@code "AMATEUR"}, {@code "INTERMEDIATE"}, {@code "SKILLED"}.
         */
        @SerializedName("game_stage")
        private String gameStage;

        /** Other item IDs that map to this museum slot. */
        @SerializedName("mapped_item_ids")
        private List<String> mappedItemIds = Collections.emptyList();

        /**
         * XP contributed per armor set donation. Key is the set name (e.g. {@code "ARACHNE"}),
         * value is the XP amount. Only present for {@code ARMOR_SETS} type items.
         */
        @SerializedName("armor_set_donation_xp")
        private Map<String, Integer> armorSetDonationXp;
    }

}