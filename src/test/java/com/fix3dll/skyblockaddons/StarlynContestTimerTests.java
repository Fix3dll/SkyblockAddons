package com.fix3dll.skyblockaddons;

import com.fix3dll.skyblockaddons.features.starlyn.StarlynContestTimer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * Tests for {@link StarlynContestTimer} without a Minecraft client or live server.
 */
public class StarlynContestTimerTests {

    private static final long SKYBLOCK_EPOCH_MILLIS = 1559829300000L;
    private static final long CONTEST_MILLIS = 20 * 60 * 1000L;

    @DisplayName("The fixed SkyBlock epoch starts a new twenty-minute contest")
    @Test
    void testSkyblockEpoch() {
        Assertions.assertEquals(1, new MutableClocks(SKYBLOCK_EPOCH_MILLIS - 1).timer().getRemainingSeconds());
        Assertions.assertEquals(1200, new MutableClocks(SKYBLOCK_EPOCH_MILLIS).timer().getRemainingSeconds());
        Assertions.assertEquals(1200, new MutableClocks(SKYBLOCK_EPOCH_MILLIS + 1).timer().getRemainingSeconds());
        Assertions.assertEquals(1199, new MutableClocks(SKYBLOCK_EPOCH_MILLIS + 1000).timer().getRemainingSeconds());
    }

    @DisplayName("UTC :15, :35 and :55 reset the countdown")
    @ParameterizedTest
    @ValueSource(ints = {15, 35, 55})
    void testEveryHourlyReset(int minute) {
        long resetMillis = Instant.parse("2026-10-06T12:00:00Z").toEpochMilli() + minute * 60_000L;
        MutableClocks clocks = new MutableClocks(resetMillis - 1000);
        StarlynContestTimer timer = clocks.timer();

        Assertions.assertEquals(1, timer.getRemainingSeconds());
        clocks.advanceMillis(999);
        Assertions.assertEquals(1, timer.getRemainingSeconds());
        clocks.advanceMillis(1);
        Assertions.assertEquals(1200, timer.getRemainingSeconds());
        clocks.advanceMillis(1);
        Assertions.assertEquals(1200, timer.getRemainingSeconds());
        clocks.advanceMillis(999);
        Assertions.assertEquals(1199, timer.getRemainingSeconds());
    }

    @DisplayName("Timezone offsets with partial hours retain the same global reset phase")
    @ParameterizedTest
    @ValueSource(strings = {
            "2026-10-06T17:45:00+05:30",
            "2026-10-06T18:00:00+05:45",
            "2026-10-06T21:45:00+09:30",
            "2026-10-06T08:45:00-03:30"
    })
    void testPartialHourTimezoneOffsets(String localTime) {
        MutableClocks clocks = new MutableClocks(OffsetDateTime.parse(localTime).toInstant().toEpochMilli());
        StarlynContestTimer timer = clocks.timer();

        Assertions.assertEquals(1200, timer.getRemainingSeconds());
        clocks.advanceMillis(60_000);
        Assertions.assertEquals(1140, timer.getRemainingSeconds());
    }

    @DisplayName("Countdowns wrap repeatedly across days and many missed contests")
    @Test
    void testElapsedTimeLongerThanOneDay() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.timer();

