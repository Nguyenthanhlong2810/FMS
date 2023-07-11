package org.opentcs.guing.application.action.course;

import org.jhotdraw.util.ResourceBundleUtil;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.action.ActionFactory;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.transport.VehicleRoutesPanel;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.UserMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.VEHICLE_ROUTES;

public class VehicleRoutesAction extends AbstractAction {

  /**
   * This action's ID.
   */
  public final static String ID = "aubot.vehicleRoutes";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(VEHICLE_ROUTES);
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VehicleRoutesAction.class);
  /**
   * The application's main frame.
   */
  private final JFrame applicationFrame;
  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  /**
   * openTCS view
   */
  private final OpenTCSView openTCSView;
  /**
   * Vehicle model
   */
  private VehicleModel vehicleModel;
  /**
   * Action factory
   */
  private final ActionFactory actionFactory;
  /**
   * Creates a new instance.
   *  @param applicationFrame Provides the application frame.
   * @param actionFactory Provides action factory.
   */
  @Inject
  public VehicleRoutesAction(@ApplicationFrame JFrame applicationFrame,
                             ModelManager modelManager,
                             OpenTCSView openTCSView,
                             ActionFactory actionFactory) {
    this.applicationFrame = requireNonNull(applicationFrame, "applicationFrame");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.openTCSView = requireNonNull(openTCSView, "openTCSView");
    this.actionFactory = requireNonNull(actionFactory, "actionFactory");

    putValue(NAME, BUNDLE.getString("title"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("shortDescription"));

    ImageIcon iconSmall = ImageDirectory.getImageIcon("/toolbar/route.png");
    ImageIcon iconLarge = ImageDirectory.getImageIcon("/toolbar/route.png");
    putValue(SMALL_ICON, iconSmall);
    putValue(LARGE_ICON_KEY, iconLarge);
  }

  public void setVehicleModel(VehicleModel vehicleModel) {
    this.vehicleModel = vehicleModel;
  }

  /**
   * Invoked when an action occurs.
   *
   * @param evt
   */
  @Override
  public void actionPerformed(ActionEvent evt) {
    if (modelManager.getModel().getGroupModels().size() == 0) {
      new UserMessageHelper().showMessageDialog("No group", BUNDLE.getString("error.noGroup"), UserMessageHelper.Type.INFO);
      return;
    }
    VehicleRoutesPanel content = actionFactory.createVehicleRoutesPanel(vehicleModel);
    content.setVisible(true);
  }
}
