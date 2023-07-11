package org.opentcs.drivers.vehicle;

import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.model.Vehicle;

public class ValidateException extends KernelRuntimeException {

  public ValidateException() {
  }

  public ValidateException(String message) {
    super(message);
  }

  public ValidateException(String message, Throwable cause) {
    super(message, cause);
  }
}
