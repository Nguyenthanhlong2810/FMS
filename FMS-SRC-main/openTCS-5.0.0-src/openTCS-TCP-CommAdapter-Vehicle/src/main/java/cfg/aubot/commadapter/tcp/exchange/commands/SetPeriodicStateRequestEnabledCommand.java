/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp.exchange.commands;

import cfg.aubot.commadapter.tcp.ExampleCommAdapter;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

/**
 * A command to enable/disable periodic state requests.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class SetPeriodicStateRequestEnabledCommand
    implements AdapterCommand {

  /**
   * The new state request state.
   */
  private final boolean enabled;

  /**
   * Creates a new instance.
   *
   * @param enabled The new state request state.
   */
  public SetPeriodicStateRequestEnabledCommand(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (!(adapter instanceof ExampleCommAdapter)) {
      return;
    }

    ExampleCommAdapter exampleAdapter = (ExampleCommAdapter) adapter;
    exampleAdapter.getProcessModel().setPeriodicStateRequestEnabled(enabled);
  }
}
