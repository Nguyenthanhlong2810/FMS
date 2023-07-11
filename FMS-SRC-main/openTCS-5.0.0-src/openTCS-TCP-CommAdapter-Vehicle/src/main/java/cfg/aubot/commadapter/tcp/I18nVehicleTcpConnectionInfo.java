package cfg.aubot.commadapter.tcp;

import javax.inject.Inject;
import java.util.ResourceBundle;

public class I18nVehicleTcpConnectionInfo {

  public static final String BUNDLE_PATH = "cfg.aubot.commadapter.tcp.Bundle";

  public final String host;

  public final String port;

  public final int DEFAULT_PORT = 2020;

  @Inject
  private I18nVehicleTcpConnectionInfo() {
    ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);
    host = bundle.getString("tcp.connection.info.host.name");
    port = bundle.getString("tcp.connection.info.port.name");
  }
}
