package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.model.User;
import com.yourcompany.cafeteria.util.SessionManager;
import com.yourcompany.cafeteria.util.TestDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StartupServiceTest {

    private Connection conn;
    private UsersService usersService;
    private ShiftService shiftService;

    @BeforeEach
    public void setup() throws Exception {
        conn = TestDatabase.open();
        usersService = new UsersService(conn);
        shiftService = new ShiftService(conn);
        // Ensure session is clean before each test
        SessionManager.setCurrentShiftId(null);
        SessionManager.setCurrentCashierId(null);
    }

    @AfterEach
    public void teardown() throws Exception {
        conn.close();
    }

    @Test
    public void testStartup_withActiveShift_resumesShift() throws Exception {
        // 1. Arrange: Create a user and an active shift in the database.
        User cashier = new User();
        cashier.setUsername("testcashier_startup");
        cashier.setFullName("Startup Test Cashier");
        cashier.setPasswordHash(BCrypt.hashpw("password", BCrypt.gensalt()));
        cashier.setRoleId(2); // CASHIER
        int cashierId = usersService.createUser(cashier);
        SessionManager.setCurrentCashierId(cashierId);

        int activeShiftId = shiftService.startShift(cashierId, BigDecimal.ZERO);

        // Manually clear the session to simulate app restart
        SessionManager.setCurrentShiftId(null);

        // 2. Act: Run the startup service logic
        StartupService startupService = new StartupService(conn);
        startupService.checkAndResumeActiveShift();

        // 3. Assert: The session should now be populated with the active shift ID.
        assertNotNull(SessionManager.getCurrentShiftId(), "StartupService should have resumed the active shift.");
        assertEquals(activeShiftId, SessionManager.getCurrentShiftId(), "The resumed shift ID should match the one we created.");
    }
}
