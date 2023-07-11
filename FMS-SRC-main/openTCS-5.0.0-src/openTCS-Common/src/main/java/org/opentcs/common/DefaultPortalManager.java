/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.common;

import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javax.inject.Inject;
import javax.swing.JOptionPane;

import org.opentcs.access.CredentialsException;
import org.opentcs.database.logging.logentity.Logs;
import org.opentcs.database.logging.LogManager;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.util.I18nCommon;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.gui.dialog.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of {@link PortalManager}, providing a single
 * {@link KernelServicePortal}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultPortalManager
    implements PortalManager {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultPortalManager.class);
  /**
   * This class's resource bundle.
   */
  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle(I18nCommon.BUNDLE_PATH);
  /**
   * The handler to send events to.
   */
  private final EventHandler eventHandler;
  /**
   * The connection bookmarks to use.
   */
  private final List<ConnectionParamSet> connectionBookmarks;
  /**
   * The service portal instance we are working with.
   */
  private final KernelServicePortal servicePortal;
  /**
   * The last successfully established connection.
   */
  private ConnectionParamSet lastConnection = new NullConnectionParamSet();
  /**
   * The current connection. {@link NullConnectionParamSet}, if no connection is currently
   * established.
   */
  private ConnectionParamSet currentConnection = new NullConnectionParamSet();


  private ConnectionMode mode = ConnectionMode.MANUAL;
  /**
   * Username and password
   */
  private String username;
  private String password;
  private static final String LAST_USER = "login.lastUser";
  private static final String LAST_IP = "login.lastIp";
  private static final String LAST_PORT = "login.lastPort";
  /**
   * Creates a new instance.
   *
   * @param servicePortal The service portal instance we a working with.
   * @param eventHandler The handler to send events to.
   * @param connectionBookmarks The connection bookmarks to use.
   */
  @Inject
  public DefaultPortalManager(KernelServicePortal servicePortal,
                              @ApplicationEventBus EventHandler eventHandler,
                              List<ConnectionParamSet> connectionBookmarks) {
    this.eventHandler = requireNonNull(eventHandler, "eventHandler");
    this.servicePortal = requireNonNull(servicePortal, "servicePortal");
    this.connectionBookmarks = requireNonNull(connectionBookmarks, "connectionBookmarks");

    Preferences prefs = Preferences.userNodeForPackage(DefaultPortalManager.class);
    username = prefs.get(LAST_USER, null);
    lastConnection = new ConnectionParamSet("SERVER",
            prefs.get(LAST_IP, "localhost"),
            prefs.getInt(LAST_PORT, 1099));
  }

  @Override
  public boolean connect(int key) {
    if (isConnected()) {
      return true;
    }

    switch (mode) {
      case AUTO:
//        if (connectionBookmarks.isEmpty()) {
//          LOG.info("Cannot connect automatically. No connection bookmarks available.");
//          return false;
//        }
        ConnectionParamSet paramSet = lastConnection;
        return connectWithLogin(paramSet.getHost(), paramSet.getPort(), key, username, password);
      case MANUAL:
        return connectWithDialog(key);
      case RECONNECT:
        if (lastConnection instanceof NullConnectionParamSet) {
          LOG.info("Cannot reconnect. No portal we were previously connected to.");
          return false;
        }
        return connectWithLogin(lastConnection.getHost(), lastConnection.getPort(), key, username, password);
      default:
        LOG.warn("Unhandled connection mode '{}'. Not connecting.", mode.name());
        return false;
    }
  }

  @Override
  public void disconnect( int key) {
    if (!isConnected()) {
      return;
    }

    eventHandler.onEvent(ConnectionState.DISCONNECTING);

    try {
      servicePortal.logout();
      String path = DefaultPortalManager.class.getCanonicalName() + ".disconnect()";
      String content;
      if (key == 0 ) {
        content = "KernelControlCenter disconnected  ";
      }
      else {
        content = "PlantOverview disconnected  ";
      }
      writeLog(path,content,Logs.LEVEL.INFO);
    }
    catch (KernelRuntimeException e) {
      LOG.warn("Exception trying to disconnect from remote portal", e);

      String path = DefaultPortalManager.class.getCanonicalName() + ".disconnect()";
      String content;
      if (key == 0 ) {
        content = "KernelControlCenter disconnection failed : " + e.getMessage();
      }
      else {
        content = "PlantOverview disconnection failed : " + e.getMessage();
      }
      writeLog(path,content,Logs.LEVEL.INFO);
    }

    lastConnection = currentConnection;
    currentConnection = new NullConnectionParamSet();
    eventHandler.onEvent(ConnectionState.DISCONNECTED);
  }

  @Override
  public boolean isConnected() {
    return !(currentConnection instanceof NullConnectionParamSet);
  }

  @Override
  public KernelServicePortal getPortal() {
    return servicePortal;
  }

  @Override
  public String getDescription() {
    return currentConnection.getDescription();
  }

  @Override
  public String getHost() {
    return currentConnection.getHost();
  }

  @Override
  public int getPort() {
    return currentConnection.getPort();
  }

  @Override
  public ConnectionMode getMode() {
    return mode;
  }

  @Override
  public void setMode(ConnectionMode mode) {
    requireNonNull(mode);
    this.mode = mode;
  }

  private boolean connectWithLogin(String host, int port, int key, String username, String password) {
    try {
      eventHandler.onEvent(ConnectionState.CONNECTING);
      servicePortal.login(host, port,username,password);
      Preferences prefs = Preferences.userNodeForPackage(DefaultPortalManager.class);
      prefs.put(LAST_USER, username);
      prefs.put(LAST_IP, currentConnection.getHost());
      prefs.putInt(LAST_PORT, currentConnection.getPort());
    }
    catch (KernelRuntimeException e) {
      LOG.warn("Failed to connect to remote portal", e);
      String path = DefaultPortalManager.class.getCanonicalName() + ".connect()";
      String content;
      if (key == 0 )
      {
        content = "KernelControlCenter connection failed : " + e.getMessage();
      } else {
        content = "PlantOverview connection failed : " + e.getMessage();
      }
      writeLog(path,content,Logs.LEVEL.ERROR);
      eventHandler.onEvent(ConnectionState.DISCONNECTED);
      String message = BUNDLE.getString("connectToServerDialog.optionPane_noConnection.message");
      if (e instanceof CredentialsException) {
        if (((CredentialsException) e).getType() == CredentialsException.Type.AUTHENTICATION_FAILED) {
          message = BUNDLE.getString("connectToServerDialog.optionPane_authenticationFailed.message");
        }
      }
      JOptionPane.showMessageDialog(null,
              message,
              BUNDLE.getString("connectToServerDialog.optionPane_noConnection.message"),
              JOptionPane.ERROR_MESSAGE);
      // Retry connection attempt
      return connectWithDialog(key);
    }

    currentConnection = new ConnectionParamSet("SERVER", host, port);
    eventHandler.onEvent(ConnectionState.CONNECTED);
    String path = DefaultPortalManager.class.getCanonicalName() + ".connect()";
    String content;
    if (key == 0 ) {
      content = "KernelControlCenter connection success ";
    }
    else {
      content = "PlantOverview connection success ";
    }
    writeLog(path,content,Logs.LEVEL.INFO);
    return true;
  }
  /**
   * Tries to establish a connection to the portal.
   *
   * @param host The name of the host running the kernel/portal.
   * @param port The port to connect to.
   * @return {@code true} if, and only if, the connection was established successfully.
   */
  private boolean connect(String description, String host, int port, int key) {
    try {
      eventHandler.onEvent(ConnectionState.CONNECTING);
      servicePortal.login(host, port, username, password);
    }
    catch (KernelRuntimeException e) {
      LOG.warn("Failed to connect to remote portal", e);
      String path = DefaultPortalManager.class.getCanonicalName() + ".connect()";
      String content;
      if (key == 0 )
      {
        content = "KernelControlCenter connection failed : " + e.getMessage();
      } else {
        content = "PlantOverview connection failed : " + e.getMessage();
      }
      writeLog(path,content,Logs.LEVEL.ERROR);
      eventHandler.onEvent(ConnectionState.DISCONNECTED);
      JOptionPane.showMessageDialog(null,
                                    BUNDLE.getString("connectToServerDialog.optionPane_noConnection.message"),
                                    BUNDLE.getString("connectToServerDialog.optionPane_noConnection.message"),
                                    JOptionPane.ERROR_MESSAGE);
      // Retry connection attempt
      return connectWithDialog(key);
    }

    currentConnection = new ConnectionParamSet(description, host, port);
    eventHandler.onEvent(ConnectionState.CONNECTED);
    String path = DefaultPortalManager.class.getCanonicalName() + ".connect()";
    String content;
    if (key == 0 ) {
      content = "KernelControlCenter connection success ";
    }
    else {
      content = "PlantOverview connection success ";
    }
    writeLog(path,content,Logs.LEVEL.INFO);
    return true;
  }

  private void writeLog(String path,String content,Logs.LEVEL level){
    Logs log = new Logs();
    log.setMethod(path);
    log.setLevel(level.name());
    log.setContent(content);
    LogManager logManager = new LogManager();
    logManager.listen();
    logManager.submit(log);
  }
  private boolean connectWithDialog(int key) {
    ConnectionParamSet paramSet = lastConnection;
    if (!connectionBookmarks.isEmpty() && lastConnection instanceof NullConnectionParamSet) {
      paramSet = connectionBookmarks.get(0);
    }
    LoginUserToServerDialog dialog = new LoginUserToServerDialog(paramSet, username, password);
    dialog.setVisible(true);
    if (dialog.getReturnStatus() == ConnectToServerDialog.RET_OK) {
      username = dialog.getUsername();
      password = dialog.getPassword();
      currentConnection = new ConnectionParamSet("SERVER", dialog.getHost(), dialog.getPort());
      return connectWithLogin(dialog.getHost(), dialog.getPort(), key, dialog.getUsername(), dialog.getPassword());
    }

    return false;
  }
}
