package org.opentcs.guing.application.menus.menubar;

import org.opentcs.guing.application.action.ViewActionMap;
import org.opentcs.guing.application.action.view.VehicleStatisticsAction;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

import javax.inject.Inject;
import javax.swing.*;

public class ViewVehicleStatisticReport extends JMenuItem {
  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(I18nPlantOverview.STATISTICS_PATH);

  @Inject
  public ViewVehicleStatisticReport(ViewActionMap actionMap){
    this.setText(BUNDLE.getString("statisticPanel.title"));
    addActionListener(actionMap.get(VehicleStatisticsAction.ID));
  }
}
