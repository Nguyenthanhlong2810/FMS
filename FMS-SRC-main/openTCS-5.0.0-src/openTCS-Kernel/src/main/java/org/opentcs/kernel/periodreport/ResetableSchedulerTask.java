package org.opentcs.kernel.periodreport;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ResetableSchedulerTask implements OnTaskChangedListener {

    private static ResetableSchedulerTask instance = null;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private ScheduledFuture<?> currentTask;

    private ResetableSchedulerTask() {}

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    public static ResetableSchedulerTask getInstance() {
        if (instance == null) {
            instance = new ResetableSchedulerTask();
        }
        return instance;
    }

    public void newTask(PeriodTask periodTask) {
        if (currentTask != null) {
            currentTask.cancel(true);
        }
        if (periodTask.getPeriod() != Period.NONE) {
            currentTask = executor.schedule(periodTask, periodTask.getPeriod().getPeriodDelay(), TimeUnit.MILLISECONDS);
        }
    }

    // just for test
    public void newTaskImmediately(PeriodTask periodTask) {
        if (currentTask != null) {
            currentTask.cancel(true);
        }
        if (periodTask.getPeriod() != Period.NONE)
            currentTask = executor.schedule(periodTask, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onTaskChanged(ScheduledFuture<?> task) {
        this.currentTask = task;
    }
}
