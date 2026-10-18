package com.fix3dll.skyblockaddons.features.events;

import java.util.ArrayDeque;
import java.util.function.Predicate;

/** Existing warnings take priority; simultaneous event reminders are shown one at a time. */
public class ReminderQueue<T> {
    private record Pending<T>(T value, long deadline) {}
    private final ArrayDeque<Pending<T>> pending = new ArrayDeque<>();
    private Pending<T> active;
    private long activeUntil;

    public void offer(T value, long deadline) {
        if (pending.size() >= 4) pending.removeFirst();
        pending.addLast(new Pending<>(value, deadline));
    }

    public T poll(long now, boolean blocked, long durationMillis) {
        if (active != null && (now >= activeUntil || now >= active.deadline())) active = null;
        pending.removeIf(notice -> notice.deadline() <= now);
        if (blocked) return null;
        if (active == null && !pending.isEmpty()) {
            active = pending.removeFirst();
            activeUntil = now + Math.max(1, durationMillis);
        }
        return active == null ? null : active.value();
    }

    public void clear() {
        pending.clear();
        active = null;
    }

    public void dismissActive() {
        active = null;
    }

    public void discardIf(Predicate<T> predicate) {
        pending.removeIf(notice -> predicate.test(notice.value()));
        if (active != null && predicate.test(active.value())) active = null;
    }
}
