package org.opentcs.database.entity;

import java.sql.Timestamp;

public class ErrorLog {
    private String errorCode;
    private String errorMessage;
    private Timestamp datetimelog;
    private String errorVehicle;
    private Integer id;
    public ErrorLog(){

    }
    public ErrorLog(Integer id,String errorCode, String errorMessage, Timestamp datetimelog, String errorVehicle) {
        this.id = id;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.datetimelog = datetimelog;
        this.errorVehicle = errorVehicle;
    }

    public Integer getId() { return id;}

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Timestamp getDatetimelog() {
        return datetimelog;
    }

    public void setDatetimelog(Timestamp datetimelog) {
        this.datetimelog = datetimelog;
    }

    public String getErrorVehicle() {
        return errorVehicle;
    }

    public void setErrorVehicle(String errorVehicle) {
        this.errorVehicle = errorVehicle;
    }
}
