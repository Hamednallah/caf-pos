package com.yourcompany.cafeteria.dao;
import java.sql.*;
public class ShiftsDAO {
  private final Connection conn; public ShiftsDAO(Connection c){ this.conn=c; }
  public int startShift(int cashierId, java.math.BigDecimal startingFloat) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO shift(cashier_id,started_at,starting_float) VALUES (?,CURRENT_TIMESTAMP,?)", Statement.RETURN_GENERATED_KEYS)){ ps.setInt(1, cashierId); ps.setBigDecimal(2, startingFloat); ps.executeUpdate(); try(ResultSet rs=ps.getGeneratedKeys()){ rs.next(); return rs.getInt(1);} }
  }
  public void endShift(int id) throws SQLException { try (PreparedStatement ps = conn.prepareStatement("UPDATE shift SET ended_at=CURRENT_TIMESTAMP WHERE id=?")){ ps.setInt(1, id); ps.executeUpdate(); } }
  public ResultSet getActiveShiftForCashier(int cashierId) throws SQLException { PreparedStatement ps = conn.prepareStatement("SELECT id,started_at,ended_at,starting_float FROM shift WHERE cashier_id=? AND ended_at IS NULL"); ps.setInt(1, cashierId); return ps.executeQuery(); }
  public ResultSet getShiftById(int shiftId) throws SQLException {
    PreparedStatement ps = conn.prepareStatement("SELECT * FROM shift WHERE id = ?");
    ps.setInt(1, shiftId);
    return ps.executeQuery();
  }
}
