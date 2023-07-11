/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.guing.components.dialogs;

import com.google.common.base.Strings;
import com.google.inject.assistedinject.Assisted;
import org.checkerframework.checker.units.qual.C;
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
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.CourseObjectFactory;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ImageDirectory;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 *
 * @author ADMIN
 */
public class SingleVehicleView2
    extends javax.swing.JPanel
        implements AttributesChangeListener,
        Comparable<SingleVehicleView2> {

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
//  private final JPanel fVehicleView;

  private VehicleFigure figure;

  private Vehicle.State previousState = null;

  private RealProgressBar pgbEnergy = new RealProgressBar();

  private Timer timer;

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
  public SingleVehicleView2(@Assisted VehicleModel vehicle,
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

    vehicle.addAttributesChangeListener(this);
    initComponents();
    initComponentsExtra();
    updateVehicle();
  }

  private void initComponentsExtra() {
    setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(VehiclesPanel.NORMAL_BORDER_COLOR),
            BorderFactory.createEmptyBorder(5,5,5,5)));
//    vehiclePanel.add(fVehicleView, BorderLayout.CENTER);
//    integratedLabel.setVisible(false);
//    integratedStateLabel.setVisible(false);
    btnRoute.addActionListener(e -> {
      Vehicle vehicle = fVehicleModel.getVehicle();
      if (vehicle.getWorkingRoute().haveRoute()) {
        drawingEditor.getAllViews().forEach(view ->
                view.focusGroup(modelManager.getModel().getGroupModel(vehicle.getWorkingRoute().getCurrentGroup().getName()), 3000));
      }
    });

    btnRun.setAction(menuFactory.createMoveVehicleAction(fVehicleModel, true));
    btnStop.setAction(menuFactory.createMoveVehicleAction(fVehicleModel, false));

    pnlEnergyArea.add(pgbEnergy, BorderLayout.CENTER);
    fVehicleModel.addAttributesChangeListener(this);
    lblVehicleName.setText(fVehicleModel.getName());
    lblVehicleName.getFont().deriveFont(Font.BOLD);
    ImageIcon imageIcon = ImageDirectory.getImageIcon("/vehicle/T500P-front.jpg");
    VehicleImagePanel vehicleImagePanel = new VehicleImagePanel(imageIcon.getImage(), true);
    lblVehicle.remove(lblVehicleImage);
    lblVehicle.add(vehicleImagePanel, BorderLayout.CENTER);

    vehicleImagePanel.addMouseListener(new VehicleMouseAdapter(fVehicleModel));

    BlinkVehicleTask blinkTask = new BlinkVehicleTask(this);
    timer = new Timer();
    timer.schedule(blinkTask,0,1000);
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
//    Point2D.Double posDialog = new Point2D.Double(fVehicleView.getWidth() / 2, fVehicleView.getHeight() / 2);
//    figure.setBounds(posDialog, null);
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
      btnRoute.setText(groupName);
      btnRoute.setOpaque(true);
      btnRoute.setBackground(modelManager.getModel().getGroupModel(groupName).getPropertyColor().getColor());
    } else {
      btnRoute.setText("-");
      btnRoute.setOpaque(false);
    }
  }

  private void updateVehicleDestination() {
//    List<ModelComponent> components = getVehicleModel().getDriveOrderComponents();
//    if (components != null && !components.isEmpty()) {
    if (fVehicleModel.getVehicle().getNextPosition() != null) {
      lblNextPosition.setText(fVehicleModel.getVehicle().getNextPosition().getName());
    } else {
      lblNextPosition.setText("-");
    }
  }

