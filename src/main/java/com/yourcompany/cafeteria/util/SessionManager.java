package com.yourcompany.cafeteria.util;

import com.yourcompany.cafeteria.model.User;

/**
 * A simple static class to hold session information like the current user and active shift.
 */
public class SessionManager {

    private static User currentUser = null;
    private static Integer currentShiftId = null;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        SessionManager.currentUser = user;
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

    public static void clearSession() {
        currentUser = null;
        currentShiftId = null;
    }
}
