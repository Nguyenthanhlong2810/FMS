package org.opentcs.guing.application.action.course;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.RouteDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.event.ActionEvent;

import static java.util.Objects.requireNonNull;

public class ShowRouteAction extends AbstractAction {

    private static final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.ROUTE_PATH);

    private static final Logger LOG = LoggerFactory.getLogger(ShowRouteAction.class);

    private final VehicleModel vehicle;

    private final SharedKernelServicePortalProvider portalProvider;

    private final OpenTCSDrawingEditor drawingEditor;

    private final ModelManager modelManager;

    @Inject
    public ShowRouteAction(@Assisted VehicleModel vehicle,
                           SharedKernelServicePortalProvider portalProvider,
                           OpenTCSDrawingEditor drawingEditor,
                           ModelManager modelManager) {
        this.vehicle = requireNonNull(vehicle, "vehicle");
        this.portalProvider = requireNonNull(portalProvider, "portalProvider");
        this.drawingEditor = requireNonNull(drawingEditor, "tcsDrawingView");
        this.modelManager = requireNonNull(modelManager, "modelManager");

        putValue(NAME, bundle.getString("title"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new RouteDialog(vehicle, portalProvider, drawingEditor, modelManager).setVisible(true);
    }
}
