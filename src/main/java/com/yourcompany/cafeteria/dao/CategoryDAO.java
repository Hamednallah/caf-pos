package com.yourcompany.cafeteria.dao;

import com.yourcompany.cafeteria.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private final Connection conn;

    public CategoryDAO(Connection c) {
        this.conn = c;
    }

    public List<Category> listAll() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, description FROM category ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                categories.add(category);
            }
        }
        return categories;
    }

    public Category create(Category category) throws SQLException {
        String sql = "INSERT INTO category (name, description) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        }
        return category;
    }
}
