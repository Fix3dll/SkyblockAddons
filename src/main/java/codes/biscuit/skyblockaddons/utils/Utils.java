package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Attribute;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.core.SkyblockDate;
import codes.biscuit.skyblockaddons.events.SkyblockJoinedEvent;
import codes.biscuit.skyblockaddons.events.SkyblockLeftEvent;
import codes.biscuit.skyblockaddons.features.itemdrops.ItemDropChecker;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.io.*;
import java.nio.FloatBuffer;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class Utils {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Logger logger = SkyblockAddons.getLogger();

    /**
     * Added to the beginning of messages sent by the mod.
     */
    public static final String MESSAGE_PREFIX =
            ColorCode.GRAY + "[" + ColorCode.AQUA + SkyblockAddons.MOD_NAME + ColorCode.GRAY + "] ";
    public static final String MESSAGE_PREFIX_SHORT =
            ColorCode.GRAY + "[" + ColorCode.AQUA + "SBA" + ColorCode.GRAY + "] " + ColorCode.RESET;

    /**
     * "Skyblock" as shown on the scoreboard title in English, Chinese Simplified, Traditional Chinese.
     */
    private static final Set<String> SKYBLOCK_IN_ALL_LANGUAGES = Sets.newHashSet("SKYBLOCK", "\u7A7A\u5C9B\u751F\u5B58", "\u7A7A\u5CF6\u751F\u5B58");

    /**
     * Matches the server ID (m##/M##) line on the Skyblock scoreboard
     */
    // TODO dungeon room coordinates can be used
    private static final Pattern SERVER_REGEX = Pattern.compile("^\\d+/\\d+/\\d+ (?<serverType>[Mm])(?<serverCode>[0-9]+[A-Z]+) ?(?:(?<x>-?\\d+),(?<z>-?\\d+))?$");
    /**
     * Matches the active slayer quest type line on the Skyblock scoreboard
     */
    private static final Pattern SLAYER_TYPE_REGEX = Pattern.compile("(?<type>Tarantula Broodfather|Revenant Horror|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend) (?<level>[IV]+)");
    /**
     * Matches the active slayer quest progress line on the Skyblock scoreboard
     */
    private static final Pattern SLAYER_PROGRESS_REGEX = Pattern.compile("(?<progress>[0-9.k]*)/(?<total>[0-9.k]*) (?:Kills|Combat XP)$");
    /**
     * Matches the date line on the Skyblock scoreboard
     */
    private static final Pattern DATE_PATTERN = Pattern.compile("(?<month>[\\w ]+) (?<day>\\d{1,2})(?:th|st|nd|rd)");
    /**
     * Matches the time line on the Skyblock scoreboard
     */
    private static final Pattern TIME_PATTERN = Pattern.compile("(?<hour>\\d{1,2}):(?<minute>\\d{2})(?<period>am|pm)");

    /**
     * A dummy world object used for spawning fake entities for GUI features without affecting the actual world
     */
    private static final WorldClient DUMMY_WORLD = new WorldClient(
            null,
            new WorldSettings(0L, WorldSettings.GameType.SURVIVAL, false, false, WorldType.DEFAULT),
            0,
            null,
            null
    );

    /**
     * Used for web requests.
     */
    public static final String USER_AGENT = "SkyblockAddons/" + SkyblockAddons.VERSION;

    // I know this is messy af, but frustration led me to take this dark path - said someone not biscuit
    public static boolean blockNextClick;

    /**
     * Get a player's attributes. This includes health, mana, and defence.
     */
    private Map<Attribute, MutableFloat> attributes = new EnumMap<>(Attribute.class);

    /**
     * This is the item checker that makes sure items being dropped or sold are allowed to be dropped or sold.
     */
    private final ItemDropChecker itemDropChecker = new ItemDropChecker();

    /**
     * List of reforges that the player is looking to find.
     */
    private List<String> reforgeMatches = new LinkedList<>();

    /**
     * List of reforge substrings that the player doesn't want to match.
     */
    private List<String> reforgeExclusions = new LinkedList<>();

    /**
     * Whether the player is on skyblock.
     */
    private boolean onSkyblock = false;

    /**
     * Whether the player is on rift dimension.
     */
    private boolean onRift = false;

    /**
     * Whether the player is doing Trapper quest
     */
    private boolean isTrackingAnimal = false;

    /**
     * The player's current location in SkyBlock
     */
    private Location location = Location.UNKNOWN;

    /**
     * The player's current map in SkyBlock
     */
    private String map = "null";

    /**
     * The player's current mode in SkyBlock
     */
    private String mode = "null";

    /**
     * The current mayor name
     * <br>
     * <i>We don't leave it blank in case the Mayor is not found</i>
     */
    private String mayor = "Fix3dll";

    /**
     * The current Jerry's Perkpocalypse mayor
     * <br>
     * <i>We don't leave it blank in case the Mayor is not found</i>
     */
    private String jerryMayor = "Fix3dll";

    /**
     * The current Jerry's Perkpocalypse mayor update timestamp
     */
    private long jerryMayorUpdateTime = 0L;

    /**
     * Dungeon floor information from the scoreboard
     */
    private String dungeonFloor = "";

    /**
     * Plot name from the scoreboard
     */
    private String plotName = "";

    /**
     * The skyblock profile that the player is currently on. Ex. "Grapefruit"
     */
    private String profileName = "Unknown";

    /**
     * Whether a loud sound is being played by the mod.
     */
    private boolean playingSound;

    /**
     * The current serverID that the player is on.
     */
    private String serverID = "";
    private int lastHoveredSlot = -1;

    /**
     * Whether the player is using the FSR container preview
     */
    private boolean usingFSRcontainerPreviewTexture = false;

    private SkyblockDate currentDate = new SkyblockDate(SkyblockDate.SkyblockMonth.EARLY_WINTER, 1, 1, 1, "am");
    private double purse = 0.0;
    private double bits = 0.0;
    private double motes = 0.0;
    private int jerryWave = -1;

    private boolean alpha;
    private boolean inDungeon;

    private boolean fadingIn;

    private EnumUtils.SlayerQuest slayerQuest;
    private int slayerQuestLevel = 1;
    private boolean slayerBossAlive;

    public Utils() {
        addDefaultStats();
    }

    private void addDefaultStats() {
        for (Attribute attribute : Attribute.values()) {
            attributes.put(attribute, new MutableFloat(attribute.getDefaultValue()));
        }
    }

    public void sendMessage(String text, boolean prefix) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte) 1, new ChatComponentText((prefix ? MESSAGE_PREFIX : "") + text));
        MinecraftForge.EVENT_BUS.post(event); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message); // Just for logs
        }
    }

    public void sendMessage(String text) {
        sendMessage(text, true);
    }

    public void sendMessage(ChatComponentText text, boolean prefix) {
        if (prefix) { // Add the prefix in front.
            ChatComponentText newText = new ChatComponentText(MESSAGE_PREFIX);
            newText.appendSibling(text);
            text = newText;
        }

        ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte) 1, text);
        MinecraftForge.EVENT_BUS.post(event); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message); // Just for logs
        }
    }

    public void sendErrorMessage(String errorText) {
        sendMessage(ColorCode.RED + "Error: " + errorText);
    }

    /**
     * Checks if the player is on the Hypixel Network
     *
     * @return {@code true} if the player is on Hypixel, {@code false} otherwise
     */
    public boolean isOnHypixel() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return false;
        }
        String brand = player.getClientBrand();
        if (brand != null) {
            for (Pattern p : main.getOnlineData().getHypixelBrands()) {
                if (p.matcher(brand).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * In some special cases (ex. Jacob Contest on the Garden) scoreboard lines are compressed
     *  to provide space. Replaced the old "switch-case" structure with "if-else" to provide
     *  more flexibility and readability.
     */
    public void parseSidebar() {
        boolean foundScoreboard = false;
        boolean foundSkyblockTitle = false;

        boolean foundServerID = false;
        boolean foundDate = false;
        boolean foundLocation = false;
        boolean foundTime = false;
        boolean foundCoins = false;
        boolean foundBits = false;

        boolean foundTrackingAnimal = false;
        boolean foundJerryWave = false;
        boolean foundAlphaIP = false;
        boolean foundInDungeon = false;
        boolean foundSlayerQuest = false;
        boolean foundBossAlive = false;

        // TODO: This can be optimized more.
        if (isOnHypixel() && ScoreboardManager.hasScoreboard()) {
            foundScoreboard = true;

            // Check title for skyblock
            String strippedScoreboardTitle = ScoreboardManager.getStrippedScoreboardTitle();
            for (String skyblock : SKYBLOCK_IN_ALL_LANGUAGES) {
                if (strippedScoreboardTitle.startsWith(skyblock)) {
                    foundSkyblockTitle = true;
                    break;
                }
            }

            if (foundSkyblockTitle) {
                // If it's a Skyblock scoreboard and the player has not joined Skyblock yet,
                // this indicates that they did so.
                if (!this.isOnSkyblock()) {
                    MinecraftForge.EVENT_BUS.post(new SkyblockJoinedEvent());
                }

                Matcher dateMatcher = null;

                for (int lineNumber = 0; lineNumber < ScoreboardManager.getNumberOfLines(); lineNumber++) {
                    String scoreboardLine = ScoreboardManager.getScoreboardLines().get(lineNumber);
                    String strippedScoreboardLine = ScoreboardManager.getStrippedScoreboardLines().get(lineNumber);

                    // Don't waste resources with empty strings
                    if (strippedScoreboardLine.isEmpty())
                        continue;

                    // No need to try to find serverID after line 0
                    if (!foundServerID && lineNumber == 0) {
                        Matcher matcher = SERVER_REGEX.matcher(strippedScoreboardLine);

                        if (matcher.find()) {
                            String serverType = matcher.group("serverType");
                            if (serverType.equals("m")) {
                                serverID = "mini" + matcher.group("serverCode");
                            } else if (serverType.equals("M")) {
                                serverID = "mega" + matcher.group("serverCode");
                            }
                            foundServerID = true;
                            continue;
                        }
                    }

                    // No need to try to find date after line 2
                    if (!foundDate && lineNumber < 3) {
                        Matcher dateM = DATE_PATTERN.matcher(strippedScoreboardLine);
                        if (dateM.find()) {
                            dateMatcher = dateM;
                            foundDate = true;
                            continue;
                        }
                    }

                    // No need to try to find date after line 3
                    if (foundDate && !foundTime && lineNumber < 4) {
                        Matcher timeM = TIME_PATTERN.matcher(strippedScoreboardLine);
                        if (timeM.find()) {
                            currentDate = SkyblockDate.parse(dateMatcher, timeM);
                            foundTime = true;
                            continue;
                        } else {
                            currentDate = SkyblockDate.parse(dateMatcher);
                        }
                    }

                    // No need to try to find location after line 4
                    if (!foundLocation && lineNumber < 5) {
                        if (strippedScoreboardLine.contains("\u23E3")) {
                            onRift = false;

                            // If the title line ends with "GUEST", then the player is visiting someone else's island.
                            if (strippedScoreboardTitle.endsWith("GUEST")) {
                                location = Location.GUEST_ISLAND;
                                if (!strippedScoreboardLine.contains("Plot"))
                                    location.setScoreboardName(
                                            strippedScoreboardLine.substring(strippedScoreboardLine.indexOf(' ') + 1)
                                    );
                                foundLocation = true;

                            } else {
                                for (Location loopLocation : Location.values()) {
                                    String scoreboardName = loopLocation.getScoreboardName();
                                    if (!strippedScoreboardLine.contains(scoreboardName))
                                        continue;

                                    // Special case causes Dwarven Village to map to Village
                                    if ((loopLocation == Location.VILLAGE || loopLocation == Location.TAVERN)
                                            && strippedScoreboardLine.contains("Dwarven")) {
                                        continue;
                                    } else if (loopLocation == Location.JERRY_POND
                                            && strippedScoreboardLine.contains("Sunken")) {
                                        continue;
                                    } else if (loopLocation == Location.MOUNTAIN
                                            && strippedScoreboardLine.contains("Desert")) {
                                        continue;
                                    } else if (loopLocation == Location.KUUDRAS_HOLLOW || loopLocation == Location.THE_CATACOMBS) {
                                        // Catacombs and Kuudra contains the floor number, so it's a special case...
                                        dungeonFloor = strippedScoreboardLine.substring(strippedScoreboardLine.lastIndexOf(" "));
                                    } else if (loopLocation == Location.GARDEN_PLOT) {
                                        plotName = strippedScoreboardLine.substring(strippedScoreboardLine.indexOf("-") + 1);
                                    }
                                    location = loopLocation;
                                    foundLocation = true;
                                    break;
                                }
                            }
                        } else if (strippedScoreboardLine.contains("\u0444")) {
                            onRift = true;
                            for (Location loopLocation : LocationUtils.getRiftLocations()) {
                                if (strippedScoreboardLine.contains(loopLocation.getScoreboardName())) {
                                    location = loopLocation;
                                    foundLocation = true;
                                    break;
                                }
                            }
                        }
                        if (foundLocation) continue;
                    }

                    // No need to try to find purse after line 8
                    if (!foundCoins && lineNumber < 9) {
                        if (!onRift && (strippedScoreboardLine.startsWith("Piggy:") || strippedScoreboardLine.contains("Purse:"))) {
                            String purseStr = strippedScoreboardLine.substring(strippedScoreboardLine.indexOf(' ') + 1);
                            try {
                                purse = TextUtils.NUMBER_FORMAT.parse(purseStr).doubleValue();
                            } catch (ParseException ex) {
                                //logger.error("Failed to parse purse (" + ex.getMessage() + ")", ex);
                                purse = 0.0;
                            }
                            foundCoins = true;
                            continue;
                        } else if (onRift && strippedScoreboardLine.startsWith("Motes:")) {
                            String motesStr = strippedScoreboardLine.substring(strippedScoreboardLine.indexOf(' ') + 1);
                            try {
                                motes = TextUtils.NUMBER_FORMAT.parse(motesStr).doubleValue();
                            } catch (ParseException ex) {
                                //logger.error("Failed to parse purse (" + ex.getMessage() + ")", ex);
                                motes = 0.0;
                            }
                            foundCoins = true;
                            continue;
                        }
                    }

                    // No need to try to find bits after line 9
                    if (!onRift && !foundBits && lineNumber < 10) {
                        if (strippedScoreboardLine.startsWith("Bits:")) {
                            String bitsStr = strippedScoreboardLine.substring(strippedScoreboardLine.indexOf(' ') + 1);
                            try {
                                bits = TextUtils.NUMBER_FORMAT.parse(bitsStr).doubleValue();
                            } catch (ParseException ex) {
                                //logger.error("Failed to parse bits (" + ex.getMessage() + ")", ex);
                                bits = 0.0;
                            }
                            foundBits = true;
                            continue;
                        }
                    }

                    // Tracker Mob Location line comes after coins always
                    if (!onRift && foundCoins && !foundTrackingAnimal) {
                        if (strippedScoreboardLine.equals("Tracker Mob Location:")) {
                            isTrackingAnimal = true;
                            foundTrackingAnimal = true;
                            continue;
                        }
                    }

                    // Lines after old switch-case
                    if (strippedScoreboardLine.endsWith("Combat XP") || strippedScoreboardLine.endsWith("Kills")) {
                        parseSlayerProgress(strippedScoreboardLine);
                        continue;
                    }

                    if (!onRift && !foundJerryWave && LocationUtils.isInWinterIsland(location)) {
                        if (strippedScoreboardLine.startsWith("Wave")) {
                            foundJerryWave = true;

                            int newJerryWave;
                            try {
                                newJerryWave = Integer.parseInt(TextUtils.keepIntegerCharactersOnly(strippedScoreboardLine));
                            } catch (NumberFormatException ignored) {
                                newJerryWave = 0;
                            }
                            if (jerryWave != newJerryWave) {
                                jerryWave = newJerryWave;
                            }

                            continue;
                        }
                    }

                    if (!onRift && !foundInDungeon && strippedScoreboardLine.startsWith("Cleared: ")) {
                        foundInDungeon = true;
                        inDungeon = true;

                        String lastServer = main.getDungeonManager().getLastServerId();
                        if (lastServer != null && !lastServer.equals(serverID)) {
                            main.getDungeonManager().reset();
                        }
                        main.getDungeonManager().setLastServerId(serverID);
                        continue;
                    }

                    if (!foundSlayerQuest) {
                        Matcher slayerMatcher = SLAYER_TYPE_REGEX.matcher(strippedScoreboardLine);
                        if (slayerMatcher.matches()) {
                            String type = slayerMatcher.group("type");
                            String levelRomanNumeral = slayerMatcher.group("level");

                            EnumUtils.SlayerQuest detectedSlayerQuest = EnumUtils.SlayerQuest.fromName(type);
                            if (detectedSlayerQuest != null) {
                                try {
                                    int level = RomanNumeralParser.parseNumeral(levelRomanNumeral);
                                    slayerQuest = detectedSlayerQuest;
                                    slayerQuestLevel = level;
                                    foundSlayerQuest = true;
                                    continue;

                                } catch (IllegalArgumentException ex) {
                                    logger.error("Failed to parse slayer level (" + ex.getMessage() + ")", ex);
                                }
                            }
                        }
                    }

                    if (strippedScoreboardLine.equals("Slay the boss!")) {
                        foundBossAlive = true;
                        slayerBossAlive = true;
                        continue;
                    }

                    if (inDungeon) {
                        try {
                            main.getDungeonManager().updateDungeonPlayer(scoreboardLine);
                        } catch (NumberFormatException ex) {
                            logger.error("Failed to update a dungeon player from the line " + scoreboardLine + ".", ex);
                        }
                    }

                    // Check if the player is on the Hypixel Alpha Network
                    if (lineNumber == ScoreboardManager.getNumberOfLines() - 1 && !foundAlphaIP && strippedScoreboardLine.contains("alpha.hypixel.net")) {
                        foundAlphaIP = true;
                        alpha = true;
                        profileName = "Alpha";
                    }

                }
            }
            if (!foundTrackingAnimal) {
                isTrackingAnimal = false;
            }
            if (!foundLocation) {
                location = Location.UNKNOWN;
            }
            if (!foundJerryWave) {
                jerryWave = -1;
            }
            if (!foundAlphaIP) {
                alpha = false;
            }
            if (!foundInDungeon) {
                inDungeon = false;
            }
            if (!foundSlayerQuest) {
                slayerQuestLevel = 1;
                slayerQuest = null;
            }
            if (!foundBossAlive) {
                slayerBossAlive = false;
            }
        }

        // If it's not a Skyblock scoreboard, the player must have left Skyblock and
        // be in some other Hypixel lobby or game.
        if (!foundSkyblockTitle && this.isOnSkyblock()) {

            // Check if we found a scoreboard in general. If not, its possible they are switching worlds.
            // If we don't find a scoreboard for 10s, then we know they actually left the server.
            if (foundScoreboard || System.currentTimeMillis() - ScoreboardManager.getLastFoundScoreboard() > 10000) {
                MinecraftForge.EVENT_BUS.post(new SkyblockLeftEvent());
            }
        }
    }

    private boolean triggeredSlayerWarning = false;
    private float lastCompletion;

    private void parseSlayerProgress(String line) {
        if (!main.getConfigValues().isEnabled(Feature.BOSS_APPROACH_ALERT)) return;

        Matcher matcher = SLAYER_PROGRESS_REGEX.matcher(line);
        if (matcher.find()) {
            String progressString = matcher.group("progress");
            String totalString = matcher.group("total");

            float progress = Float.parseFloat(TextUtils.keepFloatCharactersOnly(progressString));
            float total = Float.parseFloat(TextUtils.keepFloatCharactersOnly(totalString));

            if (progressString.contains("k")) progress *= 1000;
            if (totalString.contains("k")) total *= 1000;

            float completion = progress / total;

            if (completion > 0.85) {
                if (!triggeredSlayerWarning || (main.getConfigValues().isEnabled(Feature.REPEAT_SLAYER_BOSS_WARNING) && completion != lastCompletion)) {
                    triggeredSlayerWarning = true;
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setTitleFeature(Feature.BOSS_APPROACH_ALERT);
                    main.getScheduler().schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                }
            } else {
                triggeredSlayerWarning = false; // Reset warning flag when completion is below 85%, meaning they started a new quest.
            }

            lastCompletion = completion;
        }
    }

    public int getDefaultColor(float alphaFloat) {
        int alpha = (int) alphaFloat;
        return new Color(150, 236, 255, alpha).getRGB();
    }

    /**
     * When you use this function, any sound played will bypass the player's
     * volume setting, so make sure to only use this for like warnings or stuff like that.
     */
    public void playLoudSound(String sound, double pitch) {
        playingSound = true;
        Minecraft.getMinecraft().thePlayer.playSound(sound, 1, (float) pitch);
        playingSound = false;
    }

    /**
     * This one plays the sound normally. See {@link Utils#playLoudSound(String, double)} for playing
     * a sound that bypasses the user's volume settings.
     */
    public void playSound(String sound, double pitch) {
        Minecraft.getMinecraft().thePlayer.playSound(sound, 1, (float) pitch);
    }

    public void playSound(String sound, double volume, double pitch) {
        Minecraft.getMinecraft().thePlayer.playSound(sound, (float) volume, (float) pitch);
    }

    /**
     * Checks if the given reforge is similar to any reforges on the desired/exclusions lists from the reforge filter feature.
     *
     * @param reforge the reforge to check
     * @return {@code true} if the given reforge is similar to a desired reforge and dissimilar to all excluded reforges,
     * {@code false} otherwise
     */
    public boolean enchantReforgeMatches(String reforge) {
        reforge = reforge.trim().toLowerCase(Locale.US);
        for (String desiredReforge : reforgeMatches) {
            desiredReforge = desiredReforge.trim().toLowerCase(Locale.US);
            if (StringUtils.isNotEmpty(desiredReforge) && reforge.contains(desiredReforge)) {
                boolean foundExclusion = false;
                for (String excludedReforge : reforgeExclusions) {
                    excludedReforge = excludedReforge.trim().toLowerCase(Locale.US);
                    if (StringUtils.isNotEmpty(excludedReforge) && reforge.contains(excludedReforge)) {
                        foundExclusion = true;
                        break;
                    }
                }
                if (!foundExclusion) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the folder that SkyblockAddons is located in.
     *
     * @return the folder the SkyblockAddons jar is located in
     */
    public File getSBAFolder() {
        return Loader.instance().activeModContainer().getSource().getParentFile();
    }

    /**
     * Checks if it is currently Halloween according to the system calendar.
     *
     * @return {@code true} if it is Halloween, {@code false} otherwise
     */
    public boolean isHalloween() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == Calendar.OCTOBER && calendar.get(Calendar.DAY_OF_MONTH) == 31;
    }

    public int getDefaultBlue(int alpha) {
        return new Color(160, 225, 229, alpha).getRGB();
    }

    public float normalizeValueNoStep(float value, float min, float max) {
        return MathHelper.clamp_float((snapNearDefaultValue(value) - min) / (max - min), 0.0F, 1.0F);
    }

    /**
     * Rounds the given value to 1f if it is between 0.95f and 1.05f exclusive.
     *
     * @param value the value to round
     * @return 1f if 0.95f > {@code value} > 1.05f or {@code value} otherwise
     */
    public float snapNearDefaultValue(float value) {
        if (value != 1 && value > 1 - 0.05 && value < 1 + 0.05) {
            return 1;
        }

        return value;
    }

    /**
     * Rounds a float value for when it is being displayed as a string.
     * <p>
     * For example, if the given value is 123.456789 and the decimal places is 2, this will round
     * to 1.23.
     *
     * @param value         The value to round
     * @param decimalPlaces The decimal places to round to
     * @return A string representation of the value rounded
     */
    public static String roundForString(float value, int decimalPlaces) {
        return String.format("%." + decimalPlaces + "f", value);
    }

    public String[] wrapSplitText(String text, int wrapLength) {
        return WordUtils.wrap(text, wrapLength).replace("\r", "").split(Pattern.quote("\n"));
    }

    public boolean itemIsInHotbar(ItemStack itemStack) {
        ItemStack[] inventory = Minecraft.getMinecraft().thePlayer.inventory.mainInventory;

        for (int slot = 0; slot < 9; slot++) {
            if (inventory[slot] == itemStack) {
                return true;
            }
        }
        return false;
    }

    public boolean isAxe(Item item) {
        return item instanceof ItemAxe;
    }

    private boolean depthEnabled;
    private boolean blendEnabled;
    private boolean alphaEnabled;
    private int blendFunctionSrcFactor;
    private int blendFunctionDstFactor;

    public void enableStandardGLOptions() {
        depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        alphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        blendFunctionSrcFactor = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        blendFunctionDstFactor = GL11.glGetInteger(GL11.GL_BLEND_DST);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.color(1, 1, 1, 1);
    }

    public void restoreGLOptions() {
        if (depthEnabled) {
            GlStateManager.enableDepth();
        }
        if (!alphaEnabled) {
            GlStateManager.disableAlpha();
        }
        if (!blendEnabled) {
            GlStateManager.disableBlend();
        }
        GlStateManager.blendFunc(blendFunctionSrcFactor, blendFunctionDstFactor);
    }

    public boolean isModLoaded(String modId) {
        return isModLoaded(modId, null);
    }

    /**
     * Check if another mod is loaded.
     *
     * @param modId   The modid to check.
     * @param version The version of the mod to match (optional).
     */
    public boolean isModLoaded(String modId, String version) {
        boolean isLoaded = Loader.isModLoaded(modId); // Check for the modid...

        if (isLoaded && version != null) { // Check for the specific version...
            for (ModContainer modContainer : Loader.instance().getModList()) {
                if (modContainer.getModId().equals(modId) && modContainer.getVersion().equals(version)) {
                    return true;
                }
            }

            return false;
        }

        return isLoaded;
    }

    public static WorldClient getDummyWorld() {
        return DUMMY_WORLD;
    }

    public float[] getCurrentGLTransformations() {
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf);
        buf.rewind();
        Matrix4f mat = new Matrix4f();
        mat.load(buf);

        float x = mat.m30;
        float y = mat.m31;
        float z = mat.m32;

        float scale = (float) Math.sqrt(mat.m00 * mat.m00 + mat.m01 * mat.m01 + mat.m02 * mat.m02);

        return new float[]{x, y, z, scale};
    }

    public static EntityPlayer getPlayerFromName(String name) {
        return Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);
    }

    public boolean isEmptyGlassPane(ItemStack itemStack) {
        return itemStack != null && (itemStack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)
                || itemStack.getItem() == Item.getItemFromBlock(Blocks.glass_pane)) && itemStack.hasDisplayName() && TextUtils.stripColor(itemStack.getDisplayName().trim()).isEmpty();
    }

    public boolean isGlassPaneColor(ItemStack itemStack, EnumDyeColor color) {
        return itemStack != null && itemStack.getMetadata() == color.getMetadata();
    }

    public static float getPartialTicks() {
        return Minecraft.getMinecraft().timer.renderPartialTicks;
    }

    public static long getCurrentTick() {
        return SkyblockAddons.getInstance().getNewScheduler().getTotalTicks();
    }

    private static final Vector3d interpolatedPlayerPosition = new Vector3d();
    private static long lastTick;
    private static float lastPartialTicks;

    public static Vector3d getPlayerViewPosition() {
        long currentTick = getCurrentTick();
        float currentPartialTicks = getPartialTicks();

        if (currentTick != lastTick || currentPartialTicks != lastPartialTicks) {
            Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
            interpolatedPlayerPosition.x = MathUtils.interpolateX(renderViewEntity, currentPartialTicks);
            interpolatedPlayerPosition.y = MathUtils.interpolateY(renderViewEntity, currentPartialTicks);
            interpolatedPlayerPosition.z = MathUtils.interpolateZ(renderViewEntity, currentPartialTicks);

            lastTick = currentTick;
            lastPartialTicks = currentPartialTicks;
        }

        return interpolatedPlayerPosition;
    }

    public static byte[] toByteArray(BufferedInputStream inputStream) throws IOException {
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(inputStream);
        } finally {
            inputStream.close();
        }
        return bytes;
    }

    public static Entity getEntityByUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        for (Entity entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid)) {
                return entity;
            }
        }

        return null;
    }

    public static int getBlockMetaId(Block block, int meta) {
        return Block.getStateId(block.getStateFromMeta(meta));
    }
}
