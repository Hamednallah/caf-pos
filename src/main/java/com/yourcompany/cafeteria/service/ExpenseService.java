package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.ExpensesDAO;
import com.yourcompany.cafeteria.model.Expense;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;

public class ExpenseService {
    private final ExpensesDAO dao;

    public ExpenseService(Connection c) {
        this.dao = new ExpensesDAO(c);
    }

    public int recordExpense(Expense e) throws Exception {
        if (e.getAmount() == null || e.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be positive.");
        }
        if (e.getDescription() == null || e.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Expense description cannot be empty.");
        }
        return dao.insert(e);
    }

    public ResultSet getExpensesByShift(int shiftId) throws Exception {
        return dao.listByShift(shiftId);
    }

    public int recordExpense(BigDecimal amount, String description, String category, Integer recordedBy, Integer shiftId) throws Exception {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be positive.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Expense description cannot be empty.");
        }
        
        Expense expense = new Expense();
        expense.setAmount(amount);
        expense.setDescription(description);
        expense.setCategory(category);
        expense.setRecordedBy(recordedBy);
        expense.setShiftId(shiftId);
        
        return dao.insert(expense);
    }
}
