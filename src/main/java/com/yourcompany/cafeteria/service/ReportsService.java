package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.ReportsDAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ReportsService {
    private final ReportsDAO dao;

    public ReportsService(Connection c) {
        this.dao = new ReportsDAO(c);
    }

    public ResultSet getDailySales(LocalDate date) throws Exception {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null.");
        }
        // Note: Returning a ResultSet is not ideal as it leaks a resource.
        // This should be refactored in the future to return a list of model objects.
        return dao.dailySales(date);
    }
}
