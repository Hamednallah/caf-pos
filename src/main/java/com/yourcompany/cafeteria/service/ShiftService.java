package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.ShiftsDAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;

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
        // Note: Returning a ResultSet is not ideal as it leaks a resource.
        // This should be refactored in the future to return a Shift model object.
        return dao.getActiveShiftForCashier(cashierId);
    }
}
