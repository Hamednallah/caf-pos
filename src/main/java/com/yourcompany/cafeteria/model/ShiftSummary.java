package com.yourcompany.cafeteria.model;

import java.math.BigDecimal;

public class ShiftSummary {
    private int shiftId;
    private BigDecimal startingFloat;
    private BigDecimal totalSales;
    private BigDecimal totalCashSales;
    private BigDecimal totalBankSales;
    private BigDecimal totalExpenses;
    private BigDecimal expectedCashInDrawer;

    // Getters and Setters
    public int getShiftId() { return shiftId; }
    public void setShiftId(int shiftId) { this.shiftId = shiftId; }

    public BigDecimal getStartingFloat() { return startingFloat; }
    public void setStartingFloat(BigDecimal startingFloat) { this.startingFloat = startingFloat; }

    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }

    public BigDecimal getTotalCashSales() { return totalCashSales; }
    public void setTotalCashSales(BigDecimal totalCashSales) { this.totalCashSales = totalCashSales; }

    public BigDecimal getTotalBankSales() { return totalBankSales; }
    public void setTotalBankSales(BigDecimal totalBankSales) { this.totalBankSales = totalBankSales; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getExpectedCashInDrawer() { return expectedCashInDrawer; }
    public void setExpectedCashInDrawer(BigDecimal expectedCashInDrawer) { this.expectedCashInDrawer = expectedCashInDrawer; }
}
