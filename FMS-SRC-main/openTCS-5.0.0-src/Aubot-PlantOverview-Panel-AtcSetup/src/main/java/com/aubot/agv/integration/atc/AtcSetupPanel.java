/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aubot.agv.integration.atc;

import com.fazecast.jSerialComm.SerialPort;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import static com.aubot.agv.integration.atc.I18nPlantOverviewPanelAtcSetup.BUNDLE_PATH;


/**
 *
 * @author ADMIN
 */
public class AtcSetupPanel
    extends PluggablePanel implements AtcCommunicationListener {

  private static final Logger LOG = LoggerFactory.getLogger(AtcSetupPanel.class);

  private final AtcDevice atcDevice;

  private boolean initialized = false;

  private final ResourceBundle bundle;
  /**
   * Creates new form AtcSetupPanel
   */
  @Inject
  public AtcSetupPanel() {
    this.atcDevice = new AtcDevice();
    bundle = ResourceBundle.getBundle(BUNDLE_PATH);
    initComponents();
    initComponentsExtra();
  }
  
  private void initComponentsExtra() {
    this.getAccessibleContext().setAccessibleName(bundle.getString("atcSetupPanel.title"));
    pnlLogging.setAutoscrolls(true);
    pnlLogging.setEditable(false);
    btnSetup.setEnabled(false);
    btnUpdate.setEnabled(false);
    btnGoToWeb.setEnabled(false);
    btnRefresh.setEnabled(false);
    btnReboot.setEnabled(false);
    setupPortManager();
    setupWifiManager();
    setupConfigAtcActions();
    setPreferredSize(new Dimension(900, 640));
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem actionClear = new JMenuItem(bundle.getString("atcSetupPanel.loggingPanel.clear"));
    actionClear.addActionListener(e -> pnlLogging.setText(""));
    popupMenu.add(actionClear);
    pnlLogging.setComponentPopupMenu(popupMenu);
  }

  private void setupWifiManager() {
    btnRefresh.addActionListener(e -> {
      new SwingWorker<List<String>, Void>() {
        @Override
        protected List<String> doInBackground() throws Exception {
          return atcDevice.getWifiList();
        }

        @Override
        protected void done() {
          super.done();
          try {
            cbxWifi.removeAllItems();
            for (String wifi : get()) {
              cbxWifi.addItem(wifi);
            }
          } catch (InterruptedException | ExecutionException ex) {
            handlerError(ex);
          }
        }
      }.execute();
    });
  }

  private void setupPortManager() {
    cbxPort.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        cbxPort.removeAllItems();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort serialPort : ports) {
          cbxPort.addItem(serialPort.getSystemPortName());
        }
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {

      }
    });

    tgbConnect.addActionListener(e -> {
      if (tgbConnect.isSelected()) {
        if (cbxPort.getSelectedIndex() < 0) {
          tgbConnect.setSelected(false);
          return;
        }
        SerialPort port = SerialPort.getCommPort((String) cbxPort.getSelectedItem());
        new SwingWorker<String, Void>() {
          @Override
          protected String doInBackground() throws Exception {
            pnlLogging.setText("");
            if (atcDevice.connectAtc(port)) {
              lblStatus.setText(bundle.getString("atcSetupPanel.connectionPanel.connected"));
              lblStatus.setForeground(Color.green.darker());
              getAtcInformation();
            } else {
              lblStatus.setText(bundle.getString("atcSetupPanel.connectionPanel.connectFailed"));
              lblStatus.setForeground(Color.red);
              tgbConnect.setSelected(false);
            }

            return null;
          }

          @Override
          protected void done() {
            super.done();
            try {
              get();
              cbxWifi.removeAllItems();
            } catch (InterruptedException | ExecutionException ex) {
              handlerError(ex);
            }
          }
        }.execute();
      } else {
        if (atcDevice.isConnected()) {
          atcDevice.disconnectAtc();
        }
        lblStatus.setText(bundle.getString("atcSetupPanel.connectionPanel.disconnected"));
        lblStatus.setForeground(Color.green.darker());
      }
    });

    tgbConnect.addItemListener(e -> {
      boolean checked = e.getStateChange() == ItemEvent.SELECTED;
      btnSetup.setEnabled(checked);
      btnUpdate.setEnabled(checked);
      btnGoToWeb.setEnabled(checked);
      btnRefresh.setEnabled(checked);
      btnReboot.setEnabled(checked);
      tgbConnect.setText(checked ? bundle.getString("atcSetupPanel.connectionPanel.disconnect") : bundle.getString("atcSetupPanel.connectionPanel.connect"));
    });

    tgbConnect.setFocusPainted(false);
  }

  private void setupConfigAtcActions() {
    NumberFormatter nft = new NumberFormatter();
    nft.setValueClass(Integer.class);
    nft.setMinimum(0);
    nft.setMaximum(255);
    nft.setAllowsInvalid(false);
    nft.setCommitsOnValidEdit(true);
    DefaultFormatterFactory dff = new DefaultFormatterFactory(nft);
    FocusListener ipListener = new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        ((JFormattedTextField) e.getSource()).selectAll();
      }

      @Override
      public void focusLost(FocusEvent e) {

      }
    };
    installNumberFormatter(dff, txtIpA, ipListener);
    installNumberFormatter(dff, txtIpB, ipListener);
    installNumberFormatter(dff, txtIpC, ipListener);
    installNumberFormatter(dff, txtIpD, ipListener);
    installNumberFormatter(dff, txtMaskA, ipListener);
    installNumberFormatter(dff, txtMaskB, ipListener);
    installNumberFormatter(dff, txtMaskC, ipListener);
    installNumberFormatter(dff, txtMaskD, ipListener);
    installNumberFormatter(dff, txtGatewayA, ipListener);
    installNumberFormatter(dff, txtGatewayB, ipListener);
    installNumberFormatter(dff, txtGatewayC, ipListener);
    installNumberFormatter(dff, txtGatewayD, ipListener);

    btnSetup.addActionListener(e -> {
      new SwingWorker<String, String>() {
        @Override
        protected String doInBackground() throws Exception {
          atcDevice.setup(cbxWifi.getSelectedIndex(), String.valueOf(txtPassword.getPassword()));
          publish(bundle.getString("atcSetupPanel.setupPanel.connectWifiSuccess"));
          getAtcInformation();
          return "Done";
        }

        @Override
        protected void process(List<String> chunks) {
          super.process(chunks);
          for (String chunk : chunks) {
            JOptionPane.showMessageDialog(AtcSetupPanel.this,
                    chunk,
                    bundle.getString("atcSetupPanel.notification"),
                    JOptionPane.INFORMATION_MESSAGE);
          }
        }

        @Override
        protected void done() {
          super.done();
          try {
            get();
          } catch (Exception ex) {
            handlerError(ex);
          }
        }
      }.execute();
    });

    btnUpdate.addActionListener(e -> {
      String ip = txtIpA.getText() + "." + txtIpB.getText() + "." + txtIpC.getText() + "." + txtIpD.getText();
      String mask = txtMaskA.getText() + "." + txtMaskB.getText() + "." + txtMaskC.getText() + "." + txtMaskD.getText();
      String gateway = txtGatewayA.getText() + "." + txtGatewayB.getText() + "." + txtGatewayC.getText() + "." + txtGatewayD.getText();
      new SwingWorker<String, Void>() {
        @Override
        protected String doInBackground() throws Exception {
          atcDevice.setAddresses(ip, mask, gateway);

          return bundle.getString("atcSetupPanel.ipAddressPanel.setIpSuccess");
        }

        @Override
        protected void done() {
          super.done();
          try {
            JOptionPane.showMessageDialog(AtcSetupPanel.this, get(),
                    bundle.getString("atcSetupPanel.notification"), JOptionPane.INFORMATION_MESSAGE);
          } catch (InterruptedException | ExecutionException ex) {
            handlerError(ex);
          }
        }
      }.execute();
    });

    btnReboot.addActionListener(e -> {
      atcDevice.reboot();
      tgbConnect.doClick();
      JOptionPane.showMessageDialog(this, bundle.getString("atcSetupPanel.ipAddressPanel.rebootAtc"),
              bundle.getString("atcSetupPanel.notification"), JOptionPane.INFORMATION_MESSAGE);
    });
  }

  private void installNumberFormatter(JFormattedTextField.AbstractFormatterFactory dff,
                                      JFormattedTextField ftf, FocusListener listener) {
    ftf.setFormatterFactory(dff);
    ftf.addFocusListener(listener);
  }

  private void handlerError(Exception ex) {
    LOG.error(ex.getMessage());
    onLogging(ex.getMessage());
    if (ex instanceof ExecutionException) {
      Throwable causeEx = ex.getCause();
      if (causeEx instanceof AtcCommandException) {
        String error = "Unknown error";
        switch (((AtcCommandException) causeEx).getCode()) {
          case AtcCommandException.JOIN_FAIL:
            error = bundle.getString("atcSetupPanel.communication.error.joinFailed");
            break;
          case AtcCommandException.DISCONNECTED:
            error = bundle.getString("atcSetupPanel.communication.error.disconnected");
            break;
          case AtcCommandException.REQUEST_TIMEOUT:
            error = bundle.getString("atcSetupPanel.communication.error.requestTimeOut");
            break;
          case AtcCommandException.IP_INVALID:
            error = bundle.getString("atcSetupPanel.communication.error.ipInvalid");
            break;
          case AtcCommandException.MASK_INVALID:
            error = bundle.getString("atcSetupPanel.communication.error.maskInvalid");
            break;
          case AtcCommandException.GATEWAY_INVALID:
            error = bundle.getString("atcSetupPanel.communication.error.gatewayInvalid");
            break;
          case AtcCommandException.PORT_INVALID:
            error = bundle.getString("atcSetupPanel.communication.error.portInvalid");
            break;
          case AtcCommandException.ERROR_FROM_ATC:
            error = bundle.getString("atcSetupPanel.communication.error.atcError") + causeEx.getMessage();
          default:
        }
        JOptionPane.showMessageDialog(this, error, bundle.getString("atcSetupPanel.errorDialog.title"), JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void getAtcInformation() throws Exception {
    processBar.setValue(30);

    lblCurrentWifiValue.setText(atcDevice.getCurrentWifi());
    processBar.setValue(70);

    String[] addresses = atcDevice.getAddresses();
    String[] ip = addresses[0].split("\\.");
    txtIpA.setText(ip[0]);
    txtIpB.setText(ip[1]);
    txtIpC.setText(ip[2]);
    txtIpD.setText(ip[3]);

    String[] mask = addresses[1].split("\\.");
    txtMaskA.setText(mask[0]);
    txtMaskB.setText(mask[1]);
    txtMaskC.setText(mask[2]);
    txtMaskD.setText(mask[3]);

    String[] gateway = addresses[2].split("\\.");
    txtGatewayA.setText(gateway[0]);
    txtGatewayB.setText(gateway[1]);
    txtGatewayC.setText(gateway[2]);
    txtGatewayD.setText(gateway[3]);
    processBar.setValue(100);

    for (ActionListener actionListener : btnGoToWeb.getActionListeners()) {
      btnGoToWeb.removeActionListener(actionListener);
    }
    btnGoToWeb.addActionListener(e -> {
      try {
        Desktop.getDesktop().browse(new URI("http://" + addresses[0]));
      } catch (IOException | URISyntaxException ex) {
        handlerError(ex);
      }
    });
  }

  @Override
  public void onLogging(String logMessage) {
    pnlLogging.append(logMessage);
    pnlLogging.setCaretPosition(pnlLogging.getDocument().getLength());
  }

  /**
   * (Re-)Initializes this component before it is being used.
   */
  @Override
  public void initialize() {
    if (isInitialized()) {
      LOG.info("Initialized");
      return;
    }

    atcDevice.setListener(this);
    initialized = true;
  }

  /**
   * Checks whether this component is initialized.
   *
   * @return <code>true</code> if, and only if, this component is initialized.
   */
  @Override
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * Terminates the instance and frees resources.
   */
  @Override
  public void terminate() {
    if (!isInitialized()) {
      LOG.info("Terminated");
      return;
    }

    if (atcDevice.isConnected()) {
      atcDevice.disconnectAtc();
    }
    initialized = false;
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

        pnlProcess = new javax.swing.JPanel();
        processBar = new javax.swing.JProgressBar();
        jScrollPane2 = new javax.swing.JScrollPane();
        pnlLogging = new javax.swing.JTextArea();
        pnlInfo = new javax.swing.JPanel();
        pnlConnect = new javax.swing.JPanel();
        lblPort = new javax.swing.JLabel();
        cbxPort = new javax.swing.JComboBox<>();
        tgbConnect = new javax.swing.JToggleButton();
        lblStatus = new javax.swing.JLabel();
        pnlSetup = new javax.swing.JPanel();
        pnlWifi = new javax.swing.JPanel();
        lblWifi = new javax.swing.JLabel();
        cbxWifi = new javax.swing.JComboBox<>();
        btnRefresh = new javax.swing.JButton();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        btnSetup = new javax.swing.JButton();
        lblCurrentWifi = new javax.swing.JLabel();
        lblCurrentWifiValue = new javax.swing.JLabel();
        pnlIpAddress = new javax.swing.JPanel();
        lblIp = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnUpdate = new javax.swing.JButton();
        btnGoToWeb = new javax.swing.JButton();
        txtGatewayA = new javax.swing.JFormattedTextField();
        txtGatewayB = new javax.swing.JFormattedTextField();
        txtGatewayD = new javax.swing.JFormattedTextField();
        txtGatewayC = new javax.swing.JFormattedTextField();
        txtIpA = new javax.swing.JFormattedTextField();
        jLabel15 = new javax.swing.JLabel();
        txtIpB = new javax.swing.JFormattedTextField();
        jLabel16 = new javax.swing.JLabel();
        txtIpC = new javax.swing.JFormattedTextField();
        txtIpD = new javax.swing.JFormattedTextField();
        jLabel17 = new javax.swing.JLabel();
        txtMaskA = new javax.swing.JFormattedTextField();
        jLabel18 = new javax.swing.JLabel();
        txtMaskB = new javax.swing.JFormattedTextField();
        jLabel19 = new javax.swing.JLabel();
        txtMaskC = new javax.swing.JFormattedTextField();
        txtMaskD = new javax.swing.JFormattedTextField();
        jLabel20 = new javax.swing.JLabel();
        btnReboot = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        pnlProcess.setBorder(javax.swing.BorderFactory.createTitledBorder("Logging"));
        pnlProcess.setLayout(new java.awt.BorderLayout());
        pnlProcess.add(processBar, java.awt.BorderLayout.PAGE_START);

        pnlLogging.setColumns(20);
        pnlLogging.setRows(5);
        jScrollPane2.setViewportView(pnlLogging);

        pnlProcess.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        add(pnlProcess, java.awt.BorderLayout.CENTER);

        pnlInfo.setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/com/aubot/agv/integration/atc/Bundle"); // NOI18N
        pnlConnect.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("atcSetupPanel.connectionPanel.title"))); // NOI18N
        pnlConnect.setPreferredSize(new java.awt.Dimension(790, 70));
        pnlConnect.setRequestFocusEnabled(false);
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEADING);
        flowLayout1.setAlignOnBaseline(true);
        pnlConnect.setLayout(flowLayout1);

        lblPort.setText(bundle.getString("atcSetupPanel.connectionPanel.port")); // NOI18N
        pnlConnect.add(lblPort);

        cbxPort.setPreferredSize(new java.awt.Dimension(300, 29));
        pnlConnect.add(cbxPort);

        tgbConnect.setText(bundle.getString("atcSetupPanel.connectionPanel.connect")); // NOI18N
        pnlConnect.add(tgbConnect);
        pnlConnect.add(lblStatus);

        pnlInfo.add(pnlConnect, java.awt.BorderLayout.PAGE_START);

        pnlSetup.setPreferredSize(new java.awt.Dimension(790, 200));
        pnlSetup.setLayout(new java.awt.GridLayout(1, 0));

        pnlWifi.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("atcSetupPanel.setupPanel.title"))); // NOI18N
        pnlWifi.setLayout(new java.awt.GridBagLayout());

        lblWifi.setText(bundle.getString("atcSetupPanel.setupPanel.wifi")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        pnlWifi.add(lblWifi, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.7;
        gridBagConstraints.weighty = 0.1;
        pnlWifi.add(cbxWifi, gridBagConstraints);

        btnRefresh.setText(bundle.getString("atcSetupPanel.setupPanel.refresh")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        pnlWifi.add(btnRefresh, gridBagConstraints);

        lblPassword.setText(bundle.getString("atcSetupPanel.setupPanel.password")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        pnlWifi.add(lblPassword, gridBagConstraints);

        txtPassword.setText("jPasswordField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        pnlWifi.add(txtPassword, gridBagConstraints);

        btnSetup.setText(bundle.getString("atcSetupPanel.setupPanel.setupAtc")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        pnlWifi.add(btnSetup, gridBagConstraints);

        lblCurrentWifi.setFont(new java.awt.Font("Segoe UI", 2, 15)); // NOI18N
        lblCurrentWifi.setText(bundle.getString("atcSetupPanel.setupPanel.currentWifi")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlWifi.add(lblCurrentWifi, gridBagConstraints);

        lblCurrentWifiValue.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        pnlWifi.add(lblCurrentWifiValue, gridBagConstraints);

        pnlSetup.add(pnlWifi);

        pnlIpAddress.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("atcSetupPanel.ipAddressPanel.title"))); // NOI18N
        pnlIpAddress.setLayout(new java.awt.GridBagLayout());

        lblIp.setText(bundle.getString("atcSetupPanel.ipAddressPanel.ip")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(lblIp, gridBagConstraints);

        jLabel4.setText(".");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(jLabel4, gridBagConstraints);

        jLabel5.setText(".");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(jLabel5, gridBagConstraints);

        jLabel6.setText(".");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(jLabel6, gridBagConstraints);

        jLabel7.setText(bundle.getString("atcSetupPanel.ipAddressPanel.mask")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(jLabel7, gridBagConstraints);

        jLabel11.setText(bundle.getString("atcSetupPanel.ipAddressPanel.gateway")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(jLabel11, gridBagConstraints);

        btnUpdate.setText(bundle.getString("atcSetupPanel.ipAddressPanel.update")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(btnUpdate, gridBagConstraints);

        btnGoToWeb.setText(bundle.getString("atcSetupPanel.ipAddressPanel.goToWeb")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(btnGoToWeb, gridBagConstraints);

        txtGatewayA.setColumns(3);
        txtGatewayA.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtGatewayA, gridBagConstraints);

        txtGatewayB.setColumns(3);
        txtGatewayB.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtGatewayB, gridBagConstraints);

        txtGatewayD.setColumns(3);
        txtGatewayD.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtGatewayD, gridBagConstraints);

        txtGatewayC.setColumns(3);
        txtGatewayC.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtGatewayC, gridBagConstraints);

        txtIpA.setColumns(3);
        txtIpA.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtIpA, gridBagConstraints);

        jLabel15.setText(".");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(jLabel15, gridBagConstraints);

        txtIpB.setColumns(3);
        txtIpB.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtIpB, gridBagConstraints);

        jLabel16.setText(".");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(jLabel16, gridBagConstraints);

        txtIpC.setColumns(3);
        txtIpC.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtIpC, gridBagConstraints);

        txtIpD.setColumns(3);
        txtIpD.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtIpD, gridBagConstraints);

        jLabel17.setText(".");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(jLabel17, gridBagConstraints);

        txtMaskA.setColumns(3);
        txtMaskA.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtMaskA, gridBagConstraints);

        jLabel18.setText(".");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(jLabel18, gridBagConstraints);

        txtMaskB.setColumns(3);
        txtMaskB.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtMaskB, gridBagConstraints);

        jLabel19.setText(".");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(jLabel19, gridBagConstraints);

        txtMaskC.setColumns(3);
        txtMaskC.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtMaskC, gridBagConstraints);

        txtMaskD.setColumns(3);
        txtMaskD.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(txtMaskD, gridBagConstraints);

        jLabel20.setText(".");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 0.1;
        pnlIpAddress.add(jLabel20, gridBagConstraints);

        btnReboot.setText(bundle.getString("atcSetupPanel.ipAddressPanel.reboot")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 3;
        pnlIpAddress.add(btnReboot, gridBagConstraints);

        pnlSetup.add(pnlIpAddress);

        pnlInfo.add(pnlSetup, java.awt.BorderLayout.CENTER);

        add(pnlInfo, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGoToWeb;
    private javax.swing.JButton btnReboot;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSetup;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cbxPort;
    private javax.swing.JComboBox<String> cbxWifi;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCurrentWifi;
    private javax.swing.JLabel lblCurrentWifiValue;
    private javax.swing.JLabel lblIp;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblWifi;
    private javax.swing.JPanel pnlConnect;
    private javax.swing.JPanel pnlInfo;
    private javax.swing.JPanel pnlIpAddress;
    private javax.swing.JTextArea pnlLogging;
    private javax.swing.JPanel pnlProcess;
    private javax.swing.JPanel pnlSetup;
    private javax.swing.JPanel pnlWifi;
    private javax.swing.JProgressBar processBar;
    private javax.swing.JToggleButton tgbConnect;
    private javax.swing.JFormattedTextField txtGatewayA;
    private javax.swing.JFormattedTextField txtGatewayB;
    private javax.swing.JFormattedTextField txtGatewayC;
    private javax.swing.JFormattedTextField txtGatewayD;
    private javax.swing.JFormattedTextField txtIpA;
    private javax.swing.JFormattedTextField txtIpB;
    private javax.swing.JFormattedTextField txtIpC;
    private javax.swing.JFormattedTextField txtIpD;
    private javax.swing.JFormattedTextField txtMaskA;
    private javax.swing.JFormattedTextField txtMaskB;
    private javax.swing.JFormattedTextField txtMaskC;
    private javax.swing.JFormattedTextField txtMaskD;
    private javax.swing.JPasswordField txtPassword;
    // End of variables declaration//GEN-END:variables
}
