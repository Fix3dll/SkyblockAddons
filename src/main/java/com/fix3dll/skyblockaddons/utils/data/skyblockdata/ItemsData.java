package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.util.StringUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Top-level response from {@code GET /v2/resources/skyblock/items}.
 * <p>
 * Contains all SkyBlock item definitions. Individual malformed items are skipped during
 * parsing so a single bad entry does not discard the entire dataset.
 * <pre>
 * ItemsData
 * ├── success      : boolean
 * ├── lastUpdated  : long
 * └── items[]      → indexed into byId / byName maps
 *     └── Item     : ~50+ fields (see field-level Javadoc)
 *         ├── Skin, Cost, Requirement, SubRequirement
 *         ├── GemstoneSlot, GemstoneSlotCost, GemstoneSlotRequirement
 *         ├── MuseumData, Prestige, Salvage, Component
 *         └── item_specific (raw JsonObject), recipes (raw JsonArray)
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
    /**
     * An unmodifiable map containing all parsed SkyBlock items, indexed by their unique item ID.
     * @see Item#getId()
     */
    private Map<String, Item> byId = Map.of();
    /**
     * An unmodifiable map containing all parsed SkyBlock items, indexed by their exact display name.
     * @see Item#getName()
     */
    private Map<String, Item> byName = Map.of();

    public Item getById(String id) {
        if (StringUtil.isNullOrEmpty(id)) return null;
        return byId.get(id);
    }

    public Item getByName(String name) {
        if (StringUtil.isNullOrEmpty(name)) return null;
        return byName.get(name);
    }

    public int itemCount() {
        return Math.max(byId.size(), byName.size());
    }

    static class Deserializer implements JsonDeserializer<ItemsData> {
        @Override
        public ItemsData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            JsonObject obj   = json.getAsJsonObject();
            ItemsData data   = new ItemsData();
            data.success     = obj.get("success").getAsBoolean();
            data.lastUpdated = obj.get("lastUpdated").getAsLong();
            JsonArray itemsJsonArray = obj.getAsJsonArray("items");

            int itemsArraySize = itemsJsonArray.size();
            Map<String, Item> byId = HashMap.newHashMap(itemsArraySize);
            Map<String, Item> byName = HashMap.newHashMap(itemsArraySize);
            for (JsonElement el : itemsJsonArray) {
                try {
                    Item item = ctx.deserialize(el, Item.class);
                    if (!StringUtil.isNullOrEmpty(item.getId())) {
                        byId.put(item.getId(), item);
                    }
                    if (!StringUtil.isNullOrEmpty(item.getName())) {
                        byName.put(item.getName(), item);
                    }
                } catch (Exception e) {
                    // Skip malformed items so a single bad entry does not discard the entire dataset
                    e.printStackTrace();
                }
            }
            data.byId = Map.copyOf(byId);
            data.byName = Map.copyOf(byName);
            return data;
        }
    }

    /**
     * A single SkyBlock item definition. All optional fields may be {@code null}
     * or their primitive default ({@code 0} / {@code false}).
     */
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

        /**
         * Minecraft item durability/damage value. Primarily used for {@code SKULL_ITEM}
         * (always 3). Some items send this as a string; the lenient adapter handles
         * that gracefully, falling back to {@code 0}.
         */
        @JsonAdapter(LenientIntAdapter.class)
        @SerializedName("durability")
        private int durability;

        /**
         * Rarity tier: {@code COMMON}, {@code UNCOMMON}, {@code RARE}, {@code EPIC},
         * {@code LEGENDARY}, {@code MYTHIC}, {@code DIVINE}, {@code SPECIAL}.
         * <p>
         * The API uses {@code "tier"} for the vast majority of items and
         * {@code "rarity"} for a handful of items (e.g. pet personalities).
         * The two keys are <b>never</b> present simultaneously, so they are
         * unified into a single field via {@code alternate}.
         */
        @SerializedName(value = "tier", alternate = "rarity")
        private SkyblockRarity tier;

        /** Item category, e.g. {@code "SWORD"}, {@code "HELMET"}, {@code "REFORGE_STONE"}, {@code "ACCESSORY"}. */
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

        /** If {@code true}, item renders with enchantment glint. */
        @SerializedName("glowing")
        private boolean glowing;

        /** Skull texture data. Present only when {@code material} is {@code "SKULL_ITEM"}. */
        @SerializedName("skin")
        private Skin skin;

        /** NPC sell price in coins. {@code 0} if not sold to NPCs. */
        @SerializedName("npc_sell_price")
        private double npcSellPrice;

        /** Rift Motes sell price. {@code 0} if not applicable. */
        @SerializedName("motes_sell_price")
        private int motesSellPrice;

        /** Gear score value used for dungeon items. */
        @SerializedName("gear_score")
        private int gearScore;

        /** Base item stats, e.g. {@code {"DAMAGE": 69, "HEALTH": 2000}}. Keys are stat IDs (mixed case in API). */
        @SerializedName("stats")
        private Map<String, Double> stats;

        /**
         * Tiered stats that scale with item rarity. Each key is a stat ID and the value
         * is a list of values per tier, e.g. {@code {"DAMAGE": [10, 15, 20, 25]}}.
         */
        @SerializedName("tiered_stats")
        private Map<String, List<Integer>> tieredStats;

        /** Ability damage scaling coefficient. */
        @SerializedName("ability_damage_scaling")
        private double abilityDamageScaling;

        /** Sword subtype, e.g. {@code "CLEAVE"}. */
        @SerializedName("sword_type")
        private String swordType;

        /** Default enchantments pre-applied to this item. Keys are enchantment IDs, values are levels. */
        @SerializedName("enchantments")
        private Map<String, Integer> enchantments;

        /** If {@code true}, this item can be listed on the auction house. */
        @SerializedName("can_auction")
        private boolean canAuction;

        /** If {@code true}, the item cannot be stacked. */
        @SerializedName("unstackable")
        private boolean unstackable;

        /** If {@code true}, this is a dungeon-specific item variant. */
        @SerializedName("dungeon_item")
        private boolean dungeonItem;

        /** If {@code true}, the item can be salvaged to a lower rarity. */
        @SerializedName("rarity_salvageable")
        private boolean raritySalvageable;

        /** Soulbound type, e.g. {@code "COOP"} or {@code "SOLO"}. {@code null} if not soulbound. */
        @SerializedName("soulbound")
        private String soulbound;

        /** If {@code true}, kuudra attributes can be applied. */
        @SerializedName("can_have_attributes")
        private boolean canHaveAttributes;

        /** If {@code true}, a power scroll can be applied. */
        @SerializedName("can_have_power_scroll")
        private boolean canHavePowerScroll;

        /** If {@code true}, the item can accept a booster stone. */
        @SerializedName("can_have_booster")
        private boolean canHaveBooster;

        /** If {@code true}, this item cannot be reforged. */
        @SerializedName("cannot_reforge")
        private boolean cannotReforge;

        /** If {@code true}, the item can be placed as a block. */
        @SerializedName("can_place")
        private boolean canPlace;

        /** If {@code true}, the item can be traded with other players. */
        @SerializedName("can_trade")
        private boolean canTrade;

        /** If {@code true}, the item can be interacted with. */
        @SerializedName("can_interact")
        private boolean canInteract;

        /** If {@code true}, the item can interact with entities. */
        @SerializedName("can_interact_entity")
        private boolean canInteractEntity;

        /** If {@code true}, the item can be right-click interacted. */
        @SerializedName("can_interact_right_click")
        private boolean canInteractRightClick;

        /** If {@code true}, the item can be recombobulated. */
        @SerializedName("can_recombobulate")
        private boolean canRecombobulate;

        /** If {@code true}, the item can be burned in a furnace. */
        @SerializedName("can_burn_in_furnace")
        private boolean canBurnInFurnace;

        /** If {@code true}, the item can be salvaged from its recipe. */
        @SerializedName("salvageable_from_recipe")
        private boolean salvageableFromRecipe;

        /** If {@code true}, the item can be transferred to the Rift. */
        @SerializedName("rift_transferrable")
        private boolean riftTransferrable;

        /** If {@code true}, the item loses its Motes value on transfer. */
        @SerializedName("lose_motes_value_on_transfer")
        private boolean loseMotesValueOnTransfer;

        /** If {@code true}, the item is hidden from the public API. */
        @SerializedName("hide_from_api")
        private boolean hideFromApi;

        /** If {@code true}, the item is hidden from the /viewrecipe command. */
        @SerializedName("hide_from_viewrecipe_command")
        private boolean hideFromViewrecipeCommand;

        /** If {@code true}, this item has numbered editions. */
        @SerializedName("editioned")
        private boolean editioned;

        /** If {@code true}, dropping requires a double-tap. */
        @SerializedName("double_tap_to_drop")
        private boolean doubleTapToDrop;

        /** If {@code true}, the item can be serialized to NBT. */
        @SerializedName("serializable")
        private boolean serializable;

        /** If {@code true}, the item can be upgraded without soulbinding. */
        @SerializedName("is_upgradeable_without_soulbinding")
        private boolean upgradeableWithoutSoulbinding;

        /** If {@code true}, a recombobulator application is force-wiped. */
        @SerializedName("force_wipe_recomb")
        private boolean forceWipeRecomb;

        /**
         * Museum donatable flag (separate from {@link #museumData}).
         * If {@code true}, the item can be donated to the museum.
         */
        @SerializedName("museum")
        private boolean museum;

        /**
         * Whether this item has a UUID. The API sometimes sends this as a boolean,
         * sometimes as a string; the lenient adapter handles both forms.
         */
        @JsonAdapter(LenientBooleanAdapter.class)
        @SerializedName("has_uuid")
        private boolean hasUuid;

        /** Minion generator type, e.g. {@code "WHEAT"}, {@code "DIAMOND"}. */
        @SerializedName("generator")
        private String generator;

        /** Minion tier number. */
        @SerializedName("generator_tier")
        private int generatorTier;

        /** Crystal type, e.g. {@code "FARM_CRYSTAL_DICER"}. */
        @SerializedName("crystal")
        private String crystal;

        /** Furniture type identifier. */
        @SerializedName("furniture")
        private String furniture;

        /** Origin identifier for item source tracking. */
        @SerializedName("origin")
        private String origin;

        /** Private island type, e.g. {@code "STANDARD"}, {@code "BINGO"}. */
        @SerializedName("private_island")
        private String privateIsland;

        /** Essence or item costs returned when salvaging this item. */
        @SerializedName("salvages")
        private List<Cost> salvages = List.of();

        /**
         * Singular salvage output. Distinct from {@link #salvages} — this defines a
         * single item return, e.g. breaking down a specific material.
         */
        @SerializedName("salvage")
        private Salvage salvage;

        /** Cost to convert this item into its dungeon variant. */
        @SerializedName("dungeon_item_conversion_cost")
        private Cost dungeonItemConversionCost;

        /**
         * Upgrade cost tiers. Each inner list is the cost for one upgrade step.
         * e.g. {@code upgradeCosts.get(0)} is the cost for the first star upgrade.
         */
        @SerializedName("upgrade_costs")
        private List<List<Cost>> upgradeCosts = List.of();

        /** Prestige upgrade path and costs. */
        @SerializedName("prestige")
        private Prestige prestige;

        /** Requirements to use or equip this item. */
        @SerializedName("requirements")
        private List<Requirement> requirements = List.of();

        /** Catacombs (dungeon floor) requirements, separate from general requirements. */
        @SerializedName("catacombs_requirements")
        private List<Requirement> catacombsRequirements = List.of();

        /**
         * Gemstone slot definitions. Each entry describes one unlockable gem slot,
         * its unlock costs, and any progression requirements.
         */
        @SerializedName("gemstone_slots")
        private List<GemstoneSlot> gemstoneSlots = List.of();

        /** Museum donation metadata. */
        @SerializedName("museum_data")
        private MuseumData museumData;

        /** Item component definitions (e.g. recipe ingredients, accessory bags). */
        @SerializedName("components")
        private List<Component> components = List.of();

        // TODO: Model recipes properly with a dedicated Recipe class when crafting
        //  data is needed by the mod. For now, the raw JSON is preserved.
        /** Raw crafting recipe data. */
        @SerializedName("recipes")
        private JsonArray recipes;

        /**
         * Highly dynamic, item-specific data blob. Structure varies wildly between
         * items (portals, scaling tiers, swappable skins, etc.) so it is retained as a raw {@link JsonObject}.
         */
        @SerializedName("item_specific")
        private JsonObject itemSpecific;

        public Integer getColorInteger() {
            if (colorString == null) return null;

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
            if (value == null) return null;

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

    /**
     * A prerequisite that must be met before the item can be used or equipped.
     * Also reused for {@code catacombs_requirements}.
     */
    @Getter
    public static class Requirement {

        /**
         * Requirement type, e.g. {@code "SLAYER"}, {@code "SKILL"},
         * {@code "COLLECTION"}, {@code "DUNGEON_TIER"}, {@code "TROPHY_FISHING"},
         * {@code "GARDEN_LEVEL"}, {@code "KUUDRA_TIER"}, {@code "HEART_OF_THE_MOUNTAIN"}.
         */
        @SerializedName("type")
        private String type;

        /** Slayer boss identifier when {@link #type} is {@code SLAYER}, e.g. {@code "spider"}. */
        @SerializedName("slayer_boss_type")
        private String slayerBossType;

        /** Skill name when {@link #type} is {@code SKILL}, e.g. {@code "FARMING"}. */
        @SerializedName("skill")
        private String skill;

        /** Dungeon type when {@link #type} is {@code DUNGEON_TIER}, e.g. {@code "CATACOMBS"}. */
        @SerializedName("dungeon_type")
        private String dungeonType;

        /**
         * Required dungeon tier when {@link #type} is {@code DUNGEON_TIER}.
         */
        @SerializedName("tier")
        private int dungeonTier;

        /**
         * Required level or tier. Used by {@code SKILL}, {@code SLAYER}, {@code COLLECTION}, {@code GARDEN_LEVEL}, etc.
         */
        @SerializedName("level")
        private int level;

        /** Collection type when {@link #type} is {@code COLLECTION}, e.g. {@code "WHEAT"}. */
        @SerializedName("collection")
        private String collection;

        /** Faction name when {@link #type} is faction-gated. */
        @SerializedName("faction")
        private String faction;

        /** Kuudra tier, e.g. {@code "HOT"}, {@code "BURNING"}. */
        @SerializedName("kuudra_tier")
        private String kuudraTier;

        /** Required reputation level. */
        @SerializedName("reputation")
        private int reputation;

        /** Reward tier for trophy fishing requirements, e.g. {@code "BRONZE"}, {@code "SILVER"}. */
        @SerializedName("reward")
        private String reward;

        /** Required amount (e.g. collection count). */
        @SerializedName("amount")
        private int amount;

        /** Index in the item lore where this requirement is displayed. */
        @SerializedName("lore_index")
        private int loreIndex;

        /** Minimum account age required. */
        @SerializedName("minimum_age")
        private int minimumAge;

        /** Unit for {@link #minimumAge}, e.g. {@code "DAYS"}. */
        @SerializedName("minimum_age_unit")
        private String minimumAgeUnit;

        /** Game mode requirement, e.g. {@code "IRONMAN"}. */
        @SerializedName("mode")
        private String mode;

        /** Chocolate Factory rabbit ID. */
        @SerializedName("rabbit")
        private String rabbit;

        /** Nested sub-requirements (e.g. profile type + tier checks). */
        @SerializedName("requirements")
        private List<SubRequirement> subRequirements = List.of();
    }

    /**
     * A nested requirement inside a {@link Requirement}, used for compound checks such as profile type restrictions.
     */
    @Getter
    public static class SubRequirement {

        /** Sub-requirement type, e.g. {@code "PROFILE_TYPE"}. */
        @SerializedName("type")
        private String type;

        /** Profile type, e.g. {@code "IRONMAN"}, {@code "BINGO"}. */
        @SerializedName("profile_type")
        private String profileType;

        /** Numeric tier for the sub-requirement. */
        @SerializedName("tier")
        private int tier;
    }

    /**
     * A single gemstone slot on an item. Slots may require unlock costs and/or
     * progression requirements before a gem can be inserted.
     */
    @Getter
    public static class GemstoneSlot {

        /**
         * Gem type this slot accepts, e.g. {@code "PERIDOT"}, {@code "JASPER"}.
         * Generic attribute types like {@code "COMBAT"} accept any gem of that class.
         */
        @SerializedName("slot_type")
        private String slotType;

        /**
         * Costs required to unlock this slot. Empty when the slot is unlocked by default
         * (e.g. dungeon items with a bare {@code {"slot_type": "COMBAT"}} entry).
         */
        @SerializedName("costs")
        private List<GemstoneSlotCost> costs = List.of();

        /**
         * Progression gates that must be satisfied before this slot can be unlocked.
         * Typically {@code ITEM_DATA} checks against levelable item progress.
         */
        @SerializedName("requirements")
        private List<GemstoneSlotRequirement> requirements = List.of();
    }

    /**
     * A single cost entry for unlocking a gemstone slot.
     * <p>Unlike {@link Cost}, coin costs here use a dedicated {@code coins} field rather than
     * {@code amount}, so this is a separate class.
     * <ul>
     *   <li>{@code ITEM}  — {@link #itemId} and {@link #amount} are set</li>
     *   <li>{@code COINS} — {@link #coins} is set</li>
     * </ul>
     */
    @Getter
    public static class GemstoneSlotCost {

        /** Cost type: {@code ITEM} or {@code COINS}. */
        @SerializedName("type")
        private String type;

        /** Item ID when {@link #type} is {@code ITEM}, e.g. {@code "FINE_PERIDOT_GEM"}. */
        @SerializedName("item_id")
        private String itemId;

        /** Item quantity when {@link #type} is {@code ITEM}. */
        @SerializedName("amount")
        private int amount;

        /** Coin cost when {@link #type} is {@code COINS}. */
        @SerializedName("coins")
        private long coins;
    }

    /**
     * A progression gate on a gemstone slot unlock.
     * <p>Currently only {@code ITEM_DATA} type is observed in the wild, which checks
     * a named data key on the item against a threshold value using the given operator.
     */
    @Getter
    public static class GemstoneSlotRequirement {

        /** Requirement type, e.g. {@code "ITEM_DATA"}. */
        @SerializedName("type")
        private String type;

        /**
         * The item data key to check when {@link #type} is {@code ITEM_DATA},
         * e.g. {@code "levelable_lvl"}.
         */
        @SerializedName("data_key")
        private String dataKey;

        /**
         * The threshold value as a string, e.g. {@code "25"}. Parse to the
         * appropriate numeric type depending on {@link #dataKey}.
         */
        @SerializedName("value")
        private String value;

        /**
         * Comparison operator, e.g. {@code "GREATER_THAN_OR_EQUALS"},
         * {@code "EQUALS"}, {@code "LESS_THAN"}.
         */
        @SerializedName("operator")
        private String operator;
    }

    /** Museum donation metadata for this item. */
    @Getter
    public static class MuseumData {

        /** XP awarded when donating this item to the museum. */
        @SerializedName("donation_xp")
        private int donationXp;

        /** Museum category, e.g. {@code "WEAPONS"}, {@code "ARMOR_SETS"}, {@code "FARMING"}, {@code "RARITIES"}. */
        @SerializedName("category")
        private String category;

        /**
         * Player progression stage required, e.g. {@code "AMATEUR"}, {@code "INTERMEDIATE"},
         * {@code "SKILLED"}, {@code "EXPERT"}.
         */
        @SerializedName("game_stage")
        private String gameStage;

        /**
         * Parent item upgrade mapping. Keys and values are item IDs representing an upgrade path,
         * e.g. {@code {"CACTUS_KNIFE": "CACTUS_KNIFE_2"}}. Empty object ({@code {}}) when there is no parent.
         */
        @SerializedName("parent")
        private Map<String, String> parent;

        /** Other item IDs that map to this museum slot. */
        @SerializedName("mapped_item_ids")
        private List<String> mappedItemIds = List.of();

        /**
         * XP contributed per armor set donation. Key is the set name (e.g. {@code "ARACHNE"}),
         * value is the XP amount. Only present for {@code ARMOR_SETS} category items.
         */
        @SerializedName("armor_set_donation_xp")
        private Map<String, Integer> armorSetDonationXp;
    }

    /** Prestige upgrade data. Defines the target item and costs required to prestige this item. */
    @Getter
    public static class Prestige {

        /** Target item ID after prestiging. */
        @SerializedName("item_id")
        private String itemId;

        /** Costs required to prestige. */
        @SerializedName("costs")
        private List<Cost> costs = List.of();
    }

    /**
     * Singular salvage output. Distinct from the {@code salvages} list on
     * {@link Item} — this defines a single, fixed item return.
     */
    @Getter
    public static class Salvage {

        /** Item ID returned when salvaging. */
        @SerializedName("item_id")
        private String itemId;

        /** Quantity returned. */
        @SerializedName("amount")
        private int amount;
    }

    /**
     * An item component definition, e.g. recipe ingredient rules or accessory
     * bag sorting configuration.
     */
    @Getter
    public static class Component {

        /** Component type, e.g. {@code "RECIPE_INGREDIENT"}, {@code "ACCESSORY_BAG"}. */
        @SerializedName("type")
        private String type;

        /** If {@code true}, duplicates of this component are allowed. */
        @SerializedName("allow_duplicates")
        private boolean allowDuplicates;

        /** If {@code true}, items are sorted by accessory tier. */
        @SerializedName("sort_by_accessory_tier")
        private boolean sortByAccessoryTier;

        /** If {@code true}, items are sorted by category. */
        @SerializedName("sort_by_category")
        private boolean sortByCategory;

        /** Item IDs excluded from this component. */
        @SerializedName("excluded_items")
        private List<String> excludedItems = List.of();

        /** Item IDs to display first. */
        @SerializedName("show_items_first")
        private List<String> showItemsFirst = List.of();

        /** Item IDs to display last. */
        @SerializedName("show_items_last")
        private List<String> showItemsLast = List.of();
    }

    /**
     * A lenient {@code boolean} adapter that gracefully handles API fields which
     * sometimes arrive as a JSON boolean and sometimes as a JSON string.
     * Falls back to {@code false} on any unexpected input.
     */
    static class LenientBooleanAdapter extends TypeAdapter<Boolean> {
        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value != null && value);
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            try {
                JsonToken token = in.peek();
                return switch (token) {
                    case BOOLEAN -> in.nextBoolean();
                    case STRING -> Boolean.parseBoolean(in.nextString());
                    case NUMBER -> in.nextInt() != 0;
                    case NULL -> { in.nextNull(); yield false; }
                    default -> { in.skipValue(); yield false; }
                };
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * A lenient {@code int} adapter that gracefully handles API fields which
     * sometimes arrive as a JSON number and sometimes as a JSON string.
     * Falls back to {@code 0} on any unexpected or unparseable input.
     */
    static class LenientIntAdapter extends TypeAdapter<Integer> {
        @Override
        public void write(JsonWriter out, Integer value) throws IOException {
            out.value(value != null ? value : 0);
        }

        @Override
        public Integer read(JsonReader in) throws IOException {
            try {
                JsonToken token = in.peek();
                return switch (token) {
                    case NUMBER -> in.nextInt();
                    case STRING -> Integer.parseInt(in.nextString());
                    case NULL -> {
                        in.nextNull();
                        yield 0;
                    }
                    default -> {
                        in.skipValue();
                        yield 0;
                    }
                };
            } catch (Exception e) {
                return 0;
            }
        }
    }

}