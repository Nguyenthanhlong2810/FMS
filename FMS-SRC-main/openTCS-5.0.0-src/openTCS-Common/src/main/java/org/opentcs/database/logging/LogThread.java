package org.opentcs.database.logging;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentcs.database.ConnectionPoolCreator;
import org.opentcs.database.logging.logentity.Logs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

//Insert data into database
public class LogThread extends Task<Logs> {
    private final Logger logger = LogManager.getLogger(LogThread.class);

    @Override
    public void setItems(List<Logs> items) {
        super.setItems(items);
    }

    @Override
    public Integer call() throws Exception {
        List<Logs> lstLog = getItems(); //return list items trong Task
        try {
            if (lstLog != null && !lstLog.isEmpty()) {
                save(lstLog);
            }
        } catch (Exception e) {
            logger.error(e.toString(), e);
            return 0;
        }
        return 1;
    }

    //insert logs into database
    public void save(List<Logs> list) {
        Connection con = null;
        try {
            con = ConnectionPoolCreator.getConnection();
            String sql = "INSERT INTO public.tbl_log(\n" +
                   "\tmethod_name, log_level, log_content, log_time, log_ip)\n" +
                   "\tVALUES (?, ?, ?, ?, ?);";
            PreparedStatement ps = con.prepareStatement(sql);
            for(Logs log : list){
                ps.setString(1, log.getMethod());
                ps.setString(2, log.getLevel());
                ps.setString(3,log.getContent());
                ps.setTimestamp(4,log.getTime());
                ps.setString(5,log.getIP());
                ps.execute();
            }
            ps.close();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (con != null) ConnectionPoolCreator.releaseConnection(con);
        }
    }
}
