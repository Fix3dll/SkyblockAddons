package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class JerryPresent {

    private static final Pattern FROM_TO_PATTERN = Pattern.compile("(?:From:|To:) (?:\\[.*?] )?(?<name>\\w{1,16})");

    @Getter private static final HashMap<UUID, JerryPresent> jerryPresents = new HashMap<>();

    /** Armor stand with the present-colored skull */
    private final EntityArmorStand presentStand;
    /** Armor stand with "From: [RANK] Username" */
    private final EntityArmorStand fromLineStand;
    /** Armor stand with "CLICK TO OPEN" or "To: [RANK] Username" */
    private final EntityArmorStand toLineStand;

    private final boolean isForPlayer;
    private final boolean isFromPlayer;
    private final PresentType presentType;

    public JerryPresent(EntityArmorStand present, EntityArmorStand fromLine, EntityArmorStand toLine, PresentType color) {
        this.presentStand = present;
        this.fromLineStand = fromLine;
        this.toLineStand = toLine;

        Matcher matcher = FROM_TO_PATTERN.matcher(TextUtils.stripColor(fromLine.getCustomNameTag()));
        this.isFromPlayer = matcher.matches() && Minecraft.getMinecraft().thePlayer.getName().equals(matcher.group("name"));
        this.isForPlayer = "CLICK TO OPEN".equals(TextUtils.stripColor(toLine.getCustomNameTag()));
        this.presentType = color;
    }

    public boolean shouldHide() {
        return !isForPlayer && !isFromPlayer;
    }

    public static void detectJerryPresent(Entity targetEntity) {
        if (!(targetEntity instanceof EntityArmorStand) || !targetEntity.isInvisible()) {
            return;
        }

        // Check if this present already exists...
        if (jerryPresents.containsKey(targetEntity.getUniqueID())) {
            return;
        }

        // Check a small range around...
        List<EntityArmorStand> stands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(
                EntityArmorStand.class,
                new AxisAlignedBB(
                        targetEntity.posX - 0.1, targetEntity.posY - 2, targetEntity.posZ - 0.1,
                        targetEntity.posX + 0.1, targetEntity.posY + 2, targetEntity.posZ + 0.1
                )
        );

        EntityArmorStand present = null, fromLine = null, toLine = null;
        PresentType presentType = null;
        for (EntityArmorStand stand : stands) {
            if (stand.hasCustomName()) {
                String name = TextUtils.stripColor(stand.getCustomNameTag());

                // From line (middle)
                if (name.startsWith("From: ")) {
                    fromLine = stand;

                // To line (top)
                } else if (name.equals("CLICK TO OPEN") || name.startsWith("To: ")) {
                    toLine = stand;
                }

            } else {
                String skullID = ItemUtils.getSkullOwnerID(stand.getEquipmentInSlot(4));
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
        if (present == null || fromLine == null || toLine == null || present.posY > fromLine.posY || fromLine.posY > toLine.posY) {
            return;
        }

        JerryPresent jerryPresent = new JerryPresent(present, fromLine, toLine, presentType);
        jerryPresents.put(present.getUniqueID(), jerryPresent);
        jerryPresents.put(fromLine.getUniqueID(), jerryPresent);
        jerryPresents.put(toLine.getUniqueID(), jerryPresent);
    }

    @Override
    public String toString() {
        return "JerryPresent{" +
                "presentStand='" + presentStand + '\'' +
                ", fromLineStand='" + fromLineStand + '\'' +
                ", toLineStand='" + toLineStand + '\'' +
                ", isFromPlayer=" + isFromPlayer +
                ", isForPlayer=" + isForPlayer +
                ", presentColor=" + presentType +
                '}';
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