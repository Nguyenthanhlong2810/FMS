package cfg.aubot.commadapter.tcp.telegrams;

import com.google.common.primitives.Ints;
import org.opentcs.example.telegrams.Request;
import org.opentcs.example.telegrams.WorkingRouteRequest;

import static com.google.common.base.Ascii.ETX;
import static com.google.common.base.Ascii.STX;

public class AskRouteRequest extends WorkingRouteRequest {

  /**
   * The request type.
   */
  public static final byte TYPE = 6;
  /**
   * The expected length of a telegram of this type.
   */
  public static final int TELEGRAM_LENGTH = 8;
  /**
   * The size of the payload (the raw content, without STX, SIZE, CHECKSUM and ETX).
   */
  public static final int PAYLOAD_LENGTH = TELEGRAM_LENGTH - 4;
  /**
   * The position of the checksum byte.
   */
  public static final int CHECKSUM_POS = TELEGRAM_LENGTH - 2;

  public AskRouteRequest(int mapId) {
    super(TELEGRAM_LENGTH);
    this.mapId = mapId;
    encodeTelegramContent();
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
    rawContent[0] = STX;
    rawContent[1] = PAYLOAD_LENGTH;

    rawContent[2] = TYPE;

    byte[] tmpWord = Ints.toByteArray(id);
    rawContent[3] = tmpWord[3];
    rawContent[4] = tmpWord[2];

    tmpWord = Ints.toByteArray(mapId);
    rawContent[5] = tmpWord[3];
    rawContent[6] = tmpWord[2];

    rawContent[CHECKSUM_POS] = getCheckSum(rawContent);
    rawContent[TELEGRAM_LENGTH - 1] = ETX;
  }
}
