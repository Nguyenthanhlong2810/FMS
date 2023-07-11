package org.opentcs.database.logging.logentity;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Date;

public class Logs {
    private String method;
    private String level;
    private String content;
    private Timestamp logTime;


    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTime() throws IOException {
        String TIME_SERVER = "0.vn.pool.ntp.org"; //NTP server in VietNam's NTPServerPool :)
                                                    //see more https://www.ntppool.org/zone/vn
        //TIME_SERVER  = "ntp.xs4all.nl";
        //TIME_SERVER = "pool.ntp.org";
        NTPUDPClient timeClient = new NTPUDPClient();
        timeClient.open();
        timeClient.setDefaultTimeout(2000); //timeout if connect response take more than 2second
        InetAddress inetAddress = InetAddress.getByName(TIME_SERVER); //this gonna get ip address depend on da Server's name
        TimeInfo timeInfo = timeClient.getTime(inetAddress); // :D ?
        timeInfo.computeDetails(); //Compute and validate details of the NTP message packet if  not already done
        //after calling timeInfo.computeDetails() it's possible to get the offset by timeInfo.getOffset(). this returns the offset of the local time in millisecond
        long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime(); //get message from NTP by milly second
        java.util.Date date = new Date(returnTime);
//        Timestamp logTime  = new Timestamp(date.getTime());
//        System.out.println(date.getTime());
//        System.out.println(logTime);
        this.logTime  = new Timestamp(date.getTime());
        timeClient.close();
        return logTime;
    }


    public String getIP() {
        String ip = "";
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("1.2.3.4"), 12345);
             ip = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) { e.printStackTrace(); }
        return ip;
    }

    public enum LEVEL{
        INFO,
        DEBUG,
        ERROR,
        WARN,
        CRITICAL
    }

}
