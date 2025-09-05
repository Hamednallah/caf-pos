package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.ExpensesDAO;
import com.yourcompany.cafeteria.dao.OrdersDAO;
import com.yourcompany.cafeteria.dao.UsersDAO;
import com.yourcompany.cafeteria.model.DashboardStats;

import java.sql.Connection;
import java.sql.SQLException;

public class DashboardService {
    private final UsersDAO usersDAO;
    private final OrdersDAO ordersDAO;
    private final ExpensesDAO expensesDAO;

    public DashboardService(Connection connection) {
        this.usersDAO = new UsersDAO(connection);
        this.ordersDAO = new OrdersDAO(connection);
        this.expensesDAO = new ExpensesDAO(connection);
    }

    public DashboardStats getDashboardStats() throws SQLException {
        DashboardStats stats = new DashboardStats();

        stats.setUserCountsByRole(usersDAO.countUsersByRole());
        stats.setTotalSales(ordersDAO.getTotalSales());
        stats.setSalesForCurrentMonth(ordersDAO.getSalesForCurrentMonth());
        stats.setAverageDailySales(ordersDAO.getAverageDailySales());
        stats.setAverageMonthlySales(ordersDAO.getAverageMonthlySales());
        stats.setTotalExpenses(expensesDAO.getTotalExpenses());
        stats.setExpensesForCurrentMonth(expensesDAO.getExpensesForCurrentMonth());
        stats.setAverageDailyExpenses(expensesDAO.getAverageDailyExpenses());
        stats.setAverageMonthlyExpenses(expensesDAO.getAverageMonthlyExpenses());
        stats.setSalesByMonth(ordersDAO.getSalesByMonth());

        return stats;
    }
}
