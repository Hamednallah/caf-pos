package com.yourcompany.cafeteria.model;

import java.math.BigDecimal;

public class OrderItem {
    private Integer id;
    private Integer orderId;
    private Integer itemId;
    private int quantity;
    private BigDecimal priceAtPurchase;
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

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getLineTotal() {
        // If price is available, calculate on the fly for the cart view.
        if (priceAtPurchase != null) {
            return priceAtPurchase.multiply(new BigDecimal(quantity));
        }
        // Otherwise, return the value from the database (for loaded orders).
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
