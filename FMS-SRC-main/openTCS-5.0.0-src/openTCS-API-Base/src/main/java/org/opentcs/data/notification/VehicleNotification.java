package org.opentcs.data.notification;

import org.opentcs.data.model.Vehicle;

public class VehicleNotification extends UserNotification {

  private final State state;
  private final Vehicle vehicle;
  private final Vehicle previousVehicle;

  public VehicleNotification(Vehicle vehicle, Vehicle previousVehicle, State state, Level level) {
    super(vehicle.getName(), state.name(), level);
    this.vehicle = vehicle;
    this.previousVehicle = previousVehicle;
    this.state = state;
  }

  public State getState() {
    return state;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public Vehicle getPreviousVehicle() {
    return previousVehicle;
  }

  public enum State {
    CONNECTED,
    DISCONNECTED,
    WARNING,
    ERROR,
    WRONG_NEXT_POSITION,
    LOST_WORKING_ROUTE,
  }
}
