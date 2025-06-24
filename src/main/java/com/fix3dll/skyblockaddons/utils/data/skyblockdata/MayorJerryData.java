package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.SkyblockMayor;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter @Setter @ToString
public class MayorJerryData {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    private static final long ONE_MINUTE = 60 * 1000;
    private static final Pattern DATE_PATTERN = Pattern.compile("Next set of perks in (?:(?<hours>\\d+)h)?(?: ?(?<minutes>\\d+)m)?(?: ?(?<seconds>\\d+)s)?!");

    private Long nextSwitch = 0L;
    private SkyblockMayor mayor = null;

    public boolean hasMayorAndActive() {
        return mayor != null && nextSwitch >= System.currentTimeMillis();
    }

    public void parseMayorJerryPerkpocalypse(ItemStack mayorItem) {
        if (mayorItem == null) {
            return; // silently return
        }

        List<String> loreList = ItemUtils.getItemLore(mayorItem);

        boolean perkpocalypsePerksFound = false;
        boolean perksStartFlag = false;
        for (String line : loreList) {
            String stripped = TextUtils.stripColor(line);
            if (!perksStartFlag && stripped.isEmpty()) continue;

            if (!perkpocalypsePerksFound && stripped.contains("Perkpocalypse Perks:")) {
                perkpocalypsePerksFound = true;
            } else if (!perkpocalypsePerksFound) {
                continue;
            }

            if (line.contains("§m")) {
                perksStartFlag = !perksStartFlag;
            } else if (perksStartFlag) {
                String resetStripped = TextUtils.stripResets(line);
                if (!resetStripped.startsWith("§7")) {
                    this.mayor = SkyblockMayor.getByPerkName(stripped);
                    if (this.mayor != null) continue;
                }
            }

            if (!perksStartFlag) {
                parseNextSwitch(stripped);
            }
        }

        if (Feature.DEVELOPER_MODE.isEnabled()) {
            LOGGER.info(this);
        }
    }

    private void parseNextSwitch(String strippedLine) {
        Matcher matcher = DATE_PATTERN.matcher(strippedLine);
        if (!matcher.matches()) return;

        try {
            int delayMs = 0;
            String hours = matcher.group("hours");
            if (!StringUtil.isNullOrEmpty(hours)) {
                delayMs += Integer.parseInt(hours) * 60 * 60 * 1000;
            }
            String minutes = matcher.group("minutes");
            if (!StringUtil.isNullOrEmpty(minutes)) {
                delayMs += Integer.parseInt(minutes) * 60 * 1000;
            }
            String seconds = matcher.group("seconds");
            if (!StringUtil.isNullOrEmpty(seconds)) {
                delayMs += Integer.parseInt(seconds) * 1000;
            }
            // round up to the next minute
            this.nextSwitch = ((System.currentTimeMillis() + delayMs + ONE_MINUTE - 1) / ONE_MINUTE) * ONE_MINUTE;
        } catch (IllegalStateException | IllegalArgumentException ignored) {
        }
    }
}