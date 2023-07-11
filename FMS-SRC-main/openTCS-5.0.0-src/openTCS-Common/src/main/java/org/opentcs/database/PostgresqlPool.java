package org.opentcs.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresqlPool implements Pool {
    /**
     * This class's Bundle
     */
    //private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(I18nCommon.BUNDLE_PATH);
    /**
     * Username and Password for Postgresql
     */
    public static   String USER = null;
    public  static String PASS = null;
    /**
     * Connect String
     */
    public static String URL = null;
    /**
     * List of Connection available
     */
    private List<Connection> connectionPool;
    /**
     * List of connection are used
     */
    private final List<Connection> usedConnections = new ArrayList<>();
    /**
     * Max connection at a time
     */
    private static final int MAX_CONNECT = 10;

    /**
     * Constructor
     */
    PostgresqlPool(String url, String user, String password){
        USER = user;
        PASS = password;
        URL = url;
        create(URL,USER,PASS);
    }
    /**
     *
     * @param url
     * @param user
     * @param password
     * @return a connection
     * @throws SQLException
     * Create connection
     */
    private static Connection createConnection(String url, String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
    /**
     * Create List of connection
     */
    public void create(String url, String user, String password) {
        try {
            List<Connection> pool = new ArrayList<>(MAX_CONNECT);
            for (int i = 0; i < MAX_CONNECT; i++) {
                pool.add(createConnection(url, user, password));
            }
            this.connectionPool = pool;
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    public  Connection getConnection()  {
        try {
            if (connectionPool.isEmpty() ) {
                if (usedConnections.size() < MAX_CONNECT) {
                    connectionPool.add(createConnection(URL, USER, PASS));
                } else {
                    throw new RuntimeException("Maximum pool size reached, no available connections! ");
                }
            }
            Connection connection = connectionPool.remove(connectionPool.size() - 1);
            if (!connection.isValid(5000)) {
                connection = createConnection(URL, USER, PASS);
            }
            usedConnections.add(connection);
            return connection;
        }catch (SQLException e ){e.printStackTrace();}
       return null;
    }


    @Override
    public boolean  releaseConn(Connection connection) {
        try {
            if (connection.isClosed()){
                usedConnections.remove(connection);
                Connection conn = createConnection(URL,USER,PASS);
                 connectionPool.add(conn);
            }else{ connectionPool.add(connection);}
        }catch (Exception e){e.printStackTrace();}
        return usedConnections.remove(connection);
    }
    public int getSize() {
        return connectionPool.size() + usedConnections.size();
    }
    public void shutdown() throws SQLException {
        usedConnections.forEach(this::releaseConn);
        for (Connection c :connectionPool) {
            c.close();
        }
        connectionPool.clear();
    }

}

