package com.yourcompany.cafeteria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Return {
    private Integer id;
    private int orderId;
    private LocalDateTime returnedAt;
    private int processedByUserId;
    private BigDecimal totalRefundAmount;
    private List<ReturnItem> returnItems;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public int getProcessedByUserId() {
        return processedByUserId;
    }

    public void setProcessedByUserId(int processedByUserId) {
        this.processedByUserId = processedByUserId;
    }

    public BigDecimal getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public void setTotalRefundAmount(BigDecimal totalRefundAmount) {
        this.totalRefundAmount = totalRefundAmount;
    }

    public List<ReturnItem> getReturnItems() {
        return returnItems;
    }

    public void setReturnItems(List<ReturnItem> returnItems) {
        this.returnItems = returnItems;
    }
}
