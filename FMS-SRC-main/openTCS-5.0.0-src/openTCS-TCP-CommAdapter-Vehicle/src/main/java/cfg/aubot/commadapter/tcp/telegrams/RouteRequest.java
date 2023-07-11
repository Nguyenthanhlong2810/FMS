package cfg.aubot.commadapter.tcp.telegrams;

import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import org.opentcs.example.telegrams.Request;
import org.opentcs.example.telegrams.WorkingRouteRequest;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.google.common.base.Ascii.ETX;
import static com.google.common.base.Ascii.STX;

public class RouteRequest extends WorkingRouteRequest {

  public static final byte TYPE = 5;

  private int routeCount;

  private int routeId;

  private Map<String, Character> pointDirections;
  /**
   * Creates a new instance.
   */
  public RouteRequest(int mapId, int routeCount, int routeId, Map<String, Character> pointDirections) {
    super(calculateTelegramLength(pointDirections.size()));
    this.mapId = mapId;
    this.routeCount = routeCount;
    this.routeId = routeId;
    this.pointDirections = pointDirections;
    encodeTelegram();
  }

  @Override
  public int getMapId() {
    return mapId;
  }

  public int getRouteCount() {
    return routeCount;
  }

  public int getRouteId() {
    return routeId;
  }

  public Map<String, Character> getPointDirections() {
    return pointDirections;
  }

  /**
   * Updates the content of the request to include the given id.
   *
   * @param telegramId The request's new id.
   */
  @Override
  public void updateRequestContent(int telegramId) {
    this.id = telegramId;
    encodeTelegram();
  }

  private void encodeTelegram() {
    ByteBuffer buffer = ByteBuffer.allocate(rawContent.length);
    // Start of each telegram
    buffer.put(STX);

    buffer.put((byte) (rawContent.length - 4));

    // Payload of the telegram
    buffer.put(TYPE);

    byte[] tmpWord = Ints.toByteArray(id);
    buffer.put(tmpWord[3]);
    buffer.put(tmpWord[2]);

    tmpWord = Ints.toByteArray(mapId);
    buffer.put(tmpWord[3]);
    buffer.put(tmpWord[2]);

    buffer.put((byte) routeCount);

    buffer.put((byte) routeId);

    buffer.put((byte) pointDirections.size());

    pointDirections.forEach((point, direction) -> {
      buffer.put(point.substring(0, 4).getBytes());
      buffer.put((byte) direction.charValue());
    });

    // End of each telegram
    buffer.put(getCheckSum(buffer.array()));
    buffer.put(ETX);

    System.arraycopy(buffer.array(), 0, rawContent, 0, buffer.limit());
  }

  public static int calculateTelegramLength(int pointDirectionsSize) {
    return 1 // byte STX
          + 1 // byte ETX
          + 1 // byte length
          + 1 // byte type
          + 2 // bytes id
          + 2 // bytes mapId
          + 1 // byte routeCount
          + 1 // byte routeId
          + 1 // byte point directions size
          + 1 // byte checksum
          + pointDirectionsSize * 5;
  }
}
