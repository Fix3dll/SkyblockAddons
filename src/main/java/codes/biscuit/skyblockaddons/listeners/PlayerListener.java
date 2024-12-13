package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.PersistentValuesManager;
import codes.biscuit.skyblockaddons.core.*;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonMilestone;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonPlayer;
import codes.biscuit.skyblockaddons.core.npc.NPCUtils;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreatureManager;
import codes.biscuit.skyblockaddons.events.DungeonPlayerReviveEvent;
import codes.biscuit.skyblockaddons.events.SkyblockBlockBreakEvent;
import codes.biscuit.skyblockaddons.events.SkyblockPlayerDeathEvent;
import codes.biscuit.skyblockaddons.features.BaitManager;
import codes.biscuit.skyblockaddons.features.EndstoneProtectorManager;
import codes.biscuit.skyblockaddons.features.FetchurManager;
import codes.biscuit.skyblockaddons.features.JerryPresent;
import codes.biscuit.skyblockaddons.features.PetManager;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonTracker;
import codes.biscuit.skyblockaddons.features.dungeonmap.DungeonMapManager;
import codes.biscuit.skyblockaddons.features.enchants.EnchantManager;
import codes.biscuit.skyblockaddons.features.fishParticles.FishParticleManager;
import codes.biscuit.skyblockaddons.features.deployables.DeployableManager;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.features.tablist.TabListParser;
import codes.biscuit.skyblockaddons.features.tablist.TabStringType;
import codes.biscuit.skyblockaddons.gui.screens.IslandWarpGui;

import codes.biscuit.skyblockaddons.utils.ActionBarParser;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DevUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.InventoryUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.RomanNumeralParser;
import codes.biscuit.skyblockaddons.utils.ScoreboardManager;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import codes.biscuit.skyblockaddons.utils.data.requests.MayorRequest;
import com.google.common.collect.Sets;
import com.google.common.math.DoubleMath;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.math.RoundingMode;
import java.util.Arrays;
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

//TODO Fix for Hypixel localization
public class PlayerListener {

    private static final Logger logger = SkyblockAddons.getLogger();

    private static final Pattern NO_ARROWS_LEFT_PATTERN = Pattern.compile("§r§c§lQUIVER! §r§cYou have run out of §r(?<type>§.*)§r§c!§r");
    private static final Pattern ONLY_HAVE_ARROWS_LEFT_PATTERN = Pattern.compile("§r§c§lQUIVER! §r§cYou only have (?<arrows>[0-9]+) §r(?<type>§.*) §r§cleft!§r");
    private static final Pattern ABILITY_CHAT_PATTERN = Pattern.compile("§r§aUsed §r§6[A-Za-z ]+§r§a! §r§b\\([0-9]+ Mana\\)§r");
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
    private static final Pattern PET_LEVELED_UP_PATTERN = Pattern.compile("§r§aYour §r§(?<rarityColor>.)(?<name>.*)(?:§r§. ✦)? §r§aleveled up to level §r(?:§.)*(?<newLevel>\\d+)§r§a!§r");
    private static final Pattern PET_ITEM_PATTERN = Pattern.compile("§r§aYour pet is now holding §r§(?<rarityColor>.)(?<petItem>.*)§r§a.§r");

    private static final Set<String> SOUP_RANDOM_MESSAGES = new HashSet<>(Arrays.asList("I feel like I can fly!", "What was in that soup?",
            "Hmm… tasty!", "Hmm... tasty!", "You can now fly for 2 minutes.", "Your flight has been extended for 2 extra minutes.",
            "You can now fly for 200 minutes.", "Your flight has been extended for 200 extra minutes."));

    private static final Set<String> BONZO_STAFF_SOUNDS = new HashSet<>(Arrays.asList("fireworks.blast", "fireworks.blast_far",
            "fireworks.twinkle", "fireworks.twinkle_far", "mob.ghast.moan"));

    // All Rat pet sounds as instance with their respective sound categories, except the sound when it lays a cheese
    private static final Set<PositionedSoundRecord> RAT_SOUNDS = new HashSet<>(Arrays.asList(new PositionedSoundRecord(new ResourceLocation("minecraft", "mob.bat.idle"), 1.0f, 1.1904762f, 0.0f, 0.0f, 0.0f),
            new PositionedSoundRecord(new ResourceLocation("minecraft", "mob.chicken.step"), 0.15f, 1.0f, 0.0f, 0.0f, 0.0f)));

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

    private final SkyblockAddons main = SkyblockAddons.getInstance();
    @Getter private final ActionBarParser actionBarParser = new ActionBarParser();

