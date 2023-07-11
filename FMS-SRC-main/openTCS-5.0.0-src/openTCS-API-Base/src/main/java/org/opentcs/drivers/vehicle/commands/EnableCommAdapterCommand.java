package org.opentcs.drivers.vehicle.commands;

import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;

public class EnableCommAdapterCommand implements AdapterCommand {

  private final boolean enable;

  public EnableCommAdapterCommand(boolean enable) {
    this.enable = enable;
  }

  @Override
  public void execute(VehicleCommAdapter adapter) {
    if (enable) {
      adapter.enable();
    } else {
      adapter.disable();
    }
  }
}
