package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.util.SessionManager;
import com.yourcompany.cafeteria.model.User;

import java.sql.Connection;
import java.sql.ResultSet;

public class StartupService {

    private final Connection connection;

    public StartupService(Connection connection) {
        this.connection = connection;
    }

    public void checkAndResumeActiveShift() {
        if (SessionManager.isShiftActive() || SessionManager.getCurrentUser() == null) {
            return;
        }

        try {
            ShiftService shiftService = new ShiftService(this.connection);
            Integer cashierId = SessionManager.getCurrentUser().getId();
            if (cashierId != null) {
                ResultSet rs = shiftService.getActiveShiftForCashier(cashierId);
                if (rs.next()) {
                    SessionManager.setCurrentShiftId(rs.getInt("id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
