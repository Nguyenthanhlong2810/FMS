/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp.telegrams;

import com.google.common.primitives.Ints;
import org.opentcs.example.dispatching.LoadAction;
import org.opentcs.example.telegrams.Request;

import static com.google.common.base.Ascii.ETX;
import static com.google.common.base.Ascii.STX;
import static java.util.Objects.requireNonNull;

/**
 * Represents an order request addressed to the vehicle.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class OrderRequest
    extends Request {

  /**
   * The request type.
   */
  public static final byte TYPE = 2;
  /**
   * The expected length of a telegram of this type.
   */
  public static final int TELEGRAM_LENGTH = 14;
  /**
   * The size of the payload (the raw content, without STX, SIZE, CHECKSUM and ETX).
   */
  public static final int PAYLOAD_LENGTH = TELEGRAM_LENGTH - 4;
  /**
   * The position of the checksum byte.
   */
  public static final int CHECKSUM_POS = TELEGRAM_LENGTH - 2;
  /**
   * The transport order orderId.
   */
  private final int orderId;
  /**
   * The name of the destination point.
   */
  private final String destinationId;
  /**
   * The action to execute at the destination point.
   */
  private final String destinationAction;

  private char direction;

  private int loopCount = 0;

  /**
   * Creates a new instance.
   *
   * @param requestId The request's id.
   * @param orderId The order id.
   * @param destinationId The name of the destination point.
   * @param destinationAction The action to execute at the destination point.
   */
  public OrderRequest(int requestId,
                      int orderId,
                      String destinationId,
                      String destinationAction,
                      char direction, int loopCount) {
    super(TELEGRAM_LENGTH);
    this.id = requestId;
    this.orderId = orderId;
    this.destinationId = destinationId;
    this.destinationAction = requireNonNull(destinationAction, "destinationAction");
    this.direction = direction;
    this.loopCount = loopCount;

    encodeTelegramContent(orderId, destinationId, destinationAction, direction, loopCount);
  }

  /**
   * Returns this order request's order id.
   *
   * @return This order request's order id.
   */
  public int getOrderId() {
    return orderId;
  }

  /**
   * Returns this order request's destination name.
   *
   * @return This order request's destination name.
   */
  public String getDestinationId() {
    return destinationId;
  }

  /**
   * Returns this order request's destination action.
   *
   * @return This order request's destination action.
   */
  public String getDestinationAction() {
    return destinationAction;
  }

  public void setLoopCount(int loopCount) {
    this.loopCount = loopCount;
  }

  @Override
  public String toString() {
    return "OrderRequest{"
        + "requestId=" + id + ", "
        + "orderId=" + orderId + ", "
        + "destinationId=" + destinationId + ", "
        + "destinationAction=" + destinationAction + '}';
  }

  @Override
  public void updateRequestContent(int requestId) {
    id = requestId;
    encodeTelegramContent(orderId, destinationId, destinationAction, direction, loopCount);
  }

  /**
   * Encodes this telegram's content into the raw content byte array.
   *
   * @param orderId The order id
   * @param destinationId The destination name
   * @param destinationAction The destination action
   */
  private void encodeTelegramContent(int orderId,
                                     String destinationId,
                                     String destinationAction,
                                     char direction, int loopCount) {
    // Start of each telegram
    rawContent[0] = STX;
    rawContent[1] = PAYLOAD_LENGTH;

    // Payload of the telegram
    rawContent[2] = TYPE;

    byte[] tmpWord = Ints.toByteArray(id);
    rawContent[3] = tmpWord[2];
    rawContent[4] = tmpWord[3];

    tmpWord = Ints.toByteArray(orderId);
    rawContent[5] = tmpWord[2];
    rawContent[6] = tmpWord[3];

//    tmpWord = Ints.toByteArray(destinationId);
    char[] desId = destinationId.toCharArray();
    rawContent[7] = (byte) desId[0];
    rawContent[8] = (byte) desId[1];

    rawContent[9] = (byte) destinationAction.toCharArray()[0];

    rawContent[10] = (byte) direction;

    rawContent[11] = (byte) loopCount;

    // End of each telegram
    rawContent[CHECKSUM_POS] = getCheckSum(rawContent);
    rawContent[TELEGRAM_LENGTH - 1] = ETX;
  }

  /**
   * Defines all actions that a vehicle can execute as part of an order.
   */
  public enum OrderAction {
    /**
     * No action.
     */
    NONE('N'),
    /**
     * Action to load an object.
     */
    LOAD('L'),
    /**
     * Action to unload an object.
     */
    UNLOAD('U'),
    /**
     * Charge vehicle.
     */
    CHARGE('C'),
    /**
     * Custom action
     */
    STOP('S'),
    PAUSE_30S('T'),
    LOAD_FRONT('I'),
    LOAD_BACK('J'),
    UNLOAD_FRONT('O'),
    UNLOAD_BACK('P'),
    ROTATE('R');

    /**
     * The actual byte to put into the telegram to the vehicle.
     */
    private final byte actionByte;

    /**
     * Creates a new Action.
     *
     * @param action The actual byte to put into the telegram to the vehicle.
     */
    OrderAction(char action) {
      this.actionByte = (byte) action;
    }

    /**
     * Returns the actual byte to put into the telegram to the vehicle.
     *
     * @return The actual byte to put into the telegram to the vehicle.
     */
    public byte getActionByte() {
      return actionByte;
    }

    /**
     * Maps the given {@code actionString} to an order action.
     *
     * @param actionString
     * @return The action associated with the {@code actionString}.
     * Returns {@link #NONE} if there isn't any action associated with the {@code actionString}.
     */
    public static OrderAction stringToAction(String actionString) {
      OrderAction action = NONE;
      switch (actionString) {
        case LoadAction.LOAD:
          action = LOAD;
          break;
        case LoadAction.UNLOAD:
          action = UNLOAD;
          break;
        case LoadAction.LOAD_FRONT:
          action = LOAD_FRONT;
          break;
        case LoadAction.LOAD_BACK:
          action = LOAD_BACK;
          break;
        case LoadAction.UNLOAD_FRONT:
          action = UNLOAD_FRONT;
          break;
        case LoadAction.UNLOAD_BACK:
          action = UNLOAD_BACK;
          break;
        case LoadAction.CHARGE:
          action = CHARGE;
          break;
        case LoadAction.STOP:
          action = STOP;
          break;
        case LoadAction.PAUSE_30S:
          action = PAUSE_30S;
          break;
        case LoadAction.ROTATE:
          action = ROTATE;
      }
      return action;
    }
  }

}
