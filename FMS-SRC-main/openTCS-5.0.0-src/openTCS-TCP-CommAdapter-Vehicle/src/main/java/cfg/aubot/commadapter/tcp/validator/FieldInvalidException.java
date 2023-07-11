package cfg.aubot.commadapter.tcp.validator;

public class FieldInvalidException extends TcpValidateException {

  public static final int INVALID_HOST = 1;
  public static final int INVALID_PORT = 2;

  private final int type;

  public FieldInvalidException(int type) {
    super();
    this.type = type;
  }

  public int getType() {
    return type;
  }
}
