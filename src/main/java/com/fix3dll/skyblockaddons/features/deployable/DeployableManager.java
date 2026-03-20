package com.fix3dll.skyblockaddons.features.deployable;

import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for managing active Deployable around the player.
 * {@link #put(Deployable, int, UUID) Insert} deployable that get detected and {@link #getActiveDeployable() get} the
 * active deployable with the highest priority (enum ordinal).
 */
public class DeployableManager {

    /** The DeployableManager instance. */
    @Getter private static final DeployableManager instance = new DeployableManager();
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Pattern DEPLOYABLE_PATTERN = Pattern.compile("[A-Za-z '-]* (?<seconds>[0-9]*)s");
    private static final Pattern TOTEM_PATTERN = Pattern.compile("Remaining: (?:(?<minutes>\\d{1,2})m )?(?<seconds>\\d{1,2})s");

    /** Entry displaying {@link Deployable#SOS_FLARE} at 90 seconds for the edit screen */
    public static final ArmorStand DUMMY_ARMOR_STAND;
    public static final DeployableEntry DUMMY_DEPLOYABLE_ENTRY;

    private final Map<Deployable, DeployableEntry> deployableEntryMap = new HashMap<>();

    static {
        DUMMY_ARMOR_STAND = new ArmorStand(EntityType.ARMOR_STAND, MC.level);
        DUMMY_ARMOR_STAND.setItemSlot(EquipmentSlot.HEAD, ItemUtils.getTexturedHead("WILL_O_WISP"));
        DUMMY_ARMOR_STAND.setInvisible(true);
        DUMMY_DEPLOYABLE_ENTRY = new DeployableEntry(Deployable.WILL_O_WISP, 300, DUMMY_ARMOR_STAND.getUUID());
    }

    /**
     * Put any detected deployable into the list of active deployables.
     * @param deployable Detected Deployable type
     * @param seconds Seconds the deployable has left before running out
     */
    private void put(Deployable deployable, int seconds, UUID uuid) {
        deployableEntryMap.put(deployable, new DeployableEntry(deployable, seconds, uuid));
    }

    /**
     * Get the active deployable with the highest priority. Priority is based on enum value's ordinal
     * and the returned deployable is guaranteed to have been active at least 100ms ago.
     * @return Highest priority deployable or null if none is around
     */
    public DeployableEntry getActiveDeployable() {
        Optional<Map.Entry<Deployable, DeployableEntry>> max = deployableEntryMap.entrySet().stream()
                .filter(deployableEntryEntry -> deployableEntryEntry.getValue().timestamp + 100 > System.currentTimeMillis())
                .max(Map.Entry.comparingByKey());

        return max.map(Map.Entry::getValue).orElse(null);
    }

    /**
     * Detects a deployable from an entity, and puts it in this manager.
     * @param armorStand The entity to detect whether it is a deployable or not.
     */
    public void detectDeployables(ArmorStand armorStand) {
        Component customName = armorStand.getCustomName();

        if (customName != null) {
            String customNameString = customName.getString();
            Deployable orb = Deployable.getByDisplayName(customNameString);

            if (orb != null && (orb.isInRadius(armorStand.distanceToSqr(MC.player)) || lanternInMineshaft(orb))) {
                if (orb == Deployable.TOTEM_OF_CORRUPTION) {
                    List<ArmorStand> surroundingArmorStands = MC.level.getEntitiesOfClass(
                            ArmorStand.class,
                            new AABB(armorStand.getX() - 0.1,
                                    armorStand.getY() - 1,
                                    armorStand.getZ() - 0.1,
                                    armorStand.getX() + 0.1,
                                    armorStand.getY(),
                                    armorStand.getZ() + 0.1)
                    );
                    String ownerLine = "Owner: " + MC.player.getGameProfile().name();
                    int seconds = Integer.MIN_VALUE;
                    UUID uuid = null;

                    boolean patternFound = false;
                    boolean ownedByPlayer = false;
                    for (ArmorStand entry : surroundingArmorStands) {
                        Component entryCustomName = entry.getCustomName();
                        if (entryCustomName == null) continue;

                        String entryCustomNameString = entryCustomName.getString();
                        Matcher matcher = TOTEM_PATTERN.matcher(entryCustomNameString);
                        if (!patternFound && matcher.matches()) {
                            seconds = getSeconds(matcher);
                            uuid = entry.getUUID();
                            patternFound = true;
                        } else if (entryCustomNameString.equals(ownerLine)) {
                            ownedByPlayer = true;
                        }

                        if (patternFound && ownedByPlayer) {
                            put(orb, seconds, uuid);
                        }
                    }
                } else {
                    Matcher matcher = DEPLOYABLE_PATTERN.matcher(customNameString);

                    if (matcher.matches()) {
                        int seconds;
                        try {
                            // Apparently they don't have a second count for moment after spawning, that's what this try-catch is for
                            seconds = Integer.parseInt(matcher.group("seconds"));
                        } catch (NumberFormatException ex) {
                            // It's okay, just don't add the deployable I guess...
                            return;
                        }

                        List<ArmorStand> surroundingArmorStands = MC.level.getEntitiesOfClass(
                                ArmorStand.class,
                                new AABB(
                                        armorStand.getX() - 0.1,
                                        armorStand.getY() - 1,
                                        armorStand.getZ() - 0.1,
                                        armorStand.getX() + 0.1,
                                        armorStand.getY() + 1,
                                        armorStand.getZ() + 0.1
                                ),
                                armorStandEntity -> armorStandEntity.getItemBySlot(EquipmentSlot.HEAD) != ItemStack.EMPTY
                        );
                        if (!surroundingArmorStands.isEmpty()) {
                            ArmorStand orbArmorStand = surroundingArmorStands.getFirst();

                            put(orb, seconds, orbArmorStand == null ? null : orbArmorStand.getUUID());
                        }
                    }
                }
            }
        } else {
            // Flare detection
            // TODO optimize
            if (armorStand.isInvisible()) {
                // we need skull on head
                ItemStack headItem = armorStand.getItemBySlot(EquipmentSlot.HEAD);
                if (headItem == ItemStack.EMPTY) return;

                String skullTexture = ItemUtils.getSkullTexture(headItem);
                String decodedTextureUrl = TextUtils.decodeSkinTexture(skullTexture, true);
                if (decodedTextureUrl == null) return;

                Deployable flare = Deployable.getByTextureId(decodedTextureUrl);
                if (flare != null && flare.isInRadius(armorStand.distanceToSqr(MC.player))) {
                    // Default exist time of flares
                    int seconds = 180;
                    // 1 tick = 50ms
                    seconds -= armorStand.tickCount * 50 / 1000;

                    put(flare, seconds, armorStand.getUUID());
                }
            }
        }
    }

    private static int getSeconds(Matcher matcher) {
        String secondsStr = matcher.group("seconds");
        int seconds = 0;
        if (secondsStr != null && !secondsStr.isEmpty()) {
            seconds = Integer.parseInt(secondsStr);
        }

        String minutesStr = matcher.group("minutes");
        if (minutesStr != null && !minutesStr.isEmpty()) {
            seconds += Integer.parseInt(minutesStr) * 60;
        }

        return seconds;
    }

    private boolean lanternInMineshaft(Deployable deployable) {
        return (deployable == Deployable.GLACITE_LANTERN || deployable == Deployable.WILL_O_WISP)
                && LocationUtils.isOn(Island.MINESHAFT);
    }

    @Getter @AllArgsConstructor
    public static class DeployableEntry {
        /** The Deployable type. */
        private final Deployable deployable;

        /** Seconds the deployable has left before running out */
        private final int seconds;

        private final long timestamp = System.currentTimeMillis();

        private final UUID uuid;
    }

}