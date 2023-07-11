package org.opentcs.data.notification;

import java.io.Serializable;
import java.util.function.Predicate;

public class VehicleNotificationPredicate implements Serializable, Predicate<UserNotification> {

  @Override
  public boolean test(UserNotification notification) {
    return notification instanceof VehicleNotification && notification.getLevel() == UserNotification.Level.IMPORTANT;
  }
}
