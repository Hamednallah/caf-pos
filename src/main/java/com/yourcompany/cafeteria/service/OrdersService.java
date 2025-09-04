package com.yourcompany.cafeteria.service;
import com.yourcompany.cafeteria.dao.OrdersDAO;
import com.yourcompany.cafeteria.model.Order;
import java.sql.Connection;
import java.math.BigDecimal;

public class OrdersService {
    private final OrdersDAO dao;

    public OrdersService(Connection c) {
        this.dao = new OrdersDAO(c);
    }

    public int create(Order o) throws Exception {
        if (o == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }
        if (o.items == null || o.items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item.");
        }
        if (o.totalAmount == null || o.totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total amount must be positive.");
        }
        return dao.createOrderTransactional(o);
    }

    public Order findById(int orderId) throws Exception {
        return dao.findById(orderId);
    }
}
