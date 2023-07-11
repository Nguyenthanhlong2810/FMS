package org.opentcs.database.entity;

import java.sql.Timestamp;

public class VehicleRunLog {
    private int id;
    private String vehicleName;
    private String status;
    private Timestamp dateTimeLog;
    private String energyLevel;
    private float voltage;
    private float current;
    /*
        Constructor
     */
    public VehicleRunLog(){ }
    public VehicleRunLog(int id, String vehicleName, String status, Timestamp datetimelog, String energyLevel, float voltage, float curent) {
        this.id = id;
        this.vehicleName = vehicleName;
        this.status = status;
        this.dateTimeLog = datetimelog;
        this.energyLevel = energyLevel;
        this.voltage = voltage;
        this.current = curent;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getDateTimeLog() {
        return dateTimeLog;
    }

    public void setDateTimeLog(Timestamp dateTimeLog) {
        this.dateTimeLog = dateTimeLog;
    }

    public String getEnergyLevel() {
        return energyLevel;
    }

    public void setEnergyLevel(String energyLevel) {
        this.energyLevel = energyLevel;
    }

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
    }
}
