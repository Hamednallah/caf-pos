package com.yourcompany.cafeteria.dao;

import com.yourcompany.cafeteria.model.Shift;
import com.yourcompany.cafeteria.model.User;

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

    public java.util.List<Shift> getAllShifts() throws Exception {
        java.util.List<Shift> shifts = new java.util.ArrayList<>();
        String sql = "SELECT s.id, s.start_time, s.end_time, s.starting_float, s.cashier_id, u.full_name as cashier_name " +
                     "FROM shift s JOIN \"user\" u ON s.cashier_id = u.id ORDER BY s.start_time DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Shift shift = new Shift();
                shift.setId(rs.getInt("id"));
                shift.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                if (rs.getTimestamp("end_time") != null) {
                    shift.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
                }
                shift.setStartingFloat(rs.getBigDecimal("starting_float"));

                User cashier = new User();
                cashier.setId(rs.getInt("cashier_id"));
                cashier.setFullName(rs.getString("cashier_name"));
                shift.setCashier(cashier);
                shift.setCashierId(cashier.getId());
                shifts.add(shift);
            }
        }
        return shifts;
    }
}
