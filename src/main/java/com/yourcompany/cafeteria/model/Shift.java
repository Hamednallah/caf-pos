package com.yourcompany.cafeteria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Shift {
    private int id;
    private int cashierId;
    private User cashier;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal startingFloat;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCashierId() {
        return cashierId;
    }

    public void setCashierId(int cashierId) {
        this.cashierId = cashierId;
    }

    public User getCashier() {
        return cashier;
    }

    public void setCashier(User cashier) {
        this.cashier = cashier;
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

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String cashierName = (cashier != null) ? cashier.getFullName() : "Unknown";
        return String.format("Shift #%d - %s (%s)", id, cashierName, startTime.format(formatter));
    }
}
