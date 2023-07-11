/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.guing.communication;

import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.common.VehicleType;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.drivers.vehicle.ValidateException;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.VehicleCommunicationInfoPanel;
import org.opentcs.drivers.vehicle.management.VehicleCommunicationInfoPanelFactory;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.UserMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

import static org.opentcs.guing.util.I18nPlantOverview.CREATE_EDIT_VEHICLE_PATH;

/**
 *
 * @author ADMIN
 */
public class CreateEditVehiclePanel
    extends javax.swing.JDialog {

  private static final Logger LOG = LoggerFactory.getLogger(CreateEditVehiclePanel.class);

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(CREATE_EDIT_VEHICLE_PATH);

  private Vehicle vehicle;

  private final UserMessageHelper messageHelper;

  private final Map<VehicleCommAdapterDescription, VehicleCommunicationInfoPanelFactory> commInfoFactories;

  private VehicleCommunicationInfoPanel commInfoPanel;

  private final List<VehicleCommAdapterDescription> vehicleCommAdapterDescriptions = new ArrayList<>();

  private final SharedKernelServicePortalProvider portalProvider;

  private static final int HEIGHT = 300;
  /**
   * Creates new form CreateVehiclePanel
   */
  @Inject
  public CreateEditVehiclePanel(@ApplicationFrame JFrame application,
                                UserMessageHelper messageHelper,
                                Set<VehicleCommunicationInfoPanelFactory> commInfoFactories,
                                SharedKernelServicePortalProvider portalProvider) {
    super(application, true);
    this.portalProvider = portalProvider;
    this.messageHelper = messageHelper;
    this.commInfoFactories = new HashMap<>();
    commInfoFactories.forEach(commInfoFactory -> {
      this.commInfoFactories.put(commInfoFactory.getDescription(), commInfoFactory);
    });
    initComponents();
    this.getRootPane().setDefaultButton(btnOk);
    btnRemove.setForeground(Color.white);
    btnRemove.setFont(btnRemove.getFont().deriveFont(Font.BOLD));
    btnRemove.setBackground(Color.red);
    btnRemove.setVisible(false);
    this.setTitle(BUNDLE.getString("createVehicle.action.title"));
    setLocationRelativeTo(application);
    btnColorChooser.addActionListener(e -> {
      Color color = JColorChooser.showDialog(this,
              BUNDLE.getString("panel.label.color.chooser"),
              btnColorChooser.getBackground());
      if (color != null) {
        btnColorChooser.setBackground(color);
      }
    });
    initCbxType();
    initCommAdaptersComboBox();
    btnOk.addActionListener(e -> {
      if (!validateVehicle()) {
        return;
      }

      if (vehicle == null) {
        createVehicle();
      } else {
        updateVehicle();
      }
    });
    btnRemove.addActionListener(e -> removeVehicle());
  }

  public void setVehicle(Vehicle vehicle){
    if(vehicle == null){
      return;
    }
    this.vehicle = vehicle;
    txtName.setText(vehicle.getName());
    txtName.setEnabled(false);
    btnRemove.setVisible(true);
    cbxType.setSelectedItem(VehicleType.parseVehicleType(vehicle.getTypeTheme()));
    btnColorChooser.setBackground(new Color(vehicle.getColor()));
    String commAdapter = vehicle.getProperty(Vehicle.PREFERRED_ADAPTER);
    cbxCommAdapter.setSelectedItem(parseCommAdapter(commAdapter));
    updateCommInfo();
    commInfoPanel.setCommInfo(vehicle.getProperties());
    setTitle(vehicle.getName());
    revalidate();
  }

  private void initCbxType(){
    for (VehicleType type : VehicleType.values())
      cbxType.addItem(type);
  }

  private void initCommAdaptersComboBox() {
    commInfoFactories.keySet().forEach(factory -> {
      cbxCommAdapter.addItem(factory);
      vehicleCommAdapterDescriptions.add(factory);
    });
    cbxCommAdapter.addItemListener(e ->{
      if(e.getStateChange() == ItemEvent.SELECTED){
        updateCommInfo();
      }
    });
    updateCommInfo();
  }

  private void updateCommInfo() {
    VehicleCommunicationInfoPanelFactory factory = commInfoFactories.get(cbxCommAdapter.getSelectedItem());
    commInfoPanel = factory.getPanel();
    if(vehicle != null){
      commInfoPanel.setCommInfo(vehicle.getProperties());
    }
    commInfoContainer.removeAll();
    commInfoContainer.add(commInfoPanel);
    commInfoContainer.revalidate();
    this.setSize(new Dimension(400, HEIGHT + commInfoPanel.getMinHeight()));
  }

  private VehicleCommAdapterDescription parseCommAdapter(String name){
    for(VehicleCommAdapterDescription vehicleComm : vehicleCommAdapterDescriptions){
      if(vehicleComm.getClass().getName().equals(name)){
        return vehicleComm;
      }
    }
    return null;
  }

  private void createVehicle() {
    Map<String,String> properties = new HashMap<>();
    properties.put(Vehicle.PREFERRED_ADAPTER,cbxCommAdapter.getSelectedItem().getClass().getName());
    properties.putAll(commInfoPanel.getCommInfo());
    Vehicle vehicle = new Vehicle(txtName.getText())
            .withTypeTheme(Objects.requireNonNull(cbxType.getSelectedItem()).toString())
            .withColor(btnColorChooser.getBackground().getRGB())
            .withProperties(properties);

    try (SharedKernelServicePortal servicePortal = portalProvider.register()) {
      KernelServicePortal portal = servicePortal.getPortal();
      Set<Vehicle> vehicleSet = portal.getVehicleService().fetchObjects(Vehicle.class);
      for(Vehicle vhc : vehicleSet){
        if(vhc.getName().equals(vehicle.getName())){
          messageHelper.showMessageDialog(BUNDLE.getString("panel.error.title"),
                  BUNDLE.getString("panel.error.vehicleExisted"),
                  UserMessageHelper.Type.ERROR);
          return;
        }
      }
      portal.getVehicleService().createVehicle(vehicle);
      portal.getNotificationService().publishUserNotification(
              new UserNotification(getClient(),
                      BUNDLE.getFormatted("panel.createVehicle.notification.text", vehicle.getName()),
                      UserNotification.Level.INFORMATIONAL)
      );
      dispose();
    } catch (KernelRuntimeException ex) {
      if (ex instanceof ValidateException) {
        commInfoPanel.handleException((ValidateException) ex);
      } else {
        messageHelper.showMessageDialog(BUNDLE.getString("panel.error.title"), ex.getMessage(), UserMessageHelper.Type.ERROR);
      }
    }
  }

  private void updateVehicle() {
    Map<String,String> vehicleProperties = new HashMap<>();
    vehicleProperties.put(Vehicle.PREFERRED_ADAPTER,cbxCommAdapter.getSelectedItem().getClass().getName());
    commInfoPanel.getCommInfo().forEach((key, value) -> {
      vehicleProperties.put(key,value);
    });
    vehicleProperties.put("id", this.vehicle.getProperty("id"));
    Vehicle vehicle = this.vehicle.withTypeTheme(cbxType.getSelectedItem().toString())
            .withColor(btnColorChooser.getBackground().getRGB())
            .withProperties(vehicleProperties);

    try (SharedKernelServicePortal servicePortal = portalProvider.register()) {
      KernelServicePortal portal = servicePortal.getPortal();
      portal.getVehicleService().updateVehicle(vehicle);
      portal.getNotificationService().publishUserNotification(
              new UserNotification(getClient(),
                      BUNDLE.getFormatted("panel.editVehicle.notification.text", vehicle.getName()),
                      UserNotification.Level.INFORMATIONAL));
      dispose();
    }catch (KernelRuntimeException ex) {
      if (ex instanceof ValidateException) {
        commInfoPanel.handleException((ValidateException) ex);
      } else {
        messageHelper.showMessageDialog(BUNDLE.getString("panel.error.title"), ex.getMessage(), UserMessageHelper.Type.ERROR);
      }
    }
  }

  private void removeVehicle() {
    UserMessageHelper.ReturnType returnType = messageHelper.showConfirmDialog(BUNDLE.getFormatted("panel.button.remove.confirmation.title", vehicle.getName()),
            BUNDLE.getString("panel.button.remove.confirmation.text"),
            UserMessageHelper.Type.QUESTION);
    if(returnType != UserMessageHelper.ReturnType.OK){
      return;
    }
    try (SharedKernelServicePortal servicePortal = portalProvider.register()) {
      KernelServicePortal portal = servicePortal.getPortal();
      portal.getVehicleService().removeVehicle(vehicle);
      portal.getNotificationService().publishUserNotification(
              new UserNotification(getClient(),
                      BUNDLE.getFormatted("panel.removeVehicle.notification.text", vehicle.getName()),
                      UserNotification.Level.INFORMATIONAL));
      dispose();
    }catch (KernelRuntimeException ex) {
      LOG.error(ex.getLocalizedMessage());
      messageHelper.showMessageDialog(BUNDLE.getString("panel.error.title"), ex.getMessage(), UserMessageHelper.Type.ERROR);
    }
  }

  private boolean validateVehicle() {
    Set<String> errors = new HashSet<>();
    if (txtName.getText().isEmpty()) {
      errors.add(BUNDLE.getString("panel.error.nameEmpty"));
    }

    if (!errors.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      errors.forEach(error -> builder.append(error).append("\n"));
      messageHelper.showMessageDialog(BUNDLE.getString("panel.error.title"), builder.toString(), UserMessageHelper.Type.ERROR);
      return false;
    }

    return true;
  }

  public String getClient() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      LOG.error(e.getLocalizedMessage());
      return "UNKNOWN";
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

    commInfoContainer = new javax.swing.JPanel();
    pnlVehicleInfo = new javax.swing.JPanel();
    lblName = new javax.swing.JLabel();
    txtName = new javax.swing.JTextField();
    lblType = new javax.swing.JLabel();
    lblColor = new javax.swing.JLabel();
    colorContainer = new javax.swing.JPanel();
    btnColorChooser = new javax.swing.JButton();
    cbxType = new javax.swing.JComboBox<>();
    lblComm = new javax.swing.JLabel();
    cbxCommAdapter = new javax.swing.JComboBox<>();
    pnlButtons = new javax.swing.JPanel();
    btnOk = new javax.swing.JButton();
    btnRemove = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    commInfoContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 15));
    commInfoContainer.setLayout(new java.awt.BorderLayout());
    getContentPane().add(commInfoContainer, java.awt.BorderLayout.CENTER);

    pnlVehicleInfo.setPreferredSize(new java.awt.Dimension(100, 200));
    pnlVehicleInfo.setLayout(new java.awt.GridBagLayout());

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/plantoverview/dialogs/createEditVehiclePanel"); // NOI18N
    lblName.setText(bundle.getString("panel.label.name")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
    pnlVehicleInfo.add(lblName, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.6;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
    pnlVehicleInfo.add(txtName, gridBagConstraints);

    lblType.setText(bundle.getString("panel.label.type")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
    pnlVehicleInfo.add(lblType, gridBagConstraints);

    lblColor.setText(bundle.getString("panel.label.color")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
    pnlVehicleInfo.add(lblColor, gridBagConstraints);

    colorContainer.setBackground(new java.awt.Color(153, 153, 153));
    colorContainer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
    colorContainer.setMinimumSize(new java.awt.Dimension(100, 30));
    colorContainer.setOpaque(false);
    colorContainer.setPreferredSize(new java.awt.Dimension(287, 30));
    colorContainer.setLayout(new java.awt.GridBagLayout());

    btnColorChooser.setBackground(new java.awt.Color(255, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    colorContainer.add(btnColorChooser, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.6;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
    pnlVehicleInfo.add(colorContainer, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.6;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
    pnlVehicleInfo.add(cbxType, gridBagConstraints);

    lblComm.setText(bundle.getString("panel.label.comm")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
    pnlVehicleInfo.add(lblComm, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.6;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
    pnlVehicleInfo.add(cbxCommAdapter, gridBagConstraints);

    getContentPane().add(pnlVehicleInfo, java.awt.BorderLayout.PAGE_START);

    pnlButtons.setMinimumSize(new java.awt.Dimension(0, 50));
    pnlButtons.setPreferredSize(new java.awt.Dimension(0, 50));

    btnOk.setText(bundle.getString("panel.button.ok")); // NOI18N
    btnOk.setPreferredSize(new java.awt.Dimension(110, 29));
    pnlButtons.add(btnOk);

    btnRemove.setText(bundle.getString("panel.button.remove")); // NOI18N
    pnlButtons.add(btnRemove);

    getContentPane().add(pnlButtons, java.awt.BorderLayout.SOUTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnColorChooser;
  private javax.swing.JButton btnOk;
  private javax.swing.JButton btnRemove;
  private javax.swing.JComboBox<VehicleCommAdapterDescription> cbxCommAdapter;
  private javax.swing.JComboBox<VehicleType> cbxType;
  private javax.swing.JPanel colorContainer;
  private javax.swing.JPanel commInfoContainer;
  private javax.swing.JLabel lblColor;
  private javax.swing.JLabel lblComm;
  private javax.swing.JLabel lblName;
  private javax.swing.JLabel lblType;
  private javax.swing.JPanel pnlButtons;
  private javax.swing.JPanel pnlVehicleInfo;
  private javax.swing.JTextField txtName;
  // End of variables declaration//GEN-END:variables
}
