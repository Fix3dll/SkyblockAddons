package com.fix3dll.skyblockaddons.listeners;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.config.PersistentValuesManager;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.ItemType;
import com.fix3dll.skyblockaddons.core.PlayerStat;
import com.fix3dll.skyblockaddons.core.SkillType;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.SkyblockOre;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.core.npc.NPCUtils;
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
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerTracker;
import com.fix3dll.skyblockaddons.features.tablist.TabListParser;
import com.fix3dll.skyblockaddons.gui.screens.IslandWarpGui;
import com.fix3dll.skyblockaddons.utils.ActionBarParser;
import com.fix3dll.skyblockaddons.utils.DevUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.InventoryUtils;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.RomanNumeralParser;
import com.fix3dll.skyblockaddons.utils.ScoreboardManager;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.fix3dll.skyblockaddons.utils.data.requests.MayorRequest;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
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

    private static final ObjectOpenHashSet<String> SOUP_RANDOM_MESSAGES = ObjectOpenHashSet.of(
            "I feel like I can fly!", "What was in that soup?",
            "Hmm… tasty!", "Hmm... tasty!", "You can now fly for 2 minutes.", "Your flight has been extended for 2 extra minutes.",
            "You can now fly for 200 minutes.", "Your flight has been extended for 200 extra minutes."
    );

    private static final Set<ResourceLocation> BONZO_STAFF_SOUNDS = Set.of(
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
    private long lastFishingAlert = 0;
    private long lastBobberEnteredWater = Long.MAX_VALUE;
    private long lastSkyblockServerJoinAttempt = 0;
    private long lastDeath = 0;
    private long lastRevive = 0;
    private long lastMaddoxLevelTime;
    private String lastMaddoxSlayerType;

    @Getter private long rainmakerTimeEnd = -1;

    private boolean oldBobberIsInWater;
    private double oldBobberPosY = 0;

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

    public static final ResourceLocation SBA_FIRST_PHASE = SkyblockAddons.resourceLocation("first");
    public static final ResourceLocation SBA_LAST_PHASE = SkyblockAddons.resourceLocation("last");

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
//            FishParticleManager.clearParticleCache();
            main.getRenderListener().setMaxRiftHealth(0.0F);
            PlayerStat.MAX_RIFT_HEALTH.setValue(0);
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
                String petName = matcher.group("name");
                String petCosmetic = matcher.group("cosmetic");
                if (!StringUtil.isNullOrEmpty(petCosmetic)) {
                    petName += petCosmetic.replace("§r", "");
                }
                PetManager.getInstance().updateAndSetCurrentLevelledPet(
                        matcher.group("newLevel"), matcher.group("rarityColor"), petName
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
                int spawned = unformattedText.contains("Magma Slug") || unformattedText.contains("Bayou Sludge") ? 4 : 1;
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
                    DataUtils.loadOnlineData(new MayorRequest(mayorName));

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
                    if (shouldTriggerFishingIndicator(mc)) { // The logic fits better in its own function
                        main.getUtils().playLoudSound(SoundEvents.ARROW_HIT_PLAYER, 0.8);
                    }
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

        if (livingEntity.tickCount < 5) {
            if (Feature.HIDE_OTHER_PLAYERS_PRESENTS.isEnabled()) {
                JerryPresent.detectJerryPresent(livingEntity);
            }

            if (livingEntity instanceof RemotePlayer remotePlayer && Feature.HIDE_PLAYERS_NEAR_NPCS.isEnabled()
                    && !main.getUtils().isGuest() && !LocationUtils.isOn(Island.DUNGEON)) {
                float health = remotePlayer.getHealth();

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

        CustomData extraAttributes = ItemUtils.getExtraAttributes(itemStack);
        if (extraAttributes != null) {
            if (Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE.isEnabled() && extraAttributes.contains("baseStatBoostPercentage")) {
                int baseStatBoost = extraAttributes.read(Codec.INT.fieldOf("baseStatBoostPercentage")).result().orElse(0);

                ColorCode colorCode = Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE.getRestrictedColor();
                if (Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE.isEnabled(FeatureSetting.BASE_STAT_COLOR_BY_RARITY)) {
                    int rarityIndex = baseStatBoost / 10;
                    if (rarityIndex < 0) rarityIndex = 0;
                    if (rarityIndex >= SkyblockRarity.values().length) rarityIndex = SkyblockRarity.values().length - 1;

                    colorCode = SkyblockRarity.values()[rarityIndex].getColorCode();
                }
                components.add(insertAt++, Component.literal("§7Base Stat Boost: " + colorCode + "+" + baseStatBoost + "%"));
            }

            if (Feature.SHOW_STACKING_ENCHANT_PROGRESS.isEnabled()) {
                insertAt = EnchantManager.insertStackingEnchantProgress(components, extraAttributes, insertAt);
            }

            if (Feature.SHOW_SWORD_KILLS.isEnabled() && extraAttributes.contains("sword_kills")) {
                ColorCode colorCode = Feature.SHOW_SWORD_KILLS.getRestrictedColor();
                int swordKills = extraAttributes.read(Codec.INT.fieldOf("sword_kills")).result().orElse(0);
                components.add(insertAt++, Component.literal("§7Sword Kills: " + colorCode + swordKills));
            }

            if (Feature.SHOW_ITEM_DUNGEON_FLOOR.isEnabled() && extraAttributes.contains("item_tier")) {
                int floor = extraAttributes.read(Codec.INT.fieldOf("item_tier")).result().orElse(0);
                ColorCode colorCode = Feature.SHOW_ITEM_DUNGEON_FLOOR.getRestrictedColor();
                components.add(insertAt, Component.literal("§7Obtained on Floor: " + colorCode + (floor == 0 ? "Entrance" : floor)));
            }
        }
    }

    private void onGetComponentLast(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> components) {
        if (components.isEmpty() || !main.getUtils().isOnSkyblock()) return;

        // Last
        if (Feature.ENCHANTMENT_LORE_PARSING.isEnabled()) {
            EnchantManager.parseEnchants(components, itemStack);
        }

        if (Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS.isEnabled()) {
            boolean replaceItemName = Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS.isEnabled(
                    FeatureSetting.DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME
            );
            int startIndex = replaceItemName ? 1 : 0;

            // TODO clean this shit + EnchantManager
            for (int i = startIndex; i < components.size(); i++) {
                String line = components.get(i).getString();
                boolean legacy = line.contains("§");
                if (!legacy) {
                    line = TextUtils.getFormattedText(components.get(i));
                }

                String parsedFormattedText = RomanNumeralParser.replaceNumeralsWithIntegers(line);

                if (legacy) {
                    components.set(i, EnchantManager.CREATE_STYLED_COMPONENT.apply(parsedFormattedText));
                } else {
                    components.set(i, Component.literal(parsedFormattedText));
                }
            }
        }

        if (Feature.SHOW_SKYBLOCK_ITEM_ID.isEnabled() || Feature.DEVELOPER_MODE.isEnabled()) {
            String itemId = ItemUtils.getSkyblockItemID(itemStack);
            Component tooltipLine = Component.literal(ColorCode.DARK_GRAY + "skyblock:" + itemId);

            if (itemId != null) {
                if (tooltipFlag.isAdvanced()) {
                    for (int i = components.size(); i-- > 0; ) {
                        if (TextUtils.getFormattedText(components.get(i)).startsWith(ColorCode.DARK_GRAY + "minecraft:")) {
                            components.add(i + 1, tooltipLine);
                            break;
                        }
                    }
                } else {
                    components.add(tooltipLine);
                }
            }
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

    public void onPlaySound(SoundInstance soundInstance, CallbackInfo ci) {
//        LOGGER.info(
//                "location: {}, pitch: {}, volume: {}, isRelative: {}",
//                soundInstance.getLocation(), soundInstance.getPitch(), soundInstance.getVolume(), soundInstance.isRelative()
//        );
        // Ignore sounds that don't have a specific location like GUIs
        if (!main.getUtils().isOnSkyblock() || soundInstance.isRelative()) return;

        if (Feature.STOP_RAT_SOUNDS.isEnabled() && soundInstance.getSource() == SoundSource.NEUTRAL) {
            boolean isRatSound = RAT_SOUNDS.stream().anyMatch(ratSound ->
                    soundInstance.getLocation().equals(ratSound.location())
                    && soundInstance.getPitch() == ratSound.pitch()
                    && soundInstance.getVolume() == ratSound.volume()
            );
            if (isRatSound && (Feature.STOP_RAT_SOUNDS.isDisabled(FeatureSetting.STOP_ONLY_RAT_SQUEAK)
                    || soundInstance.getLocation().equals(SoundEvents.BAT_AMBIENT.location()))) {
                ci.cancel();
                return;
            }
        }

        LocalPlayer player = MC.player;

        // When a player opens a backpack, a chest open sound is played at the player's location.
        if (Feature.BACKPACK_OPENING_SOUND.isEnabled()
                && System.currentTimeMillis() - main.getScreenListener().getLastBackpackOpenMs() < 500
                && soundInstance.getLocation().equals(SoundEvents.CHEST_OPEN.location())) {
            // When a player opens a backpack, a chest open sound is played at the player's location.
            if (player != null &&
                    Math.round(soundInstance.getX()) == player.position().x() &&
                    Math.round(soundInstance.getY()) == player.position().y() &&
                    Math.round(soundInstance.getZ()) == player.position().z()) {
                ci.cancel();
                return;
            }
        }

        if (Feature.STOP_BONZO_STAFF_SOUNDS.isEnabled() && BONZO_STAFF_SOUNDS.contains(soundInstance.getLocation())
                && MC.player != null) {
            String skyblockId = ItemUtils.getSkyblockItemID(MC.player.getMainHandItem());
            if (skyblockId != null && skyblockId.endsWith("BONZO_STAFF")) {
                ci.cancel();
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
            // Update fishing status if the player is fishing and reels in their rod.
            if (Feature.FISHING_SOUND_INDICATOR.isEnabled() && isHoldingRod()) {
                oldBobberIsInWater = false;
                lastBobberEnteredWater = Long.MAX_VALUE;
                oldBobberPosY = 0;
            }
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
     * Checks if the fishing indicator sound should be played. To play the sound, these conditions have to be met:
     * <p>1. Fishing sound indicator feature is enabled</p>
     * <p>2. The player is on Skyblock (checked in {@link #onTickStart(Minecraft)}</p>
     * <p>3. The player is holding a fishing rod</p>
     * <p>4. The fishing rod is in the water</p>
     * <p>5. The bobber suddenly moves downwards, indicating a fish has been caught</p>
     * @return {@code true} if the fishing alert sound should be played, {@code false} otherwise
     * @see Feature#FISHING_SOUND_INDICATOR
     */
    private boolean shouldTriggerFishingIndicator(Minecraft MC) {
        if (Feature.FISHING_SOUND_INDICATOR.isEnabled() && MC.player != null && MC.player.fishing != null && isHoldingRod()) {
            // Highly consistent detection by checking when the hook has been in the water for a while and
            // suddenly moves downward. The client may rarely bug out with the idle bobbing and trigger a false positive.
            FishingHook bobber = MC.player.fishing;
            long currentTime = System.currentTimeMillis();
            if (bobber.isOpenWaterFishing() && !oldBobberIsInWater) lastBobberEnteredWater = currentTime;
            oldBobberIsInWater = bobber.isOpenWaterFishing();
            if (bobber.isOpenWaterFishing() && Math.abs(bobber.getDeltaMovement().x()) < 0.01 && Math.abs(bobber.getDeltaMovement().z()) < 0.01
                    && currentTime - lastFishingAlert > 1000 && currentTime - lastBobberEnteredWater > 1500) {
                double movement = bobber.getY() - oldBobberPosY; // The Entity#motionY field is inaccurate for this purpose
                oldBobberPosY = bobber.getY();
                if (movement < -0.04d) {
                    lastFishingAlert = currentTime;
                    return true;
                }
            }
        }
        return false;
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

            return ItemUtils.getItemType(item) == ItemType.FISHING_ROD;
        }
        return false;
    }

    public boolean isHoldingMiningTool() {
        LocalPlayer player = MC.player;

        if (player != null) {
            ItemStack item = player.getMainHandItem();
            if (item != ItemStack.EMPTY) {
                ItemType type = ItemUtils.getItemType(item);
                if (type != null) {
                    return switch (type) {
                        case PICKAXE, GAUNTLET, DRILL -> true;
                        default -> false;
                    };
                }
            }
        }
        return false;
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
                .filter(pi -> pi.getProfile().getName() != null)
                .filter(pi -> pi.getProfile().getName().equals(finalUsername))
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
                String formattedRoot = TextUtils.getFormattedText(root).replace(finalUsername, finalUsername + suffix);
                MutableComponent modifiedRoot = Component.literal(formattedRoot);
                Style rootStyle = root.getStyle().getHoverEvent() != null ? root.getStyle() : root.getSiblings().getFirst().getStyle();
                return modifiedRoot.withStyle(style -> style
                        .withClickEvent(rootStyle.getClickEvent())
                        .withHoverEvent(rootStyle.getHoverEvent())
                );
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
                    total = Integer.parseInt(m.group("total").replaceAll(",", ""));
                } else if ((m = MAXED_TIER_PET_PROGRESS.matcher(milestoneProgress)).matches()) {
                    total = Integer.parseInt(m.group("total").replaceAll(",", ""));
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
            }

            previousAllowFlyingState = thePlayer.getAbilities().mayfly;
        }
    }

    private record RatSound(ResourceLocation location, float volume, float pitch) {
    }
}