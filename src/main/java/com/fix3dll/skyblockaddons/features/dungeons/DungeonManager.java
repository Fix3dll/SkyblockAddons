package com.fix3dll.skyblockaddons.features.dungeons;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.EssenceType;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.core.render.chroma.ManualChromaManager;
import com.fix3dll.skyblockaddons.events.RenderEntityOutlineEvent;
import com.fix3dll.skyblockaddons.features.healingcircle.HealingCircle;
import com.fix3dll.skyblockaddons.features.tablist.TabStringType;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils.ChromaMode;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.objects.Pair;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.ParseException;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains a set of utility methods for Skyblock Dungeons.
 */
public class DungeonManager {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private static final Pattern PATTERN_MILESTONE = Pattern.compile("^.+?(Healer|Tank|Mage|Archer|Berserk) Milestone .+?([❶-❿]).+?§r§.([\\d,]+)");
    private static final Pattern PATTERN_COLLECTED_ESSENCES = Pattern.compile("§.+?(\\d+) (Wither|Spider|Undead|Dragon|Gold|Diamond|Ice|Crimson) Essence");
    private static final Pattern PATTERN_BONUS_ESSENCE = Pattern.compile("^§.+?[^You] .+?found a .+?(Wither|Spider|Undead|Dragon|Gold|Diamond|Ice|Crimson) Essence.+?");
    private static final Pattern PATTERN_SALVAGE_ESSENCES = Pattern.compile("\\+(?<essenceNum>[0-9]+) (?<essenceType>Wither|Spider|Undead|Dragon|Gold|Diamond|Ice|Crimson) Essence!");
    private static final Pattern PATTERN_SECRETS = Pattern.compile("§7([0-9]+)/([0-9]+) Secrets");
    private static final Pattern PATTERN_PLAYER_LINE = Pattern.compile("§.\\[(?<classLetter>.)] (?<name>[\\w§]+) §(?<healthColor>.)(?:§l)?(?<health>[\\w,§]+)(?:[§c❤]{0,3})?");
    private static final Pattern PATTERN_PLAYER_LIST_INFO_DEATHS = Pattern.compile("Team Deaths: (?<deaths>\\d+)");
    private static final Pattern PATTERN_STRIP_FORMAT = Pattern.compile("§.?");
    private static final ResourceLocation CRITICAL = SkyblockAddons.resourceLocation("critical.png");
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
    @Getter private final Object2ObjectOpenHashMap<String, DungeonPlayer> teammates = new Object2ObjectOpenHashMap<>();

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

    /** The number of deaths according to the number of {@link com.fix3dll.skyblockaddons.events.SkyblockEvents.SkyblockPlayerDeath}
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
        if (e instanceof RemotePlayer remotePlayer && main.getUtils().isInDungeon()) {
            String profileName = remotePlayer.getGameProfile().name();
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

    public DungeonManager() {
        RenderEntityOutlineEvent.EVENT.register(this::onRenderEntityOutlines);
    }

    /**
     * Clear the dungeon game data. Called by {@link com.fix3dll.skyblockaddons.utils.Utils} each new game
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
     * @param message the action bar message to parse secrets information from
     */
    public void addSecrets(String message) {
        Matcher matcher = PATTERN_SECRETS.matcher(message);
        if (!matcher.find()) {
            secrets = -1;
            maxSecrets = 0;
            return;
        }

        secrets = Integer.parseInt(matcher.group(1));
        maxSecrets = Integer.parseInt(matcher.group(2));
        SkyblockAddons.getInstance().getPlayerListener().getActionBarParser().getStringsToRemove().add(matcher.group());
    }

