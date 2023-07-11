package cfg.aubot.commadapter.tcp.telegrams;

import com.google.common.primitives.Ints;
import org.opentcs.example.telegrams.Response;

import static com.google.common.base.Ascii.ETX;
import static com.google.common.base.Ascii.STX;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class RouteResponse extends Response {

   /**
   * The response type.
   */
  public static final byte TYPE = 5;
  /**
   * The expected length of a telegram of this type.
   */
  public static final int TELEGRAM_LENGTH = 13;
  /**
   * The size of the payload (the raw content, without STX, SIZE, CHECKSUM and ETX).
   */
  public static final int PAYLOAD_LENGTH = TELEGRAM_LENGTH - 4;
  /**
   * The position of the checksum byte.
   */
  public static final int CHECKSUM_POS = TELEGRAM_LENGTH - 2;

  private int mapId;

  private int routeCount;

  private int routeId;

  private byte routeState;

  private char mapState;

  /**
   * Creates a new instance.
   *
   * @param telegramData This telegram's raw content.
   */
  public RouteResponse(byte[] telegramData) {
    super(TELEGRAM_LENGTH);
    requireNonNull(telegramData, "telegramData");
    checkArgument(telegramData.length == TELEGRAM_LENGTH);

    System.arraycopy(telegramData, 0, rawContent, 0, TELEGRAM_LENGTH);
    decodeTelegramContent();
  }

  public boolean isFinalRoute() {
    return routeId == routeCount - 1;
  }

  public int getMapId() {
    return mapId;
  }

  public int getRouteCount() {
    return routeCount;
  }

  public int getRouteId() {
    return routeId;
  }

  public byte getRouteState() {
    return routeState;
  }

  public char getMapState() {
    return mapState;
  }

  private void decodeTelegramContent() {
    this.id = Ints.fromBytes((byte) 0, (byte) 0, rawContent[4], rawContent[3]);
    mapId = Ints.fromBytes((byte) 0, (byte) 0, rawContent[6], rawContent[5]);
    routeCount = rawContent[7];
    routeId = rawContent[8];
    routeState = rawContent[9];
    mapState = (char) rawContent[10];
  }

  public static boolean isRouteResponse(byte[] telegramData) {
    requireNonNull(telegramData, "telegramData");

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


}
