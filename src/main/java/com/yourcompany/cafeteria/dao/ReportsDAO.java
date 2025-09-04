package com.yourcompany.cafeteria.dao;
import java.sql.*; import java.time.LocalDate;
public class ReportsDAO {
  private final Connection conn; public ReportsDAO(Connection c){ this.conn=c; }
  public ResultSet dailySales(LocalDate date) throws SQLException {
    PreparedStatement ps = conn.prepareStatement("SELECT i.name, SUM(oi.quantity) qty, SUM(oi.line_total) sales FROM order_item oi JOIN \"order\" o ON oi.order_id=o.id JOIN item i ON oi.item_id=i.id WHERE CAST(o.created_at AS DATE)=? GROUP BY i.name ORDER BY sales DESC");
    ps.setDate(1, Date.valueOf(date)); return ps.executeQuery();
  }
}
