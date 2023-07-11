package org.opentcs.database.entity;

public class VehicleTotalDistance {
    private String vehicleProcess;
    private double totalDistance;

    /**
     * constructor of class
     * @param vehicleProcess
     * @param totalDistance
     */
    public VehicleTotalDistance(String vehicleProcess, double totalDistance) {
        this.vehicleProcess = vehicleProcess;
        this.totalDistance = totalDistance;
    }

    /**
     * getter and setter
     * @return
     */
    public String getVehicleProcess() {
        return vehicleProcess;
    }

    public void setVehicleProcess(String vehicleProcess) {
        this.vehicleProcess = vehicleProcess;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }
}
