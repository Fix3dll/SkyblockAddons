package codes.biscuit.skyblockaddons.features.dungeon;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.EssenceType;
import codes.biscuit.skyblockaddons.core.chroma.ManualChromaManager;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircle;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.objects.Pair;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.text.ParseException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class contains a set of utility methods for Skyblock Dungeons.
 */
public class DungeonManager {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private static final Pattern PATTERN_MILESTONE = Pattern.compile("^.+?(Healer|Tank|Mage|Archer|Berserk) Milestone .+?([❶-❿]).+?§r§.([\\d,]+)");
    private static final Pattern PATTERN_COLLECTED_ESSENCES = Pattern.compile("§.+?(\\d+) (Wither|Spider|Undead|Dragon|Gold|Diamond|Ice|Crimson) Essence");
    private static final Pattern PATTERN_BONUS_ESSENCE = Pattern.compile("^§.+?[^You] .+?found a .+?(Wither|Spider|Undead|Dragon|Gold|Diamond|Ice|Crimson) Essence.+?");
    private static final Pattern PATTERN_SALVAGE_ESSENCES = Pattern.compile("\\+(?<essenceNum>[0-9]+) (?<essenceType>Wither|Spider|Undead|Dragon|Gold|Diamond|Ice|Crimson) Essence!");
    private static final Pattern PATTERN_SECRETS = Pattern.compile("§7([0-9]+)/([0-9]+) Secrets");
    private static final Pattern PATTERN_PLAYER_LINE = Pattern.compile("§.\\[(?<classLetter>.)] (?<name>[\\w§]+) §(?<healthColor>.)(?:§l)?(?<health>[\\w,§]+)(?:[§c❤]{0,3})?");
    private static final Pattern PATTERN_PLAYER_LIST_INFO_DEATHS = Pattern.compile("Team Deaths: (?<deaths>\\d+)");
    private static final Pattern PATTERN_STRIP_FORMAT = Pattern.compile("§.?");
    private static final ResourceLocation CRITICAL = new ResourceLocation("skyblockaddons", "critical.png");
    private static final int CRITICAL_ICON_SIZE = 25;

    /** The last dungeon server the player played on */
    @Getter @Setter private String lastServerId;

    /** The latest milestone the player received during a dungeon game */
    @Getter @Setter private DungeonMilestone dungeonMilestone;

    /** The latest essences the player collected during a dungeon game */
    @Getter private final EnumMap<EssenceType, Integer> collectedEssences = new EnumMap<>(EssenceType.class);

    /**
     * Represents the number of essences from salvaged items by the player.
     * It's in a separate map to avoid conflict with the collected map.
     */
    @Getter private final EnumMap<EssenceType, Integer> salvagedEssences = new EnumMap<>(EssenceType.class);

    /** The current teammates of the dungeon game */
    @Getter private final HashMap<String, DungeonPlayer> teammates = new HashMap<>();

    /** The current number of secrets found in the room */
    @Getter @Setter private int secrets = -1;

    /** The maximum number of secrets found in the room */
    @Getter @Setter private int maxSecrets;

    private EssenceType lastEssenceType;
    private int lastEssenceAmount;
    private int lastEssenceRepeat;

    /*
    Dungeon death counters
    These record the number of player deaths during the current dungeon run. There are multiple of them each with a
    different method of measurement. Their counts are compared to get as accurate of a measurement as possible.
     */

    /** The number of deaths according to the number of {@link codes.biscuit.skyblockaddons.events.SkyblockPlayerDeathEvent}
     *  events fired */
    @Getter private int deaths;
    /** The number of deaths according the number of deaths reported by the team health display in the scoreboard */
    @Getter private int alternateDeaths;
    /** The number of deaths displayed on the detailed tab list (if enabled) */
    @Getter private int playerListInfoDeaths;

    @Getter @Setter private Pair<DungeonClass, Integer> thePlayerClass = null;

    /**
     * Entity-level predicate to determine whether a specific entity should be outlined, and if so, what color.
     * <p>
     * Return {@code null} if the entity should not be outlined, or the integer color of the entity to be outlined
     * if the entity should be outlined
     */
    private static final Function<Entity, Integer> OUTLINE_COLOR = e -> {
        // Only accept other player entities
        if (e instanceof EntityOtherPlayerMP && main.getUtils().isInDungeon()) {
            String profileName = ((EntityOtherPlayerMP) e).getGameProfile().getName();
            DungeonPlayer teammate = SkyblockAddons.getInstance().getDungeonManager().getDungeonPlayerByName(profileName);

            if (teammate != null) {
                if (Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY.isEnabled(FeatureSetting.CLASS_COLORED_TEAMMATE)) {
                    return teammate.getDungeonClass().getColor();
                } else {
                    if (teammate.isCritical()) {
                        return ColorCode.RED.getColor();
                    } else if (teammate.isLow()) {
                        return ColorCode.YELLOW.getColor();
                    }
                }
            }
            // NPCs don't have a color on their team. Don't show them on outlines.
            return null;
        }
        return null;
    };

