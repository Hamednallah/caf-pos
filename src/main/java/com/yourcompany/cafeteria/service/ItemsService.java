package com.yourcompany.cafeteria.service;
import com.yourcompany.cafeteria.dao.ItemsDAO; import com.yourcompany.cafeteria.model.Item; import java.sql.Connection; import java.util.List;
import java.math.BigDecimal;

public class ItemsService {
    private final ItemsDAO dao;

    public ItemsService(Connection c) {
        this.dao = new ItemsDAO(c);
    }

    private void validateItem(Item i) {
        if (i.getName() == null || i.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }
        if (i.getPrice() == null || i.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Item price must be positive.");
        }
    }

    public int add(Item i) throws Exception {
        validateItem(i);
        return dao.insert(i);
    }

    public List<Item> listAll() throws Exception {
        return dao.listAll();
    }

    public Item findById(int id) throws Exception {
        return dao.findById(id);
    }

    public void update(Item it) throws Exception {
        validateItem(it);
        dao.update(it);
    }

    public void delete(int id) throws Exception {
        dao.delete(id);
    }
}
