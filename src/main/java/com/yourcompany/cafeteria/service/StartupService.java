package com.yourcompany.cafeteria.service;

import java.sql.Connection;

import com.yourcompany.cafeteria.util.SessionManager;

import java.sql.Connection;
import java.sql.ResultSet;

public class StartupService {

    private final Connection connection;

    public StartupService(Connection connection) {
        this.connection = connection;
    }

    public void checkAndResumeActiveShift() {
        if (SessionManager.isShiftActive()) {
            return;
        }

        try {
            ShiftService shiftService = new ShiftService(this.connection);
            Integer cashierId = SessionManager.getCurrentCashierId();
            if (cashierId != null) {
                ResultSet rs = shiftService.getActiveShiftForCashier(cashierId);
                if (rs.next()) {
                    SessionManager.setCurrentShiftId(rs.getInt("id"));
                }
            }
        } catch (Exception e) {
            // In a real service, you might log this error or throw a custom exception
            e.printStackTrace();
        }
    }
}
