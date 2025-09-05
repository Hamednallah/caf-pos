package com.yourcompany.cafeteria.dao;

import com.yourcompany.cafeteria.model.Category;
import com.yourcompany.cafeteria.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemsDAO {
    private final Connection conn;
    private final String baseSelectSQL = "SELECT i.id, i.name, i.description, i.price, i.image_path, c.id as cat_id, c.name as cat_name, c.description as cat_desc FROM item i LEFT JOIN category c ON i.category_id = c.id";

    public ItemsDAO(Connection c) {
        this.conn = c;
    }

    private Item mapRowToItem(ResultSet rs) throws SQLException {
        Item i = new Item();
        i.setId(rs.getInt("id"));
        i.setName(rs.getString("name"));
        i.setDescription(rs.getString("description"));
        i.setPrice(rs.getBigDecimal("price"));
        i.setImagePath(rs.getString("image_path"));

        int categoryId = rs.getInt("cat_id");
        if (!rs.wasNull()) {
            Category cat = new Category();
            cat.setId(categoryId);
            cat.setName(rs.getString("cat_name"));
            cat.setDescription(rs.getString("cat_desc"));
            i.setCategory(cat);
        }
        return i;
    }

    public int insert(Item it) throws SQLException {
        String sql = "INSERT INTO item(name, description, price, category_id, image_path) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, it.getName());
            ps.setString(2, it.getDescription());
            ps.setBigDecimal(3, it.getPrice());
            if (it.getCategory() == null || it.getCategory().getId() == 0) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, it.getCategory().getId());
            }
            ps.setString(5, it.getImagePath());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    it.setId(rs.getInt(1));
                    return it.getId();
                } else {
                    throw new SQLException("Inserting item failed, no ID obtained.");
                }
            }
        }
    }

    public List<Item> listAll() throws SQLException {
        List<Item> out = new ArrayList<>();
        String sql = baseSelectSQL + " ORDER BY i.name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapRowToItem(rs));
            }
        }
        return out;
    }

    public Item findById(int id) throws SQLException {
        String sql = baseSelectSQL + " WHERE i.id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToItem(rs);
                }
            }
        }
        return null;
    }

    public void update(Item it) throws SQLException {
        String sql = "UPDATE item SET name=?, description=?, price=?, category_id=?, image_path=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, it.getName());
            ps.setString(2, it.getDescription());
            ps.setBigDecimal(3, it.getPrice());
            if (it.getCategory() == null || it.getCategory().getId() == 0) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, it.getCategory().getId());
            }
            ps.setString(5, it.getImagePath());
            ps.setInt(6, it.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM item WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
