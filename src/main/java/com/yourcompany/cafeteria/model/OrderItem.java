package com.yourcompany.cafeteria.model;

import javafx.beans.property.SimpleIntegerProperty;

import java.math.BigDecimal;

public class OrderItem {
    private final SimpleIntegerProperty returnQuantity = new SimpleIntegerProperty(0);
    private Integer id;
    private Integer orderId;
    private Integer itemId;
    private int quantity;
    private BigDecimal lineTotal;

    // This is for display purposes in the cart, not persisted.
    private transient String itemName;

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

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public int getReturnQuantity() {
        return returnQuantity.get();
    }

    public SimpleIntegerProperty returnQuantityProperty() {
        return returnQuantity;
    }

    public void setReturnQuantity(int returnQuantity) {
        this.returnQuantity.set(returnQuantity);
    }
}
