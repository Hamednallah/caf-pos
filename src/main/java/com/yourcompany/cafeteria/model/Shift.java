package com.yourcompany.cafeteria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Shift {
    private int id;
    private int cashierId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private BigDecimal startingFloat;

    // Getters and Setters

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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public BigDecimal getStartingFloat() {
        return startingFloat;
    }

    public void setStartingFloat(BigDecimal startingFloat) {
        this.startingFloat = startingFloat;
    }

    @Override
    public String toString() {
        return "Shift #" + id + " (" + startedAt + ")";
    }
}
