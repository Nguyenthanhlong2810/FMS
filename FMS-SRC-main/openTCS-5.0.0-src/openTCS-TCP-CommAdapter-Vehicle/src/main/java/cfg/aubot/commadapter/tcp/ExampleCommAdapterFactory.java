/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp;

import cfg.aubot.commadapter.tcp.exchange.ExampleCommAdapterDescription;
import cfg.aubot.commadapter.tcp.validator.TcpCommAdapterValidator;
import org.opentcs.drivers.vehicle.VehicleCommInfoValidator;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.opentcs.example.VehicleProperties.PROPKEY_VEHICLE_HOST;
import static org.opentcs.example.VehicleProperties.PROPKEY_VEHICLE_PORT;
import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkInRange;

public class ExampleCommAdapterFactory
    implements VehicleCommAdapterFactory {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ExampleCommAdapterFactory.class);

  /**
   * The factory to create components specific to the comm adapter.
   */
  private final ExampleAdapterComponentsFactory componentsFactory;
  /**
   * Validator
   */
  private final TcpCommAdapterValidator validator;
  /**
   * This component's initialized flag.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param componentsFactory The factory to create components specific to the comm adapter.
   */
  @Inject
  public ExampleCommAdapterFactory(ExampleAdapterComponentsFactory componentsFactory, TcpCommAdapterValidator validator) {
    this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
    this.validator = requireNonNull(validator, "validator");
  }

  @Override
  public void initialize() {
    if (initialized) {
      LOG.debug("Already initialized.");
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      LOG.debug("Not initialized.");
      return;
    }
    initialized = false;
  }

  @Override
  public VehicleCommAdapterDescription getDescription() {
    return new ExampleCommAdapterDescription();
  }

  @Override
  public boolean providesAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

//    if (vehicle.getProperty(PROPKEY_VEHICLE_HOST) == null) {
//      return false;
//    }
//
//    if (vehicle.getProperty(VehicleProperties.PROPKEY_VEHICLE_PORT) == null) {
//      return false;
//    }
//    try {
//      checkInRange(Integer.parseInt(vehicle.getProperty(PROPKEY_VEHICLE_PORT)),
//                   1024,
//                   65535);
//    }
//    catch (IllegalArgumentException exc) {
//      return false;
//    }

    return true;
  }

  @Override
  public VehicleCommAdapter getAdapterFor(Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    if (!providesAdapterFor(vehicle)) {
      return null;
    }

    ExampleCommAdapter adapter = componentsFactory.createExampleCommAdapter(vehicle);
    try {
      adapter.getProcessModel().setVehicleHost(vehicle.getProperty(PROPKEY_VEHICLE_HOST));
    } catch (Exception e) {
      adapter.getProcessModel().setVehicleHost("localhost");
    }
    try {
      adapter.getProcessModel().setVehiclePort(Integer.parseInt(vehicle.getProperty(PROPKEY_VEHICLE_PORT)));
    } catch (Exception e) {
      adapter.getProcessModel().setVehiclePort(2020);
    }
    return adapter;
  }

  @Override
  public VehicleCommInfoValidator getValidator() {
    return validator;
  }
}
