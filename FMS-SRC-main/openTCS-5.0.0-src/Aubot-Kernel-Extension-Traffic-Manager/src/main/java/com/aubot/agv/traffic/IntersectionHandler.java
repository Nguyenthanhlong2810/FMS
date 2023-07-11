package com.aubot.agv.traffic;

import org.opentcs.components.Lifecycle;
import org.opentcs.data.model.Vehicle;

public interface IntersectionHandler extends Lifecycle {

  void handleVehicleIntoIntersection(Vehicle oldVehicle, Vehicle vehicle);

  void handleVehicleExitIntersection(Vehicle oldVehicle, Vehicle vehicle);

  void handleVehicleStateChanged(Vehicle oldVehicle, Vehicle vehicle);
}
