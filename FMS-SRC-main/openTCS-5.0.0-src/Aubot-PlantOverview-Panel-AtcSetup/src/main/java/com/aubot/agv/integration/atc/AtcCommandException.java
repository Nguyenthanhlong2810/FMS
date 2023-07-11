package com.aubot.agv.integration.atc;

import java.util.Locale;
import java.util.ResourceBundle;

public class AtcCommandException extends Exception {

  private int code;

  public static final int JOIN_FAIL = 0x01;

  public static final int PORT_INVALID = 0x02;

  public static final int IP_INVALID = 0x03;

  public static final int MASK_INVALID = 0x04;

  public static final int GATEWAY_INVALID = 0x05;

  public static final int REQUEST_TIMEOUT = 0x06;

  public static final int DISCONNECTED = 0x0A;

  public static final int ERROR_FROM_ATC = 0x0B;

  /**
   * Constructs a new exception with the specified detail message.  The
   * cause is not initialized, and may subsequently be initialized by
   * a call to {@link #initCause}.
   *
   */
  public AtcCommandException(int code) {
    super();
    this.code = code;
  }

  /**
   * Constructs a new exception with the specified detail message.  The
   * cause is not initialized, and may subsequently be initialized by
   * a call to {@link #initCause}.
   *
   * @param message the detail message. The detail message is saved for
   *                later retrieval by the {@link #getMessage()} method.
   */
  public AtcCommandException(String message) {
    super(message);
    this.code = ERROR_FROM_ATC;
  }

  public int getCode() {
    return code;
  }
}
