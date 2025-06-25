package com.fix3dll.skyblockaddons.utils;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.SkyblockDate;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.events.SkyblockEvents;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.io.*;
import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class Utils {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    /**
     * Added to the beginning of messages sent by the mod.
     */
    public static final String MESSAGE_PREFIX =
            ColorCode.GRAY + "[" + ColorCode.AQUA + SkyblockAddons.METADATA.getName() + ColorCode.GRAY + "] ";
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
    private static final Pattern SERVER_REGEX = Pattern.compile("^\\d+/\\d+/\\d+ (?<serverType>[Mm])(?<serverCode>[0-9]+[A-Z]+) ?(?:(?<x>-?\\d+),(?<z>-?\\d+))?(?<mineshaft>[A-Z]+\\d)?$");
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
     * Matches the pests on the Skyblock scoreboard
     */
    private static final Pattern PEST_PATTERN = Pattern.compile(" ൠ x\\d");

    /**
     * A dummy world object used for spawning fake entities for GUI features without affecting the actual world
     */
    private static ClientLevel DUMMY_WORLD;

    /**
     * Used for web requests.
     */
    public static final String USER_AGENT = "SkyblockAddons/" + SkyblockAddons.METADATA.getVersion();

    // I know this is messy af, but frustration led me to take this dark path - said someone not biscuit
    public static boolean blockNextClick;

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
    private String location = "Unknown";

    /**
     * The player's current map in SkyBlock
     * @implNote Use {@link LocationUtils} instead of direct calls unless there is a special case
     */
    private Island map = Island.UNKNOWN;

    /**
     * The player's currently visiting someone's island in SkyBlock
     */
    private boolean isGuest = false;

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
     * The current minister name
     * <br>
     * <i>We don't leave it blank in case the Minister is not found</i>
     */
    private Pair<String, String> ministerAndPerk = new Pair<>("Fix3dll", "Spaghetti code");

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
    private String dungeonFloor = "0";

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
    private boolean playingLoudSound;

    /**
     * The current serverID that the player is on.
     */
    private String serverID = "";

    /**
     * The current mineshaft the player is in, taken from the Scoreboard.
     */
    private String mineshaftID = "";

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
    }

    public static void sendMessage(Component text, boolean prefix) {
        Component message = prefix ? Component.literal(MESSAGE_PREFIX).append(text) : text;
        boolean eventCanceled = !ClientReceiveMessageEvents.ALLOW_GAME.invoker().allowReceiveGameMessage(message, false);
        if (!eventCanceled) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(message, false); // Just for logs
            }
        }
    }

    public static void sendMessage(String string, boolean prefix) {
        sendMessage(Component.literal(string), prefix);
    }

    public static void sendMessage(String string) {
        sendMessage(string, true);
    }

    public static void sendMessage(Component text) {
        sendMessage(text, true);
    }

    public static void sendMessageOrElseLog(String message, Logger logger, boolean isError) {
        if (Minecraft.getInstance().player != null) {
            if (isError) {
                sendErrorMessage(message);
            } else {
                sendMessage(message);
            }
        } else {
            if (isError) {
                logger.error(message);
            } else {
                logger.info(message);
            }
        }
    }

    public static void sendErrorMessage(String errorText) {
        sendMessage(Component.literal("Error: ").append(ColorCode.RED + errorText), false);
    }

    /**
     * Checks if the player is on the Hypixel Network
     *
     * @return {@code true} if the player is on Hypixel, {@code false} otherwise
     */
    public boolean isOnHypixel() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        String brand = player.connection.serverBrand();
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
        boolean foundPlot = false;
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
            isGuest = strippedScoreboardTitle.endsWith("GUEST");

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
                    SkyblockEvents.JOINED.invoker().onSkyblockJoined();
                }

                Matcher dateMatcher = null;

                for (int lineNumber = 0; lineNumber < ScoreboardManager.getNumberOfLines(); lineNumber++) {
                    String line = ScoreboardManager.getScoreboardLines().get(lineNumber);
                    String strippedLine = ScoreboardManager.getStrippedScoreboardLines().get(lineNumber);

                    // Don't waste resources with empty strings
                    if (strippedLine.isEmpty())
                        continue;

                    // No need to try to find serverID after line 0
                    if (!foundServerID && lineNumber == 0) {
                        Matcher matcher = SERVER_REGEX.matcher(strippedLine);

                        if (matcher.find()) {
                            String serverType = matcher.group("serverType");
                            if (serverType.equals("m")) {
                                serverID = "mini" + matcher.group("serverCode");
                            } else if (serverType.equals("M")) {
                                serverID = "mega" + matcher.group("serverCode");
                            }
                            String mineshaft = matcher.group("mineshaft");
                            mineshaftID = mineshaft == null ? "" : mineshaft;
                            foundServerID = true;
                            continue;
                        }
                    }

                    // No need to try to find date after line 2
                    if (!foundDate && lineNumber < 3) {
                        Matcher dateM = DATE_PATTERN.matcher(strippedLine);
                        if (dateM.find()) {
                            dateMatcher = dateM;
                            foundDate = true;
                            continue;
                        }
                    }

                    // No need to try to find date after line 3
                    if (foundDate && !foundTime && lineNumber < 4) {
                        Matcher timeM = TIME_PATTERN.matcher(strippedLine);
                        if (timeM.find()) {
                            currentDate = SkyblockDate.parse(dateMatcher, timeM);
                            foundTime = true;
                            continue;
                        } else {
                            currentDate = SkyblockDate.parse(dateMatcher);
                        }
                    }

                    // No need to try to find location after line 5
                    if (lineNumber < 6) {
                        if (!foundLocation && (strippedLine.contains("⏣") || strippedLine.contains("ф"))) {
                            onRift = strippedLine.contains("ф");
                            location = strippedLine.substring(strippedLine.indexOf(' ') + 1).trim();

                            if (map == Island.KUUDRA || map == Island.DUNGEON) {
                                dungeonFloor = strippedLine.substring(strippedLine.lastIndexOf(" "));
                            } else if (map == Island.GARDEN) {
                                location = "The Garden";
                            } else if (map == Island.CRIMSON_ISLE) {
                                // Location fix
                                LocalPlayer player = Minecraft.getInstance().player;
                                if (player == null) return;
                                double x = player.xo;
                                double y = player.yo;
                                double z = player.zo;
                                if (-550 < x && x <-450 && 80 < y && y < 130 && -900 < z && z < -625) {
                                    location = "Burning Desert";
                                }
                            }

                            foundLocation = true;
                            continue;
                        } else if (!foundPlot && map == Island.GARDEN /* && foundLocation*/) {
                            if (strippedLine.contains("Plot -")) {
                                String rawPlotName = strippedLine.substring(strippedLine.indexOf('-') + 2);
                                plotName = PEST_PATTERN.matcher(rawPlotName).replaceAll("");
                                foundPlot = true;
                            }
                        }
                    }

                    // No need to try to find purse after line 8
                    if (!foundCoins && lineNumber < 9) {
                        if (!onRift && (strippedLine.startsWith("Piggy:") || strippedLine.contains("Purse:"))) {
                            String purseStr = strippedLine.substring(strippedLine.indexOf(' ') + 1);
                            try {
                                purse = TextUtils.NUMBER_FORMAT.parse(purseStr).doubleValue();
                            } catch (ParseException ex) {
                                //logger.error("Failed to parse purse (" + ex.getMessage() + ")", ex);
                                purse = 0.0;
                            }
                            foundCoins = true;
                            continue;
                        } else if (onRift && strippedLine.startsWith("Motes:")) {
                            String motesStr = strippedLine.substring(strippedLine.indexOf(' ') + 1);
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
                        if (strippedLine.startsWith("Bits:")) {
                            String bitsStr = strippedLine.substring(strippedLine.indexOf(' ') + 1);
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
                        if (strippedLine.equals("Tracker Mob Location:")) {
                            isTrackingAnimal = true;
                            foundTrackingAnimal = true;
                            continue;
                        }
                    }

                    // Lines after old switch-case
                    if (strippedLine.endsWith("Combat XP") || strippedLine.endsWith("Kills")) {
                        parseSlayerProgress(strippedLine);
                        continue;
                    }

                    if (!onRift && !foundJerryWave && map == Island.JERRYS_WORKSHOP) {
                        if (strippedLine.startsWith("Wave")) {
                            foundJerryWave = true;

                            int newJerryWave;
                            try {
                                newJerryWave = Integer.parseInt(TextUtils.keepIntegerCharactersOnly(strippedLine));
                            } catch (NumberFormatException ignored) {
                                newJerryWave = 0;
                            }
                            if (jerryWave != newJerryWave) {
                                jerryWave = newJerryWave;
                            }

                            continue;
                        }
                    }

                    if (!onRift && !foundInDungeon && strippedLine.startsWith("Cleared: ")) {
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
                        Matcher slayerMatcher = SLAYER_TYPE_REGEX.matcher(strippedLine);
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
                                    LOGGER.error("Failed to parse slayer level (" + ex.getMessage() + ")", ex);
                                }
                            }
                        }
                    }

                    if (strippedLine.equals("Slay the boss!")) {
                        foundBossAlive = true;
                        slayerBossAlive = true;
                        continue;
                    }

                    if (inDungeon) {
                        try {
                            main.getDungeonManager().updateDungeonPlayer(line);
                        } catch (NumberFormatException ex) {
                            LOGGER.error("Failed to update a dungeon player from the line " + line + ".", ex);
                        }
                    }

                    // Check if the player is on the Hypixel Alpha Network
                    if (lineNumber == ScoreboardManager.getNumberOfLines() - 1 && !foundAlphaIP && strippedLine.contains("alpha.hypixel.net")) {
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
                location = "Unknown";
                dungeonFloor = "";
            }
            if (!foundPlot) {
                plotName = "";
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
                SkyblockEvents.LEFT.invoker().onSkyblockLeft();
            }
        }
    }

    private boolean triggeredSlayerWarning = false;
    private float lastCompletion;

    private void parseSlayerProgress(String line) {
        if (Feature.BOSS_APPROACH_ALERT.isDisabled()) return;

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
                boolean repeating = Feature.BOSS_APPROACH_ALERT.isEnabled(FeatureSetting.REPEATING_BOSS_APPROACH_ALERT);
                if (!triggeredSlayerWarning || (repeating && completion != lastCompletion)) {
                    triggeredSlayerWarning = true;
                    main.getUtils().playLoudSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5);
//                    main.getRenderListener().setTitleFeature(Feature.BOSS_APPROACH_ALERT); TODO
//                    main.getScheduler().scheduleTask(
//                            () -> main.getRenderListener().setSubtitleFeature(null),
//                            main.getConfigValues().getWarningSeconds() * 20L
//                    );
                }
            } else {
                triggeredSlayerWarning = false; // Reset warning flag when completion is below 85%, meaning they started a new quest.
            }

            lastCompletion = completion;
        }
    }

    public int getDefaultColor(float alphaFloat) {
        int alpha = (int) alphaFloat;
        return ARGB.color(alpha, 150, 236, 255);
    }

    /**
     * When you use this function, any sound played will bypass the player's
     * volume setting, so make sure to only use this for like warnings or stuff like that.
     */
    public void playLoudSound(SoundEvent sound, double pitch) {
        playingLoudSound = true;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        player.playSound(sound, 1, (float) pitch);
        playingLoudSound = false;
    }

    /**
     * This one plays the sound normally. See {@link Utils#playLoudSound(SoundEvent, double)} for playing
     * a sound that bypasses the user's volume settings.
     */
    public static void playSound(SoundEvent sound, double pitch) {
        playSound(sound, 1, pitch);
    }

    public static void playSound(SoundEvent sound, double volume, double pitch) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        player.playSound(sound, (float) volume, (float) pitch);
    }

    /**
     * Checks if the given reforge is similar to any reforges on the desired/exclusions lists from the reforge filter feature.
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
     * @return the folder the SkyblockAddons jar is located in
     */
    public File getSBAFolder() {
        Path container = FabricLoader.getInstance().getConfigDir();
        try {
            return container.resolve(SkyblockAddons.MOD_ID).toFile();
        } catch (Exception ignored) {
            return container.toFile();
        }
    }

    /**
     * Checks if it is currently Halloween according to the system calendar.
     * @return {@code true} if it is Halloween, {@code false} otherwise
     */
    public boolean isHalloween() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == Calendar.OCTOBER && calendar.get(Calendar.DAY_OF_MONTH) == 31;
    }

    public String[] wrapSplitText(String text, int wrapLength) {
        return WordUtils.wrap(text, wrapLength).replace("\r", "").split(Pattern.quote("\n"));
    }

    public boolean itemIsInHotbar(ItemStack itemStack) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;

        NonNullList<ItemStack> inventory = player.getInventory().getNonEquipmentItems();

        for (int slot = 0; slot < 9; slot++) {
            if (inventory.get(slot) == itemStack) {
                return true;
            }
        }
        return false;
    }

    private boolean depthEnabled;
    private boolean blendEnabled;
    private boolean alphaEnabled;
    private int blendFunctionSrcFactor;
    private int blendFunctionDstFactor;

    public boolean isModLoaded(String modId) {
        return isModLoaded(modId, null);
    }

    /**
     * Check if another mod is loaded.
     * @param modId   The modid to check.
     * @param version The version of the mod to match (optional).
     */
    public boolean isModLoaded(String modId, String version) {
        boolean isLoaded = FabricLoader.getInstance().isModLoaded(modId); // Check for the modid...

        if (isLoaded && version != null) { // Check for the specific version...
            for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
                ModMetadata modMetadata = modContainer.getMetadata();
                if (modMetadata.getId().equals(modId) && modMetadata.getVersion().toString().equals(version)) {
                    return true;
                }
            }

            return false;
        }

        return isLoaded;
    }

    public float[] getCurrentGLTransformations() {
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, buf);
        buf.rewind();
        Matrix4f mat = new Matrix4f();
        mat.get(buf);

        float x = mat.m30();
        float y = mat.m31();
        float z = mat.m32();

        float scale = (float) Math.sqrt(mat.m00() * mat.m00() + mat.m01() * mat.m01() + mat.m02() * mat.m02());

        return new float[]{x, y, z, scale};
    }

    public static Player getPlayerFromName(@NonNull String name) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            for (Player player : level.players()) {
                if (name.equals(player.getGameProfile().getName())) {
                    return player;
                }
            }
        }
        return null;
    }

    public boolean isBlankGlassPane(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }

        Block block = Block.byItem(itemStack.getItem());
        boolean isGlassPane = block == Blocks.GLASS_PANE || block instanceof StainedGlassPaneBlock;
        boolean hasBlankDisplayName = itemStack.getHoverName().getString().isBlank();

        return isGlassPane && hasBlankDisplayName;
    }

    public boolean isGlassPaneColor(ItemStack itemStack, DyeColor color) {
        return itemStack != null && itemStack.getOrDefault(DataComponents.MAP_COLOR, -1) == color.getMapColor();
    }

    public static float getPartialTicks() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }

    public static byte[] toByteArray(BufferedInputStream inputStream) throws IOException {
        byte[] bytes;
        try (inputStream) {
            bytes = IOUtils.toByteArray(inputStream);
        }
        return bytes;
    }

    public static Entity getEntityFromUUID(UUID uuid) {
        ClientLevel world = Minecraft.getInstance().level;
        if (uuid == null || world == null) {
            return null;
        }

        for (Entity entity : world.entitiesForRendering()) {
            if (entity.getUUID().equals(uuid)) {
                return entity;
            }
        }

        return null;
    }

}