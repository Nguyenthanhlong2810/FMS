/**
 * Copyright (c) Fraunhofer IML
 */
package cfg.aubot.commadapter.tcp.telegrams;

import com.google.common.primitives.Ints;
import org.opentcs.example.telegrams.Response;

import java.nio.ByteBuffer;

import static com.google.common.base.Ascii.ETX;
import static com.google.common.base.Ascii.STX;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * Represents a vehicle status response sent from the vehicle.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class StateResponse
    extends Response {

  /**
   * The response type.
   */
  public static final byte TYPE = 1;
  /**
   * The expected length of a telegram of this type.
   */
  public static final int TELEGRAM_LENGTH = 42;
  /**
   * The size of the payload (the raw content, without STX, SIZE, CHECKSUM and ETX).
   */
  public static final int PAYLOAD_LENGTH = TELEGRAM_LENGTH - 4;
  /**
   * The position of the checksum byte.
   */
  public static final int CHECKSUM_POS = TELEGRAM_LENGTH - 2;
  /**
   * The id of the point at the vehicle's current position.
   */
  private String positionId;
  /**
   * The vehicle's operation state.
   */
  private OperationState operationState;
  /**
   * The vehicle's load state.
   */
  private LoadState loadState;
  /**
   * The id of the last received order.
   */
  private int lastReceivedOrderId;
  /**
   * The id of the current order.
   */
  private int currentOrderId;
  /**
   * The id of the last finished order.
   */
  private int lastFinishedOrderId;
  /**
   * current energy Level - customize by Yue
   */
  private int energyLevel;
  /**
   * current I - customize
   */
  private float current;
  /**
   * current Voltage - customize
   */
  private float voltage;
  /**
   * Identify distance from current position - customize
   */
  private float distance;
  /**
   * Identify whether vehicle is on loop mode - customize
   * 0 is off - 1 is on
   */
  private boolean warning;
  /**
   * Vehicle's next position
   * use when vehicle in loop mode
   * customized by Khoi - AUBOT
   */
  private String nextPosition;
  /**
   * Vehicle's error code
   * customized by Khoi - AUBOT
   */
  private int errorCode;
  /**
   * Attribute indicates vehicle route has changed under vehicle
   */
  private int isRouteChanged;
  /**
   * Attribute indicates number of route which vehicle completed
   */
  private int routeCount;
  /**
   * Creates a new instance.
   *
   * @param telegramData This telegram's raw content.
   */
  public StateResponse(byte[] telegramData) {
    super(TELEGRAM_LENGTH);
    requireNonNull(telegramData, "telegramData");
    checkArgument(telegramData.length == TELEGRAM_LENGTH);

    System.arraycopy(telegramData, 0, rawContent, 0, TELEGRAM_LENGTH);
    decodeTelegramContent();
  }

  /**
   * Returns the id of the point at the vehicle's current position.
   *
   * @return The id of the point at the vehicle's current position
   */
  public String getPositionId() {
    return positionId;
  }

  /**
   * Returns the vehicle's operation state.
   *
   * @return The vehicle's operation state.
   */
  public OperationState getOperationState() {
    return operationState;
  }

  /**
   * Returns the vehicle's load state.
   *
   * @return The vehicle's load state.
   */
  public LoadState getLoadState() {
    return loadState;
  }

  /**
   * Returns the id of the last received order.
   *
   * @return The id of the last received order.
   */
  public int getLastReceivedOrderId() {
    return lastReceivedOrderId;
  }

  /**
   * Returns the id of the current order.
   *
   * @return The id of the current order.
   */
  public int getCurrentOrderId() {
    return currentOrderId;
  }

  /**
   * Returns the id of the last finished order.
   *
   * @return The id of the last finished order.
   */
  public int getLastFinishedOrderId() {
    return lastFinishedOrderId;
  }

  /**
   * get current energy level - customized by Yue
   */
  public int getEnergyLevel() { return energyLevel; }

  /**
   * get Current  - customized
   */
  public float getCurrent() { return current; }
  /**
   * get Voltage - customized
   */
  public float getVoltage() { return voltage; }
  /**
   * get distance from current position
   */
  public double getDistance() {
    return (double) distance * 1000;
  }
  /**
   * Vehicle's error code
   */
  public int getErrorCode() { return errorCode; }
  /**
   * get vehicle on loop mode or not - customize
   * @return
   */
  public boolean isWarning() { return warning; }
  /**
   * get vehicle's next position if it in loop mode
   * customized by Khoi - AUBOT
   */
  public String getNextPosition() {
    return nextPosition;
  }
  /**
   * is vehicle can process order
   */
  public boolean isRouteChanged() {
    return isRouteChanged == 1;
  }
  /**
   * Number of times vehicle execute current route
   */
  public int getRouteCount() {
    return routeCount;
  }
  /**
   * Returns the telegram's checksum byte.
   *
   * @return The telegram's checksum byte.
   */
  public byte getCheckSum() {
    return rawContent[CHECKSUM_POS];
  }


  @Override
  public String toString() {
    return "StateResponse{"
        + "requestId=" + id + ", "
        + "positionId=" + positionId + ", "
        + "operationState=" + operationState + ", "
        + "loadState=" + loadState + ", "
        + "lastReceivedOrderId=" + lastReceivedOrderId + ", "
        + "currentOrderId=" + currentOrderId + ", "
        + "lastFinishedOrderId=" + lastFinishedOrderId + ", "
        + "Current=" + current +", "
        + "Voltage=" + voltage +", "
        + "energyLevel= " + energyLevel + '}';

  }

  /**
   * Checks if the given byte array is a state reponse telegram.
   *
   * @param telegramData The telegram data to check.
   * @return {@code true} if, and only if, the given data is a state response telegram.
   */
  public static boolean isStateResponse(byte[] telegramData) {
    requireNonNull(telegramData, "data");

    boolean result = true;
    if (telegramData.length != TELEGRAM_LENGTH) {
      result = false;
    }
    else if (telegramData[0] != STX) {
      result = false;
    }
    else if (telegramData[TELEGRAM_LENGTH - 1] != ETX) {
      result = false;
    }
    else if (telegramData[1] != PAYLOAD_LENGTH) {
      result = false;
    }
    else if (telegramData[2] != TYPE) {
      result = false;
    }
    else if (getCheckSum(telegramData) != telegramData[CHECKSUM_POS]) {
      result = false;
    }
    return result;
  }

  private void decodeTelegramContent() {
    id = Ints.fromBytes((byte) 0, (byte) 0, rawContent[4], rawContent[3]);
    positionId = new String(new byte[] {rawContent[5], rawContent[6], rawContent[7], rawContent[8]});
    loadState = decodeLoadState((char) rawContent[10]);
    lastReceivedOrderId = Ints.fromBytes((byte) 0, (byte) 0, rawContent[12], rawContent[11]);
    currentOrderId = Ints.fromBytes((byte) 0, (byte) 0, rawContent[14], rawContent[13]);
    lastFinishedOrderId = Ints.fromBytes((byte) 0, (byte) 0, rawContent[16], rawContent[15]);
    voltage = convert4BytesToFullFloat(new byte[]{rawContent[20], rawContent[19], rawContent[18], rawContent[17]});
    current = convert4BytesToFullFloat(new byte[]{rawContent[24], rawContent[23], rawContent[22], rawContent[21]});
    energyLevel = rawContent[25];
    distance = convert4BytesToFullFloat(new byte[] {rawContent[29],rawContent[28],rawContent[27], rawContent[26]});
    warning = rawContent[30] == 1;
    nextPosition = new String(new byte[] {rawContent[31], rawContent[32], rawContent[33], rawContent[34]});
    errorCode = Ints.fromBytes((byte) 0, (byte) 0, rawContent[36], rawContent[35]);
    operationState = errorCode == 0 ? decodeOperatingState((char) rawContent[9]) : OperationState.ERROR;
    isRouteChanged = rawContent[37];
    routeCount = Ints.fromBytes((byte) 0, (byte) 0, rawContent[39], rawContent[38]);
  }

  private int convert2BytesToInteger(byte b1, byte b2) {
    return ByteBuffer.allocate(2).put(b1).put(b2).getInt();
  }

  private float convert4BytesToFullFloat(byte[] encode) {
    return ByteBuffer.wrap(encode).getFloat();
  }

  private OperationState decodeOperatingState(char operatingStateRaw) {
    switch (operatingStateRaw) {
      case 'A':
        return OperationState.ACTING;
      case 'I':
        return OperationState.IDLE;
      case 'M':
        return OperationState.MOVING;
      case 'W':
        return OperationState.WARNING;
      case 'E':
        return OperationState.ERROR;
      case 'C':
        return OperationState.CHARGING;
      default:
        return OperationState.UNKNOWN;
    }
  }

  private LoadState decodeLoadState(char loadStateRaw) {
    switch (loadStateRaw) {
      case 'E':
        return LoadState.EMPTY;
      case 'F':
        return LoadState.FULL;
      default:
        return LoadState.UNKNOWN;
    }
  }

  private int decodeError(byte highByte, byte lowByte) {
    ByteBuffer buffer = ByteBuffer.wrap(new byte[] {0, 0, highByte, lowByte});
    return buffer.getInt();
  }

  /**
   * The load handling state of a vehicle.
   */
  public static enum LoadState {
    /**
     * The vehicle's load handling state is currently empty.
     */
    EMPTY,
    /**
     * The vehicle's load handling state is currently full.
     */
    FULL,
    /**
     * The vehicle's load handling state is currently unknown.
     */
    UNKNOWN
  }

  /**
   * The operation state of a vehicle.
   */
  public static enum OperationState {
    /**
     * The vehicle is currently executing an operation.
     */
    ACTING,
    /**
     * The vehicle is currently idle.
     */
    IDLE,
    /**
     * The vehicle is currently moving.
     */
    MOVING,
    /**
     * The vehicle is blocked temporarily
     * customized by Khoi - AUBOT
     */
    WARNING,
    /**
     * The vehicle is currently in an error state.
     */
    ERROR,
    /**
     * The vehicle is currently recharging.
     */
    CHARGING,
    /**
     * The vehicle's state is currently unknown.
     */
    UNKNOWN
  }
}
