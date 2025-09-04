package com.yourcompany.cafeteria.service;
import com.yourcompany.cafeteria.dao.OrdersDAO;
import com.yourcompany.cafeteria.model.Order;
import java.sql.Connection;
import java.math.BigDecimal;
import java.time.LocalDate;

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

    public Order findOrderByIdAndDate(int orderId, LocalDate date) throws Exception {
        return dao.findOrderByIdAndDate(orderId, date);
    }

    public void updateOrder(Order order) throws Exception {
        if (order == null || order.id == null) {
            throw new IllegalArgumentException("Order and order ID cannot be null.");
        }
        if (order.items == null || order.items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item.");
        }
        if (order.totalAmount == null || order.totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total amount must be positive.");
        }
        dao.updateOrderTransactional(order);
    }

    public java.util.List<Order> getRecentOrders(int limit) throws Exception {
        return dao.getRecentOrders(limit);
    }
}
