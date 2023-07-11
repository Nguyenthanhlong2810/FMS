/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.guing.communication;

import org.opentcs.common.VehicleType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;
import org.opentcs.drivers.vehicle.management.VehicleCommunicationInfoPanel;
import org.opentcs.drivers.vehicle.management.VehicleCommunicationInfoPanelFactory;
import org.opentcs.guing.components.dialogs.DialogContent;
import org.opentcs.guing.util.UserMessageHelper;
import org.opentcs.hibernate.entities.VehicleEntity;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.*;
import java.util.List;

/**
 *
 * @author ADMIN
 */
public class CreateVehiclePanel
    extends DialogContent {

  private Vehicle createdVehicle;

  private Vehicle updatedVehicle;

  private final UserMessageHelper messageHelper;

  private final Map<VehicleCommAdapterDescription, VehicleCommunicationInfoPanelFactory> commInfoFactories;

  private VehicleCommunicationInfoPanel commInfoPanel;

  private final List<VehicleCommAdapterDescription> vehicleCommAdapterDescriptions = new ArrayList<>();

  private static final int HEIGHT = 300;
  /**
   * Creates new form CreateVehiclePanel
   */
  @Inject
  public CreateVehiclePanel(UserMessageHelper messageHelper,
                            Set<VehicleCommunicationInfoPanelFactory> commInfoFactories) {
    this.messageHelper = messageHelper;
    this.commInfoFactories = new HashMap<>();
    commInfoFactories.forEach(commInfoFactory -> {
      this.commInfoFactories.put(commInfoFactory.getDescription(), commInfoFactory);
    });
    initComponents();
    btnColorChooser.addActionListener(e -> {
      Color color = JColorChooser.showDialog(this, "Vehicle color", btnColorChooser.getBackground());
      if (color != null) {
        btnColorChooser.setBackground(color);
      }
    });
    initCbxType();
    initCommAdaptersComboBox();
    setPreferredSize(new Dimension(500, HEIGHT));
  }

  public void setUpdatedVehicle(Vehicle oldVehicle){
    if(oldVehicle == null){
      return;
    }
    updatedVehicle = oldVehicle;
    txtName.setText(oldVehicle.getName());
    cbxType.setSelectedItem(VehicleType.parseVehicleType(oldVehicle.getTypeTheme()));
    btnColorChooser.setBackground(new Color(oldVehicle.getColor()));
    String commAdapter = oldVehicle.getProperty(Vehicle.PREFERRED_ADAPTER);
    cbxCommAdapter.setSelectedItem(parseCommAdapter(commAdapter));
    updateCommInfo();
    commInfoPanel.setCommInfo(oldVehicle.getProperties());
    revalidate();
  }

  /**
   * Initialisiert die Dialogelemente.
   */
  @Override
  public void initFields() {

  }

  /**
   * Übernimmt die Werte aus den Dialogelementen.
   */
  @Override
  public void update() {
    Set<String> errors = new HashSet<>();
    if (txtName.getText().isEmpty()) {
      errors.add("Vehicle name is empty");
    }

    if (!errors.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      errors.forEach(error -> builder.append(error).append("\n"));
      messageHelper.showMessageDialog("Error", builder.toString(), UserMessageHelper.Type.ERROR);
      updateFailed = true;
      return;
    }
    Map<String,String> properties = new HashMap<>();
    properties.put(Vehicle.PREFERRED_ADAPTER,cbxCommAdapter.getSelectedItem().getClass().getName());
    properties.putAll(commInfoPanel.getCommInfo());
    createdVehicle = new Vehicle(txtName.getText())
            .withTypeTheme(Objects.requireNonNull(cbxType.getSelectedItem()).toString())
            .withColor(btnColorChooser.getBackground().getRGB())
            .withProperties(properties);
  }
  public Vehicle getUpdatedVehicle(){
      Map<String,String> vehicleProperties = new HashMap<>();
      vehicleProperties.put(Vehicle.PREFERRED_ADAPTER,cbxCommAdapter.getSelectedItem().getClass().getName());
      commInfoPanel.getCommInfo().forEach((key, value) -> {
        vehicleProperties.put(key,value);
      });
      vehicleProperties.put("id", updatedVehicle.getProperty("id"));
      updatedVehicle = updatedVehicle.withTypeTheme(cbxType.getSelectedItem().toString())
              .withColor(btnColorChooser.getBackground().getRGB())
              .withProperties(vehicleProperties);
      return updatedVehicle;
  }

  public Vehicle getCreatedVehicle() {
    return createdVehicle;
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
    if(updatedVehicle != null){
      commInfoPanel.setCommInfo(updatedVehicle.getProperties());
    }
    commInfoContainer.removeAll();
    commInfoContainer.add(commInfoPanel);
    commInfoContainer.revalidate();
    commInfoContainer.setPreferredSize(new Dimension(0, HEIGHT + commInfoPanel.getMinHeight()));
    this.setPreferredSize(new Dimension(0, 200 + HEIGHT + commInfoPanel.getMinHeight()));
    this.updateUI();
  }

  private VehicleCommAdapterDescription parseCommAdapter(String name){
      for(VehicleCommAdapterDescription vehicleComm : vehicleCommAdapterDescriptions){
        if(vehicleComm.getClass().getName().equals(name)){
          return vehicleComm;
        }
      }
      return null;
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

    setLayout(new java.awt.BorderLayout());

    commInfoContainer.setLayout(new java.awt.BorderLayout());
    add(commInfoContainer, java.awt.BorderLayout.CENTER);

    pnlVehicleInfo.setPreferredSize(new java.awt.Dimension(100, 200));
    pnlVehicleInfo.setLayout(new java.awt.GridBagLayout());

    lblName.setText("Name:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
    pnlVehicleInfo.add(lblName, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.6;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 15);
    pnlVehicleInfo.add(txtName, gridBagConstraints);

    lblType.setText("Type:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
    pnlVehicleInfo.add(lblType, gridBagConstraints);

    lblColor.setText("Color:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.weighty = 0.1;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
    pnlVehicleInfo.add(lblColor, gridBagConstraints);

    colorContainer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
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

    lblComm.setText("Comm:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
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

    add(pnlVehicleInfo, java.awt.BorderLayout.PAGE_START);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnColorChooser;
  private javax.swing.JComboBox<VehicleCommAdapterDescription> cbxCommAdapter;
  private javax.swing.JComboBox<VehicleType> cbxType;
  private javax.swing.JPanel colorContainer;
  private javax.swing.JPanel commInfoContainer;
  private javax.swing.JLabel lblColor;
  private javax.swing.JLabel lblComm;
  private javax.swing.JLabel lblName;
  private javax.swing.JLabel lblType;
  private javax.swing.JPanel pnlVehicleInfo;
  private javax.swing.JTextField txtName;
  // End of variables declaration//GEN-END:variables
}
