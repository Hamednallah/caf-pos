package com.yourcompany.cafeteria.service;

import com.yourcompany.cafeteria.model.User;
import com.yourcompany.cafeteria.util.TestDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class UsersServiceTest {

    private Connection connection;
    private UsersService usersService;

    @BeforeEach
    void setUp() throws Exception {
        connection = TestDatabase.open();
        usersService = new UsersService(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void testCreateUser_hashesPassword() throws Exception {
        // 1. Arrange
        User user = new User();
        user.setUsername("test_hash");
        user.setFullName("Test Hash");
        user.setRoleId(2); // CASHIER

        // 2. Act
        usersService.createUser(user, "plain_password");

        // 3. Assert
        // Fetch the user directly from the DB to check the stored hash
        try (PreparedStatement ps = connection.prepareStatement("SELECT password_hash FROM \"user\" WHERE username = ?")) {
            ps.setString(1, "test_hash");
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "User should exist in the database");

            String storedHash = rs.getString("password_hash");
            assertNotEquals("plain_password", storedHash, "Password should not be stored in plain text.");
            assertTrue(BCrypt.checkpw("plain_password", storedHash), "Stored hash should match the original password.");
        }
    }

    @Test
    void testAuthenticate_withValidCredentials_returnsUser() throws Exception {
        // 1. Arrange
        User user = new User();
        user.setUsername("auth_user");
        user.setFullName("Auth User");
        user.setRoleId(1); // ADMIN
        usersService.createUser(user, "correct_password");

        // 2. Act
        User authenticatedUser = usersService.authenticate("auth_user", "correct_password");

        // 3. Assert
        assertNotNull(authenticatedUser, "Authentication should succeed with valid credentials.");
        assertEquals("auth_user", authenticatedUser.getUsername());
    }

    @Test
    void testAuthenticate_withInvalidCredentials_returnsNull() throws Exception {
        // 1. Arrange
        User user = new User();
        user.setUsername("auth_user_fail");
        user.setFullName("Auth User Fail");
        user.setRoleId(2);
        usersService.createUser(user, "correct_password");

        // 2. Act
        User authenticatedUser = usersService.authenticate("auth_user_fail", "wrong_password");

        // 3. Assert
        assertNull(authenticatedUser, "Authentication should fail with invalid credentials.");
    }

    @Test
    void testListAll_returnsAllCreatedUsers() throws Exception {
        // 1. Arrange
        // Note: The DB is cleaned before each test. The seed data does not create users.
        User user1 = new User();
        user1.setUsername("list_user_1");
        user1.setFullName("List User 1");
        user1.setRoleId(2);
        usersService.createUser(user1, "password");

        User user2 = new User();
        user2.setUsername("list_user_2");
        user2.setFullName("List User 2");
        user2.setRoleId(2);
        usersService.createUser(user2, "password");

        // 2. Act
        java.util.List<User> users = usersService.listAll();

        // 3. Assert
        assertEquals(2, users.size(), "Should return all users created within this test.");
    }
}
