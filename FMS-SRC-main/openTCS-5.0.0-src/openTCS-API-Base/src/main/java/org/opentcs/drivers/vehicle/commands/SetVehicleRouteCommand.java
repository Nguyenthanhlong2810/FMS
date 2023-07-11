package org.opentcs.drivers.vehicle.commands;

import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.WorkingRoute;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class SetVehicleRouteCommand implements AdapterCommand {

  private static final Logger LOG = LoggerFactory.getLogger(SetVehicleRouteCommand.class);

  private WorkingRoute.WorkingRouteRaw workingRoute;

  public SetVehicleRouteCommand(WorkingRoute.WorkingRouteRaw workingRoute) {
    this.workingRoute = workingRoute;
  }
  /**
   * Executes the command.
   *
   * @param adapter The comm adapter to execute the command with.
   */
  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.setVehicleCurrentRoute(workingRoute);
  }
}
