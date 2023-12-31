/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.services;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;

import org.opentcs.access.SchedulerAllocationState;
import org.opentcs.components.kernel.Scheduler;
import org.opentcs.components.kernel.services.SchedulerService;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.kernel.periodreport.MailingTask;
import org.opentcs.kernel.periodreport.Period;
import org.opentcs.kernel.periodreport.ResetableSchedulerTask;

/**
 * This class is the standard implementation of the {@link SchedulerService} interface.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class StandardSchedulerService
    implements SchedulerService {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The scheduler.
   */
  private final Scheduler scheduler;


  private int mailingPeriod = -1;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param scheduler The scheduler.
   */
  @Inject
  public StandardSchedulerService(@GlobalSyncObject Object globalSyncObject,
                                  Scheduler scheduler) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.scheduler = requireNonNull(scheduler, "scheduler");
  }

  @Override
  public SchedulerAllocationState fetchSchedulerAllocations() {
    synchronized (globalSyncObject) {
      return new SchedulerAllocationState(scheduler.getAllocations());
    }
  }

  @Override
  public void updateMailingSchedulerTask() {
    synchronized (globalSyncObject) {
      ResetableSchedulerTask taskExecutor = ResetableSchedulerTask.getInstance();
      try {
        Period period = MailingTask.getPeriodFromDb();
        if (period.getRepresent() != mailingPeriod) {
          mailingPeriod = period.getRepresent();
          taskExecutor.newTask(new MailingTask(taskExecutor, period));
          System.out.println("Period changed");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
