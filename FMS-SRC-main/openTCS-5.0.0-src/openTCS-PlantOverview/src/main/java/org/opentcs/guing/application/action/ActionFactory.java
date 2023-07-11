/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action;

import org.opentcs.access.KernelServicePortal;
import org.opentcs.common.MoveState;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.application.action.course.*;
import org.opentcs.guing.communication.*;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.transport.VehicleRoutesPanel;

import javax.annotation.Nullable;
import java.util.Collection;


/**
 * A factory for various actions.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ActionFactory {

  ScrollToVehicleAction createScrollToVehicleAction(VehicleModel vehicleModel);

  FollowVehicleAction createFollowVehicleAction(VehicleModel vehicleModel);

  SendVehicleToPointAction createSendVehicleToPointAction(VehicleModel vehicleModel);

  SendVehicleToLocationAction createSendVehicleToLocationAction(VehicleModel vehicleModel);

  WithdrawAction createWithdrawAction(Collection<VehicleModel> vehicles, boolean immediateAbort);

  IntegrationLevelChangeAction createIntegrationLevelChangeAction(Collection<VehicleModel> vehicles,
                                                                  Vehicle.IntegrationLevel level);

  MoveVehiclesAction createMoveVehiclesAction(Collection<VehicleModel> vehicles, MoveState moveState);

  ShowRouteAction createShowRouteAction(VehicleModel vehicle);

  VehicleCommunicationAction createVehicleCommunicationAction(VehicleModel vehicleModel, ActionFactory actionFactory);

  DetailPanel createCommunicationPanel(KernelServicePortal servicePortal, LocalVehicleEntry vehicleEntry);

  VehicleRoutesPanel createVehicleRoutesPanel(@Nullable VehicleModel vehicleModel);

  DeleteVehicleAction createDeleteVehicleAction(VehicleModel vehicleModel);

  UpdateVehicleAction updateVehicleAction(VehicleModel vehicleModel);
}