    /**
     * Clear the dungeon game data. Called by {@link codes.biscuit.skyblockaddons.utils.Utils} each new game
     */
    public void reset() {
        dungeonMilestone = null;
        collectedEssences.clear();
        teammates.clear();
        thePlayerClass = null;
        deaths = 0;
        alternateDeaths = 0;
        playerListInfoDeaths = 0;
        HealingCircle.setRadius(0);
    }

    /**
     * Returns the {@code DungeonPlayer} object for the player with the given username.
     *
     * @param name the player's username
     * @return the {@code DungeonPlayer} object for the player with the given username
     */
    public DungeonPlayer getDungeonPlayerByName(String name) {
        return teammates.get(name);
    }

    /**
     * This method parses the class milestone attained from the chat message the player receives when they attain a milestone.
     *
     * @param message the chat message received
     * @return a {@code DungeonMilestone} object representing the milestone if one is found, or {@code null} if no milestone is found
     */
    public DungeonMilestone parseMilestone(String message) {
        Matcher matcher = PATTERN_MILESTONE.matcher(message);
        if (!matcher.lookingAt()) {
            return null;
        }

        DungeonClass dungeonClass = DungeonClass.fromDisplayName(matcher.group(1));
        return new DungeonMilestone(dungeonClass, matcher.group(2), matcher.group(3));
    }

    /**
     * This method parses the type and amount of essence the player collected from the action bar message that shows up
     * when an essence is collected. It then records the result in {@code collectedEssences}.
     *
     * @param message the action bar message to parse essence information from
     */
    public void addEssence(String message) {
        Matcher matcher = PATTERN_COLLECTED_ESSENCES.matcher(message);

        while (matcher.find()) {

            int amount = Integer.parseInt(matcher.group(1));
            EssenceType essenceType = EssenceType.fromName(matcher.group(2));

            // Fix: Add x3 of the original collected
            // This happens because the action bar receives the collected essence 3 times
            if (lastEssenceType != null && lastEssenceAmount == amount && lastEssenceType == essenceType) {
                lastEssenceRepeat++;

                if (lastEssenceRepeat == 3) {
                    lastEssenceType = null; // Trigger a reset of the original collected essence in the third spam
                }
                continue; // Prevent the spam collected essence to be accounted
            }
            lastEssenceType = essenceType;
            lastEssenceAmount = amount;
            lastEssenceRepeat = 1;

            if (essenceType != null) {
                collectedEssences.put(essenceType, collectedEssences.getOrDefault(essenceType, 0) + amount);
            }
        }
    }

    /**
     * This method parses the type and amount of essence gained when a dungeon teammate finds a bonus essence.
     * This information is parsed from the given chat message. It then records the result in {@code collectedEssences}.
     *
     * @param message the chat message to parse essence information from
     */
    public void addBonusEssence(String message) {
        Matcher matcher = PATTERN_BONUS_ESSENCE.matcher(message);

        if (matcher.matches()) {
            EssenceType essenceType = EssenceType.fromName(matcher.group(1));

            collectedEssences.put(essenceType, collectedEssences.getOrDefault(essenceType, 0) + 1);
        }
    }

    /**
     * This method parses the current and the maximum number of secrets found in the room.
     *
     * @param message the action bar message to parse secrets information from
     */
    public void addSecrets(String message) {
        Matcher matcher = PATTERN_SECRETS.matcher(message);
        if (!matcher.find()) {
            secrets = -1;
            return;
        }

        secrets = Integer.parseInt(matcher.group(1));
        maxSecrets = Integer.parseInt(matcher.group(2));
        SkyblockAddons.getInstance().getPlayerListener().getActionBarParser().getStringsToRemove().add(matcher.group());
    }

    /**
     * This method parses the type and amount of essences obtained from the salvaged item by the player.
     * This information is parsed from the given chat message. It then records the result in {@code collectedEssences}
     *
     * @param message the chat message to parse the obtained essences from
     */
    public void addSalvagedEssences(String message) {
        Matcher matcher = PATTERN_SALVAGE_ESSENCES.matcher(message);

        while (matcher.find()) {
            EssenceType essenceType = EssenceType.fromName(matcher.group("essenceType"));
            int amount = Integer.parseInt(matcher.group("essenceNum"));

            salvagedEssences.put(essenceType, salvagedEssences.getOrDefault(essenceType, 0) + amount);
        }
    }

