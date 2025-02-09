package codes.biscuit.skyblockaddons.core.scheduler;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;

import java.util.function.Consumer;

@SuppressWarnings("JavadocDeclaration")
public class ScheduledTask {

    private static volatile int currentId = 1;
    private static final Object anchor = new Object();
    private final long creationTime = System.currentTimeMillis();
    private long startTick = SkyblockAddons.getInstance().getScheduler().getTotalTicks();
    private final int id;
    private int delay;
    private int period;
    /**
     * -- GETTER --
     * <br>Gets if the current task is an asynchronous task.
     * @return True if the task is not run by main thread.
     */
    @Getter private final boolean async;
    /**
     * -- GETTER --
     * <br>Gets if the current task is running.
     * @return True if the task is running.
     */
    @Getter private boolean running;
    /**
     * -- GETTER --
     * <br>Gets if the current task is canceled.
     * @return True if the task is canceled.
     */
    @Getter private boolean canceled;
    /**
     * -- GETTER --
     * <br>Gets if the current task is a repeating task.
     * @return True if the task is a repeating task.
     */
    @Getter private boolean repeating;
    private Runnable task;

    /**
     * Creates a new Scheduled Task.
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param async If the task should be run asynchronously.
     */
    public ScheduledTask(final Consumer<ScheduledTask> task, int delay, int period, boolean async) {
        synchronized (anchor) {
            this.id = currentId++;
        }

        this.delay = delay;
        this.period = period;
        this.async = async;
        this.repeating = this.period > 0;

        this.task = () -> {
            this.running = true;
            task.accept(this);
            this.running = false;
        };
    }

    /**
     * Will attempt to cancel this task if running.
     */
    public final void cancel() {
        this.repeating = false;
        this.running = false;
        this.canceled = true;
    }

    /**
     * Returns the added time for the task.
     * @return When the task was added.
     */
    public final long getCreationTime() {
        return this.creationTime;
    }

    /**
     * Returns the number of ticks when the task started.
     * @return Ticks when the task was added.
     */
    public final long getStartTick() {
        return this.startTick;
    }

    /**
     * Returns the id for the task.
     * @return Task id number.
     */
    public final int getId() {
        return this.id;
    }

    /**
     * Returns the delay (in ticks) for the task.
     * @return How long the task will wait to run.
     */
    public final int getDelay() {
        return this.delay;
    }

    /**
     * Updates the delay of a scheduled task.
     * @param delay of the scheduled task
     * @return true if the delay is not the same as the previous one and has been updated
     */
    public boolean updateDelay(int delay) {
        if (this.delay != delay) {
            this.startTick = SkyblockAddons.getInstance().getScheduler().getTotalTicks();
            this.delay = delay;
            return true;
        }
        return false;
    }

    /**
     * Returns the delay (in ticks) for the task to repeat itself.
     * @return How long until the task repeats itself.
     */
    public final int getPeriod() {
        return this.period;
    }

    /**
     * Updates the period of a scheduled task.
     * @param period of the scheduled task
     * @return true if the period is not the same as the previous one and has been updated
     */
    public boolean updatePeriod(int period) {
        if (this.period != period) {
            this.period = period;
            this.repeating = period > 0;
            return true;
        }
        return false;
    }

    /**
     * Starts the task.
     */
    public void start() {
        if (this.isAsync()) {
            SkyblockAddons.runAsync(this.task);
        } else {
            this.task.run();
        }
    }

    public void updateTask(Consumer<ScheduledTask> task) {
        this.task = () -> {
            this.running = true;
            task.accept(this);
            this.running = false;
        };
    }

}