package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.UsersDAO;
import com.yourcompany.cafeteria.model.User;

import java.sql.Connection;
import java.sql.SQLException;

public class UsersService {
    private final UsersDAO usersDAO;

    public UsersService(Connection connection) {
        this.usersDAO = new UsersDAO(connection);
    }

    public User findByUsername(String username) throws SQLException {
        return usersDAO.findByUsername(username);
    }
}
