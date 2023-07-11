package cfg.aubot.commadapter.tcp.telegrams;

import com.google.common.primitives.Ints;
import org.opentcs.example.telegrams.Response;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Ascii.ETX;
import static com.google.common.base.Ascii.STX;
import static java.util.Objects.requireNonNull;

public class CurrentRouteResponse extends Response {

  public static final byte TYPE = 6;

  private int mapId;

  private int routeId;

  private Map<String, Character> pointActions = new HashMap<>();

  /**
   * Creates a new instance.
   *
   * @param telegramData The response's raw data.
   */
  public CurrentRouteResponse(byte[] telegramData) {
    super(telegramData.length);
    requireNonNull(telegramData, "telegramData");

    System.arraycopy(telegramData, 0, rawContent, 0, telegramData.length);
    if (isCurrentRouteResponse(telegramData)) {
      decodeTelegramContent(telegramData);
    }
  }

  public int getMapId() {
    return mapId;
  }

  public int getRouteId() {
    return routeId;
  }

  public Map<String, Character> getPointActions() {
    return pointActions;
  }

  private void decodeTelegramContent(byte[] telegramData) {
    this.id = Ints.fromBytes((byte) 0, (byte) 0, rawContent[4], rawContent[3]);
    mapId = Ints.fromBytes((byte) 0, (byte) 0, rawContent[6], rawContent[5]);
    routeId = rawContent[7];
    int index = 9;
    while (index < telegramData.length - 2) {
      String point = new String(new byte[] {rawContent[index++], rawContent[index++], rawContent[index++], rawContent[index++]});
      pointActions.put(point, (char) rawContent[index++]);
    }
  }

  public static boolean isCurrentRouteResponse(byte[] telegramData) {
    requireNonNull(telegramData, "telegramData");

    boolean result = true;
    int length = telegramData.length;
    int payloadLength = length - 4;

    if (length < 4) {
      result = false;
    }
    else if (telegramData[0] != STX) {
      result = false;
    }
    else if (telegramData[length - 1] != ETX) {
      result = false;
    }
    else if (telegramData[1] != payloadLength) {
      result = false;
    }
    else if (telegramData[2] != TYPE) {
      result = false;
    }
    else if (getCheckSum(telegramData) != telegramData[length - 2]) {
      result = false;
    }
    else if ((payloadLength - 7) % 5 != 0) {
      result = false;
    }

    return result;
  }
}
