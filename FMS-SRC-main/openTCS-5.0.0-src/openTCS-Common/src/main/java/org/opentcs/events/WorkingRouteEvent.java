package org.opentcs.events;

import org.opentcs.data.model.Vehicle;

import java.io.Serializable;

public class WorkingRouteEvent implements Serializable {

  private final EventType type;

  private Vehicle vehicle;

  public WorkingRouteEvent(EventType type, Vehicle vehicle) {
    this.type = type;
    this.vehicle = vehicle;
  }

  public EventType getType() {
    return type;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public enum EventType {
    SYNC_ROUTES_SUCCESS,
    SYNC_ROUTES_FAILED,
    CURRENT_ROUTE,
    COMPLETE_ROUTE,
  }
}
