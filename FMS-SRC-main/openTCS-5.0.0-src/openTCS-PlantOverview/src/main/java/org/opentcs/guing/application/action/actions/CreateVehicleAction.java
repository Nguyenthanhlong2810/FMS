/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.model.elements.VehicleModel;
import static org.opentcs.guing.util.I18nPlantOverview.TOOLBAR_PATH;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to trigger the creation of a vehicle.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class CreateVehicleAction
    extends AbstractAction {

  /**
   * This action class's ID.
   */
  public static final String ID = "openTCS.createVehicle";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(TOOLBAR_PATH);
  /**
   * The GUI manager instance we're working with.
   */
  private final GuiManager guiManager;

  /**
   * Creates a new instance.
   *
   * @param guiManager The GUI manager instance we're working with.
   */
  public CreateVehicleAction(GuiManager guiManager) {
    this.guiManager = guiManager;

    putValue(NAME, BUNDLE.getString("createVehicleAction.name"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("createVehicleAction.shortDescription"));

    ImageIcon icon = ImageDirectory.getImageIcon("/toolbar/vehicle.png");
    putValue(SMALL_ICON, icon);
    putValue(LARGE_ICON_KEY, icon);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    guiManager.createModelComponent(VehicleModel.class);
  }
}
