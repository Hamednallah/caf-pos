package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.UsersDAO;
import com.yourcompany.cafeteria.model.Role;
import com.yourcompany.cafeteria.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UsersService {
    private final UsersDAO usersDAO;

    public UsersService(Connection connection) {
        this.usersDAO = new UsersDAO(connection);
    }

    public User findByUsername(String username) throws SQLException {
        return usersDAO.findByUsername(username);
    }

    public int createUser(User user, String plainTextPassword) throws SQLException {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
        user.setPasswordHash(hashedPassword);
        return usersDAO.createUser(user.getUsername(), user.getPasswordHash(), user.getFullName(), user.getRole().getId());
    }

    public User authenticate(String username, String plainTextPassword) throws SQLException {
        User user = findByUsername(username);
        if (user != null && BCrypt.checkpw(plainTextPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public List<User> listAll() throws SQLException {
        return usersDAO.listAll();
    }

    public void updateUser(User user, String plainTextPassword) throws SQLException {
        if (plainTextPassword != null && !plainTextPassword.trim().isEmpty()) {
            String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
            user.setPasswordHash(hashedPassword);
        } else {
            user.setPasswordHash(null);
        }
        usersDAO.updateUser(user);
    }

    public void toggleUserStatus(int userId) throws SQLException {
        User user = usersDAO.findById(userId);
        if (user != null) {
            usersDAO.updateUserStatus(userId, !user.isActive());
        }
    }

    public List<Role> getAllRoles() throws SQLException {
        return usersDAO.getAllRoles();
    }
}
