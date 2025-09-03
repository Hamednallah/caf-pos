package com.yourcompany.cafeteria.dao;
import com.yourcompany.cafeteria.model.DateRangeReport;
import com.yourcompany.cafeteria.model.ShiftReport;

import java.sql.*;
import java.time.LocalDate;

public class ReportsDAO {
  private final Connection conn;

  public ReportsDAO(Connection c) {
    this.conn = c;
  }

  public ResultSet dailySales(LocalDate date) throws SQLException {
    PreparedStatement ps = conn.prepareStatement("SELECT i.name, SUM(oi.quantity) qty, SUM(oi.line_total) sales FROM order_item oi JOIN \"order\" o ON oi.order_id=o.id JOIN item i ON oi.item_id=i.id WHERE CAST(o.created_at AS DATE)=? GROUP BY i.name ORDER BY sales DESC");
    ps.setDate(1, Date.valueOf(date));
    return ps.executeQuery();
  }

  public ShiftReport getShiftReport(int shiftId) throws SQLException {
    ShiftReport report = new ShiftReport();

    String orderSql = "SELECT " +
            "COALESCE(SUM(total_amount), 0) AS total_sales, " +
            "COALESCE(SUM(discount_amount), 0) AS total_discounts, " +
            "COALESCE(SUM(CASE WHEN payment_method = 'CASH' THEN total_amount ELSE 0 END), 0) AS cash_total, " +
            "COALESCE(SUM(CASE WHEN payment_method = 'BANK' THEN total_amount ELSE 0 END), 0) AS bank_total, " +
            "COUNT(id) AS orders_count " +
            "FROM \"order\" " +
            "WHERE shift_id = ? AND status = 'FINALIZED' AND payment_confirmed = TRUE";

    try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
      ps.setInt(1, shiftId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          report.setTotalSales(rs.getBigDecimal("total_sales"));
          report.setTotalDiscounts(rs.getBigDecimal("total_discounts"));
          report.setCashTotal(rs.getBigDecimal("cash_total"));
          report.setBankTotal(rs.getBigDecimal("bank_total"));
          report.setOrdersCount(rs.getLong("orders_count"));
        }
      }
    }

    String expenseSql = "SELECT COALESCE(SUM(amount), 0) AS total_expenses FROM expense WHERE shift_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(expenseSql)) {
      ps.setInt(1, shiftId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          report.setTotalExpenses(rs.getBigDecimal("total_expenses"));
        }
      }
    }

    return report;
  }

  public DateRangeReport getDateRangeReport(LocalDate from, LocalDate to) throws SQLException {
    DateRangeReport report = new DateRangeReport();
    String sql = "SELECT " +
            "COALESCE(SUM(total_amount), 0) AS total_sales, " +
            "COUNT(id) AS orders_count " +
            "FROM \"order\" " +
            "WHERE created_at >= ? AND created_at < ? AND status = 'FINALIZED' AND payment_confirmed = TRUE";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
      ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          report.setTotalSales(rs.getBigDecimal("total_sales"));
          report.setOrdersCount(rs.getLong("orders_count"));
        }
      }
    }
    return report;
  }
}
