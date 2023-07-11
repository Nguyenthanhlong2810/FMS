package org.opentcs.drivers.vehicle;

import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.data.model.Vehicle;

import java.util.List;
import java.util.Set;

public interface VehicleCommInfoValidator {

  /**
   * Method used for validating vehicle communication information
   *
   * @param vehicles list of vehicle
   * @return set of errors
   */
  Set<String> validateVehicles(List<VehicleCreationTO> vehicles);

  boolean validateVehicleCommInfo(Vehicle vehicle, Set<Vehicle> vehicles) throws ValidateException;
}
