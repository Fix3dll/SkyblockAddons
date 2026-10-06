package com.fix3dll.skyblockaddons.features.starlyn;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A repeating SkyBlock-day countdown, optionally corrected by a received contest timer. */
public class StarlynContestTimer {

    private static final long SKYBLOCK_EPOCH_MILLIS = 1559829300000L;
    private static final long CONTEST_MILLIS = 20 * 60 * 1000L;
    private static final Pattern FORMATTING = Pattern.compile("§[0-9a-fk-or]", Pattern.CASE_INSENSITIVE);
    private static final Pattern DURATION = Pattern.compile(
            "^(?:Time Left:|Ends in:|Ending in:)?\\s*(?:(?<minutes>\\d{1,2})m\\s*)?(?:(?<seconds>\\d{1,2})s)?$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern CLOCK = Pattern.compile("^(?<minutes>\\d{1,2}):(?<seconds>\\d{2})$");

    private final LongSupplier monotonicNanos;
    private long anchorNanos;
    private long elapsedAtAnchorMillis;
    private long cycleAtAnchor;
    private boolean synchronizedOnce;
    public enum Source { SCOREBOARD, TAB_LIST }

    private final int[] lastObservedSeconds = {-1, -1};

    public StarlynContestTimer(LongSupplier wallMillis, LongSupplier monotonicNanos) {
        this.monotonicNanos = monotonicNanos;
        anchorNanos = monotonicNanos.getAsLong();
        // SkyBlock midnight occurs at :15, :35 and :55, independent of the client's timezone.
        long elapsed = wallMillis.getAsLong() - SKYBLOCK_EPOCH_MILLIS;
        elapsedAtAnchorMillis = Math.floorMod(elapsed, CONTEST_MILLIS);
        cycleAtAnchor = Math.floorDiv(elapsed, CONTEST_MILLIS);
    }

    public record Snapshot(long eventId, int remainingSeconds) {}

    public Snapshot getSnapshot() {
        long elapsedMillis = Math.max(0, (monotonicNanos.getAsLong() - anchorNanos) / 1_000_000L);
        long elapsed = elapsedAtAnchorMillis + elapsedMillis;
        long remainingMillis = CONTEST_MILLIS - elapsed % CONTEST_MILLIS;
        return new Snapshot(cycleAtAnchor + elapsed / CONTEST_MILLIS, (int) ((remainingMillis + 999) / 1000));
    }

    public int getRemainingSeconds() {
        return getSnapshot().remainingSeconds();
    }

    public long getEventId() {
        return getSnapshot().eventId();
    }

    public boolean observe(int remainingSeconds) {
        return observe(remainingSeconds, Source.SCOREBOARD);
    }

    public boolean observe(int remainingSeconds, Source source) {
        if (remainingSeconds < 0 || remainingSeconds > CONTEST_MILLIS / 1000
                || remainingSeconds == lastObservedSeconds[source.ordinal()]) {
            return false;
        }
        Snapshot snapshot = getSnapshot();
        long currentCycle = snapshot.eventId();
        int previousRemaining = snapshot.remainingSeconds();
        // Old end-of-contest packets may arrive just after a confirmed reset from another source.
        if (synchronizedOnce && previousRemaining >= 1170 && remainingSeconds > 0 && remainingSeconds <= 30) return false;
        // A received rollover can precede the locally predicted boundary. Small corrections keep the same identity.
        if (previousRemaining <= 30 && (remainingSeconds == 0 || remainingSeconds >= 1170)) currentCycle++;
        cycleAtAnchor = currentCycle;
        synchronizedOnce = true;
        lastObservedSeconds[source.ordinal()] = remainingSeconds;
        elapsedAtAnchorMillis = Math.floorMod(CONTEST_MILLIS - remainingSeconds * 1000L, CONTEST_MILLIS);
        anchorNanos = monotonicNanos.getAsLong();
        return true;
    }

    /** Forget stale packet values after changing worlds, while retaining the corrected clock. */
    public void clearObservation() {
        lastObservedSeconds[0] = lastObservedSeconds[1] = -1;
    }

    public static OptionalInt parseRemainingSeconds(List<String> lines, String contestName) {
        if (lines == null) return OptionalInt.empty();
        String header = contestName + "'s Contest";
        for (int i = 0; i < lines.size(); i++) {
            String line = clean(lines.get(i));
            if (!line.startsWith(header)) continue;
            String suffix = line.substring(header.length());
            if (!suffix.isEmpty() && !suffix.startsWith(":") && !suffix.startsWith(" ")) continue;
            suffix = suffix.replaceFirst("^:", "").trim();
            if (!suffix.isEmpty()) {
                OptionalInt remaining = parseDuration(suffix);
                if (remaining.isPresent()) return remaining;
                continue;
            }
            // A tab widget may put the countdown immediately below its named header.
            for (int j = i + 1; j < lines.size() && j <= i + 2; j++) {
                String following = clean(lines.get(j));
                if (following.isEmpty()) continue;
                OptionalInt remaining = parseDuration(following);
                if (remaining.isPresent()) return remaining;
                break;
            }
        }
        return OptionalInt.empty();
    }

    private static String clean(String line) {
        return line == null ? "" : FORMATTING.matcher(line).replaceAll("").trim();
    }

    private static OptionalInt parseDuration(String text) {
        Matcher matcher = CLOCK.matcher(text);
        boolean clock = matcher.matches();
        if (!clock) {
            matcher = DURATION.matcher(text);
            if (!matcher.matches() || (matcher.group("minutes") == null && matcher.group("seconds") == null)) {
                return OptionalInt.empty();
            }
        }
        int minutes = matcher.group("minutes") == null ? 0 : Integer.parseInt(matcher.group("minutes"));
        int seconds = matcher.group("seconds") == null ? 0 : Integer.parseInt(matcher.group("seconds"));
        int total = minutes * 60 + seconds;
        return seconds < 60 && total <= CONTEST_MILLIS / 1000 ? OptionalInt.of(total) : OptionalInt.empty();
    }
}
