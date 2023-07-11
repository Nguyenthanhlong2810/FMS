/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aubot.agv.integration.rfid;

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
import java.util.ResourceBundle;

/**
 *
 * @author ADMIN
 */
public class RfidSetupPanel extends PluggablePanel {

    private final Logger LOG = LoggerFactory.getLogger(RfidSetupPanel.class);

    private final RfidReadWrite rrw;

    private boolean initialized = false;

    private final ResourceBundle bundle = ResourceBundle.getBundle("i18n.com.aubot.agv.integration.rfid.Bundle");
    /**
     * Creates new form RfidSetupPanel
     */
    @Inject
    public RfidSetupPanel() {
        rrw = new RfidReadWrite();
        initComponents();
        initComponentsExtra();
        initActions();
        txtIntersectionNo.setEnabled(false);
        txtIntersectionRoadNo.setEnabled(false);
        btnRead.setEnabled(false);
        btnWrite.setEnabled(false);
    }

    private void initComponentsExtra() {
        tgbConnect.addItemListener(e -> {
            boolean checked = e.getStateChange() == ItemEvent.SELECTED;
            btnWrite.setEnabled(checked);
            btnRead.setEnabled(checked);
            tgbConnect.setText(checked
                    ? bundle.getString("rfidSetupPanel.connectPanel.tgbDisconnect")
                    : bundle.getString("rfidSetupPanel.connectPanel.tgbConnect"));
        });

        FocusListener focusListener = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                ((JFormattedTextField) e.getSource()).selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {}
        };
        txtR.addFocusListener(focusListener);
        txtF.addFocusListener(focusListener);
        txtI.addFocusListener(focusListener);
        txtD.addFocusListener(focusListener);

        KeyListener rfidKeyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char key = e.getKeyChar();
                if (!Character.isDigit(key) && !Character.isLetter(key)) {
                    e.consume();
                } else {
                    e.setKeyChar(Character.toUpperCase(key));
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        };
        txtR.addKeyListener(rfidKeyListener);
        txtF.addKeyListener(rfidKeyListener);
        txtI.addKeyListener(rfidKeyListener);
        txtD.addKeyListener(rfidKeyListener);

        NumberFormatter nft = new NumberFormatter();
        nft.setMinimum(1);
        nft.setMaximum(255);
        nft.setAllowsInvalid(false);
        nft.setCommitsOnValidEdit(true);
        DefaultFormatterFactory dff = new DefaultFormatterFactory(nft);
        txtIntersectionNo.setFormatterFactory(dff);
        txtIntersectionRoadNo.setFormatterFactory(dff);

