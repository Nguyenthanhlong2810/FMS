package org.opentcs.database.entity;

import java.sql.Timestamp;

public class VehicleActiveLog {
    private String vehicleName;
    private Timestamp startTime;
    private Timestamp stopTime;
    private float voltage;
    private float current;
    private String runTime;
    private int startEnergy;
    private int stopEnergy;

    private float  totalActiveTime;

    public VehicleActiveLog(String vehicleName, Timestamp startTime, Timestamp stopTime, float voltage, float current, String runTime, int startEnergy,int stopEnergy) {
        this.vehicleName = vehicleName;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.voltage = voltage;
        this.current = current;
        this.runTime = runTime;
        this.startEnergy = startEnergy;
        this.stopEnergy  = stopEnergy;
    }

    public VehicleActiveLog(String vehicleName, float totalActiveTime) {
        this.vehicleName = vehicleName;
        this.totalActiveTime = totalActiveTime;
    }

    public int getStopEnergy() {
        return stopEnergy;
    }

    public void setStopEnergy(int stopEnergy) {
        this.stopEnergy = stopEnergy;
    }

    public float getTotalActiveTime() {
        return totalActiveTime;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getStopTime() {
        return stopTime;
    }

    public void setStopTime(Timestamp stopTime) {
        this.stopTime = stopTime;
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

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public int getStartEnergy() {
        return startEnergy;
    }

    public void setStartEnergy(int startEnergy) {
        this.startEnergy = startEnergy;
    }
}
