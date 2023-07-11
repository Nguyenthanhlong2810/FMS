package org.opentcs.guing.application.action.view;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.common.MoveState;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.opentcs.guing.util.I18nPlantOverview.TOOLBAR_PATH;

public class MoveVehicleAction extends AbstractAction {

  private static final Logger LOG = LoggerFactory.getLogger(MoveVehicleAction.class);

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(TOOLBAR_PATH);

  private final boolean move;

  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;

  private final VehicleModel vModel;

  @Inject
  public MoveVehicleAction(@Assisted VehicleModel vModel,
                           @Assisted boolean move,
                           SharedKernelServicePortalProvider portalProvider) {
    this.move = move;
    this.vModel = vModel;
    this.portalProvider = portalProvider;
    ImageIcon iconSmall;
    ImageIcon iconLarge;
    if (move) {
//      putValue(NAME, BUNDLE.getString("moveAllVehiclesAction.name"));
//      putValue(SHORT_DESCRIPTION, BUNDLE.getString("moveAllVehiclesAction.shortDescription"));
      iconSmall = ImageDirectory.getImageIcon("/toolbar/move-vehicles.16.png");
      iconLarge = ImageDirectory.getImageIcon("/toolbar/move-vehicles.22.png");
    } else {
//      putValue(NAME, BUNDLE.getString("pauseAllVehiclesAction.name"));
//      putValue(SHORT_DESCRIPTION, BUNDLE.getString("pauseAllVehiclesAction.shortDescription"));
      iconSmall = ImageDirectory.getImageIcon("/toolbar/pause-vehicles.16.png");
      iconLarge = ImageDirectory.getImageIcon("/toolbar/pause-vehicles.22.png");
    }

    putValue(SMALL_ICON, iconSmall);
    putValue(LARGE_ICON_KEY, iconLarge);
  }


  @Override
  public void actionPerformed(ActionEvent e) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      if (portalProvider.portalShared()) {
        sharedPortal.getPortal()
                .getVehicleService()
                .sendCommAdapterMessage(vModel.getVehicle().getReference(),
                        move ? MoveState.MOVE : MoveState.STOP);
      }
    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Could not connect to kernel", exc);
    }
  }
}
