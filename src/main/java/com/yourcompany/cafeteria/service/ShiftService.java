package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.ShiftsDAO;

import java.math.BigDecimal;
import com.yourcompany.cafeteria.model.Shift;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ShiftService {
    private final ShiftsDAO dao;

    public ShiftService(Connection c) {
        this.dao = new ShiftsDAO(c);
    }

    public int startShift(int cashierId, BigDecimal startingFloat) throws Exception {
        if (startingFloat == null || startingFloat.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Starting float cannot be negative.");
        }
        return dao.startShift(cashierId, startingFloat);
    }

    public void endShift(int id) throws Exception {
        dao.endShift(id);
    }

    public ResultSet getActiveShiftForCashier(int cashierId) throws Exception {
        return dao.getActiveShiftForCashier(cashierId);
    }

    public List<Shift> listAllShifts() throws Exception {
        List<Shift> shifts = new ArrayList<>();
        try (ResultSet rs = dao.listAllShifts()) {
            while (rs.next()) {
                Shift shift = new Shift();
                shift.setId(rs.getInt("id"));
                shift.setCashierId(rs.getInt("cashier_id"));
                shift.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime());
                Timestamp endedAt = rs.getTimestamp("ended_at");
                if (endedAt != null) {
                    shift.setEndedAt(endedAt.toLocalDateTime());
                }
                shift.setStartingFloat(rs.getBigDecimal("starting_float"));
                shifts.add(shift);
            }
        }
        return shifts;
    }
}
