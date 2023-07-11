package com.aubot.agv.traffic;

import com.google.inject.Inject;
import org.opentcs.common.MoveState;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.*;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;

import java.util.*;

public class BlockIntersection implements IntersectionHandler, EventHandler {

  private boolean initialized;

  private final EventBus eventBus;

  private final InternalVehicleService vehicleService;

  private final Map<BlockPoints, LinkedList<IntersectionMover>> trafficMap;

  @Inject
  public BlockIntersection(@ApplicationEventBus EventBus eventBus,
                           InternalVehicleService vehicleService) {
    this.eventBus = eventBus;
    this.vehicleService = vehicleService;
    this.trafficMap = new HashMap<>();
  }

  @Override
  public void handleVehicleIntoIntersection(Vehicle oldVehicle, Vehicle vehicle) {
    TCSObjectReference<Point> position = vehicle.getCurrentPosition();
    TCSObjectReference<Vehicle> vehicleRef = vehicle.getReference();
    trafficMap.keySet().stream().forEach(block -> {
      if (block.getEntryPoints().contains(position)) {
        LinkedList<IntersectionMover> moverQueue = trafficMap.get(block);
        IntersectionMover currentMover = moverQueue.peek();
        if (currentMover != null) {
          if (!currentMover.getEntryPoint().equals(position)) {
            moverQueue.add(new IntersectionMover(vehicleRef, vehicle.getCurrentPosition()));
            vehicleService.sendCommAdapterMessage(vehicleRef, MoveState.STOP);
            return;
          }
        }
        moverQueue.addFirst(new IntersectionMover(vehicleRef, vehicle.getCurrentPosition()));
      }
    });
  }

  @Override
  public void handleVehicleExitIntersection(Vehicle oldVehicle, Vehicle vehicle) {
    TCSObjectReference<Point> position = vehicle.getCurrentPosition();
    TCSObjectReference<Vehicle> vehicleRef = vehicle.getReference();
    trafficMap.keySet().stream().forEach(block -> {
      if (block.getExitPoints().contains(position)) {
        LinkedList<IntersectionMover> moverQueue = trafficMap.get(block);
        moverQueue.removeIf(mover -> mover.getVehicle().equals(vehicleRef));
        IntersectionMover currentMover = moverQueue.peek();
        if (currentMover != null) {
          vehicleService.sendCommAdapterMessage(currentMover.getVehicle(), MoveState.MOVE);
        }
      }
    });
  }

  @Override
  public void handleVehicleStateChanged(Vehicle oldVehicle, Vehicle vehicle) {
    if (!oldVehicle.hasState(Vehicle.State.UNKNOWN) && vehicle.hasState(Vehicle.State.UNKNOWN)) {
      for (LinkedList<IntersectionMover> vehicleQueue : trafficMap.values()) {
        vehicleQueue.removeIf(mover -> mover.getVehicle().equals(vehicle.getReference()));
      }
    }
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    vehicleService.fetchObjects(Block.class).forEach(block -> {
      Set<TCSObjectReference<Point>> entries = new HashSet<>();
      Set<TCSObjectReference<Point>> exits = new HashSet<>();
      block.getMembers().stream().filter(mem -> mem.getReferentClass().equals(Path.class)).forEach(pathRef -> {
        Path path = vehicleService.fetchObject(Path.class, pathRef.getName());
        entries.add(path.getSourcePoint());
        exits.add(path.getDestinationPoint());
      });
      Set<TCSObjectReference<Point>> common = new HashSet<>(entries);
      common.retainAll(exits);
      entries.removeAll(common);
      exits.removeAll(common);
      trafficMap.put(new BlockPoints(block.getReference(), entries, exits), new LinkedList<>());
    });
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
      if (newVehicle.getCurrentPosition() != null) {
        handleVehicleExitIntersection(oldVehicle, newVehicle);
        handleVehicleIntoIntersection(oldVehicle, newVehicle);
      }
    }
  }
}