        clocks.advanceMillis(36 * 60 * 60 * 1000L + 9 * CONTEST_MILLIS + 123_456);
        Assertions.assertEquals(1077, timer.getRemainingSeconds());
        clocks.advanceMillis(1_076_544);
        Assertions.assertEquals(1200, timer.getRemainingSeconds());
        clocks.advanceMillis(CONTEST_MILLIS * 3 + 1000);
        Assertions.assertEquals(1199, timer.getRemainingSeconds());
    }

    @DisplayName("Forward and backward wall-clock jumps cannot change an existing countdown")
    @Test
    void testWallClockJumps() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS + 5 * 60_000);
        StarlynContestTimer timer = clocks.timer();

        Assertions.assertEquals(900, timer.getRemainingSeconds());
        clocks.advanceMillis(1000);
        Assertions.assertEquals(899, timer.getRemainingSeconds());
        clocks.wallMillis += 2 * 24 * 60 * 60 * 1000L + 137_000;
        Assertions.assertEquals(899, timer.getRemainingSeconds());
        clocks.advanceMillis(10_000);
        Assertions.assertEquals(889, timer.getRemainingSeconds());
        clocks.wallMillis -= 5 * 24 * 60 * 60 * 1000L + 421_000;
        Assertions.assertEquals(889, timer.getRemainingSeconds());
        clocks.advanceMillis(500);
        Assertions.assertEquals(889, timer.getRemainingSeconds());
        clocks.advanceMillis(500);
        Assertions.assertEquals(888, timer.getRemainingSeconds());
    }

    @DisplayName("A late live reading corrects a deliberately wrong initial wall clock")
    @Test
    void testLateFirstLiveCorrection() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS + 19 * 60_000);
        StarlynContestTimer timer = clocks.timer();
        clocks.advanceMillis(45_000);
        Assertions.assertEquals(15, timer.getRemainingSeconds());

        Assertions.assertTrue(timer.observe(328));
        Assertions.assertEquals(328, timer.getRemainingSeconds());
        clocks.wallMillis -= 13 * CONTEST_MILLIS + 91_000;
        clocks.advanceMillis(3000);
        Assertions.assertEquals(325, timer.getRemainingSeconds());
    }

    @DisplayName("Repeated stale readings cannot freeze or rewind the local countdown")
    @Test
    void testUnchangedReadingsAreIgnored() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.timer();
        Assertions.assertTrue(timer.observe(328));

        for (int elapsedSeconds = 5; elapsedSeconds <= 25; elapsedSeconds += 5) {
            clocks.advanceMillis(5000);
            Assertions.assertFalse(timer.observe(328));
            Assertions.assertEquals(328 - elapsedSeconds, timer.getRemainingSeconds());
        }
    }

    @DisplayName("Fresh changed readings are accepted even when they extend the countdown")
    @Test
    void testChangedReadingsCorrectTheCountdown() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.timer();
        Assertions.assertTrue(timer.observe(328));
        clocks.advanceMillis(5000);

        Assertions.assertTrue(timer.observe(324));
        Assertions.assertEquals(324, timer.getRemainingSeconds());
        clocks.advanceMillis(1000);
        Assertions.assertFalse(timer.observe(324));
        Assertions.assertEquals(323, timer.getRemainingSeconds());
        Assertions.assertTrue(timer.observe(325));
        Assertions.assertEquals(325, timer.getRemainingSeconds());
    }

    @DisplayName("Stale scoreboard and tab readings are deduplicated independently")
    @Test
    void testStaleReadingsFromAlternatingSources() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.timer();
        Assertions.assertTrue(timer.observe(328, StarlynContestTimer.Source.SCOREBOARD));
        Assertions.assertTrue(timer.observe(327, StarlynContestTimer.Source.TAB_LIST));

        for (int elapsedSeconds = 1; elapsedSeconds <= 5; elapsedSeconds++) {
            clocks.advanceMillis(1000);
            Assertions.assertFalse(timer.observe(328, StarlynContestTimer.Source.SCOREBOARD));
            Assertions.assertFalse(timer.observe(327, StarlynContestTimer.Source.TAB_LIST));
            Assertions.assertEquals(327 - elapsedSeconds, timer.getRemainingSeconds());
        }
        Assertions.assertTrue(timer.observe(321, StarlynContestTimer.Source.SCOREBOARD));
        Assertions.assertEquals(321, timer.getRemainingSeconds());
    }

    @DisplayName("Zero means rollover into the next contest rather than a stuck zero")
    @Test
    void testZeroObservationAndRollover() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.timer();
        Assertions.assertTrue(timer.observe(1));
        clocks.advanceMillis(999);
        Assertions.assertEquals(1, timer.getRemainingSeconds());
        clocks.advanceMillis(1);
        Assertions.assertEquals(1200, timer.getRemainingSeconds());
        Assertions.assertFalse(timer.observe(1));

        Assertions.assertTrue(timer.observe(0));
        Assertions.assertEquals(1200, timer.getRemainingSeconds());
        clocks.advanceMillis(1000);
        Assertions.assertFalse(timer.observe(0));
        Assertions.assertEquals(1199, timer.getRemainingSeconds());
    }

    @DisplayName("The full twenty-minute reading is a valid live correction")
    @Test
    void testFullContestObservation() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS + 5 * 60_000);
        StarlynContestTimer timer = clocks.timer();
        Assertions.assertTrue(timer.observe(1200));
        Assertions.assertEquals(1200, timer.getRemainingSeconds());
        clocks.advanceMillis(1000);
        Assertions.assertFalse(timer.observe(1200));
        Assertions.assertEquals(1199, timer.getRemainingSeconds());
    }

    @DisplayName("Invalid server readings preserve both the countdown and deduplication state")
    @ParameterizedTest
    @ValueSource(ints = {-1, -60, 1201, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void testInvalidObservations(int invalidReading) {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.timer();
        Assertions.assertTrue(timer.observe(35));
        clocks.advanceMillis(3000);

        Assertions.assertFalse(timer.observe(invalidReading));
        Assertions.assertEquals(32, timer.getRemainingSeconds());
        Assertions.assertFalse(timer.observe(35));
        Assertions.assertEquals(32, timer.getRemainingSeconds());
    }

    @DisplayName("Warp or disconnect clears stale readings while preserving the countdown")
    @Test
    void testClearObservationPreservesPhaseAndAllowsCorrection() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.timer();
        Assertions.assertTrue(timer.observe(35, StarlynContestTimer.Source.SCOREBOARD));
        Assertions.assertTrue(timer.observe(35, StarlynContestTimer.Source.TAB_LIST));
        clocks.advanceMillis(5000);

        timer.clearObservation();
        Assertions.assertEquals(30, timer.getRemainingSeconds());
        clocks.advanceMillis(1000);
        Assertions.assertEquals(29, timer.getRemainingSeconds());
        Assertions.assertTrue(timer.observe(35, StarlynContestTimer.Source.SCOREBOARD));
        Assertions.assertEquals(35, timer.getRemainingSeconds());
        Assertions.assertTrue(timer.observe(35, StarlynContestTimer.Source.TAB_LIST));
        clocks.advanceMillis(1000);
        Assertions.assertFalse(timer.observe(35, StarlynContestTimer.Source.TAB_LIST));
        Assertions.assertEquals(34, timer.getRemainingSeconds());
    }

    @DisplayName("Missing or unrelated live timer data leaves the countdown running")
    @Test
    void testMissingLiveDataContinuesCountdown() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.timer();
        Assertions.assertTrue(timer.observe(35));
        clocks.advanceMillis(10_000);

        StarlynContestTimer.parseRemainingSeconds(List.of("Starlyn", "Miria's Contest 5m28s"), "Agatha")
                .ifPresent(timer::observe);
        StarlynContestTimer.parseRemainingSeconds(List.of(), "Agatha").ifPresent(timer::observe);
        Assertions.assertEquals(25, timer.getRemainingSeconds());
        clocks.advanceMillis(25_000);
        Assertions.assertEquals(1200, timer.getRemainingSeconds());
    }

    @DisplayName("Agatha and Miria maintain independent corrected countdowns")
    @Test
    void testIndependentContestInstances() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS + 200_000);
        StarlynContestTimer agatha = clocks.timer();
        StarlynContestTimer miria = clocks.timer();
        Assertions.assertTrue(agatha.observe(328));
        Assertions.assertEquals(1000, miria.getRemainingSeconds());
        clocks.advanceMillis(3000);

        Assertions.assertEquals(325, agatha.getRemainingSeconds());
        Assertions.assertEquals(997, miria.getRemainingSeconds());
        Assertions.assertTrue(miria.observe(35));
        Assertions.assertEquals(325, agatha.getRemainingSeconds());
        agatha.clearObservation();
        Assertions.assertTrue(agatha.observe(0));
        clocks.advanceMillis(1000);
        Assertions.assertEquals(1199, agatha.getRemainingSeconds());
        Assertions.assertEquals(34, miria.getRemainingSeconds());
    }

    @DisplayName("Parse named inline and nearby tab contest durations")
    @ParameterizedTest
    @MethodSource("validContestTimers")
    void testValidContestTimerParsing(String contestName, List<String> lines, int expectedSeconds) {
        Assertions.assertEquals(OptionalInt.of(expectedSeconds),
                StarlynContestTimer.parseRemainingSeconds(lines, contestName));
    }

    static Stream<Arguments> validContestTimers() {
        return Stream.of(
                Arguments.of("Agatha", List.of("Agatha's Contest 5m28s"), 328),
                Arguments.of("Miria", List.of("Miria's Contest 0m35s"), 35),
                Arguments.of("Agatha", List.of("Agatha's Contest: 05:28"), 328),
                Arguments.of("Miria", List.of("Miria's Contest 0:35"), 35),
                Arguments.of("Agatha", List.of("Agatha's Contest 5m 28s"), 328),
                Arguments.of("Agatha", List.of("Agatha's Contest 5m"), 300),
                Arguments.of("Agatha", List.of("Agatha's Contest 35s"), 35),
                Arguments.of("Agatha", List.of("Agatha's Contest 20m0s"), 1200),
                Arguments.of("Agatha", List.of("Agatha's Contest 19:59"), 1199),
                Arguments.of("Agatha", List.of("Agatha's Contest 0m0s"), 0),
                Arguments.of("Agatha", List.of("Agatha's Contest 00:00"), 0),
                Arguments.of("Agatha", List.of("§6Agatha's Contest §a5m§e28s§r"), 328),
                Arguments.of("Miria", List.of(" §dMiria's Contest: §f00:35§r "), 35),
                Arguments.of("Agatha", List.of("Agatha's Contest:", "Time Left: 5m 28s"), 328),
                Arguments.of("Miria", List.of("Miria's Contest:", "0m35s"), 35),
                Arguments.of("Agatha", List.of("Agatha's Contest:", "05:28"), 328),
                Arguments.of("Agatha", List.of("§6Agatha's Contest:", "§r", "§7Time Left: §a5m 28s"), 328),
                Arguments.of("Agatha", List.of("Agatha's Contest:", "", "5m28s"), 328),
                Arguments.of("Agatha", List.of("Miria's Contest 0m35s", "Agatha's Contest 5m28s"), 328),
                Arguments.of("Miria", List.of("Agatha's Contest 5m28s", "Miria's Contest 0m35s"), 35)
        );
    }

    @DisplayName("Ignore unrelated, malformed, out-of-range and distant contest data")
    @ParameterizedTest
    @MethodSource("invalidContestTimers")
    void testInvalidContestTimerParsing(List<String> lines) {
        Assertions.assertEquals(OptionalInt.empty(), StarlynContestTimer.parseRemainingSeconds(lines, "Agatha"));
    }

    static Stream<Arguments> invalidContestTimers() {
        return Stream.of(
                Arguments.of(List.of()),
                Arguments.of(List.of("Starlyn", "Time Left: 5m28s")),
                Arguments.of(List.of("Miria's Contest 5m28s")),
                Arguments.of(List.of("Next Agatha's Contest 5m28s")),
                Arguments.of(List.of("Agatha's Contests 5m28s")),
                Arguments.of(List.of("Agatha's Contestant 5m28s")),
                Arguments.of(List.of("Agatha's Contest")),
                Arguments.of(List.of("Agatha's Contest:")),
                Arguments.of(List.of("Agatha's Contest [328]")),
                Arguments.of(List.of("Agatha's Contest 328")),
                Arguments.of(List.of("Agatha's Contest 5m28s [328]")),
                Arguments.of(List.of("Agatha's Contest 21m")),
                Arguments.of(List.of("Agatha's Contest 20m1s")),
                Arguments.of(List.of("Agatha's Contest 21:00")),
                Arguments.of(List.of("Agatha's Contest 20:01")),
                Arguments.of(List.of("Agatha's Contest 5m60s")),
                Arguments.of(List.of("Agatha's Contest 5:60")),
                Arguments.of(List.of("Agatha's Contest 60s")),
                Arguments.of(List.of("Agatha's Contest -1m35s")),
                Arguments.of(List.of("Agatha's Contest 5m-1s")),
                Arguments.of(List.of("Agatha's Contest 1h")),
                Arguments.of(List.of("Agatha's Contest 5:2")),
                Arguments.of(List.of("Agatha's Contest 5m28s remaining")),
                Arguments.of(List.of("Agatha's Contest:", "[328]")),
                Arguments.of(List.of("Agatha's Contest:", "Points: 328", "Time Left: 5m28s")),
                Arguments.of(List.of("Agatha's Contest:", "Miria's Contest:", "Time Left: 0m35s")),
                Arguments.of(List.of("Agatha's Contest:", "", "", "Time Left: 5m28s")),
                Arguments.of(List.of("Agatha's Contest:", "Unrelated event", "Time Left: 5m28s"))
        );
    }

    private static class MutableClocks {

        private long wallMillis;
        // System.nanoTime has an arbitrary origin, so the test clock need not start at zero.
        private long monotonicNanos = 123_456_789L;

        private MutableClocks(long wallMillis) {
            this.wallMillis = wallMillis;
        }

        private StarlynContestTimer timer() {
            return new StarlynContestTimer(() -> wallMillis, () -> monotonicNanos);
        }

        private void advanceMillis(long elapsedMillis) {
            wallMillis += elapsedMillis;
            monotonicNanos += elapsedMillis * 1_000_000L;
        }
    }
}
