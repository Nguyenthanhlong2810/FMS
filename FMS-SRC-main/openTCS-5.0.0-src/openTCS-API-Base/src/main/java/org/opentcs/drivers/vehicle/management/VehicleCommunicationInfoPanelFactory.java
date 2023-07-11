package org.opentcs.drivers.vehicle.management;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

import javax.annotation.Nonnull;
import java.util.Set;

public interface VehicleCommunicationInfoPanelFactory {

    VehicleCommAdapterDescription getDescription();

    VehicleCommunicationInfoPanel getPanel();
}
