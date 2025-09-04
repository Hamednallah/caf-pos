package com.yourcompany.cafeteria.dao;

import com.yourcompany.cafeteria.model.Return;
import com.yourcompany.cafeteria.model.ReturnItem;

import java.sql.*;

public class ReturnsDAO {
    private final Connection conn;

    public ReturnsDAO(Connection c) {
        this.conn = c;
    }

    public int insertReturn(Return newReturn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // Insert into returns table
            String returnSql = "INSERT INTO returns (order_id, processed_by_user_id, total_refund_amount) VALUES (?, ?, ?)";
            int returnId;
            try (PreparedStatement ps = conn.prepareStatement(returnSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, newReturn.getOrderId());
                ps.setInt(2, newReturn.getProcessedByUserId());
                ps.setBigDecimal(3, newReturn.getTotalRefundAmount());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        returnId = rs.getInt(1);
                    } else {
                        throw new SQLException("Creating return failed, no ID obtained.");
                    }
                }
            }

            // Insert into return_item table
            String itemSql = "INSERT INTO return_item (return_id, order_item_id, quantity_returned) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (ReturnItem item : newReturn.getReturnItems()) {
                    ps.setInt(1, returnId);
                    ps.setInt(2, item.getOrderItemId());
                    ps.setInt(3, item.getQuantityReturned());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return returnId;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
