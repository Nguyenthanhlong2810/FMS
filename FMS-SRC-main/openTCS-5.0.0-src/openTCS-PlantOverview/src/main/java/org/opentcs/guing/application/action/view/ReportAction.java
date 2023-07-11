package org.opentcs.guing.application.action.view;

import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.action.report.form.ReportFrame;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.ActionEvent;

public class ReportAction extends AbstractAction {

    /**
     * This action's ID.
     */
    public final static String ID = "actions.openReport";

    private OpenTCSView view;

    public ReportAction(OpenTCSView view) {
        this.view = view;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new ReportFrame(view).setVisible(true);

    }
}
