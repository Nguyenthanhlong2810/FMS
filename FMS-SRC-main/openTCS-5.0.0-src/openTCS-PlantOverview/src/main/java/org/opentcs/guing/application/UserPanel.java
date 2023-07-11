package org.opentcs.guing.application;

import com.google.inject.Inject;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.common.PortalManager;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.event.EventHandler;

import javax.inject.Provider;
import javax.swing.*;

import java.awt.*;

import static java.util.Objects.requireNonNull;
import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;

public class UserPanel extends JPanel implements EventHandler {

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);

  private final SharedKernelServicePortalProvider sharedProvider;

  private final PortalManager portalManager;

  private final OpenTCSView openTCSView;

  private final ApplicationState applicationState;

  private final Provider<ChangePasswordDialog> changePasswordProvider;

  private JButton btnSwitchMode;
  private JLabel lblUsername;
  private JButton btnLogin;
  private JButton btnLogout;
  private JButton btnChangePassword;

  private boolean loggedIn;

  @Inject
  public UserPanel(SharedKernelServicePortalProvider sharedProvider,
                   PortalManager portalManager,
                   OpenTCSView openTCSView,
                   ApplicationState applicationState,
                   Provider<ChangePasswordDialog> changePasswordProvider) {
    this.sharedProvider = requireNonNull(sharedProvider, "sharedProvider");
    this.portalManager = requireNonNull(portalManager, "portalManager");
    this.openTCSView = requireNonNull(openTCSView, "openTCSView");
    this.applicationState = requireNonNull(applicationState, "applicationState");
    this.changePasswordProvider = requireNonNull(changePasswordProvider, "changePasswordProvider");
    initComponents();
  }

  private void initComponents() {
    setLayout(new FlowLayout(FlowLayout.RIGHT));
    btnSwitchMode = new JButton(ImageDirectory.getImageIcon("/menu/switch-mode.png"));
    btnSwitchMode.setText(BUNDLE.getString("switchToOperatingAction.name"));
    btnSwitchMode.setToolTipText(BUNDLE.getString("switchToOperatingAction.shortDescription"));
    btnSwitchMode.addActionListener(e->{
      if(applicationState.getOperationMode() == OperationMode.MODELLING){
        openTCSView.switchPlantOverviewState(OperationMode.OPERATING);
      }else {
        openTCSView.switchPlantOverviewState(OperationMode.MODELLING);
      }
    });
    add(btnSwitchMode);
    lblUsername = new JLabel();
    lblUsername.setHorizontalAlignment(SwingConstants.CENTER);
    add(lblUsername);
    btnLogin = new JButton(BUNDLE.getString("button.login"));
    btnLogin.addActionListener(e -> login());
    add(btnLogin);
    btnChangePassword = new JButton(ImageDirectory.getImageIcon("/menu/changePassword.png"));
    btnChangePassword.addActionListener(e -> changePasswordProvider.get().setVisible(true));
    add(btnChangePassword);
    btnLogout = new JButton(ImageDirectory.getImageIcon("/menu/logout.png"));
    btnLogout.addActionListener(e -> {
      if (applicationState.hasOperationMode(OperationMode.OPERATING)) {
        if (JOptionPane.showConfirmDialog(this,
                BUNDLE.getString("button.logout.confirmation.message"),
                BUNDLE.getString("button.logout.confirmation.title"),
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
          return;
        }
      }
      logout();
    });
    add(btnLogout);
    setLoggedIn(false);
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof OperationModeChangeEvent) {
      handleModeChange((OperationModeChangeEvent) event);
    }
  }

  private void handleModeChange(OperationModeChangeEvent evt) {
    switch (evt.getNewMode()) {
      case OPERATING:
        btnSwitchMode.setText(BUNDLE.getString("switchToModellingAction.name"));
        btnSwitchMode.setToolTipText(BUNDLE.getString("switchToModellingAction.shortDescription"));
        login();
        break;
      case MODELLING:
        btnSwitchMode.setText(BUNDLE.getString("switchToOperatingAction.name"));
        btnSwitchMode.setToolTipText(BUNDLE.getString("switchToOperatingAction.shortDescription"));
//        logout();
      default:
    }
  }

  void setLoggedIn(boolean loggedIn) {
    this.loggedIn = loggedIn;
    btnLogin.setVisible(!loggedIn);
    lblUsername.setVisible(loggedIn);
    btnChangePassword.setVisible(loggedIn);
    btnLogout.setVisible(loggedIn);
    portalManager.setMode(loggedIn ? PortalManager.ConnectionMode.AUTO : PortalManager.ConnectionMode.MANUAL);
  }

  public boolean isLoggedIn() {
    return loggedIn;
  }

  private void login() {
    if (isLoggedIn()) {
      return;
    }

    try (SharedKernelServicePortal servicePortal = sharedProvider.register()) {
      lblUsername.setText(servicePortal.getPortal().getClientId().getClientName());
      setLoggedIn(true);
    } catch (KernelRuntimeException ex) {
      ex.printStackTrace();
    }
  }

  private void logout() {
    if (!isLoggedIn()) {
      return;
    }
    lblUsername.setText("");
    if (applicationState.getOperationMode() != OperationMode.MODELLING) {
      btnSwitchMode.doClick();
    }
    setLoggedIn(false);
  }
}
