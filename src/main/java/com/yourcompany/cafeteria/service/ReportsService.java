package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.ExpensesDAO;
import com.yourcompany.cafeteria.dao.OrdersDAO;
import com.yourcompany.cafeteria.dao.ReportsDAO;
import com.yourcompany.cafeteria.dao.ShiftsDAO;
import com.yourcompany.cafeteria.model.ShiftSummary;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ReportsService {
    private final Connection connection;
    private final ReportsDAO dao;

    public ReportsService(Connection c) {
        this.connection = c;
        this.dao = new ReportsDAO(c);
    }

    public ResultSet getDailySales(LocalDate date) throws Exception {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null.");
        }
        return dao.dailySales(date);
    }

    public ShiftSummary getShiftSummary(int shiftId) throws Exception {
        ShiftsDAO shiftsDAO = new ShiftsDAO(connection);
        OrdersDAO ordersDAO = new OrdersDAO(connection);
        ExpensesDAO expensesDAO = new ExpensesDAO(connection);

        ShiftSummary summary = new ShiftSummary();
        summary.setShiftId(shiftId);

        // 1. Get Starting Float
        com.yourcompany.cafeteria.model.Shift shift = new ShiftsDAO(connection).findById(shiftId);
        if (shift != null) {
            summary.setStartingFloat(shift.getStartingFloat());
        } else {
            throw new IllegalStateException("Shift not found with ID: " + shiftId);
        }

        // 2. Calculate Sales
        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalCashSales = BigDecimal.ZERO;
        BigDecimal totalBankSales = BigDecimal.ZERO;
        ResultSet ordersRs = ordersDAO.getOrdersByShift(shiftId);
        while (ordersRs.next()) {
            BigDecimal totalAmount = ordersRs.getBigDecimal("total_amount");
            BigDecimal discount = ordersRs.getBigDecimal("discount_amount");
            BigDecimal finalAmount = totalAmount.subtract(discount);
            totalSales = totalSales.add(finalAmount);
            if ("CASH".equalsIgnoreCase(ordersRs.getString("payment_method"))) {
                totalCashSales = totalCashSales.add(finalAmount);
            } else {
                totalBankSales = totalBankSales.add(finalAmount);
            }
        }
        summary.setTotalSales(totalSales);
        summary.setTotalCashSales(totalCashSales);
        summary.setTotalBankSales(totalBankSales);

        // 3. Calculate Expenses
        BigDecimal totalExpenses = BigDecimal.ZERO;
        ResultSet expensesRs = expensesDAO.listByShift(shiftId);
        while (expensesRs.next()) {
            totalExpenses = totalExpenses.add(expensesRs.getBigDecimal("amount"));
        }
        summary.setTotalExpenses(totalExpenses);

        // 4. Calculate Expected Cash
        BigDecimal expectedCash = summary.getStartingFloat()
                                      .add(summary.getTotalCashSales())
                                      .subtract(summary.getTotalExpenses());
        summary.setExpectedCashInDrawer(expectedCash);

        return summary;
    }

    public com.yourcompany.cafeteria.model.ShiftReport getShiftReport(int shiftId) throws Exception {
        return dao.getShiftReport(shiftId);
    }

    public com.yourcompany.cafeteria.model.DateRangeReport getDateRangeReport(LocalDate from, LocalDate to) throws Exception {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid date range.");
        }
        return dao.getDateRangeReport(from, to);
    }
}
