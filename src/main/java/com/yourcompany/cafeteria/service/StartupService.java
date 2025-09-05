package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.model.User;
import com.yourcompany.cafeteria.util.SessionManager;

import java.sql.Connection;

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
            Integer userId = SessionManager.getCurrentUser().getId();
            if (userId != null) {
                com.yourcompany.cafeteria.model.Shift activeShift = shiftService.getActiveShiftForUser(userId);
                if (activeShift != null) {
                    SessionManager.setCurrentShiftId(activeShift.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
