package com.yourcompany.cafeteria.dao;

import com.yourcompany.cafeteria.model.Shift;
import org.junit.jupiter.api.*;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

import com.yourcompany.cafeteria.util.TestDatabase;

import java.math.BigDecimal;

public class ShiftsDAOTest {
    Connection conn;

    @BeforeEach
    public void setup() throws Exception {
        conn = TestDatabase.open();
    }

    @AfterEach
    public void teardown() throws Exception {
        conn.close();
    }

    @Test
    public void startAndEndShift() throws Exception {
        ShiftsDAO dao = new ShiftsDAO(conn);
        int id = dao.startShift(1, new BigDecimal("100.00"));
        assertTrue(id > 0);

        Shift activeShift = dao.getActiveShiftForUser(1);
        assertNotNull(activeShift);
        assertEquals(id, activeShift.getId());

        dao.endShift(id, new BigDecimal("150.00"));

        activeShift = dao.getActiveShiftForUser(1);
        assertNull(activeShift);
    }
}
