package org.opentcs.database.entity.errorchart;


//Object for errorReportTable
public class ErrorChartObject {
    String vehicleName;
    Integer numberOfError;

    public ErrorChartObject(String vehicleName, Integer numberOfError) {
        this.vehicleName = vehicleName;
        this.numberOfError = numberOfError;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public Integer getNumberOfError() {
        return numberOfError;
    }

    public void setNumberOfError(Integer numberOfError) {
        this.numberOfError = numberOfError;
    }
}
