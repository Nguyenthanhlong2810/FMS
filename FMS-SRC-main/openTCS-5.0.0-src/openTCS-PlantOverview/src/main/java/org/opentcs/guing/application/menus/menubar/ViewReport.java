package org.opentcs.guing.application.menus.menubar;

import org.opentcs.guing.application.action.ViewActionMap;
import org.opentcs.guing.application.action.view.ReportAction;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

import javax.inject.Inject;
import javax.swing.*;

public class ViewReport extends JMenuItem {

    private final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.REPORT_PATH);

    @Inject
    public ViewReport(ViewActionMap actionMap){
        this.setText(bundle.getString("title"));
        addActionListener(actionMap.get(ReportAction.ID));
    }
}
