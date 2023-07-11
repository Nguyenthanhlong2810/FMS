package org.opentcs.kernel.extensions.statistics;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.opentcs.database.ConnectionPoolCreator;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.statistics.StatisticsEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class PsqlLogging {
    String sql;
    PreparedStatement ps;
    //with Tbl_transportorder
    public void insertTO(  TransportOrder ordernaw) throws SQLException {
            Connection conn = ConnectionPoolCreator.getConnection();
//            Timestamp time = getTime();
            sql = "INSERT INTO public.tbl_transportorder( to_name, create_time)VALUES (?, localtimestamp);";
            ps = conn.prepareStatement(sql);
            ps.setString(1, ordernaw.getName());
//            ps.setTimestamp(2, time);
            ps.executeUpdate();
            ps.close();
            ConnectionPoolCreator.releaseConnection(conn);
    }
    public void updateTOassign( TransportOrder ordernaw) throws SQLException {
        Connection conn = ConnectionPoolCreator.getConnection();
//        Timestamp time = getTime();
        sql = "UPDATE public.tbl_transportorder SET assigned_time= localtimestamp WHERE to_name = ?;";
        ps = conn.prepareStatement(sql);
//        ps.setTimestamp(1,time);
        ps.setString(1, ordernaw.getName());
        ps.executeUpdate();
        ps.close();
        ConnectionPoolCreator.releaseConnection(conn);
    }
    public void updateTOfinish( TransportOrder ordernaw, boolean success) throws SQLException {
        Connection conn = ConnectionPoolCreator.getConnection();
//        Timestamp time = getTime();
        long distance = 0;
        for (DriveOrder dro: ordernaw.getAllDriveOrders()) { distance += dro.getRoute().getCosts();}
        sql = "UPDATE public.tbl_transportorder \n" +
                "\tSET vehicle_process = ?,finish_time = localtimestamp,success = ?, crossdeadline = ? , distance = ? \n" +
                "\tWHERE to_name = ?;";
        ps = conn.prepareStatement(sql);
        ps.setString(1, ordernaw.getProcessingVehicle().getName());
//        ps.setTimestamp(2,time);
        ps.setBoolean(2,success);
        ps.setBoolean(3,false);
        ps.setLong(4, distance);
        ps.setString(5,ordernaw.getName());
        ps.executeUpdate();
        ps.close();
        ConnectionPoolCreator.releaseConnection(conn);
    }
    public void updateTOcrossdeadline( TransportOrder ordernaw)throws SQLException{
        Connection conn = ConnectionPoolCreator.getConnection();
        sql = "UPDATE public.tbl_transportorder\n" +
                "\tSET crossdeadline = ? \n" +
                "\tWHERE to_name = ?;";
        ps = conn.prepareStatement(sql);
        ps.setBoolean(1,true);
        ps.setString(2,ordernaw.getName());
        ps.executeUpdate();
        ps.close();
        ConnectionPoolCreator.releaseConnection(conn);
    }

    //with tbl_driveorders
    private int getTransportOrderID(String transportOrderName) throws SQLException {  //layTO-id
        Connection conn = ConnectionPoolCreator.getConnection();
            int to_id = 0;
            String sql = "select to_id from tbl_transportorder where to_name = ?";
            PreparedStatement ps1 = conn.prepareStatement(sql);
            ps1.setString(1,transportOrderName);
            ResultSet rs = ps1.executeQuery();
            while (rs.next()){
                to_id = rs.getInt(1);
            }
            ps1.close();
            ConnectionPoolCreator.releaseConnection(conn);
        return to_id;
    }

    public void insertDRO( TransportOrder ordernaw) throws SQLException {
        int  to_id = getTransportOrderID(ordernaw.getName());
         List<DriveOrder> listDRO = ordernaw.getAllDriveOrders();
        if (listDRO.size()>=1) {
            for (int i = 0; i < listDRO.size(); i++) {
                    sql = "INSERT INTO public.tbl_driveorders(\n" +
                            "\t to_id, point_start, point_end, action, status)\n" +
                            "\tVALUES ( ?, ?, ?, ?, ?);";
                    Connection conn = ConnectionPoolCreator.getConnection();
                    ps = conn.prepareStatement(sql);
                    ps.setInt(1, to_id); //to_id
                     if (i == 0){ps.setString(2,ordernaw.getStartPosition());}
                     else { ps.setString(2,listDRO.get(i-1).getRoute().getFinalDestinationPoint().getName()); }
                    ps.setString(3,listDRO.get(i).getRoute().getFinalDestinationPoint().getName());//point end
                    ps.setString(4,listDRO.get(i).getDestination().getOperation());
                    if (listDRO.get(i).getState().equals(DriveOrder.State.FINISHED))
                         {ps.setString(5,"Finished");}
                    else {ps.setString(5,"Failed");}
                    ps.executeUpdate();
                    ps.close();
                    ConnectionPoolCreator.releaseConnection(conn);
            }
        }
    }

    //with tbl_vehicles
    public void  vehicleInsert(Vehicle vehiclenaw , StatisticsEvent event) throws SQLException {
        Connection conn = ConnectionPoolCreator.getConnection();
//        Timestamp time = getTime();
        sql = "INSERT INTO public.tbl_vehicle(\n" +
                "  vehicle_name, status, datetimelog, energy_level, voltage, current)\n" +
                "\tVALUES ( ?, ?, localtimestamp, ?, ?, ?);";
        ps = conn.prepareStatement(sql);
        ps.setString(1,vehiclenaw.getName());
        ps.setString(2,event.name());
//        ps.setTimestamp(3,time);
        ps.setInt(3, vehiclenaw.getEnergyLevel());
        ps.setFloat(4,vehiclenaw.getVoltage());
        ps.setFloat(5,vehiclenaw.getCurrent());
        ps.executeUpdate();
        ps.close();
        ConnectionPoolCreator.releaseConnection(conn);
    }
    private Timestamp getTime() {
        Timestamp time =  new Timestamp(System.currentTimeMillis());
        NTPUDPClient timeClient = new NTPUDPClient();
        try {
            String TIME_SERVER = "0.vn.pool.ntp.org"; //NTP server in VietNam's NTPServerPool :)
            //see more https://www.ntppool.org/zone/vn
            //TIME_SERVER  = "ntp.xs4all.nl";
            //TIME_SERVER = "pool.ntp.org";
            timeClient.open();
            timeClient.setDefaultTimeout(2000); //timeout if connect response take more than 2 second
            InetAddress inetAddress = InetAddress.getByName(TIME_SERVER); //this gonna get ip address depend on da Server's name
            TimeInfo timeInfo = timeClient.getTime(inetAddress); // :D ?
            timeInfo.computeDetails(); //Compute and validate details of the NTP message packet if  not already done
            //after calling timeInfo.computeDetails() it's possible to get the offset by timeInfo.getOffset(). this returns the offset of the local time in millisecond
            long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime(); //get message from NTP by milly second
            java.util.Date date = new Date(returnTime);
            time = new Timestamp(date.getTime());
        }catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (timeClient.isOpen()) {
                timeClient.close();
            }
        }
        return time;
    }
    public void insertErrorlog(Vehicle vehiclenaw, Vehicle vehicleold)  {
        try {

            HashMap<String, String> errorsList = getNewErrors(vehiclenaw.getErrorMessages(), vehicleold.getErrorMessages());
            if (errorsList.isEmpty()) {
                return;
            }
            String sql = "INSERT INTO public.tbl_errorlog(\n" +
                    "\t error_code, error_message, datetimelog, error_vehicle)\n" +
                    "\tVALUES (?, ?, localtimestamp, ?);";
//            Timestamp time = getTime();
            Connection conn = ConnectionPoolCreator.getConnection();
            ps = conn.prepareStatement(sql);
            for (Map.Entry error : errorsList.entrySet()) {
                ps.setString(1, (String) error.getKey());
                ps.setString(2, (String) error.getValue());
//                ps.setTimestamp(3, time);
                ps.setString(3, vehiclenaw.getName());
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
            ConnectionPoolCreator.releaseConnection(conn);
        }catch (SQLException ex){ex.printStackTrace();}
    }

    private HashMap<String, String> getNewErrors(HashMap<String, String> newList, HashMap<String, String> oldList) {
        HashMap<String, String> diff = (HashMap<String, String>) newList.clone();
        for (String item : oldList.keySet()) {
            diff.remove(item);
        }
        return diff;
    }

    public void insertVehicleLogStart(Vehicle vehiclenaw){
        try{
            String sql = "INSERT INTO tbl_vehiclelog( vehicle_name, start_time,voltage, current, start_energy)\n" +
                    "\tVALUES ( ?, localtimestamp, ?, ?, ?);";
            Connection conn = ConnectionPoolCreator.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1,vehiclenaw.getName());
            ps.setFloat(2,vehiclenaw.getVoltage());
            ps.setFloat(3,vehiclenaw.getCurrent());
            ps.setInt(4,vehiclenaw.getEnergyLevel());
            ps.executeUpdate();
            ps.close();
            ConnectionPoolCreator.releaseConnection(conn);
        }catch (SQLException e) {  e.printStackTrace();  }
    }
    public void updateVehicleLogStop(Vehicle vehiclenaw){
        try
        {
            String sql = "UPDATE public.tbl_vehiclelog\n" +
                    "\tSET  stop_time = localtimestamp , stop_energy = ?, voltage = ?, current = ?\n" +
                    "\tWHERE id = (select id from tbl_vehiclelog where vehicle_name = ? order by id desc limit 1)";
            Connection conn = ConnectionPoolCreator.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1,vehiclenaw.getEnergyLevel());
            ps.setFloat(2,vehiclenaw.getVoltage());
            ps.setFloat(3,vehiclenaw.getCurrent());
            ps.setString(4,vehiclenaw.getName());
            ps.executeUpdate();
            ps.close();
            ConnectionPoolCreator.releaseConnection(conn);
        }catch (SQLException e){e.printStackTrace(); }
    }
}
