package org.opentcs.virtualvehicle;

import org.opentcs.drivers.vehicle.ValidateException;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.VehicleCommunicationInfoPanel;
import org.opentcs.drivers.vehicle.management.VehicleCommunicationInfoPanelFactory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LoopbackCommunicationInfoPanelFactory implements VehicleCommunicationInfoPanelFactory {

    @Override
    public VehicleCommAdapterDescription getDescription() {
        return new LoopbackCommunicationAdapterDescription();
    }

    @Override
    public VehicleCommunicationInfoPanel getPanel() {
        return new VehicleCommunicationInfoPanel() {

            @Override
            public void setCommInfo(Map<String,String> info) {}

            @Override
            public Map<String, String> getCommInfo() {
                return new HashMap<>();
            }

            @Override
            public int getMinHeight() { return 0; }

            @Override
            public void handleException(ValidateException ex) {}
        };
    }
}
