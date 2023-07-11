package org.opentcs.database;

import java.sql.Connection;

public class ConnectionPoolCreator {
    /**
     * Port,Username and Password for Postgresql
     */
    public static String PORT  ;
    public  static String USER ;
    public static  String PASS ;
    public static String IP ;
    public static String DB_NAME;
    /**
     * Connect String
     */
    public static String URL = null ;
    private static PostgresqlPool pool ;
    public static void setProperties(String port, String ipaddress, String user, String password,String db_name){
        if ( pool == null || PORT == null && USER ==null && PASS == null && IP == null && DB_NAME == null) {
            PORT = port;
            IP = ipaddress;
            USER = user;
            PASS = password;
            DB_NAME = db_name;
            URL = "jdbc:postgresql://"+ IP +":" + PORT + "/" + db_name;
            pool = new PostgresqlPool(URL, USER, PASS);
        }
    }


    public static Connection getConnection(){
         Connection connection = pool.getConnection();
        return connection;
    }
    public static boolean releaseConnection(Connection connection){
        return pool.releaseConn(connection);
    }
}
