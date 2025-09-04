package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.UsersDAO;
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
        return usersDAO.createUser(user.getUsername(), hashedPassword, user.getFullName(), user.getRoleId());
    }

    public User authenticate(String username, String plainTextPassword) throws SQLException {
        User user = findByUsername(username);
        if (user != null && BCrypt.checkpw(plainTextPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public List<User> listAllUsers() throws SQLException {
        return usersDAO.listAll();
    }

    public void updateUser(User user, String plainTextPassword) throws SQLException {
        if (plainTextPassword != null && !plainTextPassword.trim().isEmpty()) {
            String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
            user.setPasswordHash(hashedPassword);
        } else {
            // Keep the old password hash
            User oldUser = usersDAO.findById(user.getId());
            user.setPasswordHash(oldUser.getPasswordHash());
        }
        usersDAO.updateUser(user);
    }

    public void updateUserStatus(int userId, boolean isActive) throws SQLException {
        usersDAO.updateUserStatus(userId, isActive);
    }
}
