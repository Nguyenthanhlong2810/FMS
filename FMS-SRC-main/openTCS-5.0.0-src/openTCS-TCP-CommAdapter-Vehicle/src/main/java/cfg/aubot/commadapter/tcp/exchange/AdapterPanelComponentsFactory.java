/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp.exchange;

import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.components.kernel.services.VehicleService;

/**
 * A factory for creating various comm adapter panel specific instances.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface AdapterPanelComponentsFactory {

  /**
   * Creates a {@link ControlPanel} representing the given process model's content.
   *
   * @param processModel The process model to represent.
   * @param vehicleService The vehicle service used for interaction with the comm adapter.
   * @return The control panel.
   */
  ControlPanel createControlPanel(ExampleProcessModelTO processModel,
                                  VehicleService vehicleService);

  /**
   * Creates a {@link StatusPanel} representing the given process model's content.
   *
   * @param processModel The process model to represent.
   * @param vehicleService The vehicle service used for interaction with the comm adapter.
   * @return The status panel.
   */
  StatusPanel createStatusPanel(ExampleProcessModelTO processModel,
                                VehicleService vehicleService);

  SendRoutePanel createSendRoutePanel(ExampleProcessModelTO processModel,
                                      VehicleService vehicleService,
                                      RouterService routerService);
}
