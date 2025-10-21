package com.fix3dll.skyblockaddons.core.scheduler;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class Scheduler {

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private final ObjectArrayList<ScheduledTask> queuedTasks;
    private final ObjectArrayList<ScheduledTask> pendingTasks;
    private final Object anchor = new Object();
    private volatile long totalTicks = 0;

    public Scheduler() {
        queuedTasks = new ObjectArrayList<>();
        pendingTasks = new ObjectArrayList<>();
        ClientTickEvents.START_CLIENT_TICK.register(this::ticker);
    }

    public synchronized long getTotalTicks() {
        return this.totalTicks;
    }

    public void ticker(Minecraft mc) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("SkyblockAddonsScheduler");
        synchronized (this.anchor) {
            this.totalTicks++;
        }

        if (mc != null) {
            if (!this.pendingTasks.isEmpty()) this.pendingTasks.removeIf(ScheduledTask::isCanceled);
            if (!this.queuedTasks.isEmpty()) this.queuedTasks.removeIf(ScheduledTask::isCanceled);

            this.pendingTasks.addAll(queuedTasks);
            queuedTasks.clear();

            try {
                for (ScheduledTask scheduledTask : this.pendingTasks) {
                    if (this.getTotalTicks() >= (scheduledTask.getStartTick() + scheduledTask.getDelay())) {
                        scheduledTask.start();

                        if (scheduledTask.isRepeating()) {
                            if (!scheduledTask.isCanceled()) {
                                scheduledTask.updateDelay(scheduledTask.getPeriod(), true);
                            }
                        } else {
                            scheduledTask.cancel();
                        }
                    }
                }
            } catch (Throwable ex) {
                LOGGER.error("Scheduler ticking error: ", ex);
            }
        }
        profilerFiller.pop();
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