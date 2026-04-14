package com.fix3dll.skyblockaddons.features.spooky;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.Regex;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;

// TODO: Feature Rewrite
public class SpookyEventManager {

    private static final Logger LOGGER = SkyblockAddons.getLogger();
    @Getter private static final Object2IntMap<CandyType> dummyCandyCounts = Object2IntMap.ofEntries(
            Object2IntMap.entry(CandyType.GREEN, 12),
            Object2IntMap.entry(CandyType.PURPLE, 34)
    );
    @Getter private static final Object2IntMap<CandyType> candyCounts = new Object2IntOpenHashMap<>();
    @Getter private static int points;

    public static void reset() {
        for (CandyType candyType : CandyType.values()) {
            candyCounts.put(candyType, 0);
        }
        points = 0;
    }

    public static boolean isActive() {
        return candyCounts.getInt(CandyType.GREEN) != 0 || candyCounts.getInt(CandyType.PURPLE) != 0;
    }

    public static void update(String strippedTabFooterString) {
        if (strippedTabFooterString == null) {
            reset();
            return;
        }

        try {
            Matcher matcher = Regex.CANDY_PATTERN.matcher(strippedTabFooterString);
            if (matcher.find()) {
                candyCounts.put(CandyType.GREEN, Integer.parseInt(matcher.group("green").replace(",", "")));
                candyCounts.put(CandyType.PURPLE, Integer.parseInt(matcher.group("purple").replace(",", "")));
                points = Integer.parseInt(matcher.group("points").replace(",", ""));
            }
        } catch (Exception ex) {
            LOGGER.error("An error occurred while parsing the spooky event event text in the tab list!", ex);
        }
    }

    /**
     * Temp function until feature re-write
     * @param green
     * @param purple
     * @param pts
     */
    public static void update(int green, int purple, int pts) {
        candyCounts.put(CandyType.GREEN, green);
        candyCounts.put(CandyType.PURPLE, purple);
        points = pts;
    }

}