package org.opentcs.guing.application.action.view;

import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.drivers.vehicle.commands.EnableCommAdapterCommand;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionEvent;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.TOOLBAR_PATH;

public class EnableAllVehiclesAction extends AbstractAction {

    /**
     * This action's ID.
     */
    public final static String ID = "openTCS.moveAllVehicles";

    private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(TOOLBAR_PATH);
    /**
     * This class's logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DisableAllVehiclesAction.class);
    /**
     * Provides the current system model.
     */
    private final ModelManager modelManager;
    /**
     * Provides access to a portal.
     */
    private final SharedKernelServicePortalProvider portalProvider;
    /**
     * Creates a new instance.
     *  @param modelManager Provides the current system model.
     * @param portalProvider Provides access to a portal.
     */
    @Inject
    public EnableAllVehiclesAction(ModelManager modelManager,
                                   SharedKernelServicePortalProvider portalProvider) {
        this.modelManager = requireNonNull(modelManager, "modelManager");
        this.portalProvider = requireNonNull(portalProvider, "portalProvider");

        putValue(NAME, BUNDLE.getString("enableAllVehiclesAction.name"));
        putValue(SHORT_DESCRIPTION, BUNDLE.getString("enableAllVehiclesAction.shortDescription"));

        ImageIcon iconSmall = ImageDirectory.getImageIcon("/toolbar/connect.png");
        ImageIcon iconLarge = ImageDirectory.getImageIcon("/toolbar/connect.png");
        putValue(SMALL_ICON, iconSmall);
        putValue(LARGE_ICON_KEY, iconLarge);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
            ModelComponent folder
                    = modelManager.getModel().getMainFolder(SystemModel.FolderKey.VEHICLES);

            if (portalProvider.portalShared()) {
                for (ModelComponent component : folder.getChildComponents()) {
                    VehicleModel vModel = (VehicleModel) component;
                    sharedPortal.getPortal()
                            .getVehicleService()
                            .sendCommAdapterCommand(vModel.getVehicle().getReference(),
                                    new EnableCommAdapterCommand(true));
                }
            }
        }
        catch (ServiceUnavailableException exc) {
            LOG.warn("Could not connect to kernel", exc);
        }
    }
}
