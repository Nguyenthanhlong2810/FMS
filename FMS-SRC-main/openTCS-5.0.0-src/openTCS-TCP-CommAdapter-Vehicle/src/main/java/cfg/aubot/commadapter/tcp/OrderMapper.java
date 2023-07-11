/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp;

import cfg.aubot.commadapter.tcp.telegrams.OrderRequest;
import cfg.aubot.commadapter.tcp.telegrams.OrderRequest.OrderAction;
import cfg.aubot.commadapter.tcp.telegrams.StateResponse;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.example.telegrams.BoundedCounter;
import org.opentcs.example.telegrams.Telegram;

import java.util.List;
import java.util.Objects;

import static org.opentcs.example.telegrams.BoundedCounter.UINT16_MAX_VALUE;

/**
 * Maps {@link MovementCommand}s from openTCS to a telegram sent to the vehicle.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class OrderMapper {

  /**
   * Counts the order id's sent to the vehicle.
   */
  private final BoundedCounter orderIdCounter = new BoundedCounter(1, UINT16_MAX_VALUE);

  private boolean loopMode = false;

  /**
   * Creates a new instance.
   */
  public OrderMapper() {
  }

  public void setLoopMode(boolean loopMode) {
    this.loopMode = loopMode;
  }

  public boolean isLoopMode() {
    return loopMode;
  }

  /**
   * Maps the given command to an order request that can be sent to the vehicle.
   *
   * @param command The command to be mapped.
   * @return The order request to be sent.
   * @throws IllegalArgumentException If the movement command could not be mapped properly.
   */
  public OrderRequest mapToOrder(MovementCommand command)
      throws IllegalArgumentException {
    //Check for turn left or right or not, add to destinationAction
    char turn = 'S';
    try {
      //check for present
      String nextPoint = command.getStep().getDestinationPoint().getName();
      String pointLeft;
      String pointRight;

      pointRight = command.getStep().getSourcePoint().getRightPoint().isEmpty() ? "" : command.getStep().getSourcePoint().getRightPoint() ;
      pointLeft = command.getStep().getSourcePoint().getLeftPoint().isEmpty() ? "" : command.getStep().getSourcePoint().getLeftPoint();

      if(nextPoint.equals(pointLeft)){
        turn = 'L';
      }else if(nextPoint.equals(pointRight)){
        turn = 'R';
      }
    }
    catch (Exception e) {
      // System.out.println("Don't worry, we got it.");
    }
    return new OrderRequest(Telegram.ID_DEFAULT,
                            orderIdCounter.getAndIncrement(),
                            command.getStep().getDestinationPoint().getName().substring(0, 4),
                            command.getOperation(),
                            turn, command.getLoopCount());
  }

  public OrderRequest mapToRouteStep(Route.Step step, String action, int count) {
    char turn = 'S';
    String nextPoint = step.getDestinationPoint().getName();
    String turnProperty = step.getSourcePoint().getProperty(nextPoint);
    try {
      //check for present
      if (turnProperty != null) {
        turn = turnProperty.charAt(0);
      }
    }
    catch (Exception e) {
      // System.out.println("Don't worry, we got it.");
    }
    return new OrderRequest(Telegram.ID_DEFAULT,
                            orderIdCounter.getAndIncrement(),
                            step.getDestinationPoint().getName().substring(0, 4),
                            action,
                            turn,
                            count);
  }

  public OrderRequest getNullOrderRequest(StateResponse stateResponse) {
    return new OrderRequest(Telegram.ID_DEFAULT,
                            orderIdCounter.getAndIncrement(),
                            stateResponse.getPositionId(),
                            "NOP",
                            'S', 0);
  }

  private static int extractDestinationId(Point point)
      throws IllegalArgumentException {
    try {
      return Integer.parseInt(point.getName());
    }
    catch (NumberFormatException e) {
      throw new IllegalArgumentException("Cannot parse point name: " + point.getName(), e);
    }
  }
}
