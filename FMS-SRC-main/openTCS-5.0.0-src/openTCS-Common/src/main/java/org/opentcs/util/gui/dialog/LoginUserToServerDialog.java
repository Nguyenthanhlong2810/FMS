/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.util.gui.dialog;

import org.opentcs.util.I18nCommon;
import org.opentcs.util.gui.Icons;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 *
 * @author ADMIN
 */
public class LoginUserToServerDialog
    extends javax.swing.JDialog {

  /**
   * This class's resource bundle.
   */
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(I18nCommon.BUNDLE_PATH);
  /**
   * A return status code - returned if Cancel button has been pressed.
   */
  public static final int RET_CANCEL = 0;
  /**
   * A return status code - returned if OK button has been pressed.
   */
  public static final int RET_OK = 1;

  private String host;
  private int port;
  private String username;
  private String password;
  private int returnStatus;
  /**
   * Creates new form ConnectUserToServerDialog
   */
  public LoginUserToServerDialog(ConnectionParamSet paramSet, String username, String password) {
    super((JFrame) null, true);
    initComponents();
    initComponentsExtra();
    txtHost.setText(paramSet.getHost());
    txtPort.setText(String.valueOf(paramSet.getPort()));
    txtUsername.requestFocus();
    if (username != null) {
      txtUsername.setText(username);
      txtPassword.requestFocus();
    }
    if (password != null) {
      txtPassword.setText(password);
      txtPassword.selectAll();
    }
  }

  private void initComponentsExtra() {
    txtPassword.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) { txtPassword.selectAll(); }

      @Override
      public void focusLost(FocusEvent e) {}
    });

    NumberFormatter nft = new NumberFormatter(new DecimalFormat("####"));
    nft.setMinimum(0);
    nft.setMaximum(65535);
    nft.setAllowsInvalid(false);
    txtPort.setFormatterFactory(new DefaultFormatterFactory(nft));

    getRootPane().setDefaultButton(btnOk);
    setIconImages(Icons.getOpenTCSIcons());
    setLocationRelativeTo(null);
    btnOk.addActionListener(e -> {
      host = txtHost.getText();
      if (host.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                BUNDLE.getString("connectToServerDialog.optionPane_invalidHost.message"),
                BUNDLE.getString("connectToServerDialog.optionPane_invalidHost.title"),
                JOptionPane.ERROR_MESSAGE
        );
        return;
      }

      try {
        port = Integer.parseInt(txtPort.getText());
        if (port < 0 || port > 65535) {
          throw new NumberFormatException();
        }
      }
      catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this,
                BUNDLE.getString("connectToServerDialog.optionPane_invalidPort.message"),
                BUNDLE.getString("connectToServerDialog.optionPane_invalidPort.title"),
                JOptionPane.ERROR_MESSAGE
        );
        return;
      }

      username = txtUsername.getText().trim();
      password = String.valueOf(txtPassword.getPassword()).trim();

      doClose(RET_OK);
    });

    btnCancel.addActionListener(e -> doClose(RET_CANCEL));
  }

  /**
   * Returns the return status of this dialog.
   *
   * @return the return status of this dialog - one of {@link #RET_OK} or {@link #RET_CANCEL}.
   */
  public int getReturnStatus() {
    return returnStatus;
  }

  private void doClose(int retStatus) {
    returnStatus = retStatus;
    setVisible(false);
    dispose();
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
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

    lblHost = new javax.swing.JLabel();
    txtHost = new javax.swing.JTextField();
    lblUsername = new javax.swing.JLabel();
    txtUsername = new javax.swing.JTextField();
    lblPassword = new javax.swing.JLabel();
    txtPassword = new javax.swing.JPasswordField();
    lblPort = new javax.swing.JLabel();
    pnlButtons = new javax.swing.JPanel();
    btnOk = new javax.swing.JButton();
    btnCancel = new javax.swing.JButton();
    txtPort = new javax.swing.JFormattedTextField();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/org/opentcs/common/Bundle"); // NOI18N
    setTitle(bundle.getString("connectToServerDialog.title")); // NOI18N
    setPreferredSize(new java.awt.Dimension(400, 200));
    getContentPane().setLayout(new java.awt.GridBagLayout());

    lblHost.setText(bundle.getString("connectToServerDialog.label_host.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    getContentPane().add(lblHost, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    getContentPane().add(txtHost, gridBagConstraints);

    lblUsername.setText(bundle.getString("connectUserToServerDialog.username")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    getContentPane().add(lblUsername, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    getContentPane().add(txtUsername, gridBagConstraints);

    lblPassword.setText(bundle.getString("connectUserToServerDialog.password")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    getContentPane().add(lblPassword, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    getContentPane().add(txtPassword, gridBagConstraints);

    lblPort.setText(bundle.getString("connectToServerDialog.label_port.text")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    getContentPane().add(lblPort, gridBagConstraints);

    btnOk.setText(bundle.getString("connectToServerDialog.button_ok.text")); // NOI18N
    pnlButtons.add(btnOk);

    btnCancel.setText(bundle.getString("connectToServerDialog.button_cancle.text")); // NOI18N
    pnlButtons.add(btnCancel);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 4;
    getContentPane().add(pnlButtons, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    getContentPane().add(txtPort, gridBagConstraints);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnCancel;
  private javax.swing.JButton btnOk;
  private javax.swing.JLabel lblHost;
  private javax.swing.JLabel lblPassword;
  private javax.swing.JLabel lblPort;
  private javax.swing.JLabel lblUsername;
  private javax.swing.JPanel pnlButtons;
  private javax.swing.JTextField txtHost;
  private javax.swing.JPasswordField txtPassword;
  private javax.swing.JFormattedTextField txtPort;
  private javax.swing.JTextField txtUsername;
  // End of variables declaration//GEN-END:variables
}
