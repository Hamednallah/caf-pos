package com.yourcompany.cafeteria.model;

import java.math.BigDecimal;

public class DateRangeReport {
    private BigDecimal totalSales;
    private long ordersCount;

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public long getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(long ordersCount) {
        this.ordersCount = ordersCount;
    }
}
