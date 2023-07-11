package org.opentcs.guing.communication;

import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.UserMessageHelper;
import org.opentcs.hibernate.entities.VehicleEntity;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.CREATE_EDIT_VEHICLE_PATH;

public class CreateVehicleInOperatingAction extends AbstractAction implements AttributesChangeListener {

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(CREATE_EDIT_VEHICLE_PATH);

  public static final String ID = "communication.createVehicle";

  private final Provider<CreateEditVehiclePanel> panelProvider;

  @Inject
  public CreateVehicleInOperatingAction(Provider<CreateEditVehiclePanel> panelProvider) {
    this.panelProvider = requireNonNull(panelProvider, "panelProvider");

    putValue(SHORT_DESCRIPTION, BUNDLE.getString("createVehicle.action.helptext"));

    Icon icon = ImageDirectory.getImageIcon("/toolbar/vehicle.png");
    putValue(SMALL_ICON, icon);
    putValue(LARGE_ICON_KEY, icon);
  }

  /**
   * Invoked when an action occurs.
   *
   * @param e
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    CreateEditVehiclePanel content = panelProvider.get();
//    StandardContentDialog dialog = new StandardContentDialog(applicationFrame, content);
    content.setVisible(true);

//    if (dialog.getReturnStatus() != StandardContentDialog.RET_OK) {
//      return;
//    }
//    try (SharedKernelServicePortal servicePortal = portalProvider.register()) {
//      Vehicle vehicle = content.getCreatedVehicle();
//
//      if (vehicle != null) {
//        KernelServicePortal portal = servicePortal.getPortal();
//        Set<Vehicle> vehicleSet = portal.getVehicleService().fetchObjects(Vehicle.class);
//        for(Vehicle vhc : vehicleSet){
//          if(vhc.getName().equals(vehicle.getName())){
//            userMessageHelper.showMessageDialog("Create vehicle","This name vehicle is existed!", UserMessageHelper.Type.ERROR);
//            return;
//          }
//        }
//        portal.getVehicleService().createVehicle(vehicle);
//      }
//    } catch (KernelRuntimeException ex) {
//      userMessageHelper.showMessageDialog("Error", ex.getMessage(), UserMessageHelper.Type.ERROR);
//    }
  }

  /**
   * Information für den View, dass sich die Eigenschaften des Models geändert
   * haben. Der View ist nun selbst dafür zuständig, sich zu aktualisieren.
   *
   * @param e
   */
  @Override
  public void propertiesChanged(AttributesChangeEvent e) {

  }
}
