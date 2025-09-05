package com.yourcompany.cafeteria.dao;

import com.yourcompany.cafeteria.model.Role;
import com.yourcompany.cafeteria.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsersDAO {
    private final Connection conn;
    private final String baseSelectSQL = "SELECT u.id, u.username, u.password_hash, u.full_name, u.active, r.id as role_id, r.name as role_name FROM \"user\" u JOIN role r ON u.role_id = r.id";

    public UsersDAO(Connection c) {
        this.conn = c;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setActive(rs.getBoolean("active"));

        Role role = new Role();
        role.setId(rs.getInt("role_id"));
        role.setName(rs.getString("role_name"));
        u.setRole(role);
        return u;
    }

    public User findByUsername(String username) throws SQLException {
        String sql = baseSelectSQL + " WHERE u.username=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    public User findById(int id) throws SQLException {
        String sql = baseSelectSQL + " WHERE u.id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    public int createUser(String username, String passwordHash, String fullName, int roleId) throws SQLException {
        String sql = "INSERT INTO \"user\"(username, password_hash, full_name, role_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setInt(4, roleId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    public List<User> listAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = baseSelectSQL + " ORDER BY u.username";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowToUser(rs));
                }
            }
        }
        return users;
    }

    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE \"user\" SET full_name = ?, role_id = ?" +
                (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty() ? ", password_hash = ?" : "") +
                " WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setInt(2, user.getRole().getId());
            int lastIndex = 3;
            if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
                ps.setString(lastIndex++, user.getPasswordHash());
            }
            ps.setInt(lastIndex, user.getId());
            ps.executeUpdate();
        }
    }

    public void updateUserStatus(int userId, boolean isActive) throws SQLException {
        String sql = "UPDATE \"user\" SET active = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isActive);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public List<Role> getAllRoles() throws SQLException {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT id, name FROM role ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Role role = new Role();
                role.setId(rs.getInt("id"));
                role.setName(rs.getString("name"));
                roles.add(role);
            }
        }
        return roles;
    }
}
