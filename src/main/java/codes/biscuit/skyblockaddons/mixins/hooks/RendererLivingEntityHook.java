package codes.biscuit.skyblockaddons.mixins.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.features.dungeon.DungeonPlayer;
import codes.biscuit.skyblockaddons.features.outline.EntityOutlineRenderer;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;

import java.time.ZonedDateTime;
import java.util.Set;

// cough nothing to see here
public class RendererLivingEntityHook {

    // TODO: Convert this to UUIDs instead of names
    private static final Set<String> coolPeople = Sets.newHashSet("Dinnerbone", "Fix3dll");
    private static boolean isCoolPerson;

    public static boolean isCoolPerson(String string) {
        ZonedDateTime zdt = SkyblockAddons.getHypixelZonedDateTime();
        isCoolPerson = (zdt.getMonth().getValue() == 4 && zdt.getDayOfMonth() == 1) != coolPeople.contains(string);
        return isCoolPerson;
    }

    public static boolean isWearing(EntityPlayer entityPlayer, EnumPlayerModelParts p_175148_1_) {
        return isCoolPerson || entityPlayer.isWearing(p_175148_1_);
    }

    public static int setOutlineColor(EntityLivingBase entity, int originalColor) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY.isDisabled(FeatureSetting.CLASS_COLORED_TEAMMATE)
                && main.getUtils().isInDungeon()
                && main.getDungeonManager().getTeammates().containsKey(entity.getName())) {
            DungeonPlayer dungeonPlayer = main.getDungeonManager().getTeammates().get(entity.getName());

            if (dungeonPlayer.isCritical()) {
                return Minecraft.getMinecraft().fontRendererObj.getColorCode('c');
            } else if (dungeonPlayer.isLow()) {
                return Minecraft.getMinecraft().fontRendererObj.getColorCode('e');
            }
        } else {
            Integer i = EntityOutlineRenderer.getCustomOutlineColor(entity);
            if (i != null) {
                return i;
            }
        }
        return originalColor;
    }
}