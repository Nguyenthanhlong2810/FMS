package org.opentcs.database.entity.errorchart;

import org.opentcs.database.access.ReportDataHandler;

import java.sql.Date;
import java.util.ArrayList;

/**
 * Vehicle's name with number of Errors Count for Each Error
 */
public class VehicleErrorCount {
    /**
     * List of Error Name
     */
    private ArrayList<String> errorCodeList;
    /**
     * This Vehicle's name
     */
    private String vehicleName;
    /**
     * count stored the number of errors corresponding to the errorCodeList above
     */
    private int[] count;

    /**
     * Constructor
     * @param dateStart parameters to query the database
     * @param dateEnd parameters to query the database
     */
    public  VehicleErrorCount(Date dateStart, Date dateEnd){
        this.errorCodeList = ReportDataHandler.getInstance().getErrorMessageList(dateStart,dateEnd);
    }

    /**
     *
     * @param vehicleName this Vehicle's Name
     * @param dateStart parameters to query the database
     * @param dateEnd parameters to query the database
     */
    public VehicleErrorCount(String vehicleName,Date dateStart, Date dateEnd) {
        this.vehicleName = vehicleName;
        this.errorCodeList = ReportDataHandler.getInstance().getErrorMessageList(dateStart,dateEnd);
    }

    public VehicleErrorCount(String vehicleName, int[] count,Date dateStart, Date dateEnd) {
        this.vehicleName = vehicleName;
        this.count = count;
        this.errorCodeList = ReportDataHandler.getInstance().getErrorMessageList(dateStart,dateEnd);
    }


    /*
        Getter and Setter
     */
    public ArrayList<String> getErrorCodeList() {
        return errorCodeList;
    }

    public void setErrorCodeList(ArrayList<String> errorCodeList) {
        this.errorCodeList = errorCodeList;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public int[] getCount() {
        return count;
    }

    public void setCount(int[] count) {
        this.count = count;
    }
}
