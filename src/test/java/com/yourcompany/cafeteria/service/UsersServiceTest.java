package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.dao.UsersDAO;
import com.yourcompany.cafeteria.model.User;
import com.yourcompany.cafeteria.util.DataSourceProvider;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class UsersServiceTest {

    private Connection connection;
    private UsersService usersService;

    @BeforeEach
    void setUp() throws SQLException {
        DataSourceProvider.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        connection = DataSourceProvider.getConnection();

        Flyway flyway = Flyway.configure().dataSource(DataSourceProvider.getDataSource()).load();
        flyway.migrate();

        usersService = new UsersService(connection);

        // clean up before each test
        connection.createStatement().execute("DELETE FROM \"user\"");
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void testFindByUsername() throws SQLException {
        // Given
        UsersDAO usersDAO = new UsersDAO(connection);
        usersDAO.createUser("testuser", "password", "Test User", 1);

        // When
        User foundUser = usersService.findByUsername("testuser");
        User notFoundUser = usersService.findByUsername("nonexistent");

        // Then
        assertNotNull(foundUser, "User should be found");
        assertEquals("testuser", foundUser.getUsername(), "Username should match");
        assertNull(notFoundUser, "User should not be found");
    }
}
