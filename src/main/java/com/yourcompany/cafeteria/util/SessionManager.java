package com.yourcompany.cafeteria.util;

/**
 * A simple static class to hold session information like the current user and active shift.
 * This is a temporary solution. In a real application, this would be replaced by a
 * more robust session management mechanism.
 */
public class SessionManager {
    // Hardcoded to user 'admin' (ID=1) for now.
    // This would be set upon successful login.
    private static Integer currentCashierId = 1;

    private static Integer currentShiftId = null;

    public static Integer getCurrentCashierId() {
        return currentCashierId;
    }

    public static void setCurrentCashierId(Integer cashierId) {
        SessionManager.currentCashierId = cashierId;
    }

    public static Integer getCurrentShiftId() {
        return currentShiftId;
    }

    public static void setCurrentShiftId(Integer shiftId) {
        SessionManager.currentShiftId = shiftId;
    }

    public static boolean isShiftActive() {
        return currentShiftId != null;
    }
}
