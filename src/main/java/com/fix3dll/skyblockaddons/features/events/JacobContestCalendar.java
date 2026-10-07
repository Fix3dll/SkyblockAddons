package com.fix3dll.skyblockaddons.features.events;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads received monthly calendar pages. Unknown or incomplete crop data is never a match. */
public class JacobContestCalendar {
    private static final List<String> MONTHS = List.of("Early Spring", "Spring", "Late Spring", "Early Summer", "Summer",
            "Late Summer", "Early Autumn", "Autumn", "Late Autumn", "Early Winter", "Winter", "Late Winter");
    private static final Pattern PAGE = Pattern.compile("^(.+), Year (\\d{1,6})$");
    private static final Pattern DAY = Pattern.compile("^Day (\\d{1,2})(?:st|nd|rd|th)?$");
    private static final Pattern CROP = Pattern.compile("^[○\\uE051]\\s+(.+)$");
    private static final Pattern FORMATTING = Pattern.compile("§[0-9a-fk-or]", Pattern.CASE_INSENSITIVE);

    public record DayEntry(String name, List<String> lore) {}

    public static boolean isCalendarPage(String title) {
        Matcher page = PAGE.matcher(clean(title));
        return page.matches() && MONTHS.contains(page.group(1));
    }

    public static Map<Long, Set<JacobCrop>> parsePage(String title, List<DayEntry> items) {
        Map<Long, Set<JacobCrop>> result = new HashMap<>();
        Matcher page = PAGE.matcher(clean(title));
        if (!page.matches() || items == null) return result;
        int month = MONTHS.indexOf(page.group(1));
        int year = Integer.parseInt(page.group(2));
        if (month < 0 || year < 1) return result;
        for (DayEntry item : items) {
            if (item == null || item.lore() == null) continue;
            Matcher day = DAY.matcher(clean(item.name()));
            if (!day.matches()) continue;
            int dayNumber = Integer.parseInt(day.group(1));
            if (dayNumber < 1 || dayNumber > 31) continue;
            long headers = item.lore().stream().map(JacobContestCalendar::clean)
                    .filter(line -> line.equals("Jacob's Farming Contest") || line.equals("Jacob's Farming Contest:")).count();
            if (headers != 1) continue;
            boolean contest = false;
            boolean invalid = false;
            Set<JacobCrop> crops = EnumSet.noneOf(JacobCrop.class);
            for (String raw : item.lore()) {
                String line = clean(raw);
                if (line.equals("Jacob's Farming Contest") || line.equals("Jacob's Farming Contest:")) {
                    if (contest) invalid = true;
                    contest = true;
                    continue;
                }
                if (!contest) continue;
                Matcher crop = CROP.matcher(line);
                if (crop.matches()) {
                    var parsed = JacobCrop.fromName(crop.group(1));
                    if (parsed.isEmpty() || !crops.add(parsed.get())) invalid = true;
                } else if (!line.isEmpty()) {
                    break;
                }
            }
            if (contest && !invalid && crops.size() == 3) {
                // Year indexing belongs with this epoch: SkyBlock year 1 is epoch + one year.
                long start = 1559829300000L + year * 446400000L + month * 37200000L + (dayNumber - 1) * 1200000L;
                Set<JacobCrop> old = result.putIfAbsent(start, crops);
                if (old != null && !old.equals(crops)) result.put(start, Set.of());
            }
        }
        result.entrySet().removeIf(entry -> entry.getValue().size() != 3);
        return result;
    }

    public static Set<JacobCrop> matchingCrops(Set<JacobCrop> scheduled, Set<JacobCrop> selected) {
        Set<JacobCrop> result = EnumSet.noneOf(JacobCrop.class);
        if (scheduled == null || selected == null || scheduled.size() != 3) return result;
        for (JacobCrop crop : scheduled) if (crop == null) return result;
        for (JacobCrop crop : selected) if (crop != null && scheduled.contains(crop)) result.add(crop);
        return result;
    }

    private static String clean(String text) {
        return text == null ? "" : FORMATTING.matcher(text).replaceAll("").trim();
    }
}
