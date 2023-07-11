/**
 * Copyright (c) Fraunhofer IML
 */
package org.opentcs.example.dispatching;

import org.opentcs.data.order.DriveOrder;

/**
 * Defines (configurable) strings for loading and unloading that can be used for vehicle actions in 
 * the kernel's model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface LoadAction {

  String NONE = DriveOrder.Destination.OP_NOP;
  /**
   * A constant for adding load.
   */
  String LOAD = "Load";
  /**
   * A constant for removing load.
   */
  String UNLOAD = "Unload";
  /**
   * A constant for charging the battery.
   */
  String CHARGE = "Charge";

  String STOP = "S";

  String PAUSE_30S = "T";

  String LOAD_FRONT = "Load 1";

  String LOAD_BACK = "Load 2";

  String UNLOAD_FRONT = "Unload 1";

  String UNLOAD_BACK = "Unload 2";

  String ROTATE = "Rotate";
}
