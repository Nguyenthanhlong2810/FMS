/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp.exchange.commands;

import cfg.aubot.commadapter.tcp.ExampleCommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to set the vehicle's port.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetVehiclePortCommand
    implements AdapterCommand {

  /**
   * The port to set.
   */
  private final int port;

  /**
   * Creates a new instnace.
   *
   * @param port The host to set.
   */
  public SetVehiclePortCommand(int port) {
    this.port = port;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof ExampleCommAdapter)) {
      return;
    }

    ExampleCommAdapter exampleAdapter = (ExampleCommAdapter) adapter;
    exampleAdapter.getProcessModel().setVehiclePort(port);
  }
}
