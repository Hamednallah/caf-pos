package com.yourcompany.cafeteria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Shift {
    private int id;
    private int userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal startingFloat;
    private BigDecimal actualCash;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public BigDecimal getActualCash() {
        return actualCash;
    }

    public void setActualCash(BigDecimal actualCash) {
        this.actualCash = actualCash;
    }
}
