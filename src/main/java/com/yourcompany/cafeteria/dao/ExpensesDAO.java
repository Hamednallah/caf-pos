package com.yourcompany.cafeteria.dao;
import java.sql.*; import com.yourcompany.cafeteria.model.Expense;
public class ExpensesDAO {
  private final Connection conn; public ExpensesDAO(Connection c){ this.conn=c; }
  public int insert(Expense e) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO expense(amount,description,category,recorded_by,shift_id) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)){
      ps.setBigDecimal(1, e.getAmount()); ps.setString(2, e.getDescription()); ps.setString(3, e.getCategory()); if(e.getRecordedBy()==null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, e.getRecordedBy()); if(e.getShiftId()==null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, e.getShiftId()); ps.executeUpdate(); try (ResultSet rs=ps.getGeneratedKeys()){ rs.next(); return rs.getInt(1); }
    }
  }
  public java.util.List<Expense> listByShift(int shiftId) throws SQLException {
    java.util.List<Expense> expenses = new java.util.ArrayList<>();
    try (PreparedStatement ps = conn.prepareStatement("SELECT id,amount,description,category,recorded_by,recorded_at,shift_id FROM expense WHERE shift_id=?")) {
      ps.setInt(1, shiftId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Expense e = new Expense();
          e.setId(rs.getInt("id"));
          e.setAmount(rs.getBigDecimal("amount"));
          e.setDescription(rs.getString("description"));
          e.setCategory(rs.getString("category"));
          e.setRecordedBy(rs.getInt("recorded_by"));
          e.setRecordedAt(rs.getTimestamp("recorded_at").toLocalDateTime());
          e.setShiftId(rs.getInt("shift_id"));
          expenses.add(e);
        }
      }
    }
    return expenses;
  }
}
