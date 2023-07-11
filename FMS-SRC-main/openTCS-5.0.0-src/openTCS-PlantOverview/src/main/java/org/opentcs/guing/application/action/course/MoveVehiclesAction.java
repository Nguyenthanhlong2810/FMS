package org.opentcs.guing.application.action.course;

import com.google.inject.assistedinject.Assisted;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.common.MoveState;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class MoveVehiclesAction extends AbstractAction {

    private static final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.VEHICLEPOPUP_PATH);

    private static final Logger LOG = LoggerFactory.getLogger(MoveVehiclesAction.class);

    private final MoveState moveState;

    private final Collection<VehicleModel> vehicles;

    private final SharedKernelServicePortalProvider portalProvider;

    @Inject
    public MoveVehiclesAction(@Assisted MoveState moveState,
                              @Assisted Collection<VehicleModel> vehicles,
                              SharedKernelServicePortalProvider portalProvider) {
        this.moveState = requireNonNull(moveState, "moveState");
        this.vehicles = requireNonNull(vehicles, "vehicles");
        this.portalProvider = requireNonNull(portalProvider, "portalProvider");

        if (moveState == MoveState.MOVE) {
            putValue(NAME, bundle.getString("movingVehicle.run.text"));
        } else {
            putValue(NAME, bundle.getString("movingVehicle.stop.text"));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
            for (VehicleModel vehicle : vehicles) {
                sharedPortal.getPortal()
                        .getVehicleService()
                        .sendCommAdapterMessage(vehicle.getVehicle().getReference(), moveState);
            }
        }
        catch (KernelRuntimeException ex) {
            LOG.warn("Unexpected exception", ex);
        }
    }
}
