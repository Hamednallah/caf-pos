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
}
