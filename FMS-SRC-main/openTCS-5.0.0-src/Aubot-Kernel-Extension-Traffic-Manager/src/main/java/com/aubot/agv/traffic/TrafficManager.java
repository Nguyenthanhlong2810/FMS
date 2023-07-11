package com.aubot.agv.traffic;


import com.google.inject.Inject;
import org.opentcs.common.MoveState;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class TrafficManager implements KernelExtension {

  private final TrafficManagerConfiguration configuration;

  private final EventBus eventBus;

  private final InternalVehicleService vehicleService;

  private boolean initialized;

  private final Set<IntersectionHandler> intersectionHandlers;

  @Inject
  public TrafficManager(TrafficManagerConfiguration configuration,
                        @ApplicationEventBus EventBus eventBus,
                        InternalVehicleService vehicleService,
                        Set<IntersectionHandler> intersectionHandlers) {
    this.configuration = requireNonNull(configuration, "configuration");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.intersectionHandlers = requireNonNull(intersectionHandlers, "intersectionHandlers");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    intersectionHandlers.forEach(Lifecycle::initialize);
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

    intersectionHandlers.forEach(Lifecycle::terminate);
    initialized = false;
  }
}
