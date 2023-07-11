package org.opentcs.kernel.periodreport;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class PeriodTask implements Runnable {
    private final ScheduledExecutorService executor;
    protected final Period period;
    private OnTaskChangedListener listener;

    public PeriodTask(ScheduledExecutorService executor, OnTaskChangedListener listener, Period period) {
        this.executor = executor;
        this.period = period;
        this.listener = listener;
    }

    public Period getPeriod() {
        return period;
    }

    protected abstract void executeTask();

    @Override
    public void run() {
        long delay = period.getTime();
        if (delay == 0) {
            return;
        }
        try {
            executeTask();
        } finally {
            listener.onTaskChanged(executor.schedule(this, delay, TimeUnit.MILLISECONDS));
        }
    }
}
