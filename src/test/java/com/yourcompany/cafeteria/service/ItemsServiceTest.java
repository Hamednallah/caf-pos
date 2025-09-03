package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.model.Item;
import com.yourcompany.cafeteria.util.TestDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemsServiceTest {

    private Connection connection;
    private ItemsService itemsService;

    @BeforeEach
    void setUp() throws Exception {
        // Use the standard test database utility to ensure a clean, migrated state
        connection = TestDatabase.open();
        itemsService = new ItemsService(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testAddAndFindItem() throws Exception {
        Item item = new Item();
        item.setName("Test Coffee");
        item.setPrice(new BigDecimal("2.50"));
        item.setDescription("A test coffee");

        int id = itemsService.add(item);
        assertTrue(id > 0);

        Item found = itemsService.findById(id);
        assertNotNull(found);
        assertEquals("Test Coffee", found.getName());
    }

    @Test
    void testAddInvalidItem() {
        Item item1 = new Item();
        item1.setName("");
        item1.setPrice(new BigDecimal("1.00"));
        assertThrows(IllegalArgumentException.class, () -> itemsService.add(item1));

        Item item2 = new Item();
        item2.setName("Bad Coffee");
        item2.setPrice(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> itemsService.add(item2));

        Item item3 = new Item();
        item3.setName("Bad Coffee");
        item3.setPrice(new BigDecimal("-1.00"));
        assertThrows(IllegalArgumentException.class, () -> itemsService.add(item3));
    }

    @Test
    void testUpdateItem() throws Exception {
        Item item = new Item();
        item.setName("Original Name");
        item.setPrice(new BigDecimal("10.00"));
        int id = itemsService.add(item);

        Item toUpdate = itemsService.findById(id);
        toUpdate.setName("Updated Name");
        itemsService.update(toUpdate);

        Item updated = itemsService.findById(id);
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void testListAllItems() throws Exception {
        Item item1 = new Item();
        item1.setName("Test Item 1");
        item1.setDescription("Desc 1");
        item1.setPrice(new BigDecimal("1.00"));
        itemsService.add(item1);

        Item item2 = new Item();
        item2.setName("Test Item 2");
        item2.setDescription("Desc 2");
        item2.setPrice(new BigDecimal("2.00"));
        itemsService.add(item2);

        // The database is cleaned and seeded before each test.
        // Seed data has 3 items. This test adds 2 more.
        List<Item> items = itemsService.listAll();
        assertEquals(5, items.size());
    }

    @Test
    void testDeleteItem() throws Exception {
        Item item = new Item();
        item.setName("Delete Me");
        item.setDescription("Desc");
        item.setPrice(new BigDecimal("5.00"));
        int id = itemsService.add(item);

        itemsService.delete(id);
        Item deleted = itemsService.findById(id);
        assertNull(deleted);
    }
}
