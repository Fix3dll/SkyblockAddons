package codes.biscuit.skyblockaddons.features.deployables;

import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ReportedException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for managing active Deployable around the player.
 * {@link #put(Deployable, int, UUID) Insert} deployable that get detected and {@link #getActiveDeployable() get} the
 * active deployable with the highest priority (enum ordinal).
 *
 * @author DidiSkywalker
 */
public class DeployableManager {
    private static final Pattern TEXTURE_URL_PATTERN = Pattern.compile("\\\"url\\\"\\s?:\\s?\\\".+/(?<textureId>\\w+)\\\"");
    private static final Pattern POWER_ORB_PATTERN = Pattern.compile("[A-Za-z ]* (?<seconds>[0-9]*)s");

    /** The DeployableManager instance. */
    @Getter private static final DeployableManager instance = new DeployableManager();

    /**
     * Entry displaying {@link Deployable#RADIANT} at 20 seconds for the edit screen
     */
    public static final DeployableEntry DUMMY_POWER_ORB_ENTRY = new DeployableEntry(Deployable.SOS_FLARE, 90, null);

    private Map<Deployable, DeployableEntry> deployableEntryMap = new HashMap<>();

    /**
     * Put any detected deployable into the list of active deployables.
     *
     * @param deployable Detected Deployable type
     * @param seconds Seconds the deployable has left before running out
     */
    private void put(Deployable deployable, int seconds, UUID uuid) {
        deployableEntryMap.put(deployable, new DeployableEntry(deployable, seconds, uuid));
    }

    /**
     * Get the active deployable with the highest priority. Priority is based on enum value's ordinal
     * and the returned deployable is guaranteed to have been active at least 100ms ago.
     *
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
     *
     * @param entityArmorStand The entity to detect whether it is a deployable or not.
     */
    public void detectDeployables(EntityArmorStand entityArmorStand) {
        if (entityArmorStand.hasCustomName()) {
            String customNameTag = entityArmorStand.getCustomNameTag();
            Deployable orb = Deployable.getByDisplayname(customNameTag);

            if (orb != null && orb.isInRadius(entityArmorStand.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer))) {
                Matcher matcher = POWER_ORB_PATTERN.matcher(TextUtils.stripColor(customNameTag));

                if (matcher.matches()) {
                    int seconds;
                    try {
                        // Apparently they don't have a second count for moment after spawning, that's what this try-catch is for
                        seconds = Integer.parseInt(matcher.group("seconds"));
                    } catch (NumberFormatException ex) {
                        // It's okay, just don't add the deployable I guess...
                        return;
                    }

                    List<EntityArmorStand> surroundingArmorStands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(
                            EntityArmorStand.class,
                            new AxisAlignedBB(
                                    entityArmorStand.posX - 0.1,
                                    entityArmorStand.posY - 3,
                                    entityArmorStand.posZ - 0.1,
                                    entityArmorStand.posX + 0.1,
                                    entityArmorStand.posY,
                                    entityArmorStand.posZ + 0.1
                            )
                    );
                    if (!surroundingArmorStands.isEmpty()) {
                        EntityArmorStand orbArmorStand = null;

                        for (EntityArmorStand surroundingArmorStand : surroundingArmorStands) {
                            ItemStack helmet = surroundingArmorStand.getCurrentArmor(3);
                            if (helmet != null) {
                                orbArmorStand = surroundingArmorStand;
                            }
                        }

                        put(orb, seconds, orbArmorStand == null ? null : orbArmorStand.getUniqueID());
                    }
                }
            }
        } else {
            // Flare detection
            // TODO optimize
            if (entityArmorStand.isInvisible()) {
                // we need skull on head
                if (entityArmorStand.getCurrentArmor(3) == null)
                    return;

                String textureBase64;
                try {
                    textureBase64 = entityArmorStand.getCurrentArmor(3).getSubCompound("SkullOwner", false)
                            .getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0)
                            .getString("Value");
                } catch (NullPointerException|ReportedException ex) {
                    // entity head texture is empty
                    return;
                }

                String decodedTextureUrl =  new String(
                        Base64.getDecoder().decode(
                                // Getting before '=' to avoid IllegalArgumentException. No padding needed
                                textureBase64.contains("=")
                                        ? textureBase64.substring(0, textureBase64.indexOf('='))
                                        : textureBase64
                        ),
                        StandardCharsets.UTF_8
                );

                Deployable flare;
                Matcher matcher = TEXTURE_URL_PATTERN.matcher(decodedTextureUrl);
                if (matcher.find()) {
                    flare = Deployable.getByTextureId(matcher.group("textureId"));
                } else
                    return;

                // Default exist time of flares
                int seconds = 180;

                if (flare != null && flare.isInRadius(entityArmorStand.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer))) {
                    // 1 tick = 50ms
                    seconds -= entityArmorStand.ticksExisted * 50 / 1000;

                    put(flare, seconds, entityArmorStand.getUniqueID() == null ? null : entityArmorStand.getUniqueID());
                }
            }
        }
    }

    public EntityArmorStand createVirtualArmorStand(EntityArmorStand armorStandToClone) {
        EntityArmorStand virtualArmorStand = new EntityArmorStand(Utils.getDummyWorld());

        virtualArmorStand.setCurrentItemOrArmor(4, armorStandToClone.getEquipmentInSlot(4));

        return virtualArmorStand;
    }

    @Getter @RequiredArgsConstructor
    public static class DeployableEntry {
        /** The Deployable type. */
        private final Deployable deployable;

        /** Seconds the deployable has left before running out */
        private final int seconds;

        private final long timestamp = System.currentTimeMillis();

        private final UUID uuid;
    }
}
