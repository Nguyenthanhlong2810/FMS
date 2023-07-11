/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp.exchange;

import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

import java.util.ResourceBundle;

/**
 * The comm adapter's {@link VehicleCommAdapterDescription}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ExampleCommAdapterDescription
    extends VehicleCommAdapterDescription {

  @Override
  public String getDescription() {
    return ResourceBundle.getBundle("cfg/aubot/commadapter/tcp/Bundle").
        getString("ExampleAdapterFactoryDescription");
  }

  @Override
  public boolean isSimVehicleCommAdapter() {
    return true;
  }
}
