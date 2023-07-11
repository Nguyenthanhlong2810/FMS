package cfg.aubot.commadapter.tcp;

import cfg.aubot.commadapter.tcp.exchange.ExampleCommAdapterDescription;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.VehicleCommunicationInfoPanel;
import org.opentcs.drivers.vehicle.management.VehicleCommunicationInfoPanelFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TcpCommunicationInfoPanelFactory implements VehicleCommunicationInfoPanelFactory {
    @Override
    public VehicleCommAdapterDescription getDescription() {
        return new ExampleCommAdapterDescription();
    }

    @Override
    public VehicleCommunicationInfoPanel getPanel() {
        return new TcpCommInfoPanel();
    }


}