//  private void updateVehicleIntegrationLevel() {
//    Vehicle.IntegrationLevel integrationLevel
//            = (Vehicle.IntegrationLevel) fVehicleModel.getPropertyIntegrationLevel().getValue();
//    switch (integrationLevel) {
//      case TO_BE_IGNORED:
//      case TO_BE_NOTICED:
//        integratedStateLabel.setText(BUNDLE.getString("singleVehicleView.label_integratedState.no.text"));
//        integratedStateLabel.setOpaque(false);
//        break;
//      case TO_BE_RESPECTED:
//        integratedStateLabel.setText(BUNDLE.getString("singleVehicleView.label_integratedState.partially.text"));
//        integratedStateLabel.setOpaque(true);
//        integratedStateLabel.setBackground(ORANGE);
//        break;
//      case TO_BE_UTILIZED:
//        integratedStateLabel.setText(BUNDLE.getString("singleVehicleView.label_integratedState.fully.text"));
//        integratedStateLabel.setOpaque(true);
//        integratedStateLabel.setBackground(GREEN);
//        break;
//      default:
//        integratedStateLabel.setText(integrationLevel.name());
//        integratedStateLabel.setOpaque(false);
//    }
//  }

  private void updateVehicleState() {
    Vehicle.State state = (Vehicle.State) fVehicleModel.getPropertyState().getValue();

    switch (state) {
      case UNKNOWN:
        lblState.setText(BUNDLE.getString("singleVehicleView.vehicleState.unknown.text"));
        break;
      case ERROR:
        lblState.setText(BUNDLE.getString("singleVehicleView.vehicleState.error.text"));
        break;
      case IDLE:
        lblState.setText(BUNDLE.getString("singleVehicleView.vehicleState.idle.text"));
        break;
      case EXECUTING:
        lblState.setText(BUNDLE.getString("singleVehicleView.vehicleState.executing.text"));
        break;
      default:
        lblState.setText(state.name());
    }

    if (fVehicleModel.getVehicle().getState() != previousState || previousState == null) {
//      fVehicleView.repaint();.
      previousState = fVehicleModel.getVehicle().getState();
    }

    if (state == Vehicle.State.UNKNOWN) {
      lblState.setBackground(ORANGE);
      lblState.setOpaque(true);
    } else if (state == Vehicle.State.ERROR) {
      lblState.setBackground(RED);
      lblState.setOpaque(true);
      StringBuilder toolTipText = new StringBuilder("<html>");
      List<VehicleError> errors = VehicleError.decode(fVehicleModel.getVehicle().getErrorCode());
      VehicleError.decode(fVehicleModel.getVehicle().getErrorCode()).forEach(obj -> toolTipText.append(obj).append("<br>"));
      toolTipText.append("</html>");
      lblState.setToolTipText(toolTipText.toString());
      ToolTipManager.sharedInstance().setInitialDelay(0);
      lblState.setText(String.join("| ", errors.stream().map(VehicleError::name).collect(Collectors.toList())));
    } else if (fVehicleModel.getVehicle().isWarning()) {
      lblState.setBackground(ORANGE);
      lblState.setOpaque(true);
      lblState.setText("WARNING");
    } else if (state == Vehicle.State.IDLE) {
      lblState.setBackground(RED);
      lblState.setOpaque(true);
    } else if (state == Vehicle.State.EXECUTING) {
      lblState.setBackground(GREEN);
      lblState.setOpaque(true);
    } else {
      lblState.setOpaque(false);
      lblState.setForeground(Color.BLACK);
      lblState.setToolTipText(null);
      ToolTipManager.sharedInstance().setInitialDelay(750);
    }
  }

  private void updateVehiclePosition() {
    if (Strings.isNullOrEmpty(fVehicleModel.getPropertyPoint().getText())) {
      lblCurrentPosition.setText("-");
    } else {
      lblCurrentPosition.setText(fVehicleModel.getPropertyPoint().getText());
    }
  }

  private void updateEnergyLevel() {
    int energy = (int) fVehicleModel.getPropertyEnergyLevel().getValue();
    lblEnergyValue.setText(energy + "%");
    pgbEnergy.setValue(energy); // plus 100 for vertical progress bar display ?? :D ??
    Vehicle vehicle = fVehicleModel.getVehicle();

    if (vehicle.isEnergyLevelCritical()) {
      pgbEnergy.setForeground(new Color(255, 65, 65));
    } else if (vehicle.isEnergyLevelDegraded()) {
      pgbEnergy.setForeground(new Color(255, 173, 30));
    } else if (vehicle.isEnergyLevelGood()) {
      pgbEnergy.setForeground(new Color(0, 215, 77));
    }
  }

  private void updateVoltageAndCurrent() {
    float current = (float) fVehicleModel.getPropertyCurrent().getValue();
    float voltage = (float) fVehicleModel.getPropertyVoltage().getValue();

    lblCurrentValue.setText(String.format("%4.2f",current));
    lblVoltageValue.setText(String.format("%4.2f",voltage));
  }

  public VehicleModel getVehicleModel() {
    return fVehicleModel;
  }