    /**
     * @param type Essence type
     * @param number Total number of essences
     */
    public void setSalvagedEssences(EssenceType type, String number) {
        int amount = 0;
        try {
            amount = TextUtils.NUMBER_FORMAT.parse(number).intValue();
        } catch (ParseException ex) {
            LOGGER.error("Failed to parse " + type.getNiceName() + " essence amount: ", ex);
        }
        salvagedEssences.put(type, amount);
    }

    /**
     * This method parses dungeon player stats displayed on the scoreboard sidebar and stores them as {@code DungeonPlayer}
     * objects. It first determines if the given line represents a dungeon player's stats. If so, it then parses all the
     * stats from the line. Finally, it creates a new {@code DungeonPlayer} object containing the parsed stats or updates
     * an existing {@code DungeonPlayer} object with the parsed stats (if one already exists for the player whose stats
     * are shown in the line).
     */
    public void updateDungeonPlayer(String scoreboardLine) {
        Matcher matcher = PATTERN_PLAYER_LINE.matcher(scoreboardLine);

        if (matcher.find()) {
            String name = TextUtils.stripColor(matcher.group("name"));

            // This is inconsistent, don't add the player themselves...
            if (name.equals(MC.thePlayer.getName())) {
                return;
            }

            DungeonClass dungeonClass = DungeonClass.fromFirstLetter(matcher.group("classLetter").charAt(0));
            ColorCode healthColor = ColorCode.getByChar(matcher.group("healthColor").charAt(0));
            String healthText = PATTERN_STRIP_FORMAT.matcher(matcher.group("health")).replaceAll("");
            int health;

            if (healthText.equals("DEAD")) {
                health = 0;
            } else {
                try {
                    health = TextUtils.NUMBER_FORMAT.parse(healthText).intValue();
                } catch (ParseException ex) {
                    LOGGER.error("Failed to parse player "+ name + " health: " + healthText, ex);
                    return;
                }
            }

            for (DungeonPlayer player: teammates.values()) {
                if (player.getName().startsWith(name)) {
                    player.setHealthColor(healthColor);

                    if (player.getHealth() > 0 && health == 0) {
                        this.addAlternateDeath();
                    }

                    player.setHealth(health);
                    return;
                }
            }

            for (NetworkPlayerInfo networkPlayerInfo: MC.getNetHandler().getPlayerInfoMap()) {
                String profileName = networkPlayerInfo.getGameProfile().getName();

                if (profileName.startsWith(name)) {
                    teammates.put(profileName, new DungeonPlayer(profileName, dungeonClass, healthColor, health));
                }
            }
        }
    }

    /**
     * Returns the most accurate death count available. If the player has enabled their "Player List Info" setting, the
     * death count from the tab menu is returned. If that setting isn't enabled, the highest count out of the main counter
     * and the alternative counter's counts is returned.
     *
     * @return the most accurate death count available
     */
    public int getDeathCount() {
        if (SkyblockAddons.getInstance().getDungeonManager().isPlayerListInfoEnabled()) {
            return playerListInfoDeaths;
        } else {
            return Math.max(deaths, alternateDeaths);
        }
    }

    /**
     * Adds one death to the counter
     */
    public void addDeath() {
        deaths++;
    }

    /**
     * Adds one death to the alternative counter.
     */
    public void addAlternateDeath() {
        alternateDeaths++;
    }

    /**
     * This method updates the death counter with the count from the death counter in the Player List Info display.
     * If the death counter isn't being shown in the Player List Info display, nothing will be changed.
     */
    public void updateDeathsFromPlayerListInfo() {
        NetHandlerPlayClient netHandlerPlayClient = MC.getNetHandler();
        NetworkPlayerInfo deathDisplayPlayerInfo = netHandlerPlayClient.getPlayerInfo("!B-f");

        if (deathDisplayPlayerInfo != null) {
            String deathDisplayString = deathDisplayPlayerInfo.getDisplayName().getUnformattedText();
            Matcher deathDisplayMatcher = PATTERN_PLAYER_LIST_INFO_DEATHS.matcher(deathDisplayString);

            if (deathDisplayMatcher.matches()) {
                playerListInfoDeaths = Integer.parseInt(deathDisplayMatcher.group("deaths"));
            }
        }
    }

