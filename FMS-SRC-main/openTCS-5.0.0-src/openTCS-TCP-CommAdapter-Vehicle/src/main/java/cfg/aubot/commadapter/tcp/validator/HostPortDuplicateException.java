package cfg.aubot.commadapter.tcp.validator;

import org.opentcs.data.model.Vehicle;

public class HostPortDuplicateException extends TcpValidateException {

  private final Vehicle vehicle;

  public HostPortDuplicateException(Vehicle duplicatedVehicle) {
    super();
    this.vehicle = duplicatedVehicle;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }
}
