package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.ReturnsDAO;
import com.yourcompany.cafeteria.model.Return;

import java.sql.Connection;
import java.sql.SQLException;

public class ReturnsService {
    private final ReturnsDAO returnsDAO;

    public ReturnsService(Connection connection) {
        this.returnsDAO = new ReturnsDAO(connection);
    }

    public int processReturn(Return newReturn) throws SQLException {
        if (newReturn == null) {
            throw new IllegalArgumentException("Return object cannot be null.");
        }
        if (newReturn.getReturnItems() == null || newReturn.getReturnItems().isEmpty()) {
            throw new IllegalArgumentException("Return must have at least one item.");
        }
        if (newReturn.getTotalRefundAmount() == null || newReturn.getTotalRefundAmount().signum() <= 0) {
            throw new IllegalArgumentException("Total refund amount must be positive.");
        }
        // Additional validation can be added here, e.g., check if quantities are valid

        return returnsDAO.insertReturn(newReturn);
    }
}
