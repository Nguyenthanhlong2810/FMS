package org.opentcs.database.access;

import org.opentcs.database.ConnectionPoolCreator;
import org.opentcs.database.entity.AubotConfiguration;

import java.sql.*;
import java.util.ArrayList;

public class ConfigurationDal
    implements DataAccessObject<AubotConfiguration> {

  @Override
  public ArrayList<AubotConfiguration> getAll() {
    Connection conn = ConnectionPoolCreator.getConnection();
    ArrayList<AubotConfiguration> configurations = new ArrayList<>();
    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT * FROM public.tbl_configuration");
      while (rs.next()) {
        configurations.add(new AubotConfiguration(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("value"),
            rs.getString("description")
        ));
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return configurations;
  }

  public String getConfiguration(String name) {
    Connection conn = ConnectionPoolCreator.getConnection();
    String value = null;
    try {
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM public.tbl_configuration WHERE name = ?");
      ps.setString(1, name);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        value = rs.getString("value");
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return value;
  }

  @Override
  public long add(AubotConfiguration conf) {
    Connection conn = ConnectionPoolCreator.getConnection();
    long id = 0;
    try {
      PreparedStatement ps = conn.prepareStatement("INSERT INTO tbl_configuration(name, value, description) VALUES(?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
      ps.setString(1, conf.getName());
      ps.setString(2, conf.getValue());
      ps.setString(3, conf.getDescription());
      ps.executeUpdate();
      ResultSet rs = ps.getGeneratedKeys();
      if (rs.next()) {
        id = rs.getLong("id");
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
      return 0;
    } finally {
      ConnectionPoolCreator.releaseConnection(conn);
    }
    return id;
  }

  @Override
  public boolean modify(AubotConfiguration conf) {
    Connection conn = ConnectionPoolCreator.getConnection();
    try {
      PreparedStatement ps = conn.prepareStatement("UPDATE tbl_configuration SET name = ?, value = ?, description = ? WHERE id = ?");
      ps.setString(1, conf.getName());
      ps.setString(2, conf.getValue());
      ps.setString(3, conf.getDescription());
      ps.setLong(4, conf.getId());
      ps.executeUpdate();
    }
    catch (SQLException e) {
      e.printStackTrace();
      return false;
    } finally {
      ConnectionPoolCreator.releaseConnection(conn);
    }
    return true;
  }

  @Override
  public boolean remove(long id) {
    Connection conn = ConnectionPoolCreator.getConnection();
    try {
      PreparedStatement ps = conn.prepareStatement("DELETE FROM tbl_configuration WHERE id = ?");
      ps.setLong(1, id);
      ps.executeUpdate();
    }
    catch (SQLException e) {
      e.printStackTrace();
      return false;
    } finally {
      ConnectionPoolCreator.releaseConnection(conn);
    }
    return true;
  }
}
