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
        return dao.getActiveShiftForCashier(cashierId);
    }

    public java.util.List<com.yourcompany.cafeteria.model.Shift> getAllShifts() throws Exception {
        return dao.getAllShifts();
    }
}
