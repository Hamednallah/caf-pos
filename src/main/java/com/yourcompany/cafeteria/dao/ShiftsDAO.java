package com.yourcompany.cafeteria.dao;

import com.yourcompany.cafeteria.model.Shift;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShiftsDAO {
    private final Connection conn;

    public ShiftsDAO(Connection c) {
        this.conn = c;
    }

    public int startShift(int userId, BigDecimal startingFloat) throws SQLException {
        String sql = "INSERT INTO shift(user_id, start_time, starting_float) VALUES (?, CURRENT_TIMESTAMP, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setBigDecimal(2, startingFloat);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Starting shift failed, no ID obtained.");
                }
            }
        }
    }

    public void endShift(int id, BigDecimal actualCash) throws SQLException {
        String sql = "UPDATE shift SET end_time=CURRENT_TIMESTAMP, actual_cash=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, actualCash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public Shift getActiveShiftForUser(int userId) throws SQLException {
        String sql = "SELECT id, user_id, start_time, end_time, starting_float, actual_cash FROM shift WHERE user_id=? AND end_time IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToShift(rs);
                }
            }
        }
        return null;
    }

    public Shift findById(int shiftId) throws SQLException {
        String sql = "SELECT id, user_id, start_time, end_time, starting_float, actual_cash FROM shift WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToShift(rs);
                }
            }
        }
        return null;
    }

    public List<Shift> getAllShifts() throws SQLException {
        List<Shift> shifts = new ArrayList<>();
        String sql = "SELECT id, user_id, start_time, end_time, starting_float, actual_cash FROM shift ORDER BY start_time DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                shifts.add(mapRowToShift(rs));
            }
        }
        return shifts;
    }

    private Shift mapRowToShift(ResultSet rs) throws SQLException {
        Shift shift = new Shift();
        shift.setId(rs.getInt("id"));
        shift.setUserId(rs.getInt("user_id"));
        shift.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        Timestamp endTime = rs.getTimestamp("end_time");
        if (endTime != null) {
            shift.setEndTime(endTime.toLocalDateTime());
        }
        shift.setStartingFloat(rs.getBigDecimal("starting_float"));
        shift.setActualCash(rs.getBigDecimal("actual_cash"));
        return shift;
    }
}
