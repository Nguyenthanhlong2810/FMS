package org.opentcs.guing.application.action.configuration;

import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.dialogs.ConfigurationForm;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenConfigurationTableAction extends AbstractAction {

    private static final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.CONFIGURATION_PATH);

    public final static String ID = "actions.openConfigurationTable";

    private OpenTCSView view;

    public OpenConfigurationTableAction(OpenTCSView view) {
        this.view = view;
        putValue(NAME, bundle.getString("title"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new ConfigurationForm(view).setVisible(true);
    }
}
