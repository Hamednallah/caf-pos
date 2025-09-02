package com.yourcompany.cafeteria.dao;
import java.sql.*;
public class SettingsDAO {
  private final Connection conn; public SettingsDAO(Connection c){ this.conn=c; }
  public String get(String key) throws SQLException { try (PreparedStatement ps = conn.prepareStatement("SELECT value FROM settings WHERE key=?")){ ps.setString(1, key); try (ResultSet rs = ps.executeQuery()){ return rs.next()? rs.getString(1): null; } } }
  public void set(String key, String value) throws SQLException { try (PreparedStatement ps = conn.prepareStatement("MERGE INTO settings(key,value) KEY(key) VALUES(?,?)")){ ps.setString(1, key); ps.setString(2, value); ps.executeUpdate(); } }
}
