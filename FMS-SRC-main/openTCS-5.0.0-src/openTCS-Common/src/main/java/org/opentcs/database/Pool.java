package org.opentcs.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface Pool {
    /**
     *
     * @return a connection from pool
     * @throws SQLException ahi
     */
    Connection getConnection() throws SQLException;
    /**
     *
     * @param connection 112011
     * @return connection to pool
     */
    boolean releaseConn(Connection connection);
}
