package org.opentcs.database.entity;


import java.sql.Timestamp;

public class TransPortOrder {
    /**
     * TransportOrder's Name
     */
    private String toName;
    /**
     * TransportOrder's CreateTime
     */
    private Timestamp createTime;
    /**
     * TransportOrder's AssignTime
     */
    private Timestamp assignedTime;
    /**
     * Vehicle that processing the TransportOrder
     */
    private String vehicle;
    /**
     * TransportOrder's finishedTime
     */
    private Timestamp finishedTime;
    /**
     * Provide boolean value to know if transportOrder is success or not
     */
    private boolean success;
    /**
     * Provide boolean value to know if transportOrder is Cross Deadline or not
     */
    private boolean crossDeadLine;
    /**
     * TransportOder's Distance
     */
    private double distance;

    /**
     * Constructor
     */
    public TransPortOrder(){}
    public TransPortOrder(String toName, Timestamp createTime, Timestamp assignedTime, String vehicle,
                          Timestamp finishedTime, boolean success, boolean crossdeadline, double distance) {
        this.toName = toName;
        this.createTime = createTime;
        this.assignedTime = assignedTime;
        this.vehicle = vehicle;
        this.finishedTime = finishedTime;
        this.success = success;
        this.crossDeadLine = crossdeadline;
        this.distance = distance;
    }

    /**
     * Getter and Setter
     */
    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getAssignedTime() {
        return assignedTime;
    }

    public void setAssignedTime(Timestamp assignedTime) {
        this.assignedTime = assignedTime;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public Timestamp getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(Timestamp finishedTime) {
        this.finishedTime = finishedTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isCrossDeadLine() {
        return crossDeadLine;
    }

    public void setCrossDeadLine(boolean crossDeadLine) {
        this.crossDeadLine = crossDeadLine;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
