/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator.batchcreator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.plugins.panels.loadgenerator.DriveOrderStructure;
import org.opentcs.guing.plugins.panels.loadgenerator.TransportOrderData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A batch generator for creating explicit transport orders.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class ExplicitOrderBatchGenerator
    implements OrderBatchCreator {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ExplicitOrderBatchGenerator.class);
  /**
   * The transport order service we talk to.
   */
  private final TransportOrderService transportOrderService;
  /**
   * The dispatcher service.
   */
  private final DispatcherService dispatcherService;
  /**
   * The TransportOrderData we're building the transport orders from.
   */
  private final List<TransportOrderData> data;


  public static final String TRANSPORT_ORDER_TRIGGER_PROPERTY = "dataIndex";

  /**
   * Creates a new ExplicitOrderBatchGenerator.
   *
   * @param transportOrderService The portal.
   * @param data The transport order data.
   */
  public ExplicitOrderBatchGenerator(TransportOrderService transportOrderService,
                                     DispatcherService dispatcherService,
                                     List<TransportOrderData> data) {
    this.transportOrderService = requireNonNull(transportOrderService, "transportOrderService");
    this.dispatcherService = requireNonNull(dispatcherService, "dispatcherService");
    this.data = requireNonNull(data, "data");
    data.forEach(d -> d.addProperty(TRANSPORT_ORDER_TRIGGER_PROPERTY, randomString(5)));
  }

  @Override
  public Set<TransportOrder> createOrderBatch()
      throws KernelRuntimeException {
    Set<TransportOrder> createdOrders = new HashSet<>();
    for (TransportOrderData curData : data) {
      createdOrders.add(createSingleOrder(curData));
    }

    dispatcherService.dispatch();

    return createdOrders;
  }

  public void reCreateOrder(TransportOrder transportOrder) {
    String mark = transportOrder.getProperty(TRANSPORT_ORDER_TRIGGER_PROPERTY);
    this.data.stream()
            .filter(d -> d.getProperties().get(TRANSPORT_ORDER_TRIGGER_PROPERTY).equals(mark))
            .findFirst()
            .ifPresent(this::createSingleOrder);
  }

  private TransportOrder createSingleOrder(TransportOrderData curData)
      throws KernelRuntimeException {
    TransportOrder newOrder = transportOrderService.createTransportOrder(
        new TransportOrderCreationTO("TOrder-",
                                     createDestinations(curData.getDriveOrders()))
            .withIncompleteName(true)
            .withDeadline(Instant.now().plusSeconds(curData.getDeadline().getTime() / 1000))
            .withIntendedVehicleName(curData.getIntendedVehicle() == null
                ? null
                : curData.getIntendedVehicle().getName())
            .withProperties(curData.getProperties()));

    return newOrder;
  }

  private List<DestinationCreationTO> createDestinations(List<DriveOrderStructure> structures) {
    List<DestinationCreationTO> result = new ArrayList<>();
    for (DriveOrderStructure currentOrder : structures) {
      result.add(new DestinationCreationTO(currentOrder.getDriveOrderLocation().getName(),
                                           currentOrder.getDriveOrderVehicleOperation()));
    }
    return result;
  }

  private String randomString(int n) {
    String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "0123456789"
            + "abcdefghijklmnopqrstuvxyz";
    // create StringBuffer size of AlphaNumericString
    StringBuilder sb = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      // generate a random number between
      // 0 to AlphaNumericString variable length
      int index
              = (int)(AlphaNumericString.length()
              * Math.random());

      // add Character one by one in end of sb
      sb.append(AlphaNumericString
              .charAt(index));
    }
    return sb.toString();
  }
}
