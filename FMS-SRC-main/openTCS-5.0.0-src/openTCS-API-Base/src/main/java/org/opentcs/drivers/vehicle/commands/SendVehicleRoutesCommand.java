package org.opentcs.drivers.vehicle.commands;

import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SendVehicleRoutesCommand implements AdapterCommand {

  private static final Logger LOG = LoggerFactory.getLogger(SendVehicleRoutesCommand.class);

  private int mapId;

  private Map<Integer, Route> routes;

  private boolean success = false;

  public SendVehicleRoutesCommand(int mapId, Map<Integer, Route> routes) {
    this.mapId = mapId;
    this.routes = routes;
  }

  public boolean isSuccess() {
    return success;
  }

  /**
   * Executes the command.
   *
   * @param adapter The comm adapter to execute the command with.
   */
  @Override
  public void execute(VehicleCommAdapter adapter) {
    adapter.sendVehicleRoutes(mapId, routes);
  }
}
