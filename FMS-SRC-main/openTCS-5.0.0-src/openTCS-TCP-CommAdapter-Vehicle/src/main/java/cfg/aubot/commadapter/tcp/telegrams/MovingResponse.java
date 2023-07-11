package cfg.aubot.commadapter.tcp.telegrams;

import com.google.common.primitives.Ints;
import org.opentcs.example.telegrams.Response;

import static com.google.common.base.Ascii.ETX;
import static com.google.common.base.Ascii.STX;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class MovingResponse extends Response {
    /**
     * The response type.
     */
    public static final byte TYPE = 4;
    /**
     * The expected length of a telegram of this type.
     */
    public static final int TELEGRAM_LENGTH = 7;
    /**
     * The size of the payload (the raw content, without STX, SIZE, CHECKSUM and ETX).
     */
    public static final int PAYLOAD_LENGTH = TELEGRAM_LENGTH - 4;
    /**
     * The position of the checksum byte.
     */
    public static final int CHECKSUM_POS = TELEGRAM_LENGTH - 2;
    /**
     * Creates a new instance.
     *
     * @param telegramData This telegram's raw content.
     */
    public MovingResponse(byte[] telegramData) {
        super(TELEGRAM_LENGTH);
        requireNonNull(telegramData, "telegramData");
        checkArgument(telegramData.length == TELEGRAM_LENGTH);

        System.arraycopy(telegramData, 0, rawContent, 0, TELEGRAM_LENGTH);
        decodeTelegramContent();
    }

    private void decodeTelegramContent() {
        this.id = Ints.fromBytes((byte) 0, (byte) 0, rawContent[4], rawContent[3]);
    }

    public static boolean isMovingResponse(byte[] telegramData) {
        requireNonNull(telegramData, "data");
        int payloadLength = telegramData[1] & 0xFF;

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
        else if (payloadLength != PAYLOAD_LENGTH) {
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
