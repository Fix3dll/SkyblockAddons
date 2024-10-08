package codes.biscuit.skyblockaddons.misc.scheduler;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;

@SuppressWarnings("JavadocDeclaration")
public class ScheduledTask {

    private static volatile int currentId = 1;
    private static final Object anchor = new Object();
    private final long addedTime = System.currentTimeMillis();
    private long addedTicks = SkyblockAddons.getInstance().getNewScheduler().getTotalTicks();
    private final int id;
    private int delay;
    private final int period;
    /**
     * -- GETTER --
     * <br>Gets if the current task is an asynchronous task.
     * @return True if the task is not run by main thread.
     */
    @Getter
    private final boolean async;
    /**
     * -- GETTER --
     * <br>Gets if the current task is running.
     * @return True if the task is running.
     */
    @Getter
    private boolean running;
    /**
     * -- GETTER --
     * <br>Gets if the current task is canceled.
     * @return True if the task is canceled.
     */
    @Getter
    private boolean canceled;
    /**
     * -- GETTER --
     * <br>Gets if the current task is a repeating task.
     * @return True if the task is a repeating task.
     */
    @Getter
    private boolean repeating;
    private Runnable task;

    /**
     * Creates a new Scheduled Task.
     *
     * @param delay The delay (in ticks) to wait before the task is run.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param async If the task should be run asynchronously.
     */
    public ScheduledTask(int delay, int period, boolean async) {
        synchronized (anchor) {
            this.id = currentId++;
        }

        this.delay = delay;
        this.period = period;
        this.async = async;
        this.repeating = this.period > 0;
    }

    /**
     * Creates a new Scheduled Task.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param async If the task should be run asynchronously.
     */
    public ScheduledTask(final SkyblockRunnable task, int delay, int period, boolean async) {
        synchronized (anchor) {
            this.id = currentId++;
        }

        this.delay = delay;
        this.period = period;
        this.async = async;
        this.repeating = this.period > 0;

        task.setThisTask(this);

        this.task = () -> {
            this.running = true;
            task.run();
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
     *
     * @return When the task was added.
     */
    public final long getAddedTime() {
        return this.addedTime;
    }

    /**
     * Returns the added ticks for the task.
     *
     * @return Ticks when the task was added.
     */
    public final long getAddedTicks() {
        return this.addedTicks;
    }

    /**
     * Returns the id for the task.
     *
     * @return Task id number.
     */
    public final int getId() {
        return this.id;
    }

    /**
     * Returns the delay (in ticks) for the task.
     *
     * @return How long the task will wait to run.
     */
    public final int getDelay() {
        return this.delay;
    }

    /**
     * Returns the delay (in ticks) for the task to repeat itself.
     *
     * @return How long until the task repeats itself.
     */
    public final int getPeriod() {
        return this.period;
    }

    void setDelay(int delay) {
        this.addedTicks = SkyblockAddons.getInstance().getNewScheduler().getTotalTicks();
        this.delay = delay;
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

    public void setTask(SkyblockRunnable task) {
        this.task = () -> {
            this.running = true;
            task.run();
            this.running = false;
        };
    }
}
