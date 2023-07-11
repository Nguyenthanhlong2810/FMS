package org.opentcs.guing.communication;

import com.google.inject.assistedinject.Assisted;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.UserMessageHelper;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.*;
import java.awt.event.ActionEvent;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.CREATE_EDIT_VEHICLE_PATH;

public class UpdateVehicleAction extends AbstractAction implements AttributesChangeListener {
    public static final String ID = "communication.updateVehicle";

    private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(CREATE_EDIT_VEHICLE_PATH);

    private final Provider<CreateEditVehiclePanel> panelProvider;
    /**
     * The vehicle.
     */
    private final VehicleModel vehicleModel;

    @Inject
    public UpdateVehicleAction(@Assisted VehicleModel vehicle,
                               Provider<CreateEditVehiclePanel> panelProvider) {
        this.vehicleModel = vehicle;
        this.panelProvider = requireNonNull(panelProvider, "panelProvider");

        putValue(NAME, BUNDLE.getString("editVehicle.action.title"));
        putValue(SHORT_DESCRIPTION, BUNDLE.getString("editVehicle.action.helptext"));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        CreateEditVehiclePanel content = panelProvider.get();
        content.setVehicle(vehicleModel.getVehicle());
        content.setVisible(true);
//        content.setUpdatedVehicle(vehicleModel.getVehicle());
//        StandardContentDialog dialog = new StandardContentDialog(applicationFrame, content);
//        dialog.setVisible(true);
//        if (dialog.getReturnStatus() != StandardContentDialog.RET_OK) {
//            return;
//        }
//        UserMessageHelper.ReturnType returnType = userMessageHelper.showConfirmDialog("update vehicle","Are you sure to update this vehicle", UserMessageHelper.Type.INFO);
//        if(returnType != UserMessageHelper.ReturnType.OK){
//            return;
//        }

//        Vehicle vehicle = content.getUpdatedVehicle();
//        try (SharedKernelServicePortal servicePortal = portalProvider.register()) {
//            KernelServicePortal portal = servicePortal.getPortal();
//            portal.getVehicleService().updateVehicle(vehicle);
//        }catch (KernelRuntimeException ex) {
//            userMessageHelper.showMessageDialog("Error", ex.getMessage(), UserMessageHelper.Type.ERROR);
//        }
    }

    @Override
    public void propertiesChanged(AttributesChangeEvent e) {

    }
}
