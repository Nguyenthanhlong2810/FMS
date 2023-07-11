package org.opentcs.database.entity;

public class TimeActivity {
    private String vehicle_process;
    private double totalactivity;
    private double alltime;
    private double noActivity;

    /**
     * Constructor
     * @param vehicle_process
     * @param totalactivity
     * @param alltime
     * @param noActivity
     */
    public TimeActivity(String vehicle_process, double totalactivity, double alltime, double noActivity) {
        this.vehicle_process = vehicle_process;
        this.totalactivity = totalactivity;
        this.alltime = alltime;
        this.noActivity = noActivity;
    }

    /**
     * Getter and Setter of class
     */

    public TimeActivity(){}
    public String getVehicle_process() {
        return vehicle_process;
    }

    public void setVehicle_process(String vehicle_process) {
        this.vehicle_process = vehicle_process;
    }

    public double getTotalactivity() {
        return totalactivity;
    }

    public void setTotalactivity(double totalactivity) {
        this.totalactivity = totalactivity;
    }

    public double getAlltime() {
        return alltime;
    }

    public void setAlltime(double alltime) {
        this.alltime = alltime;
    }

    public double getNoActivity() {
        return noActivity;
    }

    public void setNoActivity(double noActivity) {
        this.noActivity = noActivity;
    }
}
