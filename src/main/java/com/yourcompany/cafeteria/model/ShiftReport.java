package com.yourcompany.cafeteria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShiftReport {

    private int shiftId;
    private String cashierName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal startingFloat;
    private BigDecimal totalSales;
    private BigDecimal totalDiscounts;
    private BigDecimal cashTotal;
    private BigDecimal bankTotal;
    private long ordersCount;
    private BigDecimal totalExpenses;
    private BigDecimal actualCash;
    private BigDecimal difference;

    // Calculated property
    public BigDecimal getExpectedCash() {
        BigDecimal start = startingFloat != null ? startingFloat : BigDecimal.ZERO;
        BigDecimal cashIn = cashTotal != null ? cashTotal : BigDecimal.ZERO;
        BigDecimal expense = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        return start.add(cashIn).subtract(expense);
    }


    // Standard Getters and Setters

    public int getShiftId() {
        return shiftId;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getStartingFloat() {
        return startingFloat;
    }

    public void setStartingFloat(BigDecimal startingFloat) {
        this.startingFloat = startingFloat;
    }

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

    public BigDecimal getActualCash() {
        return actualCash;
    }

    public void setActualCash(BigDecimal actualCash) {
        this.actualCash = actualCash;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
    }
}
