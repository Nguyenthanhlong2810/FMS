package com.aubot.agv.traffic;

import com.google.inject.Inject;
import org.opentcs.common.MoveState;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class PointIntersection implements IntersectionHandler, EventHandler {

  private boolean initialized;

  private final EventBus eventBus;

  private final InternalVehicleService vehicleService;

  private final Map<TCSObjectReference<Point>, LinkedList<IntersectionMover>> trafficMap;

  @Inject
  public PointIntersection(@ApplicationEventBus EventBus eventBus,
                           InternalVehicleService vehicleService) {
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    trafficMap = new HashMap<>();
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof TCSObjectEvent) {
      if (((TCSObjectEvent) event).getType() == TCSObjectEvent.Type.OBJECT_MODIFIED) {
        handleObjectEvent((TCSObjectEvent) event);
      }
    }
  }

  private void handleObjectEvent(TCSObjectEvent event) {
    if (!(event.getCurrentOrPreviousObjectState() instanceof Vehicle)) {
      return;
    }
    Vehicle oldVehicle = (Vehicle) event.getPreviousObjectState();
    Vehicle newVehicle = (Vehicle) event.getCurrentObjectState();

    handleVehicleStateChanged(oldVehicle, newVehicle);
    if (!Objects.equals(oldVehicle.getCurrentPosition(), newVehicle.getCurrentPosition())) {
      handleVehicleExitIntersection(oldVehicle, newVehicle);
    }
    if (!Objects.equals(oldVehicle.getNextPosition(), newVehicle.getNextPosition())) {
      handleVehicleIntoIntersection(oldVehicle, newVehicle);
    }
  }

  @Override
  public void handleVehicleIntoIntersection(Vehicle oldVehicle, Vehicle vehicle) {
    TCSObjectReference<Point> pointRef = vehicle.getNextPosition();
    TCSObjectReference<Vehicle> vehicleRef = vehicle.getReference();
    if (pointRef == null) {
      return;
    }
    Point point = vehicleService.fetchObject(Point.class, pointRef);
    if (point.getIncomingPaths().size() <= 1) {
      return;
    }
    LinkedList<IntersectionMover> moverQueue = trafficMap.computeIfAbsent(pointRef, q -> new LinkedList<>());
    IntersectionMover currentMover = moverQueue.peek();
    if (currentMover != null) {
      if (!currentMover.getEntryPoint().equals(pointRef)) {
        moverQueue.add(new IntersectionMover(vehicleRef, vehicle.getCurrentPosition()));
        vehicleService.sendCommAdapterMessage(vehicleRef, MoveState.STOP);
        return;
      }
    }
    moverQueue.addFirst(new IntersectionMover(vehicleRef, vehicle.getCurrentPosition()));
  }

  @Override
  public void handleVehicleExitIntersection(Vehicle oldVehicle, Vehicle vehicle) {
    TCSObjectReference<Point> pointRef = oldVehicle.getCurrentPosition();
    if (pointRef == null) {
      return;
    }
    LinkedList<IntersectionMover> vehicleQueue = trafficMap.get(pointRef);
    if (vehicleQueue == null) {
      return;
    }
    vehicleQueue.removeIf(v -> Objects.equals(v.getVehicle(), vehicle.getReference()));
    if (!vehicleQueue.isEmpty()) {
      vehicleService.sendCommAdapterMessage(vehicleQueue.peek().getVehicle(), MoveState.MOVE);
    } else {
      trafficMap.remove(pointRef);
    }
  }

  @Override
  public void handleVehicleStateChanged(Vehicle oldVehicle, Vehicle vehicle) {
    if (!oldVehicle.hasState(Vehicle.State.UNKNOWN) && vehicle.hasState(Vehicle.State.UNKNOWN)) {
      for (LinkedList<IntersectionMover> vehicleQueue : trafficMap.values()) {
        vehicleQueue.removeIf(v -> v.getVehicle().equals(vehicle.getReference()));
      }
    }
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    eventBus.subscribe(this);
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    eventBus.unsubscribe(this);
    trafficMap.clear();
    initialized = false;
  }
}
