/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.RemoteException;
import java.util.List;

import org.opentcs.access.KernelRuntimeException;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;

/**
 * The default implementation of the router service.
 * Delegates method invocations to the corresponding remote service.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
class RemoteRouterServiceProxy
    extends AbstractRemoteServiceProxy<RemoteRouterService>
    implements RouterService {

  @Override
  public void updatePathLock(TCSObjectReference<Path> ref, boolean locked)
      throws ObjectUnknownException, KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updatePathLock(getClientId(), ref, locked);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void updateRoutingTopology()
      throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().updateRoutingTopology(getClientId());
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public Route getRoute(Vehicle vehicle, Point sourcePoint, Point destinationPoint)
          throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().getRoute(getClientId(), vehicle, sourcePoint, destinationPoint);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public void exportRoute(TCSObjectReference<Vehicle> vehicleRef, List<DriveOrder> route)
          throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      getRemoteService().exportRoute(getClientId(), vehicleRef, route);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }

  @Override
  public List<DriveOrder> importRoute(TCSObjectReference<Vehicle> vehicleRef) throws KernelRuntimeException {
    checkServiceAvailability();

    try {
      return getRemoteService().importRoute(getClientId(), vehicleRef);
    }
    catch (RemoteException ex) {
      throw findSuitableExceptionFor(ex);
    }
  }
}
