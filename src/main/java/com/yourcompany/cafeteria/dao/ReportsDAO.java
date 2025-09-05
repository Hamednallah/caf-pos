package com.yourcompany.cafeteria.dao;

import com.yourcompany.cafeteria.model.DateRangeReport;
import com.yourcompany.cafeteria.model.ShiftReport;

import java.math.BigDecimal;
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
    report.setShiftId(shiftId);

    // Get shift details (including cashier name)
    String shiftSql = "SELECT s.start_time, s.end_time, s.starting_float, s.actual_cash, u.full_name " +
                      "FROM shift s JOIN \"user\" u ON s.user_id = u.id WHERE s.id = ?";
    try (PreparedStatement ps = conn.prepareStatement(shiftSql)) {
        ps.setInt(1, shiftId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                report.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                Timestamp endTime = rs.getTimestamp("end_time");
                if (endTime != null) {
                    report.setEndTime(endTime.toLocalDateTime());
                }
                report.setStartingFloat(rs.getBigDecimal("starting_float"));
                report.setActualCash(rs.getBigDecimal("actual_cash"));
                report.setCashierName(rs.getString("full_name"));
            } else {
                throw new SQLException("Shift not found with ID: " + shiftId);
            }
        }
    }

    // Get order aggregates
    String orderSql = "SELECT " +
            "COALESCE(SUM(total_amount), 0) AS total_sales, " +
            "COALESCE(SUM(CASE WHEN payment_method = 'CASH' THEN total_amount ELSE 0 END), 0) AS cash_total " +
            "FROM \"order\" " +
            "WHERE shift_id = ? AND status = 'FINALIZED' AND payment_confirmed = TRUE";

    try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
      ps.setInt(1, shiftId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          report.setTotalSales(rs.getBigDecimal("total_sales"));
          report.setCashTotal(rs.getBigDecimal("cash_total"));
        }
      }
    }

    // Get expense aggregates
    String expenseSql = "SELECT COALESCE(SUM(amount), 0) AS total_expenses FROM expense WHERE shift_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(expenseSql)) {
      ps.setInt(1, shiftId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          report.setTotalExpenses(rs.getBigDecimal("total_expenses"));
        }
      }
    }

    // Calculate difference if shift is closed
    if (report.getActualCash() != null) {
        BigDecimal difference = report.getActualCash().subtract(report.getExpectedCash());
        report.setDifference(difference);
    }

    return report;
  }

  public DateRangeReport getDateRangeReport(LocalDate from, LocalDate to) throws SQLException {
    DateRangeReport report = new DateRangeReport();
    String summarySql = "SELECT " +
            "COALESCE(SUM(total_amount), 0) AS total_sales, " +
            "COUNT(id) AS orders_count " +
            "FROM \"order\" " +
            "WHERE created_at >= ? AND created_at < ? AND status = 'FINALIZED' AND payment_confirmed = TRUE";

    try (PreparedStatement ps = conn.prepareStatement(summarySql)) {
      ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
      ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          report.setTotalSales(rs.getBigDecimal("total_sales"));
          report.setOrdersCount(rs.getLong("orders_count"));
        }
      }
    }

    String byDaySql = "SELECT CAST(created_at AS DATE) AS sale_date, SUM(total_amount) AS daily_total " +
            "FROM \"order\" " +
            "WHERE created_at >= ? AND created_at < ? AND status = 'FINALIZED' AND payment_confirmed = TRUE " +
            "GROUP BY sale_date ORDER BY sale_date";

    try (PreparedStatement ps = conn.prepareStatement(byDaySql)) {
        ps.setTimestamp(1, Timestamp.valueOf(from.atStartOfDay()));
        ps.setTimestamp(2, Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDate date = rs.getDate("sale_date").toLocalDate();
                report.getSalesByDay().put(date, rs.getBigDecimal("daily_total"));
            }
        }
    }

    return report;
  }
}
