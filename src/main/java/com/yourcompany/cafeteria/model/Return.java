package com.yourcompany.cafeteria.model;

import java.time.LocalDateTime;
import java.util.List;

public class Return {
    private Integer id;
    private Integer orderId;
    private LocalDateTime createdAt;
    private String reason;
    private List<ReturnedItem> returnedItems;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<ReturnedItem> getReturnedItems() {
        return returnedItems;
    }

    public void setReturnedItems(List<ReturnedItem> returnedItems) {
        this.returnedItems = returnedItems;
    }
}
