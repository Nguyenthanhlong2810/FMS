package org.opentcs.common;

import java.util.ArrayList;
import java.util.List;

public enum VehicleError {
  OUTLINE,
  LOSS_GUIDELINE,
  LOSS_CAN,
  OVERLOAD,
  E_STOP,
  LOW_BATTERY;

  public static List<VehicleError> decode(int errorCode) {
    List<VehicleError> errors = new ArrayList<>();
    int i = 0;
    for (VehicleError error : VehicleError.values()) {
      if (((errorCode >> i) & 1) == 1) {
        errors.add(error);
      }
      i++;
    }
    return errors;
  }

  public static List<VehicleError> getNewErrorFromPrevious(int previousErrorCode, int errorCode) {
    int newErrors = (previousErrorCode ^ errorCode) & errorCode;
    return decode(newErrors);
  }
}
