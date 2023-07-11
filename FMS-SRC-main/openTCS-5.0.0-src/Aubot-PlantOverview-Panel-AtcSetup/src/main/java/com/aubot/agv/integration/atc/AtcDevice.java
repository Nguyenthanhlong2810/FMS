package com.aubot.agv.integration.atc;

import com.aubot.agv.integration.atc.commands.*;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.util.List;

public class AtcDevice implements SerialPortDataListener {

  private SerialPort port;

  private AtcCommunicationListener listener;

  private volatile StringBuffer buffer = new StringBuffer();

  private volatile boolean lock = false;

  private volatile int cycle = 0;

  private static final int MAX_CYCLE = 360;

  public void setListener(AtcCommunicationListener listener) {
    this.listener = listener;
  }

  public boolean isConnected() {
    return port != null && port.isOpen();
  }

  public boolean connectAtc(SerialPort port) {
    if (isConnected()) {
      return false;
    }
    if (port == null) {
      return false;
    }
    this.port = port;
    port.setBaudRate(115200);
    port.setNumDataBits(8);
    port.setNumStopBits(1);
    port.setParity(0);
    port.addDataListener(this);
    if (port.openPort()) {
      try {
        startUart();
      } catch (Exception ignored) {}
    }

    return port.isOpen();
  }

  private void startUart() throws Exception {
    execute(new AtcCommand() {
      @Override
      public String getCommand() {
        return "+++";
      }

      @Override
      public void handleResult(String result) {
        // Do nothing
      }

      @Override
      protected boolean handleResult0(String[] lines) {
        return true;
      }
    });
  }

  public void disconnectAtc() {
    if (port != null) {
      if (port.isOpen()) {
        port.closePort();
      }
      port.removeDataListener();
      port = null;
    }
  }

  public List<String> getWifiList() throws Exception {
    GetWifiListCommand command = new GetWifiListCommand();
    execute(command);

    return command.getWifiNames();
  }

  public String getCurrentWifi() throws Exception {
    GetCurrentWifiCommand command = new GetCurrentWifiCommand();
    execute(command);

    return command.getWifiName();
  }

  public boolean setup(int jbss, String password) throws Exception {
    if (!password.equals("")) {
      execute(new SetWifiPasswordCommand(password));
    }
    setDhcpClient(true);
    setServerPort(2020);
    return execute(new ConnectWifiCommand(jbss)).isSuccess();
  }

  public boolean setAddresses(String ip, String mask, String gateway) throws AtcCommandException {
    String ADDRESS_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    if (!ip.matches(ADDRESS_REGEX)) {
      throw new AtcCommandException(AtcCommandException.IP_INVALID);
    }
    if (!mask.matches(ADDRESS_REGEX)) {
      throw new AtcCommandException(AtcCommandException.MASK_INVALID);
    }
    if (!gateway.matches(ADDRESS_REGEX)) {
      throw new AtcCommandException(AtcCommandException.GATEWAY_INVALID);
    }

    setDhcpClient(false);
    return setIp(ip) && setMask(mask) && setGateway(gateway) && saveConfig();
  }

  public boolean saveConfig() throws AtcCommandException {
    return execute(new SaveConfigCommand()).isSuccess();
  }

  public boolean setIp(String ipAddress) throws AtcCommandException {
    return execute(new SetIpCommand(ipAddress)).isSuccess();
  }

  public boolean setMask(String mask) throws AtcCommandException {
    return execute(new SetMaskCommand(mask)).isSuccess();
  }

  public boolean setGateway(String gateway) throws AtcCommandException {
    return execute(new SetGatewayCommand(gateway)).isSuccess();
  }

  public boolean setDhcpClient(boolean checked) throws AtcCommandException {
    return execute(new SetDhcpClientCommand(checked)).isSuccess();
  }

  public boolean setServerPort(int port) throws AtcCommandException {
    if (port < 0 || port > 65535) {
      throw new AtcCommandException(AtcCommandException.PORT_INVALID);
    }

    return execute(new SetServerModeCommand()).isSuccess() &&
           execute(new SetServerPortCommand(port)).isSuccess();
  }

  public String[] getAddresses() throws AtcCommandException {
    GetIpConfigCommand command = new GetIpConfigCommand();
    execute(command);

    return command.getAddresses();
  }

  public void reboot() {
    try {
      execute(new RebootCommand()).isSuccess();
    } catch (AtcCommandException ignored) {}
  }

  private synchronized AtcCommand execute(AtcCommand command) throws AtcCommandException {
    if (port == null || !port.isOpen()) {
      throw new AtcCommandException(AtcCommandException.DISCONNECTED);
    }

    lock = true;
    String cmdStr = command.getCommand() + '\r';
    port.writeBytes(cmdStr.getBytes(), cmdStr.length());
    while (lock) {
      cycle++;
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (cycle >= MAX_CYCLE) {
        throw new AtcCommandException(AtcCommandException.REQUEST_TIMEOUT);
      }
    }
    lock = false;
    command.handleResult(buffer.toString());
    buffer.setLength(0);

    if (!command.isSuccess()) {
      throw new AtcCommandException(command.getError());
    }

    return command;
  }

  /**
   * Must be overridden to return one or more desired event constants for which the {@link #serialEvent(SerialPortEvent)} callback should be triggered.
   * <p>
   * Valid event constants are:
   * <p>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@link SerialPort#LISTENING_EVENT_DATA_AVAILABLE}<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@link SerialPort#LISTENING_EVENT_DATA_RECEIVED}<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{@link SerialPort#LISTENING_EVENT_DATA_WRITTEN}<br>
   * <p>
   * Two or more events may be OR'd together to listen for multiple events; however, if {@link SerialPort#LISTENING_EVENT_DATA_AVAILABLE} is OR'd with {@link SerialPort#LISTENING_EVENT_DATA_RECEIVED}, the {@link SerialPort#LISTENING_EVENT_DATA_RECEIVED} flag will take precedence.
   * <p>
   * Note that event-based <i>write</i> callbacks are only supported on Windows operating systems. As such, the {@link SerialPort#LISTENING_EVENT_DATA_WRITTEN}
   * event will never be called on a non-Windows system.
   *
   * @return The event constants that should trigger the {@link #serialEvent(SerialPortEvent)} callback.
   * @see SerialPort#LISTENING_EVENT_DATA_AVAILABLE
   * @see SerialPort#LISTENING_EVENT_DATA_RECEIVED
   * @see SerialPort#LISTENING_EVENT_DATA_WRITTEN
   */
  @Override
  public int getListeningEvents() {
    return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
  }

  /**
   * Called whenever one of the serial port events specified by the {@link #getListeningEvents()} method occurs.
   * <p>
   * Note that your implementation of this function should always perform as little data processing as possible, as the speed at which this callback will fire is at the mercy of the underlying operating system. If you need to collect a large amount of data, application-level buffering should be implemented and data processing should occur on a separate thread.
   *
   * @param event A {@link SerialPortEvent} object containing information and/or data about the serial event that occurred.
   * @see SerialPortEvent
   */
  @Override
  public void serialEvent(SerialPortEvent event) {
    String message = new String(event.getReceivedData());
    if (lock) {
      if (message.contains(">")) {
        lock = false;
      } else {
        buffer.append(message);
        if (listener != null) {
          listener.onLogging(message);
        }
      }
      cycle = 0;
    }
  }
}
