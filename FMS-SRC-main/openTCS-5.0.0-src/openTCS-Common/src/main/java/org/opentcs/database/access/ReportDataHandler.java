package org.opentcs.database.access;

import org.opentcs.database.ConnectionPoolCreator;
import org.opentcs.database.entity.*;
import org.opentcs.database.entity.errorchart.ErrorChartObject;
import org.opentcs.database.entity.errorchart.VehicleErrorCount;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;


public class ReportDataHandler {
    private static ReportDataHandler instance = null;
    private ReportDataHandler(){ }
    public static ReportDataHandler getInstance(){
        if (instance==null){
            instance = new ReportDataHandler();
        }
        return  instance;
    }

    public ArrayList<ErrorCount> getErrorLogGroupByError(Date dateStart, Date dateEnd) {
        Connection conn = ConnectionPoolCreator.getConnection();
        ArrayList<ErrorCount> errorLogs = new ArrayList<>();
        try {
            String sql = "SELECT error_code, error_message, COUNT(error_message) as error_count " +
                    "FROM public.tbl_errorlog " +
                    "WHERE datetimelog BETWEEN ? AND ?" +
                    "GROUP BY error_code, error_message";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setDate(1, new Date(dateStart.getTime()));
            ps.setDate(2, new Date(dateEnd.getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ErrorCount errorCount = new ErrorCount();
                errorCount.setErrorCode(rs.getString("error_code"));
                errorCount.setErrorMessage(rs.getString("error_message"));
                errorCount.setCount(rs.getInt("error_count"));

                errorLogs.add(errorCount);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPoolCreator.releaseConnection(conn);
        }
        return errorLogs;
    }

    public ArrayList<ErrorCount> getErrorLogGroupByVehicle(Date dateStart, Date dateEnd) {
        Connection conn = ConnectionPoolCreator.getConnection();
        ArrayList<ErrorCount> errorLogs = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT error_vehicle, COUNT(error_vehicle) as error_count " +
                    "FROM public.tbl_errorlog " +
                    "WHERE datetimelog BETWEEN ? AND ?" +
                    "GROUP BY error_vehicle ");
            ps.setDate(1, new Date(dateStart.getTime()));
            ps.setDate(2, new Date(dateEnd.getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ErrorCount errorCount = new ErrorCount();
                errorCount.setErrorVehicle(rs.getString("error_vehicle"));
                errorCount.setCount(rs.getInt("error_count"));

                errorLogs.add(errorCount);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPoolCreator.releaseConnection(conn);
        }
        return errorLogs;
    }

    /**
     * @param dateStart dateStart
     * @param dateEnd   dateEnd
     * @return
     */
    public int getErrorCount(Date dateStart, Date dateEnd) {
        Connection conn = ConnectionPoolCreator.getConnection();
        int count = 0;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(id) as error_count " +
                    "FROM public.tbl_errorlog " +
                    "WHERE datetimelog BETWEEN ? AND ?");
            ps.setDate(1, new Date(dateStart.getTime()));
            ps.setDate(2, new Date(dateEnd.getTime()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("error_count");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPoolCreator.releaseConnection(conn);
        }
        return count;
    }

    /**
     *
     * @param dateStart dateStart
     * @param dateEnd dateEnd
     * @return
     */
    public  ArrayList<ErrorChartObject> getErrorList(Date dateStart,Date dateEnd){
        ArrayList<ErrorChartObject> list = new ArrayList<>();
        try {
            Connection conn = ConnectionPoolCreator.getConnection();
            String sql = "SELECT error_vehicle,count(error_message) \n" +
                    "FROM tbl_errorlog \n" +
                    "where datetimelog between ? and ?\n" +
                    "group by error_vehicle\n";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, dateStart);
            ps.setDate(2, dateEnd);
            ResultSet rs = ps.executeQuery();
            ErrorChartObject errorObject ;
            while(rs.next()){
                errorObject = new ErrorChartObject(rs.getString(1),rs.getInt(2));
                list.add(errorObject);
            }
            ConnectionPoolCreator.releaseConnection(conn);
            ps.close();
            rs.close();
        }catch (SQLException e){e.printStackTrace();}
        return list;
    }
    /**
     *
     * @param dateStart thick thighs save lives
     * @param dateEnd thick thighs save lives
     * @return  List of  TimeActivity
     */
    public ArrayList<TimeActivity> getTimeActivityList(Date dateStart, Date dateEnd){
        ArrayList<TimeActivity> list = new ArrayList<>();
        try {
            Connection conn = ConnectionPoolCreator.getConnection();
            String sql = "SELECT vehicle_process,\n" +
                    " ((sum(EXTRACT(EPOCH FROM (finish_time - create_time)::INTERVAL))::numeric(16,2))/3600)::NUMERIC(15,2) as totalactivity,\n" +
                    "(EXTRACT(EPOCH FROM ( ?::timestamp - ?::timestamp  )::INTERVAL)/3600)::NUMERIC(15,2) as alltime,\n" +
                    "((EXTRACT(EPOCH FROM ( ?::timestamp  - ?::timestamp  )::INTERVAL) - sum(EXTRACT(EPOCH FROM (finish_time - create_time)::INTERVAL))::numeric(16,2))/3600)::NUMERIC(15,2) as noactivity \n" +
                    "FROM tbl_transportorder " +
                    "WHERE create_time BETWEEN ? AND ? " +
                    "GROUP BY vehicle_process,alltime ;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, dateEnd);
            ps.setDate(2, dateStart);
            ps.setDate(3, dateEnd);
            ps.setDate(4, dateStart);
            ps.setDate(5, dateStart);
            ps.setDate(6, dateEnd);
            ResultSet rs = ps.executeQuery();
            TimeActivity time_activity_idle;
            while (rs.next()){
                time_activity_idle = new TimeActivity(rs.getString("vehicle_process"),
                        rs.getDouble("totalactivity"),
                        rs.getDouble("alltime"),
                        rs.getDouble("noactivity"));
                list.add(time_activity_idle);
            }
            rs.close();
            ps.close();
            ConnectionPoolCreator.releaseConnection(conn);
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * @param dateStart thick thighs save lives
     * @param dateEnd thick thighs save lives
     * @return list of vehicle with it's total distance between dateStart and dateEnd
     */
    public ArrayList<VehicleTotalDistance> getTotalDistance(Date dateStart, Date dateEnd){
        ArrayList<VehicleTotalDistance> arrayList = new ArrayList<>();
        try {
            Connection conn = ConnectionPoolCreator.getConnection();
            String sql = "SELECT vehicle_process, SUM(distance) AS Total_distance\n" +
                    "FROM tbl_transportorder\n" +
                    "WHERE create_time between ? AND ? \n" +
                    "GROUP BY vehicle_process";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, dateStart);
            ps.setDate(2, dateEnd);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                VehicleTotalDistance vehicleTotalDistance = new VehicleTotalDistance(rs.getString("vehicle_process"),rs.getDouble("Total_distance"));
                arrayList.add(vehicleTotalDistance);
            }
            rs.close();
            ps.close();
            ConnectionPoolCreator.releaseConnection(conn);
            return arrayList;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    /**
     *  GET ErrorLog List for table Error in ErrorPanel
     * @param dateStat
     * @param dateEnd
     * @return
     */
    public ArrayList<ErrorLog> getErrorLogList(Date dateStat, Date dateEnd) {
        ArrayList<ErrorLog> arrayList = new ArrayList<>();
        try {
            Connection conn = ConnectionPoolCreator.getConnection();
            String sql = "SELECT * FROM tbl_errorlog WHERE datetimelog BETWEEN ? AND ? ORDER BY id DESC ";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1, dateStat);
            ps.setDate(2, dateEnd);
            ResultSet rs = ps.executeQuery();
            ErrorLog errorLog;
            while (rs.next()) {
                 errorLog = new ErrorLog(rs.getInt("id"),
                                         rs.getString("error_code"),
                                         rs.getString("error_message"),
                                         rs.getTimestamp("datetimelog"),
                                         rs.getString("error_vehicle"));
                 arrayList.add(errorLog);
            }
            rs.close();
            ps.close();
            ConnectionPoolCreator.releaseConnection(conn);
            return arrayList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     *
     * @param dateStart dateStart
     * @param dateEnd dateEnd
     * @return Activities List for table Activities in ActivitiesPanel
     */
    public ArrayList<TransPortOrder> getActivitiesList(Date dateStart, Date dateEnd) {
        ArrayList<TransPortOrder> arrayList = new ArrayList<>();
        try {
            Connection conn = ConnectionPoolCreator.getConnection();
            String sql = "SELECT * FROM tbl_transportorder WHERE create_time BETWEEN ? AND ? ORDER BY to_id ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1,dateStart);
            ps.setDate(2,dateEnd);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TransPortOrder activitiesRp = new TransPortOrder();
                activitiesRp.setToName(rs.getString("to_name"));
                activitiesRp.setCreateTime(rs.getTimestamp("create_time"));
                activitiesRp.setAssignedTime(rs.getTimestamp("assigned_time"));
                activitiesRp.setVehicle(rs.getString("vehicle_process"));
                activitiesRp.setFinishedTime(rs.getTimestamp("finish_time"));
                activitiesRp.setSuccess(rs.getBoolean("success"));
                activitiesRp.setCrossDeadLine(rs.getBoolean("crossdeadline"));
                activitiesRp.setDistance(rs.getDouble("distance"));
                arrayList.add(activitiesRp);
            }
            rs.close();
            return arrayList;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
    /**
     *
     * @param dateStart  dateStart
     * @param dateEnd dateEnd
     * @param state Vehicle's State
     * @return Activities List for table VehicleLog in VehiclePanel
     */
    public ArrayList<VehicleRunLog> getVehicleListWithState(Date dateStart, Date dateEnd, String state) {
        ArrayList<VehicleRunLog> arrayList = new ArrayList<>();
        try {
            Connection conn = ConnectionPoolCreator.getConnection();
            ResultSet rs = null;
            PreparedStatement ps =null;
            if(state.equals("*--ALL--*")){
                String sql = "select * from tbl_vehicle where datetimelog between ? and ? ;";
                ps= conn.prepareStatement(sql);
                ps.setDate(1, dateStart);
                ps.setDate(2, dateEnd);
                ConnectionPoolCreator.releaseConnection(conn);
                rs = ps.executeQuery();
            }else {
                String sql = "select * from tbl_vehicle where datetimelog between ? and ?  AND status = ? ;";
                ps = conn.prepareStatement(sql);
                ps.setDate(1, dateStart);
                ps.setDate(2, dateEnd);
                ps.setString(3,state);
                rs = ps.executeQuery();
            }
            VehicleRunLog vehicleRunLog;
            while (rs.next()) {
                 vehicleRunLog = new VehicleRunLog(rs.getInt("id"),
                                            rs.getString("vehicle_name"),
                                            rs.getString("status"),
                                            rs.getTimestamp("datetimelog"),
                                            rs.getString("energy_level"),
                                            rs.getFloat("voltage"),
                                            rs.getFloat("current"));
                arrayList.add(vehicleRunLog);
            }
            ps.close();
            rs.close();
            ConnectionPoolCreator.releaseConnection(conn);
            return arrayList;
        } catch (SQLException e) {e.printStackTrace(); }
        return null;
    }


    /**
     * Function get List Of Error's Name for class VehicleErrorCount
     */
    public ArrayList<String> getErrorMessageList(Date dateStart,Date dateEnd){
        ArrayList<String> errorNameList = new ArrayList<>();
        try {
            String sql = "select distinct error_message " +
                         "from tbl_errorlog " +
                         "where (datetimelog between ? and ?) order by 1";
            Connection conn = ConnectionPoolCreator.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1,dateStart);
            ps.setDate(2,dateEnd);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                errorNameList.add(rs.getString(1));
            }
            ConnectionPoolCreator.releaseConnection(conn);
            ps.close();
            rs.close();
        }catch (Exception e){e.printStackTrace();}
        return  errorNameList;
    }

    public ArrayList<VehicleErrorCount> getErrorCountList(Date dateStart, Date dateEnd){
        //create Sql with StringBuilder
        VehicleErrorCount vehicleErrorCount = new VehicleErrorCount(dateStart,dateEnd);
        ArrayList<VehicleErrorCount> list = new ArrayList<>();
        if (vehicleErrorCount.getErrorCodeList().size() > 0)
        {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT \n" +
                    "error_vehicle as VehicleName\n");
            for (int i = 0; i < vehicleErrorCount.getErrorCodeList().size(); i++) {
                String errorName = vehicleErrorCount.getErrorCodeList().get(i);
                errorName = errorName.replaceAll(" ", "_");
                sql.append(", " + errorName + "::integer as " + errorName + "\n");
            }
            sql.append("FROM \n");
            sql.append("crosstab($$\n");
            sql.append("SELECT error_vehicle::text, error_message, countt::int \n");
            sql.append("FROM (SELECT error_vehicle,error_message,count(error_message) as countt\n");
            sql.append("FROM tbl_errorlog \n");
            sql.append("WHERE (datetimelog between '" + dateStart + "' and '" + dateEnd + "')\n");
            sql.append("GROUP BY error_vehicle,error_message\n");
            sql.append("ORDER BY 1,2) as tbl\n");
            sql.append("$$,\n");
            sql.append("$$ VALUES");
            for (int i = 0; i < vehicleErrorCount.getErrorCodeList().size(); i++) {
                String errorName = vehicleErrorCount.getErrorCodeList().get(i);
                if (i == 0) {
                    sql.append("('" + errorName + "')");
                } else {
                    sql.append(",('" + errorName + "')");
                }
            }
            sql.append("$$)\n");
            sql.append("AS ct(\"error_vehicle\" text");
            for (int i = 0; i < vehicleErrorCount.getErrorCodeList().size(); i++) {
                String errorName = vehicleErrorCount.getErrorCodeList().get(i);
                errorName = errorName.replaceAll(" ", "_");
                sql.append(",\"" + errorName.toLowerCase() + "\" int");
            }
            sql.append(")");

            try {
                PreparedStatement ps;
                Connection conn = ConnectionPoolCreator.getConnection();
                String sqlCreateTableFunc = "CREATE EXTENSION  if not Exists tablefunc";
                ps = conn.prepareStatement(sqlCreateTableFunc);
                ps.executeUpdate();
                ps = conn.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    vehicleErrorCount = new VehicleErrorCount(rs.getString(1), dateStart, dateEnd);
                    int[] count = {};
                    int max = vehicleErrorCount.getErrorCodeList().size();
                    for (int i = 0; i < max; i++) {
                        count = addElement(count, rs.getString(i + 2) == null ? 0 : rs.getInt(i + 2));
                    }
                    vehicleErrorCount.setCount(count);
                    list.add(vehicleErrorCount);
                }

                ps.close();
                rs.close();
                ConnectionPoolCreator.releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     *
     * @param intArray Array of int
     * @param element element
     * Add an element into a Array of int
     */
     private static int[] addElement(int[] intArray, int element) {
        intArray  = Arrays.copyOf(intArray, intArray.length + 1);
        intArray[intArray.length - 1] = element;
        return intArray;
    }

    public  ArrayList<VehicleActiveLog> getVehicleActiveLog(Date dateStart,Date dateEnd){
         ArrayList<VehicleActiveLog> list = new ArrayList<>();
        try{
            String sql = "SELECT id,vehicle_name, start_time, stop_time, voltage, current, to_char((stop_time -start_time),' DD \"day(s)\" HH24h MIm SSs') as run_time , start_energy, stop_energy\n" +
                    "\tFROM public.tbl_vehiclelog\n" +
                    "\tWhere start_time between ? and ? and stop_time is not null\n" +
                    "\torder by id desc ";
            Connection conn = ConnectionPoolCreator.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1,dateStart);
            ps.setDate(2,dateEnd);
            ResultSet rs = ps.executeQuery();
            VehicleActiveLog vehicleActiveLog ;
            while(rs.next()){
                vehicleActiveLog = new VehicleActiveLog(rs.getString("vehicle_name"),
                                                        rs.getTimestamp("start_time"),
                        rs.getTimestamp("stop_time"),
                        rs.getFloat("voltage"),
                        rs.getFloat("current"),
                        rs.getString("run_time"),
                        rs.getInt("start_energy"),
                        rs.getInt("stop_energy"));
                list.add(vehicleActiveLog);
            }
            ps.close();
            rs.close();
            ConnectionPoolCreator.releaseConnection(conn);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return  list;
    }
    public  ArrayList<VehicleActiveLog> getVehicleActiveLogsForChart(Date dateStart,Date dateEnd){
        ArrayList<VehicleActiveLog> list = new ArrayList<>();
        try{
            String sql = "SELECT vehicle_name,\n" +
                    "((sum(EXTRACT(EPOCH  FROM (stop_time - start_time))))/3600)::NUMERIC(20,3) as total_run_time\n" +
                    "\tFROM public.tbl_vehiclelog\n" +
                    "\tWhere start_time between ? and ? and stop_time is not null\n" +
                    "\tgroup by vehicle_name;";
            Connection conn = ConnectionPoolCreator.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDate(1,dateStart);
            ps.setDate(2,dateEnd);
            ResultSet rs = ps.executeQuery();
            VehicleActiveLog vehicleActiveLog ;
            while(rs.next()){
                vehicleActiveLog = new VehicleActiveLog(rs.getString("vehicle_name"),
                        rs.getFloat("total_run_time"));
                list.add(vehicleActiveLog);
            }
            ps.close();
            rs.close();
            ConnectionPoolCreator.releaseConnection(conn);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return  list;
    }

}
