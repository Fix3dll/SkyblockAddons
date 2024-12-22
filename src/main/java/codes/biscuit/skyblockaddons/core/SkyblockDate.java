package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Getter;

import java.util.regex.Matcher;

/**
 * This class represents a date (excluding the year) and time in Skyblock.
 * <p>
 * <p>Examples:</p>
 * <p>Spring 28th</p>
 * <p>5:20pm ☀</p>
 * <p>
 * <p>Spring 28th</p>
 * <p>9:10pm ☽</p>
 */
@Getter
public class SkyblockDate {

    public static SkyblockDate parse(Matcher dateMatcher) {
        if(dateMatcher == null) {
            return null;
        }

        int day = Integer.parseInt(dateMatcher.group("day"));
        String month = dateMatcher.group("month");

        return new SkyblockDate(SkyblockMonth.fromName(month), day);
    }

    public static SkyblockDate parse(Matcher dateMatcher, Matcher timeMatcher) {
        if(dateMatcher == null || timeMatcher == null) {
            return null;
        }

        int day = Integer.parseInt(dateMatcher.group("day"));
        int hour = Integer.parseInt(timeMatcher.group("hour"));
        int minute = Integer.parseInt(timeMatcher.group("minute"));
        String month = dateMatcher.group("month");
        String period = timeMatcher.group("period");

        return new SkyblockDate(SkyblockMonth.fromName(month), day, hour, minute, period);
    }

    private final SkyblockMonth month;
    private final int day;
    private final int hour;
    private final int minute;
    private final String period;

    public SkyblockDate(SkyblockMonth month, int day) {
        this.month = month;
        this.day = day;
        this.hour = -1;
        this.minute = -1;
        this.period = "";
    }

    public SkyblockDate(SkyblockMonth month, int day, int hour, int minute, String period) {
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.period = period;
    }

    /**
     * All the months of the Skyblock calendar
     */
    public enum SkyblockMonth {
        EARLY_WINTER("Early Winter"),
        WINTER("Winter"),
        LATE_WINTER("Late Winter"),
        EARLY_SPRING("Early Spring"),
        SPRING("Spring"),
        LATE_SPRING("Late Spring"),
        EARLY_SUMMER("Early Summer"),
        SUMMER("Summer"),
        LATE_SUMMER("Late Summer"),
        EARLY_AUTUMN("Early Autumn"),
        AUTUMN("Autumn"),
        LATE_AUTUMN("Late Autumn");

        final String scoreboardString;

        SkyblockMonth(String scoreboardString) {
            this.scoreboardString = scoreboardString;
        }

        /**
         * Returns the {@code SkyblockMonth} value with the given name.
         *
         * @param scoreboardName the name of the month as it appears on the scoreboard
         * @return the {@code SkyblockMonth} value with the given name or {@code null} if a value with the given name
         * isn't found
         */
        public static SkyblockMonth fromName(String scoreboardName) {
            for (SkyblockMonth skyblockMonth : values()) {
                if(skyblockMonth.scoreboardString.equals(scoreboardName)) {
                    return skyblockMonth;
                }
            }
            return null;
        }
    }

    /**
     * Returns this Skyblock date as a String in the format:
     * Month Day, hh:mm
     *
     * @return this Skyblock date as a formatted String
     */
    @Override
    public String toString() {
        String monthName;

        if (month != null) {
            monthName = month.scoreboardString;
        }
        else {
            monthName = null;
        }

        if (hour == -1 || minute == -1)
            return String.format("%s %s",
                    monthName,
                    day + TextUtils.getOrdinalSuffix(day));
        else
            return String.format("%s %s, %d:%s%s",
                    monthName,
                    day + TextUtils.getOrdinalSuffix(day),
                    hour,
                    minute == 0 ? "00" : minute,
                    period);
    }
}