    /**
     * This method parses the type and amount of essences obtained from the salvaged item by the player.
     * This information is parsed from the given chat message. It then records the result in {@code collectedEssences}
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

            LocalPlayer playerEntity = MC.player;

            // This is inconsistent, don't add the playerEntity themselves...
            if (playerEntity == null || name.equals(playerEntity.getGameProfile().name())) {
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
                    LOGGER.error("Failed to parse playerEntity "+ name + " health: " + healthText, ex);
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

            ClientPacketListener networkHandler =  MC.getConnection();
            if (networkHandler == null) return;

            for (PlayerInfo playerListEntry : networkHandler.getOnlinePlayers()) {
                String profileName = playerListEntry.getProfile().name();

                if (profileName.startsWith(name)) {
                    teammates.put(profileName, new DungeonPlayer(profileName, dungeonClass, healthColor, health, playerListEntry.getProfile().id()));
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
        if (this.isPlayerListInfoEnabled()) {
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
        ClientPacketListener networkHandler =  MC.getConnection();
        if (networkHandler == null) return;
        PlayerInfo deathDisplayPlayerInfo = networkHandler.getPlayerInfo("!B-f");

        if (deathDisplayPlayerInfo != null && deathDisplayPlayerInfo.getTabListDisplayName() != null) {
            String deathDisplayString = deathDisplayPlayerInfo.getTabListDisplayName().getString();
            Matcher deathDisplayMatcher = PATTERN_PLAYER_LIST_INFO_DEATHS.matcher(deathDisplayString);

            if (deathDisplayMatcher.matches()) {
                playerListInfoDeaths = Integer.parseInt(deathDisplayMatcher.group("deaths"));
            }
        }
    }

    public boolean isPlayerListInfoEnabled() {
        ClientPacketListener networkHandler =  MC.getConnection();
        if (networkHandler == null) {
            return false;
        }

        List<PlayerInfo> networkPlayerInfoList = networkHandler.getOnlinePlayers().stream().limit(10).toList();
        for (PlayerInfo networkPlayerInfo : networkPlayerInfoList) {
            String username = networkPlayerInfo.getProfile().name();
            if (username.startsWith("!")) {
                return true;
            }
        }

        return false;
    }

    public boolean onRenderNameTag(AvatarRenderState state,
                                   PoseStack poseStack,
                                   SubmitNodeCollector submitNodeCollector,
                                   CameraRenderState cameraRenderState,
                                   CallbackInfo ci) {
        Vec3 vec3 = state.nameTagAttachment;
        Component nameTag = state.nameTag;
        if (nameTag == null || vec3 == null || MC.player == null || TabStringType.usernameFromLine(nameTag.getString())
                .equals(MC.player.getGameProfile().name())) {
            return false;
        }

        boolean nameOverlayEnabled = Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY.isEnabled();
        boolean criticalOverlayEnabled = Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES.isEnabled();
        ClientLevel level = MC.level;

        if (level != null && main.getUtils().isOnSkyblock() && main.getUtils().isInDungeon() && (criticalOverlayEnabled || nameOverlayEnabled)) {
            Entity cameraEntity = MC.getCameraEntity();
            AbstractClientPlayer player = null;
            DungeonPlayer dungeonPlayer = null;


            for (DungeonPlayer teammate : teammates.values()) {
                if (TabStringType.usernameFromLine(nameTag.getString()).equals(teammate.getName())) {
                    if (level.getEntity(teammate.getEntityId()) instanceof AbstractClientPlayer playerEntity) {
                        player = playerEntity;
                        dungeonPlayer = teammate;
                        break;
                    }
                }
            }

            if (cameraEntity != null && cameraEntity != player && dungeonPlayer != null) {
                boolean canceled = false;
                float distanceScale = Math.max(1.0F, (float) cameraEntity.position().distanceTo(player.position()) / 5F);
                // 9.0F == MC.font.lineHeight

                poseStack.pushPose();
                poseStack.scale(distanceScale, distanceScale, distanceScale);
                poseStack.translate(0, -vec3.y + 1.8F / distanceScale, 0);

                if (criticalOverlayEnabled && !dungeonPlayer.isGhost() && (dungeonPlayer.isCritical() || dungeonPlayer.isLow())) {
                    poseStack.pushPose();
                    poseStack.translate(0, (CRITICAL_ICON_SIZE + 18.0F) * 1.15F * 0.025F, 0);
                    poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
                    poseStack.mulPose(cameraRenderState.orientation);
                    poseStack.scale(0.025F, -0.025F, 0.025F);
                    submitNodeCollector.submitCustomGeometry(
                            poseStack,
                            RenderType.blockScreenEffect(CRITICAL),
                            (pose, vertexConsumer) -> DrawUtils.blitAbsolute(pose, vertexConsumer, -CRITICAL_ICON_SIZE / 2F, 0, 0, 0, CRITICAL_ICON_SIZE, CRITICAL_ICON_SIZE, CRITICAL_ICON_SIZE, CRITICAL_ICON_SIZE, -1)
                    );
                    poseStack.popPose();

                    String text;
                    if (dungeonPlayer.isLow()) {
                        text = ColorCode.YELLOW + "LOW";
                    } else if (dungeonPlayer.isCritical()) {
                        text = ColorCode.RED + "CRITICAL";
                    } else {
                        text = null;
                    }

                    if (text != null) {
                        poseStack.pushPose();
                        poseStack.translate(0, 18.0F * 1.15F * 0.025F, 0); // 18.0F == 2 * lineHeight
                        submitNodeCollector.submitNameTag(poseStack, vec3, 0, Component.literal(text), true, LightTexture.FULL_BRIGHT, state.distanceToCameraSq, cameraRenderState);
                        poseStack.popPose();
                    }
                    canceled = true;
                }

                if (!dungeonPlayer.isGhost() && dungeonPlayer.getDungeonClass() != null && nameOverlayEnabled) {
                    MutableComponent playerName = Component.literal(dungeonPlayer.getName());
                    int classColor = dungeonPlayer.getDungeonClass().getColor();
                    if (classColor == ColorCode.CHROMA.getColor()) {
                        if (Feature.CHROMA_MODE.getValue() == ChromaMode.FADE) {
                            playerName.withStyle(style -> style.withColor(DrawUtils.CHROMA_TEXT_COLOR));
                        } else {
                            playerName.withColor(ManualChromaManager.getChromaColor(0, 0, 255));
                        }
                    } else {
                        playerName.withColor(classColor);
                    }

                    String dungeonClass = ColorCode.YELLOW + "[" + dungeonPlayer.getDungeonClass().getFirstLetter() + "] ";
                    MutableComponent playerNameTag = Component.literal(dungeonClass).append(playerName);

                    poseStack.pushPose();
                    String health = dungeonPlayer.getHealth() + " " + ColorCode.RED + "❤";
                    submitNodeCollector.submitNameTag(poseStack, vec3, 0, Component.literal(health), true, LightTexture.FULL_BRIGHT, state.distanceToCameraSq, cameraRenderState);
                    poseStack.translate(0, 9.0F * 1.15F * 0.025F, 0);
                    submitNodeCollector.submitNameTag(poseStack, vec3, 0, playerNameTag, true, LightTexture.FULL_BRIGHT, state.distanceToCameraSq, cameraRenderState);
                    poseStack.popPose();
                    canceled = true;
                }
                poseStack.popPose();

                return canceled;
            }
        }
        return false;
    }

    /**
     * Queues items to be outlined that satisfy global- and entity-level predicates.
     * @param e the outline event
     */
    private void onRenderEntityOutlines(RenderEntityOutlineEvent e) {
        if (e.getType() == RenderEntityOutlineEvent.Type.XRAY) {
            // Test whether we should add any entities at all
            e.queueEntitiesToOutline(OUTLINE_COLOR);
        }
    }

}