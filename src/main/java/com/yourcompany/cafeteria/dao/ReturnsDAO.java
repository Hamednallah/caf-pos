package com.yourcompany.cafeteria.dao;

import com.yourcompany.cafeteria.model.Return;
import com.yourcompany.cafeteria.model.ReturnedItem;

import java.sql.*;

public class ReturnsDAO {
    private final Connection conn;

    public ReturnsDAO(Connection conn) {
        this.conn = conn;
    }

    public Return createReturn(Return newReturn) throws SQLException {
        String returnSql = "INSERT INTO returns (order_id, reason, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(returnSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, newReturn.getOrderId());
            ps.setString(2, newReturn.getReason());
            ps.setTimestamp(3, Timestamp.valueOf(newReturn.getCreatedAt()));
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newReturn.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating return failed, no ID obtained.");
                }
            }
        }

        String returnedItemSql = "INSERT INTO returned_items (return_id, order_item_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(returnedItemSql)) {
            for (ReturnedItem item : newReturn.getReturnedItems()) {
                ps.setInt(1, newReturn.getId());
                ps.setInt(2, item.getOrderItemId());
                ps.setInt(3, item.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }

        return newReturn;
    }
}
