package org.opentcs.database.entity;

public class ErrorCount {
    private String errorCode;
    private String errorMessage;
    private String errorVehicle;
    private int count;

    public ErrorCount() {}

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setErrorVehicle(String errorVehicle) {
        this.errorVehicle = errorVehicle;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorVehicle() {
        return errorVehicle;
    }

    public int getCount() {
        return count;
    }
}
