package org.opentcs.guing.components.properties.type;

import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.model.elements.VehicleModel;

import java.util.Arrays;
import java.util.ResourceBundle;

import static org.opentcs.guing.I18nPlantOverviewBase.BUNDLE_PATH;

public class VehicleStateSelectionProperty extends SelectionProperty<Vehicle.State> {

  private final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);

  public VehicleStateSelectionProperty(VehicleModel model) {
    super(model, Arrays.asList(Vehicle.State.values()), Vehicle.State.UNKNOWN);
  }

  @Override
  public String toString() {
    Vehicle.State state = (Vehicle.State) getValue();
    return bundle.getString("vehicleModel.property_state." + state.name().toLowerCase());
  }
}
