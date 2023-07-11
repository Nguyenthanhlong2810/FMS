package cfg.aubot.commadapter.tcp.telegrams;

import com.google.common.primitives.Ints;
import org.opentcs.common.MoveState;
import org.opentcs.example.telegrams.Request;

import static com.google.common.base.Ascii.ETX;
import static com.google.common.base.Ascii.STX;

public class MovingRequest extends Request {
    /**
     * The request type.
     */
    public static final byte TYPE = 4;
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
    /**
     *
     */
    private final MoveState moveState;
    /**
     * Creates a new instance.
     *
     * @param requestId The request's id.
     */
    public MovingRequest(int requestId, MoveState moveState) {
        super(TELEGRAM_LENGTH);
        this.id = requestId;
        this.moveState = moveState;

        encodeTelegramContent();
    }

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

        rawContent[5] = decodeMoveState(moveState);

        rawContent[CHECKSUM_POS] = getCheckSum(rawContent);
        rawContent[TELEGRAM_LENGTH - 1] = ETX;
    }

    private byte decodeMoveState(MoveState moveState) {
        switch (moveState) {
            case MOVE:
                return 'M';
            case STOP:
                return 'S';
            default:
                return 0;
        }
    }
}
