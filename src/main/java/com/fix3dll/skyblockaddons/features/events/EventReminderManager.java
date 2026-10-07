package com.fix3dll.skyblockaddons.features.events;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.features.starlyn.StarlynContestManager;
import net.minecraft.client.Minecraft;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EventReminderManager {
    private static final EventReminderClock CLOCK = new EventReminderClock(System::currentTimeMillis, System::nanoTime);
    private static final Map<Feature, ReminderTrigger> TRIGGERS = new EnumMap<>(Feature.class);
    private static final ReminderQueue<Notice> TITLES = new ReminderQueue<>();
    private static final Feature[] TIMERS = {Feature.DARK_AUCTION_TIMER, Feature.FARM_EVENT_TIMER,
            Feature.MIRIA_CONTEST_TIMER, Feature.AGATHA_CONTEST_TIMER};
    private static final long SKYBLOCK_YEAR_MILLIS = 446400000L;

    public record Notice(Feature feature, long eventId, long deadline, String crops, boolean preview) {
        public String message() {
            int seconds = (int) Math.max(1, (deadline - CLOCK.nowMillis() + 999) / 1000);
            if (!preview && (feature == Feature.MIRIA_CONTEST_TIMER || feature == Feature.AGATHA_CONTEST_TIMER)) {
                seconds = StarlynContestManager.getRemainingSeconds(feature == Feature.MIRIA_CONTEST_TIMER);
            }
            String time = "%d:%02d".formatted(seconds / 60, seconds % 60);
            return Translations.getMessage(switch (feature) {
                case DARK_AUCTION_TIMER -> "messages.darkAuctionReminder";
                case FARM_EVENT_TIMER -> "messages.jacobContestReminder";
                case MIRIA_CONTEST_TIMER -> "messages.miriaContestReminder";
                case AGATHA_CONTEST_TIMER -> "messages.agathaContestReminder";
                default -> throw new IllegalArgumentException("Not an event timer");
            }, time);
        }
    }

    public static boolean isEventTimer(Feature feature) {
        return feature == Feature.DARK_AUCTION_TIMER || feature == Feature.FARM_EVENT_TIMER
                || feature == Feature.MIRIA_CONTEST_TIMER || feature == Feature.AGATHA_CONTEST_TIMER;
    }

    public static int leadMinutes(Feature feature) {
        Object value = feature.get(FeatureSetting.EVENT_REMINDER_MINUTES);
        int fallback = feature == Feature.DARK_AUCTION_TIMER ? 5 : 10;
        if (!(value instanceof Number number) || !Double.isFinite(number.doubleValue())) return fallback;
        int max = feature == Feature.MIRIA_CONTEST_TIMER || feature == Feature.AGATHA_CONTEST_TIMER ? 19 : 59;
        return Math.clamp(number.intValue(), 1, max);
    }

    public static void clearTitles() {
        TITLES.clear();
        // Keep notified event identities across warps/reconnects to avoid duplicate titles.
    }

    public static void onWorldChange() {
        // Hide an already shown title, retaining pending notices so a warp does not lose a deferred reminder.
        TITLES.dismissActive();
    }

    public static void update() {
        Minecraft mc = Minecraft.getInstance();
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (mc.level == null || mc.player == null || !main.getUtils().isOnSkyblock()) {
            onWorldChange();
            return;
        }
        long now = CLOCK.nowMillis();
        for (Feature feature : TIMERS) {
            long deadline;
            long eventId;
            int remaining;
            Set<JacobCrop> crops = Set.of();
            boolean eligible = true;
            if (feature == Feature.MIRIA_CONTEST_TIMER || feature == Feature.AGATHA_CONTEST_TIMER) {
                boolean miria = feature == Feature.MIRIA_CONTEST_TIMER;
                var snapshot = StarlynContestManager.getSnapshot(miria);
                remaining = snapshot.remainingSeconds();
                eventId = snapshot.eventId();
                deadline = now + remaining * 1000L;
            } else {
                deadline = EventReminderClock.nextHourlyEvent(now, feature == Feature.DARK_AUCTION_TIMER ? 55 : 15);
                eventId = deadline;
                remaining = (int) ((deadline - now + 999) / 1000);
                if (feature == Feature.DARK_AUCTION_TIMER) {
                    eligible = !feature.isEnabled(FeatureSetting.SHOW_ONLY_WHEN_SCORPIUS_IS_MAYOR)
                            || "Scorpius".equals(main.getUtils().getMayor());
                } else {
                    Set<JacobCrop> scheduled = cachedCrops(deadline);
                    if (feature.isEnabled(FeatureSetting.JACOB_REMINDER_SELECTED_CROPS_ONLY)) {
                        crops = JacobContestCalendar.matchingCrops(scheduled, selectedCrops());
                        eligible = !crops.isEmpty();
                    } else if (scheduled != null && scheduled.size() == 3) {
                        crops = scheduled;
                    }
                }
            }
            ReminderTrigger trigger = TRIGGERS.computeIfAbsent(feature, ignored -> new ReminderTrigger());
            if (trigger.shouldNotify(eventId, remaining, leadMinutes(feature),
                    feature.isEnabled(FeatureSetting.EVENT_REMINDER_ENABLED), eligible)) {
                TITLES.offer(new Notice(feature, eventId, deadline, cropNames(crops), false), deadline);
            }
        }
    }

    public static Notice currentTitle(boolean warningActive) {
        TITLES.discardIf(notice -> !notice.preview() && !stillEligible(notice));
        int durationSeconds = Math.clamp(Feature.WARNING_TIME.numberValue().intValue(), 1, 99);
        Notice notice = TITLES.poll(CLOCK.nowMillis(), warningActive, durationSeconds * 1000L);
        if (notice != null && !notice.preview() && notice.feature() == Feature.FARM_EVENT_TIMER) {
            Set<JacobCrop> crops = cachedCrops(notice.deadline());
            if (notice.feature().isEnabled(FeatureSetting.JACOB_REMINDER_SELECTED_CROPS_ONLY)) {
                crops = JacobContestCalendar.matchingCrops(crops, selectedCrops());
            }
            return new Notice(notice.feature(), notice.eventId(), notice.deadline(),
                    crops == null ? "" : cropNames(crops), false);
        }
        return notice;
    }

    private static boolean stillEligible(Notice notice) {
        Feature feature = notice.feature();
        if (feature.isDisabled(FeatureSetting.EVENT_REMINDER_ENABLED)) return false;
        if (feature == Feature.MIRIA_CONTEST_TIMER || feature == Feature.AGATHA_CONTEST_TIMER) {
            return notice.eventId() == StarlynContestManager.getEventId(feature == Feature.MIRIA_CONTEST_TIMER);
        }
        if (feature == Feature.DARK_AUCTION_TIMER) {
            return !feature.isEnabled(FeatureSetting.SHOW_ONLY_WHEN_SCORPIUS_IS_MAYOR)
                    || "Scorpius".equals(SkyblockAddons.getInstance().getUtils().getMayor());
        }
        return feature.isDisabled(FeatureSetting.JACOB_REMINDER_SELECTED_CROPS_ONLY)
                || !JacobContestCalendar.matchingCrops(cachedCrops(notice.deadline()), selectedCrops()).isEmpty();
    }

    public static void preview(Feature feature) {
        if (!isEventTimer(feature)) return;
        long deadline = CLOCK.nowMillis() + leadMinutes(feature) * 60_000L;
        String crops = feature == Feature.FARM_EVENT_TIMER ? cropNames(selectedCrops()) : "";
        TITLES.offer(new Notice(feature, deadline, deadline, crops, true), deadline);
    }

    public static void readCalendar(String title, List<JacobContestCalendar.DayEntry> days) {
        if (SkyblockAddons.getInstance().getUtils().isAlpha()) return;
        Map<Long, Set<JacobCrop>> received = JacobContestCalendar.parsePage(title, days);
        if (received.isEmpty()) return;
        var manager = SkyblockAddons.getInstance().getPersistentValuesManager();
        Map<Long, Set<JacobCrop>> old = manager.getData().getJacobContestCrops();
        Map<Long, Set<JacobCrop>> merged = new HashMap<>();
        long now = CLOCK.nowMillis();
        if (old != null) old.forEach((time, crops) -> addValid(merged, time, crops, now));
        received.forEach((time, crops) -> addValid(merged, time, crops, now));
        if (!merged.equals(old)) {
            // Replace rather than mutate a map that the asynchronous persistent writer may be reading.
            manager.getData().setJacobContestCrops(merged);
            SkyblockAddons.getInstance().getPlayerListener().setSavePersistentFlag(true);
        }
    }

    private static void addValid(Map<Long, Set<JacobCrop>> result, Long time, Set<JacobCrop> crops, long now) {
        if (time == null || time <= now || time > now + SKYBLOCK_YEAR_MILLIS || crops == null || crops.size() != 3) return;
        for (JacobCrop crop : crops) if (crop == null) return;
        result.put(time, Set.copyOf(crops));
    }

    private static Set<JacobCrop> cachedCrops(long deadline) {
        if (SkyblockAddons.getInstance().getUtils().isAlpha()) return null;
        var cache = SkyblockAddons.getInstance().getPersistentValuesManager().getData().getJacobContestCrops();
        if (cache == null) return null;
        Set<JacobCrop> crops = cache.get(deadline);
        if (crops == null || crops.size() != 3) return null;
        for (JacobCrop crop : crops) if (crop == null) return null;
        return crops;
    }

    private static Set<JacobCrop> selectedCrops() {
        Set<JacobCrop> selected = EnumSet.noneOf(JacobCrop.class);
        for (JacobCrop crop : JacobCrop.values()) {
            if (Feature.FARM_EVENT_TIMER.isEnabled(FeatureSetting.valueOf("JACOB_REMINDER_" + crop.name()))) selected.add(crop);
        }
        return selected;
    }

    private static String cropNames(Set<JacobCrop> crops) {
        return crops.stream().sorted().map(JacobCrop::getDisplayName).collect(Collectors.joining(", "));
    }
}
