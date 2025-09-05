package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.model.Shift;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ShiftServiceTest {

    private Connection connection;
    private ShiftService shiftService;

    @BeforeEach
    void setUp() throws SQLException {
        DataSourceProvider.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        connection = DataSourceProvider.getConnection();

        // Clean the database before each test
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("DROP ALL OBJECTS");
        }

        Flyway flyway = Flyway.configure().dataSource(DataSourceProvider.getDataSource()).load();
        flyway.migrate();
        shiftService = new ShiftService(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testStartAndEndShift() throws Exception {
        int userId = 1; // Assuming user with ID 1 exists (seeded)
        BigDecimal startFloat = new BigDecimal("100.00");

        int shiftId = shiftService.startShift(userId, startFloat);
        assertTrue(shiftId > 0);

        Shift activeShift = shiftService.getActiveShiftForUser(userId);
        assertNotNull(activeShift);
        assertEquals(shiftId, activeShift.getId());

        shiftService.endShift(shiftId, new BigDecimal("200.00"));

        activeShift = shiftService.getActiveShiftForUser(userId);
        assertNull(activeShift);
    }

    @Test
    void testStartShiftWithNegativeFloat() {
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.startShift(1, new BigDecimal("-50.00"));
        });
    }

    @Test
    void testStartShiftWhenAlreadyActive() throws Exception {
        int userId = 1;
        shiftService.startShift(userId, new BigDecimal("100.00"));

        // Try to start another shift for the same user
        assertThrows(IllegalStateException.class, () -> {
            shiftService.startShift(userId, new BigDecimal("50.00"));
        });
    }
}
