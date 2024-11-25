package codes.biscuit.skyblockaddons.misc.scheduler;

import lombok.Setter;

@Setter
public abstract class SkyblockRunnable implements Runnable {

    private ScheduledTask thisTask;

    public void cancel() {
        thisTask.cancel();
    }
}
