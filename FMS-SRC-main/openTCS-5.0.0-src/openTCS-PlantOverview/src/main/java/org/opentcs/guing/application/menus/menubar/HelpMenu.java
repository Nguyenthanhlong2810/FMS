/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.menus.menubar;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.*;

import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.action.ViewActionMap;
import org.opentcs.guing.application.action.app.AboutAction;
import org.opentcs.guing.components.dialogs.EmailAddressForm;
import org.opentcs.guing.application.action.configuration.OpenConfigurationTableAction;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

import java.awt.event.ActionEvent;

/**
 * The application's "Help" menu.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class HelpMenu
    extends JMenu {

  private JMenuItem menuItemConfig;

  private JMenuItem menuItemAddressList;
  /**
   * A menu item for showing the application's "about" panel.
   */
  private final JMenuItem menuItemAbout;

  private final JMenuItem languageMenu;

  /**
   * Creates a new instance.
   *
   * @param actionMap The application's action map.
   */
  @Inject
  public HelpMenu(ViewActionMap actionMap) {
    requireNonNull(actionMap, "actionMap");
    
    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle(I18nPlantOverview.MENU_PATH);
    
    this.setText( labels.getString("helpMenu.text"));
    this.setToolTipText(labels.getString("helpMenu.tooltipText"));
    this.setMnemonic('?');

    languageMenu = add(new LanguageMenu());

//    menuItemConfig = add(actionMap.get(OpenConfigurationTableAction.ID));

//    menuItemAddressList = add(new AbstractAction() {
//      @Override
//      public void actionPerformed(ActionEvent e) {
//        new EmailAddressForm().setVisible(true);
//      }
//    });
//    menuItemAddressList.setText("Email");

    menuItemAbout = add(actionMap.get(AboutAction.ID));
  }

  /**
   * Updates the menu's items for the given mode of operation.
   *
   * @param mode The new mode of operation.
   */
  public void setOperationMode(OperationMode mode) {
    requireNonNull(mode, "mode");

  }
}
