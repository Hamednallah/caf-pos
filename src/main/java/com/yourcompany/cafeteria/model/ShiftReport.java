package com.yourcompany.cafeteria.model;

import java.math.BigDecimal;

public class ShiftReport {

    private BigDecimal totalSales;
    private BigDecimal totalDiscounts;
    private BigDecimal cashTotal;
    private BigDecimal bankTotal;
    private long ordersCount;
    private BigDecimal totalExpenses;

    // Getters and Setters

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getTotalDiscounts() {
        return totalDiscounts;
    }

    public void setTotalDiscounts(BigDecimal totalDiscounts) {
        this.totalDiscounts = totalDiscounts;
    }

    public BigDecimal getCashTotal() {
        return cashTotal;
    }

    public void setCashTotal(BigDecimal cashTotal) {
        this.cashTotal = cashTotal;
    }

    public BigDecimal getBankTotal() {
        return bankTotal;
    }

    public void setBankTotal(BigDecimal bankTotal) {
        this.bankTotal = bankTotal;
    }

    public long getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(long ordersCount) {
        this.ordersCount = ordersCount;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }
}
