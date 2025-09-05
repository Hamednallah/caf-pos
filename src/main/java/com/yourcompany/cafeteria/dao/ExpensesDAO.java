package com.yourcompany.cafeteria.dao;

import com.yourcompany.cafeteria.model.Expense;

import java.sql.*;

public class ExpensesDAO {
    private final Connection conn;

    public ExpensesDAO(Connection c) {
        this.conn = c;
    }

    public int insert(Expense e) throws SQLException {
        String sql = "INSERT INTO expense(amount, description, category, user_id, shift_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, e.getAmount());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getCategory());
            if (e.getUserId() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, e.getUserId());
            }
            if (e.getShiftId() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, e.getShiftId());
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Inserting expense failed, no ID obtained.");
                }
            }
        }
    }

    public ResultSet listByShift(int shiftId) throws SQLException {
        String sql = "SELECT id, amount, description, category, user_id, recorded_at, shift_id FROM expense WHERE shift_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, shiftId);
        return ps.executeQuery();
    }
}
