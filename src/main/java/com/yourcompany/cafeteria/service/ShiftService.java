package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.ShiftsDAO;
import com.yourcompany.cafeteria.model.Shift;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class ShiftService {
    private final ShiftsDAO dao;

    public ShiftService(Connection c) {
        this.dao = new ShiftsDAO(c);
    }

    public int startShift(int userId, BigDecimal startingFloat) throws Exception {
        if (startingFloat == null || startingFloat.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Starting float cannot be negative.");
        }
        // Check if user already has an active shift
        if (dao.getActiveShiftForUser(userId) != null) {
            throw new IllegalStateException("User already has an active shift.");
        }
        return dao.startShift(userId, startingFloat);
    }

    public void endShift(int id, BigDecimal actualCash) throws Exception {
        if (actualCash == null || actualCash.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Actual cash amount cannot be negative.");
        }
        dao.endShift(id, actualCash);
    }

    public Shift getActiveShiftForUser(int userId) throws Exception {
        return dao.getActiveShiftForUser(userId);
    }

    public List<Shift> getAllShifts() throws Exception {
        return dao.getAllShifts();
    }
}
