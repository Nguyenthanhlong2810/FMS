package cfg.aubot.commadapter.tcp.telegrams;

import com.google.common.primitives.Ints;
import org.opentcs.data.model.WorkingRoute;
import org.opentcs.example.telegrams.Request;
import org.opentcs.example.telegrams.WorkingRouteRequest;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.google.common.base.Ascii.ETX;
import static com.google.common.base.Ascii.STX;

public class SetRouteRequest extends WorkingRouteRequest {

  public static final byte TYPE = 7;

  private int routeId;

  private Map<String, Character> pointActions;
  /**
   * Creates a new instance.
   *
   * @param mapId
   * @param routeId
   * @param pointActions
   */
  public SetRouteRequest(int mapId, int routeId, Map<String, Character> pointActions) {
    super(calculateDataLength(pointActions.size()));
    this.mapId = mapId;
    this.routeId = routeId;
    this.pointActions = pointActions;
    encodeTelegramContent();
  }

  public SetRouteRequest(WorkingRoute.WorkingRouteRaw workingRoute) {
    this(workingRoute.getMapId(), workingRoute.getRouteId(), workingRoute.getPointActions());
  }

  /**
   * Updates the content of the request to include the given id.
   *
   * @param telegramId The request's new id.
   */
  @Override
  public void updateRequestContent(int telegramId) {
    this.id = telegramId;
    encodeTelegramContent();
  }

  private void encodeTelegramContent() {
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

    buffer.put((byte) routeId);

    buffer.put((byte) pointActions.size());

    pointActions.forEach((point, action) -> {
      buffer.put(point.substring(0, 4).getBytes());
      buffer.put((byte) action.charValue());
    });

    // End of each telegram
    buffer.put(getCheckSum(buffer.array()));
    buffer.put(ETX);

    System.arraycopy(buffer.array(), 0, rawContent, 0, buffer.limit());
  }

  private static int calculateDataLength(int pointActionsSize) {
    return 1 // STX
            + 1 // length
            + 1 // type
            + 2 // id
            + 2 // map id
            + 1 // route id
            + 1 // point actions size
            + 1 // checksum
            + 1 // etx
            + pointActionsSize * 5;
  }

  @Override
  public int getMapId() {
    return mapId;
  }
}
