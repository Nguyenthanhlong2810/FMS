package cfg.aubot.commadapter.tcp.validator;

import org.opentcs.drivers.vehicle.ValidateException;

public class TcpValidateException extends ValidateException {

  public TcpValidateException() {
    super();
  }

  public TcpValidateException(String message) {
    super(message);
  }
}
