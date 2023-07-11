package org.opentcs.kernel.periodreport;

import java.util.concurrent.ScheduledFuture;

public interface OnTaskChangedListener {

    void onTaskChanged(ScheduledFuture<?> task);
}
