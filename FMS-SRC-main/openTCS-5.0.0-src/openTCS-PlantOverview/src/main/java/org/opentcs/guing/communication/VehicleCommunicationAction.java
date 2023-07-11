package org.opentcs.guing.communication;

import com.google.inject.assistedinject.Assisted;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.management.AttachmentInformation;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import org.opentcs.guing.application.action.ActionFactory;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.ResourceBundleUtil;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionEvent;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.VEHICLEPOPUP_PATH;

public class VehicleCommunicationAction extends AbstractAction {

  /**
   * Resource bundle
   */
  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(VEHICLEPOPUP_PATH);
  /**
   * The vehicle.
   */
  private final VehicleModel vehicleModel;
  /**
   * Action factory.
   */
  private final ActionFactory actionFactory;
  /**
   * The application's main frame.
   */
  private final JFrame applicationFrame;
  /**
   * Share portal provider.
   */
  private final SharedKernelServicePortalProvider sharedProvider;

  @Inject
  public VehicleCommunicationAction(@Assisted VehicleModel vehicle,
                                    @Assisted ActionFactory actionFactory,
                                    @ApplicationFrame JFrame applicationFrame,
                                    SharedKernelServicePortalProvider sharedProvider) {
    this.vehicleModel = requireNonNull(vehicle, "vehicle");
    this.actionFactory = requireNonNull(actionFactory, "actionFactory");
    this.applicationFrame = requireNonNull(applicationFrame, "applicationFrame");
    this.sharedProvider = requireNonNull(sharedProvider, "sharedProvider");

    putValue(NAME, BUNDLE.getString("communication.name"));
  }
  /**
   * Invoked when an action occurs.
   *
   * @param e
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    try (SharedKernelServicePortal sharedService = sharedProvider.register()) {
      VehicleService vehicleService = sharedService.getPortal().getVehicleService();
      TCSObjectReference<Vehicle> vehicleRef = vehicleModel.getVehicle().getReference();
      AttachmentInformation ai = vehicleService.fetchAttachmentInformation(vehicleRef);
      VehicleProcessModelTO processModel = vehicleService.fetchProcessModel(vehicleRef);
      DialogContent content = actionFactory.createCommunicationPanel(sharedService.getPortal(), new LocalVehicleEntry(ai, processModel));
      StandardContentDialog dialog = new StandardContentDialog(applicationFrame, content, true, StandardContentDialog.CLOSE);
      dialog.setSize(600, 600);
      dialog.setVisible(true);
    }

  }
}
