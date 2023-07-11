package org.opentcs.database.access;

import org.opentcs.database.ConnectionPoolCreator;

import java.sql.*;
import java.util.ArrayList;

public class EmailReceiptsDal implements DataAccessObject<String> {
    @Override
    public ArrayList<String> getAll() {
        Connection conn = ConnectionPoolCreator.getConnection();
        ArrayList<String> emails = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM public.tbl_emailaddress");
            while (rs.next()) {
                emails.add(rs.getString("email"));
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPoolCreator.releaseConnection(conn);
        }
        return emails;
    }

    @Override
    public long add(String email) {
        return 0;
    }

    @Override
    public boolean modify(String pair) {
        return false;
    }

    @Override
    public boolean remove(long id) {
        return false;
    }

    public void addNewEmailList(String[] emails) {
        Connection conn = ConnectionPoolCreator.getConnection();
        try {
            conn.setAutoCommit(false);

            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM public.tbl_emailaddress");
            stmt.execute("ALTER SEQUENCE tbl_emailaddress_id_seq RESTART WITH 1");
            stmt.close();

            StringBuffer sql;
            PreparedStatement ps = conn.prepareStatement("INSERT INTO public.tbl_emailaddress(email) VALUES(?)");
            for (String email : emails) {
                ps.setString(1, email);
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ConnectionPoolCreator.releaseConnection(conn);
        }
    }
}
