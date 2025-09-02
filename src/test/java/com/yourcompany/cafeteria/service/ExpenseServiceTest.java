package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.model.Expense;
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

class ExpenseServiceTest {

    private Connection connection;
    private ExpenseService expenseService;
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
        expenseService = new ExpenseService(connection);
        shiftService = new ShiftService(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testRecordAndListExpense() throws Exception {
        int shiftId = shiftService.startShift(1, new BigDecimal("100"));

        Expense expense = new Expense();
        expense.setAmount(new BigDecimal("15.50"));
        expense.setDescription("Office Supplies");
        expense.setCategory("Supplies");
        expense.setShiftId(shiftId);
        expense.setRecordedBy(1);

        int expenseId = expenseService.recordExpense(expense);
        assertTrue(expenseId > 0);

        try (ResultSet rs = expenseService.getExpensesByShift(shiftId)) {
            assertTrue(rs.next());
            assertEquals(expenseId, rs.getInt("id"));
            assertFalse(rs.next());
        }
    }

    @Test
    void testRecordInvalidExpense() {
        Expense expense1 = new Expense();
        expense1.setAmount(BigDecimal.ZERO);
        expense1.setDescription("Invalid");
        assertThrows(IllegalArgumentException.class, () -> expenseService.recordExpense(expense1));

        Expense expense2 = new Expense();
        expense2.setAmount(new BigDecimal("10.00"));
        expense2.setDescription(" ");
        assertThrows(IllegalArgumentException.class, () -> expenseService.recordExpense(expense2));
    }
}
