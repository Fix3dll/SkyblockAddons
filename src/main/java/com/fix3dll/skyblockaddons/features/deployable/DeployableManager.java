package com.fix3dll.skyblockaddons.features.deployable;

import com.fix3dll.skyblockaddons.utils.ItemUtils;
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
    private static final Pattern POWER_ORB_PATTERN = Pattern.compile("[A-Za-z ]* (?<seconds>[0-9]*)s");
    private static final Pattern TOTEM_PATTERN = Pattern.compile("Remaining: (?:(?<minutes>\\d{1,2})m )?(?<seconds>\\d{1,2})s");

    /** Entry displaying {@link Deployable#SOS_FLARE} at 90 seconds for the edit screen */
    public static ArmorStand DUMMY_ARMOR_STAND;
    public static DeployableEntry DUMMY_DEPLOYABLE_ENTRY;

    private final Map<Deployable, DeployableEntry> deployableEntryMap = new HashMap<>();

    static {
        DUMMY_ARMOR_STAND = new ArmorStand(EntityType.ARMOR_STAND, MC.level);
        DUMMY_ARMOR_STAND.setItemSlot(EquipmentSlot.HEAD, ItemUtils.getTexturedHead("SOS_FLARE"));
        DUMMY_ARMOR_STAND.setInvisible(true);
        DUMMY_DEPLOYABLE_ENTRY = new DeployableEntry(Deployable.SOS_FLARE, 90, DUMMY_ARMOR_STAND.getUUID());
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
     * @param entityArmorStand The entity to detect whether it is a deployable or not.
     */
    public void detectDeployables(ArmorStand entityArmorStand) {
        Component customName = entityArmorStand.getCustomName();

        if (customName != null) {
            String customNameString = customName.getString();
            Deployable orb = Deployable.getByDisplayName(customNameString);

            if (orb != null && orb.isInRadius(entityArmorStand.distanceToSqr(MC.player))) {
                if (orb == Deployable.TOTEM_OF_CORRUPTION) {
                    List<ArmorStand> surroundingArmorStands = MC.level.getEntitiesOfClass(
                            ArmorStand.class,
                            new AABB(
                                    entityArmorStand.getX() - 0.1,
                                    entityArmorStand.getY() - 1,
                                    entityArmorStand.getZ() - 0.1,
                                    entityArmorStand.getX() + 0.1,
                                    entityArmorStand.getY(),
                                    entityArmorStand.getZ() + 0.1
                            )
                    );
                    for (ArmorStand entry : surroundingArmorStands) {
                        Component entryCustomName = entry.getCustomName();
                        if (entryCustomName == null) continue;

                        Matcher matcher = TOTEM_PATTERN.matcher(entryCustomName.getString());
                        if (matcher.matches()) {
                            put(orb, getSeconds(matcher), entry.getUUID());
                            break;
                        }
                    }
                } else {
                    Matcher matcher = POWER_ORB_PATTERN.matcher(customNameString);

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
                                        entityArmorStand.getX() - 0.1,
                                        entityArmorStand.getY() - 1,
                                        entityArmorStand.getZ() - 0.1,
                                        entityArmorStand.getX() + 0.1,
                                        entityArmorStand.getY() + 1,
                                        entityArmorStand.getZ() + 0.1
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
            if (entityArmorStand.isInvisible()) {
                // we need skull on head
                ItemStack headItem = entityArmorStand.getItemBySlot(EquipmentSlot.HEAD);
                if (headItem == ItemStack.EMPTY) return;

                String skullTexture = ItemUtils.getSkullTexture(headItem);
                String decodedTextureUrl = TextUtils.decodeSkinTexture(skullTexture, true);
                if (decodedTextureUrl == null) return;

                Deployable flare = Deployable.getByTextureId(decodedTextureUrl);
                if (flare != null && flare.isInRadius(entityArmorStand.distanceToSqr(MC.player))) {
                    // Default exist time of flares
                    int seconds = 180;
                    // 1 tick = 50ms
                    seconds -= entityArmorStand.tickCount * 50 / 1000;

                    put(flare, seconds, entityArmorStand.getUUID());
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