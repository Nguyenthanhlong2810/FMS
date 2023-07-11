package org.opentcs.drivers.vehicle.management;

import org.opentcs.drivers.vehicle.ValidateException;

import javax.swing.JPanel;
import java.util.Map;

public abstract class VehicleCommunicationInfoPanel extends JPanel {

    public abstract void setCommInfo(Map<String,String> info);

    public abstract Map<String,String> getCommInfo();

    public abstract int getMinHeight();

    public abstract void handleException(ValidateException ex);
}