        chkBeginIntersection.addItemListener(e -> {
            boolean checked = e.getStateChange() == ItemEvent.SELECTED;
            txtIntersectionNo.setEnabled(checked);
            txtIntersectionRoadNo.setEnabled(checked);
            if (checked) {
                chkEndIntersection.setSelected(false);
            }
        });
        chkEndIntersection.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                chkBeginIntersection.setSelected(false);
            }
        });
    }

    private void initActions() {
        cbxPort.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                cbxPort.removeAllItems();
                SerialPort[] ports = SerialPort.getCommPorts();
                for (SerialPort serialPort : ports) {
                    cbxPort.addItem(serialPort.getDescriptivePortName());
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
                int index = cbxPort.getSelectedIndex();
                if (cbxPort.getSelectedIndex() < 0) {
                    tgbConnect.setSelected(false);
                    return;
                }
                SerialPort port = SerialPort.getCommPort(SerialPort.getCommPorts()[index].getSystemPortName());
                if (rrw.connect(port)) {
                    lblStatus.setText(bundle.getString("rfidSetupPanel.connectPanel.lblStatus.connected"));
                    lblStatus.setForeground(Color.green.darker());
                } else {
                    lblStatus.setText(bundle.getString("rfidSetupPanel.connectPanel.lblStatus.connectFail"));
                    lblStatus.setForeground(Color.red);
                    tgbConnect.setSelected(false);
                }
            } else {
                lblStatus.setText(bundle.getString("rfidSetupPanel.connectPanel.lblStatus.disconnected"));
                lblStatus.setForeground(Color.green.darker());
                rrw.disconnect();
            }
        });

        btnRead.addActionListener(e -> {
            if (!rrw.isConnected()) {
                JOptionPane.showMessageDialog(this,
                        bundle.getString("rfidSetupPanel.setupPanel.message.deviceNotConnect"),
                        bundle.getString("rfidSetupPanel.setupPanel.message.errorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            RfidReadResult rs = rrw.read();
            if (rs != null) {
                Rfid rfid = rs.getRifd();
                String name = rfid.getName();
                txtR.setText(String.valueOf(name.charAt(0)));
                txtF.setText(String.valueOf(name.charAt(1)));
                txtI.setText(String.valueOf(name.charAt(2)));
                txtD.setText(String.valueOf(name.charAt(3)));
                chkBeginIntersection.setSelected(rfid.isBeginIntersection());
                chkEndIntersection.setSelected(rfid.isEndIntersection());
                if (rfid.isBeginIntersection()) {
                    txtIntersectionNo.setText(String.valueOf(rfid.getIntersectionNo()));
                    txtIntersectionRoadNo.setText(String.valueOf(rfid.getIntersectionRoadNo()));
                }
                if (rs.isCrcOk()) {
                    JOptionPane.showMessageDialog(this,
                            bundle.getString("rfidSetupPanel.setupPanel.message.readOk"),
                            bundle.getString("rfidSetupPanel.setupPanel.message.infoTitle"),
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            bundle.getString("rfidSetupPanel.setupPanel.message.readOkCrcFail"),
                            bundle.getString("rfidSetupPanel.setupPanel.message.errorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        bundle.getString("rfidSetupPanel.setupPanel.message.readError"),
                        bundle.getString("rfidSetupPanel.setupPanel.message.errorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnWrite.addActionListener(e -> {
            if (!rrw.isConnected()) {
                JOptionPane.showMessageDialog(this,
                        bundle.getString("rfidSetupPanel.setupPanel.message.deviceNotConnect"),
                        bundle.getString("rfidSetupPanel.setupPanel.message.errorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            Rfid rfid = new Rfid(new String(new char[] {
                    firstCharOrDefault(txtR.getText()),
                    firstCharOrDefault(txtF.getText()),
                    firstCharOrDefault(txtI.getText()),
                    firstCharOrDefault(txtD.getText()),
            }));
            if (chkBeginIntersection.isSelected()) {
                if (txtIntersectionNo.getText().isEmpty() || txtIntersectionRoadNo.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            bundle.getString("rfidSetupPanel.setupPanel.message.intersectionEmpty"),
                            bundle.getString("rfidSetupPanel.setupPanel.message.errorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                rfid.setBeginIntersection(true);
                rfid.setIntersectionNo(Integer.parseInt(txtIntersectionNo.getText()));
                rfid.setIntersectionRoadNo(Integer.parseInt(txtIntersectionRoadNo.getText()));
            } else if (chkEndIntersection.isSelected()) {
                rfid.setEndIntersection(true);
            }
            if (rrw.write(rfid)) {
                JOptionPane.showMessageDialog(this,
                        bundle.getString("rfidSetupPanel.setupPanel.message.writeOk"),
                        bundle.getString("rfidSetupPanel.setupPanel.message.infoTitle"),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        bundle.getString("rfidSetupPanel.setupPanel.message.writeError"),
                        bundle.getString("rfidSetupPanel.setupPanel.message.errorTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private char firstCharOrDefault(String text) {
        return text == null || text.isEmpty() || text.trim().isEmpty() ? '0' : text.charAt(0);
    }

    /**
     * (Re-)Initializes this component before it is being used.
     */
    @Override
    public void initialize() {
        if (isInitialized()) {
            return;
        }

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
            return;
        }
        rrw.disconnect();

        initialized = false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlConnect = new javax.swing.JPanel();
        lblPort = new javax.swing.JLabel();
        cbxPort = new javax.swing.JComboBox<>();
        tgbConnect = new javax.swing.JToggleButton();
        lblStatus = new javax.swing.JLabel();
        pnlSetup = new javax.swing.JPanel();
        lblRfid = new javax.swing.JLabel();
        lblEndIntersection = new javax.swing.JLabel();
        chkEndIntersection = new javax.swing.JCheckBox();
        lblRoadInIntersection = new javax.swing.JLabel();
        lblIntersectionNo = new javax.swing.JLabel();
        lblBeginIntersection = new javax.swing.JLabel();
        chkBeginIntersection = new javax.swing.JCheckBox();
        btnWrite = new javax.swing.JButton();
        btnRead = new javax.swing.JButton();
        txtR = new javax.swing.JFormattedTextField();
        txtF = new javax.swing.JFormattedTextField();
        txtI = new javax.swing.JFormattedTextField();
        txtD = new javax.swing.JFormattedTextField();
        txtIntersectionNo = new javax.swing.JFormattedTextField();
        txtIntersectionRoadNo = new javax.swing.JFormattedTextField();

        setMinimumSize(new java.awt.Dimension(640, 320));
        setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/com/aubot/agv/integration/rfid/Bundle"); // NOI18N
        pnlConnect.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("rfidSetupPanel.connectPanel.title")), javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1))); // NOI18N
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.LEADING);
        flowLayout1.setAlignOnBaseline(true);
        pnlConnect.setLayout(flowLayout1);

        lblPort.setText(bundle.getString("rfidSetupPanel.connectPanel.lblPort")); // NOI18N
        pnlConnect.add(lblPort);

        cbxPort.setPreferredSize(new java.awt.Dimension(300, 29));
        pnlConnect.add(cbxPort);

        tgbConnect.setText(bundle.getString("rfidSetupPanel.connectPanel.tgbConnect")); // NOI18N
        pnlConnect.add(tgbConnect);
        pnlConnect.add(lblStatus);

        add(pnlConnect, java.awt.BorderLayout.PAGE_START);

        pnlSetup.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("rfidSetupPanel.setupPanel.title"))); // NOI18N
        java.awt.GridBagLayout jPanel2Layout = new java.awt.GridBagLayout();
        jPanel2Layout.columnWidths = new int[] {0, 10, 0, 10, 0, 10, 0, 10, 0, 10, 0, 10, 0, 10, 0, 10, 0, 10, 0, 10, 0, 10, 0, 10, 0};
        jPanel2Layout.rowHeights = new int[] {0, 10, 0, 10, 0, 10, 0, 10, 0};
        pnlSetup.setLayout(jPanel2Layout);

        lblRfid.setText(bundle.getString("rfidSetupPanel.setupPanel.lblRfid")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlSetup.add(lblRfid, gridBagConstraints);

        lblEndIntersection.setText(bundle.getString("rfidSetupPanel.setupPanel.lblEndIntersection")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlSetup.add(lblEndIntersection, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlSetup.add(chkEndIntersection, gridBagConstraints);

        lblRoadInIntersection.setText(bundle.getString("rfidSetupPanel.setupPanel.lblIntersectionRoadNo")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlSetup.add(lblRoadInIntersection, gridBagConstraints);

        lblIntersectionNo.setText(bundle.getString("rfidSetupPanel.setupPanel.lblIntersectionNo")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlSetup.add(lblIntersectionNo, gridBagConstraints);

        lblBeginIntersection.setText(bundle.getString("rfidSetupPanel.setupPanel.lblBeginIntersection")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlSetup.add(lblBeginIntersection, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        pnlSetup.add(chkBeginIntersection, gridBagConstraints);

        btnWrite.setText(bundle.getString("rfidSetupPanel.setupPanel.btnWrite")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        pnlSetup.add(btnWrite, gridBagConstraints);

        btnRead.setText(bundle.getString("rfidSetupPanel.setupPanel.btnRead")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        pnlSetup.add(btnRead, gridBagConstraints);

        txtR.setColumns(1);
        txtR.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        pnlSetup.add(txtR, gridBagConstraints);

        txtF.setColumns(1);
        txtF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        pnlSetup.add(txtF, gridBagConstraints);

        txtI.setColumns(1);
        txtI.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        pnlSetup.add(txtI, gridBagConstraints);

        txtD.setColumns(1);
        txtD.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        pnlSetup.add(txtD, gridBagConstraints);

        txtIntersectionNo.setColumns(3);
        txtIntersectionNo.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        pnlSetup.add(txtIntersectionNo, gridBagConstraints);

        txtIntersectionRoadNo.setColumns(3);
        txtIntersectionRoadNo.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        pnlSetup.add(txtIntersectionRoadNo, gridBagConstraints);

        add(pnlSetup, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRead;
    private javax.swing.JButton btnWrite;
    private javax.swing.JComboBox<String> cbxPort;
    private javax.swing.JCheckBox chkBeginIntersection;
    private javax.swing.JCheckBox chkEndIntersection;
    private javax.swing.JLabel lblBeginIntersection;
    private javax.swing.JLabel lblEndIntersection;
    private javax.swing.JLabel lblIntersectionNo;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblRfid;
    private javax.swing.JLabel lblRoadInIntersection;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JPanel pnlConnect;
    private javax.swing.JPanel pnlSetup;
    private javax.swing.JToggleButton tgbConnect;
    private javax.swing.JFormattedTextField txtD;
    private javax.swing.JFormattedTextField txtF;
    private javax.swing.JFormattedTextField txtI;
    private javax.swing.JFormattedTextField txtIntersectionNo;
    private javax.swing.JFormattedTextField txtIntersectionRoadNo;
    private javax.swing.JFormattedTextField txtR;
    // End of variables declaration//GEN-END:variables
}
