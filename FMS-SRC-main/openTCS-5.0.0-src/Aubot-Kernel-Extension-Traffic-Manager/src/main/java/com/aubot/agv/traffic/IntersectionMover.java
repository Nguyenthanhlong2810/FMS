package com.aubot.agv.traffic;

import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

public class IntersectionMover {

  private TCSObjectReference<Vehicle> vehicleRef;

  private TCSObjectReference<Point> entryPointRef;

  public IntersectionMover(TCSObjectReference<Vehicle> vehicleRef,
                           TCSObjectReference<Point> entryPointRef) {
    this.vehicleRef = vehicleRef;
    this.entryPointRef = entryPointRef;
  }

  public TCSObjectReference<Vehicle> getVehicle() {
    return vehicleRef;
  }

  public TCSObjectReference<Point> getEntryPoint() {
    return entryPointRef;
  }
}
