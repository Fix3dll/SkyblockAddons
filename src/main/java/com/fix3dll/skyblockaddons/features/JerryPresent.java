package com.fix3dll.skyblockaddons.features;

import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class JerryPresent {

    private static final Pattern FROM_TO_PATTERN = Pattern.compile("(?:From:|To:) (?:\\[.*?] )?(?<name>\\w{1,16})");

    @Getter private static final Map<UUID, JerryPresent> jerryPresents = new HashMap<>();

    /** Armor stand with the present-colored skull */
    private final ArmorStand presentStand;
    /** Armor stand with "From: [RANK] Username" */
    private final ArmorStand fromLineStand;
    /** Armor stand with "CLICK TO OPEN" or "To: [RANK] Username" */
    private final ArmorStand toLineStand;

    private final boolean isForPlayer;
    private final boolean isFromPlayer;
    private final PresentType presentType;

    public JerryPresent(ArmorStand present, ArmorStand fromLine, ArmorStand toLine, PresentType color) {
        this.presentStand = present;
        this.fromLineStand = fromLine;
        this.toLineStand = toLine;

        Matcher matcher = FROM_TO_PATTERN.matcher(TextUtils.stripColor(fromLine.getCustomName().getString()));
        this.isFromPlayer = matcher.matches() && Minecraft.getInstance().player.getName().equals(matcher.group("name"));
        this.isForPlayer = "CLICK TO OPEN".equals(TextUtils.stripColor(toLine.getCustomName().getString()));
        this.presentType = color;
    }

    public boolean shouldHide() {
        return !isForPlayer && !isFromPlayer;
    }

    public static void detectJerryPresent(Entity targetEntity) {
        if (!(targetEntity instanceof ArmorStand) || !targetEntity.isInvisible() || Minecraft.getInstance().level == null) {
            return;
        }

        // Check if this present already exists...
        if (jerryPresents.containsKey(targetEntity.getUUID())) {
            return;
        }

        // Check a small range around...
        List<ArmorStand> stands = Minecraft.getInstance().level.getEntitiesOfClass(
                ArmorStand.class,
                new AABB(
                        targetEntity.getX() - 0.1, targetEntity.getY() - 2, targetEntity.getZ() - 0.1,
                        targetEntity.getX() + 0.1, targetEntity.getY() + 2, targetEntity.getZ() + 0.1
                )
        );

        ArmorStand present = null, fromLine = null, toLine = null;
        PresentType presentType = null;
        for (ArmorStand stand : stands) {
            if (stand.getCustomName() != null) {
                String name = TextUtils.stripColor(stand.getCustomName().getString());

                // From line (middle)
                if (name.startsWith("From: ")) {
                    fromLine = stand;

                    // To line (top)
                } else if (name.equals("CLICK TO OPEN") || name.startsWith("To: ")) {
                    toLine = stand;
                }

            } else {
                String skullID = ItemUtils.getSkullOwnerID(stand.getItemBySlot(EquipmentSlot.HEAD));
                if (skullID == null) {
                    continue;
                }

                PresentType standType = PresentType.fromSkullID(skullID);
                if (standType == null) {
                    continue;
                }

                // Present stand (bottom)
                present = stand;
                presentType = standType;
            }
        }

        // Verify that we've found all parts, and that the positions make sense
        if (present == null || fromLine == null || toLine == null || present.getY() > fromLine.getY() || fromLine.getY() > toLine.getY()) {
            return;
        }

        JerryPresent jerryPresent = new JerryPresent(present, fromLine, toLine, presentType);
        jerryPresents.put(present.getUUID(), jerryPresent);
        jerryPresents.put(fromLine.getUUID(), jerryPresent);
        jerryPresents.put(toLine.getUUID(), jerryPresent);
    }

    public enum PresentType {
        WHITE("3047a516-415b-3bf4-b597-b78fd2a9ccf4"),
        GREEN("5fa813c0-5519-30c4-a53c-955945e93e10"),
        RED("4afe5d71-918f-3741-a969-5f785c5b2945"),
        // Mayor Foxy
        PARTY("7709e502-6eb4-34f7-9566-264606689fb2"),
        // Century Raffle
        STRAWBERRY("3150a961-c8a0-3b97-90d2-0b46d58cf67a"),
        RED_VELVET("feda593e-3195-309a-a080-2a34f0499815"),
        GREEN_VELVET("be14aa93-494e-33c7-960f-7c22639e998e"),
        CHEESECAKE("10b96870-8c24-3c83-ae17-269eca761d11"),
        BLUEBERRY("d653d288-163c-36ac-ba4b-87a0cefa42b5");

        private final String skullID;

        PresentType(String skullID) {
            this.skullID = skullID;
        }

        public static PresentType fromSkullID(String skullID) {
            for (PresentType presentColor : PresentType.values()) {
                if (presentColor.skullID.equals(skullID)) {
                    return presentColor;
                }
            }

            return null;
        }
    }
}