package cfg.aubot.commadapter.tcp;

import cfg.aubot.commadapter.tcp.validator.FieldInvalidException;
import cfg.aubot.commadapter.tcp.validator.HostPortDuplicateException;
import cfg.aubot.commadapter.tcp.validator.TcpValidateException;
import org.opentcs.drivers.vehicle.ValidateException;
import org.opentcs.drivers.vehicle.management.VehicleCommunicationInfoPanel;
import org.opentcs.example.VehicleProperties;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class TcpCommInfoPanel extends VehicleCommunicationInfoPanel {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(I18nVehicleTcpConnectionInfo.BUNDLE_PATH);

    JLabel lblHost;
    JLabel lblPort;
    JTextField txtHost;
    JFormattedTextField txtPort;

    public TcpCommInfoPanel() {
        initComponents();
    }

    private void initComponents() {
        lblHost = new JLabel(BUNDLE.getString("commInfoPanel.label.host"));
        lblPort = new JLabel(BUNDLE.getString("commInfoPanel.label.port"));
        txtHost = new JTextField();
        txtPort = new JFormattedTextField();
        DecimalFormat decimalFormat = new DecimalFormat("###0");
        NumberFormatter nft = new NumberFormatter(decimalFormat);
        nft.setValueClass(Integer.class);
        nft.setMinimum(0);
        nft.setMaximum(65535);
        nft.setAllowsInvalid(false);
        nft.setCommitsOnValidEdit(true);
        DefaultFormatterFactory dff = new DefaultFormatterFactory(nft);
        FocusListener selectAllListener = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                ((JFormattedTextField) e.getSource()).selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        };
        txtPort.setFormatterFactory(dff);
        txtPort.addFocusListener(selectAllListener);

        txtPort.setText("2020");

        this.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.weighty = 0.1;
        g.gridx = 0;
        g.gridy = 0;
        g.insets = new Insets(0, 0, 0, 10);
        this.add(lblHost, g);

        g.gridy = 1;
        this.add(lblPort, g);

        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 1;
        g.gridy = 0;
        g.weightx = 0.6;
        g.insets = new Insets(0, 0, 0, 0);
        this.add(txtHost, g);

        g.gridy = 1;
        this.add(txtPort, g);
    }

    @Override
    public void setCommInfo(Map<String, String> info) {
        txtHost.setText(info.get(VehicleProperties.PROPKEY_VEHICLE_HOST));
        txtPort.setText(info.get(VehicleProperties.PROPKEY_VEHICLE_PORT));
    }

    @Override
    public Map<String, String> getCommInfo() {
        HashMap<String,String> info = new HashMap<>();
        info.put(VehicleProperties.PROPKEY_VEHICLE_HOST, txtHost.getText().trim());
        info.put(VehicleProperties.PROPKEY_VEHICLE_PORT, txtPort.getText().trim());
        return info;
    }

    @Override
    public int getMinHeight() {
        return 100;
    }

    @Override
    public void handleException(ValidateException ex) {
        if (ex instanceof TcpValidateException) {
            if (ex instanceof FieldInvalidException) {
                if (((FieldInvalidException) ex).getType() == FieldInvalidException.INVALID_HOST) {
                    showErrorDialog(BUNDLE.getString("validate.invalidHost"));
                } else if (((FieldInvalidException) ex).getType() == FieldInvalidException.INVALID_PORT) {
                    showErrorDialog(BUNDLE.getString("validate.invalidPort"));
                } else {
                    showErrorDialog("Unknown invalid communication field");
                }
            } else if (ex instanceof HostPortDuplicateException) {
                showErrorDialog(MessageFormat.format(BUNDLE.getString("validate.hostPortExistsVehicle"),
                        ((HostPortDuplicateException) ex).getVehicle().getName()));
            }
        } else {
            showErrorDialog(ex.getMessage());
        }
    }

    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, BUNDLE.getString("comInfoPanel.error.title"), JOptionPane.ERROR_MESSAGE);
    }
}
