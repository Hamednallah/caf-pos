package com.yourcompany.cafeteria.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

public class DateRangeReport {
    private BigDecimal totalSales;
    private long ordersCount;
    private Map<LocalDate, BigDecimal> salesByDay = new TreeMap<>();

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

    public Map<LocalDate, BigDecimal> getSalesByDay() {
        return salesByDay;
    }

    public void setSalesByDay(Map<LocalDate, BigDecimal> salesByDay) {
        this.salesByDay = salesByDay;
    }
}
