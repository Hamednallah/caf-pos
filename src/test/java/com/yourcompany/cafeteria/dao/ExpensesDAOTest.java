package com.yourcompany.cafeteria.dao;

import org.junit.jupiter.api.*;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

import com.yourcompany.cafeteria.util.TestDatabase;
import com.yourcompany.cafeteria.model.Expense;

import java.math.BigDecimal;

public class ExpensesDAOTest {
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
    public void insertExpense() throws Exception {
        ExpensesDAO dao = new ExpensesDAO(conn);
        Expense e = new Expense();
        e.setAmount(new BigDecimal("12.50"));
        e.setDescription("Bought sugar");
        e.setCategory("Ingredients");
        e.setUserId(1); // Changed from setRecordedBy
        int id = dao.insert(e);
        assertTrue(id > 0);
    }
}
