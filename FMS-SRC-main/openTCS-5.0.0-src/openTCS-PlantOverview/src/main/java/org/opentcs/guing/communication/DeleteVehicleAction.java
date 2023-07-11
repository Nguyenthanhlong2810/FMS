package org.opentcs.guing.communication;

import com.google.inject.assistedinject.Assisted;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.UserMessageHelper;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionEvent;

import static java.util.Objects.requireNonNull;

public class DeleteVehicleAction extends AbstractAction implements AttributesChangeListener {
    public static final String ID = "communication.deleteVehicle";

    private final SharedKernelServicePortalProvider portalProvider;

    private final UserMessageHelper userMessageHelper;

    /**
     * The vehicle.
     */
    private final VehicleModel vehicleModel;

    @Inject
    public DeleteVehicleAction(@Assisted VehicleModel vehicle,
                               SharedKernelServicePortalProvider portalProvider,
                               UserMessageHelper userMessageHelper) {
        this.vehicleModel = vehicle;
        this.portalProvider = requireNonNull(portalProvider, "portalProvider");
        this.userMessageHelper = requireNonNull(userMessageHelper, "userMessageHelper");

        putValue(NAME, "Delete vehicle");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        UserMessageHelper.ReturnType returnType = userMessageHelper.showConfirmDialog("Delete vehicle","Are you sure to delete this vehicle", UserMessageHelper.Type.INFO);
        if(returnType != UserMessageHelper.ReturnType.OK){
            return;
        }
        try (SharedKernelServicePortal servicePortal = portalProvider.register()) {
            KernelServicePortal portal = servicePortal.getPortal();
            portal.getVehicleService().removeVehicle(vehicleModel.getVehicle());
        }catch (KernelRuntimeException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void propertiesChanged(AttributesChangeEvent e) {

    }
}