//  public JPanel getfVehicleView() {
//    return fVehicleView;
//  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    updateVehicle();

    if (figure == null) {
      drawVehicle((Graphics2D) getGraphics());
    }

    figure.propertiesChanged(e);
  }

  @Override
  public int compareTo(SingleVehicleView2 o) {
    return fVehicleModel.getName().compareTo(o.getVehicleModel().getName());
  }

  private static class BlinkVehicleTask extends TimerTask
  {
    private boolean isDefaultColor;
    private final SingleVehicleView2 vehicleView;
    private final Color defaultColor;
    private static final Color ERROR_COLOR = new Color(255, 54, 54, 219);
    private static final Color WARNING_COLOR = new Color(241, 189, 1, 237);

    BlinkVehicleTask(SingleVehicleView2 vehicleView){
        this.vehicleView = vehicleView;
        this.defaultColor = vehicleView.getBackground();
    }
    @Override
    public void run() {
      final Vehicle vehicle = vehicleView.getVehicleModel().getVehicle();
      if(vehicle.hasState(Vehicle.State.UNKNOWN)){
        vehicleView.setBackground(defaultColor);
        return;
      }
      if (isDefaultColor) {
        if (vehicle.hasState(Vehicle.State.ERROR)) {
          vehicleView.setBackground(ERROR_COLOR);
        } else if (vehicle.isWarning()) {
          vehicleView.setBackground(WARNING_COLOR);
        }
      } else {
        vehicleView.setBackground(defaultColor);
      }
      isDefaultColor = !isDefaultColor;
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
  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    pnlInfo = new javax.swing.JPanel();
    pnlLocation = new javax.swing.JPanel();
    pnlPosition = new javax.swing.JPanel();
    lblCurrentPosition = new javax.swing.JLabel();
    lblNextSymbol = new javax.swing.JLabel();
    lblNextPosition = new javax.swing.JLabel();
    btnRoute = new javax.swing.JButton();
    pnlElectric = new javax.swing.JPanel();
    pnlCurrent = new javax.swing.JPanel();
    lblCurrentValue = new javax.swing.JLabel();
    lblCurrentSymbol = new javax.swing.JLabel();
    pnlVoltage = new javax.swing.JPanel();
    lblVoltageValue = new javax.swing.JLabel();
    lblVoltageSymbol = new javax.swing.JLabel();
    pnlEnergyArea = new javax.swing.JPanel();
    lblEnergyValue = new javax.swing.JLabel();
    pnlButtons = new javax.swing.JPanel();
    btnRun = new javax.swing.JButton();
    btnStop = new javax.swing.JButton();
    lblVehicle = new javax.swing.JPanel();
    lblState = new javax.swing.JLabel();
    lblVehicleImage = new javax.swing.JLabel();
    lblVehicleName = new javax.swing.JLabel();

    setLayout(new java.awt.BorderLayout());

    pnlInfo.setPreferredSize(new java.awt.Dimension(238, 72));
    pnlInfo.setLayout(new java.awt.GridBagLayout());

    pnlLocation.setLayout(new java.awt.GridLayout(0, 1));

    pnlPosition.setLayout(new java.awt.GridBagLayout());

    lblCurrentPosition.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    lblCurrentPosition.setText("-");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.weightx = 0.1;
    pnlPosition.add(lblCurrentPosition, gridBagConstraints);

    lblNextSymbol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/forward_report_24px.png"))); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    pnlPosition.add(lblNextSymbol, gridBagConstraints);

    lblNextPosition.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    lblNextPosition.setText("-");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.weightx = 0.1;
    pnlPosition.add(lblNextPosition, gridBagConstraints);

    pnlLocation.add(pnlPosition);

    btnRoute.setText("ROUTE");
    pnlLocation.add(btnRoute);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.6;
    pnlInfo.add(pnlLocation, gridBagConstraints);

    pnlElectric.setLayout(new java.awt.GridLayout(0, 1));

    pnlCurrent.setLayout(new java.awt.GridBagLayout());

    lblCurrentValue.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    lblCurrentValue.setText("5.14");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.weightx = 0.1;
    pnlCurrent.add(lblCurrentValue, gridBagConstraints);

    lblCurrentSymbol.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    lblCurrentSymbol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/ampere.png"))); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    pnlCurrent.add(lblCurrentSymbol, gridBagConstraints);

    pnlElectric.add(pnlCurrent);

    pnlVoltage.setLayout(new java.awt.GridBagLayout());

    lblVoltageValue.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    lblVoltageValue.setText("24.27");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.weightx = 0.1;
    pnlVoltage.add(lblVoltageValue, gridBagConstraints);

    lblVoltageSymbol.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    lblVoltageSymbol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/voltage.png"))); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    pnlVoltage.add(lblVoltageSymbol, gridBagConstraints);

    pnlElectric.add(pnlVoltage);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.4;
    pnlInfo.add(pnlElectric, gridBagConstraints);

    add(pnlInfo, java.awt.BorderLayout.PAGE_END);

    pnlEnergyArea.setPreferredSize(new java.awt.Dimension(56, 198));
    pnlEnergyArea.setLayout(new java.awt.BorderLayout());

    lblEnergyValue.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
    lblEnergyValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblEnergyValue.setText("100%");
    pnlEnergyArea.add(lblEnergyValue, java.awt.BorderLayout.SOUTH);

    add(pnlEnergyArea, java.awt.BorderLayout.LINE_START);

    pnlButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
    pnlButtons.setPreferredSize(new java.awt.Dimension(56, 177));
    pnlButtons.setLayout(new java.awt.GridLayout(0, 1, 0, 20));

    btnRun.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/play.png"))); // NOI18N
    pnlButtons.add(btnRun);

    btnStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/symbols/panel/pause.png"))); // NOI18N
    pnlButtons.add(btnStop);

    add(pnlButtons, java.awt.BorderLayout.LINE_END);

    lblVehicle.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
    lblVehicle.setLayout(new java.awt.BorderLayout());

    lblState.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblState.setText("STATE");
    lblVehicle.add(lblState, java.awt.BorderLayout.PAGE_END);

    lblVehicleImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/opentcs/guing/res/logos/cfg.png"))); // NOI18N
    lblVehicle.add(lblVehicleImage, java.awt.BorderLayout.CENTER);

    add(lblVehicle, java.awt.BorderLayout.CENTER);

    lblVehicleName.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    lblVehicleName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    lblVehicleName.setText("VEHICLE NAME");
    add(lblVehicleName, java.awt.BorderLayout.PAGE_START);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnRoute;
  private javax.swing.JButton btnRun;
  private javax.swing.JButton btnStop;
  private javax.swing.JLabel lblCurrentPosition;
  private javax.swing.JLabel lblCurrentSymbol;
  private javax.swing.JLabel lblCurrentValue;
  private javax.swing.JLabel lblEnergyValue;
  private javax.swing.JLabel lblNextPosition;
  private javax.swing.JLabel lblNextSymbol;
  private javax.swing.JLabel lblState;
  private javax.swing.JPanel lblVehicle;
  private javax.swing.JLabel lblVehicleImage;
  private javax.swing.JLabel lblVehicleName;
  private javax.swing.JLabel lblVoltageSymbol;
  private javax.swing.JLabel lblVoltageValue;
  private javax.swing.JPanel pnlButtons;
  private javax.swing.JPanel pnlCurrent;
  private javax.swing.JPanel pnlElectric;
  private javax.swing.JPanel pnlEnergyArea;
  private javax.swing.JPanel pnlInfo;
  private javax.swing.JPanel pnlLocation;
  private javax.swing.JPanel pnlPosition;
  private javax.swing.JPanel pnlVoltage;
  // End of variables declaration//GEN-END:variables
}
