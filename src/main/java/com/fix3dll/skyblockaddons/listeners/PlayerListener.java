package com.fix3dll.skyblockaddons.listeners;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.config.PersistentValuesManager;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.ItemType;
import com.fix3dll.skyblockaddons.core.PetInfo;
import com.fix3dll.skyblockaddons.core.PlayerStat;
import com.fix3dll.skyblockaddons.core.SkillType;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.SkyblockOre;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.core.SkyblockRune;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.core.seacreatures.SeaCreatureManager;
import com.fix3dll.skyblockaddons.events.ClientEvents;
import com.fix3dll.skyblockaddons.events.SkyblockEvents;
import com.fix3dll.skyblockaddons.features.BaitManager;
import com.fix3dll.skyblockaddons.features.EndstoneProtectorManager;
import com.fix3dll.skyblockaddons.features.FetchurManager;
import com.fix3dll.skyblockaddons.features.JerryPresent;
import com.fix3dll.skyblockaddons.features.PetManager;
import com.fix3dll.skyblockaddons.features.cooldowns.CooldownManager;
import com.fix3dll.skyblockaddons.features.deployable.DeployableManager;
import com.fix3dll.skyblockaddons.features.dragontracker.DragonTracker;
import com.fix3dll.skyblockaddons.features.dungeonmap.DungeonMapManager;
import com.fix3dll.skyblockaddons.features.dungeons.DungeonMilestone;
import com.fix3dll.skyblockaddons.features.dungeons.DungeonPlayer;
import com.fix3dll.skyblockaddons.features.enchants.EnchantManager;
import com.fix3dll.skyblockaddons.features.fishing.FishParticleManager;
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerTracker;
import com.fix3dll.skyblockaddons.features.tablist.TabListParser;
import com.fix3dll.skyblockaddons.gui.screens.IslandWarpGui;
import com.fix3dll.skyblockaddons.utils.ActionBarParser;
import com.fix3dll.skyblockaddons.utils.DevUtils;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.InventoryUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils.ItemClassification;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.NPCUtils;
import com.fix3dll.skyblockaddons.utils.RomanNumeralParser;
import com.fix3dll.skyblockaddons.utils.ScoreboardManager;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.fix3dll.skyblockaddons.utils.data.requests.ElectionRequest;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.BazaarData;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.EnchantmentsData.Enchant;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.ItemsData;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerListener {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    @Getter private final ActionBarParser actionBarParser = new ActionBarParser();

    private static final Pattern NO_ARROWS_LEFT_PATTERN = Pattern.compile("§c§lQUIVER! §r§cYou have run out of §r(?<type>§.*)§r§c!§r");
    private static final Pattern ONLY_HAVE_ARROWS_LEFT_PATTERN = Pattern.compile("§c§lQUIVER! §r§cYou only have (?<arrows>[0-9]+) §r(?<type>§.*) §r§cleft!§r");
    private static final Pattern ABILITY_CHAT_PATTERN = Pattern.compile("§aUsed §r§6[A-Za-z ]+§r§a! §r§b\\([0-9]+ Mana\\)§r");
    private static final Pattern PROFILE_CHAT_PATTERN = Pattern.compile("You are playing on profile: ([A-Za-z]+).*");
    private static final Pattern SWITCH_PROFILE_CHAT_PATTERN = Pattern.compile("Your profile was changed to: ([A-Za-z]+).*");
    private static final Pattern MINION_CANT_REACH_PATTERN = Pattern.compile("§cI can't reach any (?<mobName>\\w+?)(s?)$");
    private static final Pattern DRAGON_KILLED_PATTERN = Pattern.compile(" *[A-Z]* DRAGON DOWN!");
    private static final Pattern DRAGON_SPAWNED_PATTERN = Pattern.compile("☬ The (?<dragonType>[A-Za-z ]+) Dragon has spawned!");
    private static final Pattern SLAYER_COMPLETED_PATTERN = Pattern.compile(" {3}» Talk to Maddox to claim your (?<slayerType>[A-Za-z]+) Slayer XP!");
    private static final Pattern SLAYER_COMPLETED_PATTERN_AUTO1 = Pattern.compile(" *(?<slayerType>[A-Za-z]+) Slayer LVL \\d+ - (?:Next LVL in [\\d,]+ XP!|LVL MAXED OUT!)");
    private static final Pattern SLAYER_COMPLETED_PATTERN_AUTO2 = Pattern.compile(" *SLAYER QUEST STARTED!");
    private static final Pattern DEATH_MESSAGE_PATTERN = Pattern.compile(" ☠ (?<username>\\w+) (?<causeOfDeath>.+)\\.");
    private static final Pattern REVIVE_MESSAGE_PATTERN = Pattern.compile(" ❣ (?<revivedPlayer>\\w+) was revived(?: by (?<reviver>\\w+))*!");
    private static final Pattern NEXT_TIER_PET_PROGRESS = Pattern.compile("Next tier: (?<total>[0-9,]+)/.*");
    private static final Pattern MAXED_TIER_PET_PROGRESS = Pattern.compile(".*: (?<total>[0-9,]+)");
    private static final Pattern SPIRIT_SCEPTRE_MESSAGE_PATTERN = Pattern.compile("Your (?:Implosion|Spirit Sceptre|Molten Wave) hit (?<hitEnemies>[0-9]+) enem(?:y|ies) for (?<dealtDamage>[0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]+)*) damage\\.");
    private static final Pattern PROFILE_TYPE_SYMBOL = Pattern.compile("(?i)§[0-9A-FK-ORZ][♲Ⓑ]");
    private static final Pattern NETHER_FACTION_SYMBOL = Pattern.compile("(?i)§[0-9A-FK-ORZ][⚒ቾ]");
    private static final Pattern AUTOPET_PATTERN = Pattern.compile("§cAutopet §eequipped your §7\\[Lvl (?<level>\\d+)](?: §8\\[§6\\d+§8§.✦§8])? §(?<rarityColor>.)(?<name>.*)§e! §a§lVIEW RULE§r");
    private static final Pattern PET_LEVELED_UP_PATTERN = Pattern.compile("§aYour §r§(?<rarityColor>.)(?<name>.*?)(?<cosmetic>§r§. ✦)? §r§aleveled up to level §r(?:§.)*(?<newLevel>\\d+)§r§a!§r");
    private static final Pattern PET_ITEM_PATTERN = Pattern.compile("§aYour pet is now holding §r§(?<rarityColor>.)(?<petItem>.*)§r§a.§r");
    private static final Pattern PET_CUSTOM_NAME_PATTERN = Pattern.compile("(?:(?i)(§r)?§e⭐ )?§7\\[Lvl (?<level>\\d+)](?: §.\\[§.\\d+(§.)+✦§8])? §(?<rarityColor>.)(?<name>[^§]+)(?:§.\\s*✦)?");
    public static final Pattern ESSENCE_NAME_PATTERN = Pattern.compile("(?<type>[\\w ]+) Essence(?: x(?<amount>\\d+))?");
    public static final Pattern ATTRIBUTE_SHARD_NAME_PATTERN = Pattern.compile("(?<type>[\\w ]+) Shard(?: x(?<amount>\\d+))?");

    private static final ObjectOpenHashSet<String> SOUP_RANDOM_MESSAGES = ObjectOpenHashSet.of(
            "I feel like I can fly!", "What was in that soup?",
            "Hmm… tasty!", "Hmm... tasty!", "You can now fly for 2 minutes.", "Your flight has been extended for 2 extra minutes.",
            "You can now fly for 200 minutes.", "Your flight has been extended for 200 extra minutes."
    );

    private static final Set<Identifier> BONZO_STAFF_SOUNDS = Set.of(
            SoundEvents.FIREWORK_ROCKET_BLAST.location(),
            SoundEvents.FIREWORK_ROCKET_BLAST_FAR.location(),
            SoundEvents.FIREWORK_ROCKET_TWINKLE.location(),
            SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR.location(),
            SoundEvents.GHAST_AMBIENT.location(),
            SoundEvents.GHAST_WARN.location() // TODO others are unnecessary?
    );

    // All Rat pet sounds as instance with their respective sound categories, except the sound when it lays a cheese
    private static final ObjectOpenHashSet<RatSound> RAT_SOUNDS = ObjectOpenHashSet.of(
            new RatSound(SoundEvents.BAT_AMBIENT.location(),1.0F, 1.1904762F),
            new RatSound(SoundEvents.CHICKEN_STEP.location(),0.15F, 1.0F)
    );

    private long lastWorldJoin = -1;
    private long lastBal = -1;
    private int lastBalEntityId = -1;
    private long lastBroodmother = -1;
    private int timerTick = 1;
    private long lastMinionSound = -1;
    private long lastSkyblockServerJoinAttempt = 0;
    private long lastDeath = 0;
    private long lastRevive = 0;
    private long lastMaddoxLevelTime;
    private String lastMaddoxSlayerType;

    @Getter private long rainmakerTimeEnd = -1;

    @Getter private final Set<UUID> countedEndermen = new HashSet<>();
    @Getter private final TreeMap<Long, Set<Vec3>> recentlyKilledZealots = new TreeMap<>();

    @Getter private int spiritSceptreHitEnemies = 0;
    @Getter private float spiritSceptreDealtDamage = 0;

    @Getter private final TreeMap<Long, Vec3> explosiveBowExplosions = new TreeMap<>();

    // For caching for the PROFILE_TYPE_IN_CHAT feature, saves the last MAX_SIZE names.
    private final LinkedHashMap<String, String> namesWithSymbols = new LinkedHashMap<>(81) {
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 80; // MAX_SIZE = 80
        }
    };
    @Getter @Setter private long fireFreezeTimer = 0L;
    private boolean doubleHook = false;
    private String cachedChatRunCommand;
    @Setter private boolean savePersistentFlag = false;

    private final ArrayList<Component> cachedTooltipFirstComponents = new ArrayList<>();
    private ItemStack lastTooltipFirstItemStack = null;

    // addItemPricesToTooltip() caching fields
    private final ArrayList<Component> cachedPriceComponents = new ArrayList<>();
    private ItemStack lastPriceItemStack       = null;
    private int       lastPriceItemHash        = 0;
    private int       lastPriceItemCount       = -1;
    private boolean   lastPriceShiftState      = false;
    private boolean   lastPriceBoldLines       = false;
    private boolean   lastPriceIsChroma        = false;
    private int       lastLowestBinIdentity    = 0;
    private int       lastLBinAveragesIdentity = 0;
    private long      lastBazaarIdentity       = 0;
    private long      lastItemsDataIdentity    = 0;

    public static final Identifier SBA_FIRST_PHASE = SkyblockAddons.identifier("first");
    public static final Identifier SBA_LAST_PHASE = SkyblockAddons.identifier("last");

    public PlayerListener() {
        ClientTickEvents.START_CLIENT_TICK.register(this::onTickStart);

        ClientEvents.ENTITY_JOIN_WORLD.register(this::onEntityJoinWorld);
        ClientEvents.HANDLE_KEYBINDS.register(this::handleKeybinds);
        ClientEvents.LIVING_ENTITY_TICK.register(this::onEntityTick);
        ClientEvents.PLAY_SOUND.register(this::onPlaySound);

        ClientReceiveMessageEvents.GAME_CANCELED.register(this::onChatReceived);
        ClientReceiveMessageEvents.ALLOW_GAME.register(this::onChatReceived);
        ClientReceiveMessageEvents.MODIFY_GAME.register(this::onChatModify);

        UseItemCallback.EVENT.register(this::onRightClickBlock);
        ItemTooltipCallback.EVENT.addPhaseOrdering(SBA_FIRST_PHASE, Event.DEFAULT_PHASE);
        ItemTooltipCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, SBA_LAST_PHASE);
        ItemTooltipCallback.EVENT.register(SBA_FIRST_PHASE, this::onGetComponentFirst);
        ItemTooltipCallback.EVENT.register(SBA_LAST_PHASE, this::onGetComponentLast);

        SkyblockEvents.DUNGEON_PLAYER_DEATH.register(this::onDungeonPlayerDeath);
        SkyblockEvents.DUNGEON_PLAYER_REVIVE.register(this::onDungeonPlayerRevive);
        SkyblockEvents.BLOCK_BREAK.register(this::onBlockBreak);
    }

    private void onEntityJoinWorld(Entity entity, CallbackInfo callbackInfo) {
        if (MC.player != null && entity == MC.player) {
            lastWorldJoin = Util.getMillis();
            timerTick = 1;
            main.getInventoryUtils().resetPreviousInventory();
            countedEndermen.clear();
            EndstoneProtectorManager.reset();

            IslandWarpGui.Marker doubleWarpMarker = IslandWarpGui.getDoubleWarpMarker();
            if (doubleWarpMarker != null) {
                IslandWarpGui.setDoubleWarpMarker(null);
                MC.player.connection.sendChat("/warp " + doubleWarpMarker.getWarpName());
            }

            NPCUtils.getNpcLocations().clear();
            JerryPresent.getJerryPresents().clear();
            FishParticleManager.clearParticleCache();
            main.getRenderListener().setMaxRiftHealth(0.0F);
            PlayerStat.MAX_RIFT_HEALTH.setValue(0);
            DungeonMapManager.clearSkinCache();
        }
    }

    private Component onChatModify(Component component, boolean actionBar) {
        if (!main.getUtils().isOnHypixel()) return component;

        String formattedText = TextUtils.getFormattedText(component);
        String unformattedText = component.getString();
        String strippedText = TextUtils.stripColor(component.getString());

        if (actionBar) {
            Iterator<String> itr = actionBarParser.getStringsToRemove().iterator();
            String message = unformattedText;
            while (itr.hasNext()) {
                message = message.replaceAll(" *" + Pattern.quote(itr.next()), "");
            }
            message = message.trim();
            return Component.literal(message);
        } else {
            if (formattedText.equals("§aA special §r§5Zealot §r§ahas spawned nearby!§r")) {
                if (Feature.SPECIAL_ZEALOT_ALERT.isEnabled()) {
                    main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5);
                    main.getRenderListener().setTitleFeature(Feature.SUMMONING_EYE_ALERT);
                    main.getRenderListener().setTitleFeature(Feature.SPECIAL_ZEALOT_ALERT);
                }
                if (Feature.ZEALOT_COUNTER.isEnabled()) {
                    // Edit the message to include counter.
                    // TODO test
                    component = Component.literal(formattedText + ColorCode.GRAY + " (" + main.getPersistentValuesManager().getPersistentValues().getKills() + ")");
                }
                main.getPersistentValuesManager().addEyeResetKills();
            } else if (Feature.PLAYER_SYMBOLS_IN_CHAT.isEnabled() && unformattedText.contains(":")) {
                component = playerSymbolsDisplay(component);
            }
        }

        return component;
    }

    /**
     * Checks the conditions related to chat messages. You can modify component or cancel it with return null value.
     * @param component Text Component
     * @param actionBar True if Action Bar component
     * @return false if cancelled
     */
    private boolean onChatReceived(Component component, boolean actionBar) {
        if (!main.getUtils().isOnHypixel() || component == null) return true;

        String formattedText = TextUtils.getFormattedText(component);
        String unformattedText = component.getString();
        String strippedText = TextUtils.stripColor(component.getString());

        if (formattedText.startsWith("§7Sending to server ")) {
            lastSkyblockServerJoinAttempt = Util.getMillis();
            DragonTracker.getInstance().reset();
            return true;
        }

        if (Feature.OUTBID_ALERT_SOUND.isEnabled() && formattedText.matches("§6\\[Auction] §..*§eoutbid you .*")
                && (Feature.OUTBID_ALERT_SOUND.isEnabled(FeatureSetting.OUTBID_ALERT_SOUND_IN_OTHER_GAMES) || main.getUtils().isOnSkyblock())) {
            main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5);
        }

        if (actionBar) {
            // Log the message to the game log if action bar message logging is enabled.
            if (Feature.DEVELOPER_MODE.isEnabled() && DevUtils.isLoggingActionBarMessages()) {
                LOGGER.info("[ACTION BAR] {}", unformattedText);
            }

            // Parse using ActionBarParser and display the rest message instead
            String restMessage = actionBarParser.parseActionBar(component.getString());
            if (restMessage.trim().isEmpty()) {
                return true;
            }

            if (main.getUtils().isInDungeon()) {
                if (Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY.isEnabled()) {
                    main.getDungeonManager().addEssence(restMessage);
                }

                if (Feature.DUNGEONS_SECRETS_DISPLAY.isEnabled()) {
                    main.getDungeonManager().addSecrets(restMessage);
                }
            }
            // Mark the message for change
        } else {
//            LOGGER.info("Formatted chat: {}, class: {}", formattedText, component.getClass().getName());
//            LOGGER.info("Unformatted chat: {}", component.getString());
            Matcher matcher;

            if (cachedChatRunCommand == null && formattedText.contains("§2§l[PICK UP]")) {
                this.setChatRunCommandFromComponent(component);

            } else if (formattedText.contains("§a§l[YES]") || formattedText.contains("§a[Yes]")) {
                this.setChatRunCommandFromComponent(component);

            } if (main.getRenderListener().isPredictMana() && unformattedText.startsWith("Used ") && unformattedText.endsWith("Mana)")) {
                int manaLost = Integer.parseInt(unformattedText.split(Pattern.quote("! ("))[1].split(Pattern.quote(" Mana)"))[0]);
                PlayerStat.MANA.setValue(PlayerStat.MANA.getValue() - manaLost);
            } else if ((matcher = AUTOPET_PATTERN.matcher(formattedText)).find()) {
                PetManager.getInstance().findCurrentPetFromAutopet(
                        matcher.group("level"), matcher.group("rarityColor"), matcher.group("name")
                );

            } else if ((matcher = PET_LEVELED_UP_PATTERN.matcher(formattedText)).find()) {
                int newLevel = Integer.parseInt(matcher.group("newLevel"));
                String petName = matcher.group("name");
                String petCosmetic = matcher.group("cosmetic");

                // The ✦ of skins with cosmetic level feature is not at the end: '[Lvl 200] [2✦] Golden Dragon'
                // §aYour §r§6Golden Dragon§r§4 ✦ §r§aleveled up to level §r§6§l202§r§a!§r
                if (!StringUtil.isNullOrEmpty(petCosmetic) && newLevel <= 200) {
                    petName += petCosmetic.replace("§r", "");
                }

                PetManager.getInstance().updateAndSetCurrentLevelledPet(
                        newLevel, matcher.group("rarityColor"), petName
                );

            } else if ((matcher = PET_ITEM_PATTERN.matcher(formattedText)).find()) {
                PetManager.getInstance().updatePetItem(
                        matcher.group("rarityColor"), matcher.group("petItem")
                );

            } else if ((matcher = DEATH_MESSAGE_PATTERN.matcher(unformattedText)).matches() && MC.level != null) {
                // Hypixel's dungeon reconnect messages look exactly like death messages.
                String causeOfDeath = matcher.group("causeOfDeath");
                if (!causeOfDeath.equals("reconnected")) {
                    String username = matcher.group("username");

                    AbstractClientPlayer deadPlayer = null;
                    if (username.equals("You")) {
                        deadPlayer = MC.player;
                    } else {
                        for (AbstractClientPlayer player : MC.level.players()) {
                            if (player.getName().getString().equals(username)) {
                                deadPlayer = player;
                                break;
                            }
                        }
                    }

                    if (deadPlayer != null) {
                        SkyblockEvents.DUNGEON_PLAYER_DEATH.invoker().onDungeonPlayerDeath(deadPlayer, username, causeOfDeath);
                    }

                    return true;
                }

            } else if (Feature.SUMMONING_EYE_ALERT.isEnabled() && formattedText.equals("§6§lRARE DROP! §r§5Summoning Eye§r")) {
                main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5); // credits to tomotomo, thanks lol
                main.getRenderListener().setTitleFeature(Feature.SUMMONING_EYE_ALERT);

                // TODO: Seems like leg warning and num sc killed should be separate features
            } else if (SeaCreatureManager.getInstance().getAllSeaCreatureSpawnMessages().contains(unformattedText)) {
                int spawned = unformattedText.contains("Magma Slug")
                           || unformattedText.contains("Bayou Sludge")
                           || unformattedText.contains("Liltads") ? 4 : 1;

                if (doubleHook) {
                    spawned *= 2;
                    doubleHook = false;
                }
                main.getPersistentValuesManager().addSeaCreaturesKilled(spawned);
                if (Feature.LEGENDARY_SEA_CREATURE_WARNING.isEnabled()
                        && SeaCreatureManager.getInstance().getLegendarySeaCreatureSpawnMessages().contains(unformattedText)) {
                    main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5);
                    main.getRenderListener().setTitleFeature(Feature.LEGENDARY_SEA_CREATURE_WARNING);
                }

            } else if (formattedText.startsWith("§eIt's a §r§aDouble Hook§r§e!")) {
                doubleHook = true;

            } else if (Feature.DISABLE_MAGICAL_SOUP_MESSAGES.isEnabled() && SOUP_RANDOM_MESSAGES.contains(unformattedText)) {
                return false;

            } else if (Feature.DISABLE_TELEPORT_PAD_MESSAGES.isEnabled() && (formattedText.startsWith("§aWarped from ") || formattedText.equals("§cThis Teleport Pad does not have a destination set!§r"))) {
                return false;

            } else if (Feature.DISABLE_MORT_MESSAGES.isEnabled() && strippedText.startsWith("[NPC] Mort:")) {
                return false;

            } else if (strippedText.startsWith("[BOSS] ")) {
                if (Feature.FIRE_FREEZE_TIMER.isEnabled()
                        && strippedText.equals("[BOSS] The Professor: Oh? You found my Guardians' one weakness?")) {
                    fireFreezeTimer = System.currentTimeMillis() + 5000;
                }

                if (Feature.DISABLE_BOSS_MESSAGES.isEnabled()) {
                    return false;
                }

            } else if (Feature.SPIRIT_SCEPTRE_DISPLAY.isEnabled()
                    && strippedText.startsWith("Your Implosion hit")
                    || strippedText.startsWith("Your Spirit Sceptre hit")
                    || strippedText.startsWith("Your Molten Wave hit")) {
                matcher = SPIRIT_SCEPTRE_MESSAGE_PATTERN.matcher(unformattedText);
                // Ensure matcher.group gets what it wants, we don't need the whole result
                if (matcher.find()) {
                    this.spiritSceptreHitEnemies = Integer.parseInt(matcher.group("hitEnemies"));
                    this.spiritSceptreDealtDamage = Float.parseFloat(matcher.group("dealtDamage").replace(",", ""));

                    if (Feature.SPIRIT_SCEPTRE_DISPLAY.isEnabled(FeatureSetting.DISABLE_SPIRIT_SCEPTRE_MESSAGES)) {
                        return false;
                    }
                }
            } else if (SlayerTracker.getInstance().isTrackerEnabled() &&
                    (matcher = SLAYER_COMPLETED_PATTERN.matcher(unformattedText)).matches()) { // §r   §r§5§l» §r§7Talk to Maddox to claim your Wolf Slayer XP!§r
                SlayerTracker.getInstance().completedSlayer(matcher.group("slayerType"));

            } else if (SlayerTracker.getInstance().isTrackerEnabled() &&
                    (matcher = SLAYER_COMPLETED_PATTERN_AUTO1.matcher(strippedText)).matches()) { // Spider Slayer LVL 7 - Next LVL in 181,000 XP!
                lastMaddoxLevelTime = System.currentTimeMillis();
                lastMaddoxSlayerType = matcher.group("slayerType");
            } else if (SLAYER_COMPLETED_PATTERN_AUTO2.matcher(strippedText).matches() && System.currentTimeMillis() - lastMaddoxLevelTime < 100) {
                SlayerTracker.getInstance().completedSlayer(lastMaddoxSlayerType);

            } else if (Feature.DRAGON_STATS_TRACKER.isEnabled() &&
                    strippedText.startsWith("☬ You placed a Summoning Eye!")) { // §r§5☬ §r§dYou placed a Summoning Eye! §r§7(§r§e5§r§7/§r§a8§r§7)§r
                DragonTracker.getInstance().addEye();

            } else if (Feature.DRAGON_STATS_TRACKER.isEnabled() &&
                    strippedText.equals("You recovered a Summoning Eye!")) {
                DragonTracker.getInstance().removeEye();

            } else if (Feature.DRAGON_STATS_TRACKER.isEnabled() &&
                    (matcher = DRAGON_SPAWNED_PATTERN.matcher(strippedText)).matches()) {
                DragonTracker.getInstance().dragonSpawned(matcher.group("dragonType"));

            } else if (Feature.DRAGON_STATS_TRACKER.isEnabled() &&
                    DRAGON_KILLED_PATTERN.matcher(strippedText).matches()) {
                DragonTracker.getInstance().dragonKilled();

            } else if (Feature.BIRCH_PARK_RAINMAKER_TIMER.isEnabled() && formattedText.startsWith("§eYou added a minute of rain!")) {
                if (this.rainmakerTimeEnd == -1 || this.rainmakerTimeEnd < System.currentTimeMillis()) {
                    this.rainmakerTimeEnd = System.currentTimeMillis() + (1000 * 60); // Set the timer to a minute from now.
                } else {
                    this.rainmakerTimeEnd += (1000 * 60); // Extend the timer one minute.
                }
            } else if (Feature.FETCHUR_TODAY.isEnabled() && formattedText.startsWith("§e[NPC] Fetchur§f:")) {
                FetchurManager fetchur = FetchurManager.getInstance();
                // Triggered if player has just given the correct item to Fetchur, or if sba isn't in sync (already handed in quest)
                if (unformattedText.contains(fetchur.getFetchurTaskCompletedPhrase())) {
                    fetchur.saveLastTimeFetched();
                } else if (!fetchur.hasFetchedToday() && unformattedText.contains(fetchur.getFetchurAlreadyDidTaskPhrase())) {
                    fetchur.saveLastTimeFetched();
                }
                // Tries to check if a message is from a player to add the player profile icon
            } else if (Feature.SHOW_ITEM_COOLDOWNS.isEnabled()) {
                if (unformattedText.equals("You laid an egg!")) {
                    // Put the Chicken Head on cooldown for 5 seconds when the player lays an egg.
                    CooldownManager.put("CHICKEN_HEAD");
                }
            }

            if (Feature.NO_ARROWS_LEFT_ALERT.isEnabled()) {
                if (NO_ARROWS_LEFT_PATTERN.matcher(formattedText).matches()) {
                    main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5);
                    main.getRenderListener().setSubtitleFeature(Feature.NO_ARROWS_LEFT_ALERT);
                    main.getRenderListener().setArrowsLeft(-1);
                    return false;

                } else if ((matcher = ONLY_HAVE_ARROWS_LEFT_PATTERN.matcher(formattedText)).matches()) {
                    int arrowsLeft = Integer.parseInt(matcher.group("arrows"));
                    main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5);
                    main.getRenderListener().setSubtitleFeature(Feature.NO_ARROWS_LEFT_ALERT);
                    main.getRenderListener().setArrowsLeft(arrowsLeft);
                    main.getRenderListener().setArrowsType(matcher.group("type"));
                    return false;
                }
            }

            if (main.getInventoryUtils().getInventoryType() == InventoryType.SALVAGING
                    && Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY.isEnabled(FeatureSetting.SHOW_SALVAGE_ESSENCES_COUNTER)) {
                main.getDungeonManager().addSalvagedEssences(unformattedText);
            }

            if (main.getUtils().isInDungeon()) {
                Matcher reviveMessageMatcher = REVIVE_MESSAGE_PATTERN.matcher(unformattedText);

                if (reviveMessageMatcher.matches() && MC.level != null) {
                    List<AbstractClientPlayer> players = MC.level.players();

                    String revivedPlayerName = reviveMessageMatcher.group("revivedPlayer");
                    String reviverName = reviveMessageMatcher.group("reviver");
                    AbstractClientPlayer revivedPlayer = null;
                    AbstractClientPlayer revivingPlayer = null;

                    for (AbstractClientPlayer player : players) {
                        if (revivedPlayer != null && revivingPlayer != null) {
                            break;
                        }

                        if (player.getName().getString().equals(revivedPlayerName)) {
                            revivedPlayer = player;
                            lastRevive = Util.getMillis();
                        }

                        if (reviverName != null && player.getName().getString().equals(reviverName)) {
                            revivingPlayer = player;
                        }
                    }

                    SkyblockEvents.DUNGEON_PLAYER_REVIVE.invoker().onDungeonPlayerRevive(revivedPlayer, revivingPlayer);
                }

                if (Feature.SHOW_DUNGEON_MILESTONE.isEnabled()) {
                    DungeonMilestone dungeonMilestone = main.getDungeonManager().parseMilestone(formattedText);
                    if (dungeonMilestone != null) {
                        main.getDungeonManager().setDungeonMilestone(dungeonMilestone);
                    }
                }

                if (Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY.isEnabled()) {
                    main.getDungeonManager().addBonusEssence(formattedText);
                }
            }


            if (ABILITY_CHAT_PATTERN.matcher(formattedText).matches() && MC.player != null) {
                CooldownManager.put(MC.player.getMainHandItem());

            } else if ((matcher = PROFILE_CHAT_PATTERN.matcher(strippedText)).matches()) {
                String profile = matcher.group(1);
                main.getUtils().setProfileName(profile);

            } else if ((matcher = SWITCH_PROFILE_CHAT_PATTERN.matcher(strippedText)).matches()) {
                String profile = matcher.group(1);
                main.getUtils().setProfileName(profile);

            }
        }

        if (unformattedText.contains("§eis elected Mayor for the year, §6gg§e!")) {
            if (!main.getUtils().isAlpha()) {
                HoverEvent hoverEvent = component.getStyle().getHoverEvent();
                if (hoverEvent instanceof HoverEvent.ShowText(Component value)) {
                    String[] lines = TextUtils.getFormattedText(value).split("\n"); // 1.21.5
                    String mayorName = lines[0].substring(lines[0].lastIndexOf(" ") + 1);

                    // Update new mayor data from API
                    DataUtils.loadOnlineData(new ElectionRequest(mayorName));

                    main.getUtils().setMayor(mayorName);
                    LOGGER.info("Mayor changed to {}", mayorName);
                }
            }
        }

        return true;
    }

    private void onTickStart(Minecraft mc) {
        timerTick++;
        ScoreboardManager.tick();
        if (mc.level == null) {
            // To be able to change the gui when the level is not loaded. For modmenu etc.
            main.getRenderListener().setGui();
        }

        if (actionBarParser.getHealthUpdate() != null && System.currentTimeMillis() - actionBarParser.getLastHealthUpdate() > 3000) {
            actionBarParser.setHealthUpdate(null);
        }

        updateHealthAttributes(mc);
        PetManager.getInstance().checkCurrentPet(mc);
        flyingCheck();

        if (timerTick == 20) {
            // Add natural mana every second (increase is based on your max mana).
            if (main.getRenderListener().isPredictMana()) {
                float mana = PlayerStat.MANA.getValue();
                float maxMana = PlayerStat.MAX_MANA.getValue();

                // If regen-ing, cap at the max mana
                if (mana < maxMana) {
                    DeployableManager.DeployableEntry activeDeployable = DeployableManager.getInstance().getActiveDeployable();
                    float predictedRegenMana = maxMana / 50;

                    if (activeDeployable != null)
                        predictedRegenMana += (float) (maxMana * activeDeployable.getDeployable().getManaRegen() / 50);

                    PlayerStat.MANA.setValue(Math.min(mana + predictedRegenMana, maxMana));
                }
                // If above mana cap, do nothing
            }

            if (Feature.DUNGEON_DEATH_COUNTER.isEnabled() && main.getUtils().isInDungeon()
                    && main.getDungeonManager().isPlayerListInfoEnabled()) {
                main.getDungeonManager().updateDeathsFromPlayerListInfo();
            }
            if (savePersistentFlag) {
                main.getPersistentValuesManager().saveValues();
                savePersistentFlag = false;
            }
        } else if (timerTick % 5 == 0) { // Check inventory, location, updates, and skeleton helmet every 1/4 second.
            LocalPlayer player = mc.player;

            if (player != null) {
                EndstoneProtectorManager.checkGolemStatus();
                TabListParser.parse();
                main.getUtils().parseSidebar();
                main.getInventoryUtils().checkIfInventoryIsFull(mc, player);

                if (main.getUtils().isOnSkyblock()) {
                    main.getInventoryUtils().checkIfWearingSkeletonHelmet(player);
                    main.getInventoryUtils().checkIfUsingArrowPoison(player);
                    main.getInventoryUtils().checkIfWearingSlayerArmor(player);
                    main.getInventoryUtils().checkIfThunderBottle(player);
                    if (Feature.FETCHUR_TODAY.isEnabled()) {
                        FetchurManager.getInstance().recalculateFetchurItem();
                    }
                    checkPetMilestones(mc);
                }

                if (mc.screen == null && main.getPlayerListener().didntRecentlyJoinWorld()
                        && (!main.getUtils().isInDungeon()
                        || Util.getMillis() - lastDeath > 1000
                        && Util.getMillis() - lastRevive > 1000)) {
                    main.getInventoryUtils().calculateInventoryDifference(player.getInventory().getNonEquipmentItems());
                }

                if (Feature.BAIT_LIST.isEnabled() && isHoldingRod()) {
                    BaitManager.getInstance().refreshBaits(player);
                }
            }
            main.getInventoryUtils().cleanUpPickupLog();

        } else if (timerTick > 20) { // To keep the timer going from 1 to 21 only.
            timerTick = 1;
        }
    }

    private void onEntityTick(LivingEntity livingEntity) {
        if (!main.getUtils().isOnSkyblock()) return;

        // Detect Broodmother spawn
        if (Feature.BROOD_MOTHER_ALERT.isEnabled() && LocationUtils.isOn(Island.SPIDERS_DEN)) {
            if (livingEntity.hasCustomName() && livingEntity.getY() > 165 && livingEntity.getName().getString().contains("Broodmother")) {
                if (lastBroodmother == -1 || System.currentTimeMillis() - lastBroodmother > 15000) {
                    lastBroodmother = System.currentTimeMillis();
                    main.getRenderListener().setTitleFeature(Feature.BROOD_MOTHER_ALERT);
                }
                if (livingEntity.tickCount < 13 && livingEntity.tickCount % 3 == 0) {
                    Utils.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5);
                }
            }
        }
        if (Feature.BAL_BOSS_ALERT.isEnabled() && LocationUtils.isOn(Island.CRYSTAL_HOLLOWS) && MC.level != null) {
            if (lastBal == -1 || System.currentTimeMillis() - lastBal > 60000) {
                for (Entity cubes : MC.level.entitiesForRendering()) {
                    if (cubes instanceof MagmaCube magmaCube) {
                        // Find a big bal boss
                        if (magmaCube.getSize() > 10 && lastBalEntityId != magmaCube.getId()) {
                            lastBal = System.currentTimeMillis();
                            lastBalEntityId = magmaCube.getId();
                            main.getRenderListener().setTitleFeature(Feature.BAL_BOSS_ALERT);
                            break;
                        }
                    }
                }
            }
            if (main.getRenderListener().getTitleFeature() == Feature.BAL_BOSS_ALERT && timerTick % 4 == 0) {
                Utils.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP,0.5);
            }
        }

        if (livingEntity.tickCount < 20) {
            if (Feature.HIDE_OTHER_PLAYERS_PRESENTS.isEnabled()) {
                JerryPresent.detectJerryPresent(livingEntity);
            }

            if (/*Feature.HIDE_PLAYERS_NEAR_NPCS.isEnabled() && */
                    livingEntity instanceof RemotePlayer
                    && !main.getUtils().isGuest()
                    && !LocationUtils.isOn(Island.DUNGEON)) {
                float health = livingEntity.getHealth();

                if (NPCUtils.getNpcLocations().containsKey(livingEntity.getId())) {
                    if (health != 20.0F) {
                        NPCUtils.getNpcLocations().remove(livingEntity.getId());
                        return;
                    }
                } else if (NPCUtils.isNPC(livingEntity)) {
                    NPCUtils.getNpcLocations().put(livingEntity.getId(), livingEntity.position());
                    return;
                }
            }
        }

        if (livingEntity instanceof ArmorStand armorStand) {
            DeployableManager.getInstance().detectDeployables(armorStand);

            if (livingEntity.hasCustomName()) {
                if (LocationUtils.isOn(Island.PRIVATE_ISLAND) && !main.getUtils().isGuest()) {
                    int cooldown = Feature.WARNING_TIME.numberValue().intValue() * 1000 + 10000;
                    String nameTag = TextUtils.getFormattedText(livingEntity.getCustomName());
                    if (Feature.MINION_FULL_WARNING.isEnabled() && nameTag.equals("§cMy storage is full! :(")) {
                        long now = System.currentTimeMillis();
                        if (now - lastMinionSound > cooldown) {
                            lastMinionSound = now;
                            main.getUtils().playLoudSound(SoundEvents.ITEM_PICKUP, 1);
                            main.getRenderListener().setSubtitleFeature(Feature.MINION_FULL_WARNING);
                        }
                    } else if (Feature.MINION_STOP_WARNING.isEnabled()) {
                        Matcher matcher = MINION_CANT_REACH_PATTERN.matcher(nameTag);
                        if (matcher.matches()) {
                            long now = System.currentTimeMillis();
                            if (now - lastMinionSound > cooldown) {
                                lastMinionSound = now;
                                main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1);

                                String mobName = matcher.group("mobName");
                                main.getRenderListener().setCannotReachMobName(mobName);
                                main.getRenderListener().setSubtitleFeature(Feature.MINION_STOP_WARNING);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Builds and caches tooltip components inserted before the rarity line.
     * <p>
     * The cache is keyed by {@link ItemStack} reference equality. Since
     * {@link net.minecraft.world.entity.LivingEntity#getItemBySlot} returns the same instance
     * from an internal {@link java.util.EnumMap} as long as the slot has not changed, this is
     * sufficient for most cases.
     * <p>
     * Known exception: auction house preview tooltips update their lore every second (e.g. "Ends in: 32m 36s"),
     * which causes a new {@link ItemStack} instance to be created each time. As a result, the cache
     * is invalidated and rebuilt every second for such items. This is acceptable given the low frequency.
     * @param itemStack      the item whose tooltip is being built
     * @param tooltipContext context for tooltip rendering
     * @param tooltipFlag    flags controlling tooltip detail level
     * @param components     the tooltip component list being built
     */
    private void onGetComponentFirst(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> components) {
        if (components.isEmpty() || !main.getUtils().isOnSkyblock()) return;

        // First
        int insertAt = components.size() - 1; // 1 line for the rarity
        if (tooltipFlag.isAdvanced()) {
            insertAt -= 2; // 1 line for the item name, and 1 line for the nbt
            if (itemStack.isDamaged()) {
                insertAt--; // 1 line for damage
            }
        }
        insertAt = Math.max(0, insertAt);

        // Caching
        if (lastTooltipFirstItemStack == itemStack) {
            components.addAll(insertAt, cachedTooltipFirstComponents);
            return;
        }

        this.cachedTooltipFirstComponents.clear();
        this.lastTooltipFirstItemStack = itemStack;

        CompoundTag extraAttributes = ItemUtils.getExtraAttributes(itemStack);
        if (extraAttributes != null) {
            if (Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE.isEnabled() && extraAttributes.contains("baseStatBoostPercentage")) {
                int baseStatBoost = extraAttributes.getIntOr("baseStatBoostPercentage", 0);
                ColorCode colorCode = Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE.getRestrictedColor();
                if (colorCode == null) colorCode = ColorCode.WHITE;

                if (Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE.isEnabled(FeatureSetting.BASE_STAT_COLOR_BY_RARITY)) {
                    int rarityIndex = baseStatBoost / 10;
                    if (rarityIndex < 0) rarityIndex = 0;
                    if (rarityIndex >= SkyblockRarity.values().length) rarityIndex = SkyblockRarity.values().length - 1;

                    colorCode = SkyblockRarity.values()[rarityIndex].getColorCode();
                }

                cachedTooltipFirstComponents.add(
                        Component.literal(Translations.getMessage("tooltip.baseStatBoost")).withColor(ColorCode.GRAY.getColor())
                                .append(Component.literal("+" + baseStatBoost + "%").withColor(colorCode.getColor()))
                );
            }

            EnchantManager.insertStackingEnchantProgress(cachedTooltipFirstComponents, extraAttributes);

            if (Feature.SHOW_SWORD_KILLS.isEnabled() && extraAttributes.contains("sword_kills")) {
                ColorCode colorCode = Feature.SHOW_SWORD_KILLS.getRestrictedColor();
                if (colorCode == null) colorCode = ColorCode.WHITE;
                int swordKills = extraAttributes.getIntOr("sword_kills", 0);

                cachedTooltipFirstComponents.add(
                        Component.literal(Translations.getMessage("tooltip.swordKills")).withColor(ColorCode.GRAY.getColor())
                                .append(Component.literal(String.valueOf(swordKills)).withColor(colorCode.getColor()))
                );
            }

            if (Feature.SHOW_ITEM_DUNGEON_FLOOR.isEnabled() && extraAttributes.contains("item_tier")) {
                ColorCode colorCode = Feature.SHOW_ITEM_DUNGEON_FLOOR.getRestrictedColor();
                if (colorCode == null) colorCode = ColorCode.WHITE;
                int floor = extraAttributes.getIntOr("item_tier", 0);
                String floorString = floor == 0 ? "Entrance" : Integer.toString(floor);

                cachedTooltipFirstComponents.add(
                        Component.literal(Translations.getMessage("tooltip.itemDungeonFloor")).withColor(ColorCode.GRAY.getColor())
                                .append(Component.literal(floorString).withColor(colorCode.getColor()))
                );
            }
        }

        components.addAll(insertAt, cachedTooltipFirstComponents);
    }

    private void onGetComponentLast(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> components) {
        if (components.isEmpty() || !main.getUtils().isOnSkyblock()) return;

        // Last
        Feature feature = Feature.ENCHANTMENT_LORE_PARSING;
        int[] enchantmentLoreIdx = null;
        if (feature.isEnabled()) {
            enchantmentLoreIdx = EnchantManager.parseEnchants(components, itemStack);

            boolean showMissingEnchants = feature.isEnabled(FeatureSetting.SHOW_MISSING_ENCHANTS)
                    && SkyblockKeyBinding.SHOW_MISSING_ENCHANTS.isKeyDown();
            if (showMissingEnchants) {
                List<Component> missingLines = EnchantManager.getCachedMissingEnchantsComponent();

                if (!missingLines.isEmpty()) {
                    int addIndex;
                    if (enchantmentLoreIdx != null) {
                        addIndex = enchantmentLoreIdx[1] + 1;
                    } else {
                        addIndex = components.size();
                        for (int i = 0; i < components.size(); i++) {
                            Component c = components.get(i);
                            if (c.getSiblings().isEmpty()
                                    && c.getContents() instanceof PlainTextContents contents
                                    && StringUtil.isNullOrEmpty(contents.text())) {
                                addIndex = i;
                                break;
                            }
                        }
                    }
                    components.add(addIndex++, Component.empty());
                    components.add(addIndex++, Component.literal(Translations.getMessage("tooltip.missingTitle"))
                            .withColor(ColorCode.RED.getColor()));
                    for (Component line : missingLines) {
                        components.add(addIndex++, line);
                    }
                }
            }
        }

        if (Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS.isEnabled()) {
            boolean replaceItemName = Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS.isEnabled(
                    FeatureSetting.DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME
            );
            int startIndex = replaceItemName ? 1 : 0;

            for (int i = startIndex; i < components.size(); i++) {
                // Ignore enchantment lines which are processed by EnchantManager
                if (enchantmentLoreIdx != null && i >= enchantmentLoreIdx[0] && i <= enchantmentLoreIdx[1]) {
                    continue;
                }

                Component loreLine = components.get(i);
                Component replacedLine = RomanNumeralParser.replaceNumeralsWithIntegers(loreLine);

                if (replacedLine != loreLine) {
                    components.set(i, replacedLine);
                }
            }
        }

        String itemId = null;
        if (Feature.SHOW_SKYBLOCK_ITEM_ID.isEnabled() || Feature.DEVELOPER_MODE.isEnabled()) {
            itemId = ItemUtils.getSkyblockItemID(itemStack);

            if (itemId != null) {
                Component tooltipLine = Component.literal( "skyblock:" + itemId).withColor(ColorCode.DARK_GRAY.getColor());

                if (tooltipFlag.isAdvanced()) {
                    for (int i = components.size(); i-- > 0; ) {
                        Component component = components.get(i);

                        if (component != null && component.getString().startsWith("minecraft:")) {
                            components.add(i + 1, tooltipLine);
                            break;
                        }
                    }
                } else {
                    components.add(tooltipLine);
                }
            }
        }

        if (Feature.ITEM_PRICES_IN_TOOLTIP.isEnabled()) {
            addItemPricesToTooltip(itemId, itemStack, tooltipContext, tooltipFlag, components);
        }
    }

    private void handleKeybinds() {
        while (SkyblockKeyBinding.OPEN_SETTINGS.consumeClick()) {
            main.getUtils().setFadingIn(true);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);

        }
        while (SkyblockKeyBinding.OPEN_EDIT_GUI.consumeClick()) {
            main.getUtils().setFadingIn(false);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.EDIT_LOCATIONS, 0, null);

        }
        while (SkyblockKeyBinding.ANSWER_ABIPHONE_OR_OPTION.consumeClick()) {
            if (cachedChatRunCommand != null && MC.player != null && MC.player.connection.isAcceptingMessages()) {
                MC.player.connection.sendChat(cachedChatRunCommand);
            }
        }
        while (SkyblockKeyBinding.DEVELOPER_COPY_NBT.consumeClick()) {
            if (Feature.DEVELOPER_MODE.isEnabled()) {
                DevUtils.copyData();
            }
        }

        if (Feature.DUNGEONS_MAP_DISPLAY.isEnabled(FeatureSetting.CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD)
                && main.getUtils().isInDungeon()) {
            while (SkyblockKeyBinding.DECREASE_DUNGEON_MAP_ZOOM.consumeClick()) {
                DungeonMapManager.decreaseZoomByStep();
            }
            while (SkyblockKeyBinding.INCREASE_DUNGEON_MAP_ZOOM.consumeClick()) {
                DungeonMapManager.increaseZoomByStep();
            }
        }
    }

    public void onPlaySound(SoundInstance soundInstance, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
//        LOGGER.info(
//                "location: {}, pitch: {}, volume: {}, isRelative: {}",
//                soundInstance.getLocation(), soundInstance.getPitch(), soundInstance.getVolume(), soundInstance.isRelative()
//        );
        // Ignore sounds that don't have a specific location like GUIs
        if (!main.getUtils().isOnSkyblock() || soundInstance.isRelative()) return;

        if (Feature.STOP_RAT_SOUNDS.isEnabled() && soundInstance.getSource() == SoundSource.NEUTRAL) {
            boolean isRatSound = RAT_SOUNDS.stream().anyMatch(ratSound ->
                    soundInstance.getIdentifier().equals(ratSound.location())
                    && soundInstance.getPitch() == ratSound.pitch()
                    && soundInstance.getVolume() == ratSound.volume()
            );
            if (isRatSound && (Feature.STOP_RAT_SOUNDS.isDisabled(FeatureSetting.STOP_ONLY_RAT_SQUEAK)
                    || soundInstance.getIdentifier().equals(SoundEvents.BAT_AMBIENT.location()))) {
                cir.cancel();
                return;
            }
        }

        LocalPlayer player = MC.player;

        // When a player opens a backpack, a chest open sound is played at the player's location.
        if (Feature.BACKPACK_OPENING_SOUND.isEnabled()
                && System.currentTimeMillis() - main.getScreenListener().getLastBackpackOpenMs() < 500
                && soundInstance.getIdentifier().equals(SoundEvents.CHEST_OPEN.location())) {
            // When a player opens a backpack, a chest open sound is played at the player's location.
            if (player != null &&
                    Math.round(soundInstance.getX()) == player.position().x() &&
                    Math.round(soundInstance.getY()) == player.position().y() &&
                    Math.round(soundInstance.getZ()) == player.position().z()) {
                cir.cancel();
                return;
            }
        }

        if (Feature.STOP_BONZO_STAFF_SOUNDS.isEnabled() && BONZO_STAFF_SOUNDS.contains(soundInstance.getIdentifier())
                && MC.player != null) {
            String skyblockId = ItemUtils.getSkyblockItemID(MC.player.getMainHandItem());
            if (skyblockId != null && skyblockId.endsWith("BONZO_STAFF")) {
                cir.cancel();
                return;
            }
        }
    }

    private void onDungeonPlayerDeath(AbstractClientPlayer player, String username, String cause) {
        LocalPlayer localPlayer = MC.player;
        if (localPlayer == null) return;

        //  Resets all user input on death as to not walk backwards or strafe into the portal
        if (Feature.PREVENT_MOVEMENT_ON_DEATH.isEnabled() && player == localPlayer) {
            KeyMapping.releaseAll();
        }

        /*
        Don't show log for losing all items when the player dies in dungeons.
         The items come back after the player is revived and the large log causes a distraction.
         */
        if (Feature.ITEM_PICKUP_LOG.isEnabled() && player == localPlayer && main.getUtils().isInDungeon()) {
            lastDeath = Util.getMillis();
            main.getInventoryUtils().resetPreviousInventory();
        }

        if (Feature.DUNGEON_DEATH_COUNTER.isEnabled() && main.getUtils().isInDungeon()) {
            DungeonPlayer dungeonPlayer = main.getDungeonManager().getDungeonPlayerByName(username);
            if (dungeonPlayer != null) {
                // Hypixel sends another death message if the player disconnects. Don't count two deaths if the player
                // disconnects while dead.
                if (cause.contains("disconnected") && dungeonPlayer.isGhost()) {
                    return;
                }
                main.getDungeonManager().addDeath();

            } else if (player == localPlayer) { // TODO Keep track of a variable in the manager for the player's dungeon state
                // Hypixel sends another death message if the player disconnects. Don't count two deaths if the player
                // disconnects while dead. We can use flying state to check if player is a ghost.
                if (cause.contains("disconnected") && localPlayer.getAbilities().mayfly) {
                    return;
                }
                main.getDungeonManager().addDeath();

            } else {
                LOGGER.warn("Could not record death for {}. This dungeon player isn't in the registry.", username);
            }
        }
    }

    private void onDungeonPlayerRevive(AbstractClientPlayer revivedPlayer, AbstractClientPlayer revivingPlayer) {
        if (revivedPlayer == MC.player) {
            lastRevive = Util.getMillis();
        }

        // Reset the previous inventory so the screen doesn't get spammed with a large pickup log
        if (Feature.ITEM_PICKUP_LOG.isEnabled()) {
            main.getInventoryUtils().resetPreviousInventory();
        }
    }

    private void onBlockBreak(BlockPos blockPos, long l) {
        if (MC.level == null || MC.player == null) return;

        BlockState blockState = MC.level.getBlockState(blockPos);

        SkyblockOre minedOre = SkyblockOre.getByStateOrNull(blockState);
        if (minedOre != null) {
            switch (minedOre.getBlockType()) {
                case ORE:
                case DWARVEN_METAL:
                case GEMSTONE:
                    main.getPersistentValuesManager().addOresMined();
                    break;
            }

            if (DevUtils.isLoggingSkyBlockOre()) {
                Utils.sendMessage("§eMined ore: §f" + minedOre.name());
            }
        }
        if (Feature.SHOW_ITEM_COOLDOWNS.isEnabled()) {
            String itemId = ItemUtils.getSkyblockItemID(MC.player.getMainHandItem());
            if (itemId == null) return;

            if ((itemId.equals("JUNGLE_AXE") || itemId.equals("TREECAPITATOR_AXE")) && blockState.is(BlockTags.LOGS)) {
                long cooldownTime = CooldownManager.getItemCooldown(itemId);
                // Min cooldown time is 400 because anything lower than that can allow the player to hit a block
                // already marked for block removal by treecap/jungle axe ability
                PetManager.Pet pet = main.getPetCacheManager().getCurrentPet();
                if (pet != null
                        && pet.getPetInfo().getPetRarity() == SkyblockRarity.LEGENDARY
                        && pet.getPetInfo().getPetSkyblockId().equalsIgnoreCase("monkey")) {
                    cooldownTime -=  (int) (2000 * (0.005 * pet.getPetLevel()));
                }
                CooldownManager.put(itemId, Math.max(cooldownTime, 400));
            }
        }
    }

    private InteractionResult onRightClickBlock(Player player, Level world, InteractionHand hand) {
        if (player.getMainHandItem().getItem() == Items.FISHING_ROD) {
            if (Feature.SHOW_ITEM_COOLDOWNS.isEnabled()) {
                String itemId = ItemUtils.getSkyblockItemID(player.getMainHandItem());
                // Grappling hook cool-down
                if (itemId != null && itemId.equals("GRAPPLING_HOOK") && player.fishing != null) {
                    boolean wearingFullBatPerson = InventoryUtils.isWearingFullSet(player, InventoryUtils.BAT_PERSON_SET_IDS);
                    int cooldownTime = wearingFullBatPerson ? 0 : CooldownManager.getItemCooldown(itemId);
                    CooldownManager.put(itemId, cooldownTime);
                }
            }
        }

        return InteractionResult.PASS;
    }

    public boolean aboutToJoinSkyblockServer() {
        return Util.getMillis() - lastSkyblockServerJoinAttempt < 6000;
    }

    public boolean didntRecentlyJoinWorld() {
        return (Util.getMillis() - lastWorldJoin) > 3000;
    }

    public int getMaxTickers() {
        return actionBarParser.getMaxTickers();
    }

    public int getTickers() {
        return actionBarParser.getTickers();
    }

    public void updateLastSecondHealth() {
        float health = PlayerStat.HEALTH.getValue();
        // Update the health gained/lost over the last second
        if (Feature.HEALTH_UPDATES.isEnabled() && actionBarParser.getLastSecondHealth() != health) {
            actionBarParser.setHealthUpdate(health - actionBarParser.getLastSecondHealth());
            actionBarParser.setLastHealthUpdate(System.currentTimeMillis());
        }
        actionBarParser.setLastSecondHealth(health);
    }

    public boolean shouldResetMouse() {
        return System.currentTimeMillis() - main.getScreenListener().getLastContainerCloseMs() > 150L;
    }

    /**
     * Check if our Player is holding a Fishing Rod, and filters out the Grapple Hook and Soul Whip and other items
     * that are {@link Items#FISHING_ROD}s but aren't used for fishing. This is done by checking for the item type of
     * "FISHING ROD" which is displayed beside the item rarity.
     * @return {@code true} if the player is holding a fishing rod that can be used for fishing, {@code false} otherwise
     */
    public boolean isHoldingRod() {
        LocalPlayer player = MC.player;

        if (player != null) {
            ItemStack item = player.getMainHandItem();
            if (item == ItemStack.EMPTY || item.getItem() != Items.FISHING_ROD) return false;

            ItemClassification itemClassification = ItemUtils.getItemClassification(item);
            return itemClassification != null && itemClassification.type() == ItemType.FISHING_ROD;
        }
        return false;
    }

    public boolean isHoldingMiningTool() {
        LocalPlayer player = MC.player;
        if (player == null) return false;

        ItemStack item = player.getMainHandItem();
        if (item == ItemStack.EMPTY) return false;

        ItemClassification itemClassification = ItemUtils.getItemClassification(item);
        if (itemClassification == null) return false;

        return switch (itemClassification.type()) {
            case PICKAXE, GAUNTLET, DRILL -> true;
            case null, default -> false;
        };
    }

    public boolean isHoldingFireFreeze() {
        LocalPlayer player = MC.player;

        if (player != null) {
            ItemStack item = player.getMainHandItem();
            if (item != ItemStack.EMPTY && item.getCustomName() != null) {
                String itemName = item.getCustomName().getString();
                return itemName.contains("Fire Freeze Staff");
            }
        }
        return false;
    }

    private Component playerSymbolsDisplay(Component root) {
        LocalPlayer localPlayer = MC.player;
        if (localPlayer == null) return root;

        // For some reason guild chat messages still contain color codes in the unformatted text
        String username = TextUtils.stripColor(root.getString().split(":")[0]);

        // Remove chat channel prefix
        if (username.contains(">")){
            username = username.substring(username.indexOf('>') + 1).trim();
        }

        // Check if stripped username is a real username or the player
        if (!TextUtils.isUsername(username) && !username.equals("**MINECRAFTUSERNAME**")) return root;

        // Remove rank prefix and guild rank suffix if exists
        String finalUsername  = TextUtils.stripUsername(username);

        // Search in tablist
        Collection<PlayerInfo> networkPlayerInfos = localPlayer.connection.getListedOnlinePlayers();
        Optional<PlayerInfo> result = networkPlayerInfos.stream()
                .filter(pi -> {
                    String profileName = pi.getProfile().name();
                    return profileName!= null && profileName.equals(finalUsername);
                })
                .findAny();

        // Put in cache if found
        if (result.isPresent()) {
            PlayerTabOverlay tabList = Minecraft.getInstance().gui.getTabList();
            namesWithSymbols.put(username, TextUtils.getFormattedText(tabList.getNameForDisplay(result.get())).trim());
        }

        // Check cache regardless if found nearby
        if (namesWithSymbols.containsKey(username)){
            String usernameWithSymbols = namesWithSymbols.get(username);
            String suffix = " ";

            if (Feature.PLAYER_SYMBOLS_IN_CHAT.isEnabled(FeatureSetting.SHOW_PROFILE_TYPE)) {
                Matcher m = PROFILE_TYPE_SYMBOL.matcher(usernameWithSymbols);
                if (m.find()) {
                    suffix += m.group(0);
                }
            }
            if (Feature.PLAYER_SYMBOLS_IN_CHAT.isEnabled(FeatureSetting.SHOW_NETHER_FACTION)) {
                Matcher m = NETHER_FACTION_SYMBOL.matcher(usernameWithSymbols);
                if (m.find()) {
                    suffix += m.group(0);
                }
            }
            if (!suffix.isBlank()) {
                return TextUtils.replaceComponent(root, finalUsername, finalUsername + suffix);
            }
        }

        return root;
    }

    /**
     * Updates health/ max health attributes
     */
    private void updateHealthAttributes(Minecraft MC) {
        LocalPlayer p = MC.player;
        if (p != null) {
            if (main.getUtils().isOnRift()) {
                if (Feature.HEALTH_BAR.isEnabled() || Feature.HEALTH_TEXT.isEnabled()) {
                    PlayerStat.MAX_RIFT_HEALTH.setValue(p.getMaxHealth());
                    PlayerStat.HEALTH.setValue(p.getHealth());
                }
            } else {
                // Reverse calculate the player's health by using the player's vanilla hearts.
                // Also calculate the health change for the gui item.
                if (Feature.HEALTH_BAR.isEnabled(FeatureSetting.HEALTH_PREDICTION)) {
                    float newHealth = PlayerStat.HEALTH.getValue() > PlayerStat.MAX_HEALTH.getValue()
                            ? PlayerStat.HEALTH.getValue()
                            : Math.round(PlayerStat.MAX_HEALTH.getValue() * ((p.getHealth()) / p.getMaxHealth()));
                    PlayerStat.HEALTH.setValue(newHealth);
                }
            }
        }
    }

    /**
     * Update mining/fishing pet tracker numbers when the player opens the skill menu
     */
    private void checkPetMilestones(Minecraft mc) {
        if (main.getInventoryUtils().getInventoryType() == InventoryType.SKILL_TYPE_MENU) {
            SkillType skill = SkillType.getFromString(main.getInventoryUtils().getInventorySubtype());
            if (mc.screen instanceof ContainerScreen containerScreen && (skill == SkillType.MINING || skill == SkillType.FISHING)) {
                ChestMenu container = containerScreen.getMenu();
                NonNullList<ItemStack> itemList = container.getItems();

                ItemStack milestoneItem = itemList.get(51);
                // The player may persistently try to get the item :)
                if (milestoneItem == ItemStack.EMPTY) return;

                List<String> lore = ItemUtils.getItemLore(milestoneItem);
                // No milestone items in new profiles
                if (lore.isEmpty()) return;

                String milestoneProgress = TextUtils.stripColor(lore.getLast());

                Matcher m = NEXT_TIER_PET_PROGRESS.matcher(milestoneProgress);
                int total = -1;
                if (m.matches()) {
                    total = Integer.parseInt(m.group("total").replace(",", ""));
                } else if ((m = MAXED_TIER_PET_PROGRESS.matcher(milestoneProgress)).matches()) {
                    total = Integer.parseInt(m.group("total").replace(",", ""));
                }
                if (total > 0) {
                    PersistentValuesManager.PersistentValues persistentValues = main.getPersistentValuesManager().getPersistentValues();
                    int original;
                    if (skill == SkillType.FISHING) {
                        original = persistentValues.getSeaCreaturesKilled();
                        main.getPersistentValuesManager().getPersistentValues().setSeaCreaturesKilled(total);
                    } else {
                        original = persistentValues.getOresMined();
                        main.getPersistentValuesManager().getPersistentValues().setOresMined(total);
                    }
                    if (original != total) {
                        main.getPersistentValuesManager().saveValues();
                    }
                }
            }
        }
    }

    private void setChatRunCommandFromComponent(Component component) {
        for (Component sibling : component.getSiblings()) {
            Style style = sibling.getStyle();
            if (style != Style.EMPTY) {
                ClickEvent clickEvent = style.getClickEvent();
                if (clickEvent instanceof ClickEvent.RunCommand(String command)) {
                    cachedChatRunCommand = command;
                    main.getScheduler().scheduleTask(scheduledTask -> cachedChatRunCommand = null, 20 * 10);
                    return;
                }
            }
        }
    }

    private boolean previousAllowFlyingState = false;

    private void flyingCheck() {
        LocalPlayer thePlayer = MC.player;

        if (thePlayer != null && LocationUtils.isOn(Island.KUUDRA)) {
            if (previousAllowFlyingState != thePlayer.getAbilities().mayfly) {
                // Reset the previous inventory so the screen doesn't get spammed with a large pickup log
                if (Feature.ITEM_PICKUP_LOG.isEnabled()) {
                    main.getInventoryUtils().resetPreviousInventory();
                }

                previousAllowFlyingState = thePlayer.getAbilities().mayfly;
            }
        }
    }

    /**
     * Appends SkyBlock price information to the given tooltip component list.
     *
     * <p>Depending on the enabled {@link FeatureSetting settings}, the following lines may be added:
     * <ul>
     *   <li><b>Lowest BIN prices</b> - from <a href="https://moulberry.codes">moulberry.codes</a></li>
     *   <li><b>Lowest BIN average prices</b> — from <a href="https://moulberry.codes">moulberry.codes</a>,
     *       averaged over the period configured in {@link FeatureSetting#LBIN_AVERAGES_TYPE}
     *       (1, 3, or 7 days). {@link #resolveLowestBinItemId(String, ItemStack)} result is reused
     *       from the Lowest BIN block if both are enabled.</li>
     *   <li><b>Bazaar buy/sell prices</b> — resolved via the Hypixel Bazaar API.
     *       Enchanted books are looked up by their enchantment ID
     *       (e.g. {@code ENCHANTMENT_SHARPNESS_5}) rather than {@code ENCHANTED_BOOK}.</li>
     *   <li><b>NPC sell price</b> — resolved from the SkyBlock items API.</li>
     * </ul>
     *
     * <p>Special item types (PET, RUNE, NEW_YEAR_CAKE, POTION, perfect stat boost items)
     * are resolved to their API-specific item IDs before lookup, via
     * {@link #resolveLowestBinItemId(String, ItemStack)}.
     *
     * <p>If {@link FeatureSetting#ALWAYS_SHOW_BULK_PRICE} is disabled and
     * {@code LEFT SHIFT} is not held, prices are shown for a single item.
     * When the stack count is greater than one and shift is held, prices are
     * multiplied by {@link ItemStack#getCount()}. In the latter case a
     * {@code [LSHIFT] for x<count>} hint is prepended to the added lines.
     *
     * <p>Price labels are rendered in the feature's configured color, or in
     * chroma if {@link Feature#isChroma()} is {@code true}.
     *
     * <p>Results are cached per item stack and invalidated when the item identity,
     * stack count, shift state, feature settings (bold/chroma), or any underlying
     * data source changes. The cache is keyed on {@link ItemStack} reference equality
     * first, falling back to {@link ItemStack#hashItemAndComponents} for different
     * references with identical content.
     *
     * <p><b>This method is called on the render thread.</b>
     * @param itemId        the SkyBlock item ID, e.g. {@code "ASPECT_OF_THE_DRAGON"};
     *                      may be {@code null}, in which case it is resolved lazily
     *                      via {@link ItemUtils#getSkyblockItemID(ItemStack)}
     * @param itemStack     the item whose tooltip is being built
     * @param tooltipContext Minecraft tooltip context passed through from the event
     * @param tooltipFlag   Minecraft tooltip flag passed through from the event
     * @param components    mutable tooltip line list to append price lines to
     * @since 2.2.3
     */
    private void addItemPricesToTooltip(String itemId,
                                        ItemStack itemStack,
                                        Item.TooltipContext tooltipContext,
                                        TooltipFlag tooltipFlag,
                                        List<Component> components) {
        InventoryType inventoryType = main.getInventoryUtils().getInventoryType();
        if (inventoryType == InventoryType.CROSEUS_CHEST_MENU) return;

        Feature feature = Feature.ITEM_PRICES_IN_TOOLTIP;
        boolean boldLines = feature.isEnabled(FeatureSetting.BOLD_PRICE_LINES);
        boolean isChroma = feature.isChroma();
        boolean isLeftShiftPressed = feature.isEnabled(FeatureSetting.ALWAYS_SHOW_BULK_PRICE)
                || SkyblockKeyBinding.SHOW_BULK_PRICE.isKeyDown();
        int count = itemStack.getCount();

        var lowestBinData        = main.getLowestBinData();
        var lBinAveragesData     = main.getLowestBinAveragesData();
        BazaarData bazaarData    = main.getBazaarData();
        ItemsData itemsData      = main.getItemsData();

        // Snapshot current data-source identities
        int lowestBinIdentity    = System.identityHashCode(lowestBinData);
        int lbinAveragesIdentity = System.identityHashCode(lBinAveragesData);
        long bazaarIdentity      = bazaarData.getLastUpdated();
        long itemsDataIdentity   = itemsData.getLastUpdated();

        // Cache validity checks
        boolean isReferenceSame  = this.lastPriceItemStack == itemStack;
        boolean isStateSame      = this.lastPriceItemCount == count && this.lastPriceShiftState == isLeftShiftPressed;
        boolean isSettingsSame   = (this.lastPriceBoldLines == boldLines) && (this.lastPriceIsChroma == isChroma);
        boolean isDataSame       = (this.lastLowestBinIdentity == lowestBinIdentity)
                                && (this.lastLBinAveragesIdentity == lbinAveragesIdentity)
                                && (this.lastBazaarIdentity == bazaarIdentity)
                                && (this.lastItemsDataIdentity == itemsDataIdentity);

        boolean isContentSame = false;
        if (!isReferenceSame && isStateSame && isSettingsSame && isDataSame) {
            int currentHash = ItemStack.hashItemAndComponents(itemStack);
            if (currentHash == this.lastPriceItemHash) {
                isContentSame = true;
                this.lastPriceItemStack = itemStack;
            }
        }

        if ((isReferenceSame && isStateSame && isSettingsSame && isDataSame) || isContentSame) {
            components.addAll(this.cachedPriceComponents);
            return;
        }

        this.cachedPriceComponents.clear();
        this.lastPriceItemStack       = itemStack;
        this.lastPriceItemCount       = count;
        this.lastPriceShiftState      = isLeftShiftPressed;
        this.lastPriceBoldLines       = boldLines;
        this.lastPriceIsChroma        = isChroma;
        this.lastPriceItemHash        = ItemStack.hashItemAndComponents(itemStack);
        this.lastLowestBinIdentity    = lowestBinIdentity;
        this.lastLBinAveragesIdentity = lbinAveragesIdentity;
        this.lastBazaarIdentity       = bazaarIdentity;
        this.lastItemsDataIdentity    = itemsDataIdentity;

        boolean expTableItems = inventoryType == InventoryType.SUPERPAIRS || inventoryType == InventoryType.EXP_TABLE_RNG;
        int countToBeShown = isLeftShiftPressed && !expTableItems ? count : 1;
        UnaryOperator<Style> textColor = style -> isChroma
                ? style.withBold(boldLines).withColor(DrawUtils.CHROMA_TEXT_COLOR)
                : style.withBold(boldLines).withColor(feature.getColor());
        if (itemId == null) itemId = ItemUtils.getSkyblockItemID(itemStack);

        ResolvedItemId resolvedItemId = null;
        if (feature.isEnabled(FeatureSetting.LOWEST_BIN_PRICES_IN_TOOLTIP)) {
            resolvedItemId = resolveLowestBinItemId(itemId, itemStack);

            if (resolvedItemId != null) { // itemId was null (unresolvable)
                double price = lookupWithFallback(lowestBinData, resolvedItemId);

                if (price > 0.0D) {
                    this.cachedPriceComponents.add(Component.literal(Translations.getMessage("tooltip.lowestBinPrice"))
                            .withStyle(textColor).append(TextUtils.formatPrice(price, 0, boldLines)));
                }
            }
        }

        if (feature.isEnabled(FeatureSetting.LBIN_AVERAGE_PRICES_IN_TOOLTIP)) {
            if (resolvedItemId == null) resolvedItemId = resolveLowestBinItemId(itemId, itemStack);

            if (resolvedItemId != null) { // itemId was null (unresolvable)
                double price = lookupWithFallback(lBinAveragesData, resolvedItemId);

                if (price > 0.0D) {
                    this.cachedPriceComponents.add(Component.literal(Translations.getMessage("tooltip.lowestBinAveragePrice"))
                            .withStyle(textColor).append(TextUtils.formatPrice(price, 0, boldLines)));
                }
            }
        }

        if (feature.isEnabled(FeatureSetting.BAZAAR_PRICES_IN_TOOLTIP)) {
            // Resolve the specific Bazaar item ID using the extracted helper method
            int[] countOutParam = {0};
            String apiItemId = resolveBazaarItemId(itemId, itemStack, itemsData, countOutParam);
            if (countOutParam[0] != 0) {
                count = countOutParam[0];
                countToBeShown = isLeftShiftPressed && !expTableItems ? count : 1;
                this.lastPriceItemCount = count;
            }

            BazaarData.Product product = null;
            if (!StringUtil.isNullOrEmpty(apiItemId)) {
                product = bazaarData.getProducts().get(apiItemId);
            }

            if (product != null) {
                Component buyPrice = TextUtils.formatPrice(product.getInstaBuyPrice() * countToBeShown, 1, boldLines);
                Component sellPrice = TextUtils.formatPrice(product.getInstaSellPrice() * countToBeShown, 1, boldLines);

                this.cachedPriceComponents.add(Component.literal(Translations.getMessage("tooltip.buyPrice"))
                        .withStyle(textColor).append(buyPrice));
                this.cachedPriceComponents.add(Component.literal(Translations.getMessage("tooltip.sellPrice"))
                        .withStyle(textColor).append(sellPrice));
            }
        }

        if (feature.isEnabled(FeatureSetting.NPC_SELL_PRICES_IN_TOOLTIP)) {
            ItemsData.Item item = null;
            if (!StringUtil.isNullOrEmpty(itemId)) {
                item = itemsData.getById(itemId);
            } else {
                // Fallback lookup by exact display name
                Component customName = itemStack.getCustomName();
                if (customName != null) {
                    String customNameString = customName.getString();
                    if ((itemStack.is(Items.ORANGE_STAINED_GLASS) || itemStack.is(Items.ORANGE_TERRACOTTA))
                            && "Orange".equals(customNameString)) {
                        // Exception for InventoryType#CHRONOMATRON
                        //noinspection DataFlowIssue Exception
                        item = null;
                    } else if (!customNameString.isBlank()) {
                        item = itemsData.getByName(customNameString);
                    }
                }
            }

            if (item != null && item.getNpcSellPrice() != 0.0D) {
                double price = item.getNpcSellPrice() * countToBeShown;
                Component npcSellPrice = TextUtils.formatPrice(price, price <= 10.0D ? 2 : 0, boldLines);

                this.cachedPriceComponents.add(Component.literal(Translations.getMessage("tooltip.npcSellPrice"))
                        .withStyle(textColor).append(npcSellPrice));
            }
        }

        if (!expTableItems && !isLeftShiftPressed && count > 1 && !this.cachedPriceComponents.isEmpty()) {
            String translatedKeyMsg = SkyblockKeyBinding.SHOW_BULK_PRICE.getKeyBinding().getTranslatedKeyMessage().getString();
            this.cachedPriceComponents.addFirst(Component.literal("[" + translatedKeyMsg + "] for x" + count)
                    .withColor(ColorCode.DARK_GRAY.getColor()));
        }

        components.addAll(this.cachedPriceComponents);
    }

    /**
     * Looks up the price for the resolved item in the given data map,
     * falling back to the base item ID (without the extra suffix) if not found.
     * @return price from the map, or {@code -1.0} if absent
     */
    public static double lookupWithFallback(@NonNull Object2DoubleMap<String> data, @NonNull ResolvedItemId resolved) {
        String apiItemId = resolved.apiItemId();
        double price = data.getOrDefault(apiItemId, -1.0D);
        String extraString = resolved.extraString();

        // Fallback to base item API ID if the specific variant (e.g., max level) is not found
        if (extraString != null && price == -1.0D) {
            price = data.getOrDefault(apiItemId.replace(extraString, ""), -1.0D);
        }
        return price;
    }

    /**
     * Resolves Lowest BIN API item ID to use for price lookups.
     *
     * <p>Most items use their raw SkyBlock item ID directly. The following special
     * types require additional NBT inspection to produce a lookup-ready ID:
     * <ul>
     *   <li><b>PET</b> — resolved to {@code <PET_ID>;<RARITY_ORDINAL>}, with an
     *       optional {@code +<level>} suffix for pets at level 100 or 200.</li>
     *   <li><b>RUNE</b> — resolved to {@code <TYPE>_RUNE;<LEVEL>}.</li>
     *   <li><b>NEW_YEAR_CAKE</b> — resolved to {@code NEW_YEAR_CAKE+<YEAR>}.</li>
     *   <li><b>POTION</b> — resolved to {@code POTION_<TYPE>;<LEVEL>}.</li>
     *   <li><b>null (pet fallback)</b> — when {@code itemId} is {@code null}, attempts
     *       to resolve a pet ID from the item's custom name via {@code PET_CUSTOM_NAME_PATTERN},
     *       producing {@code <PET_ID>;<RARITY_ORDINAL>} with an optional {@code +<level>}
     *       suffix for level 100 or 200 pets. Returns {@code null} if the pattern does not
     *       match or rarity cannot be determined.</li>
     *   <li><b>Default</b> — items with {@code baseStatBoostPercentage == 50} receive
     *       a {@code +PERFECT} suffix.</li>
     * </ul>
     * @param itemId    the raw SkyBlock item ID; if {@code null}, a pet resolution from
     *                  the item's custom name is attempted before giving up
     * @param itemStack the item stack, used to read NBT extra attributes and custom name
     * @return a {@link ResolvedItemId} containing the resolved API item ID and optional
     *         variant suffix (e.g. {@code "+PERFECT"}), or {@code null} if {@code itemId}
     *         was {@code null} and pet resolution from the custom name also failed
     * @since 2.2.3
     */
    @Nullable
    public static ResolvedItemId resolveLowestBinItemId(String itemId, ItemStack itemStack) {
        String apiItemId   = itemId;
        String extraString = null;

        switch (apiItemId) {
            case "PET" -> {
                PetManager.Pet pet = PetManager.getInstance().getPetFromItemStack(itemStack, false);

                if (pet != null) {
                    PetInfo petInfo = pet.getPetInfo();

                    if (petInfo != null) {
                        apiItemId = petInfo.getPetSkyblockId() + ";" + petInfo.getPetRarity().ordinal();

                        int petLevel = pet.getPetLevel();
                        if (100 <= petLevel) {
                            extraString = "+" + (petLevel / 100) * 100;
                            apiItemId += extraString;
                        }
                    }
                }
            }
            case "RUNE" -> {
                CompoundTag extraAttributes = ItemUtils.getExtraAttributes(itemStack);

                if (extraAttributes != null) {
                    SkyblockRune rune = ItemUtils.getRuneData(extraAttributes);

                    if (rune != null) {
                        apiItemId = rune.getType() + "_RUNE;" + rune.getLevel();
                    }
                }
            }
            case "NEW_YEAR_CAKE" -> {
                CompoundTag extraAttributes = ItemUtils.getExtraAttributes(itemStack);

                if (extraAttributes != null) {
                    int cakeYears = extraAttributes.getIntOr("new_years_cake", -1);

                    if (cakeYears != -1) {
                        apiItemId += "+" + cakeYears;
                    }
                }
            }
            case "POTION" -> {
                CompoundTag extraAttributes = ItemUtils.getExtraAttributes(itemStack);

                if (extraAttributes != null) {
                    String potion      = extraAttributes.getStringOr("potion", "");
                    int    potionLevel = extraAttributes.getIntOr("potion_level", -1);

                    if (!potion.isEmpty() && potionLevel != -1) {
                        apiItemId += "_" + potion.toUpperCase(Locale.ENGLISH) + ";" + potionLevel;
                    }
                }
            }
            case null -> {
                Component customName = itemStack.getCustomName();
                if (customName == null) break;

                String formattedCustomName = TextUtils.getFormattedText(customName, true);
                if (formattedCustomName.isBlank()) break;

                Matcher m = PET_CUSTOM_NAME_PATTERN.matcher(formattedCustomName);
                if (m.find()) {
                    ColorCode rarityColor = ColorCode.getByChar(m.group("rarityColor").charAt(0));
                    if (rarityColor == null) break;

                    SkyblockRarity rarity = SkyblockRarity.getByColorCode(rarityColor);
                    if (rarity == null) break;

                    String nameGroup = m.group("name")
                            .toUpperCase(Locale.ENGLISH)
                            .replace(" ", "_")
                            .replace("_EGG", "");
                    apiItemId = nameGroup + ";" + rarity.ordinal();
                }
            }
            default -> {
                CompoundTag extraAttributes = ItemUtils.getExtraAttributes(itemStack);

                if (extraAttributes != null) {
                    int baseStatBoost = extraAttributes.getIntOr("baseStatBoostPercentage", -1);

                    if (baseStatBoost == 50) {
                        extraString = "+PERFECT";
                        apiItemId += extraString;
                    }
                }
            }
        }

        return apiItemId == null ? null : new ResolvedItemId(apiItemId, extraString);
    }

    /**
     * Resolves the Bazaar API item ID to use for price lookups.
     * <p>Enchanted books are looked up by their first enchantment ID
     * (e.g., {@code ENCHANTMENT_SHARPNESS_5}) rather than the generic {@code ENCHANTED_BOOK} ID.
     * If the provided item ID is {@code null}, this method attempts to resolve the ID
     * by parsing the item's custom display name and lore.
     * @param itemId    the raw SkyBlock item ID, or {@code null} if unknown
     * @param itemStack the item stack, used to read enchantments, custom names, and lore
     * @param itemsData the cached SkyBlock items data used for name-to-ID fallback resolution
     * @return the resolved Bazaar API item ID, or the uppercase custom name as a fallback
     * @since 2.2.5
     */
    public static String resolveBazaarItemId(@Nullable String itemId, ItemStack itemStack, ItemsData itemsData, int[] countOutParam) {
        String apiItemId = itemId;

        if ("ENCHANTED_BOOK".equals(itemId)) {
            var enchantments = ItemUtils.getEnchantments(itemStack).entrySet().iterator();

            if (enchantments.hasNext()) {
                var enchant = enchantments.next();
                apiItemId = "ENCHANTMENT_" + enchant.getKey().toUpperCase(Locale.ENGLISH) + "_" + enchant.getValue();
            }
        } else if ("ATTRIBUTE_SHARD".equals(itemId)) {
            Component customName = itemStack.getCustomName();

            if (customName != null) {
                String attributeShardResolution = attributeShardResolution(countOutParam, customName.getString());
                if (attributeShardResolution != null) return attributeShardResolution;
            }
        } else if (itemId == null) {
            // Try item resolution from custom name and lore
            Component customName = itemStack.getCustomName();
            if (customName == null) return apiItemId;

            String customNameStr = customName.getString();
            if (customNameStr.isBlank()) return apiItemId;

            String essenceResolution = essenceResolution(countOutParam, customNameStr);
            if (essenceResolution != null) return essenceResolution;

            String attributeShardResolution = attributeShardResolution(countOutParam, customNameStr);
            if (attributeShardResolution != null) return attributeShardResolution;

            Matcher m = EnchantManager.ENCHANTMENT_PATTERN.matcher(customNameStr);
            boolean isNamedEnchant = m.find();
            boolean isNamedBook = "Enchanted Book".equals(customNameStr);

            if (isNamedEnchant) {
                return enchantedBookResolution(apiItemId, m);
            } else if (isNamedBook) {
                for (Component line : ItemUtils.getItemLoreComponent(itemStack)) {
                    String lineStr = line.getString();
                    if (lineStr.isBlank()) continue;

                    m = EnchantManager.ENCHANTMENT_PATTERN.matcher(lineStr);
                    if (m.find()) {
                        return enchantedBookResolution(apiItemId, m);
                    }
                }
            } else {
                // Reusing the general fallback logic
                ItemsData.Item item = itemsData.getByName(customNameStr);
                return item != null
                        ? item.getId()
                        : customNameStr.toUpperCase(Locale.ENGLISH).replace(" ", "_");
            }
        }

        return apiItemId;
    }

    /**
     * Resolves the SkyBlock API item ID and quantity for an Essence item from its custom name.
     * <p>
     * Matches the given name against {@link #ESSENCE_NAME_PATTERN}. If a match is found,
     * the parsed amount is stored in {@code countOutParam[0]} (defaulting to 1 if absent),
     * and the formatted API ID is returned.
     * @param countOutParam    a single-element array used as an out parameter to store the parsed item count
     * @param customNameString the plain-text custom name of the item
     * @return the formatted API item ID (e.g., {@code ESSENCE_WITHER}), or {@code null} if the name does not match
     */
    @Nullable
    public static String essenceResolution(int[] countOutParam, String customNameString) {
        Matcher m = ESSENCE_NAME_PATTERN.matcher(customNameString);
        if (m.find()) {
            String type   = m.group("type");
            String amount = m.group("amount");
            countOutParam[0] = StringUtil.isNullOrEmpty(amount) ? 1 : Integer.parseInt(amount);
            return "ESSENCE_" + type.toUpperCase(Locale.ENGLISH).replace(" ", "_");
        }
        return null;
    }

    /**
     * Resolves the SkyBlock API item ID and quantity for an Attribute Shard from its custom name.
     * <p>
     * Matches the given name against {@link #ATTRIBUTE_SHARD_NAME_PATTERN}. If a match is found,
     * the parsed amount is stored in {@code countOutParam[0]} (defaulting to 1 if absent).
     * This method applies specific string replacements to handle edge cases where the in-game
     * entity name differs from the official API ID (e.g., mapping "Bogged" to "SEA_ARCHER").
     * @param countOutParam    a single-element array used as an out parameter to store the parsed item count
     * @param customNameString the plain-text custom name of the item
     * @return the formatted API item ID (e.g., {@code SHARD_SEA_ARCHER}), or {@code null} if the name does not match
     */
    @Nullable
    public static String attributeShardResolution(int[] countOutParam, String customNameString) {
        Matcher m = ATTRIBUTE_SHARD_NAME_PATTERN.matcher(customNameString);
        if (m.find()) {
            String type   = m.group("type");
            String amount = m.group("amount");
            countOutParam[0] = StringUtil.isNullOrEmpty(amount) ? 1 : Integer.parseInt(amount);
            return "SHARD_" + type.toUpperCase(Locale.ENGLISH)
                    .replace(" ", "_")
                    // TODO add to remote data instead of hardcode
                    .replace("END_STONE", "ENDSTONE")
                    .replace("BOGGED", "SEA_ARCHER")
                    .replace("STRIDERSURFER", "STRIDER_SURFER")
                    .replace("ABYSSAL_LANTERNFISH", "ABYSSAL_LANTERN")
                    .replace("LOCH_EMPEROR", "SEA_EMPEROR")
                    .replace("CINDERBAT", "CINDER_BAT");
        }
        return null;
    }

    /**
     * Resolves the Bazaar API item ID for an enchanted book by extracting the enchantment
     * name and level from a successful regex match.
     * <p>This method reads the {@code "enchant"} and {@code "levelNumeral"} capture groups
     * from the provided matcher, converts the Roman numeral to an integer, and formats
     * the result into the standard Hypixel Bazaar API format (e.g., {@code ENCHANTMENT_SHARPNESS_5}).
     * If the enchantment is unrecognized or invalid (i.e., resolved as a dummy enchant),
     * the original {@code apiItemId} is returned unchanged.
     * @param apiItemId the fallback API item ID to return if the resolution fails
     * @param m         the regex matcher containing the successful enchantment match groups
     * @return the formatted Bazaar API item ID, or the original {@code apiItemId} if unresolved
     * @since 2.2.5
     */
    public static String enchantedBookResolution(String apiItemId, Matcher m) {
        String enchantName = m.group("enchant");
        int levelNumeral =  RomanNumeralParser.parseNumeral(m.group("levelNumeral"));

        if (!StringUtil.isNullOrEmpty(enchantName) && levelNumeral != 0) {
            enchantName = enchantName.toLowerCase(Locale.ENGLISH);

            Enchant enchant = EnchantManager.getEnchants().getFromLore(enchantName);
            if (!(enchant instanceof Enchant.Dummy)) {
                apiItemId = "ENCHANTMENT_" + enchant.getNbtName().toUpperCase(Locale.ENGLISH) + "_" + levelNumeral;
            }
        }
        return apiItemId;
    }

    /**
     * Holds the result of {@link #resolveLowestBinItemId(String, ItemStack)}.
     * @param apiItemId   the resolved Lowest BIN API item ID ready for price map lookup
     * @param extraString the variant suffix that was appended to {@code apiItemId}
     *                    (e.g. {@code "+PERFECT"}, {@code "+100"}), or {@code null}
     *                    if no suffix was added; used for the fallback lookup when
     *                    the specific variant has no listing
     */
    public record ResolvedItemId(String apiItemId, @Nullable String extraString) {}

    private record RatSound(Identifier location, float volume, float pitch) {}

}