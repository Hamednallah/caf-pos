package com.yourcompany.cafeteria.model;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardStats {
    private Map<String, Integer> userCountsByRole;
    private BigDecimal totalSales;
    private BigDecimal salesForCurrentMonth;
    private BigDecimal averageDailySales;
    private BigDecimal averageMonthlySales;
    private BigDecimal totalExpenses;
    private BigDecimal expensesForCurrentMonth;
    private BigDecimal averageDailyExpenses;
    private BigDecimal averageMonthlyExpenses;
    private Map<String, BigDecimal> salesByMonth;

    // Getters and Setters
    public Map<String, Integer> getUserCountsByRole() {
        return userCountsByRole;
    }

    public void setUserCountsByRole(Map<String, Integer> userCountsByRole) {
        this.userCountsByRole = userCountsByRole;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getSalesForCurrentMonth() {
        return salesForCurrentMonth;
    }

    public void setSalesForCurrentMonth(BigDecimal salesForCurrentMonth) {
        this.salesForCurrentMonth = salesForCurrentMonth;
    }

    public BigDecimal getAverageDailySales() {
        return averageDailySales;
    }

    public void setAverageDailySales(BigDecimal averageDailySales) {
        this.averageDailySales = averageDailySales;
    }

    public BigDecimal getAverageMonthlySales() {
        return averageMonthlySales;
    }

    public void setAverageMonthlySales(BigDecimal averageMonthlySales) {
        this.averageMonthlySales = averageMonthlySales;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getExpensesForCurrentMonth() {
        return expensesForCurrentMonth;
    }

    public void setExpensesForCurrentMonth(BigDecimal expensesForCurrentMonth) {
        this.expensesForCurrentMonth = expensesForCurrentMonth;
    }

    public BigDecimal getAverageDailyExpenses() {
        return averageDailyExpenses;
    }

    public void setAverageDailyExpenses(BigDecimal averageDailyExpenses) {
        this.averageDailyExpenses = averageDailyExpenses;
    }

    public BigDecimal getAverageMonthlyExpenses() {
        return averageMonthlyExpenses;
    }

    public void setAverageMonthlyExpenses(BigDecimal averageMonthlyExpenses) {
        this.averageMonthlyExpenses = averageMonthlyExpenses;
    }

    public Map<String, BigDecimal> getSalesByMonth() {
        return salesByMonth;
    }

    public void setSalesByMonth(Map<String, BigDecimal> salesByMonth) {
        this.salesByMonth = salesByMonth;
    }
}
