package cfg.aubot.commadapter.tcp.telegrams;

import com.google.common.primitives.Ints;
import org.opentcs.example.telegrams.Response;

import static com.google.common.base.Ascii.ETX;
import static com.google.common.base.Ascii.STX;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class ErrorResponse extends Response {
    /**
     * The response type.
     */
    public static final byte TYPE = 3;
    /**
     * The expected length of a telegram of this type.
     */
    public static final int TELEGRAM_LENGTH = 207;
    /**
     * The size of the payload (the raw content, without STX, SIZE, CHECKSUM and ETX).
     */
    public static final int PAYLOAD_LENGTH = TELEGRAM_LENGTH - 4;
    /**
     * The position of the checksum byte.
     */
    public static final int CHECKSUM_POS = TELEGRAM_LENGTH - 2;
    /**
     * error message of vehicle
     */
    private String message;
    /**
     * Get message method
     */
    public String getMessage() {
        return message;
    }

    /**
     * Creates a new instance.
     *
     * @param telegramData This telegram's raw content.
     */
    public ErrorResponse(byte[] telegramData) {
        super(TELEGRAM_LENGTH);
        requireNonNull(telegramData, "telegramData");
        checkArgument(telegramData.length == TELEGRAM_LENGTH);

        System.arraycopy(telegramData, 0, rawContent, 0, TELEGRAM_LENGTH);
        decodeTelegramContent();
    }

    private void decodeTelegramContent() {
        this.id = Ints.fromBytes((byte) 0, (byte) 0, rawContent[3], rawContent[4]);
        byte[] messageByte = new byte[PAYLOAD_LENGTH - 2];
        System.arraycopy(rawContent, 5, messageByte, 0, PAYLOAD_LENGTH - 3);
        message = new String(messageByte);
    }

    public static boolean isErrorResponse(byte[] telegramData) {
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
