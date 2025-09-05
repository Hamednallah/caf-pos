package com.yourcompany.cafeteria.dao;

import com.yourcompany.cafeteria.model.Order;
import com.yourcompany.cafeteria.model.OrderItem;

import java.sql.*;
import java.util.List;

public class OrdersDAO {
    private final Connection conn;

    public OrdersDAO(Connection c) {
        this.conn = c;
    }

    public int createOrderTransactional(Order order) throws SQLException {
        conn.setAutoCommit(false);
        try {
            int orderId;
            String orderSql = "INSERT INTO \"order\"(user_id, shift_id, total_amount, discount_amount, status, payment_method, payment_confirmed) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, order.getUserId() == null ? 1 : order.getUserId());
                if (order.getShiftId() == null) {
                    ps.setNull(2, Types.INTEGER);
                } else {
                    ps.setInt(2, order.getShiftId());
                }
                ps.setBigDecimal(3, order.getTotalAmount());
                ps.setBigDecimal(4, order.getDiscountAmount() == null ? java.math.BigDecimal.ZERO : order.getDiscountAmount());
                ps.setString(5, order.getStatus());
                ps.setString(6, order.getPaymentMethod());
                ps.setBoolean(7, order.isPaymentConfirmed());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    orderId = rs.getInt(1);
                }
            }

            if (order.getItems() != null && !order.getItems().isEmpty()) {
                String itemSql = "INSERT INTO order_item(order_id, item_id, quantity, price_at_purchase, line_total) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ips = conn.prepareStatement(itemSql)) {
                    for (OrderItem oi : order.getItems()) {
                        ips.setInt(1, orderId);
                        ips.setInt(2, oi.getItemId());
                        ips.setInt(3, oi.getQuantity());
                        ips.setBigDecimal(4, oi.getPriceAtPurchase());
                        ips.setBigDecimal(5, oi.getLineTotal());
                        ips.addBatch();
                    }
                    ips.executeBatch();
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            return orderId;
        } catch (SQLException ex) {
            conn.rollback();
            conn.setAutoCommit(true);
            throw ex;
        }
    }

    public ResultSet findOrdersBetween(Timestamp from, Timestamp to) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM \"order\" WHERE created_at BETWEEN ? AND ?");
        ps.setTimestamp(1, from);
        ps.setTimestamp(2, to);
        return ps.executeQuery();
    }

    public ResultSet getOrdersByShift(int shiftId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM \"order\" WHERE shift_id = ?");
        ps.setInt(1, shiftId);
        return ps.executeQuery();
    }
}
