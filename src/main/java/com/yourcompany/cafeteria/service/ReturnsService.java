package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.ReturnsDAO;
import com.yourcompany.cafeteria.model.Return;
import com.yourcompany.cafeteria.model.ReturnedItem;
import com.yourcompany.cafeteria.util.DataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ReturnsService {
    private ReturnsDAO returnsDAO;

    public ReturnsService() throws SQLException {
        this.returnsDAO = new ReturnsDAO(DataSourceProvider.getConnection());
    }

    public Return processReturn(int orderId, String reason, List<ReturnedItem> itemsToReturn) throws SQLException {
        if (itemsToReturn == null || itemsToReturn.isEmpty()) {
            throw new IllegalArgumentException("No items selected for return.");
        }

        Return newReturn = new Return();
        newReturn.setOrderId(orderId);
        newReturn.setReason(reason);
        newReturn.setCreatedAt(LocalDateTime.now());
        newReturn.setReturnedItems(itemsToReturn);

        return returnsDAO.createReturn(newReturn);
    }
}
