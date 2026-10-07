package com.fix3dll.skyblockaddons.features.events;

/** Remembers a notified event even when its countdown is corrected or the player changes worlds. */
public class ReminderTrigger {
    private long lastEventId = Long.MIN_VALUE;

    public boolean shouldNotify(long eventId, int remainingSeconds, int leadMinutes, boolean enabled, boolean eligible) {
        if (!enabled || !eligible || remainingSeconds <= 0 || leadMinutes <= 0
                || remainingSeconds > (long) leadMinutes * 60 || eventId <= lastEventId) {
            return false;
        }
        lastEventId = eventId;
        return true;
    }
}