    public boolean isPlayerListInfoEnabled() {
        NetHandlerPlayClient netHandlerPlayClient = MC.getNetHandler();
        if (netHandlerPlayClient == null) {
            return false;
        }

        List<NetworkPlayerInfo> networkPlayerInfoList = netHandlerPlayClient.getPlayerInfoMap().stream().limit(10).collect(Collectors.toList());
        for (NetworkPlayerInfo networkPlayerInfo : networkPlayerInfoList) {
            String username = networkPlayerInfo.getGameProfile().getName();
            if (username.startsWith("!")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method for rendering {@link Feature#SHOW_CRITICAL_DUNGEONS_TEAMMATES}
     * and {@link Feature#SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY}
     */
    @SubscribeEvent()
    public void onRenderLivingName(RenderLivingEvent.Specials.Pre<EntityLivingBase> e) {
        AbstractClientPlayer player;
        if (e.entity instanceof AbstractClientPlayer) {
            player = (AbstractClientPlayer) e.entity;
        } else {
            return;
        }

        boolean nameOverlayEnabled = Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY.isEnabled();
        boolean criticalOverlayEnabled = Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES.isEnabled();

        if (main.getUtils().isOnSkyblock() && main.getUtils().isInDungeon() && (criticalOverlayEnabled || nameOverlayEnabled)) {
            final Entity renderViewEntity = MC.getRenderViewEntity();
            String profileName = player.getName();
            DungeonPlayer dungeonPlayer = main.getDungeonManager().getTeammates().getOrDefault(profileName, null);

            if (renderViewEntity != player && dungeonPlayer != null) {
                double newY = e.y + player.height;

                if (nameOverlayEnabled) {
                    newY += 0.35F;
                }

                double distanceScale = Math.max(1.5, renderViewEntity.getPositionVector().distanceTo(player.getPositionVector()) / 8);

                if (Feature.ENTITY_OUTLINES.isEnabled(FeatureSetting.OUTLINE_DUNGEON_TEAMMATES)) {
                    newY += distanceScale * 0.85F;
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate(e.x, newY, e.z);
                GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-MC.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(MC.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-0.025, -0.025, 0.025);

                GlStateManager.scale(distanceScale, distanceScale, distanceScale);

                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.enableTexture2D();
                GlStateManager.color(1, 1, 1, 1);

                if (criticalOverlayEnabled && !dungeonPlayer.isGhost()
                        && (dungeonPlayer.isCritical() || dungeonPlayer.isLow())) {
                    MC.getTextureManager().bindTexture(CRITICAL);
                    DrawUtils.drawModalRectWithCustomSizedTexture(-CRITICAL_ICON_SIZE / 2F, -CRITICAL_ICON_SIZE, 0, 0, CRITICAL_ICON_SIZE, CRITICAL_ICON_SIZE, CRITICAL_ICON_SIZE, CRITICAL_ICON_SIZE);

                    String text;
                    if (dungeonPlayer.isLow()) {
                        text = ColorCode.YELLOW + "LOW";
                    } else if (dungeonPlayer.isCritical()) {
                        text = ColorCode.RED + "CRITICAL";
                    } else {
                        text = null;
                    }

                    if (text != null) {
                        MC.fontRendererObj.drawString(
                                text,
                                -MC.fontRendererObj.getStringWidth(text) / 2F,
                                CRITICAL_ICON_SIZE / 2F - 9,
                                -1,
                                true
                        );
                    }
                    e.setCanceled(true);
                }

                if (!dungeonPlayer.isGhost() && dungeonPlayer.getDungeonClass() != null && nameOverlayEnabled) {
                    String dungeonClass = ColorCode.YELLOW + "[" + dungeonPlayer.getDungeonClass().getFirstLetter() + "] ";
                    float nameX = MC.fontRendererObj.getStringWidth(dungeonClass.concat(profileName)) / 2F;
                    float nameY = CRITICAL_ICON_SIZE / 2F + 2;

                    DrawUtils.drawText(dungeonClass, -nameX, nameY, -1);

                    int classColor = dungeonPlayer.getDungeonClass().getColor();
                    if (classColor == ColorCode.CHROMA.getColor()) {
                       classColor = ManualChromaManager.getChromaColor(0, 0, 255);
                    }
                    DrawUtils.drawText(
                            profileName,
                            -nameX + MC.fontRendererObj.getStringWidth(dungeonClass),
                            nameY,
                            classColor
                    );

                    String health = dungeonPlayer.getHealth() + (ColorCode.RED + "❤");
                    DrawUtils.drawText(
                            health,
                            -MC.fontRendererObj.getStringWidth(health) / 2F,
                            CRITICAL_ICON_SIZE / 2F + 13,
                            -1
                    );
                    e.setCanceled(true);
                }

                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            }
        }
    }

    /**
     * Queues items to be outlined that satisfy global- and entity-level predicates.
     * @param e the outline event
     */
    @SubscribeEvent
    public void onRenderEntityOutlines(RenderEntityOutlineEvent e) {
        if (e.getType() == RenderEntityOutlineEvent.Type.XRAY) {
            // Test whether we should add any entities at all
            e.queueEntitiesToOutline(OUTLINE_COLOR);
        }
    }
}