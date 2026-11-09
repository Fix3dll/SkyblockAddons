package com.fix3dll.skyblockaddons;

import com.fix3dll.skyblockaddons.features.events.EventReminderClock;
import com.fix3dll.skyblockaddons.features.events.JacobContestCalendar;
import com.fix3dll.skyblockaddons.features.events.JacobCrop;
import com.fix3dll.skyblockaddons.features.events.ReminderQueue;
import com.fix3dll.skyblockaddons.features.events.ReminderTrigger;
import com.fix3dll.skyblockaddons.features.starlyn.StarlynContestTimer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/** Pure schedule, eligibility, deduplication and warning-queue regressions. */
public class EventReminderTests {

    private static final long SKYBLOCK_EPOCH_MILLIS = 1559829300000L;
    private static final String JACOB_HEADER = "Jacob's Farming Contest";
    private static final Set<JacobCrop> CROPS = Set.of(JacobCrop.WHEAT, JacobCrop.MELON, JacobCrop.COCOA_BEANS);

    @DisplayName("A reminder fires on the first eligible sample inside its inclusive lead window")
    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 30})
    void testReminderThreshold(int leadMinutes) {
        ReminderTrigger trigger = new ReminderTrigger();
        long event = millis("2026-10-06T12:15:00Z");
        Assertions.assertFalse(trigger.shouldNotify(event, leadMinutes * 60 + 1, leadMinutes, true, true));
        Assertions.assertTrue(trigger.shouldNotify(event, leadMinutes * 60, leadMinutes, true, true));
        Assertions.assertFalse(trigger.shouldNotify(event, leadMinutes * 60 - 1, leadMinutes, true, true));
    }

    @DisplayName("Joining inside the lead window does not require witnessing the threshold crossing")
    @Test
    void testFirstSampleAlreadyInsideWindow() {
        ReminderTrigger trigger = new ReminderTrigger();
        Assertions.assertTrue(trigger.shouldNotify(millis("2026-10-06T12:55:00Z"), 17, 5, true, true));
    }

    @DisplayName("Suppressed or ended samples do not consume a future notification")
    @Test
    void testSuppressedSamplesDoNotConsumeIdentity() {
        ReminderTrigger trigger = new ReminderTrigger();
        long event = millis("2026-10-06T12:15:00Z");
        Assertions.assertFalse(trigger.shouldNotify(event, 60, 1, false, true));
        Assertions.assertFalse(trigger.shouldNotify(event, 59, 1, true, false));
        Assertions.assertFalse(trigger.shouldNotify(event, 0, 1, true, true));
        Assertions.assertFalse(trigger.shouldNotify(event, -1, 1, true, true));
        Assertions.assertFalse(trigger.shouldNotify(event, 30, 0, true, true));
        Assertions.assertFalse(trigger.shouldNotify(event, 30, -1, true, true));
        Assertions.assertTrue(trigger.shouldNotify(event, 29, 1, true, true));
    }

    @DisplayName("Corrections and disable or eligibility changes cannot notify the same event twice")
    @Test
    void testEventDedupSurvivesCorrectionsAndWorldEligibility() {
        ReminderTrigger trigger = new ReminderTrigger();
        long event = millis("2026-10-06T12:15:00Z");
        Assertions.assertTrue(trigger.shouldNotify(event, 60, 1, true, true));
        Assertions.assertFalse(trigger.shouldNotify(event, 61, 1, true, true));
        Assertions.assertFalse(trigger.shouldNotify(event, 59, 1, false, true));
        Assertions.assertFalse(trigger.shouldNotify(event, 58, 1, true, false));
        Assertions.assertFalse(trigger.shouldNotify(event, 57, 1, true, true));
        Assertions.assertFalse(trigger.shouldNotify(event, 60, 1, true, true));
        Assertions.assertTrue(trigger.shouldNotify(millis("2026-10-06T13:15:00Z"), 60, 1, true, true));
    }

    @DisplayName("Revisited or backward event identities stay suppressed")
    @Test
    void testOlderEventsCannotRearmTheTrigger() {
        ReminderTrigger trigger = new ReminderTrigger();
        long latest = millis("2026-10-06T13:15:00Z");
        Assertions.assertTrue(trigger.shouldNotify(latest, 60, 1, true, true));
        Assertions.assertFalse(trigger.shouldNotify(latest - 3_600_000L, 60, 1, true, true));
        Assertions.assertFalse(trigger.shouldNotify(latest, 59, 1, true, true));
        Assertions.assertTrue(trigger.shouldNotify(latest + 3_600_000L, 60, 1, true, true));
    }

    @DisplayName("Epoch time advances from a monotonic clock despite wall-clock jumps")
    @Test
    void testMonotonicClockIgnoresWallClockChanges() {
        MutableClocks clocks = new MutableClocks(millis("2026-10-06T12:14:00Z"));
        EventReminderClock clock = clocks.reminderClock();
        long initial = clock.nowMillis();
        clocks.advanceMillis(45_000);
        Assertions.assertEquals(initial + 45_000, clock.nowMillis());
        clocks.wallMillis += 2 * 86_400_000L + 137_000;
        Assertions.assertEquals(initial + 45_000, clock.nowMillis());
        clocks.advanceMillis(1000);
        clocks.wallMillis -= 5 * 86_400_000L + 421_000;
        Assertions.assertEquals(initial + 46_000, clock.nowMillis());
        Assertions.assertEquals(millis("2026-10-06T12:15:00Z"),
                EventReminderClock.nextHourlyEvent(clock.nowMillis(), 15));
    }

    @DisplayName("The monotonic clock retains subsecond precision up to its millisecond API")
    @Test
    void testMonotonicClockMillisecondBoundary() {
        MutableClocks clocks = new MutableClocks(millis("2026-10-06T12:14:59.999Z"));
        clocks.nanos = -9_000_000_000L;
        EventReminderClock clock = clocks.reminderClock();
        long initial = clock.nowMillis();
        clocks.nanos += 999_999;
        Assertions.assertEquals(initial, clock.nowMillis());
        clocks.nanos += 1;
        Assertions.assertEquals(initial + 1, clock.nowMillis());
        Assertions.assertEquals(millis("2026-10-06T13:15:00Z"),
                EventReminderClock.nextHourlyEvent(clock.nowMillis(), 15));
    }

    @DisplayName("Hourly event timestamps are exact and strictly after the supplied time")
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "2026-10-06T12:14:59.999Z|15|2026-10-06T12:15:00Z",
            "2026-10-06T12:15:00Z|15|2026-10-06T13:15:00Z",
            "2026-10-06T12:15:00.001Z|15|2026-10-06T13:15:00Z",
            "2026-10-06T12:54:59.999Z|55|2026-10-06T12:55:00Z",
            "2026-10-06T12:55:00Z|55|2026-10-06T13:55:00Z",
            "2026-10-06T12:55:00.001Z|55|2026-10-06T13:55:00Z",
            "2026-10-06T23:55:00Z|15|2026-10-07T00:15:00Z",
            "2026-10-06T23:55:00Z|55|2026-10-07T00:55:00Z"
    })
    void testHourlyEventBoundaries(String now, int minute, String expected) {
        long next = EventReminderClock.nextHourlyEvent(millis(now), minute);
        Assertions.assertEquals(millis(expected), next);
        Assertions.assertEquals(0, Instant.ofEpochMilli(next).getNano());
    }

    @DisplayName("Partial-hour timezone offsets do not move an hourly event")
    @ParameterizedTest
    @ValueSource(strings = {
            "2026-10-06T17:44:59.999+05:30",
            "2026-10-06T17:59:59.999+05:45",
            "2026-10-06T21:44:59.999+09:30",
            "2026-10-06T08:44:59.999-03:30"
    })
    void testHourlyEventTimezoneIndependence(String localTime) {
        long now = OffsetDateTime.parse(localTime).toInstant().toEpochMilli();
        Assertions.assertEquals(millis("2026-10-06T12:15:00Z"), EventReminderClock.nextHourlyEvent(now, 15));
        Assertions.assertEquals(millis("2026-10-06T12:55:00Z"), EventReminderClock.nextHourlyEvent(now, 55));
    }

    @DisplayName("All ten Jacob crops have distinct display names and round-trip lookups")
    @Test
    void testJacobCropEnumeration() {
        Assertions.assertEquals(10, JacobCrop.values().length);
        Assertions.assertEquals(Set.of("Wheat", "Carrot", "Potato", "Pumpkin", "Melon", "Sugar Cane",
                        "Cactus", "Cocoa Beans", "Mushroom", "Nether Wart"),
                Arrays.stream(JacobCrop.values()).map(JacobCrop::getDisplayName).collect(java.util.stream.Collectors.toSet()));
        for (JacobCrop crop : JacobCrop.values()) {
            Assertions.assertEquals(Optional.of(crop), JacobCrop.fromName(crop.getDisplayName()));
        }
        Assertions.assertEquals(Optional.of(JacobCrop.SUGAR_CANE), JacobCrop.fromName(" sugar cane "));
        Assertions.assertEquals(Optional.of(JacobCrop.MUSHROOM), JacobCrop.fromName("Red Mushroom"));
        Assertions.assertEquals(Optional.of(JacobCrop.MUSHROOM), JacobCrop.fromName("Brown Mushroom"));
        Assertions.assertEquals(Optional.of(JacobCrop.MELON), JacobCrop.fromName("Melon Slice"));
        Assertions.assertEquals(Optional.of(JacobCrop.WHEAT), JacobCrop.fromName("Seeds"));
        for (String unknown : Arrays.asList(null, "", " ", "Coffee", "Wheat / Melon")) {
            Assertions.assertEquals(Optional.empty(), JacobCrop.fromName(unknown));
        }
    }

    @DisplayName("Any selected scheduled crop matches without changing either input set")
    @Test
    void testSelectedCropIntersection() {
        Set<JacobCrop> selected = Set.of(JacobCrop.CARROT, JacobCrop.MELON);
        Assertions.assertEquals(Set.of(JacobCrop.MELON), JacobContestCalendar.matchingCrops(CROPS, selected));
        Assertions.assertEquals(Set.of(JacobCrop.WHEAT, JacobCrop.MELON),
                JacobContestCalendar.matchingCrops(CROPS, Set.of(JacobCrop.WHEAT, JacobCrop.MELON)));
        Assertions.assertEquals(Set.of(), JacobContestCalendar.matchingCrops(CROPS, Set.of(JacobCrop.CARROT)));
        Assertions.assertEquals(Set.of(JacobCrop.CARROT, JacobCrop.MELON), selected);
        Assertions.assertEquals(Set.of(JacobCrop.WHEAT, JacobCrop.MELON, JacobCrop.COCOA_BEANS), CROPS);
    }

    @DisplayName("Missing, incomplete or oversized crop schedules never match")
    @Test
    void testInvalidCropSchedulesFailClosed() {
        Set<JacobCrop> selected = Set.of(JacobCrop.WHEAT);
        Assertions.assertEquals(Set.of(), JacobContestCalendar.matchingCrops(null, selected));
        Assertions.assertEquals(Set.of(), JacobContestCalendar.matchingCrops(CROPS, null));
        Assertions.assertEquals(Set.of(), JacobContestCalendar.matchingCrops(CROPS, Set.of()));
        Assertions.assertEquals(Set.of(), JacobContestCalendar.matchingCrops(Set.of(), selected));
        Assertions.assertEquals(Set.of(), JacobContestCalendar.matchingCrops(Set.of(JacobCrop.WHEAT), selected));
        Assertions.assertEquals(Set.of(), JacobContestCalendar.matchingCrops(Set.of(JacobCrop.WHEAT, JacobCrop.MELON), selected));
        Assertions.assertEquals(Set.of(), JacobContestCalendar.matchingCrops(
                Set.of(JacobCrop.WHEAT, JacobCrop.MELON, JacobCrop.CARROT, JacobCrop.POTATO), selected));
        Set<JacobCrop> containsNull = new HashSet<>(Set.of(JacobCrop.WHEAT, JacobCrop.MELON));
        containsNull.add(null);
        Assertions.assertEquals(Set.of(), JacobContestCalendar.matchingCrops(containsNull, selected));
    }

    @DisplayName("Monthly calendar fixtures map to fixed absolute 2026 timestamps")
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Early Spring|2026-10-03T21:55:00Z",
            "Spring|2026-10-04T08:15:00Z",
            "Late Spring|2026-10-04T18:35:00Z",
            "Early Summer|2026-10-05T04:55:00Z",
            "Summer|2026-10-05T15:15:00Z",
            "Late Summer|2026-10-06T01:35:00Z",
            "Early Autumn|2026-10-06T11:55:00Z",
            "Autumn|2026-10-06T22:15:00Z",
            "Late Autumn|2026-10-07T08:35:00Z",
            "Early Winter|2026-10-07T18:55:00Z",
            "Winter|2026-10-08T05:15:00Z",
            "Late Winter|2026-10-08T15:35:00Z"
    })
    void testCalendarMonthAbsoluteDates(String month, String expected) {
        Assertions.assertEquals(Map.of(millis(expected), CROPS), JacobContestCalendar.parsePage(
                month + ", Year 518", List.of(contest("Day 1", "Wheat", "Melon", "Cocoa Beans"))));
    }

    @DisplayName("Year indexing retains the SkyBlock epoch and month or year rollover")
    @Test
    void testCalendarEpochAndRollover() {
        Assertions.assertEquals(Map.of(millis("2019-06-11T17:55:00Z"), CROPS), JacobContestCalendar.parsePage(
                "Early Spring, Year 1", List.of(contest("Day 1", "Wheat", "Melon", "Cocoa Beans"))));
        Assertions.assertEquals(Map.of(millis("2026-10-04T07:55:00Z"), CROPS), JacobContestCalendar.parsePage(
                "Early Spring, Year 518", List.of(contest("Day 31st", "Wheat", "Melon", "Cocoa Beans"))));
        Assertions.assertEquals(Map.of(millis("2026-10-04T08:15:00Z"), CROPS), JacobContestCalendar.parsePage(
                "Spring, Year 518", List.of(contest("Day 1st", "Wheat", "Melon", "Cocoa Beans"))));
        Assertions.assertEquals(Map.of(millis("2026-10-09T01:35:00Z"), CROPS), JacobContestCalendar.parsePage(
                "Late Winter, Year 518", List.of(contest("Day 31", "Wheat", "Melon", "Cocoa Beans"))));
        Assertions.assertEquals(Map.of(millis("2026-10-09T01:55:00Z"), CROPS), JacobContestCalendar.parsePage(
                "Early Spring, Year 519", List.of(contest("Day 1", "Wheat", "Melon", "Cocoa Beans"))));
    }

    @DisplayName("Formatted calendar entries and boosted crop glyphs parse the same exact day")
    @Test
    void testFormattedAndBoostedCalendarFixtures() {
        JacobContestCalendar.DayEntry boosted = new JacobContestCalendar.DayEntry("§aDay 2nd§r", List.of(
                "§6Jacob's Farming Contest§r", "§e\uE051 §aWheat", "§6○ §eMelon", "§b\uE051 §aCocoa Beans§r"));
        Assertions.assertEquals(Map.of(millis("2026-10-06T12:15:00Z"), CROPS),
                JacobContestCalendar.parsePage("§6Early Autumn, Year §b518§r", List.of(boosted)));
    }

    @DisplayName("Other event sections do not add crops to a complete Jacob block")
    @Test
    void testCalendarIgnoresSeparateEventSections() {
        JacobContestCalendar.DayEntry day = new JacobContestCalendar.DayEntry("Day 2", List.of(
                "Fishing Festival", "○ Carrot", "", JACOB_HEADER, "○ Wheat", "○ Melon", "○ Cocoa Beans",
                "", "Spooky Festival", "○ Potato"));
        Assertions.assertEquals(Map.of(millis("2026-10-06T12:15:00Z"), CROPS),
                JacobContestCalendar.parsePage("Early Autumn, Year 518", List.of(day)));
    }

    @DisplayName("Missing, malformed or unrelated page titles produce no schedule")
    @ParameterizedTest
    @ValueSource(strings = {"", "Calendar", "Calendar - Early Spring, Year 518", "Unknown, Year 518",
            "Early Autumn Year 518", "Early Autumn, Year 0", "Early Autumn, Year -1",
            "Early Autumn, Year 1000000", "Early Autumn, Year 518 extra"})
    void testInvalidCalendarTitles(String title) {
        Assertions.assertEquals(Map.of(), JacobContestCalendar.parsePage(title,
                List.of(contest("Day 2", "Wheat", "Melon", "Cocoa Beans"))));
    }

    @DisplayName("Day names must identify a single valid day from one to thirty-one")
    @ParameterizedTest
    @ValueSource(strings = {"Day 0", "Day 32", "Day -1", "Day 2 extra", "2nd", "Not a day"})
    void testInvalidCalendarDays(String day) {
        Assertions.assertEquals(Map.of(), JacobContestCalendar.parsePage("Early Autumn, Year 518",
                List.of(contest(day, "Wheat", "Melon", "Cocoa Beans"))));
    }

    @DisplayName("Unknown, incomplete, duplicate or ambiguous contest lore fails closed")
    @ParameterizedTest
    @MethodSource("invalidCalendarLore")
    void testInvalidCalendarLore(String description, List<String> lore) {
        Assertions.assertEquals(Map.of(), JacobContestCalendar.parsePage("Early Autumn, Year 518",
                List.of(new JacobContestCalendar.DayEntry("Day 2", lore))), description);
    }

    static Stream<Arguments> invalidCalendarLore() {
        return Stream.of(
                Arguments.of("missing contest header", List.of("○ Wheat", "○ Melon", "○ Cocoa Beans")),
                Arguments.of("wrong contest header", List.of("Jacob's Contest", "○ Wheat", "○ Melon", "○ Cocoa Beans")),
                Arguments.of("unknown crop", List.of(JACOB_HEADER, "○ Wheat", "○ Coffee", "○ Melon")),
                Arguments.of("only two crops", List.of(JACOB_HEADER, "○ Wheat", "○ Melon")),
                Arguments.of("four crops", List.of(JACOB_HEADER, "○ Wheat", "○ Melon", "○ Cocoa Beans", "○ Carrot")),
                Arguments.of("duplicate crop", List.of(JACOB_HEADER, "○ Wheat", "○ Wheat", "○ Melon")),
                Arguments.of("duplicate aliases", List.of(JACOB_HEADER, "○ Mushroom", "○ Red Mushroom", "○ Wheat")),
                Arguments.of("missing crop bullets", List.of(JACOB_HEADER, "Wheat", "Melon", "Cocoa Beans")),
                Arguments.of("unrecognized bullet", List.of(JACOB_HEADER, "• Wheat", "• Melon", "• Cocoa Beans")),
                Arguments.of("interrupted crop block", List.of(JACOB_HEADER, "○ Wheat", "Fishing Festival", "○ Melon", "○ Cocoa Beans")),
                Arguments.of("other event before first crop", List.of(JACOB_HEADER, "Fishing Festival", "○ Wheat", "○ Melon", "○ Cocoa Beans")),
                Arguments.of("duplicate contest headers", List.of(JACOB_HEADER, JACOB_HEADER, "○ Wheat", "○ Melon", "○ Cocoa Beans")),
                Arguments.of("second contest after another event", List.of(JACOB_HEADER, "○ Wheat", "○ Melon", "○ Cocoa Beans",
                        "Fishing Festival", JACOB_HEADER, "○ Wheat", "○ Melon", "○ Cocoa Beans"))
        );
    }

    @DisplayName("Conflicting entries for one calendar day are rejected independently of page order")
    @Test
    void testConflictingCalendarDaysAreAmbiguous() {
        JacobContestCalendar.DayEntry original = contest("Day 2", "Wheat", "Melon", "Cocoa Beans");
        JacobContestCalendar.DayEntry conflicting = contest("Day 2", "Carrot", "Potato", "Pumpkin");
        JacobContestCalendar.DayEntry other = contest("Day 3", "Wheat", "Melon", "Cocoa Beans");
        for (List<JacobContestCalendar.DayEntry> page : List.of(List.of(original, conflicting, other),
                List.of(conflicting, original, other), List.of(original, conflicting, original, other))) {
            Assertions.assertEquals(Map.of(millis("2026-10-06T12:35:00Z"), CROPS),
                    JacobContestCalendar.parsePage("Early Autumn, Year 518", page));
        }
        Assertions.assertEquals(Map.of(millis("2026-10-06T12:15:00Z"), CROPS),
                JacobContestCalendar.parsePage("Early Autumn, Year 518", List.of(original, original)));
    }

    @DisplayName("Absent or unusable calendar data is safe to ignore")
    @Test
    void testNullCalendarData() {
        Assertions.assertEquals(Map.of(), JacobContestCalendar.parsePage(null, List.of()));
        Assertions.assertEquals(Map.of(), JacobContestCalendar.parsePage("Early Autumn, Year 518", null));
        Assertions.assertEquals(Map.of(), JacobContestCalendar.parsePage("Early Autumn, Year 518", Arrays.asList(
                null, new JacobContestCalendar.DayEntry(null, List.of()), new JacobContestCalendar.DayEntry("Day 2", null))));
    }

    @DisplayName("Wrong or stale pages and absent selections suppress reminders without consuming the event")
    @Test
    void testExactCalendarLookupControlsReminderEligibility() {
        long event = millis("2026-10-06T12:15:00Z");
        ReminderTrigger trigger = new ReminderTrigger();
        Set<JacobCrop> selected = Set.of(JacobCrop.MELON);
        Map<Long, Set<JacobCrop>> stale = JacobContestCalendar.parsePage("Spring, Year 518",
                List.of(contest("Day 2", "Wheat", "Melon", "Cocoa Beans")));
        Map<Long, Set<JacobCrop>> wrongDay = JacobContestCalendar.parsePage("Early Autumn, Year 518",
                List.of(contest("Day 1", "Wheat", "Melon", "Cocoa Beans")));
        Assertions.assertFalse(trigger.shouldNotify(event, 60, 1, true,
                !JacobContestCalendar.matchingCrops(stale.get(event), selected).isEmpty()));
        Assertions.assertFalse(trigger.shouldNotify(event, 59, 1, true,
                !JacobContestCalendar.matchingCrops(wrongDay.get(event), selected).isEmpty()));
        Assertions.assertFalse(trigger.shouldNotify(event, 58, 1, true,
                !JacobContestCalendar.matchingCrops(null, selected).isEmpty()));
        Assertions.assertFalse(trigger.shouldNotify(event, 57, 1, true,
                !JacobContestCalendar.matchingCrops(CROPS, Set.of()).isEmpty()));
        Assertions.assertTrue(trigger.shouldNotify(event, 56, 1, true,
                !JacobContestCalendar.matchingCrops(CROPS, selected).isEmpty()));
    }

    @DisplayName("Existing warnings defer reminders and simultaneous events retain FIFO order")
    @Test
    void testReminderQueueWaitsAndDisplaysInOrder() {
        ReminderQueue<String> queue = new ReminderQueue<>();
        queue.offer("Agatha", 10_000);
        queue.offer("Jacob", 10_000);
        queue.offer("Fishing Festival", 10_000);
        Assertions.assertNull(queue.poll(0, true, 1000));
        Assertions.assertNull(queue.poll(500, true, 1000));
        Assertions.assertEquals("Agatha", queue.poll(500, false, 1000));
        Assertions.assertEquals("Agatha", queue.poll(1499, false, 1000));
        Assertions.assertEquals("Jacob", queue.poll(1500, false, 1000));
        Assertions.assertEquals("Fishing Festival", queue.poll(2500, false, 1000));
        Assertions.assertNull(queue.poll(3500, false, 1000));
    }

    @DisplayName("A queued reminder that expires behind another warning is never shown")
    @Test
    void testQueueDropsExpiredPendingNotices() {
        ReminderQueue<String> queue = new ReminderQueue<>();
        queue.offer("already expired", 0);
        queue.offer("short lived", 500);
        queue.offer("still eligible", 5000);
        Assertions.assertNull(queue.poll(0, true, 1000));
        Assertions.assertNull(queue.poll(500, true, 1000));
        Assertions.assertEquals("still eligible", queue.poll(500, false, 1000));
    }

    @DisplayName("An active reminder disappears at its event deadline even before display expiry")
    @Test
    void testQueueActiveDeadlineAndBlocking() {
        ReminderQueue<String> queue = new ReminderQueue<>();
        queue.offer("ends now", 500);
        queue.offer("next", 5000);
        Assertions.assertEquals("ends now", queue.poll(0, false, 2000));
        Assertions.assertNull(queue.poll(100, true, 2000));
        Assertions.assertEquals("ends now", queue.poll(499, false, 2000));
        Assertions.assertEquals("next", queue.poll(500, false, 2000));
        Assertions.assertNull(queue.poll(2500, false, 2000));
    }

    @DisplayName("Clear removes both active and pending notices")
    @Test
    void testQueueClear() {
        ReminderQueue<String> queue = new ReminderQueue<>();
        queue.offer("active", 5000);
        queue.offer("pending", 5000);
        Assertions.assertEquals("active", queue.poll(0, false, 1000));
        queue.clear();
        Assertions.assertNull(queue.poll(1, false, 1000));
        queue.offer("new", 5000);
        Assertions.assertEquals("new", queue.poll(2, false, 1000));
    }

    @DisplayName("Discard removes stale active and disabled pending notices while eligible notices continue")
    @Test
    void testQueueDiscardRemovesActiveAndPendingNotices() {
        ReminderQueue<String> queue = new ReminderQueue<>();
        queue.offer("stale active", 10_000);
        queue.offer("disabled pending", 10_000);
        queue.offer("eligible first", 10_000);
        queue.offer("eligible second", 10_000);
        Assertions.assertEquals("stale active", queue.poll(0, false, 1000));

        queue.discardIf(notice -> notice.startsWith("stale") || notice.startsWith("disabled"));
        Assertions.assertEquals("eligible first", queue.poll(1, false, 1000));
        Assertions.assertEquals("eligible first", queue.poll(1000, false, 1000));
        Assertions.assertEquals("eligible second", queue.poll(1001, false, 1000));
        Assertions.assertNull(queue.poll(2001, false, 1000));
    }

    @DisplayName("Discard preserves an eligible active notice and its original display expiry")
    @Test
    void testQueueDiscardKeepsEligibleActiveNotice() {
        ReminderQueue<String> queue = new ReminderQueue<>();
        queue.offer("eligible active", 10_000);
        queue.offer("disabled pending", 10_000);
        queue.offer("stale pending", 10_000);
        queue.offer("eligible pending", 10_000);
        Assertions.assertEquals("eligible active", queue.poll(0, false, 1000));

        queue.discardIf(notice -> notice.startsWith("stale") || notice.startsWith("disabled"));
        Assertions.assertEquals("eligible active", queue.poll(999, false, 1000));
        Assertions.assertEquals("eligible pending", queue.poll(1000, false, 1000));
        Assertions.assertNull(queue.poll(2000, false, 1000));
    }

    @DisplayName("Discarding every notice leaves the queue ready for a later eligible reminder")
    @Test
    void testQueueDiscardAll() {
        ReminderQueue<String> queue = new ReminderQueue<>();
        queue.offer("active", 10_000);
        queue.offer("pending", 10_000);
        Assertions.assertEquals("active", queue.poll(0, false, 1000));
        queue.discardIf(notice -> true);
        Assertions.assertNull(queue.poll(1, false, 1000));
        queue.offer("new eligible", 10_000);
        Assertions.assertEquals("new eligible", queue.poll(2, false, 1000));
    }

    @DisplayName("Dismissing a shown reminder for a warp preserves deferred notices in order")
    @Test
    void testQueueDismissActivePreservesPendingAfterWarp() {
        ReminderQueue<String> queue = new ReminderQueue<>();
        queue.offer("already shown", 10_000);
        queue.offer("deferred first", 10_000);
        queue.offer("deferred second", 10_000);
        Assertions.assertEquals("already shown", queue.poll(0, false, 1000));
        queue.dismissActive();
        Assertions.assertNull(queue.poll(100, true, 1000));
        Assertions.assertEquals("deferred first", queue.poll(200, false, 1000));
        Assertions.assertEquals("deferred second", queue.poll(1200, false, 1000));
        Assertions.assertNull(queue.poll(2200, false, 1000));
    }

    @DisplayName("Deferred notices that expire during a warp are dropped after active dismissal")
    @Test
    void testQueueDismissActiveStillDropsExpiredPending() {
        ReminderQueue<String> queue = new ReminderQueue<>();
        queue.offer("already shown", 10_000);
        queue.offer("expired while away", 100);
        queue.offer("still eligible", 10_000);
        Assertions.assertEquals("already shown", queue.poll(0, false, 1000));
        queue.dismissActive();
        Assertions.assertNull(queue.poll(50, true, 1000));
        Assertions.assertEquals("still eligible", queue.poll(100, false, 1000));
        Assertions.assertNull(queue.poll(1100, false, 1000));
    }

    @DisplayName("The queue bounds pending notices to the four most recent entries")
    @Test
    void testQueueCapacity() {
        ReminderQueue<Integer> queue = new ReminderQueue<>();
        for (int notice = 0; notice < 5; notice++) queue.offer(notice, 10_000);
        for (int notice = 1; notice < 5; notice++) {
            Assertions.assertEquals(notice, queue.poll((notice - 1) * 1000L, false, 1000));
        }
        Assertions.assertNull(queue.poll(4000, false, 1000));
    }

    @DisplayName("Starlyn corrections and world changes retain the logical event identity")
    @Test
    void testStarlynEventIdentitySurvivesCorrectionsAndWarp() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS + 900_000);
        StarlynContestTimer timer = clocks.starlynTimer();
        ReminderTrigger trigger = new ReminderTrigger();
        long event = timer.getEventId();
        Assertions.assertTrue(trigger.shouldNotify(event, timer.getRemainingSeconds(), 5, true, true));
        Assertions.assertTrue(timer.observe(301));
        Assertions.assertEquals(event, timer.getEventId());
        Assertions.assertFalse(trigger.shouldNotify(timer.getEventId(), timer.getRemainingSeconds(), 5, true, true));
        Assertions.assertTrue(timer.observe(299));
        Assertions.assertEquals(event, timer.getEventId());
        Assertions.assertFalse(trigger.shouldNotify(timer.getEventId(), timer.getRemainingSeconds(), 5, true, true));
        timer.clearObservation();
        clocks.wallMillis += 86_400_000L + 45_000;
        clocks.advanceMillis(1000);
        Assertions.assertEquals(event, timer.getEventId());
        Assertions.assertEquals(298, timer.getRemainingSeconds());
        Assertions.assertTrue(timer.observe(300));
        Assertions.assertEquals(event, timer.getEventId());
        Assertions.assertFalse(trigger.shouldNotify(timer.getEventId(), timer.getRemainingSeconds(), 5, true, true));
    }

    @DisplayName("One monotonic sample pairs countdown and identity consistently across a rollover")
    @Test
    void testStarlynSnapshotCannotStraddleRollover() {
        long[] nanos = {0, 999_999, 1_000_000, 1_000_001, 1_001_000_000};
        AtomicInteger reads = new AtomicInteger();
        StarlynContestTimer timer = new StarlynContestTimer(
                () -> SKYBLOCK_EPOCH_MILLIS + 1_199_999,
                () -> nanos[reads.getAndIncrement()]);
        Assertions.assertEquals(1, reads.get());

        Assertions.assertEquals(new StarlynContestTimer.Snapshot(0, 1), timer.getSnapshot());
        Assertions.assertEquals(2, reads.get());
        Assertions.assertEquals(new StarlynContestTimer.Snapshot(1, 1200), timer.getSnapshot());
        Assertions.assertEquals(3, reads.get());
        Assertions.assertEquals(new StarlynContestTimer.Snapshot(1, 1200), timer.getSnapshot());
        Assertions.assertEquals(4, reads.get());
        Assertions.assertEquals(new StarlynContestTimer.Snapshot(1, 1199), timer.getSnapshot());
        Assertions.assertEquals(5, reads.get());
    }

    @DisplayName("Natural twenty-minute rollover creates exactly one new event identity")
    @Test
    void testStarlynNaturalEventRollover() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS + 900_000);
        StarlynContestTimer timer = clocks.starlynTimer();
        ReminderTrigger trigger = new ReminderTrigger();
        long original = timer.getEventId();
        Assertions.assertTrue(trigger.shouldNotify(original, timer.getRemainingSeconds(), 5, true, true));
        clocks.advanceMillis(299_999);
        Assertions.assertEquals(original, timer.getEventId());
        clocks.advanceMillis(1);
        Assertions.assertEquals(original + 1, timer.getEventId());
        Assertions.assertEquals(original + 1, timer.getEventId());
        Assertions.assertEquals(1200, timer.getRemainingSeconds());
        clocks.advanceMillis(900_000);
        Assertions.assertTrue(trigger.shouldNotify(timer.getEventId(), timer.getRemainingSeconds(), 5, true, true));
        clocks.advanceMillis(2_400_000);
        Assertions.assertEquals(original + 3, timer.getEventId());
    }

    @DisplayName("A near-boundary server rollover advances identity before the local deadline")
    @ParameterizedTest
    @ValueSource(ints = {0, 1170, 1200})
    void testStarlynEarlyServerRollover(int receivedRemaining) {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS + 1_180_000);
        StarlynContestTimer timer = clocks.starlynTimer();
        long original = timer.getEventId();
        Assertions.assertTrue(timer.observe(receivedRemaining));
        Assertions.assertEquals(original + 1, timer.getEventId());
        Assertions.assertFalse(timer.observe(receivedRemaining));
        timer.clearObservation();
        Assertions.assertEquals(original + 1, timer.getEventId());
        clocks.advanceMillis(20_000);
        Assertions.assertEquals(original + 1, timer.getEventId());
    }

    @DisplayName("Delayed old end packets cannot undo a confirmed server reset from another source")
    @ParameterizedTest
    @ValueSource(ints = {1, 19, 30})
    void testStarlynRejectsDelayedEndPacketAfterServerReset(int staleRemaining) {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.starlynTimer();
        Assertions.assertTrue(timer.observe(20, StarlynContestTimer.Source.SCOREBOARD));
        long previous = timer.getSnapshot().eventId();
        Assertions.assertTrue(timer.observe(1200, StarlynContestTimer.Source.SCOREBOARD));
        Assertions.assertEquals(new StarlynContestTimer.Snapshot(previous + 1, 1200), timer.getSnapshot());
        Assertions.assertFalse(timer.observe(staleRemaining, StarlynContestTimer.Source.TAB_LIST));
        Assertions.assertEquals(new StarlynContestTimer.Snapshot(previous + 1, 1200), timer.getSnapshot());
        Assertions.assertTrue(timer.observe(1199, StarlynContestTimer.Source.SCOREBOARD));
        Assertions.assertEquals(new StarlynContestTimer.Snapshot(previous + 1, 1199), timer.getSnapshot());
    }

    @DisplayName("Delayed changed packets cannot undo a natural rollover after synchronization")
    @Test
    void testStarlynRejectsDelayedEndPacketAfterNaturalReset() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.starlynTimer();
        Assertions.assertTrue(timer.observe(20, StarlynContestTimer.Source.SCOREBOARD));
        long previous = timer.getSnapshot().eventId();
        clocks.advanceMillis(20_000);
        Assertions.assertEquals(new StarlynContestTimer.Snapshot(previous + 1, 1200), timer.getSnapshot());
        Assertions.assertFalse(timer.observe(18, StarlynContestTimer.Source.TAB_LIST));
        Assertions.assertEquals(new StarlynContestTimer.Snapshot(previous + 1, 1200), timer.getSnapshot());
    }

    @DisplayName("The first live end reading can correct an unsynchronized full-countdown baseline")
    @Test
    void testStarlynFirstLiveEndReadingStillCorrectsInitialPhase() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.starlynTimer();
        StarlynContestTimer.Snapshot initial = timer.getSnapshot();
        Assertions.assertEquals(1200, initial.remainingSeconds());
        Assertions.assertTrue(timer.observe(19, StarlynContestTimer.Source.TAB_LIST));
        Assertions.assertEquals(new StarlynContestTimer.Snapshot(initial.eventId(), 19), timer.getSnapshot());
        clocks.advanceMillis(1000);
        Assertions.assertEquals(new StarlynContestTimer.Snapshot(initial.eventId(), 18), timer.getSnapshot());
    }

    @DisplayName("Warp observation clearing retains the guard against delayed old end packets")
    @Test
    void testStarlynWarpRetainsDelayedPacketProtection() {
        MutableClocks clocks = new MutableClocks(SKYBLOCK_EPOCH_MILLIS);
        StarlynContestTimer timer = clocks.starlynTimer();
        Assertions.assertTrue(timer.observe(20, StarlynContestTimer.Source.SCOREBOARD));
        Assertions.assertTrue(timer.observe(1200, StarlynContestTimer.Source.SCOREBOARD));
        StarlynContestTimer.Snapshot reset = timer.getSnapshot();
        timer.clearObservation();
        Assertions.assertFalse(timer.observe(19, StarlynContestTimer.Source.TAB_LIST));
        Assertions.assertFalse(timer.observe(20, StarlynContestTimer.Source.SCOREBOARD));
        Assertions.assertEquals(reset, timer.getSnapshot());
        Assertions.assertTrue(timer.observe(1199, StarlynContestTimer.Source.SCOREBOARD));
        Assertions.assertEquals(new StarlynContestTimer.Snapshot(reset.eventId(), 1199), timer.getSnapshot());
    }

    private static long millis(String instant) {
        return Instant.parse(instant).toEpochMilli();
    }

    private static JacobContestCalendar.DayEntry contest(String day, String... crops) {
        List<String> lore = new ArrayList<>();
        lore.add(JACOB_HEADER);
        for (String crop : crops) lore.add("○ " + crop);
        return new JacobContestCalendar.DayEntry(day, lore);
    }

    private static class MutableClocks {
        private long wallMillis;
        private long nanos = 123_456_789L;

        private MutableClocks(long wallMillis) {
            this.wallMillis = wallMillis;
        }

        private EventReminderClock reminderClock() {
            return new EventReminderClock(() -> wallMillis, () -> nanos);
        }

        private StarlynContestTimer starlynTimer() {
            return new StarlynContestTimer(() -> wallMillis, () -> nanos);
        }

        private void advanceMillis(long elapsed) {
            wallMillis += elapsed;
            nanos += elapsed * 1_000_000L;
        }
    }
}
