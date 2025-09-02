package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.util.DataSourceProvider;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ShiftServiceTest {

    private Connection connection;
    private ShiftService shiftService;

    @BeforeEach
    void setUp() throws SQLException {
        DataSourceProvider.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        connection = DataSourceProvider.getConnection();
        Flyway flyway = Flyway.configure().dataSource(DataSourceProvider.getDataSource()).load();
        flyway.clean();
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
        int cashierId = 1; // Assuming user with ID 1 exists (seeded)
        BigDecimal startFloat = new BigDecimal("100.00");

        int shiftId = shiftService.startShift(cashierId, startFloat);
        assertTrue(shiftId > 0);

        try (ResultSet rs = shiftService.getActiveShiftForCashier(cashierId)) {
            assertTrue(rs.next());
            assertEquals(shiftId, rs.getInt("id"));
            assertFalse(rs.next());
        }

        shiftService.endShift(shiftId);

        try (ResultSet rs = shiftService.getActiveShiftForCashier(cashierId)) {
            assertFalse(rs.next());
        }
    }

    @Test
    void testStartShiftWithNegativeFloat() {
        assertThrows(IllegalArgumentException.class, () -> {
            shiftService.startShift(1, new BigDecimal("-50.00"));
        });
    }
}
