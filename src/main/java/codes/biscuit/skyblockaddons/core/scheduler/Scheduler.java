package codes.biscuit.skyblockaddons.core.scheduler;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Scheduler {

    private final List<ScheduledTask> queuedTasks = new ArrayList<>();
    private final List<ScheduledTask> pendingTasks = new ArrayList<>();
    private final Object anchor = new Object();
    private volatile long totalTicks = 0;

    public synchronized long getTotalTicks() {
        return this.totalTicks;
    }

    @SubscribeEvent
    public void ticker(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            synchronized (this.anchor) {
                this.totalTicks++;
            }

            if (Minecraft.getMinecraft() != null) {
                this.pendingTasks.removeIf(ScheduledTask::isCanceled);
                this.queuedTasks.removeIf(ScheduledTask::isCanceled);

                this.pendingTasks.addAll(queuedTasks);
                queuedTasks.clear();

                try {
                    for (ScheduledTask scheduledTask : this.pendingTasks) {
                        if (this.getTotalTicks() >= (scheduledTask.getStartTick() + scheduledTask.getDelay())) {
                            scheduledTask.start();

                            if (scheduledTask.isRepeating()) {
                                if (!scheduledTask.isCanceled()) {
                                    scheduledTask.updateDelay(scheduledTask.getPeriod());
                                }
                            } else {
                                scheduledTask.cancel();
                            }
                        }
                    }
                } catch (Throwable ex) {
                    SkyblockAddons.getLogger().error("Scheduler ticking error: ", ex);
                }
            }
        }
    }

    public synchronized void cancel(int id) {
        pendingTasks.forEach(scheduledTask -> {
            if (scheduledTask.getId() == id)
                scheduledTask.cancel();
        });
    }

    /**
     * Runs a task (asynchronously) on the next tick.
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before running the task.
     * @return The scheduled task.
     */
    public ScheduledTask scheduleAsyncTask(Consumer<ScheduledTask> task, int delay) {
        return this.scheduleAsyncTask(task, delay, 0);
    }

    /**
     * Runs a task (asynchronously) on the next tick.
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before running the task.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @return The scheduled task.
     */
    public ScheduledTask scheduleAsyncTask(Consumer<ScheduledTask> task, int delay, int period) {
        return this.scheduleTask(task, delay, period, false, true);
    }

    /**
     * Runs a task (synchronously) on the next tick.
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before running the task.
     * @return The scheduled task.
     */
    public ScheduledTask scheduleTask(Consumer<ScheduledTask> task, int delay) {
        return this.scheduleTask(task, delay, 0);
    }

    /**
     * Runs a task (synchronously) on the next tick.
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before running the task.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @return The scheduled task.
     */
    public ScheduledTask scheduleTask(Consumer<ScheduledTask> task, int delay, int period) {
        return this.scheduleTask(task, delay, period, false, false);
    }

    /**
     * Runs a task (synchronously) on the next tick.
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is run.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param queued Whether to queue this task to be run next loop; to be used for scheduling tasks directly from a
     *              synchronous task.
     * @return The scheduled task.
     */
    public ScheduledTask scheduleTask(Consumer<ScheduledTask> task, int delay, int period, boolean queued, boolean async) {
        ScheduledTask scheduledTask = new ScheduledTask(task, delay, period, async);
        if (queued) {
            this.queuedTasks.add(scheduledTask);
        } else {
            this.pendingTasks.add(scheduledTask);
        }
        return scheduledTask;
    }
}