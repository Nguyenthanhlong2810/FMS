/**
 * Copyright (c) Fraunhofer IML
 */
package org.opentcs.example;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface VehicleProperties {

  /**
   * The key of the vehicle property containing the vehicle's host name/IP address.
   */
  String PROPKEY_VEHICLE_HOST = "TCP:Host";
  /**
   * The key of the vehicle property containing the vehicle's TCP port.
   */
  String PROPKEY_VEHICLE_PORT = "TCP:Port";
}
