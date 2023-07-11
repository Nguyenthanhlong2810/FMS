package cfg.aubot.commadapter.tcp.validator;

import cfg.aubot.commadapter.tcp.I18nVehicleTcpConnectionInfo;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.drivers.vehicle.ValidateException;
import org.opentcs.drivers.vehicle.VehicleCommInfoValidator;
import org.opentcs.data.model.Vehicle;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.opentcs.data.model.Vehicle.PREFERRED_ADAPTER;
import static org.opentcs.example.VehicleProperties.PROPKEY_VEHICLE_HOST;
import static org.opentcs.example.VehicleProperties.PROPKEY_VEHICLE_PORT;

public class TcpCommAdapterValidator implements VehicleCommInfoValidator {

  private final I18nVehicleTcpConnectionInfo i18n;

  private static final String IP_ADDRESS_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(I18nVehicleTcpConnectionInfo.BUNDLE_PATH);

  private final String propKeyVehicleHost;

  private final String propKeyVehiclePort;

  @Inject
  public TcpCommAdapterValidator(I18nVehicleTcpConnectionInfo i18n) {
    this.i18n = requireNonNull(i18n, "I18nVehicleTcpConnectionInfo");
    propKeyVehicleHost = i18n.host;
    propKeyVehiclePort = i18n.port;
  }

  @Override
  public Set<String> validateVehicles(List<VehicleCreationTO> vehicles) {
    Set<String> errors = new HashSet<>();
    Map<String, String> hosts = new HashMap<>();
    Map<String, String> ports = new HashMap<>();
    vehicles.forEach(vehicle -> {
      if (vehicle.getProperties().containsKey(propKeyVehicleHost)) {
        Set<String> errs = new HashSet<>();
        String host = vehicle.getProperties().get(propKeyVehicleHost).trim();
        String port = vehicle.getProperties().get(propKeyVehiclePort);
        if (!isHostValid(host)) {
          errs.add(addVehicleInfo(vehicle, BUNDLE.getString("validate.invalidHost")));
        }
        port = port == null ? String.valueOf(i18n.DEFAULT_PORT) : port.trim();
        if (!isPortValid(port)) {
          errs.add(addVehicleInfo(vehicle, BUNDLE.getString("validate.invalidPort")));
        }
        if (hosts.containsKey(host)) {
          String existsVehicle = hosts.get(host);
          String vehiclePort = ports.get(existsVehicle);
          if (port.equals(vehiclePort)) {
            errs.add(addVehicleInfo(vehicle, MessageFormat.format(BUNDLE.getString("validate.hostPortExists"), host, port)));
          }
        }
        if (errs.isEmpty()) {
          hosts.put(host, vehicle.getName());
          ports.put(vehicle.getName(), port);
        } else {
          errors.addAll(errs);
        }
      }
    });

    return errors;
  }

  @Override
  public boolean validateVehicleCommInfo(Vehicle vehicle, Set<Vehicle> vehicles) throws ValidateException {
    String host = vehicle.getProperty(PROPKEY_VEHICLE_HOST);
    String port = vehicle.getProperty(PROPKEY_VEHICLE_PORT);

    if (!isHostValid(host)) {
      throw new FieldInvalidException(FieldInvalidException.INVALID_HOST);
    }
    if (!isPortValid(port)) {
      throw new FieldInvalidException(FieldInvalidException.INVALID_PORT);
    }

    Vehicle foundedVehicle = vehicles.stream()
            .filter(v -> Objects.equals(v.getProperty(PREFERRED_ADAPTER), vehicle.getProperty(PREFERRED_ADAPTER)))
            .filter(v -> Objects.equals(v.getProperty(PROPKEY_VEHICLE_HOST), host)
                      && Objects.equals(v.getProperty(PROPKEY_VEHICLE_PORT), port))
            .findFirst().orElse(null);

    if (foundedVehicle == null) {
      return true;
    }
    if (vehicle.getName().equals(foundedVehicle.getName())) {
      return true;
    } else {
      throw new HostPortDuplicateException(foundedVehicle);
    }
  }

  public boolean isHostValid(String host) {
    return host.matches(IP_ADDRESS_REGEX);
  }

  public boolean isPortValid(String port) {
    try {
    int p = Integer.parseInt(port);
    return p >= 0 && p <= 65535;
    } catch (Exception e) {
      return false;
    }
  }

  private String addVehicleInfo(VehicleCreationTO vehicle, String error) {
    return "[" + vehicle.getName() + "] " + error;
  }
}