    // For caching for the PROFILE_TYPE_IN_CHAT feature, saves the last MAX_SIZE names.
    private final LinkedHashMap<String, String> namesWithSymbols = new LinkedHashMap<String, String>(){
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest)
        {
            // MAX_SIZE = 80
            return size() > 80;
        }
    };
    @Getter @Setter private long fireFreezeTimer = 0L;
    private boolean doubleHook = false;

    /**
     * Reset all the timers and stuff when joining a new world.
     */
    @SubscribeEvent()
    public void onWorldJoin(EntityJoinWorldEvent e) {
        Entity entity = e.entity;

        if (entity == Minecraft.getMinecraft().thePlayer) {
            lastWorldJoin = Minecraft.getSystemTime();
            timerTick = 1;
            main.getInventoryUtils().resetPreviousInventory();
            countedEndermen.clear();
            EndstoneProtectorManager.reset();

            IslandWarpGui.Marker doubleWarpMarker = IslandWarpGui.getDoubleWarpMarker();
            if (doubleWarpMarker != null) {
                IslandWarpGui.setDoubleWarpMarker(null);
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp " + doubleWarpMarker.getWarpName());
            }

            NPCUtils.getNpcLocations().clear();
            JerryPresent.getJerryPresents().clear();
            FishParticleManager.clearParticleCache();
        }
    }

    /**
     * Interprets the action bar to extract mana, health, and defence. Enables/disables mana/health prediction,
     * and looks for mana usage messages in chat while predicting.
     */
    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
    public void onChatReceive(ClientChatReceivedEvent e) {
        if (!main.getUtils().isOnHypixel()) {
            return;
        }

        String formattedText = e.message.getFormattedText();
        String unformattedText = e.message.getUnformattedText();
        String strippedText = TextUtils.stripColor(formattedText);

        if (formattedText.startsWith("§7Sending to server ")) {
            lastSkyblockServerJoinAttempt = Minecraft.getSystemTime();
            DragonTracker.getInstance().reset();
            return;
        }

        if (Feature.OUTBID_ALERT_SOUND.isEnabled() && formattedText.matches("§6\\[Auction] §..*§eoutbid you .*")
                && (Feature.OUTBID_ALERT_SOUND_IN_OTHER_GAMES.isEnabled() || main.getUtils().isOnSkyblock())) {
            main.getUtils().playLoudSound("random.orb", 0.5);
        }

        if (!main.getUtils().isOnSkyblock()) return;

        // Type 2 means it's an action bar message.
        if (e.type == 2) {
            // Log the message to the game log if action bar message logging is enabled.
            if (Feature.DEVELOPER_MODE.isEnabled() && DevUtils.isLoggingActionBarMessages()) {
                logger.info("[ACTION BAR] {}", unformattedText);
            }

            // Parse using ActionBarParser and display the rest message instead
            String restMessage = actionBarParser.parseActionBar(unformattedText);
            if (main.isUsingOofModv1() && restMessage.trim().isEmpty()) {
                e.setCanceled(true);
                return;
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
            Matcher matcher;

            if (main.getRenderListener().isPredictMana() && unformattedText.startsWith("Used ") && unformattedText.endsWith("Mana)")) {
                int manaLost = Integer.parseInt(unformattedText.split(Pattern.quote("! ("))[1].split(Pattern.quote(" Mana)"))[0]);
                PlayerStats.MANA.setValue(PlayerStats.MANA.getValue() - manaLost);
            } else if ((matcher = AUTOPET_PATTERN.matcher(formattedText)).matches()) {
                PetManager.getInstance().findCurrentPetFromAutopet(
                        matcher.group("level"), matcher.group("rarityColor"), matcher.group("name")
                );

            } else if ((matcher = PET_LEVELED_UP_PATTERN.matcher(formattedText)).matches()) {
                PetManager.getInstance().updateAndSetCurrentLevelledPet(
                        matcher.group("newLevel"), matcher.group("rarityColor"), matcher.group("name")
                );

            } else if ((matcher = PET_ITEM_PATTERN.matcher(formattedText)).matches()) {
                PetManager.getInstance().updatePetItem(
                        matcher.group("rarityColor"), matcher.group("petItem")
                );

            } else if ((matcher = DEATH_MESSAGE_PATTERN.matcher(unformattedText)).matches()) {
                // Hypixel's dungeon reconnect messages look exactly like death messages.
                String causeOfDeath = matcher.group("causeOfDeath");
                if (!causeOfDeath.equals("reconnected")) {
                    String username = matcher.group("username");

                    EntityPlayer deadPlayer;
                    if (username.equals("You")) {
                        deadPlayer = Minecraft.getMinecraft().thePlayer;
                    } else {
                        deadPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(username);
                    }

                    MinecraftForge.EVENT_BUS.post(new SkyblockPlayerDeathEvent(deadPlayer, username, causeOfDeath));
                }

            } else if (Feature.SUMMONING_EYE_ALERT.isEnabled() && formattedText.equals("§r§6§lRARE DROP! §r§5Summoning Eye§r")) {
                main.getUtils().playLoudSound("random.orb", 0.5); // credits to tomotomo, thanks lol
                main.getRenderListener().setTitleFeature(Feature.SUMMONING_EYE_ALERT);

            } else if (formattedText.equals("§r§aA special §r§5Zealot §r§ahas spawned nearby!§r")) {
                if (Feature.SPECIAL_ZEALOT_ALERT.isEnabled()) {
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setTitleFeature(Feature.SUMMONING_EYE_ALERT);
                    main.getRenderListener().setTitleFeature(Feature.SPECIAL_ZEALOT_ALERT);
                }
                if (Feature.ZEALOT_COUNTER.isEnabled()) {
                    // Edit the message to include counter.
                    e.message = new ChatComponentText(formattedText + ColorCode.GRAY + " (" + main.getPersistentValuesManager().getPersistentValues().getKills() + ")");
                }
                main.getPersistentValuesManager().addEyeResetKills();
                // TODO: Seems like leg warning and num sc killed should be separate features
            } else if (SeaCreatureManager.getInstance().getAllSeaCreatureSpawnMessages().contains(unformattedText)) {
                int spawned = unformattedText.contains("Magma Slug") ? 4 : 1;
                if (doubleHook) {
                    spawned *= 2;
                    doubleHook = false;
                }
                main.getPersistentValuesManager().addSeaCreaturesKilled(spawned);
                if (Feature.LEGENDARY_SEA_CREATURE_WARNING.isEnabled()
                        && SeaCreatureManager.getInstance().getLegendarySeaCreatureSpawnMessages().contains(unformattedText)) {
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setTitleFeature(Feature.LEGENDARY_SEA_CREATURE_WARNING);
                }

            } else if (formattedText.startsWith("§r§eIt's a §r§aDouble Hook§r§e!")) {
                doubleHook = true;

            } else if (Feature.DISABLE_MAGICAL_SOUP_MESSAGES.isEnabled() && SOUP_RANDOM_MESSAGES.contains(unformattedText)) {
                e.setCanceled(true);

            } else if (Feature.DISABLE_TELEPORT_PAD_MESSAGES.isEnabled() && (formattedText.startsWith("§r§aWarped from ") || formattedText.equals("§r§cThis Teleport Pad does not have a destination set!§r"))) {
                e.setCanceled(true);

            } else if (Feature.DISABLE_MORT_MESSAGES.isEnabled() && strippedText.startsWith("[NPC] Mort:")) {
                e.setCanceled(true);

            } else if (strippedText.startsWith("[BOSS] ")) {
                if (Feature.FIRE_FREEZE_TIMER.isEnabled()
                        && strippedText.equals("[BOSS] The Professor: Oh? You found my Guardians' one weakness?")) {
                    fireFreezeTimer = System.currentTimeMillis() + 5000;
                }

                if (Feature.DISABLE_BOSS_MESSAGES.isEnabled())
                    e.setCanceled(true);

            } else if (Feature.SPIRIT_SCEPTRE_DISPLAY.isEnabled()
                    && strippedText.startsWith("Your Implosion hit")
                    || strippedText.startsWith("Your Spirit Sceptre hit")
                    || strippedText.startsWith("Your Molten Wave hit")) {
                matcher = SPIRIT_SCEPTRE_MESSAGE_PATTERN.matcher(unformattedText);
                // Ensure matcher.group gets what it wants, we don't need the whole result
                if (matcher.find()) {
                    this.spiritSceptreHitEnemies = Integer.parseInt(matcher.group("hitEnemies"));
                    this.spiritSceptreDealtDamage = Float.parseFloat(matcher.group("dealtDamage").replace(",", ""));

                    if (Feature.DISABLE_SPIRIT_SCEPTRE_MESSAGES.isEnabled()) {
                        e.setCanceled(true);
                    }
                }
            } else if (SlayerTracker.getInstance().isTrackerEnabled() &&
                    (matcher = SLAYER_COMPLETED_PATTERN.matcher(strippedText)).matches()) { // §r   §r§5§l» §r§7Talk to Maddox to claim your Wolf Slayer XP!§r
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

            } else if (Feature.BIRCH_PARK_RAINMAKER_TIMER.isEnabled() && formattedText.startsWith("§r§eYou added a minute of rain!")) {
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
            } else if (Feature.PLAYER_SYMBOLS_IN_CHAT.isEnabled() && unformattedText.contains(":")) {
                playerSymbolsDisplay(e, unformattedText);
            } else if (Feature.SHOW_ITEM_COOLDOWNS.isEnabled()) {
                if (unformattedText.equals("You laid an egg!")) {
                    // Put the Chicken Head on cooldown for 5 seconds when the player lays an egg.
                    CooldownManager.put("CHICKEN_HEAD");
                }
            }

            if (Feature.NO_ARROWS_LEFT_ALERT.isEnabled()) {
                if (NO_ARROWS_LEFT_PATTERN.matcher(formattedText).matches()) {
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setSubtitleFeature(Feature.NO_ARROWS_LEFT_ALERT);
                    main.getRenderListener().setArrowsLeft(-1);
                    e.setCanceled(true);

                } else if ((matcher = ONLY_HAVE_ARROWS_LEFT_PATTERN.matcher(formattedText)).matches()) {
                    int arrowsLeft = Integer.parseInt(matcher.group("arrows"));
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setSubtitleFeature(Feature.NO_ARROWS_LEFT_ALERT);
                    main.getRenderListener().setArrowsLeft(arrowsLeft);
                    main.getRenderListener().setArrowsType(matcher.group("type"));
                    e.setCanceled(true);
                }
            }

            if (main.getInventoryUtils().getInventoryType() == InventoryType.SALVAGING && Feature.SHOW_SALVAGE_ESSENCES_COUNTER.isEnabled()) {
                main.getDungeonManager().addSalvagedEssences(unformattedText);
            }

            if (main.getUtils().isInDungeon()) {
                Matcher reviveMessageMatcher = REVIVE_MESSAGE_PATTERN.matcher(unformattedText);

                if (reviveMessageMatcher.matches()) {
                    List<EntityPlayer> players = Minecraft.getMinecraft().theWorld.playerEntities;

                    String revivedPlayerName = reviveMessageMatcher.group("revivedPlayer");
                    String reviverName = reviveMessageMatcher.group("reviver");
                    EntityPlayer revivedPlayer = null;
                    EntityPlayer revivingPlayer = null;

                    for (EntityPlayer player : players) {
                        if (revivedPlayer != null && revivingPlayer != null) {
                            break;
                        }

                        if (player.getName().equals(revivedPlayerName)) {
                            revivedPlayer = player;
                            lastRevive = Minecraft.getSystemTime();
                        }

                        if (reviverName != null && player.getName().equals(reviverName)) {
                            revivingPlayer = player;
                        }
                    }

                    MinecraftForge.EVENT_BUS.post(new DungeonPlayerReviveEvent(revivedPlayer, revivingPlayer));
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


            if (ABILITY_CHAT_PATTERN.matcher(formattedText).matches()) {
                CooldownManager.put(Minecraft.getMinecraft().thePlayer.getHeldItem());

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
                HoverEvent hoverEvent = e.message.getChatStyle().getChatHoverEvent();
                if (hoverEvent.getValue() == null || (hoverEvent.getAction() != HoverEvent.Action.SHOW_TEXT))
                    return;

                String[] lines = hoverEvent.getValue().getFormattedText().split("\n");
                String mayorName = lines[0].substring(lines[0].lastIndexOf(" ") + 1);

                // Update new mayor data from API
                DataUtils.loadOnlineData(new MayorRequest(mayorName));

                main.getUtils().setMayor(mayorName);
                logger.info("Mayor changed to " + mayorName);
            }
        }
    }

    private void playerSymbolsDisplay(ClientChatReceivedEvent e, String unformattedText) {
        // For some reason guild chat messages still contain color codes in the unformatted text
        String username = TextUtils.stripColor(unformattedText.split(":")[0]);
        // Remove chat channel prefix
        if(username.contains(">")){
            username = username.substring(username.indexOf('>')+1).trim();
        }
        // Check if stripped username is a real username or the player
        if (TextUtils.isUsername(username) || username.equals("**MINECRAFTUSERNAME**")) {
            // Remove rank prefix and guild rank suffix if exists
            username = TextUtils.stripUsername(username);
            EntityPlayer chattingPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(username);
            // Put player in cache if found nearby
            if(chattingPlayer != null) {
                namesWithSymbols.put(username, chattingPlayer.getDisplayName().getSiblings().get(0).getUnformattedText());
            }
            // Otherwise search in tablist
            else {
                Collection<NetworkPlayerInfo> networkPlayerInfos = Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap();
                String finalUsername = username;
                Optional<NetworkPlayerInfo> result = networkPlayerInfos.stream()
                        .filter(npi -> npi.getDisplayName() != null)
                        .filter(npi -> TabStringType.usernameFromLine(npi.getDisplayName().getFormattedText()).equals(finalUsername))
                        .findAny();
                // Put in cache if found
                if(result.isPresent()){
                    namesWithSymbols.put(username, result.get().getDisplayName().getFormattedText());
                }
            }
            // Check cache regardless if found nearby
            if(namesWithSymbols.containsKey(username)){
                IChatComponent oldMessage = e.message;
                String usernameWithSymbols = namesWithSymbols.get(username);
                String suffix = " ";
                if (Feature.SHOW_PROFILE_TYPE.isEnabled()){
                    Matcher m = PROFILE_TYPE_SYMBOL.matcher(usernameWithSymbols);
                    if(m.find()) {
                        suffix +=  m.group(0);
                    }
                }
                if (Feature.SHOW_NETHER_FACTION.isEnabled()){
                    Matcher m = NETHER_FACTION_SYMBOL.matcher(usernameWithSymbols);
                    if(m.find()) {
                        suffix += m.group(0);
                    }
                }
                if(!suffix.equals(" ")) {
                    String finalSuffix = suffix;
                    String finalUsername = username;
                    TextUtils.transformAnyChatComponent(oldMessage, component -> {
                                if (component instanceof ChatComponentText & ((ChatComponentText)component).text.contains(finalUsername)) {
                                    ChatComponentText textComponent = (ChatComponentText) component;
                                    textComponent.text = textComponent.text.replace(finalUsername, finalUsername + finalSuffix);
                                    return true;
                                }
                                return false;
                            }
                    );
                }
            }
        }
    }

    /**
     * Acts as a callback to set the actionbar message after other mods have a chance to look at the message
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChatReceiveLast(ClientChatReceivedEvent e) {
        if (e.type == 2 && !e.isCanceled()) {
            Iterator<String> itr = actionBarParser.getStringsToRemove().iterator();
            String message = e.message.getUnformattedText();
            while (itr.hasNext()) {
                message = message.replaceAll(" *" + Pattern.quote(itr.next()), "");
            }
            message = message.trim();
            e.message = new ChatComponentText(message);
        }
    }


    /**
     * This method is triggered by the player right-clicking on something.
     * Yes, it says it works for left-clicking blocks too, but it actually doesn't, so please don't use it to detect that.
     * <br>
     * Also, when the player right-clicks on a block, {@code PlayerInteractEvent} gets fired twice. The first time,
     * the correct action type {@code Action.RIGHT_CLICK_BLOCK}, is used. The second time, the action type is
     * {@code Action.RIGHT_CLICK_AIR} for some reason. Both of these events will cause a {@code C08PacketPlayerBlockPlacement}
     * packet to be sent to the server, so block both of them if you want to prevent a block from being placed.
     * <br>
     * Look at {@code Minecraft#rightClickMouse()} to see when the event is fired.
     *
     * @see Minecraft
     */
    @SubscribeEvent()
    public void onInteract(PlayerInteractEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItem = e.entityPlayer.getHeldItem();

        if (main.getUtils().isOnSkyblock() && heldItem != null) {
            if (heldItem.getItem() == Items.fishing_rod
                    && (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)) {
                // Update fishing status if the player is fishing and reels in their rod.
                if (Feature.FISHING_SOUND_INDICATOR.isEnabled() && isHoldingRod()) {
                    oldBobberIsInWater = false;
                    lastBobberEnteredWater = Long.MAX_VALUE;
                    oldBobberPosY = 0;
                }
                if (Feature.SHOW_ITEM_COOLDOWNS.isEnabled()) {
                    String itemId = ItemUtils.getSkyblockItemID(heldItem);
                    // Grappling hook cool-down
                    if (itemId != null && itemId.equals("GRAPPLING_HOOK") && mc.thePlayer.fishEntity != null) {
                        boolean wearingFullBatPerson = InventoryUtils.isWearingFullSet(mc.thePlayer, InventoryUtils.BAT_PERSON_SET_IDS);
                        int cooldownTime = wearingFullBatPerson ? 0 : CooldownManager.getItemCooldown(itemId);
                        CooldownManager.put(itemId, cooldownTime);
                    }
                }
            }
        }
    }

    /**
     * The main timer for a bunch of stuff.
     */
    @SubscribeEvent()
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null) return;
            timerTick++;

            ScoreboardManager.tick();

            if (actionBarParser.getHealthUpdate() != null && System.currentTimeMillis() - actionBarParser.getLastHealthUpdate() > 3000) {
                actionBarParser.setHealthUpdate(null);
            }

            updateHealthAttributes(mc);
            PetManager.getInstance().checkCurrentPet(mc);

            if (timerTick == 20) {
                // Add natural mana every second (increase is based on your max mana).
                if (main.getRenderListener().isPredictMana()) {
                    float mana = PlayerStats.MANA.getValue();
                    float maxMana = PlayerStats.MAX_MANA.getValue();

                    // If regen-ing, cap at the max mana
                    if (mana < maxMana) {
                        DeployableManager.DeployableEntry activeDeployable = DeployableManager.getInstance().getActiveDeployable();
                        float predictedRegenMana = maxMana / 50;

                        if (activeDeployable != null)
                            predictedRegenMana += (float) (maxMana * activeDeployable.getDeployable().getManaRegen() / 50);

                        PlayerStats.MANA.setValue(Math.min(mana + predictedRegenMana, maxMana));
                    }
                    // If above mana cap, do nothing
                }

                if (Feature.DUNGEON_DEATH_COUNTER.isEnabled() && main.getUtils().isInDungeon()
                        && main.getDungeonManager().isPlayerListInfoEnabled()) {
                    main.getDungeonManager().updateDeathsFromPlayerListInfo();
                }
            } else if (timerTick % 5 == 0) { // Check inventory, location, updates, and skeleton helmet every 1/4 second.
                EntityPlayerSP player = mc.thePlayer;

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
                            main.getUtils().playLoudSound("random.successful_hit", 0.8);
                        }
                        if (Feature.FETCHUR_TODAY.isEnabled()) {
                            FetchurManager.getInstance().recalculateFetchurItem();
                        }
                        checkPetMilestones(mc);
                    }

                    if (mc.currentScreen == null && main.getPlayerListener().didntRecentlyJoinWorld()
                            && (!main.getUtils().isInDungeon()
                            || Minecraft.getSystemTime() - lastDeath > 1000
                            && Minecraft.getSystemTime() - lastRevive > 1000)) {
                        main.getInventoryUtils().calculateInventoryDifference(player.inventory.mainInventory);
                    }

                    if (Feature.BAIT_LIST.isEnabled() && isHoldingRod()) {
                        BaitManager.getInstance().refreshBaits();
                    }
                }
                main.getInventoryUtils().cleanUpPickupLog();

            } else if (timerTick > 20) { // To keep the timer going from 1 to 21 only.
                timerTick = 1;
            }
        }
    }

    @SubscribeEvent
    public void onEntityEvent(LivingEvent.LivingUpdateEvent e) {
        if (!main.getUtils().isOnSkyblock()) return;
        Entity entity = e.entity;

        // Detect Broodmother spawn
        if (Feature.BROOD_MOTHER_ALERT.isEnabled() && main.getUtils().getMap() == Island.SPIDERS_DEN) {
            if (entity.hasCustomName() && entity.posY > 165 && entity.getName().contains("Broodmother")) {
                if (lastBroodmother == -1 || System.currentTimeMillis() - lastBroodmother > 15000) {
                    lastBroodmother = System.currentTimeMillis();
                    main.getRenderListener().setTitleFeature(Feature.BROOD_MOTHER_ALERT);
                }
                if (entity.ticksExisted < 13 && entity.ticksExisted % 3 == 0) {
                    main.getUtils().playLoudSound("random.orb", 0.5);
                }
            }
        }
        if (Feature.BAL_BOSS_ALERT.isEnabled() && main.getUtils().getMap() == Island.CRYSTAL_HOLLOWS) {
            if (lastBal == -1 || System.currentTimeMillis() - lastBal > 60000) {
                for (Entity cubes : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                    if (cubes instanceof EntityMagmaCube) {
                        EntityMagmaCube magma = (EntityMagmaCube) cubes;
                        // Find a big bal boss
                        if (magma.getSlimeSize() > 10 && lastBalEntityId != magma.getEntityId()) {
                            lastBal = System.currentTimeMillis();
                            lastBalEntityId = magma.getEntityId();
                            main.getRenderListener().setTitleFeature(Feature.BAL_BOSS_ALERT);
                            break;
                        }
                    }
                }
            }
            if (main.getRenderListener().getTitleFeature() == Feature.BAL_BOSS_ALERT && timerTick % 4 == 0) {
                main.getUtils().playLoudSound("random.orb", 0.5);
            }
        }

        if (entity.ticksExisted < 5) {
            if (Feature.HIDE_OTHER_PLAYERS_PRESENTS.isEnabled()) {
                if (!JerryPresent.getJerryPresents().containsKey(entity.getUniqueID())) {
                    JerryPresent present = JerryPresent.getJerryPresent(entity);
                    if (present != null) {
                        JerryPresent.getJerryPresents().put(entity.getUniqueID(), present);
                        return;
                    }
                }
            }

            if (entity instanceof EntityOtherPlayerMP && Feature.HIDE_PLAYERS_NEAR_NPCS.isEnabled()
                    && !main.getUtils().isGuest() && main.getUtils().getMap() != Island.DUNGEON) {
                float health = ((EntityOtherPlayerMP) entity).getHealth();

                if (NPCUtils.getNpcLocations().containsKey(entity.getUniqueID())) {
                    if (health != 20.0F) {
                        NPCUtils.getNpcLocations().remove(entity.getUniqueID());
                        return;
                    }
                } else if (NPCUtils.isNPC(entity)) {
                    NPCUtils.getNpcLocations().put(entity.getUniqueID(), entity.getPositionVector());
                    return;
                }
            }
        }

        if (entity instanceof EntityArmorStand) {
            DeployableManager.getInstance().detectDeployables((EntityArmorStand) entity);

            if (entity.hasCustomName()) {
                if (main.getUtils().getMap() == Island.PRIVATE_ISLAND && !main.getUtils().isGuest()) {
                    int cooldown = main.getConfigValues().getWarningSeconds() * 1000 + 10000;
                    String nameTag = entity.getCustomNameTag();
                    if (Feature.MINION_FULL_WARNING.isEnabled() && nameTag.equals("§cMy storage is full! :(")) {
                        long now = System.currentTimeMillis();
                        if (now - lastMinionSound > cooldown) {
                            lastMinionSound = now;
                            main.getUtils().playLoudSound("random.pop", 1);
                            main.getRenderListener().setSubtitleFeature(Feature.MINION_FULL_WARNING);
                        }
                    } else if (Feature.MINION_STOP_WARNING.isEnabled()) {
                        Matcher matcher = MINION_CANT_REACH_PATTERN.matcher(nameTag);
                        if (matcher.matches()) {
                            long now = System.currentTimeMillis();
                            if (now - lastMinionSound > cooldown) {
                                lastMinionSound = now;
                                main.getUtils().playLoudSound("random.orb", 1);

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

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (e.target instanceof EntityEnderman) {
            if (isZealot(e.target)) {
                countedEndermen.add(e.target.getUniqueID());
            }
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        if (e.entity instanceof EntityEnderman) {
            if (countedEndermen.remove(e.entity.getUniqueID())) {
                main.getPersistentValuesManager().addKills();
                EndstoneProtectorManager.onKill();
            } else if (main.getUtils().isOnSkyblock() && Feature.ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT.isEnabled()) {
                if (isZealot(e.entity)) {
                    long now = System.currentTimeMillis();
                    if (recentlyKilledZealots.containsKey(now)) {
                        recentlyKilledZealots.get(now).add(e.entity.getPositionVector());
                    } else {
                        recentlyKilledZealots.put(now, Sets.newHashSet(e.entity.getPositionVector()));
                    }

                    explosiveBowExplosions.keySet().removeIf((explosionTime) -> now - explosionTime > 150);
                    Map.Entry<Long, Vec3> latestExplosion = explosiveBowExplosions.lastEntry();
                    if (latestExplosion == null) return;

                    Vec3 explosionLocation = latestExplosion.getValue();

//                    int possibleZealotsKilled = 1;
//                    System.out.println("This means "+possibleZealotsKilled+" may have been killed...");
//                    int originalPossibleZealotsKilled = possibleZealotsKilled;

                    Vec3 deathLocation = e.entity.getPositionVector();

//                    double distance = explosionLocation.distanceTo(deathLocation);
//                    System.out.println("Distance was "+distance+"!");
                    if (explosionLocation.distanceTo(deathLocation) < 4.6) {
//                        possibleZealotsKilled--;

                        main.getPersistentValuesManager().addKills();
                        EndstoneProtectorManager.onKill();
                    }

//                    System.out.println((originalPossibleZealotsKilled-possibleZealotsKilled)+" zealots were actually killed...");
                }
            }
        }

        NPCUtils.getNpcLocations().remove(e.entity.getUniqueID());
    }

    public boolean isZealot(Entity enderman) {
        List<EntityArmorStand> stands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityArmorStand.class,
                new AxisAlignedBB(enderman.posX - 1, enderman.posY, enderman.posZ - 1, enderman.posX + 1, enderman.posY + 5, enderman.posZ + 1));
        if (stands.isEmpty()) return false;

        EntityArmorStand armorStand = stands.get(0);
        return armorStand.hasCustomName() && armorStand.getCustomNameTag().contains("Zealot");
    }

    @SubscribeEvent()
    public void onEntitySpawn(EntityEvent.EnteringChunk e) {
        if (!main.getUtils().isOnSkyblock()) return;
        Entity entity = e.entity;

        if (Feature.ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT.isEnabled() && entity instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow) entity;

            EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
            ItemStack heldItem = p.getHeldItem();
            if (heldItem != null && "EXPLOSIVE_BOW".equals(ItemUtils.getSkyblockItemID(heldItem))) {

                AxisAlignedBB playerRadius = new AxisAlignedBB(p.posX - 3, p.posY - 3, p.posZ - 3, p.posX + 3, p.posY + 3, p.posZ + 3);
                if (playerRadius.isVecInside(arrow.getPositionVector())) {
//                    System.out.println("Spawned explosive arrow!");
                    main.getScheduler().scheduleTask(scheduledTask -> {
                        if (arrow.isDead || arrow.isCollided || arrow.inGround) {
                            scheduledTask.cancel();

//                            System.out.println("Arrow is done, added an explosion!");
                            Vec3 explosionLocation = new Vec3(arrow.posX, arrow.posY, arrow.posZ);
                            explosiveBowExplosions.put(System.currentTimeMillis(), explosionLocation);

                            recentlyKilledZealots.keySet().removeIf((killedTime) -> System.currentTimeMillis() - killedTime > 150);
                            Set<Vec3> filteredRecentlyKilledZealots = new HashSet<>();
                            for (Map.Entry<Long, Set<Vec3>> recentlyKilledZealotEntry : recentlyKilledZealots.entrySet()) {
                                filteredRecentlyKilledZealots.addAll(recentlyKilledZealotEntry.getValue());
                            }
                            if (filteredRecentlyKilledZealots.isEmpty()) return;

//                            int possibleZealotsKilled = filteredRecentlyKilledZealots.size();
//                            System.out.println("This means "+possibleZealotsKilled+" may have been killed...");
//                            int originalPossibleZealotsKilled = possibleZealotsKilled;

                            for (Vec3 zealotDeathLocation : filteredRecentlyKilledZealots) {
                                double distance = explosionLocation.distanceTo(zealotDeathLocation);
//                                System.out.println("Distance was "+distance+"!");
                                if (distance < 4.6) {
//                                        possibleZealotsKilled--;

                                    main.getPersistentValuesManager().addKills();
                                    EndstoneProtectorManager.onKill();
                                }
                            }
//                            System.out.println((originalPossibleZealotsKilled-possibleZealotsKilled)+" zealots were actually killed...");
                        }
                    }, 0, 1);
                }
            }
        }
    }

    @SubscribeEvent()
    public void onEnderTeleport(EnderTeleportEvent e) {
        if (main.getUtils().isOnSkyblock() && Feature.DISABLE_ENDERMAN_TELEPORTATION_EFFECT.isEnabled()) {
            e.setCanceled(true);
        }
    }

    /**
     * Modifies bottom of item tooltips and activates the copy item nbt feature
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onItemTooltipFirst(ItemTooltipEvent e) {
        if (e.toolTip == null || !main.getUtils().isOnSkyblock()) return;
        ItemStack hoveredItem = e.itemStack;

        int insertAt = e.toolTip.size();
        insertAt--; // 1 line for the rarity
        if (e.showAdvancedItemTooltips) {
            insertAt -= 2; // 1 line for the item name, and 1 line for the nbt
            if (e.itemStack.isItemDamaged()) {
                insertAt--; // 1 line for damage
            }
        }
        insertAt = Math.max(0, insertAt);

        NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(hoveredItem);
        if (extraAttributes != null) {
            if (Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE.isEnabled()
                    && extraAttributes.hasKey("baseStatBoostPercentage", ItemUtils.NBT_INTEGER)) {
                int baseStatBoost = extraAttributes.getInteger("baseStatBoostPercentage");

                ColorCode colorCode = main.getConfigValues().getRestrictedColor(Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE);
                if (Feature.BASE_STAT_BOOST_COLOR_BY_RARITY.isEnabled()) {
                    int rarityIndex = baseStatBoost / 10;
                    if (rarityIndex < 0) rarityIndex = 0;
                    if (rarityIndex >= Rarity.values().length) rarityIndex = Rarity.values().length - 1;

                    colorCode = Rarity.values()[rarityIndex].getColorCode();
                }
                e.toolTip.add(insertAt++, "§7Base Stat Boost: " + colorCode + "+" + baseStatBoost + "%");
            }

            if (Feature.SHOW_STACKING_ENCHANT_PROGRESS.isEnabled()) {
                insertAt = EnchantManager.insertStackingEnchantProgress(e.toolTip, extraAttributes, insertAt);
            }

            if (Feature.SHOW_SWORD_KILLS.isEnabled() && extraAttributes.hasKey("sword_kills", ItemUtils.NBT_INTEGER)) {
                ColorCode colorCode = main.getConfigValues().getRestrictedColor(Feature.SHOW_SWORD_KILLS);
                e.toolTip.add(insertAt++, "§7Sword Kills: " + colorCode + extraAttributes.getInteger("sword_kills"));
            }

            if (Feature.SHOW_ITEM_DUNGEON_FLOOR.isEnabled() && extraAttributes.hasKey("item_tier", ItemUtils.NBT_INTEGER)) {
                int floor = extraAttributes.getInteger("item_tier");
                ColorCode colorCode = main.getConfigValues().getRestrictedColor(Feature.SHOW_ITEM_DUNGEON_FLOOR);
                e.toolTip.add(insertAt, "§7Obtained on Floor: " + colorCode + (floor == 0 ? "Entrance" : floor));
            }
        }
    }

    /**
     * Modifies item enchantments on tooltips as well as roman numerals
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltipLast(ItemTooltipEvent e) {
        if (e.toolTip == null || !main.getUtils().isOnSkyblock()) return;
        ItemStack hoveredItem = e.itemStack;

        if (Feature.ENCHANTMENT_LORE_PARSING.isEnabled()) {
            EnchantManager.parseEnchants(e.toolTip, hoveredItem);
        }

        if (Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS.isEnabled()) {
            int startIndex = Feature.DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME.isEnabled() ? 1 : 0;

            for (int i = startIndex; i < e.toolTip.size(); i++) {
                e.toolTip.set(i, RomanNumeralParser.replaceNumeralsWithIntegers(e.toolTip.get(i)));
            }
        }

        if (Feature.SHOW_SKYBLOCK_ITEM_ID.isEnabled() || Feature.DEVELOPER_MODE.isEnabled()) {
            String itemId = ItemUtils.getSkyblockItemID(e.itemStack);
            String tooltipLine = EnumChatFormatting.DARK_GRAY + "skyblock:" + itemId;

            if (itemId != null) {
                if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
                    for (int i = e.toolTip.size(); i-- > 0; ) {
                        if (e.toolTip.get(i).startsWith(EnumChatFormatting.DARK_GRAY + "minecraft:")) {
                            e.toolTip.add(i + 1, tooltipLine);
                            break;
                        }
                    }

                } else {
                    e.toolTip.add(tooltipLine);
                }
            }
        }
    }

    /**
     * This method handles key presses while the player is in-game.
     * For handling of key presses while a GUI (e.g. chat, pause menu, F3) is open,
     * see {@link GuiScreenListener#onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre)}
     *
     * @param e the {@code KeyInputEvent} that occurred
     */
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent e) {
        if (main.getOpenSettingsKey().isPressed()) {
            main.getUtils().setFadingIn(true);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);

        } else if (main.getOpenEditLocationsKey().isPressed()) {
            main.getUtils().setFadingIn(false);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.EDIT_LOCATIONS, 0, null);

        } else if (Feature.DEVELOPER_MODE.isEnabled() && main.getDeveloperCopyNBTKey().isPressed()) {
            DevUtils.copyData();
        }

        if (Feature.areEnabled(Feature.DUNGEONS_MAP_DISPLAY, Feature.CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD)
                && main.getUtils().isInDungeon()) {
            if (Keyboard.isKeyDown(main.getKeyBindings().get(5).getKeyCode()) && Keyboard.getEventKeyState()) {
                DungeonMapManager.decreaseZoomByStep();
            } else if (Keyboard.isKeyDown(main.getKeyBindings().get(4).getKeyCode()) && Keyboard.getEventKeyState()) {
                DungeonMapManager.increaseZoomByStep();
            }
        }
    }

    /**
     * This method is called when a sound is played.
     *
     * @param event the event that caused this method to be called
     */
    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (!main.getUtils().isOnSkyblock()) return;

        // Ignore sounds that don't have a specific location like GUIs
        if (event.sound instanceof PositionedSoundRecord) {
            PositionedSoundRecord eventSound = (PositionedSoundRecord) event.sound;

            if (Feature.STOP_RAT_SOUNDS.isEnabled() && event.category == SoundCategory.ANIMALS) {
                for (PositionedSoundRecord sound : RAT_SOUNDS) {
                    // Check that the sound matches the rat sound
                    if (eventSound.getSoundLocation().equals(sound.getSoundLocation()) &&
                            eventSound.getPitch() == sound.getPitch() && eventSound.getVolume() == sound.getVolume()) {
                        if (Feature.STOP_ONLY_RAT_SQUEAK.isDisabled() ||
                                eventSound.getSoundLocation().toString().endsWith("mob.bat.idle")) {
                            // Cancel the result
                            event.result = null;
                        }
                    }
                }
            }

            if (Feature.BACKPACK_OPENING_SOUND.isEnabled() &&
                    System.currentTimeMillis() - main.getGuiScreenListener().getLastBackpackOpenMs() < 500) {
                if (event.name.equals("random.chestopen")) {
                    EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

                    // When a player opens a backpack, a chest open sound is played at the player's location.
                    if (DoubleMath.roundToInt(event.sound.getXPosF(), RoundingMode.HALF_UP) == thePlayer.getPosition().getX() &&
                            DoubleMath.roundToInt(event.sound.getYPosF(), RoundingMode.HALF_UP) == thePlayer.getPosition().getY() &&
                            DoubleMath.roundToInt(event.sound.getZPosF(), RoundingMode.HALF_UP) == thePlayer.getPosition().getZ()) {
                        event.result = null;
                    }
                }
            }
        }

        if (Feature.STOP_BONZO_STAFF_SOUNDS.isEnabled() && BONZO_STAFF_SOUNDS.contains(event.name)) {
            event.result = null;
        }
    }

    /**
     * This method is called when a player dies in Skyblock.
     *
     * @param e the event that caused this method to be called
     */
    @SubscribeEvent
    public void onPlayerDeath(SkyblockPlayerDeathEvent e) {
        EntityPlayerSP thisPlayer = Minecraft.getMinecraft().thePlayer;

        //  Resets all user input on death as to not walk backwards or strafe into the portal
        if (Feature.PREVENT_MOVEMENT_ON_DEATH.isEnabled() && e.entityPlayer == thisPlayer) {
            KeyBinding.unPressAllKeys();
        }

        /*
        Don't show log for losing all items when the player dies in dungeons.
         The items come back after the player is revived and the large log causes a distraction.
         */
        if (Feature.ITEM_PICKUP_LOG.isEnabled() && e.entityPlayer == thisPlayer && main.getUtils().isInDungeon()) {
            lastDeath = Minecraft.getSystemTime();
            main.getInventoryUtils().resetPreviousInventory();
        }

        if (Feature.DUNGEON_DEATH_COUNTER.isEnabled() && main.getUtils().isInDungeon()) {
            DungeonPlayer dungeonPlayer = main.getDungeonManager().getDungeonPlayerByName(e.username);
            if (dungeonPlayer != null) {
                // Hypixel sends another death message if the player disconnects. Don't count two deaths if the player
                // disconnects while dead.
                if (e.cause.contains("disconnected") && dungeonPlayer.isGhost()) {
                    return;
                }
                main.getDungeonManager().addDeath();

            } else if (e.entity == thisPlayer) { // TODO Keep track of a variable in the manager for the player's dungeon state
                // Hypixel sends another death message if the player disconnects. Don't count two deaths if the player
                // disconnects while dead. We can use flying state to check if player is a ghost.
                if (e.cause.contains("disconnected") && thisPlayer.capabilities.allowFlying) {
                    return;
                }
                main.getDungeonManager().addDeath();

            } else {
                logger.warn("Could not record death for {}. This dungeon player isn't in the registry.", e.username);
            }
        }
    }

    /**
     * This method is called when a player in Dungeons gets revived.
     *
     * @param e the event that caused this method to be called
     */
    @SubscribeEvent
    public void onDungeonPlayerRevive(DungeonPlayerReviveEvent e) {
        if (e.revivedPlayer == Minecraft.getMinecraft().thePlayer) {
            lastRevive = Minecraft.getSystemTime();
        }

        // Reset the previous inventory so the screen doesn't get spammed with a large pickup log
        if (Feature.ITEM_PICKUP_LOG.isEnabled()) {
            main.getInventoryUtils().resetPreviousInventory();
        }
    }

    @SubscribeEvent
    public void onBlockBreak(SkyblockBlockBreakEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        IBlockState blockState = mc.theWorld.getBlockState(e.blockPos);

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
                main.getUtils().sendMessage("§eMined ore: §f" + minedOre.name());
            }
        }
        if (Feature.SHOW_ITEM_COOLDOWNS.isEnabled()) {
            String itemId = ItemUtils.getSkyblockItemID(mc.thePlayer.getHeldItem());
            if (itemId == null) return;

            Block block = blockState.getBlock();
            if ((itemId.equals("JUNGLE_AXE") || itemId.equals("TREECAPITATOR_AXE")) && (block.equals(Blocks.log) || block.equals(Blocks.log2))) {
                long cooldownTime = CooldownManager.getItemCooldown(itemId);
                // Min cooldown time is 400 because anything lower than that can allow the player to hit a block
                // already marked for block removal by treecap/jungle axe ability
                PetManager.Pet pet = main.getPetCacheManager().getCurrentPet();
                if (pet != null
                        && pet.getPetInfo().getPetRarity() == Rarity.LEGENDARY
                        && pet.getPetInfo().getPetSkyblockId().equalsIgnoreCase("monkey")) {
                    cooldownTime -=  (int) (2000 * (0.005 * pet.getPetLevel()));
                }
                CooldownManager.put(itemId, Math.max(cooldownTime, 400));
            }
        }
    }

    public boolean aboutToJoinSkyblockServer() {
        return Minecraft.getSystemTime() - lastSkyblockServerJoinAttempt < 6000;
    }

    public boolean didntRecentlyJoinWorld() {
        return (Minecraft.getSystemTime() - lastWorldJoin) > 3000;
    }

    public int getMaxTickers() {
        return actionBarParser.getMaxTickers();
    }

    public int getTickers() {
        return actionBarParser.getTickers();
    }

    public void updateLastSecondHealth() {
        float health = PlayerStats.HEALTH.getValue();
        // Update the health gained/lost over the last second
        if (Feature.HEALTH_UPDATES.isEnabled() && actionBarParser.getLastSecondHealth() != health) {
            actionBarParser.setHealthUpdate(health - actionBarParser.getLastSecondHealth());
            actionBarParser.setLastHealthUpdate(System.currentTimeMillis());
        }
        actionBarParser.setLastSecondHealth(health);
    }

    public boolean shouldResetMouse() {
        return System.currentTimeMillis() - main.getGuiScreenListener().getLastContainerCloseMs() > 100;
    }

    /**
     * Checks if the fishing indicator sound should be played. To play the sound, these conditions have to be met:
     * <p>1. Fishing sound indicator feature is enabled</p>
     * <p>2. The player is on skyblock (checked in {@link #onTick(TickEvent.ClientTickEvent)})</p>
     * <p>3. The player is holding a fishing rod</p>
     * <p>4. The fishing rod is in the water</p>
     * <p>5. The bobber suddenly moves downwards, indicating a fish has been caught</p>
     *
     * @return {@code true} if the fishing alert sound should be played, {@code false} otherwise
     * @see Feature#FISHING_SOUND_INDICATOR
     */
    private boolean shouldTriggerFishingIndicator(Minecraft mc) {
        if (Feature.FISHING_SOUND_INDICATOR.isEnabled() && mc.thePlayer.fishEntity != null && isHoldingRod()) {
            // Highly consistent detection by checking when the hook has been in the water for a while and
            // suddenly moves downward. The client may rarely bug out with the idle bobbing and trigger a false positive.
            EntityFishHook bobber = mc.thePlayer.fishEntity;
            long currentTime = System.currentTimeMillis();
            if (bobber.isInWater() && !oldBobberIsInWater) lastBobberEnteredWater = currentTime;
            oldBobberIsInWater = bobber.isInWater();
            if (bobber.isInWater() && Math.abs(bobber.motionX) < 0.01 && Math.abs(bobber.motionZ) < 0.01
                    && currentTime - lastFishingAlert > 1000 && currentTime - lastBobberEnteredWater > 1500) {
                double movement = bobber.posY - oldBobberPosY; // The Entity#motionY field is inaccurate for this purpose
                oldBobberPosY = bobber.posY;
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
     * that are {@link Items#fishing_rod}s but aren't used for fishing. This is done by checking for the item type of
     * "FISHING ROD" which is displayed beside the item rarity.
     *
     * @return {@code true} if the player is holding a fishing rod that can be used for fishing, {@code false} otherwise
     */
    public boolean isHoldingRod() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        if (player != null) {
            ItemStack item = player.getHeldItem();
            if (item == null || item.getItem() != Items.fishing_rod) return false;

            return ItemUtils.getItemType(item) == ItemType.FISHING_ROD;
        }
        return false;
    }

    public boolean isHoldingFireFreeze() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        if (player != null) {
            ItemStack item = player.getHeldItem();
            return item != null && item.hasDisplayName() && item.getDisplayName().contains("Fire Freeze Staff");
        }
        return false;
    }

    /**
     * Updates health/ max health attributes
     */
    private void updateHealthAttributes(Minecraft mc) {
        EntityPlayerSP p = mc.thePlayer;
        if (p != null) {
            if (main.getUtils().isOnRift()) {
                if (Feature.HEALTH_BAR.isEnabled() || Feature.HEALTH_TEXT.isEnabled()) {
                    PlayerStats.MAX_RIFT_HEALTH.setValue(p.getMaxHealth());
                    PlayerStats.HEALTH.setValue(p.getHealth());
                }
            } else {
                // Reverse calculate the player's health by using the player's vanilla hearts.
                // Also calculate the health change for the gui item.
                if (Feature.HEALTH_PREDICTION.isEnabled()) {
                    float newHealth = PlayerStats.HEALTH.getValue() > PlayerStats.MAX_HEALTH.getValue()
                            ? PlayerStats.HEALTH.getValue()
                            : Math.round(PlayerStats.MAX_HEALTH.getValue() * ((p.getHealth()) / p.getMaxHealth()));
                    PlayerStats.HEALTH.setValue(newHealth);
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
            if (mc.currentScreen instanceof GuiChest && (skill == SkillType.MINING || skill == SkillType.FISHING)) {
                GuiChest chest = (GuiChest) mc.currentScreen;
                ContainerChest container = (ContainerChest) chest.inventorySlots;
                IInventory lower = container.getLowerChestInventory();

                ItemStack milestoneItem = lower.getStackInSlot(51);
                // The player may persistently try to get the item :)
                if (milestoneItem == null) return;

                List<String> lore = ItemUtils.getItemLore(milestoneItem);
                // No milestone items in new profiles
                if (lore.isEmpty()) return;

                String milestoneProgress = TextUtils.stripColor(lore.get(lore.size() - 1));

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

}
