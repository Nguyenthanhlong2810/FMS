/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.dialogs;

import com.google.inject.assistedinject.Assisted;
import org.jhotdraw.draw.Figure;
import org.opentcs.common.VehicleError;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.WorkingRoute;
import org.opentcs.guing.application.menus.MenuFactory;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;
import org.opentcs.guing.components.properties.SelectionPropertiesComponent;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.tree.ComponentsTreeViewManager;
import org.opentcs.guing.components.tree.TreeViewManager;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.CourseObjectFactory;
import org.opentcs.guing.util.I18nPlantOverview;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Ein Fahrzeug im {@link VehiclesPanel}.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class SingleVehicleView
    extends JPanel
    implements AttributesChangeListener,
               Comparable<SingleVehicleView> {

  /**
   * The resource bundle this component uses.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle(I18nPlantOverview.VEHICLEVIEW_PATH);
  /**
   * The color definition for orange.
   */
  private static final Color RED = new Color(255, 27, 27, 192);
  /**
   * The color definition for orange.
   */
  private static final Color ORANGE = new Color(0xff, 0xdd, 0x75, 192);
  /**
   * The color definition for green.
   */
  private static final Color GREEN = new Color(0x77, 0xdb, 0x6c, 192);
  /**
   * Das darzustellende Fahrzeug.
   */
  private final VehicleModel fVehicleModel;
  /**
   * The tree view's manager (for selecting the vehicle when it's clicked on).
   */
  private final TreeViewManager treeViewManager;
  /**
   * The properties component (for displaying properties of the vehicle when
   * it's clicked on).
   */
  private final SelectionPropertiesComponent propertiesComponent;
  /**
   * The drawing editor (for accessing the currently active drawing view).
   */
  private final OpenTCSDrawingEditor drawingEditor;
  /**
   * A factory to create vehicle figures.
   */
  private final CourseObjectFactory crsObjFactory;
  /**
   * A factory for popup menus.
   */
  private final MenuFactory menuFactory;
  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * Die Zeichenfläche im Dialog.
   */
  private final JPanel fVehicleView;

  private VehicleFigure figure;

  private Vehicle.State previousState = null;
  /**
   * Creates new instance.
   *
   * @param vehicle The vehicle to be displayed.
   * @param treeViewManager The tree view's manager (for selecting the vehicle
   * when it's clicked on).
   * @param propertiesComponent The properties component (for displaying
   * properties of the vehicle when it's clicked on).
   * @param drawingEditor The drawing editor (for accessing the currently active
   * drawing view).
   * @param crsObjFactory A factory to create vehicle figures.
   * @param menuFactory A factory for popup menus.
   */
  @Inject
  public SingleVehicleView(@Assisted VehicleModel vehicle,
                           ComponentsTreeViewManager treeViewManager,
                           SelectionPropertiesComponent propertiesComponent,
                           OpenTCSDrawingEditor drawingEditor,
                           CourseObjectFactory crsObjFactory,
                           MenuFactory menuFactory,
                           ModelManager modelManager) {
    this.fVehicleModel = requireNonNull(vehicle, "vehicle");
    this.treeViewManager = requireNonNull(treeViewManager, "treeViewManager");
    this.propertiesComponent = requireNonNull(propertiesComponent,
                                              "propertiesComponent");
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.crsObjFactory = requireNonNull(crsObjFactory, "crsObjFactory");
    this.menuFactory = requireNonNull(menuFactory, "menuFactory");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.fVehicleView = new VehicleView(fVehicleModel);

    initComponents();
    initComponentsExtra();
    vehicle.addAttributesChangeListener(this);
    vehicleNameLabel.setText(vehicle.getName());
    updateVehicle();
  }

  private void initComponentsExtra() {
    setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(VehiclesPanel.NORMAL_BORDER_COLOR),
            BorderFactory.createEmptyBorder(5,5,5,5)));
    vehiclePanel.add(fVehicleView, BorderLayout.CENTER);
    integratedLabel.setVisible(false);
    integratedStateLabel.setVisible(false);
    routeNameLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        Vehicle vehicle = fVehicleModel.getVehicle();
        if (vehicle.getWorkingRoute().haveRoute()) {
          drawingEditor.getAllViews().forEach(view ->
                  view.focusGroup(modelManager.getModel().getGroupModel(vehicle.getWorkingRoute().getCurrentGroup().getName()), 3000));
        }
      }
    });

    btnRun.setAction(menuFactory.createMoveVehicleAction(fVehicleModel, true));
    btnStop.setAction(menuFactory.createMoveVehicleAction(fVehicleModel, false));
  }

  /**
   * Zeichnet das Fahrzeug in den Dialog
   *
   * @param g2d der Grafikkontext
   */
  private void drawVehicle(Graphics2D g2d) {
    figure = crsObjFactory.createVehicleFigure(fVehicleModel);
    figure.setfImage(fVehicleModel.getStateImage());
    figure.setIgnorePrecisePosition(true);
    // Figur im Dialog-Panel zentrieren
    // TODO: Maßstab berücksichtigen!
    Point2D.Double posDialog = new Point2D.Double(fVehicleView.getWidth() / 2, fVehicleView.getHeight() / 2);
    figure.setBounds(posDialog, null);
    figure.setAngle(0.0);
    figure.forcedDraw(g2d);
  }

  private void showPopup(int x, int y) {

    menuFactory.createVehiclePopupMenu(Arrays.asList(fVehicleModel)).show(this, x, y);
  }

  private void updateVehicle() {
//    updateVehicleIntegrationLevel();
    updateVehicleState();
    updateVehiclePosition();
    updateEnergyLevel();
    updateVoltageAndCurrent();
    updateVehicleDestination();
    updateVehicleCurrentRoute();

    revalidate();
  }

  private void updateVehicleCurrentRoute() {
    WorkingRoute workingRoute = getVehicleModel().getVehicle().getWorkingRoute();
    if (workingRoute.haveRoute()) {
      String groupName = workingRoute.getCurrentGroup().getName();
      routeNameLabel.setText(groupName);
      routeNameLabel.setOpaque(true);
      routeNameLabel.setBackground(modelManager.getModel().getGroupModel(groupName).getPropertyColor().getColor());
    } else {
      routeNameLabel.setText("-");
      routeNameLabel.setOpaque(false);
    }
  }

  private void updateVehicleDestination() {
//    List<ModelComponent> components = getVehicleModel().getDriveOrderComponents();
//    if (components != null && !components.isEmpty()) {
    if (fVehicleModel.getVehicle().getNextPosition() != null) {
      destinationValueLabel.setText(fVehicleModel.getVehicle().getNextPosition().getName());
    } else {
      destinationValueLabel.setText("-");
    }
  }

  private void updateVehicleIntegrationLevel() {
    Vehicle.IntegrationLevel integrationLevel
        = (Vehicle.IntegrationLevel) fVehicleModel.getPropertyIntegrationLevel().getValue();
    switch (integrationLevel) {
      case TO_BE_IGNORED:
      case TO_BE_NOTICED:
        integratedStateLabel.setText(BUNDLE.getString("singleVehicleView.label_integratedState.no.text"));
        integratedStateLabel.setOpaque(false);
        break;
      case TO_BE_RESPECTED:
        integratedStateLabel.setText(BUNDLE.getString("singleVehicleView.label_integratedState.partially.text"));
        integratedStateLabel.setOpaque(true);
        integratedStateLabel.setBackground(ORANGE);
        break;
      case TO_BE_UTILIZED:
        integratedStateLabel.setText(BUNDLE.getString("singleVehicleView.label_integratedState.fully.text"));
        integratedStateLabel.setOpaque(true);
        integratedStateLabel.setBackground(GREEN);
        break;
      default:
        integratedStateLabel.setText(integrationLevel.name());
        integratedStateLabel.setOpaque(false);
    }
  }

  private void updateVehicleState() {
    Vehicle.State state = (Vehicle.State) fVehicleModel.getPropertyState().getValue();

    switch (state) {
      case UNKNOWN:
        vehicleStateValueLabel.setText(BUNDLE.getString("singleVehicleView.vehicleState.unknown.text"));
        break;
      case ERROR:
        vehicleStateValueLabel.setText(BUNDLE.getString("singleVehicleView.vehicleState.error.text"));
        break;
      case IDLE:
        vehicleStateValueLabel.setText(BUNDLE.getString("singleVehicleView.vehicleState.idle.text"));
        break;
      case EXECUTING:
        vehicleStateValueLabel.setText(BUNDLE.getString("singleVehicleView.vehicleState.executing.text"));
        break;
      default:
        vehicleStateValueLabel.setText(state.name());
    }

    if (fVehicleModel.getVehicle().getState() != previousState || previousState == null) {
      fVehicleView.repaint();
      previousState = fVehicleModel.getVehicle().getState();
    }

    if (fVehicleModel.getVehicle().isWarning() && state != Vehicle.State.UNKNOWN) {
      vehicleStateValueLabel.setBackground(ORANGE);
      vehicleStateValueLabel.setOpaque(true);
      vehicleStateValueLabel.setText("WARNING");
    } else {
      switch (state) {
        case IDLE:
          vehicleStateValueLabel.setBackground(RED);
          vehicleStateValueLabel.setOpaque(true);
          break;
        case EXECUTING:
          vehicleStateValueLabel.setBackground(GREEN);
          vehicleStateValueLabel.setOpaque(true);
          break;
        case ERROR:
          vehicleStateValueLabel.setBackground(RED);
          vehicleStateValueLabel.setOpaque(true);
          StringBuilder toolTipText = new StringBuilder("<html>");
          VehicleError.decode(fVehicleModel.getVehicle().getErrorCode()).forEach(obj -> toolTipText.append(obj).append("\n"));
          toolTipText.append("</html>");
          vehicleStateValueLabel.setToolTipText(toolTipText.toString());
          break;
        case WARNING:
        case UNAVAILABLE:
        case UNKNOWN:
          vehicleStateValueLabel.setBackground(ORANGE);
          vehicleStateValueLabel.setOpaque(true);
          break;
        default:
          vehicleStateValueLabel.setOpaque(false);
          vehicleStateValueLabel.setForeground(Color.BLACK);
          vehicleStateValueLabel.setToolTipText(null);
      }
    }
  }

  private void updateVehiclePosition() {
    positionValueLabel.setText(fVehicleModel.getPropertyPoint().getText());
  }

  private void updateEnergyLevel() {
    batteryLabel.setText(fVehicleModel.getPropertyEnergyLevel().getValue() + " %");
    Vehicle vehicle = fVehicleModel.getVehicle();

    if (vehicle.isEnergyLevelCritical()) {
      batteryIcon.setIcon(new ImageIcon(getToolkit().getImage(getClass().getClassLoader().
          getResource("org/opentcs/guing/res/symbols/panel/battery-caution-3.png"))));
    }
    else if (vehicle.isEnergyLevelDegraded()) {
      batteryIcon.setIcon(new ImageIcon(getToolkit().getImage(getClass().getClassLoader().
          getResource("org/opentcs/guing/res/symbols/panel/battery-060-2.png"))));
    }
    else if (vehicle.isEnergyLevelGood()) {
      batteryIcon.setIcon(new ImageIcon(getToolkit().getImage(getClass().getClassLoader().
          getResource("org/opentcs/guing/res/symbols/panel/battery-100-2.png"))));
    }
  }

  private void updateVoltageAndCurrent() {
    float current = (float) fVehicleModel.getPropertyCurrent().getValue();
    float voltage = (float) fVehicleModel.getPropertyVoltage().getValue();

    currentLabel.setText(String.format("%4.2f",current) + "A");
    voltageLabel.setText(String.format("%4.2f",voltage) + "V");
  }

  public VehicleModel getVehicleModel() {
    return fVehicleModel;
  }

  public JPanel getfVehicleView() {
    return fVehicleView;
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    updateVehicle();

    if (figure == null) {
      drawVehicle((Graphics2D) getGraphics());
    }

    figure.propertiesChanged(e);
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    statusPanel = new javax.swing.JPanel();
    vehiclePanel = new javax.swing.JPanel();
    vehicleLabel = new javax.swing.JLabel();
    batteryPanel = new javax.swing.JPanel();
    batteryIcon = new javax.swing.JLabel();
    batteryLabel = new javax.swing.JLabel();
    propertiesPanel = new javax.swing.JPanel();
    integratedLabel = new javax.swing.JLabel();
    integratedStateLabel = new javax.swing.JLabel();
    vehicleStateLabel = new javax.swing.JLabel();
    vehicleStateValueLabel = new javax.swing.JLabel();
    positionLabel = new javax.swing.JLabel();
    destinationLabel = new javax.swing.JLabel();
    positionValueLabel = new javax.swing.JLabel();
    routeLabel = new javax.swing.JLabel();
    routeNameLabel = new javax.swing.JLabel();
    destinationValueLabel = new javax.swing.JLabel();
    currentLabel = new javax.swing.JLabel();
    voltageLabel = new javax.swing.JLabel();
    titlePanel = new javax.swing.JPanel();
    vehicleNameLabel = new javax.swing.JLabel();
    btnRun = new javax.swing.JButton();
    btnStop = new javax.swing.JButton();

    setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 30, 10, 30));
    setMinimumSize(new java.awt.Dimension(200, 59));
    setLayout(new java.awt.GridBagLayout());

    statusPanel.setLayout(new java.awt.BorderLayout());

    vehiclePanel.setLayout(new java.awt.BorderLayout());

    vehicleLabel.setFont(vehicleLabel.getFont());
    vehicleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    vehicleLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
    vehiclePanel.add(vehicleLabel, java.awt.BorderLayout.NORTH);

    statusPanel.add(vehiclePanel, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridheight = 2;
    add(statusPanel, gridBagConstraints);

    batteryPanel.setMinimumSize(new java.awt.Dimension(20, 14));
    batteryPanel.setPreferredSize(new java.awt.Dimension(45, 14));
    batteryPanel.setLayout(new java.awt.GridBagLayout());
    batteryPanel.add(batteryIcon, new java.awt.GridBagConstraints());

    batteryLabel.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    batteryLabel.setText("battery");
    batteryLabel.setMinimumSize(new java.awt.Dimension(100, 25));
    batteryLabel.setPreferredSize(new java.awt.Dimension(80, 25));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    batteryPanel.add(batteryLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weighty = 0.1;
    add(batteryPanel, gridBagConstraints);

    propertiesPanel.setLayout(new java.awt.GridBagLayout());

    integratedLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/panels/vehicleView"); // NOI18N
    integratedLabel.setText(bundle.getString("singleVehicleView.label_integrated.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    propertiesPanel.add(integratedLabel, gridBagConstraints);

    integratedStateLabel.setText(bundle.getString("singleVehicleView.label_integratedState.no.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
    propertiesPanel.add(integratedStateLabel, gridBagConstraints);

    vehicleStateLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
    vehicleStateLabel.setText(bundle.getString("singleVehicleView.label_state.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(vehicleStateLabel, gridBagConstraints);

    vehicleStateValueLabel.setText("UNAVAILABLE");
    vehicleStateValueLabel.setToolTipText("");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(vehicleStateValueLabel, gridBagConstraints);

    positionLabel.setText(bundle.getString("singleVehicleView.label_position.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(positionLabel, gridBagConstraints);

    destinationLabel.setText(bundle.getString("singleVehicleView.label_destination.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(destinationLabel, gridBagConstraints);

    positionValueLabel.setText("-");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(positionValueLabel, gridBagConstraints);

    routeLabel.setText(bundle.getString("singleVehicleView.label_route.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(routeLabel, gridBagConstraints);

    routeNameLabel.setText("-");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(routeNameLabel, gridBagConstraints);

    destinationValueLabel.setText("-");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
    propertiesPanel.add(destinationValueLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(propertiesPanel, gridBagConstraints);

    currentLabel.setText("current");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
    add(currentLabel, gridBagConstraints);

    voltageLabel.setText("voltage");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
    add(voltageLabel, gridBagConstraints);

    titlePanel.setLayout(new java.awt.GridBagLayout());

    vehicleNameLabel.setText("vehicle");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 0.1;
    titlePanel.add(vehicleNameLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    add(titlePanel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.weightx = 0.1;
    add(btnRun, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.weightx = 0.1;
    add(btnStop, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel batteryIcon;
  private javax.swing.JLabel batteryLabel;
  private javax.swing.JPanel batteryPanel;
  private javax.swing.JButton btnRun;
  private javax.swing.JButton btnStop;
  private javax.swing.JLabel currentLabel;
  private javax.swing.JLabel destinationLabel;
  private javax.swing.JLabel destinationValueLabel;
  private javax.swing.JLabel integratedLabel;
  private javax.swing.JLabel integratedStateLabel;
  private javax.swing.JLabel positionLabel;
  private javax.swing.JLabel positionValueLabel;
  private javax.swing.JPanel propertiesPanel;
  private javax.swing.JLabel routeLabel;
  private javax.swing.JLabel routeNameLabel;
  private javax.swing.JPanel statusPanel;
  private javax.swing.JPanel titlePanel;
  private javax.swing.JLabel vehicleLabel;
  private javax.swing.JLabel vehicleNameLabel;
  private javax.swing.JPanel vehiclePanel;
  private javax.swing.JLabel vehicleStateLabel;
  private javax.swing.JLabel vehicleStateValueLabel;
  private javax.swing.JLabel voltageLabel;
  // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON

  @Override
  public int compareTo(SingleVehicleView o) {
    return fVehicleModel.getName().compareTo(o.getVehicleModel().getName());
  }

  public class VehicleView
      extends JPanel {

    public VehicleView(VehicleModel vehicleModel) {
      requireNonNull(vehicleModel, "vehicleModel");

      setBackground(Color.WHITE);

      Figure vehicleFigure = modelManager.getModel().getFigure(vehicleModel);
      Rectangle2D.Double r2d = vehicleFigure == null
          ? new Rectangle2D.Double(0, 0, 30, 20)
          : vehicleFigure.getBounds();
      Rectangle r = r2d.getBounds();
      r.grow(10, 10);
      setPreferredSize(new Dimension(r.width, r.height));

      addMouseListener(new VehicleMouseAdapter(vehicleModel));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      drawVehicle((Graphics2D) g);
    }
  }

  private class VehicleMouseAdapter
      extends MouseAdapter {

    private final VehicleModel vehicleModel;

    public VehicleMouseAdapter(VehicleModel vehicleModel) {
      this.vehicleModel = requireNonNull(vehicleModel, "vehicleModel");
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
      if (evt.getButton() == MouseEvent.BUTTON1) {
        treeViewManager.selectItem(vehicleModel);
        propertiesComponent.setModel(vehicleModel);
      }

      if (evt.getClickCount() == 2) {
        Figure vehicleFigure = modelManager.getModel().getFigure(vehicleModel);
        drawingEditor.getActiveView().scrollTo(vehicleFigure);
      }

      if (evt.getButton() == MouseEvent.BUTTON3) {
        showPopup(evt.getX(), evt.getY());
      }
    }
  }
}
