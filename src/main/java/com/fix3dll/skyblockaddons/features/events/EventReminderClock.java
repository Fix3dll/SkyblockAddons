package com.fix3dll.skyblockaddons.features.events;

import java.util.function.LongSupplier;

/** Hourly reminders retain their initial wall-clock phase through local clock changes. */
public class EventReminderClock {
    private final long initialMillis;
    private final long initialNanos;
    private final LongSupplier nanos;

    public EventReminderClock(LongSupplier wallMillis, LongSupplier nanos) {
        this.nanos = nanos;
        initialMillis = wallMillis.getAsLong();
        initialNanos = nanos.getAsLong();
    }

    public long nowMillis() {
        return initialMillis + Math.max(0, (nanos.getAsLong() - initialNanos) / 1_000_000L);
    }

    public static long nextHourlyEvent(long nowMillis, int minute) {
        if (minute < 0 || minute > 59) throw new IllegalArgumentException("Invalid event minute");
        long next = Math.floorDiv(nowMillis, 3_600_000L) * 3_600_000L + minute * 60_000L;
        return next > nowMillis ? next : next + 3_600_000L;
    }
}
