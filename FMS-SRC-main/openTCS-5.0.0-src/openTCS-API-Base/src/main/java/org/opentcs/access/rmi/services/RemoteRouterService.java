/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.rmi.services;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.opentcs.access.rmi.ClientID;
import org.opentcs.components.kernel.services.RouterService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;

/**
 * Declares the methods provided by the {@link RouterService} via RMI.
 *
 * <p>
 * The majority of the methods declared here have signatures analogous to their counterparts in
 * {@link RouterService}, with an additional {@link ClientID} parameter which serves the purpose
 * of identifying the calling client and determining its permissions.
 * </p>
 * <p>
 * To avoid redundancy, the semantics of methods that only pass through their arguments are not
 * explicitly documented here again. See the corresponding API documentation in
 * {@link RouterService} for these, instead.
 * </p>
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface RemoteRouterService
    extends Remote {

  public void updatePathLock(ClientID clientId, TCSObjectReference<Path> ref, boolean locked)
      throws RemoteException;

  public void updateRoutingTopology(ClientID clientId)
      throws RemoteException;

  public Route getRoute(ClientID clientId, Vehicle vehicle, Point sourcePoint, Point destinationPoint)
      throws RemoteException;

  void exportRoute(ClientID clientId, TCSObjectReference<Vehicle> vehicleRef, List<DriveOrder> route)
      throws RemoteException;

  List<DriveOrder> importRoute(ClientID clientId, TCSObjectReference<Vehicle> vehicleRef)
      throws RemoteException;
}
