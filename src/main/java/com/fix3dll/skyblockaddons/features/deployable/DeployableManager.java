package com.fix3dll.skyblockaddons.features.deployable;

import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
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
    private static final Pattern POWER_ORB_PATTERN = Pattern.compile("[A-Za-z ]* (?<seconds>[0-9]*)s");

    /** Entry displaying {@link Deployable#SOS_FLARE} at 90 seconds for the edit screen */
    public static ArmorStand DUMMY_ARMOR_STAND;
    public static DeployableEntry DUMMY_DEPLOYABLE_ENTRY;

    private final Map<Deployable, DeployableEntry> deployableEntryMap = new HashMap<>();

    static {
        DUMMY_ARMOR_STAND = new ArmorStand(EntityType.ARMOR_STAND, Minecraft.getInstance().level);
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
        if (entityArmorStand.getCustomName() != null) {
            Minecraft mc = Minecraft.getInstance();

            String customNameTag;
            if (entityArmorStand.getCustomName() != null) {
                customNameTag = entityArmorStand.getCustomName().getString();
            } else {
                return;
            }

            Deployable orb = Deployable.getByDisplayName(customNameTag);

            if (orb != null && orb.isInRadius(entityArmorStand.distanceToSqr(mc.player))) {
                Matcher matcher = POWER_ORB_PATTERN.matcher(customNameTag);

                if (matcher.matches()) {
                    int seconds;
                    try {
                        // Apparently they don't have a second count for moment after spawning, that's what this try-catch is for
                        seconds = Integer.parseInt(matcher.group("seconds"));
                    } catch (NumberFormatException ex) {
                        // It's okay, just don't add the deployable I guess...
                        return;
                    }

                    if (mc.level == null) return;
                    List<ArmorStand> surroundingArmorStands = mc.level.getEntitiesOfClass(
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
                if (flare != null && flare.isInRadius(entityArmorStand.distanceToSqr(Minecraft.getInstance().player))) {
                    // Default exist time of flares
                    int seconds = 180;
                    // 1 tick = 50ms
                    seconds -= entityArmorStand.tickCount * 50 / 1000;

                    put(flare, seconds, entityArmorStand.getUUID());
                }
            }
        }
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